#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include "HX711.h"

// ---------------- WIFI ----------------
const char *ssid = "Aaron";
const char *password = "Aaron_2005";

// 🔴 CHANGE THIS TO YOUR LAPTOP IP
const char* serverUrl = "http://10.164.59.25:3000/event";

// ---------------- PINS ----------------
#define BREAKFAST_PIN 18
#define LUNCH_PIN 19
#define DINNER_PIN 32

#define DT 4
#define SCK 5

// ---------------- LOAD CELL ----------------
HX711 scale;
float calibrationFactor = -2010.0;
float threshold = 0.15;

// ---------------- STATE ----------------
bool fillMode = false;
int fillDay = 0;
float breakfastWeights[7];

// Day/Meal tracking
int currentDay = 0;
int currentMeal = 0; // 0=B, 1=L, 2=D

// Debounce
bool lunchHandled = false;
bool dinnerHandled = false;

// Breakfast logic
bool breakfastOpenDetected = false;
float weightAtOpen = 0;

// ---------------- SETUP ----------------
void setup() {
  Serial.begin(115200);

  pinMode(BREAKFAST_PIN, INPUT_PULLUP);
  pinMode(LUNCH_PIN, INPUT_PULLUP);
  pinMode(DINNER_PIN, INPUT_PULLUP);

  scale.begin(DT, SCK);
  scale.set_scale(calibrationFactor);
  scale.tare();

  connectWiFi();

  Serial.println("Commands:");
  Serial.println("fill  -> start fill mode");
  Serial.println("next  -> save weight");
  Serial.println("done  -> exit fill mode");
}

// ---------------- LOOP ----------------
void loop() {

  // -------- SERIAL COMMANDS --------
  if (Serial.available()) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();

    if (cmd == "fill") {
      fillMode = true;
      fillDay = 0;
      Serial.println("=== FILL MODE START ===");
      Serial.println("Fill Day 0 breakfast and type NEXT");
    }

    if (cmd == "next" && fillMode) {
      breakfastWeights[fillDay] = getStableWeight();

      Serial.print("Saved Day ");
      Serial.print(fillDay);
      Serial.print(": ");
      Serial.println(breakfastWeights[fillDay], 2);

      fillDay++;

      if (fillDay >= 7) {
        fillMode = false;
        Serial.println("=== FILL COMPLETE ===");
      } else {
        Serial.print("Fill Day ");
        Serial.println(fillDay);
      }
    }

    if (cmd == "done") {
      fillMode = false;
      Serial.println("Fill mode exited");
    }
  }

  // -------- FILL MODE --------
  if (fillMode) {
    Serial.print("Live weight: ");
    Serial.println(getStableWeight(), 2);
    delay(500);
    return;
  }

  // -------- BREAKFAST --------
  handleBreakfast();

  // -------- LUNCH --------
  bool lunchOpen = (digitalRead(LUNCH_PIN) == HIGH);

  if (currentMeal == 1 && lunchOpen && !lunchHandled) {
    lunchHandled = true;

    Serial.println("Lunch TAKEN");
    sendEvent(currentDay, 1);

    currentMeal = 2; // move to dinner
  }

  if (!lunchOpen) {
    lunchHandled = false;
  }

  // -------- DINNER --------
  bool dinnerOpen = (digitalRead(DINNER_PIN) == HIGH);

  if (currentMeal == 2 && dinnerOpen && !dinnerHandled) {
    dinnerHandled = true;

    Serial.println("Dinner TAKEN");
    sendEvent(currentDay, 2);

    // move to next day
    currentMeal = 0;
    currentDay++;

    Serial.print("Moving to Day: ");
    Serial.println(currentDay);

    if (currentDay >= 7) {
      currentDay = 0;
      Serial.println("New Week Started");
    }
  }

  if (!dinnerOpen) {
    dinnerHandled = false;
  }
}

// ---------------- BREAKFAST HANDLER ----------------
void handleBreakfast() {

  bool isOpen = (digitalRead(BREAKFAST_PIN) == HIGH);

  // Lid opened
  if (currentMeal == 0 && isOpen && !breakfastOpenDetected) {
    breakfastOpenDetected = true;

    Serial.println("Breakfast opened");

    delay(300);
    weightAtOpen = getStableWeight();

    Serial.print("Weight at open: ");
    Serial.println(weightAtOpen, 2);
  }

  // Lid closed
  if (currentMeal == 0 && !isOpen && breakfastOpenDetected) {

    Serial.println("Breakfast closed");

    delay(500);
    float weightAfter = getStableWeight();

    Serial.print("Weight after close: ");
    Serial.println(weightAfter, 2);

    float diff = weightAtOpen - weightAfter;

    Serial.print("Difference: ");
    Serial.println(diff, 2);

    if (diff > threshold) {
      Serial.println("Breakfast TAKEN");
      sendEvent(currentDay, 0);

      currentMeal = 1; // move to lunch
    } else {
      Serial.println("No pill taken");
    }

    breakfastOpenDetected = false;
  }
}

// ---------------- FUNCTIONS ----------------

float getStableWeight() {
  float sum = 0;
  for (int i = 0; i < 10; i++) {
    sum += scale.get_units(1);
    delay(20);
  }
  return sum / 10.0;
}

void connectWiFi() {
  Serial.println("Connecting to WiFi...");

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nConnected!");
  Serial.println(WiFi.localIP());
}

void sendEvent(int day, int meal) {
  if (WiFi.status() == WL_CONNECTED) {

    HTTPClient http;
    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");

    String json = "{";
    json += "\"day\":" + String(day) + ",";
    json += "\"meal\":" + String(meal);
    json += "}";

    int response = http.POST(json);

    Serial.print("Server response: ");
    Serial.println(response);

    http.end();
  }
}