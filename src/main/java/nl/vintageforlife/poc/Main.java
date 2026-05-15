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
 * Entry point of the PoC. Loads sample data (Zwolle area) and opens both
 * portals side by side: the Planner Portal and the Deliverer Portal.
 */
public class Main {

    public static void main(String[] args) {
        RouteManagement rm = setupSampleData();
        SwingUtilities.invokeLater(() -> {
            DelivererUI deliverer = new DelivererUI(rm);
            PlannerUI planner = new PlannerUI(rm, deliverer::onRoutesChanged);

            // Planner on the left, Deliverer on the right.
            planner.setLocation(50, 80);
            deliverer.setLocation(1000, 80);
            planner.setVisible(true);
            deliverer.setVisible(true);
        });
    }

    /** Builds a sample data set: depot, vehicles, crew members and orders. */
    private static RouteManagement setupSampleData() {
        RouteManagement rm = new RouteManagement();

        // Depot in Zwolle.
        Location depot = new Location("Depot Zwolle", 52.5168, 6.0830);
        rm.setDepot(depot);
        rm.setDayStart(8 * 60);  // 08:00

        // Two delivery vans.
        rm.addVehicle(new Vehicle("BUS-01", 500, depot));
        rm.addVehicle(new Vehicle("BUS-02", 500, depot));

        // Four crew members: enough for two routes with a driver + assistant
        // each. The role assignment per route happens in
        // RouteManagement.generateRoutes().
        rm.addDeliverer(new Deliverer("D-01", "Bram"));
        rm.addDeliverer(new Deliverer("D-02", "Sanne"));
        rm.addDeliverer(new Deliverer("D-03", "Joris"));
        rm.addDeliverer(new Deliverer("D-04", "Lotte"));

        // Sample orders spread across the wider Zwolle region.
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
        rm.addOrder(new Order("ORD-7", "K-107",
                new Location("Wilhelminastraat 9, Dalfsen", 52.5111, 6.2528),
                9 * 60, 12 * 60, 30, false));
        rm.addOrder(new Order("ORD-8", "K-108",
                new Location("Burgemeester Backxlaan 4, Nieuwleusen", 52.5917, 6.2917),
                10 * 60, 14 * 60, 70, true));
        rm.addOrder(new Order("ORD-9", "K-109",
                new Location("Grotestraat 11, Raalte", 52.3833, 6.2778),
                12 * 60, 16 * 60, 45, false));
        rm.addOrder(new Order("ORD-10", "K-110",
                new Location("Langstraat 7, Wijhe", 52.3833, 6.1389),
                9 * 60, 13 * 60, 20, false));
        rm.addOrder(new Order("ORD-11", "K-111",
                new Location("Jan Schamhartstraat 2, Olst", 52.3389, 6.1083),
                13 * 60, 17 * 60, 50, false));
        rm.addOrder(new Order("ORD-12", "K-112",
                new Location("Dorpsweg 18, IJsselmuiden", 52.5667, 5.9444),
                10 * 60, 12 * 60, 65, true));
        rm.addOrder(new Order("ORD-13", "K-113",
                new Location("Hoogstraat 14, Hasselt", 52.5917, 6.0944),
                11 * 60, 15 * 60, 40, false));
        rm.addOrder(new Order("ORD-14", "K-114",
                new Location("Handelskade 3, Zwartsluis", 52.6361, 6.0833),
                9 * 60, 13 * 60, 30, false));
        rm.addOrder(new Order("ORD-15", "K-115",
                new Location("Hoofdstraat 25, Meppel", 52.6961, 6.1944),
                12 * 60, 17 * 60, 90, true));
        rm.addOrder(new Order("ORD-16", "K-116",
                new Location("Langestraat 6, Genemuiden", 52.6322, 5.9722),
                14 * 60, 17 * 60, 35, false));
        rm.addOrder(new Order("ORD-17", "K-117",
                new Location("Markt 4, Ommen", 52.5167, 6.4167),
                10 * 60, 14 * 60, 55, false));
        rm.addOrder(new Order("ORD-18", "K-118",
                new Location("Kerkplein 2, Lemelerveld", 52.4889, 6.3417),
                13 * 60, 16 * 60, 28, false));

        return rm;
    }
}
