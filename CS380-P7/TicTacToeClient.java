import java.io.*;
import java.net.*;
import java.util.Scanner;

public final class TicTacToeClient{
	public static void main(String[] args) throws Exception{
		try(Socket socket = new Socket("cs380.codebank.xyz",38007)){
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

			Scanner input = new Scanner(System.in);

			//Identify yourself and send ConnectMessage
			System.out.println("\nIdentify yourself, stranger:");
			String username = input.nextLine();
			ConnectMessage name = new ConnectMessage(username);
			oos.writeObject(name);
			System.out.println("\nWelcome to TicTacToeClient, "+username+"\n");

			//Send CommandMessage to start a new game with the server
			CommandMessage command = new CommandMessage(CommandMessage.Command.NEW_GAME);
			oos.writeObject(command);
			System.out.println("Starting new game..");

			//Get server's board response and print board
			Object response = ois.readObject();
			BoardMessage board = (BoardMessage)response;
			printBoard(board.getBoard());
			System.out.println();

			//Start playing
			byte row,col;
			MoveMessage move;

			while(true){
				System.out.print("Enter row (0-2): ");
				row = input.nextByte();
				System.out.print("Enter column (0-2): ");
				col = input.nextByte();
				System.out.println();

				//Send Move Message
				move = new MoveMessage(row,col);
				oos.writeObject(move);

				//Get server's board response and print board
				response = ois.readObject();

				if(response instanceof ErrorMessage){
					System.out.println();
					System.out.println(((ErrorMessage)response).getError());
					System.out.println();
				}
				else if(response instanceof BoardMessage){
					board = (BoardMessage)response;
					printBoard(board.getBoard());
					System.out.println();
				}

				if(board.getStatus() == BoardMessage.Status.IN_PROGRESS){
					continue;
				}
				else{
					System.out.println("Outcome: " + board.getStatus());
					System.out.println("\nGoodbye");
					socket.close();
					break;
				}
			}
		}
	}

	//print le board
	private static void printBoard(byte[][] board){
		System.out.println("\nBoard: You are X\n");
		for(int i = 0; i < board.length; i++){
			System.out.print("   ");
			for(int j = 0; j < board[i].length; j++){
				if(board[i][j] == ((byte)0)){
					System.out.print("- ");
				}
				else if(board[i][j] == ((byte)1)){
					System.out.print("X ");
				}
				else if(board[i][j] == ((byte)2)){
					System.out.print("O ");
				}
			}
			System.out.println();
		}
	}
}