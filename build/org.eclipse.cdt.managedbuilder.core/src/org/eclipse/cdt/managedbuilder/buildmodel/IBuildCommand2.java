/*******************************************************************************
 * Copyright (c) 2013 Freescale Semiconductors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Serge Beauchamp (Freescale Semiconductor) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.buildmodel;

import org.eclipse.cdt.managedbuilder.core.ITool;


/**
 *
 * This interface represents a command to be invoked for building the step, 
 * and extends the IBuildCommand interface with support for argument files.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @see IBuildCommand
 * @since 8.3
 */
public interface IBuildCommand2 extends IBuildCommand {
	
	/**
	 * An argument file format specifies the syntax of how argument files
	 * (or command files) can be passed to a command line tool to workaround
	 * limitations in the command line length limit.
	 * 
	 * The argument file format is a string including the "${file}" macro, which 
	 * will be replaced automatically by the path of the argument file, and passed
	 * as argument to the command line tool.

	 * @see ITool#getArgumentFileFormat()
	 * 
	 * @return the argument file format, or null if argument files are not supported
	 */
	String getArgumentFileFormat();
	
}
