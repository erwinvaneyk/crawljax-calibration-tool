package main.java.distributed;

import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.validator.routines.UrlValidator;

public class WorkloadRunner {
	
	public static void main(String[] args) {
		try {
			WorkloadDistributor workload = new WorkloadDistributor();
			UrlValidator urlValidator = new UrlValidator();
			for(String arg : args) {
				if(urlValidator.isValid(arg)) {
					if(workload.submitWork(arg))
						System.out.println("Added: " + arg);
				} else {
					System.out.println("Rejected invalid url: " + arg);
				}
			}
			// commandline
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
		} catch (IOException e) {
			System.out.print("Error: Unable to find connection settings.");
		}
		System.out.print("Done.");
	}
}
