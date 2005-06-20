/*******************************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

public interface IManagedCommandLineInfo {
	/**
	 * provide fully qualified command line string for tool invokation
	 * @return command line
	 */
	public String getCommandLine();
	
	/**
	 * give command line pattern 
	 * @return
	 */
	public String getCommandLinePattern();
	
	/**
	 * provide tool name
	 * @return
	 */
	public String getCommandName();
	
	/**
	 * give command flags
	 * @return
	 */
	public String getFlags();
	
	/**
	 * provide list of resources used by tool for transformation
	 * @return
	 */
	public String getInputs();
	
	/**
	 * return output file name
	 * @return
	 */
	public String getOutput();
	
	/**
	 * give command flag to generate output
	 * @return
	 */
	public String getOutputFlag();
	
	/**
	 * return output prefix
	 * @return
	 */
	public String getOutputPrefix();
}
