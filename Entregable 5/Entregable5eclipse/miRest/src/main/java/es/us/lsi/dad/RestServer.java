package es.us.lsi.dad;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class RestServer extends AbstractVerticle {

    private static Double lastValueTemperatura = 0.0;
    private static Double lastValueHumedad = 0.0;

    private MqttClient mqttClient;

    private void configureMqttClient() {
        MqttClientOptions options = new MqttClientOptions()
                .setClientId("restServer")
                .setUsername("mqtt").setPassword("mj")
                .setCleanSession(true);
        mqttClient = MqttClient.create(vertx, options);
        mqttClient.connect(1883, "192.168.152.254", s -> {
            if (s.succeeded()) {
                System.out.println("Connected to MQTT broker");
                mqttClient.subscribe("invernadero/sensor", MqttQoS.AT_LEAST_ONCE.value(), ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Subscribed to topic invernadero/sensor");
                    } else {
                        System.out.println("Failed to subscribe to topic: " + ar.cause().getMessage());
                    }
                });
            } else {
                System.out.println("Failed to connect to MQTT broker: " + s.cause().getMessage());
            }
        });

        mqttClient.publishHandler(message -> {
            String topic = message.topicName();
            String payload = message.payload().toString();
            System.out.println("Mensaje recibido en el tópico [" + topic + "]: " + payload);
            if (topic.equals("invernadero/sensor")) {
                // Procesa el mensaje recibido
                JsonObject json = new JsonObject(payload);
                SensorEntity sensor = new SensorEntity(
                        null,
                        json.getInteger("idsensor"),
                        BigInteger.valueOf(json.getLong("timestamp")),
                        json.getDouble("valueTemp"),
                        json.getDouble("valueHum"),
                        json.getInteger("idgrupo"),
                        json.getInteger("idplaca")
                );
                insertSensorData(sensor);
            }
        });
    }

    private void insertSensorData(SensorEntity sensor) {
        mySqlClient
            .preparedQuery("INSERT INTO sensor (idSensor, timeStampSensor, valueTemp, valueHum, idGrupo, idPlaca) VALUES (?, ?, ?, ?, ?, ?);")
            .execute(Tuple.of(sensor.idsensor, sensor.timestamp, sensor.valueTemp, sensor.valueHum, sensor.idgrupo, sensor.idplaca), res -> {
                if (res.succeeded()) {
                    System.out.println("Datos insertados correctamente en la base de datos.");
                    if ((sensor.valueTemp > 70 && lastValueTemperatura <= 70) || (sensor.valueHum > 70 && lastValueHumedad <= 70)) {
                        mqttClient.publish("invernadero/actuador", Buffer.buffer("ServoOn"), MqttQoS.AT_LEAST_ONCE, false, false);
                        insertActuadorData(sensor.idsensor, "ServoOn");
                    } else if ((sensor.valueTemp <= 70 && lastValueTemperatura > 70) || (sensor.valueHum <= 70 && lastValueHumedad > 70)) {
                        mqttClient.publish("invernadero/actuador", Buffer.buffer("ServoOff"), MqttQoS.AT_LEAST_ONCE, false, false);
                        insertActuadorData(sensor.idsensor, "ServoOff");
                    }
                    lastValueTemperatura = sensor.valueTemp;
                    lastValueHumedad = sensor.valueHum;
                } else {
                    System.out.println("Error al insertar datos en la base de datos: " + res.cause().getLocalizedMessage());
                }
            });
    }

    private void insertActuadorData(int idSensor, String action) {
        int value = action.equals("ServoOn") ? 1 : 0;
        mySqlClient
            .preparedQuery("INSERT INTO actuador (idActuador, timeStampActuador, valueActuador, idGrupo, idPlaca) VALUES (?, ?, ?, ?, ?);")
            .execute(Tuple.of(idSensor, System.currentTimeMillis() / 1000L, value, 1, 1), res -> {
                if (res.succeeded()) {
                    System.out.println("Datos del actuador insertados correctamente en la base de datos. Acción: " + action);
                } else {
                    System.out.println("Error al insertar datos del actuador en la base de datos: " + res.cause().getLocalizedMessage());
                }
            });
    }

    public static MySQLPool mySqlClient;

    private Gson gson;

    public void start(Promise<Void> startPromise) {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("127.0.0.1")
                .setDatabase("proyectodad")
                .setUser("root")
                .setPassword("root");

        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

        mySqlClient.query("SELECT 1").execute(ar -> {
            if (ar.succeeded()) {
                System.out.println("Conectado a la base de datos exitosamente.");
                startPromise.complete();
            } else {
                System.out.println("Error al conectar a la base de datos: " + ar.cause().getMessage());
                startPromise.fail(ar.cause());
            }
        });

        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        Router router = Router.router(vertx);

        configureMqttClient();

        router.route("/api/sensor*").handler(BodyHandler.create());
        router.get("/api/sensor").handler(this::getSensorWithAllParams);
        router.post("/api/sensor").handler(this::addOneSensor);
        router.get("/api/sensor/:sensorid").handler(this::getOneSensorValues);

        router.route("/api/actuador*").handler(BodyHandler.create());
        router.get("/api/actuador").handler(this::getActuadorWithAllParams);
        router.post("/api/actuador").handler(this::addOneActuador);
        router.get("/api/actuador/:actuadorid").handler(this::getOneActuadorValue);

        vertx.createHttpServer().requestHandler(router).listen(8080, result -> {
            if (result.succeeded()) {
                System.out.println("Servidor HTTP corriendo en el puerto 8080");
                startPromise.complete();
            } else {
                System.out.println("Error al iniciar el servidor HTTP: " + result.cause().getMessage());
                startPromise.fail(result.cause());
            }
        });
    }

    private void addOneSensor(RoutingContext routingContext) {
        SensorEntity sensor = new Gson().fromJson(routingContext.getBodyAsString(), SensorEntity.class);
        JsonObject body = routingContext.body().asJsonObject();

        Double valueTemp = body.getDouble("valueTemp");
        Double valueHum = body.getDouble("valueHum");
        System.out.println("Datos recibidos del sensor: " + sensor.toString());

        mySqlClient
                .preparedQuery("INSERT INTO sensor (idSensor, timeStampSensor, valueTemp, valueHum, idGrupo, idPlaca) VALUES (?, ?, ?, ?, ?, ?);")
                .execute(Tuple.of(sensor.idsensor, sensor.timestamp, sensor.valueTemp, sensor.valueHum, sensor.idgrupo, sensor.idplaca), res -> {
                    if (res.succeeded()) {
                        System.out.println("Datos insertados correctamente en la base de datos.");
                        routingContext.response().setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(new Gson().toJson(sensor));

                        if ((valueTemp > 35 && lastValueTemperatura <= 35) || (valueHum > 70 && lastValueHumedad <= 70)) {
                            String topic = "invernadero/actuador";
                            String mensaje = "ServoOn";
                            mqttClient.publish(topic, Buffer.buffer(mensaje), MqttQoS.AT_LEAST_ONCE, false, false);
                            insertActuadorData(sensor.idsensor, "ServoOn");
                        } else if ((valueTemp <= 35 && lastValueTemperatura > 35) || (valueHum <= 70 && lastValueHumedad > 70)) {
                            String topic = "invernadero/actuador";
                            String mensaje = "ServoOff";
                            mqttClient.publish(topic, Buffer.buffer(mensaje), MqttQoS.AT_LEAST_ONCE, false, false);
                            insertActuadorData(sensor.idsensor, "ServoOff");
                        }

                        lastValueTemperatura = valueTemp;
                        lastValueHumedad = valueHum;

                    } else {
                        System.out.println("Error al insertar datos en la base de datos: " + res.cause().getLocalizedMessage());
                        routingContext.response().setStatusCode(500)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end("Error: " + res.cause().getLocalizedMessage());
                    }
                });
    }

    private void getSensorWithAllParams(RoutingContext routingContext) {
        Integer id = Integer.parseInt(routingContext.request().getParam("idsensor"));

        mySqlClient.getConnection(connection -> {
            if (connection.succeeded()) {
                connection.result().preparedQuery("SELECT * FROM sensor WHERE idSensor = ?").execute(Tuple.of(id),
                        res -> {
                            if (res.succeeded()) {
                                RowSet<Row> resultSet = res.result();
                                JsonArray result = new JsonArray();
                                for (Row elem : resultSet) {
                                    result.add(JsonObject.mapFrom(new SensorEntity(elem.getInteger("idvalue"),
                                            elem.getInteger("idsensor"), BigInteger.valueOf(elem.getInteger("timestamp")), elem.getDouble("valueTemp"),
                                            elem.getDouble("valueHum"), elem.getInteger("idgrupo"), elem.getInteger("idplaca"))));
                                }
                                System.out.println(result.toString());
                                routingContext.response().setStatusCode(200)
                                        .putHeader("content-type", "application/json; charset=utf-8")
                                        .end(result.encodePrettily());
                            } else {
                                System.out.println("Error: " + res.cause().getLocalizedMessage());
                            }
                            connection.result().close();
                        });
            } else {
                System.out.println(connection.cause().toString());
            }
        });
    }

    private void getOneSensorValues(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("sensorid"));

        mySqlClient.getConnection(connection -> {
            if (connection.succeeded()) {
                connection.result().preparedQuery("SELECT * FROM sensor WHERE idSensor = ?").execute(Tuple.of(id),
                        res -> {
                            if (res.succeeded()) {
                                RowSet<Row> resultSet = res.result();
                                List<SensorEntity> result = new ArrayList<SensorEntity>();
                                for (Row elem : resultSet) {
                                    result.add(new SensorEntity(elem.getInteger("idvalue"),
                                            elem.getInteger("idsensor"), BigInteger.valueOf(elem.getInteger("timestamp")), elem.getDouble("valueTemp"),
                                            elem.getDouble("valueHum"), elem.getInteger("idgrupo"), elem.getInteger("idplaca")));
                                }
                                routingContext.response().setStatusCode(200)
                                        .putHeader("content-type", "application/json; charset=utf-8")
                                        .end(gson.toJson(result));
                            } else {
                                System.out.println("Error: " + res.cause().getLocalizedMessage());
                            }
                            connection.result().close();
                        });
            } else {
                System.out.println(connection.cause().toString());
            }
        });
    }

    private void getAllActuador(RoutingContext routingContext) {
        mySqlClient.query("SELECT * FROM actuador").execute(res -> {
            if (res.succeeded()) {
                RowSet<Row> resultSet = res.result();
                JsonArray result = new JsonArray();
                for (Row elem : resultSet) {
                    result.add(JsonObject.mapFrom(new ActuadorEntity(elem.getInteger("idvalue"),
                            elem.getInteger("idactuador"), BigInteger.valueOf(elem.getInteger("timestamp")), elem.getDouble("valueActuador"),
                            elem.getInteger("idgrupo"), elem.getInteger("idplaca"))));
                }
                System.out.println(result.toString());
                routingContext.response().setStatusCode(200)
                        .putHeader("content-type", "application/json; charset=utf-8").end(result.encodePrettily());
            } else {
                System.out.println("Error: " + res.cause().getLocalizedMessage());
            }
        });
    }

    private void getActuadorWithAllParams(RoutingContext routingContext) {
        Integer id = Integer.parseInt(routingContext.request().getParam("idactuador"));

        mySqlClient.getConnection(connection -> {
            if (connection.succeeded()) {
                connection.result().preparedQuery("SELECT * FROM actuador WHERE idActuador = ?").execute(Tuple.of(id),
                        res -> {
                            if (res.succeeded()) {
                                RowSet<Row> resultSet = res.result();
                                JsonArray result = new JsonArray();
                                for (Row elem : resultSet) {
                                    result.add(JsonObject.mapFrom(new ActuadorEntity(elem.getInteger("idvalue"),
                                            elem.getInteger("idactuador"), BigInteger.valueOf(elem.getInteger("timestamp")),
                                            elem.getDouble("valueActuador"), elem.getInteger("idgrupo"),
                                            elem.getInteger("idplaca"))));
                                }
                                System.out.println(result.toString());
                                routingContext.response().setStatusCode(200)
                                        .putHeader("content-type", "application/json; charset=utf-8")
                                        .end(result.encodePrettily());
                            } else {
                                System.out.println("Error: " + res.cause().getLocalizedMessage());
                            }
                            connection.result().close();
                        });
            } else {
                System.out.println(connection.cause().toString());
            }
        });
    }

    private void getOneActuadorValue(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("actuadorid"));
        mySqlClient.getConnection(connection -> {
            if (connection.succeeded()) {
                connection.result().preparedQuery("SELECT * FROM actuador WHERE idActuador = ?").execute(Tuple.of(id),
                        res -> {
                            if (res.succeeded()) {
                                RowSet<Row> resultSet = res.result();
                                JsonArray result = new JsonArray();
                                for (Row elem : resultSet) {
                                    result.add(JsonObject.mapFrom(new ActuadorEntity(elem.getInteger("idvalue"),
                                            elem.getInteger("idactuador"), BigInteger.valueOf(elem.getInteger("timestamp")),
                                            elem.getDouble("valueActuador"), elem.getInteger("idgrupo"),
                                            elem.getInteger("idplaca"))));
                                }
                                System.out.println(result.toString());
                                routingContext.response().setStatusCode(200)
                                        .putHeader("content-type", "application/json; charset=utf-8")
                                        .end(result.encodePrettily());
                            } else {
                                System.out.println("Error: " + res.cause().getLocalizedMessage());
                            }
                            connection.result().close();
                        });
            } else {
                System.out.println(connection.cause().toString());
            }
        });
    }

    private void addOneActuador(RoutingContext routingContext) {
        ActuadorEntity actuador = new Gson().fromJson(routingContext.getBodyAsString(), ActuadorEntity.class);

        System.out.println(actuador.toString());
        mySqlClient
                .preparedQuery(
                        "INSERT INTO actuador (idActuador, timeStampActuador, valueActuador, idGrupo, idPlaca) VALUES (?, ?, ?, ?, ?);")
                .execute(Tuple.of(actuador.idactuador, actuador.timestamp, actuador.value, actuador.idgrupo, actuador.idplaca), res -> {
                    if (res.succeeded()) {
                        routingContext.response().setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(new Gson().toJson(actuador));
                    } else {
                        System.out.println("Error: " + res.cause().getLocalizedMessage());
                    }
                });
    }
}

