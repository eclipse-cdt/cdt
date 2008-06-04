/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * David Dykstal (IBM) - [233876] filters lost after restart
 *********************************************************************************/
package org.eclipse.rse.internal.core.filters;

/**
 * This is a utility class used for manipulating host-owned filter pool names.
 */
public class HostOwnedFilterPoolPattern {
	
	final private static String prefix = "CN-"; //$NON-NLS-1$
	final private static int start = prefix.length();
	private String suffix;
	
	/**
	 * Create a pattern given a subsystem configuration id
	 * @param configurationId the id from which to construct the pattern.
	 */
	public HostOwnedFilterPoolPattern(String configurationId) {
		suffix = "-" + configurationId; //$NON-NLS-1$
	}
	
	/**
	 * Test a filter pool name to see if it matches this pattern
	 * @param filterPoolName The filter pool name to test.
	 * @return true if the pattern matches this name, false otherwise.
	 */
	public boolean matches(String filterPoolName) {
		return (filterPoolName.startsWith(prefix) && filterPoolName.endsWith(suffix));
	}
	
	/**
	 * Extract the host name from the filter pool name
	 * @param filterPoolName the name of the filter pool
	 * @return the associated host name
	 */
	public String extract(String filterPoolName) {
		String result = null;
		if (matches(filterPoolName)) {
			int length = filterPoolName.length() - (prefix.length() + suffix.length());
			result = filterPoolName.substring(start, start + length);
		}
		return result;
	}
	
	/**
	 * Construct a filter pool name from the host name
	 * @param hostName the host name to use as a base
	 * @return the associated filter pool name
	 */
	public String make(String hostName) {
		return prefix + hostName + suffix;
	}
	
}