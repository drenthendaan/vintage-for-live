package nl.vintageforlife.poc.logic;

import nl.vintageforlife.poc.domain.Route;
import nl.vintageforlife.poc.domain.Stop;

import java.time.LocalTime;

/**
 * Operationele uitvoering van een route. Een bezorger start de route, rondt
 * stops af en het systeem herberekent de ETA's voor de resterende stops.
 */
public class RouteExecution {

    private static final double AVG_SPEED_KMH = 50.0;

    public void startRoute(Route route) {
        if (route.getStatus() == Route.Status.GOEDGEKEURD) {
            route.setStatus(Route.Status.IN_UITVOERING);
        }
    }

    /**
     * Rondt een stop af op het opgegeven werkelijke tijdstip en
     * werkt de ETA bij voor alle volgende stops in de route.
     */
    public void completeStop(Route route, Stop stop, int actualCompletionMinutes) {
        stop.markCompleted();

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
                eta = s.getOrder().getTimeWindowStart();
            }
            s.setEta(eta);
            cursor = eta + s.getOrder().getServiceMinutes();
            prev = s;
        }

        boolean allDone = route.getStops().stream().allMatch(Stop::isCompleted);
        if (allDone) route.setStatus(Route.Status.AFGEROND);
    }

    /** Hulper: huidige tijd in minuten sinds 00:00 (handig voor de UI). */
    public static int nowMinutes() {
        LocalTime t = LocalTime.now();
        return t.getHour() * 60 + t.getMinute();
    }
}
