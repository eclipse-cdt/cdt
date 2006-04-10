/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.files.core.subsystems;

/**
 * This exception class should be used to return exception codes to
 * clients. Codes can be either integers or strings, or both may be
 * used at the same time for better subclassing of similar errors that
 * occur due to slightly different reasons.
 */

public class RemoteFileCodeException extends Exception {


	
	private int code;
	private String codeString;
	

	/**
	 * Constructor for RemoteFileCodeException
	 */
	public RemoteFileCodeException() {
		super();
	}
	
	
	/**
	 * Constructor for RemoteFileCodeException
	 */
	public RemoteFileCodeException(int code) {
		this.code = code;
	}
	
	
	/**
	 * Constructor for RemoteFileCodeException
	 */
	public RemoteFileCodeException(String codeString) {
		this.codeString = codeString;
	}
	
	
	/**
	 * Constructor for RemoteFileCodeException
	 */
	public RemoteFileCodeException(int code, String codeString) {
		this.code = code;
		this.codeString = codeString;
	}
	
	
	/**
	 * Set the error code
	 * @param the error code
	 */
	public void setErrorCode(int code) {
		this.code = code;
	}
	
	
	/**
	 * Get the error code
	 * @return the error code that was set
	 */
	public int getErrorCode() {
		return code;
	}
	
	
	/**
	 * Set the error code string
	 */
	public void setErrorCodeString(String codeString) {
		this.codeString = codeString;
	}
	
	
	/**
	 * Get the error code string
	 */
	public String getErrorCodeString() {
		return codeString;
	}
}