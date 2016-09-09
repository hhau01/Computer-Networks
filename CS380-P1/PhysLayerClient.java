/*
** Henry Au
** CS380 - Project 1
** Due: Wednesday, January 20, 2016 before midnight (50 points)
*/
import java.io.*;
import java.net.Socket;

public final class PhysLayerClient{

	public static void main(String[] args) throws Exception{
		try(Socket socket = new Socket("cs380.codebank.xyz", 38201)){
			System.out.println("Connected to server.");
			InputStream is = socket.getInputStream();
			int input;
			double average;
			int sum = 0;
			//read first 64 bits
			for(int i = 0; i < 64; i++){
				input = is.read();
				sum += input;
			}
			average = sum / 64.0;
			System.out.print("Baseline established from preamble: ");
			System.out.printf("%.2f", average);
			System.out.println();

			//if higher then 1; if lower then 0
			//convert to NRZI	
			String signals = "";
			for(int i = 0; i < 320; i++){
				input = is.read();
				if(input > average)
					signals += "1";
				else
					signals += "0";
			}

			String nrzi = "";
			nrzi += signals.charAt(0);
			for(int i = 1; i < 320; i++){
				//if current equals previous 0 otherwise 1
				if(signals.charAt(i) == signals.charAt(i-1)){
					nrzi += "0";
				}
				else{
					nrzi += "1";
				}
			}

			//5B -> 4B
			String fourBit = "";
			for(int i = 0; i < nrzi.length(); i+=5){
				if(nrzi.substring(i,i+5).equals("11110")){
					fourBit += "0";
				}
				else if(nrzi.substring(i,i+5).equals("01001")){
					fourBit += "1";
				}					
				else if(nrzi.substring(i,i+5).equals("10100")){
					fourBit += "2";
				}					
				else if(nrzi.substring(i,i+5).equals("10101")){
					fourBit += "3";
				}					
				else if(nrzi.substring(i,i+5).equals("01010")){
					fourBit += "4";
				}					
				else if(nrzi.substring(i,i+5).equals("01011")){
					fourBit += "5";
				}					
				else if(nrzi.substring(i,i+5).equals("01110")){
					fourBit += "6";
				}					
				else if(nrzi.substring(i,i+5).equals("01111")){
					fourBit += "7";
				}					
				else if(nrzi.substring(i,i+5).equals("10010")){
					fourBit += "8";
				}					
				else if(nrzi.substring(i,i+5).equals("10011")){
					fourBit += "9";
				}					
				else if(nrzi.substring(i,i+5).equals("10110")){
					fourBit += "A";
				}					
				else if(nrzi.substring(i,i+5).equals("10111")){
					fourBit += "B";
				}					
				else if(nrzi.substring(i,i+5).equals("11010")){
					fourBit += "C";
				}					
				else if(nrzi.substring(i,i+5).equals("11011")){
					fourBit += "D";
				}					
				else if(nrzi.substring(i,i+5).equals("11100")){
					fourBit += "E";
				}					
				else if(nrzi.substring(i,i+5).equals("11101")){
					fourBit += "F";
				}					
			}
			System.out.println("Received 32 bytes: " + fourBit);
			int first, second, whole;
			int count = 0;
			byte[] byteArray = new byte[32];

			//convert to byte array and send
			for(int i = 0; i < fourBit.length(); i+=2){
				first = Integer.parseInt(String.valueOf(fourBit.charAt(i)),16);
				second = Integer.parseInt(String.valueOf(fourBit.charAt(i+1)),16);
				first = first << 4;
				whole = first ^ second;
				byteArray[count] = (byte)whole;
				count++;
			}

			OutputStream os = socket.getOutputStream();
			os.write(byteArray);
			
			if(is.read() == 1) System.out.println("Response good.");
             
            else System.out.println("Response bad.");
            
			socket.close();
			System.out.println("Disconnected from server.");
		}
	}
}