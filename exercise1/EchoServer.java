/*
** Henry Au
** CS380 - Exercise 1
** Due: Monday, January 11, 2016 before midnight (50 points)
*/

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.*;

public final class EchoServer {

    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(22222)) {
            while (true) {
                //looking for clients to connect
                Socket socket = serverSocket.accept();

                Runnable getClients = () -> {
                    //get the address of the connectee
                    String address = socket.getInetAddress().getHostAddress();
                    //prompt that a client has connected
                    System.out.printf("Client connected: %s%n", address);
                    //open OutputStream first
                    OutputStream os = null;
                    try {
                        os = socket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //print stream out
                    PrintStream out = new PrintStream(os);
                    //prompt to client
                    //out.printf("Hi %s, thanks for connecting!%n", address);
                    //listen for input from Client
                    //receive InputStream from socket
                    InputStream is = null;
                    try {
                        is = socket.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //read InputStream in UTF-8
                    InputStreamReader isr = null;
                    try {
                        isr = new InputStreamReader(is, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //convert to BufferedReader
                    BufferedReader br = new BufferedReader(isr);
                    String clientInput = "";
                    while(clientInput!=null){
                        try {
                            clientInput = br.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!clientInput.equals("exit")){
                            out.println("Server> " + clientInput);
                        }
                        else break;
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.printf("Client disconnected: %s%n", address);

                    try {
                        is.close();
                        isr.close();
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                try{
                    Thread clientThread = new Thread(getClients);
                    clientThread.start();
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
