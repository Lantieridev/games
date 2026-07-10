# EXAMEN FINAL - PROGRAMACIÓN BACKEND JAVA

## Juego: Dominio Elemental

### Objetivo del examen:
Desarrollar un juego por consola donde un jugador humano compite contra una máquina.

### Reglas del juego:
1. Se utiliza un mazo de 36 cartas (4 elementos x valores del 1 al 9).
2. Cada jugador comienza con 5 cartas.
3. En cada turno: robar carta -> realizar acción -> descartar si supera 7 cartas.
4. Las combinaciones se colocan en una mesa compartida.

### Combinaciones válidas:
1. **Secuencia**: mismo elemento y valores consecutivos.
2. **Tríada**: mismo valor y elementos distintos.
3. **Cadena reactiva**: Fuego -> Aire -> Tierra -> Agua.

### Puntaje:
1. **Secuencia**: suma de valores.
2. **Tríada**: valor x 3.
3. **Cadena reactiva**: suma total x 2.
4. Los puntos se acumulan al bajar combinaciones.

### Dinámica de juego:
1. El jugador puede crear, extender o intervenir combinaciones.
2. La máquina debe tomar decisiones automáticamente.
3. El juego termina cuando se acaba el mazo.
4. Gana quien tenga mayor puntaje.

### Requisitos técnicos:
1. Implementar en Java con POO.
2. Interfaz por consola.
3. Algoritmo eficiente para decisiones de la máquina.
4. Tests unitarios con JUnit.
5. Explicar decisiones del algoritmo.

### Evaluación:
1. Modelado: 15%
2. Lógica: 25%
3. Motor de juego: 20%
4. IA: 25%
5. Tests: 15%

---
**IMPORTANTE:**