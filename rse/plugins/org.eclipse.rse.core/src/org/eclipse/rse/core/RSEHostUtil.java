package org.eclipse.rse.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class is a collection of utility methods that can be used to 
 * deal with IP addresses and hosts.
 * 
 * This class should not be subclassed.
 */
public class RSEHostUtil {
	/**
	 * Returns a qualified hostname given a potentially unqualified hostname
	 */
	public static String getQualifiedHostName(String hostName) {
		try {
			InetAddress address = InetAddress.getByName(hostName);
			return address.getCanonicalHostName();
		} catch (UnknownHostException exc) {
			return hostName;
		}
	}
}
