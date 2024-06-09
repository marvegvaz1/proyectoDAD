package es.us.lsi.dad;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServer extends AbstractVerticle {

    private Map<Integer, SensorEntity> sensores = new HashMap<>();
    private Map<Integer, ActuadorEntity> actuadores = new HashMap<>();
    private Gson gson;

    @Override
    public void start(Promise<Void> startPromise) {
        // Creating some synthetic data
        createSomeSensorData(25);
        createSomeActuadorData(25);

        // Instantiating a Gson serialize object using specific date format
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        // Defining the router object
        Router router = Router.router(vertx);

        // Handling any server startup result
        vertx.createHttpServer().requestHandler(router).listen(8084, result -> {
            if (result.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(result.cause());
            }
        });

        // SENSOR
        router.route("/api/sensor*").handler(BodyHandler.create());
        router.get("/api/sensor").handler(this::getAllSensors);
        router.post("/api/sensor").handler(this::addOneSensor);
        router.get("/api/sensor/:sensorid").handler(this::getOneSensor);
        router.get("/api/sensor/last/:sensorid").handler(this::getLastSensorValue);

        // ACTUADOR
        router.route("/api/actuador*").handler(BodyHandler.create());
        router.get("/api/actuador").handler(this::getAllActuadores);
        router.post("/api/actuador").handler(this::addOneActuador);
        router.get("/api/actuador/:actuadorid").handler(this::getOneActuador);

    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        try {
            sensores.clear();
            actuadores.clear();
            stopPromise.complete();
        } catch (Exception e) {
            stopPromise.fail(e);
        }
        super.stop(stopPromise);
    }

    // Sensor Methods

    private void getAllSensors(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
                .end(gson.toJson(sensores.values()));
    }

    private void addOneSensor(RoutingContext routingContext) {
        final SensorEntity sensor = gson.fromJson(routingContext.getBodyAsString(), SensorEntity.class);
        sensores.put(sensor.getIdSensor(), sensor);
        routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(sensor));
    }

    private void getOneSensor(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("sensorid"));
        if (sensores.containsKey(id)) {
            SensorEntity sensor = sensores.get(id);
            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(200).end(gson.toJson(sensor));
        } else {
            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(404).end();
        }
    }

    private void getLastSensorValue(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("sensorid"));
        SensorEntity lastSensor = sensores.values().stream()
                .filter(sensor -> sensor.getIdSensor().equals(id))
                .max((s1, s2) -> s1.getTimestamp().compareTo(s2.getTimestamp()))
                .orElse(null);
        if (lastSensor != null) {
            routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
                    .end(gson.toJson(lastSensor));
        } else {
            routingContext.response().setStatusCode(404).end();
        }
    }

    // Actuador Methods

    private void getAllActuadores(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
                .end(gson.toJson(actuadores.values()));
    }

    private void addOneActuador(RoutingContext routingContext) {
        final ActuadorEntity actuador = gson.fromJson(routingContext.getBodyAsString(), ActuadorEntity.class);
        actuadores.put(actuador.getIdactuador(), actuador);
        routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(actuador));
    }

    private void getOneActuador(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("actuadorid"));
        if (actuadores.containsKey(id)) {
            ActuadorEntity actuador = actuadores.get(id);
            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(200).end(gson.toJson(actuador));
        } else {
            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(404).end();
        }
    }

    private void createSomeSensorData(int number) {
        Random rnd = new Random();
        IntStream.range(0, number).forEach(elem -> {
            int id = rnd.nextInt(1000);  // Use a range to avoid collision
            sensores.put(id, new SensorEntity(id, id, BigInteger.valueOf(System.currentTimeMillis()), rnd.nextDouble() * 100, rnd.nextDouble() * 100, rnd.nextInt(10), rnd.nextInt(10)));
        });
    }

    private void createSomeActuadorData(int number) {
        Random rnd = new Random();
        IntStream.range(0, number).forEach(elem -> {
            int id = rnd.nextInt(1000);  // Use a range to avoid collision
            actuadores.put(id, new ActuadorEntity(id, id, BigInteger.valueOf(System.currentTimeMillis()), rnd.nextDouble() * 100, rnd.nextInt(10), rnd.nextInt(10)));
        });
    }
}

