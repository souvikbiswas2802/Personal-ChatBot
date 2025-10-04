# 🤖 SouvikGPT

Welcome to **SouvikGPT**, a Java-based chatbot GUI that connects to the **Google Gemini API**!  
This project allows you to have conversations with an AI-powered bot with a visually appealing interface, chat bubbles, and full chat history management.  

---

## 📂 Project Structure

```

Personal-Chatbot/
├── SouvikGPT.java         // Main program
├── libs/
│    └── gson-2.10.1.jar   // Gson dependency for JSON parsing
├── chats/                 // Folder for saved chat history
└── README.md              // Project documentation

```

---

## ⚙️ Features

- 💬 **Interactive GUI** using Swing & AWT  
- 🟦 **User messages right-aligned**
- 🟩 **Bot messages left-aligned**  
- 🖌️ **Rounded chat bubbles** with timestamps  
- 📝 **Markdown cleanup**:  
  - `**bold**` → normal text  
  - `*` → bullet points  
- 🗂️ **Chat history saved locally** in the `chats/` folder  
- 🆕 **Start new chats anytime**  
- ✏️ **Rename or delete old chats**  
- ⏳ **Input disabled until bot response is received**  

---

## 🛠️ Dependencies

1. **Java 8+**  
2. **Gson library** (`gson-2.10.1.jar`)  
   - Required for parsing JSON responses from Gemini API  
   - Place the `gson-2.10.1.jar` inside the `libs/` folder  

---

## 🔑 API Key Setup

1. Obtain your **Google Gemini API key** from Google Cloud.  
2. Open `SouvikGPT.java`.  
3. Replace the placeholder API key with your actual key:

```java
private static final String API_KEY = "YOUR_API_KEY";
````

> ⚠️ Keep your API key private. Do not share it publicly.

---

## 💻 How to Compile and Run

1. Open your terminal in the project directory.
2. Compile the program:

```bash
javac -cp ".;libs/gson-2.10.1.jar" SouvikGPT.java
```

3. Run the program:

```bash
java -cp ".;libs/gson-2.10.1.jar" SouvikGPT
```

> ⚠️ On Linux/macOS, replace `;` with `:` in the classpath:

```bash
javac -cp ".:libs/gson-2.10.1.jar" SouvikGPT.java
java -cp ".:libs/gson-2.10.1.jar" SouvikGPT
```

4. The GUI will open. Start chatting with SouvikGPT!

---

## 🗃️ Saved Chats

* All chat sessions are stored in the `chats/` folder as `.txt` files.
* Chats are **auto-loaded** when the program starts.
* Each message is saved with an **identifier** and **timestamp**.
* You can **rename** or **delete** chats from the tab menu.

---

## 🔄 Changing the AI Model

By default, the project uses:

```java
private static final String API_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
```

To use a **different Gemini model**:

1. Open `SouvikGPT.java`.
2. Find the `API_URL` constant.
3. Replace the model name (`gemini-2.5-flash`) with the model you want. For example:

```java
private static final String API_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0:generateContent?key=" + API_KEY;
```

4. Save the file and **recompile & run** using the commands above.

> ⚠️ Make sure the model you choose is **available for your API key**. Using an unsupported model will result in an error.

---

## 🎨 Visuals

* **User bubble:** 🟦 Right-aligned, light blue
* **Bot bubble:** 🟩 Left-aligned, light green
* **Timestamps** displayed below each message

---

## 🙋‍♂️ Author

**Souvik Biswas**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/souvikbiswas2802)  
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/souvikbiswas2802)  

---

**Enjoy chatting with SouvikGPT! 🤖💬**
