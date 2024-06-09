#include <WiFi.h>
#include <DHT.h>
#include <PubSubClient.h>
#include <ESP32Servo.h>  // Incluir la biblioteca ESP32Servo

// Definiciones de pines
#define DHTPIN 4         // Pin donde se conecta el DHT11
#define DHTTYPE DHT11    // DHT 11
#define SERVOPIN 18      // Pin donde se conecta el servomotor (en la ESP32)

// Credenciales de la red WiFi
const char* ssid = "Mj";
const char* password = "ComprateunputoWifi";

// Credenciales del MQTT Broker
const char* mqtt_server = "192.168.152.254";
const char* mqtt_user = "mqtt";
const char* mqtt_password = "mj";

// Inicializa el sensor DHT
DHT dht(DHTPIN, DHTTYPE);

// Inicializa el servomotor
Servo miServo;

// Umbrales de temperatura y humedad
const float umbralTemperatura = 30.0;
const float umbralHumedad = 70.0;

WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
  Serial.begin(115200);
  dht.begin();
  miServo.attach(SERVOPIN);  // Adjuntar el servomotor al pin especificado

  // Conexión a la red WiFi
  WiFi.begin(ssid, password);
  Serial.print("Conectando a ");
  Serial.println(ssid);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("Conectado a la red WiFi");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());

  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);

  reconnect();
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Conectando al broker MQTT...");
    if (client.connect("ESP32Client", mqtt_user, mqtt_password)) {
      Serial.println("Conectado al broker MQTT");
      client.subscribe("invernadero/actuador"); // Tópico de control para el actuador
    } else {
      Serial.print("Falló la conexión, rc=");
      Serial.print(client.state());
      delay(5000);
    }
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  String messageTemp;
  for (unsigned int i = 0; i < length; i++) {
    messageTemp += (char)payload[i];
  }
  Serial.print("Mensaje recibido [");
  Serial.print(topic);
  Serial.print("]: ");
  Serial.println(messageTemp);

  if (String(topic) == "invernadero/actuador") {
    if (messageTemp == "ServoOn") {
      miServo.write(90);  // Mover el servomotor a 90 grados
    } else if (messageTemp == "ServoOff") {
      miServo.write(0);   // Regresar el servomotor a la posición inicial
    }
  }
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  float temperatura = dht.readTemperature();
  float humedad = dht.readHumidity();

  if (isnan(temperatura) || isnan(humedad)) {
    Serial.println("Error al leer del DHT11!");
    return;
  }

  Serial.print("Temperatura: ");
  Serial.print(temperatura);
  Serial.print(" °C, Humedad: ");
  Serial.print(humedad);
  Serial.println(" %");

  String payload = "{\"idsensor\":1,\"timestamp\":";
  payload += String(time(NULL));
  payload += ",\"valueTemp\":";
  payload += String(temperatura);
  payload += ", \"valueHum\":";
  payload += String(humedad);
  payload += ", \"idgrupo\":1, \"idplaca\":1}";

  client.publish("invernadero/sensor", (char*) payload.c_str());

  delay(10000); // Ajusta este delay según sea necesario
}

