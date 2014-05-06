package main.java.distributed.workload;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.validator.routines.UrlValidator;

import main.java.distributed.configuration.ConfigurationDAO;
import main.java.distributed.configuration.IConfigurationDAO;

public class WorkloadRunner {
	
	/**
	 * Main runnable method for the distributor. It allows an user to submit urls to the server.
	 * @param args websites to be submitted to the server
	 */
	public static void main(String[] args) {
		UrlValidator urlvalidator = new UrlValidator();
		// Add 
		WorkloadDAO workload = new WorkloadDAO();
		URI uri;
		for(String arg : args) {
			try {
				if (!urlvalidator.isValid(arg)) throw new URISyntaxException(arg, "invalid url");
				uri = new URI(arg);
				if(workload.submitWork(uri) >= 0) {
					System.out.println("Added: " + arg);
				} else {
					System.out.println("Url already exists in the database.");
				}
			} catch (URISyntaxException e) {
				System.out.println("Invalid website; skipping " + arg);
			}
		}
		// Process commandline inputs
		Scanner in = new Scanner(System.in);
		IConfigurationDAO config = new ConfigurationDAO();
		while(true) {
			// add url
			System.out.print("> ");
			String url  = in.next();
			if(url.equals("exit")||url.equals("quit")) break;
			try {
				// Check url
				if (!urlvalidator.isValid(url)) throw new URISyntaxException(url, "invalid url");
				uri = new URI(url);
				
				// Add configurations
				System.out.println("Add custom configurations using the format key=value. To continue type: 'submit'");
				System.out.print("+ ");
				String keyValue  = in.next();
				while(!keyValue.equalsIgnoreCase("submit")) {
					String[] keyValueArr = keyValue.split("=", 2);
					if (keyValueArr.length >= 2) {
						config.updateConfiguration(uri.getHost(), keyValueArr[0], keyValueArr[1], uri.getHost().length());
						System.out.println("Config added to section " + url + ": " + Arrays.toString(keyValueArr));
					} else {
						System.out.println("Invalid configuration entry");
					}
					// ask for more
					System.out.print("+ ");
					keyValue  = in.next();
				}
				
				// Submit url
				int id = workload.submitWork(uri);
				if(id >= 0)
					System.out.println("Added: " + url + " (id: " + id + ")");
				else 
					System.out.println("Failed to add " + url + ": URL already exists in database.");
			} catch (URISyntaxException e) {
				System.out.println("Invalid website: " + url);
			}
		}
		in.close();
	}
}
