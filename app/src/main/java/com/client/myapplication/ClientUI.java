//package com.client.myapplication;
//
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.ConnectException;
//import java.net.InetAddress;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//
//public class ClientUI {
//    static Client client;
//    static BufferedReader reader;
//
//    /**
//     * If the player chooses online mode, this method will be called.
//     * This is where the online games are played for the client.
//     */
//    public static void onlineMode() {
//        try {
//            client = new Client();
//            String line;
////            while (true) {
////                System.out.println("Give a server address: ");
////                System.out.println(InetAddress.getLocalHost());
////                line = reader.readLine();
////                if (line.equals("quit")) {
////                    return;
////                }
////                InetAddress address = InetAddress.getByName(line);
////                //InetAddress address = InetAddress.getLocalHost();
////
////                System.out.println("Give a port number: ");
////                line = reader.readLine();
////                if (line.equals("EXIT")) {
////                    return;
////                }
////                int port = Integer.parseInt(line);
////                //int port = 4;
////
////                //connects to the server
////                if (client.connect(address, port)) {
////                    break;
////                } else {
////                    System.out.println("Connection failed");
////                }
////            }
//
//            System.out.println("Connection was successful");
//
//            //initiates the hello sequence
//            System.out.println("\nWrite description for your client");
//            String description = reader.readLine();
//            client.initiateHello(description);
//
//            //initiates the login sequence
//            System.out.println("\nWrite username");
//            String name = reader.readLine();
//            client.initiateLogin(name);
//
//            while (client.isAlready()) {
//                System.out.println("Unfortunately, this username is already taken." +
//                        "\nPlease try choosing a different one. Write a new username.");
//                name = reader.readLine();
//                client.initiateLogin(name);
//            }
//
//            System.out.println("\nCommands:" +
//                    "\n LIST - request a list of other clients" +
//                    "\n QUEUE - indicate that you want to participate in a game" +
//                    "\n MOVE~<location> - place a move to the wanted location" +
//                    "\n QUIT - quit from the game, disconnect from the server\n");
//
//            while (true) {
//                System.out.println("Write a command");
//                line = reader.readLine();
//                String[] command = line.split("~");
//                switch (command[0]) {
//                    case "LIST":
//                        client.sendCommand(line);
//                        break;
//                    case "QUEUE": {
//                        if (!client.getIsInQueue() && !client.getIsInGame()) {
//                            client.setIsInQueue(true);
//                            client.makeChoice();
//                            client.sendCommand(line);
//                            System.out.println("Waiting for an opponent to join...");
//                        } else {
//                            System.out.println("\nYou are not supposed to queue now.\n");
//                        }
//                    }
//                    break;
//
//                    case "QUIT": {
//                        client.sendCommand(command[0]);
//                        client.close();
//                        System.out.println("Client was closed manually.");
//                    }
//                    return;
//
//                    default: {
//                        System.out.println("Not valid command." +
//                                "\n LIST - request a list of other clients" +
//                                "\n QUEUE - indicate that you want to participate in a game" +
//                                "\n MOVE~<location> - place a move to the wanted location" +
//                                "\n QUIT - quit from the game, disconnect from the server");
//                        break;
//                    }
//                }
//            }
//        } catch (UnknownHostException e) {
//            System.out.println("Unknown host");
//        } catch (ConnectException e) {
//            System.out.println("Wrong port number");
//        } catch (SocketException e) {
//            System.out.println("No socket with that address or port");
//        } catch (IOException e) {
//            System.out.println("Problem with input");
//        } catch (NumberFormatException e) {
//            System.out.println("Wrong input");
//        }
//    }
//
//    /**
//     * .
//     * Mainly receives the input from the user and act accordingly
//     *
//     * @param args String[]
//     */
////    public static void main(String[] args) {
////        try {
////            System.out.println("Hi, this is an othello game interface!\n");
////            reader = new BufferedReader(new InputStreamReader(System.in));
////            while (true) {
////                System.out.println("\n CHOOSE YOUR OPTION\n\n" + "1)  OFFLINE\n\n" + "2)  ONLINE");
////                String line = reader.readLine();
////
////                while (!line.equals("1") && !line.equals("2") && !line.equals("quit")) {
////                    System.out.println("Wrong command, possible commands: 1 or 2");
////                    line = reader.readLine();
////                }
////
////                if (line.equals("2")) {
////                    onlineMode();
////                } else if (line.equals("1")) {
////                    System.out.println("Offline mode chosen.");
////                    GameTUI tui = new GameTUI();
////                    tui.playOffline();
////                } else {
////                    System.out.println("The program was interrupted.\n" + "Thank you for playing!");
////                    System.out.println("Program was closed manually.");
////                    break;
////                }
////            }
////
////        } catch (IOException e) {
////            throw new RuntimeException(e);
////        }
////
////    }
//}
