package main.java.distributed.workload;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.validator.routines.UrlValidator;

import main.java.distributed.ConnectionManager;
import main.java.distributed.IConnectionManager;
import main.java.distributed.configuration.ConfigurationDAO;
import main.java.distributed.configuration.IConfigurationDAO;

public class WorkloadRunner {
	
	/**
	 * Main runnable method for the distributor. It allows an user to submit urls to the server.
	 * @param args websites to be submitted to the server
	 */
	public static void main(String[] args) {
		UrlValidator urlvalidator = new UrlValidator();
		IConnectionManager conn = new ConnectionManager();
		IConfigurationDAO config = new ConfigurationDAO(conn);
		IWorkloadDAO workload = new WorkloadDAO(conn);
		URL url;
		for(String arg : args) {
			try {
				if (!urlvalidator.isValid(arg)) throw new MalformedURLException("invalid url");
				url = new URL(arg);
				if(workload.submitWork(url) >= 0) {
					System.out.println("Added: " + arg);
				} else {
					System.out.println("Url already exists in the database.");
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Process commandline inputs
		Scanner in = new Scanner(System.in);
		while(true) {
			// add url
			System.out.print("> ");
			String rawurl  = in.next();
			if(rawurl.equals("exit")||rawurl.equals("quit")) break;
			try {
				// Check url
				if (!urlvalidator.isValid(rawurl)) throw new MalformedURLException("invalid url");
				url = new URL(rawurl);
				
				// Add configurations
				System.out.println("Add custom configurations using the format key=value. To continue type: 'submit'");
				System.out.print("+ ");
				String keyValue  = in.next();
				while(!keyValue.equalsIgnoreCase("submit")) {
					String[] keyValueArr = keyValue.split("=", 2);
					if (keyValueArr.length >= 2) {
						config.updateConfiguration(url.getHost(), keyValueArr[0], keyValueArr[1], url.getHost().length());
						System.out.println("Config added to section " + url + ": " + Arrays.toString(keyValueArr));
					} else {
						System.out.println("Invalid configuration entry");
					}
					// ask for more
					System.out.print("+ ");
					keyValue  = in.next();
				}
				
				// Submit url
				int id = workload.submitWork(url);
				if(id >= 0)
					System.out.println("Added: " + url + " (id: " + id + ")");
				else 
					System.out.println("Failed to add " + url + ": URL already exists in database.");
			} catch (MalformedURLException e) {
				System.out.println(e.getMessage());
			} 
		}
		in.close();
	}
}
