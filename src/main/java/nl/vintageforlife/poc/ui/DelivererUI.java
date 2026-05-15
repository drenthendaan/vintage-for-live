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
 * User interface for the delivery team. The user picks themselves from the
 * dropdown and then sees every route they are assigned to, regardless of
 * whether they are the driver or the assistant on that route. The partner
 * is shown in the status bar. On every stop completion RouteExecution
 * updates the ETAs of the following stops.
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
        setSize(800, 500);
        setLayout(new BorderLayout(8, 8));

        // ------- Top bar: crew member + route selection -------
        delivererBox = new JComboBox<>(routeManagement.getDeliverers().toArray(new Deliverer[0]));
        delivererBox.addActionListener(e -> refreshRoutes());

        routeBox = new JComboBox<>();
        routeBox.addActionListener(e -> refreshStops());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Medewerker:"));
        top.add(delivererBox);
        top.add(new JLabel("Route:"));
        top.add(routeBox);
        add(top, BorderLayout.NORTH);

        // ------- Stops table -------
        stopModel = new DefaultTableModel(
                new String[]{"#", "ETA", "Order", "Adres", "Tijdvenster", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        stopTable = new JTable(stopModel);
        JScrollPane scroll = new JScrollPane(stopTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Stops"));
        add(scroll, BorderLayout.CENTER);

        // ------- Bottom bar: buttons + status -------
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

    /** Called when new routes are available (from PlannerUI). */
    public void onRoutesChanged() {
        refreshRoutes();
    }

    /**
     * Fills the route list with every route the selected crew member is
     * assigned to, no matter whether they are the driver or the assistant.
     */
    private void refreshRoutes() {
        Deliverer d = (Deliverer) delivererBox.getSelectedItem();
        routeBox.removeAllItems();
        if (d == null) return;
        for (Route r : d.getAssignedRoutes()) {
            routeBox.addItem(r);
        }
        refreshStops();
    }

    /** Refreshes the stop table and the status line for the selected route. */
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
        String driverName  = r.getDriver()    != null ? r.getDriver().getName()    : "-";
        String assistName  = r.getAssistant() != null ? r.getAssistant().getName() : "-";
        statusLabel.setText("Dag: " + r.formattedDay()
                + "   |   Status: " + r.getStatus()
                + "   |   Driver: " + driverName
                + "   |   Assistent: " + assistName
                + "   |   Afstand: " + String.format("%.1f", r.getTotalDistanceKm()) + " km ");
    }

    /** Starts the selected route (only if it was approved by the planner). */
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

    /**
     * Completes the stop currently selected in the table. RouteExecution
     * recalculates the ETAs of the following stops.
     */
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
        // For the PoC we take the planned ETA as the "actual" completion time.
        // In production this would be the real timestamp from the device.
        int actual = s.getEta() + s.getOrder().getServiceMinutes();
        routeExecution.completeStop(r, s, actual);
        refreshStops();
    }

    /** Helper: minutes since 00:00 -> HH:mm. */
    private String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
