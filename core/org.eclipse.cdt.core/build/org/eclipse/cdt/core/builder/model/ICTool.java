/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model;

import java.io.ByteArrayOutputStream;

import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.core.runtime.IPath;

/**
 * ICTool represents an instance of a tool.
 * <p>
 * Tools represent a particular executable (ex, "gcc", etc.)
 * that can be run in order to produce some output.  The
 * exec() method provides a shorthand that allows a caller
 * to execute the tool and gather the resultant output
 * streams.
 * <p>
 * Toolchain providers use this interface to represent
 * individual tools within a toolchain.
 * <p>
 * Stand-alone tool providers (flex, yacc, rpcgen, etc.) make
 * use of this to define generic build tools that can be "mixed
 * in" to any toolchain.
 * <p>
 * See also the <a href="../../../../../../CTool.html">CTool</a>
 * extension point documentation.
 */
public interface ICTool {

	/**
	 * Convenince class that just contains a reference to
	 * two byte array output streams named sterr and stdout.
	 */
	class IOResults {
		public ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		public ByteArrayOutputStream stdout = new ByteArrayOutputStream();
	}

	/**
	 * Returns the type ID for this tool.  This type ID corresponds
	 * to a CToolType extension ID
	 * 
	 * @return the type ID for this tool.
	 */
	String getTypeId();

	/**
	 * Returns a unique identifuer for this tool instance.
	 * 
	 * @return the type ID for this tool.
	 */
	String getId();

	/**
	 * Returns the explicit path to the executable associated
	 * with this tool instance..
	 * 
	 * @return path to executable.
	 */
	IPath getPath();

	/**
	 * Indicates whether or not the executable referenced by this
	 * tool instance actually exists.
	 * 
	 * @return true if the associated tool executable exists.
	 */
	boolean exists();

	/**
	 * Run the executable referenced by this tool, using the
	 * supplied parameters.
	 * 
	 * @param parameters parameters to pass to tool when executing.
	 * @param workingDir working directory for tool execution.
	 */
	IOResults exec(String[] parameters, String workingDir);

	/**
	 * Get an instance of an error parser that is capable
	 * of dealing with the tool's output.
	 * 
	 * @return error parser for the tool.
	 */
	IErrorParser getErrorParser();
}
