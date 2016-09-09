/*
** Henry Au
** CS380 - Exercise 2
** Due: Wednesday, January 18, 2016 before midnight (50 points)
*/

import java.io.*;
import java.net.Socket;
import java.util.zip.CRC32;
import java.util.*;

public final class Exercise2Client {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("cs380.codebank.xyz", 38101)) {
          System.out.println("Connected to server.");
        	//receive InputStream from socket
        	InputStream is = socket.getInputStream();
        	//open OutputStream first
          OutputStream os = socket.getOutputStream();
          int input1;
          int input2;
          int one;
          byte[] arr = new byte[100];
          boolean open = true;
          //change to while socket is still open
          while(open){       
            int count = 0; 
            System.out.println("Received bytes:");
            while(count < 100){
              input1 = is.read();
              input2 = is.read();

              if(count%10 == 0){
                if(count!=0)
                  System.out.print("\n");
                System.out.print("  ");
              }

              System.out.print(Integer.toHexString(input1).toUpperCase()+Integer.toHexString(input2).toUpperCase());
              input1 = input1 << 4;
              one = input1 ^ input2;
              arr[count] = (byte)one;
              count++;
            }
            System.out.println();
            //generate CRC 
            CRC32 crc = new CRC32();
            crc.update(arr);
            long errorCode = crc.getValue();
            System.out.println("Generated CRC32: " + Long.toHexString(errorCode).toUpperCase()+".");
            byte[] arr1 = new byte[4];

            count = 0;
            while(count<4){
              arr1[3-count] = (byte)(errorCode & 0xFF);
              errorCode >>= 8;
              count++;
            }
            os.write(arr1);

            if(is.read() == 1) System.out.println("Response good.");
             
            else System.out.println("Response bad.");
            

            open = false;
            socket.close();
            System.out.println("Disconnected from server.");
          }
        }
    }
}
