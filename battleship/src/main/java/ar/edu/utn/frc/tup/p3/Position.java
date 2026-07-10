package ar.edu.utn.frc.tup.p3;

/**
 * Esta clase representa una posicion en el tablero
 *
 * La clase posee las coordenadas en forma de row y column,
 * que son una posicion en la matriz de Boar
 *
 * @see Board#board
 */
public class Position {

    /**
     * Represent a row location
     */
    private Integer row;

    /**
     * Represent a column location
     */
    private Integer column;

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public Position() {
    }

    public Position(Integer row, Integer column) {
        this.row = row;
        this.column = column;
    }

    /**
     * Este metodo debe validar si una Position es igual a otro en base a su row y column.
     * Es decir, si esta istancia de Position tiene los mismo datos
     * de row y column del objeto pasado por parametro.
     *
     * @param obj La posicion a comparar contra la instancia en la que se ejecuta el metodo
     *
     * @see Object#equals(Object)
     *
     * @return true si row y column son iguales que row y column de obj, false si no se da esta condición.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Position position = (Position) obj;
        return this.row.equals(position.getRow()) && this.column.equals(position.getColumn());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(row, column);
    }
}
