package ar.edu.utn.frc.tup.piii.model;

public record Card(Element element, int value) {
    @Override
    public String toString() {
        return "[" + element + " " + value + "]";
    }
}
