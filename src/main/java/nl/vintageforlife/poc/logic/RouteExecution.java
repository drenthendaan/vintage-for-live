package nl.vintageforlife.poc.logic;

import nl.vintageforlife.poc.domain.Route;
import nl.vintageforlife.poc.domain.Stop;

import java.time.LocalTime;

/**
 * Operational execution of a route. The delivery team starts the route,
 * completes stops, and the system recalculates the ETAs for the remaining
 * stops. Matches the RouteExecution class from the class diagram.
 */
public class RouteExecution {

    /** Average driving speed for the PoC (km/h); in production GraphHopper. */
    private static final double AVG_SPEED_KMH = 50.0;

    /** Moves an approved route to status IN_UITVOERING (in progress). */
    public void startRoute(Route route) {
        if (route.getStatus() == Route.Status.GOEDGEKEURD) {
            route.setStatus(Route.Status.IN_UITVOERING);
        }
    }

    /**
     * Completes a stop at the given actual completion time and updates the
     * ETAs for all following stops in the route. When every stop is done,
     * the route is marked as AFGEROND (finished).
     */
    public void completeStop(Route route, Stop stop, int actualCompletionMinutes) {
        stop.markCompleted();

        // Walk through all stops after the completed one and recalculate ETAs.
        int cursor = actualCompletionMinutes;
        Stop prev = stop;
        boolean past = false;
        for (Stop s : route.getStops()) {
            if (!past) {
                if (s == stop) past = true;
                continue;
            }
            double km = prev.getOrder().getAddress().distanceKm(s.getOrder().getAddress());
            int travel = (int) Math.round(km / AVG_SPEED_KMH * 60.0);
            int eta = cursor + travel;
            if (eta < s.getOrder().getTimeWindowStart()) {
                eta = s.getOrder().getTimeWindowStart();  // wait for the time window
            }
            s.setEta(eta);
            cursor = eta + s.getOrder().getServiceMinutes();
            prev = s;
        }

        boolean allDone = route.getStops().stream().allMatch(Stop::isCompleted);
        if (allDone) route.setStatus(Route.Status.AFGEROND);
    }

    /** Helper: current time in minutes since 00:00 (useful for the UI). */
    public static int nowMinutes() {
        LocalTime t = LocalTime.now();
        return t.getHour() * 60 + t.getMinute();
    }
}
