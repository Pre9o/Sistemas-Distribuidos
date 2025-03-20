import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Player implements Runnable {
    char mark;
    Player opponent;
    Socket socket;
    Scanner input;
    PrintWriter output;

    public Player(Socket socket, char mark) {
        this.socket = socket;
        this.mark = mark;
    }

    @Override
    public void run() {
        try {
            setup();
            processCommands();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (opponent != null && opponent.output != null) {
                opponent.output.println("OTHER_PLAYER_LEFT");
            }
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setup() throws IOException {
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);
        output.println("WELCOME " + mark);
        if (mark == 'X') {
            output.println("MESSAGE Waiting for opponent to connect");
        }
        else {
            opponent = currentPlayer;
            opponent.opponent = this;
            opponent.output.println("MESSAGE Your move");
        }
    }

    private void processCommands() {
        while (input.hasNextLine()) {
            var command = input.nextLine();
            if (command.startsWith("QUIT")) {
                return;
            }
            else if (command.startsWith("MOVE")) {
                processMoveCommand(Integer.parseInt(command.substring(5)));
            }
        }
    }

    private void processMoveCommand(int location) {
        try {
            move(location, this);
            output.println("VALID_MOVE");
            opponent.output.println("OPPONENT_MOVED " + location);
            if (hasWinner()) {
                output.println("VICTORY");
                opponent.output.println("DEFEAT");
            }
            else if (boardFilledUp()) {
                output.println("TIE");
                opponent.output.println("TIE");
            }
        } catch (IllegalStateException e) {
            output.println("MESSAGE " + e.getMessage());
        }
    }
}
