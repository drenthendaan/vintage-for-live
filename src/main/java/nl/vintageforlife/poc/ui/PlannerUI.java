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
 * Gebruikersinterface voor de planner (zie klassendiagram en TO hoofdstuk
 * "Verbinding met de Requirements"). Hier kan de planner orders aanmaken,
 * routes genereren en routes goedkeuren.
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
        setSize(900, 600);
        setLayout(new BorderLayout(8, 8));

        // ------- Orders tabel -------
        orderModel = new DefaultTableModel(
                new String[]{"Order", "Klant", "Adres", "Tijdvenster", "Kg", "Installatie"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable orderTable = new JTable(orderModel);
        JScrollPane orderScroll = new JScrollPane(orderTable);
        orderScroll.setBorder(BorderFactory.createTitledBorder("Orders"));

        // ------- Routes tabel -------
        routeModel = new DefaultTableModel(
                new String[]{"Route", "Voertuig", "Stops", "Belading (kg)", "Afstand (km)", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        routeTable = new JTable(routeModel);
        JScrollPane routeScroll = new JScrollPane(routeTable);
        routeScroll.setBorder(BorderFactory.createTitledBorder("Routes"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, orderScroll, routeScroll);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        // ------- Knoppen -------
        JButton addOrderBtn = new JButton("Order toevoegen");
        addOrderBtn.addActionListener(e -> showAddOrderDialog());

        JButton genBtn = new JButton("Routes genereren");
        genBtn.addActionListener(e -> generateRoutes());

        JButton approveBtn = new JButton("Geselecteerde route goedkeuren");
        approveBtn.addActionListener(e -> approveSelectedRoute());

        JButton detailBtn = new JButton("Route details bekijken");
        detailBtn.addActionListener(e -> showRouteDetails());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addOrderBtn);
        buttons.add(genBtn);
        buttons.add(approveBtn);
        buttons.add(detailBtn);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        orderModel.setRowCount(0);
        for (Order o : routeManagement.getOrders()) {
            orderModel.addRow(new Object[]{
                    o.getOrderId(), o.getCustomerId(), o.getAddress().getAddress(),
                    formatTime(o.getTimeWindowStart()) + "-" + formatTime(o.getTimeWindowEnd()),
                    o.getWeightKg(),
                    o.requiresInstallation() ? "ja" : "nee"
            });
        }
        routeModel.setRowCount(0);
        for (Route r : routeManagement.getRoutes()) {
            routeModel.addRow(new Object[]{
                    r.getRouteId(), r.getVehicle().getVehicleId(),
                    r.getStops().size(), r.totalWeightKg(),
                    String.format("%.1f", r.getTotalDistanceKm()),
                    r.getStatus()
            });
        }
    }

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

    private void showRouteDetails() {
        int row = routeTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecteer eerst een route.");
            return;
        }
        Route route = routeManagement.getRoutes().get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Route ").append(route.getRouteId()).append("\n");
        sb.append("Voertuig: ").append(route.getVehicle()).append("\n");
        sb.append("Bezorger(s): ").append(route.getDeliverers()).append("\n");
        sb.append("Start: ").append(route.formattedStart()).append("\n\n");
        sb.append("Stops:\n");
        for (Stop s : route.getStops()) {
            sb.append(" ").append(s.getSequence()).append(". ")
              .append(s.formattedEta()).append(" - ")
              .append(s.getOrder().getOrderId()).append(" @ ")
              .append(s.getOrder().getAddress()).append("\n");
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta),
                "Route details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAddOrderDialog() {
        JTextField idField = new JTextField("ORD-" + (routeManagement.getOrders().size() + 1));
        JTextField customerField = new JTextField("KLANT-X");
        JTextField addressField = new JTextField("Voorbeeldstraat 1, Zwolle");
        JTextField latField = new JTextField("52.5125");
        JTextField lonField = new JTextField("6.0944");
        JTextField twStartField = new JTextField("09:00");
        JTextField twEndField = new JTextField("17:00");
        JTextField weightField = new JTextField("20");
        JCheckBox installCheck = new JCheckBox("Installatie nodig (+30 min)");

        JPanel panel = new JPanel(new GridLayout(0, 2, 4, 4));
        panel.add(new JLabel("Order ID:"));        panel.add(idField);
        panel.add(new JLabel("Klant ID:"));        panel.add(customerField);
        panel.add(new JLabel("Adres:"));           panel.add(addressField);
        panel.add(new JLabel("Latitude:"));        panel.add(latField);
        panel.add(new JLabel("Longitude:"));       panel.add(lonField);
        panel.add(new JLabel("Tijdvenster start (HH:mm):")); panel.add(twStartField);
        panel.add(new JLabel("Tijdvenster eind (HH:mm):"));  panel.add(twEndField);
        panel.add(new JLabel("Gewicht (kg):"));    panel.add(weightField);
        panel.add(new JLabel(""));                 panel.add(installCheck);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Nieuwe order", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Order o = new Order(
                    idField.getText().trim(),
                    customerField.getText().trim(),
                    new Location(addressField.getText().trim(),
                            Double.parseDouble(latField.getText().trim()),
                            Double.parseDouble(lonField.getText().trim())),
                    parseTime(twStartField.getText().trim()),
                    parseTime(twEndField.getText().trim()),
                    Integer.parseInt(weightField.getText().trim()),
                    installCheck.isSelected());
            routeManagement.addOrder(o);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ongeldige invoer: " + ex.getMessage(),
                    "Fout", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parseTime(String s) {
        String[] parts = s.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
