package main.java.distributed.workload;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Scanner;

import main.java.distributed.configuration.ConfigurationDAO;
import main.java.distributed.configuration.IConfigurationDAO;

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
			IConfigurationDAO config = new ConfigurationDAO();
			while(true) {
				// add url
				System.out.print("> ");
				String url  = in.next();
				if(url.equals("exit")||url.equals("quit")) break;
				if(!urlValidator.isValid(url)) {
					System.out.println("Rejected invalid url: " + url);
					continue;
				}
				
				// Add configurations
				System.out.println("Add custom configurations using the format key=value. To continue: type 'submit'.");
				System.out.print("+ ");
				URI uri = new URI(url);
				String keyValue  = in.next();
				while(!keyValue.equalsIgnoreCase("submit")) {
					String[] keyValueArr = keyValue.split("=", 2);
					config.updateConfiguration(uri.getHost(), keyValueArr[0], keyValueArr[1], uri.getHost().length());
					System.out.println("Config added to section " + url + ": " + Arrays.toString(keyValueArr));
					// ask for more
					System.out.print("+ ");
					keyValue  = in.next();
				}
				
				if(workload.submitWork(url))
					System.out.println("Added: " + url);
			}
			in.close();
		} catch (IOException e) {
			System.out.print("Error: Unable to find connection settings.");
		} catch (URISyntaxException e) {
			System.out.print("URL checking went wrong D:");
		}
		System.out.print("Done.");
	}
}
