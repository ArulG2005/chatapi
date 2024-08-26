package com.ChatSavvyConverter.chatsavvybackend;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;

@Service
public class ChatConverterService {

    public byte[] convertChat(String link, String format) {
        String chatContent = fetchChatContent(link);

        switch (format.toLowerCase()) {
            case "pdf":
                return convertToPdf(chatContent);
            case "word":
                return convertToWord(chatContent);
            case "png":
                throw new IllegalArgumentException("PNG format is not supported.");
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private String fetchChatContent(String link) {
        // Configure Selenium WebDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(ChromeDriverService.createDefaultService(), options);

        try {
            // Navigate to the chat link
            driver.get(link);

            // Wait for the page to load
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(webDriver -> webDriver.findElement(By.cssSelector(".whitespace-pre-wrap")));

            // Extract chat messages
            List<WebElement> userMessages = driver.findElements(By.cssSelector(".whitespace-pre-wrap"));
            List<WebElement> gptMessages = driver.findElements(By.xpath("//div[contains(@class, 'markdown') and contains(@class, 'prose') and contains(@class, 'w-full') and contains(@class, 'break-words') and contains(@class, 'dark:prose-invert') and contains(@class, 'dark')]"));

            StringBuilder chatContent = new StringBuilder();

            int maxLength = Math.max(userMessages.size(), gptMessages.size());

            for (int i = 0; i < maxLength; i++) {
                if (i < userMessages.size()) {
                    String userMessage = userMessages.get(i).getText().trim();
                    chatContent.append("User: ").append(userMessage).append("\n");
                }
                if (i < gptMessages.size()) {
                    String gptMessage = gptMessages.get(i).getText().trim();
                    chatContent.append("ChatGPT: ").append(gptMessage).append("\n");
                }
            }

            return chatContent.toString();
        } finally {
            driver.quit();
        }
    }

    private byte[] convertToPdf(String chatContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            String[] lines = chatContent.split("\n");
            for (String line : lines) {
                String[] parts;
                Text text;
                if (line.startsWith("User:")) {
                    parts = line.split("User:", 2);
                    text = new Text("User:").setBold().setFontColor(new DeviceRgb(3, 64, 161));
                    document.add(new Paragraph().add(text).add(parts[1].trim()).setMarginLeft(0));
                } else if (line.startsWith("ChatGPT:")) {
                    parts = line.split("ChatGPT:", 2);
                    text = new Text("ChatGPT:").setBold().setFontColor(new DeviceRgb(3, 64, 161));
                    document.add(new Paragraph().add(text).add(parts[1].trim()).setMarginLeft(0));
                } else {
                    text = new Text(line);
                    if (line.contains("```")) {
                        text.setFontColor(new DeviceRgb(0, 128, 0)); // Green color for code snippets
                    }
                    document.add(new Paragraph(text).setMarginLeft(20));
                }
            }

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] convertToWord(String chatContent) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            String[] lines = chatContent.split("\n");
            for (String line : lines) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run;

                if (line.startsWith("User:")) {
                    String[] parts = line.split("User:", 2);
                    
                    // Create run for "User:" with color and bold formatting
                    run = paragraph.createRun();
                    run.setBold(true);
                    run.setColor("0340A1"); // Blue color for "User:"
                    run.setText("User: ");
                    
                    // Create run for message content with default color
                    run = paragraph.createRun();
                    run.setColor("0340A1"); // Default color for message content
                    run.setText(parts[1].trim()); // Add message content after "User: "
                    paragraph.setIndentationLeft(0); // No left margin for User messages
                } else if (line.startsWith("ChatGPT:")) {
                    String[] parts = line.split("ChatGPT:", 2);
                    
                    // Create run for "ChatGPT:" with default color
                    run = paragraph.createRun();
                    run.setColor("0340A1"); // Default color for "ChatGPT:"
                    run.setText("ChatGPT: ");
                    
                    // Create run for message content with default color
                    run = paragraph.createRun();
                    run.setColor("000000"); // Default color for message content
                    run.setText(parts[1].trim()); // Add message content after "ChatGPT: "
                    paragraph.setIndentationLeft(720); // Small left margin for ChatGPT messages
                } else {
                    // Handle any other text
                    run = paragraph.createRun();
                    if (line.contains("```")) {
                        run.setColor("008000"); // Green color for code snippets
                    }
                    run.setText(line);
                    paragraph.setIndentationLeft(0); // Default indentation
                }
            }

            document.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
