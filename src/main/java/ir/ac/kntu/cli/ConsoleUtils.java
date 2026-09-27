package ir.ac.kntu.cli;

import java.io.IOException;

public final class ConsoleUtils {

    private ConsoleUtils() {
    }

    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        } catch (IOException e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    public static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        return repeat(' ', padding) + text + repeat(' ', width - text.length() - padding);
    }

    public static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
