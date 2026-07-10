package ar.edu.utn.frc.tup.p3;

/**
 * Códigos ANSI de color compartidos por toda la UI de consola.
 * Antes cada clase declaraba su propia copia privada de estas constantes;
 * BattleShipGame las usaba sin declararlas, lo que rompía la compilación.
 */
public final class AnsiColors {

    private AnsiColors() {
        // Clase de constantes: no se instancia.
    }

    public static final String RESET = "[0m";
    public static final String RED = "[31m";
    public static final String GREEN = "[32m";
    public static final String BLUE = "[34m";
    public static final String CYAN = "[36m";
    public static final String YELLOW = "[33m";
    public static final String WHITE_BOLD = "\033[1;37m";
}
