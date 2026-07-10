package ar.edu.utn.frc.tup.p3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ar.edu.utn.frc.tup.p3.AnsiColors.RESET;
import static ar.edu.utn.frc.tup.p3.AnsiColors.RED;
import static ar.edu.utn.frc.tup.p3.AnsiColors.GREEN;
import static ar.edu.utn.frc.tup.p3.AnsiColors.BLUE;
import static ar.edu.utn.frc.tup.p3.AnsiColors.CYAN;
import static ar.edu.utn.frc.tup.p3.AnsiColors.YELLOW;
import static ar.edu.utn.frc.tup.p3.AnsiColors.WHITE_BOLD;

/**
 * Esta clase mantiene el estado de un juego.
 *
 * Un juego es una partida de las muchas que puede jugar el Player
 * en una misma corrida del programa.
 *
 */
public class BattleShipGame {

    /**
     * Expresion regular para validar entradas de posiciones
     */
    private static final String POSITION_INPUT_REGEX = "[0-9]{1} [0-9]{1}";

    /**
     * Numero de barcos requeridos para jugar
     */
    private static final Integer FLEET_SIZE = 20;

    /**
     * Scanner para capturar las entradas del usuario
     */
    private Scanner scanner = new Scanner(System.in);

    /**
     * Jugador asignado al usuario
     */
    private Player player;

    /**
     * Jugador asignado a la app
     */
    private Player appPlayer;

    /**
     * Tablero de la flota del jugador
     */
    private Board playerFleetBoard;

    /**
     * Tablero de marcación de la flota enemiga del jugador
     */
    private Board playerEnemyFleetBoard;

    /**
     * Tablero de la flota de la app
     */
    private Board appFleetBoard;

    /**
     * Tablero de marcación de la flota enemiga de la app
     */
    private Board appEnemyFleetBoard;

    /**
     * Lista de los disparos efectuados por el jugador
     */
    private List<Position> playerShots;

    /**
     * Lista de los disparos efectuados por la app
     */
    private List<Position> appShots;

    /**
     * Lista de los barcos del jugador
     */
    private List<Ship> playerShips;

    /**
     * Lista de los barcos de la app
     */
    private List<Ship> appShips;

    /**
     * Jugador que gano el juego
     */
    private Player winner;
    // TODO: more attributes if necessary

    // TODO: getters & setters...

    // TODO: constructors if necessary...
    public BattleShipGame(Player player, Player appPlayer) {
        this.player = player;
        this.appPlayer = appPlayer;
        this.playerFleetBoard = new Board();
        this.playerEnemyFleetBoard = new Board();
        this.appFleetBoard = new Board();
        this.appEnemyFleetBoard = new Board();
        this.playerShots = new ArrayList<>();
        this.appShots = new ArrayList<>();
        this.playerShips = new ArrayList<>();
        this.appShips = new ArrayList<>();
        this.winner = null;
        this.playerFleetBoard.initBoardFleet();
        this.playerEnemyFleetBoard.initBoardEnemyFleet();
        this.appFleetBoard.initBoardFleet();
        this.appEnemyFleetBoard.initBoardEnemyFleet();
    }

    public BattleShipGame() {
    }

    /**
     * Este metodo genera una lista de posiciones aleatoria para
     * la flota de barcos con la que jugará la App.
     *
     * Este metodo valida que las posiciones de la cada barco de la flota es unica
     * y que se encuentra dentro de los margenes del tablero.
     *
     * Por cada barco de la flota debe agregarlo en la lista "appShips"
     *
     * @see #getPlayerFleetPositions()
     * @see #generateAppShot()
     * @see #getRandomPosition()
     *
     */
    public void generateAppFleetPositions() {
        do {
            Position position = this.getRandomPosition();
            if(isAvailablePosition(appShips, position)) {
                this.appShips.add(new Ship(position, ShipStatus.AFLOAT));
            }
        } while (this.appShips.size() < FLEET_SIZE);
        appFleetBoard.setShipPositions(appShips);
    }

