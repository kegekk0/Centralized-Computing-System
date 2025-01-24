import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Statistics stats;

    public ClientHandler(Socket clientSocket, Statistics stats) {
        this.clientSocket = clientSocket;
        this.stats = stats;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                stats.incrementRequestCount();
                String[] parts = request.split(" ");
                if (parts.length != 3) {
                    out.println("ERROR");
                    stats.incrementInvalidOps();
                    continue;
                }

                String oper = parts[0];
                try {
                    int arg1 = Integer.parseInt(parts[1]);
                    int arg2 = Integer.parseInt(parts[2]);
                    int result;

                    switch (oper) {
                        case "ADD":
                            result = arg1 + arg2;
                            stats.incrementOperation("ADD");
                            break;
                        case "SUB":
                            result = arg1 - arg2;
                            stats.incrementOperation("SUB");
                            break;
                        case "MUL":
                            result = arg1 * arg2;
                            stats.incrementOperation("MUL");
                            break;
                        case "DIV":
                            if (arg2 == 0) throw new ArithmeticException("Division by zero");
                            result = arg1 / arg2;
                            stats.incrementOperation("DIV");
                            break;
                        default:
                            out.println("ERROR");
                            stats.incrementInvalidOps();
                            continue;
                    }

                    out.println(result);
                    stats.addToSum(result);
                } catch (NumberFormatException | ArithmeticException e) {
                    out.println("ERROR");
                    stats.incrementInvalidOps();
                }
            }
        } catch (IOException e) {
            System.err.println("Client communication error: " + e.getMessage());
        }
    }
}
