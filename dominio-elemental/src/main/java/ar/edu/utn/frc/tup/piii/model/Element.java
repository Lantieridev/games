package ar.edu.utn.frc.tup.piii.model;

public enum Element {
    FUEGO, AIRE, TIERRA, AGUA;

    public static Element next(Element e) {
        return switch (e) {
            case FUEGO -> AIRE;
            case AIRE -> TIERRA;
            case TIERRA -> AGUA;
            case AGUA -> null;
        };
    }
}
