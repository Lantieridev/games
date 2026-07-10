package com.utn.buscaminas;

public final class Main {

    private Main() {
        // Entry-point class: only main() matters, never instantiated.
    }

    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
