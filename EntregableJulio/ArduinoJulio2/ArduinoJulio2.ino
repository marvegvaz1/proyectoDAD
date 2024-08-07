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
const char* mqtt_server = "192.168.229.254";
const char* mqtt_user = "mqtt";
const char* mqtt_password = "mj";

// Inicializa el sensor DHT
DHT dht(DHTPIN, DHTTYPE);

// Inicializa el servomotor
Servo miServo;

// Umbrales de temperatura y humedad
const float umbralTemperatura = 35.0;
const float umbralHumedad = 70.0;

WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
  Serial.begin(115200);
  dht.begin();
  miServo.attach(SERVOPIN);  // Adjuntar el servomotor al pin especificado

  // Conexión a la red WiFi
  Serial.println("Conectando a la red WiFi...");
  WiFi.begin(ssid, password);

  unsigned long startAttemptTime = millis();

  // Intentar conectar durante 30 segundos
  while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < 30000) {
    delay(1000);
    Serial.print(".");
  }

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("\nNo se pudo conectar a la red WiFi");
  } else {
    Serial.println("\nConectado a la red WiFi");
    Serial.print("Dirección IP: ");
    Serial.println(WiFi.localIP());
  }

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

  if (String(topic) == "invernadero/actuador/placa2") {
    if (messageTemp.startsWith("ServoOn")) {
      miServo.write(90);  // Mover el servomotor a 90 grados
      Serial.println("Servo encendido");
    } else if (messageTemp.startsWith("ServoOff")) {
      miServo.write(0);   // Regresar el servomotor a la posición inicial
      Serial.println("Servo apagado");
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

  String payload = "{\"idsensor\":2,\"timestamp\":";
  payload += String(time(NULL));
  payload += ",\"valueTemp\":";
  payload += String(temperatura);
  payload += ", \"valueHum\":";
  payload += String(humedad);
  payload += ", \"idgrupo\":2, \"idplaca\":2}";

  client.publish("invernadero/sensor", (char*) payload.c_str());
  delay(10000); // Ajusta este delay según sea necesario
}



