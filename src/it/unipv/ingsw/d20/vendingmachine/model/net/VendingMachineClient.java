package it.unipv.ingsw.d20.vendingmachine.model.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class VendingMachineClient {

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	
	public VendingMachineClient() throws IOException {
		socket = new Socket("localhost", 8888);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}
	
	public void connectToServer(String info) throws IOException {
		out.println(info);
		socket.close();
	}

	public String firstConnectionToServer(String type) throws IOException {
		out.println(type);
		return in.readLine();
	}

}