    /**
     * Este metodo gestiona el pedido de posiciones de cada barco al jugador,
     * y los agrega en la lista "playerShips".
     *
     * Se le pide al usuario por pantalla cada par de coordenadas como
     * dos Enteros separados por un espacio en blanco. Por cada coordenada que el usuario ingresa,
     * debe validarse que este dentro de los margenes del tablero y que NO haya colocado ya
     * otro barco en dicha posicion.
     *
     * Cuando el usuario ha colocado todos los barcos (20 en total),
     * el metodo los posiciona en el tablero del usuario.
     *
     * @see #generateAppFleetPositions()
     * @see #getPlayerShot()
     *
     */
    public void getPlayerFleetPositions() {
        System.out.println(YELLOW + "\nFase de Posicionamiento: Ubica tus " + FLEET_SIZE + " barcos." + RESET);
        while (this.playerShips.size() < FLEET_SIZE) {
            System.out.println("Barco #" + (playerShips.size() + 1));
            Position position = this.getPosition();
            if(isAvailablePosition(playerShips, position)) {
                this.playerShips.add(new Ship(position, ShipStatus.AFLOAT));
            } else {
                System.out.println(RED + "¡Error! Ya tienes un barco en esa posición." + RESET);
            }
        }
        playerFleetBoard.setShipPositions(playerShips);
    }

    /**
     * Este metodo gestiona la acción de disparar por parte del usuario.
     * Cuando el usuario estableció el disparo, debe agregarlo a la lista de
     * disparos realizados "playerShots" y cargarlo en su board de la flota enemiga "playerEnemyFleetBoard"
     * según haya derribado un barco o encontrado agua.
     *
     * Si el disparo alcanza un barco enemigo, se debe cambiar el barco de dicha posicion a ShipStatus.SUNKEN
     * mediante el metodo de Ship "sinkShip()"
     *
     * Se le pide al usuario por pantalla cada par de coordenadas como
     * dos Enteros separados por un espacio en blanco. Por cada coordenada que el usuario ingresa,
     * debe validarse que este dentro de los margenes del tablero.
     *
     * @see #getPlayerFleetPositions()
     *
     */
    public void getPlayerShot() {
        Position position = null;
        do {
            System.out.println(YELLOW + "\n--- TU TURNO DE DISPARO ---" + RESET);
            position = this.getPosition();
            if(!isAvailableShot(playerShots, position)) {
                System.out.println(RED + "Ya disparaste a esa posición. Elige otra." + RESET);
                position = null;
            }
        } while (position == null);

        this.playerShots.add(position);
        if(this.impactEnemyShip(appShips, position)) {
            System.out.println(GREEN + "¡IMPACTO! Has alcanzado un barco enemigo." + RESET);
            playerEnemyFleetBoard.setShipOnBoard(position);
        } else {
            System.out.println(BLUE + "Agua... Mala suerte." + RESET);
            playerEnemyFleetBoard.setWaterOnBoard(position);
        }
    }

    /**
     * Este metodo genera de manera aleatoria un disparo por parte de la app.
     * El metodo genera dos enteros entre 0 y 9 para definir las coordenadas
     * donde efectuará el disparo.
     *
     * El metodo valida que el disparo no se haya hecho antes de cargarlo en
     * la lista de disparos de la app.
     *
     * Cuando la app estableció el disparo, debe agregarlo a la lista de
     * disparos realizados "appShots" y cargarlo en su board de la flota enemiga "appEnemyFleetBoard"
     * según haya derribado un barco o encontrado agua.
     *
     * Si el disparo alcanza un barco enemigo, se debe cambiar el barco de dicha posicion a ShipStatus.SUNKEN
     * mediante el metodo de "ship.sinkShip()"
     *
     * @see #generateAppFleetPositions()
     * @see #getRandomPosition()
     */
    public void generateAppShot() {
        Position randomShot;
        do {
            randomShot = this.getRandomPosition();
        } while (!isAvailableShot(appShots, randomShot));

        this.appShots.add(randomShot);
        System.out.println(YELLOW + "\n--- TURNO DEL ENEMIGO ---" + RESET);
        if(this.impactEnemyShip(playerShips, randomShot)) {
            System.out.println(RED + "¡ALERTA! El enemigo impactó uno de tus barcos en " + randomShot.getRow() + " " + randomShot.getColumn() + RESET);
            appEnemyFleetBoard.setShipOnBoard(randomShot);
        } else {
            System.out.println(CYAN + "El enemigo disparó al agua en " + randomShot.getRow() + " " + randomShot.getColumn() + RESET);
            appEnemyFleetBoard.setWaterOnBoard(randomShot);
        }
    }

