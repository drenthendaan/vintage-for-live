package nl.vintageforlife.poc.logic;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import nl.vintageforlife.poc.domain.Location;
import nl.vintageforlife.poc.domain.Order;
import nl.vintageforlife.poc.domain.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper around Jsprit (see the technical design, chapter "Chosen
 * algorithm"). Builds a VehicleRoutingProblem from our domain objects,
 * solves it and returns the result as a plain Java object.
 *
 * In production the cost matrix would be filled with GraphHopper driving
 * distances; in this PoC we use crow-flies (haversine) distances at 50 km/h.
 */
public class DeliveryAlgorithm {

    /** Average driving speed for the PoC (km/h). */
    private static final double AVG_SPEED_KMH = 50.0;
    /** Fixed id for the depot location in the Jsprit cost matrix. */
    private static final String DEPOT_ID = "depot";

    /** A single planned trip for one vehicle. */
    public static class PlannedRoute {
        public final Vehicle vehicle;
        public final List<Order> orderedOrders;
        public final double distanceKm;

        public PlannedRoute(Vehicle vehicle, List<Order> orderedOrders, double distanceKm) {
            this.vehicle = vehicle;
            this.orderedOrders = orderedOrders;
            this.distanceKm = distanceKm;
        }
    }

    /** Result of a route calculation (all trips + any unassigned orders). */
    public static class RoutingResult {
        public final List<PlannedRoute> plannedRoutes;
        public final List<Order> unassigned;
        public final double totalDistanceKm;

        public RoutingResult(List<PlannedRoute> plannedRoutes,
                             List<Order> unassigned, double totalDistanceKm) {
            this.plannedRoutes = plannedRoutes;
            this.unassigned = unassigned;
            this.totalDistanceKm = totalDistanceKm;
        }
    }

    /**
     * Solves the routing problem for the given vehicles, orders and depot.
     * The result is, per vehicle, a list of orders in the optimal sequence,
     * plus any orders that could not be assigned.
     */
    public RoutingResult solve(List<Vehicle> vehicles, List<Order> orders,
                               Location depot, int dayStartMinutes) {

        // 1. Build a cost matrix with haversine distances between all locations.
        Map<String, Location> locById = new HashMap<>();
        locById.put(DEPOT_ID, depot);
        for (Order o : orders) {
            locById.put(o.getOrderId(), o.getAddress());
        }

        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder =
                VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        List<String> ids = new ArrayList<>(locById.keySet());
        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                String a = ids.get(i);
                String b = ids.get(j);
                double km = locById.get(a).distanceKm(locById.get(b));
                double minutes = (km / AVG_SPEED_KMH) * 60.0;
                matrixBuilder.addTransportDistance(a, b, km);
                matrixBuilder.addTransportTime(a, b, minutes);
            }
        }
        VehicleRoutingTransportCostsMatrix costs = matrixBuilder.build();

        // 2. Build the VRP problem: translate vehicles and orders into Jsprit objects.
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setRoutingCost(costs);

        Map<String, Vehicle> vehicleById = new HashMap<>();
        for (Vehicle v : vehicles) {
            vehicleById.put(v.getVehicleId(), v);
            VehicleType type = VehicleTypeImpl.Builder.newInstance("type-" + v.getVehicleId())
                    .addCapacityDimension(0, v.getCapacityKg())
                    .build();
            com.graphhopper.jsprit.core.problem.Location depotLoc =
                    com.graphhopper.jsprit.core.problem.Location.Builder.newInstance()
                            .setId(DEPOT_ID).build();
            VehicleImpl jVehicle = VehicleImpl.Builder.newInstance(v.getVehicleId())
                    .setStartLocation(depotLoc)
                    .setType(type)
                    .setEarliestStart(dayStartMinutes)
                    .setLatestArrival(dayStartMinutes + 8 * 60)  // 8-hour workday (UR-10)
                    .build();
            vrpBuilder.addVehicle(jVehicle);
        }

        Map<String, Order> orderById = new HashMap<>();
        for (Order o : orders) {
            orderById.put(o.getOrderId(), o);
            com.graphhopper.jsprit.core.problem.Location stopLoc =
                    com.graphhopper.jsprit.core.problem.Location.Builder.newInstance()
                            .setId(o.getOrderId()).build();
            Service service = Service.Builder.newInstance(o.getOrderId())
                    .addSizeDimension(0, o.getWeightKg())   // FR-8 capacity
                    .setLocation(stopLoc)
                    .setServiceTime(o.getServiceMinutes()) // UR-21
                    .setTimeWindow(TimeWindow.newInstance( // UR-11
                            o.getTimeWindowStart(), o.getTimeWindowEnd()))
                    .build();
            vrpBuilder.addJob(service);
        }

        VehicleRoutingProblem vrp = vrpBuilder.build();

        // 3. Solve with Jsprit (ruin & recreate). 200 iterations is plenty
        //    for the PoC and stays well within UR-24 (route within 1 min).
        VehicleRoutingAlgorithm algo = Jsprit.createAlgorithm(vrp);
        algo.setMaxIterations(200);
        VehicleRoutingProblemSolution best = Solutions.bestOf(algo.searchSolutions());

        // 4. Translate the Jsprit result back into our domain objects.
        List<PlannedRoute> planned = new ArrayList<>();
        for (VehicleRoute jRoute : best.getRoutes()) {
            if (jRoute.getActivities().isEmpty()) continue;
            Vehicle v = vehicleById.get(jRoute.getVehicle().getId());
            List<Order> orderedOrders = new ArrayList<>();
            for (TourActivity act : jRoute.getActivities()) {
                String locId = act.getLocation().getId();
                Order order = orderById.get(locId);
                if (order != null) orderedOrders.add(order);
            }
            double km = routeDistanceKm(jRoute, locById);
            planned.add(new PlannedRoute(v, orderedOrders, km));
        }

        List<Order> unassigned = new ArrayList<>();
        for (com.graphhopper.jsprit.core.problem.job.Job j : best.getUnassignedJobs()) {
            Order o = orderById.get(j.getId());
            if (o != null) unassigned.add(o);
        }

        double total = planned.stream().mapToDouble(r -> r.distanceKm).sum();
        return new RoutingResult(planned, unassigned, total);
    }

    /** Helper: total distance from the depot, along all activities, back to the depot. */
    private double routeDistanceKm(VehicleRoute jRoute, Map<String, Location> locById) {
        double km = 0;
        Location prev = locById.get(DEPOT_ID);
        for (TourActivity act : jRoute.getActivities()) {
            Location curr = locById.get(act.getLocation().getId());
            if (curr != null) {
                km += prev.distanceKm(curr);
                prev = curr;
            }
        }
        // Return trip to the depot.
        km += prev.distanceKm(locById.get(DEPOT_ID));
        return km;
    }
}
