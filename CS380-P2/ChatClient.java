/*
** Henry Au
** CS380 - Project 2
** Due: Wednesday, January 27, 2016 before midnight (40 points)
** Type "exit" for connection disconnection
*/

import java.io.*;
import java.net.Socket;

public final class ChatClient{
    public static void main(String[] args) throws Exception{
        Socket socket = new Socket("cs380.codebank.xyz", 38002);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

        String clientInput = "";
        System.out.print("Enter your username: ");
        clientInput = sysin.readLine();
        out.println(clientInput);

        //read from server
        Runnable reader = () -> {
            while(true){
            	String inputRead = "";
                try {
                    while(inputRead != null){
                    	inputRead = in.readLine();
                    	if(inputRead == null){
                    		break;
                    	}
                    	else{
                    		System.out.println(inputRead);	
                    	}
                    }
                } catch (IOException e) {
                    System.out.println("Connection disconnected.");
                }
                break;
            }
        };

        Thread readServer = new Thread(reader);
        readServer.start();
        
        boolean open = true;
        while(open){
        	clientInput = sysin.readLine();
        	if(clientInput.equals("exit")){
        		open = false;
        		break;
        	}
        	else out.println(clientInput);
        }
        
        socket.close();
    }
}