    /**
     * Este metodo imprime por pantalla el estado del juego, que incluye
     * cuantos barcos tiene cada jugar a flote y cuantos hundidos
     *
     * @see Player
     * @see #playerShips
     * @see #appShips
     * @see #player
     * @see #appPlayer
     *
     */
    public void printGameStatus() {
        long playerAfloat = playerShips.stream().filter(s -> s.getShipStatus() == ShipStatus.AFLOAT).count();
        long appAfloat = appShips.stream().filter(s -> s.getShipStatus() == ShipStatus.AFLOAT).count();
        
        System.out.println(WHITE_BOLD + "\nESTADO DE LA BATALLA:" + RESET);
        System.out.println(GREEN + " Tus barcos: " + playerAfloat + " a flote / " + (FLEET_SIZE - playerAfloat) + " hundidos" + RESET);
        System.out.println(RED + " Enemigo:    " + appAfloat + " a flote / " + (FLEET_SIZE - appAfloat) + " hundidos" + RESET);
        System.out.println("------------------------------------------");
    }

    /**
     * Este metodo dibuja los tableros del Player junto al titulo de cada uno.
     *
     * @see Board#drawBoard()
     *
     */
    public void drawPlayerBoards() {
        System.out.println(GREEN + "\n--- TU FLOTA ---" + RESET);
        playerFleetBoard.drawBoard();
        System.out.println(RED + "\n--- FLOTA ENEMIGA (Marcación) ---" + RESET);
        playerEnemyFleetBoard.drawBoard();
    }

    /**
     * Este metodo muestra un mensaje de finalizacion de la partida,
     * muestra el nombre del ganador, el puntaje obtenido en esta partida
     * y los puntajes acumulados a traves de las partidas jugadas.
     *
     * @see System#out
     */
    public void goodbyeMessage() {
        // TODO: Imprimir por pantalla un mensaje de despedida
    }

    /**
     * Este metodo calcula los puntos obtenidos por cada jugador en esta partida
     * y se los suma a los que ya traia de otras partidas.
     *
     * @see Player
     * @see #playerShips
     * @see #appShips
     * @see #player
     * @see #appPlayer
     *
     */
    public void calculateScores() {
        // TODO: Se debe completar este metodo
        // TODO: Calcular los puntos obtenidos por cada jugador en este juego
        // TODO: Sumar los puntos al score de cada jugardor
        // TODO: Sumar la partida ganada al jugador que ganó
    }

    /**
     * Este metodo verifica si hubo un impacto con el disparo,
     * si el disparo impacto, hunde el barco y retorna true.
     * Si el disparo no impacto retorna false
     *
     * @param fleetEnemyShips Lista de barcos de la flota enemiga
     * @param shot
     *
     * @see Position#equals(Object)
     * @see Ship#sinkShip()
     *
     * @return true si el disparo impacta, false si no lo hace.
     */
    private Boolean impactEnemyShip(List<Ship> fleetEnemyShips, Position shot) {
        for (Ship ship : fleetEnemyShips) {
            if (ship.getPosition().equals(shot)) {
                ship.sinkShip();
                return true;
            }
        }
        return false;
    }

    /**
     * Este metodo define si el juego terminó.
     * El juego termina cuando uno de los dos jugadores (El player o la app)
     * a hundido todos los barcos del contrario.
     *
     * Cuando el juego termina, este metodo setea en el atributo winner quien ganó.
     *
     * @see #validateSunkenFleet(List)
     * @see #winner
     *
     * @return true si el juego terminó, false si aun no hay un ganador
     */
    public Boolean isFinish() {
        if (validateSunkenFleet(appShips)) {
            winner = player;
            return true;
        }
        if (validateSunkenFleet(playerShips)) {
            winner = appPlayer;
            return true;
        }
        return false;
    }

