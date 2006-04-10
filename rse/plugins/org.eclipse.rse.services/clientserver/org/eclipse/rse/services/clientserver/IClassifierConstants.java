/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.clientserver;

/**
 * This interface defines some constants for classifiers.
 */
public interface IClassifierConstants {

	/**
	 * Default type. 
	 */
	public static final String TYPE_DEFAULT = "file";
	
	/**
	 * Link type, "link".
	 */
	public static final String TYPE_LINK = "link";
	
	/**
	 * Java executable type, "executable(java:*)".
	 */
	public static final String TYPE_EXECUTABLE_JAVA = "executable(java:*)";
	
	/**
	 * Binary executable type, "executable(binary)".
	 */
	public static final String TYPE_EXECUTABLE_BINARY = "executable(binary)";
	
	/**
	 * Script executable type, "executable(script)".
	 */
	public static final String TYPE_EXECUTABLE_SCRIPT = "executable(script)";
	
	/**
	 * Match java executable type, "*executable(java:*)*".
	 */
	public static final String MATCH_EXECUTABLE_JAVA = "*executable(java:*)*";
	
	/**
	 * Match binary executable type, "*executable(java:*)*".
	 */
	public static final String MATCH_EXECUTABLE_BINARY = "*executable(binary)*";
	
	/**
	 * Match script executable type, "*executable(java:*)*".
	 */
	public static final String MATCH_EXECUTABLE_SCRIPT = "*executable(script)*";
}