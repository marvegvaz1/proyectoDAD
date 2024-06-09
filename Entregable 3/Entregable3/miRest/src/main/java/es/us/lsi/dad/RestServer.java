package es.us.lsi.dad;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
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


public class RestServer extends AbstractVerticle {	
	
	public static MySQLPool mySqlClient;

	private Gson gson;

	public void start(Promise<Void> startFuture) {

		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("127.0.0.1")
				.setDatabase("proyectodad").setUser("root").setPassword("root");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

		mySqlClient.query("SELECT idValue from sensor last").execute(handler -> {
			System.out.println(handler.succeeded() ? "Todo OK" : "Error");
		});

		// Instantiating a Gson serialize object using specific date format
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		// Defining the router object
		Router router = Router.router(vertx);

		// Handling any server startup result
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		// SENSOR
		router.route("/api/sensor*").handler(BodyHandler.create());
		router.get("/api/sensor").handler(this::getSensorWithAllParams);
		router.post("/api/sensor").handler(this::addOneSensor);
		router.get("/api/sensor/:sensorid").handler(this::getOneSensorValues);

		// ACTUADOR
		router.route("/api/actuador*").handler(BodyHandler.create());
		router.get("/api/actuador").handler(this::getActuadorWithAllParams);
		router.post("/api/actuador").handler(this::addOneActuador);
		router.get("/api/actuador/:actuadorid").handler(this::getOneActuadorValue);
		router.post("/api/actuador").handler(this::addOneActuadorValue);

	}

	// Sensor

//	Integer idvalue
//	Integer idsensor;
//	BigInteger timestamp;
//	Double valueTemp;
//	Double valueHum;
//	Integer idgrupo;
//	Integer idplaca;

	@SuppressWarnings("unused")
	private void getAllSensor() {

		mySqlClient.query("SELECT * FROM sensor").execute(res -> {
			if (res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new SensorEntity(elem.getInteger("idvalue"),
							elem.getInteger("idsensor"), BigInteger.valueOf(elem.getInteger("timestamp")), elem.getDouble("valueTemp"),
							elem.getDouble("valueHum"), elem.getInteger("idgrupo"), elem.getInteger("idplaca"))));
				}
				System.out.println(result.toString());
//				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
//				.end(gson.toJson(new SensorEntityListWrapper(sensores.values())));

			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
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

	private void addOneSensor(RoutingContext routingContext) {
		SensorEntity sensor = new Gson().fromJson(routingContext.getBodyAsString(), SensorEntity.class);

		System.out.println(sensor.toString());
		mySqlClient
				.preparedQuery("INSERT INTO sensor (idSensor, timeStampSensor, valueTemp, valueHum, idGrupo, idPlaca) VALUES (?, ?, ?, ?, ?, ?);")
				.execute(Tuple.of(sensor.idsensor, sensor.timestamp, sensor.valueTemp, sensor.valueHum, sensor.idgrupo, sensor.idplaca), res -> {
					if (res.succeeded()) {
						routingContext.response().setStatusCode(201)
								.putHeader("content-type", "application/json; charset=utf-8")
								.end(new Gson().toJson(sensor));
					} else {
						System.out.println("Error: " + res.cause().getLocalizedMessage());
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

/************************************************************************************************************************************************************/

	// Actuador

//	Integer idvalue;
//	Integer idactuador;
//	BigInteger timestamp;
//	Double value;
//	Integer idgrupo;
//	Integer idplaca;

	@SuppressWarnings("unused")
	private void getAllActuador(RoutingContext routingContext) {
		mySqlClient.query("SELECT * FROM actuador").execute(res -> {
			if (res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new ActuadorEntity(elem.getInteger("idvalue"),
							elem.getInteger("idactuador"), BigInteger.valueOf(elem.getInteger("timestamp")), elem.getDouble("value"),
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
											elem.getDouble("value"), elem.getInteger("idgrupo"),
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
											elem.getDouble("value"), elem.getInteger("idgrupo"),
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

	private void addOneActuadorValue(RoutingContext routingContext) {

	}
	



}
