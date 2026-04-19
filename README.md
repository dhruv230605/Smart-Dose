# SmartDose (MUC Project) - Comprehensive Documentation

SmartDose is an integrated IOT-driven medication adherence system designed to help elderly patients manage their prescriptions and allow caregivers to monitor progress remotely. The system consists of a **Smart Pillbox (Hardware)**, a **Centralized Server**, and a **Mobile Application**.

---

## 1. System Architecture

The project operates as a triangle of connectivity:
1.  **Hardware (ESP32):** Monitors the physical pillbox. It uses a load cell for weight-based detection (Breakfast) and magnetic sensors for lid detection (Lunch/Dinner). It sends "completion events" to the server.
2.  **Server (Node.js):** Acts as the source of truth. It stores the current state of the pillbox (what was last taken) and serves this data to the mobile app.
3.  **Mobile App (Android):** The user interface for both the Elder and Caregiver. It highlights the next dose, tracks history in a local Room database, and provides accessibility features.

---

## 2. Prerequisites

### Software Requirements:
*   **Android Studio:** Ladybug or newer.
*   **Node.js:** Installed on your laptop/host machine.
*   **Arduino IDE:** With ESP32 board support and `HX711` library installed.

### Hardware Requirements:
*   **Microcontroller:** ESP32 (recommended) or ESP8266.
*   **Sensors:** 
    *   1x HX711 Load Cell Amplifier (for Breakfast compartment).
    *   2x Magnetic Reed Switches or Push Buttons (for Lunch/Dinner compartments).
*   **Network:** A WiFi Hotspot (Phone or Router).

---

## 3. Getting Started - Setup Guide

### Step 1: Network Configuration (Crucial)
All three components **must** be on the same local network.
1.  Turn on a mobile hotspot (e.g., Name: `Aaron`, Password: `Aaron_2005`).
2.  Connect your **Laptop** to this hotspot.
3.  Find your Laptop's Local IP address:
    *   **Windows:** Type `ipconfig` in CMD (look for IPv4 Address).
    *   **Mac/Linux:** Type `ifconfig` or check Network Settings (e.g., `10.164.59.25`).

### Step 2: Hosting the Server
1.  Create a folder named `server` on your laptop.
2.  Create a file named `server.js` and paste the provided Node.js code.
3.  Open a terminal in that folder and run:
    ```bash
    npm init -y
    npm install express
    node server.js
    ```
4.  The server should now say: `Server running on port 3000`.

### Step 3: Hardware (Arduino) Setup
1.  Open the Arduino code provided.
2.  **Update the IP:** Change `serverUrl` to match your Laptop's IP (e.g., `http://10.164.59.25:3000/event`).
3.  **Update WiFi:** Ensure `ssid` and `password` match your hotspot.
4.  Upload the code to your ESP32.
5.  Open Serial Monitor (115200 baud) to verify it connects to WiFi.

### Step 4: Android Application Setup
1.  Open the `MUCProject` in Android Studio.
2.  Navigate to `ApiService.kt` and ensure the `BASE_URL` matches your Laptop's IP.
3.  Build and Run the app on your Android physical device (connected to the same hotspot).
4.  **Note:** The app is configured with `usesCleartextTraffic="true"` to allow connection to the local HTTP server.

---

## 4. Key Features

### 🟢 Elder Dashboard
*   **Next Dose Highlight:** Automatically highlights the specific compartment the user needs to open next based on server data.
*   **Dynamic UI:** If the user takes a pill, they tap **Refresh**, and the app marks the previous one as "Taken" and moves the highlight to the next scheduled dose.
*   **Fill Mode:** A guided wizard to help users set up their breakfast weights using the load cell.
*   **Accessibility:** Support for **Large Text Mode** and **High Contrast Mode** for visually impaired users.

### 🔵 Caregiver Dashboard
*   **Remote Monitoring:** View a 7-day grid showing which doses were taken, delayed, or missed.
*   **Adherence Insights:** Calculates a weekly percentage score and provides text-based insights (e.g., "Most doses missed at Lunch").

### 🛠 Test & Developer Mode
*   **Fast Forward:** Located in Test Mode. It tells the server to skip to the next meal. This allows developers to test the full 7-day cycle in minutes instead of days.
*   **Clear All:** Wipes the local Room database to reset all history.
*   **Auto-Reset:** Once the server reports that Sunday Dinner is completed, the app automatically resets the UI for a new week.

---

## 5. User Onboarding (How to use)

1.  **Login:** Choose "Elder" or "Caregiver" role.
2.  **Initial Setup:** Use "Fill Mode" to put pills in the box.
3.  **Daily Use:** 
    *   Check the app for the highlighted box.
    *   Open the lid/Take the pill.
    *   The hardware sends a signal.
    *   Tap **Refresh** on the app to see the update.
4.  **Reset:** The system handles the transition from Sunday night to Monday morning automatically.

---

## 6. Troubleshooting

*   **"Pillbox Offline":** 
    *   Check if your laptop firewall is blocking port 3000.
    *   Ensure the phone browser can reach `http://<your-ip>:3000/event`.
    *   Double-check that the phone is on the hotspot.
*   **Breakfast Not Updating:**
    *   Ensure the HX711 is calibrated.
    *   The Arduino only sends the "Taken" event once the lid is **closed** and it detects a weight drop.
*   **Data Not Persisting:**
    *   The app uses Room Database. Ensure you don't "Clear Data" in Android settings unless you want to reset your history.

---
**Developed for the MUC Medication Adherence Project.** 🚀
