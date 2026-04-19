const express = require('express');
const app = express();

app.use(express.json());// Initialize with a default starting point (Day 0, Breakfast 0)
// This prevents the app from getting null when it first loads
let lastEvent = { day: 0, meal: 0 };

// 1. ESP32 sends data here when a pill is actually taken
app.post('/event', (req, res) => {
    lastEvent = req.body;
    console.log("Hardware Update Received:", lastEvent);
    res.sendStatus(200);
});

// 2. Android App fetches the current state from here
app.get('/event', (req, res) => {
    res.json(lastEvent);
});

// 3. NEW: Android App calls this to simulate taking the next pill
app.post('/fastforward', (req, res) => {
    // Logic: 0 (Breakfast) -> 1 (Lunch) -> 2 (Dinner) -> 0 (Next Day Breakfast)
    lastEvent.meal++;
    
    if (lastEvent.meal > 2) {
        lastEvent.meal = 0;
        lastEvent.day = (lastEvent.day + 1) % 7; // Wrap around after Sunday (Day 6)
    }

    console.log("Fast Forwarded to:", lastEvent);
    res.json(lastEvent);
});

// 4. NEW: Optional endpoint to reset the server back to start
app.post('/reset', (req, res) => {
    lastEvent = { day: 0, meal: 0 };
    console.log("Server State Reset");
    res.json(lastEvent);
});

app.listen(3000, '0.0.0.0', () => {
    console.log("Server running on port 3000. Accessible at http://10.164.59.25:3000");
});