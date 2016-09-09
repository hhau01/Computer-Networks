/*
** Henry Au
** CS380 - Project 4
** Due: Wednesday, February 10th, 2016 by midnight (50 points)
*/

import java.io.*;
import java.net.Socket;
import java.util.Random;

public final class Ipv6Client{
	public static void main(String[] args) throws Exception{	
		try(Socket socket = new Socket("cs380.codebank.xyz", 38004)){		
			OutputStream os = socket.getOutputStream(); 		
			InputStream is = socket.getInputStream();		
			for(int i = 1; i < 13; i++){
				//min is 20 +2,4,6,8.. etc			
				int length = (int)Math.pow(2,i) + 40; 
				byte[] packet = new byte[length];
				int payload = length - 40;			
				
				//version 6 and do not implement traffic class
				packet[0] = (byte)0x60;
				
				//traffic class and flow label do not implement
				packet[1] = 0;			
				packet[2] = 0;
				packet[3] = 0;
				
				//payload length 16-bit unsigned
				packet[4] = (byte)(payload >> 8);
				packet[5] = (byte)(payload);
				
				//next header 8bit selector set to UDP protocol value  
				packet[6] = (byte)0x11;

				//hop limit 8bit unsigned integer set to 20
				packet[7] = (byte)0x14;
				
				//source addr:
				// 128bit address assuming it is a valid ipv4 address that has 
				// been extended to ipv6 for a device that does not use ipv6
				packet[8] = 0; 
				packet[9] = 0;
				packet[10] = 0;
				packet[11] = 0;
				packet[12] = 0;
				packet[13] = 0;
				packet[14] = 0;
				packet[15] = 0;
				packet[16] = 0;
				packet[17] = 0;
				packet[18] = (byte)0xFF;
				packet[19] = (byte)0xFF;				
				packet[20] = (byte)0x55;
				packet[21] = (byte)0x37;
				packet[22] = (byte)0x80;
				packet[23] = (byte)0x08;

				//destination addr:
				// 128bit address assuming it is a valid ipv4 address that has 
				// been extended to ipv6 for a device that does not use ipv6 
				// using the ip address of the server you are connecting to
				packet[24] = 0; 
				packet[25] = 0;
				packet[26] = 0;
				packet[27] = 0;
				packet[28] = 0;
				packet[29] = 0;
				packet[30] = 0;
				packet[31] = 0;
				packet[32] = 0;
				packet[33] = 0;
				packet[34] = (byte)0xFF;
				packet[35] = (byte)0xFF;				
				packet[36] = (byte)0x34;
				packet[37] = (byte)0x21;
				packet[38] = (byte)0x83;
				packet[39] = (byte)0x10;

				//random data
				for(int j = 40; j < length; j++){
					Random random = new Random();
					packet[j] = (byte)(random.nextInt());
				}
				os.write(packet);

				// code		reason
				// 0xcafebabe	packet was correct
				// 0xcafed00d	incorrect version
				// 0xdeadf00d	one of the 'do not implement' fields was not all 0s
				// 0xbbadbeef	payload length is wrong
				// 0xfeedface	next header is wrong
				// 0xfee1dead	hop limit is wrong
				// 0xdeadc0de	bad source address
				// 0xabadcafe	bad destination address
				byte[] response = new byte[4];
				is.read(response);
				System.out.print("0x");	
				for(int k = 0; k < response.length; k++){
					System.out.print(Integer.toHexString(response[k]).toUpperCase().substring(6,8));
				}
				System.out.println();		
			}
		}
	}	
}

