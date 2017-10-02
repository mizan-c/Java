package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class TcpServer {
    public static final int PORT = 5720;
    private static final int MAX_CONNECTION = 1000;
    private DestThread destthread = null;

    HashMap<Integer, OutputStreamWriter> hashmap = new HashMap<>();

    private ServerListener listener;

    /* public static void main(String[] args) {
        GUI gui = new GUI();
        gui.createGui();
        gui.titleLabel.setText("TCP Server");
        TcpServer tcpServer = new TcpServer();
        tcpServer.startListening(PORT);
    }*/

    public void startListening(int port) {
        ServerSocket serversocket = null;
        try {
            serversocket = new ServerSocket(port, MAX_CONNECTION);

        } catch (IOException e) {
            System.err.println("Couldn't start server");
            return;
        }

        do {
            System.out.println("TcpServer Listening on port:" + port);
            try {
                Socket socket = serversocket.accept();
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(socket.getOutputStream());
                InputStream inputstream = socket.getInputStream();
                makeDestThread(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } while (true);
    }

    void makeDestThread(Socket socket) {

        destthread = new DestThread(socket);
        destthread.startThread();
    }

    public OutputStreamWriter getOutputStreamById(int id) {
        OutputStreamWriter outputStreamWriter = hashmap.get(id);
        return outputStreamWriter;
    }

    public void setServerListener(ServerListener serverListener) {
        this.listener = serverListener;
    }


    public class DestThread extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStreamWriter outStreamWriter;
        private Thread thread;
        boolean live = true;
        private int id;
        private String name;

        public DestThread(Socket socket) {
            this.socket = socket;

            try {
                inputStream = socket.getInputStream();
            } catch (Exception e) {
                System.err.println("Error creating thread:");
            }
        }

        public void startThread() {
            thread = new Thread(this);
            thread.start();
        }

        public void stopThread() {
            live = false;
            thread.interrupt();
        }


        public void run() {
            long threadId = 0;
            threadId = Thread.currentThread().getId();
            System.out.println("Starting thread of " + threadId);

            try {

                outStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                outStreamWriter.write(">> Write  ID:yourId:yourName to initiate session \n");
                outStreamWriter.flush();

                while (live) {
                    String s;
                    StringBuffer stringbuffer = new StringBuffer();
                    int i;

                    while ((i = inputStream.read()) != -1) {
                        if ((char) i == '\n')
                            break;

                        stringbuffer.append((char) i);
                    }
                    s = stringbuffer.toString().trim();

                    System.out.println("String from terminal:" + s);

                    if (s.toLowerCase().startsWith("id:")) {

                        String[] sl = s.split(":");
                        id = Integer.parseInt(sl[1]);
                        if (sl.length > 1) name = sl[2];
                        hashmap.put(id, outStreamWriter);
                        if (listener != null) {
                            listener.onNewClientJoin(id, name);
                            outStreamWriter.write(">> Connected, now write to server. \n");
                            outStreamWriter.flush();
                            System.out.println("send broadcast:" + id + " name:" + name);

                        } else System.out.println("listener null");


                    } else if (s.contains("exit")) {
                        break;

                    } else {
                        if (listener != null) listener.onNewMessage(id, s);

                        outStreamWriter.write(">>" + s + "\n");
                        outStreamWriter.flush();

                    }

                    outStreamWriter.write(" ");
                    outStreamWriter.flush();

                }

            } catch (Exception e) {
                // System.err.println("Error in while loop of thread " + " :" + e.toString());
                // System.out.println("exit from while");
                //break;
            }

            listener.onClientLeft(id, name);

            System.out.println("End thread of " + threadId);

            try {
                socket.close();
                this.stopThread();
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }

            return;
        }
    }

}
