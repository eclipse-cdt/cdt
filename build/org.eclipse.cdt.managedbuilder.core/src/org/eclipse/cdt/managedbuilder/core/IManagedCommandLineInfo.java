/*******************************************************************************
 * Copyright (c) 2004, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IManagedCommandLineInfo {
	/**
	 * provide fully qualified command line string for tool invocation
	 * @return command line
	 */
	public String getCommandLine();
	
	/**
	 * give command line pattern 
	 */
	public String getCommandLinePattern();
	
	/**
	 * provide tool name
	 */
	public String getCommandName();
	
	/**
	 * give command flags
	 */
	public String getFlags();
	
	/**
	 * provide list of resources used by tool for transformation
	 */
	public String getInputs();
	
	/**
	 * return output file name
	 */
	public String getOutput();
	
	/**
	 * give command flag to generate output
	 */
	public String getOutputFlag();
	
	/**
	 * return output prefix
	 */
	public String getOutputPrefix();
}
