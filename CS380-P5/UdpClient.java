/*
** Henry Au
** CS380 - Project 5
** Due: Wednesday, February 17th, 2016 before midnight (60 points)
*/

import java.io.*;
import java.net.Socket;
import java.util.Random;

public final class UdpClient{
	public static void main(String[] args) throws Exception{
		try(Socket socket = new Socket("cs380.codebank.xyz", 38005)){
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			//Add 0xDEADBEEF data
			byte[] data = new byte[4];
			
			data[0] = (byte)0xDE;
			data[1] = (byte)0xAD;
			data[2] = (byte)0xBE;
			data[3] = (byte)0xEF;

			//Create IPv4 handshake with 0xDEADBEEF and send
			os.write(createIPv4(data));

			//Receive response (0xCAFEBABE)
			byte[] response = new byte[4];
			is.read(response);
			System.out.print("0x");	
			for(int i = 0; i < response.length; i++){
				System.out.print(Integer.toHexString(response[i]).toUpperCase().substring(6,8));
			}
			System.out.println();

			//Receive response (Port Number)
			byte[] port = new byte[2];
			is.read(port);
			System.out.println("Port Number: " + port[0] + " " + port[1]);
			long rtt = 0;
			for(int j = 1; j < 13; j++){
				int plength = (int)Math.pow(2,j);
				byte[] udpPacket = createIPv4(createUDP(plength, port));

				long startTime = System.currentTimeMillis();
				os.write(udpPacket);

				is.read(response);
				long endTime = System.currentTimeMillis();
				// code		reason
				// 0xcafebabe	everything looks fine
				// 0xbaadf00d	problem with ipv4 portion of packet
				// 0xcafed00d	incorrect destination port in udp packet
				// 0xdeadc0de	invalid udp checksum
				// 0xbbadbeef	incorrect udp data length
				System.out.print("0x");	
				for(int i = 0; i < response.length; i++){
					System.out.print(Integer.toHexString(response[i]).toUpperCase().substring(6,8));
				}

				rtt += (endTime-startTime);
				System.out.print("\tRTT: " + (endTime-startTime) + "ms");
				System.out.println();
			}	
			System.out.println("Mean RTT: " + (rtt/12) + "ms");		
		}
	}

	static byte[] createIPv4(byte[] data){
		//From Ipv4Client
		byte[] packet = new byte[20 + data.length];
		packet[0] = (byte)0x45;
		packet[1] = 0;
		packet[2] = (byte)(packet.length >> 8);
		packet[3] = (byte)(packet.length);
		packet[4] = 0;
		packet[5] = 0;
		packet[6] = (byte)0x40;
		packet[7] = 0;
		packet[8] = (byte)0x32;
		packet[9] = (byte)0x11; //UDP Protocol
		packet[12] = (byte)0x55;
		packet[13] = (byte)0x37;
		packet[14] = (byte)0x80;
		packet[15] = (byte)0x08;
		packet[16] = (byte)0x34;
		packet[17] = (byte)0x21;
		packet[18] = (byte)0x83;
		packet[19] = (byte)0x10;

		packet[10] = (byte)(checksum(packet) >> 8);
		packet[11] = (byte)(checksum(packet));

		for(int i = 0; i < data.length; i++){
			packet[20 + i] = data[i];
		}
		return packet;
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

	 static byte[] createUDP(int size, byte[] portnum){
	 	int length = size + 8;
	 	byte[] packet = new byte[length];

	 	//Source port - anything
	 	packet[0] = (byte)0x80;
	 	packet[1] = (byte)0x08;

	 	//Destination port - one you received from server
	 	packet[2] = (byte)(portnum[0]);
	 	packet[3] = (byte)(portnum[1]);

	 	//Length
	 	packet[4] = (byte)(length >> 8);
	 	packet[5] = (byte)(length);

	 	//UDP Checksum init 0
	 	packet[6] = 0;
	 	packet[7] = 0;

	 	//Random data
	 	Random random = new Random();
	 	for(int j = 0; j < size; j++){
			packet[j+8] = (byte)(random.nextInt());
		}

	 	//UDP Checksum
	 	int sum = udpChecksum(packet);
	 	packet[6] = (byte)(sum >> 8);
	 	packet[7] = (byte)(sum);

	 	return packet;
	 }

	 static int udpChecksum(byte[] array){
	 	int checksum = 0;

	 	int length = array.length + 12;
	 	byte[] packet = new byte[length];

	 	//Pseudoheader

	 	//Source addr
	 	packet[0] = (byte)0x55;
		packet[1] = (byte)0x37;
		packet[2] = (byte)0x80;
		packet[3] = (byte)0x08;

		//Destination addr
		packet[4] = (byte)0x34;
		packet[5] = (byte)0x21;
		packet[6] = (byte)0x83;
		packet[7] = (byte)0x10; 

		//Zeroes
		packet[8] = 0;
		
		//Protocol - Set to UDP 17
		packet[9] = (byte)0x11;

		//Length
		packet[10] = (byte)(array.length >> 8);
		packet[11] = (byte)(array.length);

		for(int i = 0; i < array.length; i++){
			packet[i + 12] = array[i];
		}

		return checksum(packet);
	 }
}