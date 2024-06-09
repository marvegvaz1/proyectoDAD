#include <WiFi.h>
#include <DHT.h>
#include <HTTPClient.h>
#include <ESP32Servo.h>  // Incluir la biblioteca ESP32Servo

// Definiciones de pines
#define DHTPIN 4         // Pin donde se conecta el DHT11
#define DHTTYPE DHT11    // DHT 11
#define SERVOPIN 18      // Pin donde se conecta el servomotor (en la ESP32)

// Credenciales de la red WiFi
const char* ssid = "Mj";          // Cambia por tu SSID
const char* password = "ComprateunputoWifi";  // Cambia por tu contraseña

// Inicializa el sensor DHT
DHT dht(DHTPIN, DHTTYPE);

// Inicializa el servomotor
Servo miServo;

// Umbrales de temperatura y humedad
const float umbralTemperatura = 30.0;  // Umbral de temperatura en grados Celsius
const float umbralHumedad = 70.0;      // Umbral de humedad en porcentaje

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
}

void loop() {
  if ((WiFi.status() == WL_CONNECTED)) { // Verifica que esté conectado a la WiFi
    HTTPClient http;

    // Lee la temperatura y la humedad del DHT11
    float temperatura = dht.readTemperature();
    float humedad = dht.readHumidity();

    // Verifica si la lectura es válida
    if (isnan(temperatura) || isnan(humedad)) {
      Serial.println("Error al leer del DHT11!");
    } else {
      // Muestra la temperatura y la humedad en el monitor serial
      Serial.print("Temperatura: ");
      Serial.print(temperatura);
      Serial.print(" °C, Humedad: ");
      Serial.print(humedad);
      Serial.println(" %");

      // Verifica si se superan los umbrales y mueve el servomotor
      if (temperatura > umbralTemperatura || humedad > umbralHumedad) {
        miServo.write(90);  // Mover el servomotor a 90 grados
      } else {
        miServo.write(0);   // Regresar el servomotor a la posición inicial
      }

      // URL de la API local
      String serverName = "http://192.168.152.254:8080/api/sensor"; // Cambia a la IP y puerto correctos
      http.begin(serverName);
      http.addHeader("Content-Type", "application/json");

      // Cuerpo del POST en formato JSON
      String postData = "{\"idsensor\":1,\"timestamp\":";
      postData += String(time(NULL)); // Usa la hora actual
      postData += ",\"valueTemp\":";
      postData += String(temperatura);
      postData += ", \"valueHum\":";
      postData += String(humedad);
      postData += ", \"idgrupo\":1, \"idplaca\":1}";

      // Realiza la solicitud HTTP POST
      int httpResponseCode = http.POST(postData);

      // Imprime el código de respuesta
      if (httpResponseCode > 0) {
        String response = http.getString();
        Serial.println(httpResponseCode);
        Serial.println(response);
      } else {
        Serial.print("Error en la solicitud POST: ");
        Serial.println(httpResponseCode);
        Serial.println(http.errorToString(httpResponseCode).c_str());  // Mensaje de error más descriptivo
      }

      // Finaliza la conexión
      http.end();
    }
  }

  // Espera 10 segundos antes de la siguiente lectura
  delay(10000);
}

