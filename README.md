# Vintage for Life - PoC Leveringsroute

Proof of Concept voor het bezorgroute-systeem van Vintage for Life. Deze PoC
implementeert het Technisch Ontwerp en demonstreert het scenario
**orders -> route -> bezorging** met de Jsprit-routeoptimalisatie.

## Wat zit erin

- Domeinklassen volgens het klassendiagram (Vehicle, Order, Route, Stop, Deliverer)
- RouteManagement en RouteExecution voor planning en uitvoering
- DeliveryAlgorithm als wrapper rond Jsprit (ruin & recreate)
- PlannerUI en DelivererUI in Swing
- Voorbeelddata met depot in Zwolle en 6 orders in de regio

## Wat zit er bewust **niet** in (PoC scope)

- GraphHopper rij-afstanden -> in plaats daarvan haversine + vaste 50 km/u.
  Genoeg om het algoritme te demonstreren; productie gebruikt GraphHopper.
- Persistente opslag -> alles staat in geheugen.
- Login / accounts -> de bezorger wordt via een dropdown gekozen.

## Vereisten

- Java 11 of hoger
- Maven 3.8+

## Bouwen en draaien

```
mvn clean package
mvn exec:java -Dexec.mainClass="nl.vintageforlife.poc.Main"
```

Of na `mvn package`:

```
java -cp target/poc-leveringsroute-0.1.0.jar:$(mvn -q dependency:build-classpath -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout) nl.vintageforlife.poc.Main
```

## Demo-flow

1. Beide vensters openen automatisch: Planner Portal links, Bezorger Portal rechts.
2. In de Planner: klik **Routes genereren**. Jsprit verdeelt de 6 orders over
   de 2 busjes en respecteert tijdvensters en max belading.
3. Selecteer een route en klik **Geselecteerde route goedkeuren**.
4. In de Bezorger: kies de bezorger en route. Klik **Route starten**.
5. Selecteer een stop en klik **Geselecteerde stop afronden**. De ETA's van de
   volgende stops worden bijgewerkt.

## Mapping naar de requirements (uit TO)

| Requirement | Hoe ingevuld |
|---|---|
| FR-2 (snelle/zuinige route) | Jsprit minimaliseert afstand + tijd |
| UR-6..UR-10 (bezorgersoverzicht) | DelivererUI |
| UR-11 (tijdvensters) | Service.setTimeWindow in DeliveryAlgorithm |
| UR-20, FR-8 (max belading) | VehicleType capacityDimension |
| UR-21 (service tijd) | 15 / 45 min via Order.getServiceMinutes |
| UR-24 (route < 1 min) | algo.setMaxIterations(200) |
| BR-5 (zuinig) | distance dominante kostenfactor |
