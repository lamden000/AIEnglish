import express from "express";
import fetch from "node-fetch";

const app = express();
app.use(express.json({ limit: "20mb" }));
app.use(express.urlencoded({ limit: "20mb", extended: true }));

const TOGETHER_ENDPOINT = "https://api.together.xyz/v1/chat/completions";

app.post("/ask-ai", async (req, res) => {
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

    // ==== PHÂN BIỆT TEXT vs IMAGE ====
    const firstMessage = requestBody?.messages?.[0];
    const isImageRequest =
      firstMessage &&
      typeof firstMessage.content !== "string";

    // ===== IMAGE CHAT: trả nguyên format chat =====
    if (isImageRequest) {
      return res.status(response.status).json(data);
    }

    // ===== TEXT CHAT: ADAPT sang format legacy =====
    const text =
      data?.choices?.[0]?.message?.content ?? "";

    const adaptedResponse = {
      output: {
        choices: [
          { text }
        ]
      }
    };

    return res.status(response.status).json(adaptedResponse);

  } catch (err) {
    console.error("Server error:", err);
    res.status(500).json({ error: "Server error" });
  }
});

app.post("/chat/completions", async (req, res) => {
  // Giữ nguyên cho OpenAIAPI
  try {
    const response = await fetch(TOGETHER_ENDPOINT, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${process.env.TOGETHER_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(req.body),
    });

    const data = await response.json();
    res.status(response.status).json(data);
  } catch (err) {
    res.status(500).json({ error: "Server error" });
  }
});

app.get("/", (_, res) => {
  res.send("AI Proxy Backend running 🚀");
});

const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`Backend running on port ${port}`);
});
