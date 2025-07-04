package REPL;

import java.util.Scanner;

/**
 * Eine Read-Eval-Print-Loop (REPL) für den ModalInterpreter.
 * Ermöglicht die interaktive Eingabe von Termen und Regeln.
 */
public class Repl {

    public static void main(String[] args) {
        ModalInterpreter interpreter = new ModalInterpreter();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Modal-Interpreter REPL gestartet.");
        System.out.println("Verfügbare Befehle: :rules, :stats, :exit");

        while (true) {
            System.out.print("modal> ");
            String line = scanner.nextLine();

            if (line == null) { // EOF (Ctrl+D)
                break;
            }

            String input = line.trim();
            if (input.isEmpty()) {
                continue;
            }

            if (input.equalsIgnoreCase(":exit")) {
                break;
            }

            switch (input.toLowerCase()) {
                case ":rules":
                    interpreter.showRules();
                    break;
                case ":stats":
                    interpreter.showStats();
                    break;
                default:
                    interpreter.processInput(input);
                    break;
            }
        }

        scanner.close();
        System.out.println("Interpreter beendet.");
    }
}