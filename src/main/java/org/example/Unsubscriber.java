package org.example;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;

public class Unsubscriber {

    // Expanded set of unsubscribe keywords for fallback parsing
    private static final List<String> KEYWORDS = Arrays.asList(
            "unsubscribe", "opt out", "cancel subscription", "stop", "remove",
            "end subscription", "manage preferences", "change email settings",
            "do not send", "unsubscribe here", "opt-out"
    );

    private final Gmail service;

    public Unsubscriber(Gmail service) {
        this.service = service;
    }

    /**
     * Scans all emails, extracts unsubscribe links, and prints sender->link in real-time.
     */
    public Map<String, String> scanAllEmails() throws Exception {
        Map<String, String> emailToLink = new HashMap<>();
        Set<String> uniqueSenders = new HashSet<>();

        String pageToken = null;
        int totalProcessed = 0;

        do {
            ListMessagesResponse response = service.users().messages().list("me")
                    .setMaxResults(100L)
                    .setPageToken(pageToken)
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages != null) {
                // Newest → oldest
                for (int i = messages.size() - 1; i >= 0; i--) {
                    Message msg = service.users().messages().get("me", messages.get(i).getId())
                            .setFormat("full").execute();

                    String from = msg.getPayload().getHeaders().stream()
                            .filter(h -> "From".equalsIgnoreCase(h.getName()))
                            .map(MessagePartHeader::getValue)
                            .findFirst()
                            .orElse("unknown");

                    // Skip only if we already have an unsubscribe link for this sender
                    if (emailToLink.containsKey(from)) {
                        totalProcessed++;
                        continue;
                    }

                    uniqueSenders.add(from);

                    // 1. Fast method: List-Unsubscribe header
                    String unsubscribeLink = msg.getPayload().getHeaders().stream()
                            .filter(h -> "List-Unsubscribe".equalsIgnoreCase(h.getName()))
                            .map(MessagePartHeader::getValue)
                            .findFirst()
                            .orElse(null);

                    // 2. Fallback: parse HTML body for multiple keywords
                    if (unsubscribeLink == null) {
                        String body = getBodyFromMessage(msg);
                        Document doc = Jsoup.parse(body);
                        unsubscribeLink = doc.select("a").stream()
                                .map(a -> a.attr("href"))
                                .filter(href -> KEYWORDS.stream()
                                        .anyMatch(k -> href.toLowerCase().contains(k)))
                                .findFirst()
                                .orElse(null);
                    }

                    if (unsubscribeLink != null) {
                        emailToLink.put(from, unsubscribeLink);
                        System.out.println(from + " -> " + unsubscribeLink);
                    }

                    totalProcessed++;
                }
            }

            pageToken = response.getNextPageToken();
        } while (pageToken != null);

        System.out.println("\n=== Scan Complete ===");
        System.out.println("Total emails processed: " + totalProcessed);
        System.out.println("Unique senders detected: " + uniqueSenders.size());

        return emailToLink;
    }

    private static String getBodyFromMessage(Message message) {
        if (message.getPayload().getParts() != null) {
            for (MessagePart part : message.getPayload().getParts()) {
                if ("text/html".equalsIgnoreCase(part.getMimeType())
                        && part.getBody() != null
                        && part.getBody().getData() != null) {
                    return new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
                }
            }
        }
        if (message.getPayload().getBody() != null
                && message.getPayload().getBody().getData() != null) {
            return new String(Base64.getUrlDecoder().decode(message.getPayload().getBody().getData()));
        }
        return "";
    }
}