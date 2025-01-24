import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class CCS {
    private static final int STATISTICS_INTERVAL = 10_000;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar CCS.jar <port>");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number");
            System.exit(1);
            return;
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();
        Statistics stats = new Statistics();

        threadPool.execute(() -> startUdpDiscovery(port));

        threadPool.execute(() -> startTcpServer(port, stats));

        threadPool.execute(() -> {
            while (true) {
                try {
                    Thread.sleep(STATISTICS_INTERVAL);
                    stats.printStatistics();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private static void startUdpDiscovery(int port) {
        try (DatagramSocket udpSocket = new DatagramSocket(port)) {
            byte[] buffer = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.startsWith("CCS DISCOVER")) {
                    String response = "CCS FOUND";
                    DatagramPacket responsePacket = new DatagramPacket(
                            response.getBytes(),
                            response.length(),
                            packet.getAddress(),
                            packet.getPort()
                    );
                    udpSocket.send(responsePacket);
                }
            }
        } catch (IOException e) {
            System.err.println("Error in UDP discovery: " + e.getMessage());
        }
    }

    private static void startTcpServer(int port, Statistics stats) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                stats.incrementClientCount();
                new Thread(new ClientHandler(clientSocket, stats)).start();
            }
        } catch (IOException e) {
            System.err.println("Error in TCP server: " + e.getMessage());
        }
    }
}
