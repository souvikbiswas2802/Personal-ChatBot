import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.gson.*;

public class SouvikGPT extends JFrame {
    private static final String API_KEY = "YOUR_API_KEY"; // Replace with your API key
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private JTabbedPane tabbedPane;
    private File chatFolder;

    public SouvikGPT() {
        setTitle("SouviiGPT");
        setSize(700, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Chats folder
        chatFolder = new File("chats");
        if (!chatFolder.exists()) chatFolder.mkdir();

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        JMenu chatMenu = new JMenu("Options");
        JMenuItem newChatItem = new JMenuItem("New Chat");
        newChatItem.addActionListener(e -> addChatTab(null, null));
        chatMenu.add(newChatItem);

        JMenu aboutMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "This Chatbot is made by Souvik Biswas.\n GitHub: https://github.com/souvikbiswas2802\n LinkedIN: linkedin.com/in/souvikbiswas2802",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE));
        aboutMenu.add(aboutItem);

        menuBar.add(chatMenu);
        menuBar.add(aboutMenu);
        setJMenuBar(menuBar);

        loadOldChats();
        if (tabbedPane.getTabCount() == 0) addChatTab(null, null);

        setVisible(true);
    }

    private void loadOldChats() {
        File[] files = chatFolder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null) {
            for (File f : files) {
                addChatTab(f.getName().replace(".txt", ""), f);
            }
        }
    }

    private void addChatTab(String tabName, File file) {
        ChatPanel chatPanel = new ChatPanel(file);
        String name = (tabName != null) ? tabName : "Chat " + (tabbedPane.getTabCount() + 1);
        tabbedPane.addTab(name, chatPanel);
        tabbedPane.setSelectedComponent(chatPanel);
        int index = tabbedPane.indexOfComponent(chatPanel);
        tabbedPane.setTabComponentAt(index, createTabTitle(name, chatPanel));
    }

    private JPanel createTabTitle(String title, ChatPanel panel) {
        JPanel panelTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelTab.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        JButton btnMenu = new JButton("⋮");
        btnMenu.setMargin(new Insets(0, 0, 0, 0));
        btnMenu.setFocusable(false);

        btnMenu.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem renameItem = new JMenuItem("Rename");
            JMenuItem deleteItem = new JMenuItem("Delete");

            renameItem.addActionListener(ev -> {
                String newName = JOptionPane.showInputDialog(this, "Enter new chat name:", lblTitle.getText());
                if (newName != null && !newName.trim().isEmpty()) {
                    lblTitle.setText(newName.trim());
                    if (panel.chatFile != null) {
                        File newFile = new File(chatFolder, newName.trim() + ".txt");
                        panel.chatFile.renameTo(newFile);
                        panel.chatFile = newFile;
                    }
                }
            });

            deleteItem.addActionListener(ev -> {
                int index = tabbedPane.indexOfTabComponent(panelTab);
                if (index != -1) {
                    tabbedPane.remove(index);
                    if (panel.chatFile != null && panel.chatFile.exists()) panel.chatFile.delete();
                }
            });

            menu.add(renameItem);
            menu.add(deleteItem);
            menu.show(btnMenu, 0, btnMenu.getHeight());
        });

        panelTab.add(lblTitle);
        panelTab.add(btnMenu);
        return panelTab;
    }

    private class ChatPanel extends JPanel {
        private JPanel chatContainer;
        private JTextField inputField;
        private JButton sendButton;
        private JLabel typingLabel;
        private boolean waitingForResponse = false;
        private File chatFile;

        public ChatPanel(File file) {
            this.chatFile = file;
            setLayout(new BorderLayout());

            chatContainer = new JPanel();
            chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
            JScrollPane scrollPane = new JScrollPane(chatContainer);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            add(scrollPane, BorderLayout.CENTER);

            JPanel inputPanel = new JPanel(new BorderLayout());
            inputField = new JTextField();
            inputField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            sendButton = new JButton("Send");
            sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

            typingLabel = new JLabel(" ");
            typingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            typingLabel.setForeground(Color.GRAY);

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(inputPanel, BorderLayout.CENTER);
            bottomPanel.add(typingLabel, BorderLayout.NORTH);

            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
            add(bottomPanel, BorderLayout.SOUTH);

            sendButton.addActionListener(e -> sendMessage());
            inputField.addActionListener(e -> sendMessage());

            if (chatFile != null && chatFile.exists()) loadChatFromFile();
        }

        private void sendMessage() {
            if (waitingForResponse) return;

            String userInput = inputField.getText().trim();
            if (userInput.isEmpty()) return;

            addBubble("You: " + userInput, true);
            saveMessageToFile("You: " + userInput);
            inputField.setText("");
            inputField.setEditable(false);
            sendButton.setEnabled(false);
            waitingForResponse = true;
            typingLabel.setText("Bot is typing...");

            new Thread(() -> {
                String response = getGeminiResponse(userInput);
                response = cleanBotResponse(response);
                final String botResponse = response;  // effectively final
                SwingUtilities.invokeLater(() -> {
                    addBubble("Bot: " + botResponse, false);
                    saveMessageToFile("Bot: " + botResponse);
                    inputField.setEditable(true);
                    sendButton.setEnabled(true);
                    typingLabel.setText(" ");
                    waitingForResponse = false;
                });
            }).start();
        }

        private void addBubble(String text, boolean isUser) {
            JPanel wrapper = new JPanel();
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
            wrapper.setOpaque(false);

            JTextArea messageArea = new JTextArea(text);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setEditable(false);
            messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            messageArea.setOpaque(true);
            messageArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            messageArea.setBackground(isUser ? new Color(173, 216, 230) : new Color(200, 230, 201));

            // Rounded corners
            messageArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            // Add timestamp
            String time = new SimpleDateFormat("HH:mm").format(new Date());
            messageArea.append("\n" + time);

            int width = (int)(chatContainer.getWidth() * 0.95);
            if (width == 0) width = 780; // fallback
            messageArea.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));

            if (isUser) wrapper.add(Box.createHorizontalGlue());
            wrapper.add(messageArea);
            if (!isUser) wrapper.add(Box.createHorizontalGlue());

            chatContainer.add(wrapper);
            chatContainer.add(Box.createVerticalStrut(5));
            chatContainer.revalidate();
            chatContainer.repaint();

            JScrollBar vertical = ((JScrollPane) chatContainer.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        }

        private String cleanBotResponse(String text) {
            if (text == null) return "";
            text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1"); // bold to normal
            text = text.replaceAll("(?m)^\\*\\s+", "• ");      // bullet points
            text = text.replace("*", "");
            return text;
        }

        private void saveMessageToFile(String message) {
            try {
                if (chatFile == null) chatFile = new File(chatFolder, "Chat_" + System.currentTimeMillis() + ".txt");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(chatFile, true))) {
                    writer.write(message);
                    writer.write("|||END|||");
                    writer.newLine();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }

        private void loadChatFromFile() {
            try {
                if (chatFile == null) return;
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(chatFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line).append("\n");
                }
                String[] messages = sb.toString().split("\\|\\|\\|END\\|\\|\\|");
                for (String msg : messages) {
                    msg = msg.trim();
                    if (msg.isEmpty()) continue;
                    if (msg.startsWith("You:")) addBubble(msg, true);
                    else if (msg.startsWith("Bot:")) addBubble(msg, false);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private String getGeminiResponse(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            String jsonInput = String.format(
                    "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                    prompt.replace("\"", "\\\"")
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes("utf-8"));
            }

            if (conn.getResponseCode() != 200)
                return "Error: " + conn.getResponseCode() + " - " + conn.getResponseMessage();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line.trim());
            }

            JsonObject jsonObj = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray candidates = jsonObj.getAsJsonArray("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                if (parts != null && parts.size() > 0) {
                    return parts.get(0).getAsJsonObject().get("text").getAsString();
                }
            }
            return "No response text found.";
        } catch (Exception e) {
            return "Error occurred: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SouvikGPT::new);
    }
}
