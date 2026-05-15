package nl.vintageforlife.poc.ui;

import nl.vintageforlife.poc.domain.Deliverer;
import nl.vintageforlife.poc.domain.Route;
import nl.vintageforlife.poc.domain.Stop;
import nl.vintageforlife.poc.logic.RouteExecution;
import nl.vintageforlife.poc.logic.RouteManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gebruikersinterface voor het bezorgteam. Bij Vintage for Life rijdt er
 * altijd een tweetal mee, dus de gebruiker kiest hier zowel de driver als
 * de assistent. Op basis van die selectie worden de toegewezen routes
 * getoond en kunnen stops afgerond worden. Bij elke afronding werkt
 * RouteExecution de ETA's van de volgende stops bij.
 */
public class DelivererUI extends JFrame {

    private final RouteManagement routeManagement;
    private final RouteExecution routeExecution = new RouteExecution();

    private final JComboBox<Deliverer> driverBox;
    private final JComboBox<Deliverer> assistantBox;
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

        // ------- Bovenbalk: driver + assistent + route selectie -------
        driverBox = new JComboBox<>(routeManagement.getDeliverers().toArray(new Deliverer[0]));
        driverBox.addActionListener(e -> refreshRoutes());

        assistantBox = new JComboBox<>(routeManagement.getDeliverers().toArray(new Deliverer[0]));
        // Standaard de tweede persoon als assistent zodat het paar niet gelijk is.
        if (assistantBox.getItemCount() > 1) assistantBox.setSelectedIndex(1);
        assistantBox.addActionListener(e -> refreshRoutes());

        routeBox = new JComboBox<>();
        routeBox.addActionListener(e -> refreshStops());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Driver:"));
        top.add(driverBox);
        top.add(new JLabel("Assistent:"));
        top.add(assistantBox);
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

        // ------- Onderbalk: knoppen + status -------
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

    /**
     * Vult de routelijst met alle routes waar de geselecteerde driver
     * of de geselecteerde assistent aan toegewezen is. Zo zien beide
     * teamleden direct dezelfde route(s) staan.
     */
    private void refreshRoutes() {
        Deliverer driver = (Deliverer) driverBox.getSelectedItem();
        Deliverer assistant = (Deliverer) assistantBox.getSelectedItem();
        routeBox.removeAllItems();

        Set<Route> assigned = new LinkedHashSet<>();
        if (driver != null) assigned.addAll(driver.getAssignedRoutes());
        if (assistant != null) assigned.addAll(assistant.getAssignedRoutes());

        for (Route r : assigned) {
            routeBox.addItem(r);
        }
        refreshStops();
    }

    /** Werkt de stoptabel + statusregel bij voor de geselecteerde route. */
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
        statusLabel.setText("Route status: " + r.getStatus()
                + "   |   Driver: " + driverName
                + "   |   Assistent: " + assistName
                + "   |   Afstand: " + String.format("%.1f", r.getTotalDistanceKm()) + " km ");
    }

    /**
     * Start de geselecteerde route. Vereist dat zowel driver als assistent
     * gekozen zijn en dat het twee verschillende personen zijn.
     */
    private void startRoute() {
        Route r = (Route) routeBox.getSelectedItem();
        if (r == null) return;
        if (!validateCrewSelection()) return;
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
     * Rondt de in de tabel geselecteerde stop af. De ETA's van de volgende
     * stops worden door RouteExecution opnieuw berekend.
     */
    private void completeSelectedStop() {
        Route r = (Route) routeBox.getSelectedItem();
        if (r == null) return;
        if (!validateCrewSelection()) return;
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
        // In productie zou hier de echte tijdstempel uit het apparaat komen.
        int actual = s.getEta() + s.getOrder().getServiceMinutes();
        routeExecution.completeStop(r, s, actual);
        refreshStops();
    }

    /** Controleert dat driver en assistent gekozen en verschillend zijn. */
    private boolean validateCrewSelection() {
        Deliverer driver = (Deliverer) driverBox.getSelectedItem();
        Deliverer assistant = (Deliverer) assistantBox.getSelectedItem();
        if (driver == null || assistant == null) {
            JOptionPane.showMessageDialog(this,
                    "Kies zowel een driver als een assistent.",
                    "Niet mogelijk", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (driver == assistant) {
            JOptionPane.showMessageDialog(this,
                    "Driver en assistent moeten verschillende personen zijn.",
                    "Niet mogelijk", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /** Helper: minuten sinds 00:00 -> HH:mm. */
    private String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
