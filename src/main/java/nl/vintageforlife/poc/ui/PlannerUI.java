package nl.vintageforlife.poc.ui;

import nl.vintageforlife.poc.domain.Location;
import nl.vintageforlife.poc.domain.Order;
import nl.vintageforlife.poc.domain.Route;
import nl.vintageforlife.poc.domain.Stop;
import nl.vintageforlife.poc.logic.RouteManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * User interface for the planner (see the class diagram and the technical
 * design, chapter "Link with the requirements"). Here the planner can add
 * orders, generate routes and approve routes. For each route the table
 * shows which pair (driver + assistant) is assigned.
 */
public class PlannerUI extends JFrame {

    private final RouteManagement routeManagement;
    private final Runnable onRoutesChanged;

    private final DefaultTableModel orderModel;
    private final DefaultTableModel routeModel;
    private final JTable routeTable;

    public PlannerUI(RouteManagement routeManagement, Runnable onRoutesChanged) {
        super("Vintage for Life - Planner Portal");
        this.routeManagement = routeManagement;
        this.onRoutesChanged = onRoutesChanged;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout(8, 8));

        // ------- Orders table -------
        orderModel = new DefaultTableModel(
                new String[]{"Order", "Klant", "Adres", "Tijdvenster", "Kg", "Installatie"},
                0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable orderTable = new JTable(orderModel);
        JScrollPane orderScroll = new JScrollPane(orderTable);
        orderScroll.setBorder(BorderFactory.createTitledBorder("Orders"));

        // ------- Routes table -------
        routeModel = new DefaultTableModel(
                new String[]{"Route", "Dag", "Voertuig", "Driver", "Assistent", "Stops", "Belading (kg)", "Afstand (km)", "Status"},
                0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        routeTable = new JTable(routeModel);
        JScrollPane routeScroll = new JScrollPane(routeTable);
        routeScroll.setBorder(BorderFactory.createTitledBorder("Routes"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, orderScroll, routeScroll);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        // ------- Buttons -------
        JButton addOrderBtn = new JButton("Order toevoegen");
        addOrderBtn.addActionListener(e -> showAddOrderDialog());

        JButton routeGenerateBtn = new JButton("Routes genereren");
        routeGenerateBtn.addActionListener(e -> generateRoutes());

        JButton approveRouteBtn = new JButton("Geselecteerde route goedkeuren");
        approveRouteBtn.addActionListener(e -> approveSelectedRoute());

        JButton routeDetailBtn = new JButton("Route details bekijken");
        routeDetailBtn.addActionListener(e -> showRouteDetails());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addOrderBtn);
        buttons.add(routeGenerateBtn);
        buttons.add(approveRouteBtn);
        buttons.add(routeDetailBtn);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    /** Redraws both tables based on the current RouteManagement state. */
    public void refresh() {
        orderModel.setRowCount(0);
        for (Order order: routeManagement.getOrders()) {
            orderModel.addRow(new Object[]{
                    order.getOrderId(), order.getCustomerId(), order.getAddress().getAddress(),
                    formatTime(order.getTimeWindowStart()) + "-" + formatTime(order.getTimeWindowEnd()),
                    order.getWeightKg(),
                    order.requiresInstallation() ? "ja" : "nee"
            });
        }
        routeModel.setRowCount(0);
        for (Route route: routeManagement.getRoutes()) {
            String driverName  = route.getDriver()    != null ? route.getDriver().getName()    : "-";
            String assistName  = route.getAssistant() != null ? route.getAssistant().getName() : "-";
            routeModel.addRow(new Object[]{
                    route.getRouteId(), route.formattedDay(),
                    route.getVehicle().getVehicleId(),
                    driverName, assistName,
                    route.getStops().size(), route.totalWeightKg(),
                    String.format("%.1f", route.getTotalDistanceKm()),
                    route.getStatus()
            });
        }
    }

    /** Runs the algorithm and notifies the deliverer portal. */
    private void generateRoutes() {
        try {
            List<Route> created = routeManagement.generateRoutes();
            refresh();
            if (onRoutesChanged != null) onRoutesChanged.run();
            JOptionPane.showMessageDialog(this,
                    created.size() + " route(s) gegenereerd.",
                    "Klaar", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Fout bij genereren: " + ex.getMessage(),
                    "Fout", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Approves the route currently selected in the table. */
    private void approveSelectedRoute() {
        int row = routeTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecteer eerst een route.");
            return;
        }
        Route route = routeManagement.getRoutes().get(row);
        routeManagement.approveRoute(route);
        refresh();
        if (onRoutesChanged != null) onRoutesChanged.run();
    }

    /** Shows a textual overview of the selected route, including all stops. */
    private void showRouteDetails() {
        int row = routeTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecteer eerst een route.");
            return;
        }
        Route route = routeManagement.getRoutes().get(row);
        String driverName  = route.getDriver()    != null ? route.getDriver().getName()    : "-";
        String assistantName  = route.getAssistant() != null ? route.getAssistant().getName() : "-";

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("Route ").append(route.getRouteId()).append("\n");
        stringbuilder.append("Dag: ").append(route.formattedDay()).append("\n");
        stringbuilder.append("Voertuig: ").append(route.getVehicle()).append("\n");
        stringbuilder.append("Driver: ").append(driverName).append("\n");
        stringbuilder.append("Assistent: ").append(assistantName).append("\n");
        stringbuilder.append("Start: ").append(route.formattedStart()).append("\n\n");
        stringbuilder.append("Stops:\n");
        for (Stop stop: route.getStops()) {
            stringbuilder.append(" ").append(stop.getSequence()).append(". ")
              .append(stop.formattedEta()).append(" - ")
              .append(stop.getOrder().getOrderId()).append(" @ ")
              .append(stop.getOrder().getAddress()).append("\n");
        }
        JTextArea textArea = new JTextArea(stringbuilder.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Route details", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Dialog for adding a new order by hand (handy for demos). */
    private void showAddOrderDialog() {
        JTextField idField = new JTextField("ORD-" + (routeManagement.getOrders().size() + 1));
        JTextField customerField = new JTextField("KLANT-X");
        JTextField addressField = new JTextField("Voorbeeldstraat 1, Zwolle");
        JTextField latField = new JTextField("52.5125");
        JTextField lonField = new JTextField("6.0944");
        JTextField timeWindowStartField = new JTextField("09:00");
        JTextField timeWindowEndField = new JTextField("17:00");
        JTextField weightField = new JTextField("20");
        JCheckBox installCheck = new JCheckBox("Installatie nodig (+30 min)");

        JPanel panel = new JPanel(new GridLayout(0, 2, 4, 4));
        panel.add(new JLabel("Order ID:"));
        panel.add(idField);
        panel.add(new JLabel("Klant ID:"));
        panel.add(customerField);
        panel.add(new JLabel("Adres:"));
        panel.add(addressField);
        panel.add(new JLabel("Latitude:"));
        panel.add(latField);
        panel.add(new JLabel("Longitude:"));
        panel.add(lonField);
        panel.add(new JLabel("Tijdvenster start (HH:mm):"));
        panel.add(timeWindowStartField);
        panel.add(new JLabel("Tijdvenster eind (HH:mm):"));
        panel.add(timeWindowEndField);
        panel.add(new JLabel("Gewicht (kg):"));
        panel.add(weightField);
        panel.add(new JLabel(""));
        panel.add(installCheck);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nieuwe order", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Order order = new Order(
                    idField.getText().trim(),
                    customerField.getText().trim(),
                    new Location(addressField.getText().trim(),
                            Double.parseDouble(latField.getText().trim()),
                            Double.parseDouble(lonField.getText().trim())),
                    parseTime(timeWindowStartField.getText().trim()),
                    parseTime(timeWindowEndField.getText().trim()),
                    Integer.parseInt(weightField.getText().trim()),
                    installCheck.isSelected());
            routeManagement.addOrder(order);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ongeldige invoer: " + ex.getMessage(),
                    "Fout", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Helper: HH:mm -> minutes since 00:00. */
    private int parseTime(String s) {
        String[] parts = s.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    /** Helper: minutes since 00:00 -> HH:mm. */
    private String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