    /**
     * Este metodo valida si aun queda algun barco a flote en la flota,
     * para determinar si toda la flota fue hundida.
     *
     * @param fleet
     *
     * @see Ship#getShipStatus()
     * @see ShipStatus
     *
     * @return true si toda la flota fue hundida, flase si al menos queda un barco a flote.
     */
    private Boolean validateSunkenFleet(List<Ship> fleet) {
        return fleet.stream().allMatch(s -> s.getShipStatus() == ShipStatus.SUNKEN);
    }

    /**
     * Este metodo gestiona la acción de pedir coordenads al usuario.
     *
     * Se le pide al usuario por pantalla cada par de coordenadas como
     * dos Enteros separados por un espacio en blanco. Por cada coordenada que el usuario ingresa,
     * debe validarse que este dentro de los margenes del tablero.
     *
     * @see #isValidPositionInput(String)
     * @see Position#Position()
     * @see String#split(String)
     *
     * @return La posicion elegida por el usuario.
     */
    private Position getPosition() {
        Position position = null;
        do {
            System.out.println("Ingrese una coordenada en un formato de dos numeros " +
                    "enteros entre 0 y 9 separados por un espacio en blanco.");

            String input = scanner.nextLine();

            if (isValidPositionInput(input)) {
                String[] coords = input.split(" ");
                position = new Position(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
            } else {
                System.out.println(RED + "Error: Formato inválido. Use 'fila columna' (ej: 0 5)." + RESET);
            }
        } while(position == null);
        return position;
    }

    /**
     * Este metodo valida que la entrada del usuario como String este en el formato establecido
     * de dos numeros enteros entre 0 y 9 separados por un espacio en blanco.
     *
     * @see BattleShipMatch#getYesNoAnswer(String)
     * @see Pattern#compile(String)
     * @see Pattern#matcher(CharSequence)
     * @see Matcher#matches()
     *
     * @param input La entrada que se capturo del usuario
     * @return true si el formato es valido, false si no lo es
     */
    private Boolean isValidPositionInput(String input) {
        Pattern pattern = Pattern.compile(POSITION_INPUT_REGEX);
        if (pattern.matcher(input).matches()) {
            return true;
        } else {
            return false;
        }
        // TODO: Remember to replace the return statement with the correct object
        //return null;
    }

    /**
     * Este metodo retorna una posición random que puede ser usada
     * para representar una posicion de un barco o un disparo random de la app.
     *
     * @see Random
     * @see Random#nextInt(int)
     * @see Position
     *
     * @return la posición del disparo random.
     */
    private Position getRandomPosition() {
        Random random = new Random();
        return new Position(random.nextInt(10), random.nextInt(10));
    }

    /**
     * Este metodo valida que la posición nueva no exista en la lista de posiciones que ya fueron cargadas
     * para eso recibe por parametro la lista donde hará la busqueda y la posicion a buscar.
     *
     * El metodo otorga un mecanismo de validacion de que un objeto del tipo Position
     * no existe en una lista del tipo List<Position>
     *
     * @see List#contains(Object)
     * @see Position#equals(Object)
     *
     * @param listShots la lista donde se hará la busqueda
     * @param position La nueva posicion a buscar
     * @return true si la posición no existe en la lista, false si ya existe esa posicion.
     */
    private Boolean isAvailableShot(List<Position> listShots, Position position) {
        return !listShots.contains(position);
    }

    /**
     * Este metodo valida que la posición pasada por parametro no exista dentro de las
     * posiciones de los barcos de la lista "listToCheck".
     *
     * @param listToCheck la lista donde se hará la busqueda
     * @param position La nueva posicion a buscar
     *
     * @see List#contains(Object)
     * @see Ship#equals(Object)
     *
     * @return true si la posición no existe en la lista, false si ya existe esa posicion.
     */
    private Boolean isAvailablePosition(List<Ship> listToCheck, Position position) {
        return listToCheck.stream().noneMatch(s -> s.getPosition().equals(position));
    }

}
