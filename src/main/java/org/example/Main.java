package org.example;

import com.google.api.services.gmail.Gmail;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            // Get Gmail service
            Gmail service = GmailService.getGmailService();

            // Initialize unsubscriber scanner
            Unsubscriber unsubscriber = new Unsubscriber(service);

            // Scan inbox and extract unsubscribe links
            Map<String, String> emailToLink = unsubscriber.scanAllEmails();

            // Optional: print all links again at the end
            System.out.println("\nSenders with unsubscribe links found:");
            emailToLink.forEach((sender, link) -> System.out.println(sender + " -> " + link));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
