import express from "express";
import fetch from "node-fetch";

const app = express();
app.use(express.json());

const TOGETHER_ENDPOINT = "https://api.together.xyz/v1/chat/completions";

/**
 * Core proxy handler
 * - Nhận body từ client
 * - Forward nguyên body sang TogetherAI
 * - Trả nguyên response về
 */
async function proxyToTogether(req, res) {
  try {
    const requestBody = req.body;

    const response = await fetch(TOGETHER_ENDPOINT, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${process.env.TOGETHER_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    });

    const data = await response.json();

    res.status(response.status).json(data);
  } catch (err) {
    console.error("Proxy error:", err);
    res.status(500).json({ error: "Proxy server error" });
  }
}

/**
 * === ROUTES ===
 * Hai route này KHỚP TRỰC TIẾP với Retrofit interface của bạn
 */

// For TogetherApi
app.post("/ask-ai", proxyToTogether);

// For OpenAIAPI-compatible calls
app.post("/chat/completions", proxyToTogether);

// Optional health check (để khỏi hoảng khi mở URL)
app.get("/", (req, res) => {
  res.send("AI Proxy Backend is running 🚀");
});

const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`Backend running on port ${port}`);
});
