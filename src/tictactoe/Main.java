package tictactoe;

import java.util.Scanner;

public class Main {
    public static final byte SIZE = 3;
    public static final byte IN_ROW_TO_WIN = SIZE;

    public static void main(String[] args) {
        Field field = initField();
        Game game = new Game(field, IN_ROW_TO_WIN, CellType.X);

        while (game.getStatus() == GameStatus.NOT_FINISHED) {
            printField(game);
            Coords coords;
            do {
                coords = readCoords();
            } while (!game.canOccupyCell(coords));
            game.makeTurn(coords);
        }
        printField(game);
        printStatus(game);
    }

    public static Field initField() {
        CellType[][] cells = new CellType[SIZE][SIZE];
        for (byte i = 0; i < SIZE; ++i) {
            for (byte j = 0; j < SIZE; ++j) {
                cells[i][j] = CellType.EMPTY;
            }
        }
        return new Field(cells);
    }

    public static Field readField() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter cells: ");
        String cellsLine = scanner.nextLine();
        CellType[][] cells = parseCells(cellsLine);
        return new Field(cells);
    }

    public static CellType[][] parseCells(String cellsLine) {
        CellType[][] cells = new CellType[SIZE][SIZE];
        for (byte i = 0; i < SIZE; ++i) {
            for (byte j = 0; j < SIZE; ++j) {
                cells[i][j] = CellType.findBySymbol(cellsLine.charAt(i * SIZE + j));
            }
        }
        return cells;
    }

    public static Coords readCoords() {
        Scanner scanner = new Scanner(System.in);
        Coords coords = null;
        do {
            System.out.print("Enter the coordinates: ");
            byte x = scanner.nextByte();
            byte y = scanner.nextByte();
            if (x < 1 || x > SIZE || y < 1 || y > SIZE) {
                System.out.println("Coordinates should be from 1 to " + SIZE +"!");
                continue;
            }
            coords = new Coords(x, y);
        } while (coords == null);
        return coords;
    }

    public static void printField(Game game) {
        System.out.println("---------");
        for (byte y = SIZE; y > 0; --y) {
            System.out.print("| ");
            for (byte x = 1; x <= SIZE; ++x) {
                System.out.print(game.field.getType(x, y).symbol + " ");
            }
            System.out.println("|");
        }
        System.out.println("---------");
    }

    public static void printStatus(Game game) {
        System.out.println(getStatusMessage(game.getStatus()));
    }

    public static String getStatusMessage(GameStatus status) {
        switch (status) {
            case NOT_FINISHED:
                return "Game not finished";
            case DRAW:
                return "Draw";
            case X_WINS:
                return "X wins";
            case O_WINS:
                return "O wins";
            case IMPOSSIBLE:
                return "Impossible";
            default:
                return null;
        }
    }
}

class Game {
    Field field;
    final byte inRowToWin;
    CellType currentPlayerCellType;

    Game(Field field, byte inRowToWin, CellType firstPlayerCellType) {
        this.field = field;
        this.inRowToWin = inRowToWin;
        currentPlayerCellType = firstPlayerCellType;
    }

    void makeTurn(Coords coords) {
        occupyCell(coords, currentPlayerCellType);
        switchPlayer();
    }

    void switchPlayer() {
        if (currentPlayerCellType == CellType.X) {
            currentPlayerCellType = CellType.O;
        } else {
            currentPlayerCellType = CellType.X;
        }
    }

    boolean isCellOccupied(Coords coords) {
        return this.field.getType(coords.x, coords.y) != CellType.EMPTY;
    }

    boolean canOccupyCell(Coords coords) {
        return !isCellOccupied(coords);
    }

    void occupyCell(Coords coords, CellType type) {
        field.setType(coords.x, coords.y, type);
    }

    GameStatus getStatus() {
        int numberOfX = field.getNumberOfType(CellType.X);
        int numberOfO = field.getNumberOfType(CellType.O);
        if (numberOfX - numberOfO > 1 || numberOfO - numberOfX > 1) {
            return GameStatus.IMPOSSIBLE;
        }
        boolean xCanWin = numberOfX >= inRowToWin && field.containsNumberOfTypeInRow(inRowToWin, CellType.X);
        boolean oCanWin = numberOfO >= inRowToWin && field.containsNumberOfTypeInRow(inRowToWin, CellType.O);
        if (xCanWin && oCanWin) {
            return GameStatus.IMPOSSIBLE;
        }
        if (xCanWin) {
            return GameStatus.X_WINS;
        }
        if (oCanWin) {
            return GameStatus.O_WINS;
        }
        int numberOfEmpty = field.getNumberOfType(CellType.EMPTY);
        if (numberOfEmpty == 0) {
            return GameStatus.DRAW;
        }
        return GameStatus.NOT_FINISHED;
    }
}

enum GameStatus {
    NOT_FINISHED,
    DRAW,
    X_WINS,
    O_WINS,
    IMPOSSIBLE,
}

enum CellType {
    EMPTY('_'),
    X('X'),
    O('O'),
    ;

    char symbol;

    CellType(char symbol) {
        this.symbol = symbol;
    }

    static CellType findBySymbol(char symbol) {
        for (CellType value : CellType.values()) {
            if (value.symbol == symbol) {
                return value;
            }
        }
        return null;
    }
}

class Field {
    final CellType[][] cells;
    final byte size;

    Field(CellType[][] cells) {
        this.cells = cells;
        size = (byte) cells.length;
    }

    CellType getType(byte x, byte y) {
        return cells[size - y][x - 1];
    }

    public void setType(byte x, byte y, CellType type) {
        cells[size - y][x - 1] = type;
    }

    byte getNumberOfType(CellType type) {
        byte number = 0;
        for (byte i = 1; i <= size; ++i) {
            for (byte j = 1; j <= size; ++j) {
                if (getType(i,j) == type) {
                    ++number;
                }
            }
        }
        return number;
    }

    boolean containsNumberOfTypeInRow(byte number, CellType type) {
        byte numberInFirstDiagonal = 0;
        byte numberInSecondDiagonal = 0;
        for (byte i = 1; i <= size; ++i) {
            if (type == getType(i, i)) {
                ++numberInFirstDiagonal;
                if (numberInFirstDiagonal == number) {
                    return true;
                }
            }
            if (type == getType(i, (byte)(size + 1 - i))) {
                ++numberInSecondDiagonal;
                if (numberInSecondDiagonal == number) {
                    return true;
                }
            }
            byte numberInHorizontal = 0;
            byte numberInVertical = 0;
            for (byte j = 1; j <= size; ++j) {
                if (type == getType(i, j)) {
                    ++numberInHorizontal;
                    if (numberInHorizontal == number) {
                        return true;
                    }
                }
                if (type == getType(j, i)) {
                    ++numberInVertical;
                    if (numberInVertical == number) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

class Coords {
    byte x;
    byte y;

    public Coords(byte x, byte y) {
        this.x = x;
        this.y = y;
    }
}

