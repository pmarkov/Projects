package com.markov;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class BeatBoxServer {

    private ArrayList<ObjectOutputStream> clientOutputStreams;

    public void go() {
        clientOutputStreams = new ArrayList<>();

        try {
            ServerSocket serverSocket = new ServerSocket(4242);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(out);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tellEveryone(Object one, Object two) {
        for(ObjectOutputStream out : clientOutputStreams) {
            try {
                out.writeObject(one);
                out.writeObject(two);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        ObjectInputStream in;
        Socket clientSocket;

        ClientHandler(Socket socket) {
            try {
                clientSocket = socket;
                in = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Object o1;
            Object o2;

            try {
                while((o1 = in.readObject()) != null) {
                    o2 = in.readObject();
                    tellEveryone(o1, o2);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
