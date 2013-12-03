/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.application;

/**
 * This class provides information required to start 
 * debugging an executable. 
 */
public class NewExecutableInfo {
	private String fHostPath;
	private String fTargetPath;
	private String fArguments;

	public NewExecutableInfo(String hostPath, String targetPath, String args) {
		super();
		fHostPath = hostPath;
		fTargetPath = targetPath;
		fArguments = args;
	}
	
	/**
	 * Returns the path of the executable on the host
	 */
	public String getHostPath() {
		return fHostPath;
	}
	
	/**
	 * Sets the path of the executable on the host
	 */
	public void setHostPath(String path) {
		fHostPath = path;
	}
	
	/**
	 * For remote sessions returns the path of the executable 
	 * on the target. Otherwise returns null.
	 */
	public String getTargetPath() {
		return fTargetPath;
	}

	/**
	 * Sets the path of the executable on the target for remote executables
	 */
	public void setTargetPath(String path) {
		fTargetPath = path;
	}
	
	/**
	 * Returns the arguments to pass to the executable, or null
	 */
	public String getArguments() {
		return fArguments;
	}	
	
	/**
	 * Sets the arguments to pass to the executable
	 */
	public void setArguments(String args) {
		fArguments = args;
	}

}