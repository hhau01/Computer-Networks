/*
** Henry Au
** CS380 - Project 6
** Due: Wednesday, February 24th, 2016 before midnight (60 points)
*/

import java.io.*;
import java.net.Socket;
import java.util.Random;

public final class TcpClient{
	public static void main(String[] args) throws Exception{
		try(Socket socket = new Socket("cs380.codebank.xyz", 38006)){
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			//Randomized sequence number & SYN flag 0x02 set
			Random random = new Random();
			int sequence = random.nextInt();
			byte flag = 0x02;

			//Send Ipv4 w/ TCP header
			os.write(createIpv4(createTcp(0,sequence,0,flag)));

			//Get 0xCAFEBABE response
			byte[] response = new byte[4];
			is.read(response);
			System.out.print("0x");	
			for(int i = 0; i < response.length; i++){
				System.out.printf("%02X", response[i]);
			}
			System.out.println();

			//Get TCP header w/ the SYN and ACK flags set  
			//and a randomized sequence number
			byte[] tcpheader = new byte[20];
			is.read(tcpheader);

			//Get acknowledgement
			int acknowledgement = 0;
			acknowledgement |= (tcpheader[4] & 0x000000FF);
			acknowledgement <<= 8;
			acknowledgement |= (tcpheader[5] & 0x000000FF);
			acknowledgement <<= 8;
			acknowledgement |= (tcpheader[6] & 0x000000FF);
			acknowledgement <<= 8;
			acknowledgement |= (tcpheader[7] & 0x000000FF);
			
			//Send Ipv4 w/ TCP header w/ seq+1, ACK flag 0x10 set, and ack+1
			flag = 0x10;
			os.write(createIpv4(createTcp(0,sequence+1,acknowledgement+1,flag)));

			//Get 0xCAFEBABE response
			is.read(response);
			System.out.print("0x");	
			for(int i = 0; i < response.length; i++){
				System.out.printf("%02X", response[i]);
			}
			System.out.println();

			//Send all 12 packets with proper sequence numbers. TCP gives each byte of data
			//its own sequence number. Increment sequence number used in ACK packet
			System.out.println("Authentication complete.\nSending all 12 data packets...");
			sequence += 1;
			flag = 0;
			for(int j = 1; j < 13; j++){
				int plength = (int)Math.pow(2,j);
				byte[] tcpPacket = createIpv4(createTcp(plength,sequence+plength-1,acknowledgement,flag));

				os.write(tcpPacket);

				//Get 0xCAFEBABE response
				is.read(response);
				System.out.print("0x");	
				for(int i = 0; i < response.length; i++){
					System.out.printf("%02X", response[i]);
				}
				System.out.println();
			}

			//Begin connection teardown by sending a packet with FIN flag 0x01 set
			System.out.println("Packets sent.\nBegin connection teardown...");
			sequence += (24 -1);
			flag = 0x01;
			os.write(createIpv4(createTcp(0,sequence,acknowledgement,flag)));

			//Get 0xCAFEBABE response
			is.read(response);
			System.out.print("0x");	
			for(int i = 0; i < response.length; i++){
				System.out.printf("%02X", response[i]);
			}
			System.out.println();

			//Server will respond with TCP header with ACK flag set to acknowledge closing
			//first half of the connection
			is.read(tcpheader);
			//Server will respond with TCP header with FIN flag set to begin closing second
			//half of the connection
			is.read(tcpheader);

			//Respond with ACK flag 0x10 set to confirm closing second half of connection.
			flag = 0x10;
			os.write(createIpv4(createTcp(0,sequence+1,acknowledgement,flag)));

			//Get 0xCAFEBABE response
			is.read(response);
			System.out.print("0x");	
			for(int i = 0; i < response.length; i++){
				System.out.printf("%02X", response[i]);
			}
			System.out.println();

			System.out.println("Connection teardown complete.");

			socket.close();
			is.close();
			os.close();
		}
	}

	// code 		reason
	// 0xcafebabe	correct data packet
	// 0xbaadf00d	problem with ipv4
	// 0xcafed00d	unexpected flags set in tcp packet
	// 0xdeadc0de	invalid tcp checksum
	// 0xbbadbeef	incorrect tcp data length
	// 0xfeedface	incorrect sequence number
	// 0xfee1dead	bad data offset
	// 0xbaaaaaad	incorrect acknowledgement in three-way handshake

	static byte[] createIpv4(byte[] data){
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
		packet[9] = (byte)0x06; //TCP Protocol
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

	 static byte[] createTcp(int size, int seq, int ack, byte flags){
	 	int length = size + 20;
	 	byte[] packet = new byte[length];

	 	//Source port - anything
	 	packet[0] = (byte)0x80;
	 	packet[1] = (byte)0x08;

	 	//Destination port - anything
	 	packet[2] = (byte)0x80;
	 	packet[3] = (byte)0x08;

	 	//Sequence number - 32bit int
	 	packet[4] =  (byte)(seq >> 24);
	 	packet[5] = (byte)(seq >> 16);
	 	packet[6] = (byte)(seq >> 8);
	 	packet[7] = (byte)(seq);

	 	//Acknowledgement
	 	packet[8] = (byte)(ack >> 24);
	 	packet[9] = (byte)(ack >> 16);
	 	packet[10] = (byte)(ack >> 8);
	 	packet[11] = (byte)(ack);

	 	//Data offset TCP header min 5
	 	packet[12] = (byte)(5 << 4);

	 	//Flags
	 	packet[13] = flags;

	 	//Window size - no window
	 	packet[14] = 0;
	 	packet[15] = 0;

	 	//TCP Checksum - 0 for now
	 	packet[16] = 0;
	 	packet[17] = 0;

	 	//Urgent pointer
	 	packet[18] = 0;
	 	packet[19] = 0;

	 	//Checksum
	 	int sum = tcpChecksum(packet);
	 	packet[16] = (byte)(sum >> 8);
	 	packet[17] = (byte)(sum);

	 	return packet;
	 }

	 static int tcpChecksum(byte[] array){
	 	int length = array.length + 12;
	 	byte[] packet = new byte[length];

		for(int i = 0; i < array.length; i++){
			packet[i] = array[i];
		}	 	

		//Source addr
	 	packet[array.length] = (byte)0x55;
		packet[array.length+1] = (byte)0x37;
		packet[array.length+2] = (byte)0x80;
		packet[array.length+3] = (byte)0x08;

		//Destination addr
		packet[array.length+4] = (byte)0x34;
		packet[array.length+5] = (byte)0x21;
		packet[array.length+6] = (byte)0x83;
		packet[array.length+7] = (byte)0x10;

		//Zeroes
		packet[array.length+8] = 0;

		//Protocol - TCP
		packet[array.length+9] = (byte)0x06;

		//Length
		packet[array.length+10] = (byte)(array.length >> 8);
		packet[array.length+11] = (byte)(array.length);

		return checksum(packet);
	 }
}