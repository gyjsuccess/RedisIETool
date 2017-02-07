package com.d5.manage.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.d5.manage.IManager;

public class DealCommandThread extends Thread {

	private PrintWriter out;
	private BufferedReader userin;
	private IManager manager;

	public DealCommandThread(PrintWriter out, BufferedReader userin, IManager manager) {
		this.out = out;
		this.userin = userin;
		this.manager = manager;
	}

	@Override
	public void run() {
		try {
			while (true) {
				String command = userin.readLine();
				StringBuffer sBuffer = new StringBuffer();
				
				execute(command, sBuffer, manager);
				
				out.println(sBuffer.toString());
				out.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(out != null){
				out.close();
			}
			if(userin != null){
				try {
					userin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void execute(String command, StringBuffer sBuffer, IManager manager) {
		// TODO Auto-generated method stub
		
	}

}