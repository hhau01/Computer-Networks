/*
** Henry Au
** CS380 - Exercise 1
** Due: Monday, January 11, 2016 before midnight (50 points)
*/

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.*;
import java.util.*;

public final class EchoClient {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 22222)) {
        	//receive InputStream from socket
        	InputStream is = socket.getInputStream();
        	//open OutputStream first
            OutputStream os = socket.getOutputStream();
            //print stream out
            PrintStream out = new PrintStream(os);
        	//read InputStream in UTF-8
           	InputStreamReader isr = new InputStreamReader(is, "UTF-8");
           	//convert to BufferedReader
           	BufferedReader br = new BufferedReader(isr);
           	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
           	String clientInput = "";
           	boolean open = true;
           	//change to while socket is still open
           	while(open){
           		//System.out.print("Client> ");
           		clientInput = in.readLine();
                out.println(clientInput);

                if(clientInput.equals("exit")){
                	open = false;
                	break;
                } 
                else System.out.println(br.readLine());
            }
        }
    }
}
