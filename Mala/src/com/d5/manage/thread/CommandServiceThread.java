package com.d5.manage.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d5.common.Constants;
import com.d5.manage.IManager;

public class CommandServiceThread extends Thread{
	private Logger log = LoggerFactory.getLogger(getClass());
	private IManager manager;
	
	public CommandServiceThread(IManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void run() {
		try (ServerSocket server = new ServerSocket(Constants.SOCKET_PORT);){
			while(true){
				try {
					log.debug("waitting for connection on port {}.", Constants.SOCKET_PORT);
					Socket client = server.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
					PrintWriter out = new PrintWriter(client.getOutputStream());
					BufferedReader userin = new BufferedReader(new InputStreamReader(System.in));

					new ReceiveTread(server, in, out, userin, client).start();
					new DealCommandThread(out, userin, manager).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
		}
	}
}