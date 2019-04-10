package com.nedap.university;

import OwnCode.Server;

public class Main {

    private Main() {}

    public static void main(String[] args) {
        int portServer = 8888;
        int portClient = 8000;
        Server server = new Server(portClient,portServer);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }


}
/*
    private static boolean keepAlive = true;
    private static boolean running = false;

public static void main(String[] args) {
        running = true;
        System.out.println("Hello, Nedap University!");

        initShutdownHook();

        while (keepAlive) {
            try {
                // do useful stuff
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Stopped");
        running = false;
    }

    private static void initShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                keepAlive = false;
                while (running) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }
 */
