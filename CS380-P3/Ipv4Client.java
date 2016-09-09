/*
** Henry Au
** CS380 - Project 3
** Due: Wednesday, February 3rd, 2016 before midnight (60 points)
*/

import java.io.*;
import java.net.Socket;

public final class Ipv4Client{
	public static void main(String[] args) throws Exception{	
		try(Socket socket = new Socket("cs380.codebank.xyz", 38003)){		
			OutputStream writer = socket.getOutputStream(); 		
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
			for(int i = 1; i < 13; i++){
				//min is 20 +2,4,6,8.. etc			
				int length = (int)Math.pow(2,i) + 20; 
				byte[] packet = new byte[length];			
				
				//version 4 and hlen 5
				packet[0] = (byte)0x45;
				
				//tos do not implement
				packet[1] = 0;			
				
				//total length
				packet[2] = (byte)(length >> 8);
				packet[3] = (byte)(length);
				
				//ident do not implement
				packet[4] = 0;
				packet[5] = 0;
				
				//flags assume no fragmentation so first bit is 0, second bit is 1, and third bit is 0
				// 0100 0000 = 0x40
				packet[6] = (byte)0x40;

				//offset do not implement
				packet[7] = 0;
				
				//ttl assume every packet has ttl of 50 = 0x32
				packet[8] = (byte)0x32;
				
				//protocol assume tcp(6) for all packets 
				packet[9] = (byte)0x06;
				
				//sourceaddr ip address of choice 0x55378008
				packet[12] = (byte)0x55;
				packet[13] = (byte)0x37;
				packet[14] = (byte)0x80;
				packet[15] = (byte)0x08;
				
				//destinationaddr from server
				packet[16] = (byte)0x34;
				packet[17] = (byte)0x21;
				packet[18] = (byte)0x83;
				packet[19] = (byte)0x10;
				
				//checksum
				int sum = checksum(packet);				
				packet[10] = (byte)(sum >> 8);
				packet[11] = (byte)(sum);

				writer.write(packet);
				System.out.println(reader.readLine());			
			}
		}
	}	
 
	static int checksum(byte[] array){
		int data, sum = 0, index = 0;
		int length = array.length;

		while(length > 1){
			data = (((array[index] << 8) & 0xFF00) | ((array[index + 1]) & 0xFF));
			sum += data;	
	 			 		 
			if((sum & 0xFFFF0000) != 0){
				sum &= 0xFFFF;
				sum++;
			}		 
			index += 2;
			length -= 2;
		 }		 
		 return ~(sum & 0xFFFF);
	 }
}

