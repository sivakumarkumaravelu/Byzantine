package syssim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Participant implements Runnable {
    static final AtomicLong eventCount = new AtomicLong();
    static final AtomicLong start = new AtomicLong(System.currentTimeMillis());

    private ExecutorService pool;
    private EventListener listener;
    private int port;
    private SimSystem simSystem;

    public Participant(EventListener listener, SimSystem simSystem, int port) {
        pool = Executors.newFixedThreadPool(10);
        this.listener = listener;
        this.simSystem = simSystem;
        this.port = port;
    }

    public void run() {
        try {
            ServerSocket welcomeSocket = new ServerSocket(port);
            System.out.println("Server started on " + port);
            while (true) {
                try {
                    final Socket connectionSocket = welcomeSocket.accept();
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            BufferedReader in = null;
                            try {
                                in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                                String line = null;
                                while ((line = in.readLine()) != null) {
                                    listener.eventReceived(simSystem, line.split(","));
                                }
                            } catch (Throwable e) {
                                try {
                                    in.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }

    public int getID() {
        return port;
    }

    public static interface EventListener {
        public void eventReceived(SimSystem simSystem, String[] event);

        public void participantStarted(SimSystem simSystem);
    }

    public EventListener getListener() {
        return listener;
    }

}
