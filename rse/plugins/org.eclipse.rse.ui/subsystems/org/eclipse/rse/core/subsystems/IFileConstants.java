/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core.subsystems;
/**
 * Constants used in the remote file system support
 */
public interface IFileConstants
{
		
    // ---------------------------------------
    // UNIX FILE SYSTEM ATTRIBUTE CONSTANTS...
    // ---------------------------------------   	
    /**
     * Unix-style folder-name separator as a string: "/"
     */
    public static String SEPARATOR_UNIX = "/";
    /**
     * Unix-style folder-name separator as a char: '/'
     */
    public static char SEPARATOR_CHAR_UNIX = '/';
    /**
     * Unix-style path separator as a string: ":"
     */
    public static String PATH_SEPARATOR_UNIX = ":";
    /**
     * Unix-style path separator as a char: ':'
     */
    public static char PATH_SEPARATOR_CHAR_UNIX = ':';
	/**
	 * Unix-style line separator as a byte array: 10
	 */
	public static final byte[] LINE_SEPARATOR_BYTE_ARRAY_UNIX = {10};
    
    
    // ------------------------------------------
    // WINDOWS FILE SYSTEM ATTRIBUTE CONSTANTS...
    // ------------------------------------------   	
    /**
     * Windows-style folder-name separator as a string: "\"
     */
    public static String SEPARATOR_WINDOWS = "\\";
    /**
     * Windows-style folder-name separator as a char: '\'
     */
    public static char SEPARATOR_CHAR_WINDOWS = '\\';
    /**
     * Windows-style path separator as a string: ";"
     */
    public static String PATH_SEPARATOR_WINDOWS = ";";
    /**
     * Windows-style path separator as a char: ';'
     */
    public static char PATH_SEPARATOR_CHAR_WINDOWS = ';';
	/**
	 * Windows-style line separator as a byte array: 13 and 10 respectively
	 */
	public static final byte[] LINE_SEPARATOR_BYTE_ARRAY_WINDOWS = {13, 10};
}