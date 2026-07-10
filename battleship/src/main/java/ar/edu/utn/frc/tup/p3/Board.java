package ar.edu.utn.frc.tup.p3;

import java.util.List;

/**
 * Esta clase representa un tablero de la Batall Naval
 *
 * Las diferentes instancias pueden ser el tablero del jugador,
 * el tablero de la app y los tableros de marcación de la flota enemiga
 */
public class Board {

    /**
     * Number of row required to play
     */
    private static final Integer ROWS = 10;

    /**
     * Number of columns required to play
     */
    private static final Integer COLUMNS = 10;

    /**
     * Letter to represent the water object in the board
     */
    private static final String WATER = "\u001B[34m~";

    /**
     * Letter to represent the ship object in the board
     */
    private static final String SHIP = "\u001B[32mS";

    /**
     * Letter to represent a hide cell in the board
     */
    private static final String HIDE = "\u001B[36m·";

    /**
     * Board to draw in the screen
     */
    private String[][] board;
    // TODO: more attributes if necessary

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    // TODO: constructors if necessary...
    public Board() {
        this.board = new String[ROWS][COLUMNS];
    }

    /**
     * Este metodo dibuja el tablero
     *
     * @see StringBuilder
     * @see #ROWS
     * @see #COLUMNS
     * @see #board
     *
     */
    public void drawBoard() {
        String RESET = "\u001B[0m";
        String CYAN = "\u001B[36m";
        StringBuilder sb = new StringBuilder();
        sb.append(CYAN + "    0   1   2   3   4   5   6   7   8   9\n" + RESET);
        sb.append(CYAN + "  ┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐\n" + RESET);
        for(int f = 0; f < ROWS; f++) {
            sb.append(CYAN + f + " |" + RESET);
            for(int c = 0; c < COLUMNS; c++) {
                sb.append(" " + this.board[f][c] + RESET + " ");
                sb.append(CYAN + "|" + RESET);
            }
            sb.append(CYAN + "  " + f + "\n" + RESET);
            if (f < ROWS - 1) {
                sb.append(CYAN + "  ├───┼───┼───┼───┼───┼───┼───┼───┼───┼───┤\n" + RESET);
            }
        }
        sb.append(CYAN + "  └───┴───┴───┴───┴───┴───┴───┴───┴───┴───┘\n" + RESET);
        sb.append(CYAN + "    0   1   2   3   4   5   6   7   8   9\n" + RESET);
        System.out.println(sb);
    }

    /**
     * Este metodo posiciona la lista de barcos en el tablero
     *
     * @param ShipList Lista de barcos a posicionar
     *
     * @see Ship#getPosition()
     * @see #SHIP
     */
    public void setShipPositions(List<Ship> ShipList) {
        for(Ship ship : ShipList) {
            this.board[ship.getPosition().getRow()][ship.getPosition().getColumn()] = SHIP;
        }
    }

    /**
     * Este metodo pone "W" (water/agua) en la posicion del tablero pasada por parametro
     *
     * @param position Posicion donde poner agua
     *
     * @see #WATER
     */
    public void setWaterOnBoard(Position position) {
        this.board[position.getRow()][position.getColumn()] = WATER;
    }

    /**
     * Este metodo pone "S" (shgip/barco) en la posicion del tablero pasada por parametro
     *
     * @param position Posicion donde poner un barco
     *
     * @see #SHIP
     */
    public void setShipOnBoard(Position position) {
        this.board[position.getRow()][position.getColumn()] = SHIP;
    }

    /**
     * Este metodo inicia el tablero para la flota del jugador
     * con agua en todas sus celdas.
     *
     * @see #ROWS
     * @see #COLUMNS
     * @see #WATER
     */
    public void initBoardFleet() {
        for(int f = 0; f < ROWS; f++) {
            for(int c = 0; c < COLUMNS; c++) {
                this.board[f][c] = WATER;
            }
        }
    }

    /**
     * Este metodo inicia el tablero para la flota enemiga
     * con "_" (guiones bajos) en todas sus celdas. Este es el modo oculto
     * de lo que hay en el tablero.
     *
     * @see #ROWS
     * @see #COLUMNS
     * @see #HIDE
     */
    public void initBoardEnemyFleet() {
        for(int f = 0; f < ROWS; f++) {
            for(int c = 0; c < COLUMNS; c++) {
                this.board[f][c] = HIDE;
            }
        }
    }
}
