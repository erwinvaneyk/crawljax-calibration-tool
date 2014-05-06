package main.java.distributed.workload;

import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.validator.routines.UrlValidator;

public class WorkloadRunner {
	
	/**
	 * Main runnable method for the distributor. It allows an user to submit urls to the server.
	 * @param args websites to be submitted to the server
	 */
	public static void main(String[] args) {
		try {
			// Deal with args
			WorkloadDAO workload = new WorkloadDAO();
			UrlValidator urlValidator = new UrlValidator();
			for(String arg : args) {
				if(urlValidator.isValid(arg)) {
					if(workload.submitWork(arg)) {
						System.out.println("Added: " + arg);
					} else {
						System.out.println("Url already exists in the database.");
					}
				} else {
					System.out.println("Rejected invalid url: " + arg);
				}
			}
			// Process commandline inputs
			Scanner in = new Scanner(System.in);
			while(true) {
				System.out.print("> ");
				String url  = in.next();
				if(url.equals("exit")||url.equals("quit")) break;
				if(urlValidator.isValid(url)) {
					if(workload.submitWork(url))
						System.out.println("Added: " + url);
				} else {
					System.out.println("Rejected invalid url: " + url);
				}
			}
			in.close();
		} catch (IOException e) {
			System.out.print("Error: Unable to find connection settings.");
		}
		System.out.print("Done.");
	}
}
