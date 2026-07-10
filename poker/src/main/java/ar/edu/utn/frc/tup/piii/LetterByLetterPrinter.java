package ar.edu.utn.frc.tup.piii;

/**
 * Clase usada para imprimir por pantalla mensajes letra a letra
 */
public class LetterByLetterPrinter {

    // ANSI Color Constants
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String BOLD = "\u001B[1m";

    public static boolean DISABLE_COLORS = false;

    /**
     * Método que imprime los mensajes por pantalla letra a letra dejando 10 ms entre cada letra
     * De esta manera da la sensación que el juego se va desarrollando como un chat entre el jugador
     * y el sistema. Esto mejora la experiencia de usuario.
     *
     * @param text texto a imprimir por pantalla
     */
    public static void println(String text) {
        println(text, RESET);
    }

    public static void println(String text, String color) {
        try {
            if (!DISABLE_COLORS) {
                System.out.print(color);
            }
            for (int i = 0; i < text.length(); i++) {
                System.out.print(text.charAt(i));
                Thread.sleep(8); 
            }
            if (!DISABLE_COLORS) {
                System.out.println(RESET);
            } else {
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void print(String text, String color) {
        try {
            if (!DISABLE_COLORS) {
                System.out.print(color);
            }
            for (int i = 0; i < text.length(); i++) {
                System.out.print(text.charAt(i));
                Thread.sleep(8);
            }
            if (!DISABLE_COLORS) {
                System.out.print(RESET);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
