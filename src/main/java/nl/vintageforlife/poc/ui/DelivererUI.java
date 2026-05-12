package nl.vintageforlife.poc.ui;

import nl.vintageforlife.poc.domain.Deliverer;
import nl.vintageforlife.poc.domain.Route;
import nl.vintageforlife.poc.domain.Stop;
import nl.vintageforlife.poc.logic.RouteExecution;
import nl.vintageforlife.poc.logic.RouteManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Gebruikersinterface voor de bezorger. De bezorger kiest zijn naam, ziet de
 * toegewezen route en kan stops afronden. Bij elke afronding worden de ETA's
 * van de volgende stops bijgewerkt door RouteExecution.
 */
public class DelivererUI extends JFrame {

    private final RouteManagement routeManagement;
    private final RouteExecution routeExecution = new RouteExecution();

    private final JComboBox<Deliverer> delivererBox;
    private final JComboBox<Route> routeBox;
    private final DefaultTableModel stopModel;
    private final JTable stopTable;
    private final JLabel statusLabel;

    public DelivererUI(RouteManagement routeManagement) {
        super("Vintage for Life - Bezorger Portal");
        this.routeManagement = routeManagement;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLayout(new BorderLayout(8, 8));

        // ------- Bovenbalk: bezorger + route selectie -------
        delivererBox = new JComboBox<>(routeManagement.getDeliverers().toArray(new Deliverer[0]));
        delivererBox.addActionListener(e -> refreshRoutes());

        routeBox = new JComboBox<>();
        routeBox.addActionListener(e -> refreshStops());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Bezorger:"));
        top.add(delivererBox);
        top.add(new JLabel("Route:"));
        top.add(routeBox);
        add(top, BorderLayout.NORTH);

        // ------- Stops tabel -------
        stopModel = new DefaultTableModel(
                new String[]{"#", "ETA", "Order", "Adres", "Tijdvenster", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        stopTable = new JTable(stopModel);
        JScrollPane scroll = new JScrollPane(stopTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Stops"));
        add(scroll, BorderLayout.CENTER);

        // ------- Onderbalk -------
        statusLabel = new JLabel(" ");
        JButton startBtn = new JButton("Route starten");
        startBtn.addActionListener(e -> startRoute());
        JButton completeBtn = new JButton("Geselecteerde stop afronden");
        completeBtn.addActionListener(e -> completeSelectedStop());

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.add(startBtn);
        btns.add(completeBtn);
        bottom.add(btns, BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        refreshRoutes();
    }

    /** Aanroep wanneer er nieuwe routes zijn (vanuit PlannerUI). */
    public void onRoutesChanged() {
        refreshRoutes();
    }

    private void refreshRoutes() {
        Deliverer d = (Deliverer) delivererBox.getSelectedItem();
        routeBox.removeAllItems();
        if (d == null) return;
        for (Route r : d.getAssignedRoutes()) {
            routeBox.addItem(r);
        }
        refreshStops();
    }

    private void refreshStops() {
        stopModel.setRowCount(0);
        Route r = (Route) routeBox.getSelectedItem();
        if (r == null) {
            statusLabel.setText(" ");
            return;
        }
        for (Stop s : r.getStops()) {
            stopModel.addRow(new Object[]{
                    s.getSequence(),
                    s.formattedEta(),
                    s.getOrder().getOrderId(),
                    s.getOrder().getAddress().getAddress(),
                    formatTime(s.getOrder().getTimeWindowStart()) + "-"
                            + formatTime(s.getOrder().getTimeWindowEnd()),
                    s.isCompleted() ? "afgerond" : "open"
            });
        }
        statusLabel.setText("Route status: " + r.getStatus()
                + "   |   Afstand: " + String.format("%.1f", r.getTotalDistanceKm()) + " km ");
    }

    private void startRoute() {
        Route r = (Route) routeBox.getSelectedItem();
        if (r == null) return;
        if (r.getStatus() != Route.Status.GOEDGEKEURD) {
            JOptionPane.showMessageDialog(this,
                    "Route is nog niet goedgekeurd door de planner.",
                    "Niet mogelijk", JOptionPane.WARNING_MESSAGE);
            return;
        }
        routeExecution.startRoute(r);
        refreshStops();
    }

    private void completeSelectedStop() {
        Route r = (Route) routeBox.getSelectedItem();
        if (r == null) return;
        int row = stopTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecteer een stop.");
            return;
        }
        Stop s = r.getStops().get(row);
        if (s.isCompleted()) {
            JOptionPane.showMessageDialog(this, "Deze stop is al afgerond.");
            return;
        }
        if (r.getStatus() == Route.Status.GOEDGEKEURD) {
            routeExecution.startRoute(r);
        }
        // Voor de PoC nemen we de geplande ETA als "werkelijke" afrondtijd.
        // In productie zou hier de echte tijdstempel uit het apparaat gebruikt worden.
        int actual = s.getEta() + s.getOrder().getServiceMinutes();
        routeExecution.completeStop(r, s, actual);
        refreshStops();
    }

    private String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
