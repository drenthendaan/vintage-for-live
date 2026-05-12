package nl.vintageforlife.poc;

import nl.vintageforlife.poc.domain.Deliverer;
import nl.vintageforlife.poc.domain.Location;
import nl.vintageforlife.poc.domain.Order;
import nl.vintageforlife.poc.domain.Vehicle;
import nl.vintageforlife.poc.logic.RouteManagement;
import nl.vintageforlife.poc.ui.DelivererUI;
import nl.vintageforlife.poc.ui.PlannerUI;

import javax.swing.SwingUtilities;

/**
 * Startpunt van de PoC. Laadt voorbeelddata (Zwolle e.o.) en opent beide
 * portalen naast elkaar.
 */
public class Main {

    public static void main(String[] args) {
        RouteManagement rm = setupSampleData();
        SwingUtilities.invokeLater(() -> {
            DelivererUI deliverer = new DelivererUI(rm);
            PlannerUI planner = new PlannerUI(rm, deliverer::onRoutesChanged);

            planner.setLocation(50, 80);
            deliverer.setLocation(1000, 80);
            planner.setVisible(true);
            deliverer.setVisible(true);
        });
    }

    private static RouteManagement setupSampleData() {
        RouteManagement rm = new RouteManagement();

        // Depot in Zwolle.
        Location depot = new Location("Depot Zwolle", 52.5168, 6.0830);
        rm.setDepot(depot);
        rm.setDayStart(8 * 60);  // 08:00

        // Twee bezorgbusjes.
        rm.addVehicle(new Vehicle("BUS-01", 500, depot));
        rm.addVehicle(new Vehicle("BUS-02", 500, depot));

        // Twee bezorgers.
        rm.addDeliverer(new Deliverer("D-01", "Bezorger Bram"));
        rm.addDeliverer(new Deliverer("D-02", "Bezorger Sanne"));

        // Vijf voorbeeldorders rondom Zwolle.
        rm.addOrder(new Order("ORD-1", "K-101",
                new Location("Veerallee 12, Zwolle", 52.5151, 6.0796),
                9 * 60, 12 * 60, 35, false));
        rm.addOrder(new Order("ORD-2", "K-102",
                new Location("Diezerstraat 22, Zwolle", 52.5142, 6.0951),
                10 * 60, 13 * 60, 60, true));
        rm.addOrder(new Order("ORD-3", "K-103",
                new Location("Stationsweg 5, Wezep", 52.4720, 5.9347),
                9 * 60, 17 * 60, 80, false));
        rm.addOrder(new Order("ORD-4", "K-104",
                new Location("Hoofdstraat 8, Hattem", 52.4744, 6.0594),
                13 * 60, 16 * 60, 40, false));
        rm.addOrder(new Order("ORD-5", "K-105",
                new Location("Kerkstraat 3, Kampen", 52.5550, 5.9111),
                11 * 60, 15 * 60, 25, false));
        rm.addOrder(new Order("ORD-6", "K-106",
                new Location("Markt 1, Heerde", 52.3917, 6.0444),
                14 * 60, 17 * 60, 55, true));

        return rm;
    }
}
