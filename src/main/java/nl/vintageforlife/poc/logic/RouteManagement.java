package nl.vintageforlife.poc.logic;

import nl.vintageforlife.poc.domain.Deliverer;
import nl.vintageforlife.poc.domain.Location;
import nl.vintageforlife.poc.domain.Order;
import nl.vintageforlife.poc.domain.Route;
import nl.vintageforlife.poc.domain.Stop;
import nl.vintageforlife.poc.domain.Vehicle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Central planning class. Collects orders, vehicles and crew members,
 * invokes the algorithm and builds Route objects with their Stops from
 * the result. A planner can also approve a generated route here.
 *
 * At Vintage for Life every delivery is done by a pair: a driver and an
 * assistant. During route generation these roles are assigned automatically
 * based on the available crew members.
 */
public class RouteManagement {

    private final DeliveryAlgorithm algorithm = new DeliveryAlgorithm();
    private final List<Order> orders = new ArrayList<>();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Deliverer> deliverers = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();
    private Location depot;
    /** Default day start in minutes since 00:00 (default 08:00). */
    private int dayStartMinutes = 8 * 60;
    private int routeSequence = 1;
    private int stopSequence = 1;

    public void setDepot(Location depot) { this.depot = depot; }
    public void setDayStart(int dayStartMinutes) { this.dayStartMinutes = dayStartMinutes; }

    public List<Order> getOrders() { return orders; }
    public List<Vehicle> getVehicles() { return vehicles; }
    public List<Deliverer> getDeliverers() { return deliverers; }
    public List<Route> getRoutes() { return routes; }

    public void addOrder(Order order) { orders.add(order); }
    public void addVehicle(Vehicle vehicle) { vehicles.add(vehicle); }
    public void addDeliverer(Deliverer deliverer) { deliverers.add(deliverer); }

    /**
     * Generates routes from all open orders. Existing concept (not yet
     * approved) routes are discarded and recomputed. Each route is
     * automatically assigned a driver and an assistant.
     *
     * The crew is reused day after day: with N crew members we can run
     * floor(N / 2) routes per day. Any additional routes are pushed to the
     * next day(s) and get the same crew pairs again.
     */
    public List<Route> generateRoutes() {
        if (depot == null) {
            throw new IllegalStateException("Depot is niet ingesteld.");
        }
        // Remove old concepts; approved routes are left untouched.
        routes.removeIf(route -> route.getStatus() == Route.Status.CONCEPT);

        List<Order> openOrders = new ArrayList<>();
        for (Order order: orders) {
            if (!order.isDelivered() && !isInExistingRoute(order)) {
                openOrders.add(order);
            }
        }
         if (openOrders.isEmpty()) return new ArrayList<>();

        DeliveryAlgorithm.RoutingResult result =
                algorithm.solve(vehicles, openOrders, depot, dayStartMinutes);

        List<Route> newRoutes = new ArrayList<>();
        // Crew capacity: how many driver+assistant pairs fit in a single day.
        int pairsPerDay= deliverers.size() / 2;

        int routeIndex = 0;
        for (DeliveryAlgorithm.PlannedRoute plannedRoute: result.plannedRoutes) {
            Route route = new Route("R-" + routeSequence++, plannedRoute.vehicle, dayStartMinutes);
            route.setTotalDistanceKm(plannedRoute.distanceKm);

            // Walk through the planned stops and compute ETAs based on
            // crow-flies distance and 50 km/h (see DeliveryAlgorithm).
            int sequence = 1;
            int currentTime = dayStartMinutes;
            Location prevLocation = depot;

            for (Order order: plannedRoute.orderedOrders) {
                int travel = (int) Math.round(prevLocation.distanceKm(order.getAddress()) / 50.0 * 60.0);
                currentTime += travel;
                if (currentTime < order.getTimeWindowStart()) {
                    currentTime = order.getTimeWindowStart();  // wait for the time window
                }
                Stop stop = new Stop("S-" + stopSequence++, sequence++, order, currentTime);
                route.addStop(stop);
                currentTime += order.getServiceMinutes();
                prevLocation = order.getAddress();
            }

            // Determine the date and the crew pair for this route.
            // pairsPerDay == 0 means we have no crew at all; in that case
            // every route stays on today's date without crew assigned.
            if (pairsPerDay > 0) {
                int day = routeIndex / pairsPerDay;
                int pairSlot = routeIndex % pairsPerDay;
                Deliverer driver    = deliverers.get(pairSlot * 2);
                Deliverer assistant = deliverers.get(pairSlot * 2 + 1);
                route.setPlannedDate(LocalDate.now().plusDays(day));
                driver.assignAsDriver(route);
                assistant.assignAsAssistant(route);
            }

            newRoutes.add(route);
            routes.add(route);
            routeIndex++;
        }
        return newRoutes;
    }

    /** Helper: is the order already part of an active (non-concept) route? */
    private boolean isInExistingRoute(Order order) {
        for (Route route: routes) {
            if (route.getStatus() == Route.Status.CONCEPT) continue;
            for (Stop stop: route.getStops()) {
                if (stop.getOrder() == order) return true;
            }
        }
        return false;
    }

    /** A planner approves a concept route (see class diagram). */
    public void approveRoute(Route route) {
        if (route.getStatus() == Route.Status.CONCEPT) {
            route.setStatus(Route.Status.GOEDGEKEURD);
        }
    }
}
