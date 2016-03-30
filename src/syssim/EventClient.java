package syssim;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingDeque;

public class EventClient {
    private LinkedBlockingDeque<String[]> queue = new LinkedBlockingDeque<String[]>();
    private Socket clientSocket;
    private OutputStream outputStream;
    private boolean running = true;

    public EventClient(String host, int port) throws SysSimException {
        try {
            clientSocket = new Socket(host, port);
            outputStream = clientSocket.getOutputStream();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running) {
                        try {
                            String[] message = queue.take();
                            StringBuffer buf = new StringBuffer();
                            boolean first = true;
                            for (String token : message) {
                                if (first) {
                                    first = false;
                                } else {
                                    buf.append(",");
                                }
                                buf.append(token);
                            }
                            buf.append("\n");
                            outputStream.write(buf.toString().getBytes());
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
        } catch (UnknownHostException e) {
            throw new SysSimException(e);
        } catch (IOException e) {
            throw new SysSimException(e);
        }
    }

    public void sendMessage(String[] message) {
        queue.add(message);
    }

    public void close() throws SysSimException {
        try {
            running = false;
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new SysSimException(e);
        }

    }

}
