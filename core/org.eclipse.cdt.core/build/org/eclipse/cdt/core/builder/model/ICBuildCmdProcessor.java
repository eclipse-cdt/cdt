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

/**
 * Consumer of build commands.
 * <br>
 * "Processing" a build command might mean different
 * things for different types of processors (ex,
 * an incremental build processor and a makefile
 * generator.)
 * <br>
 * @see ICBuildCmd
 */
public interface ICBuildCmdProcessor {

	/**
	 * Process the provided build commands.  This
	 * might me executing the associated tool, recording
	 * the build command in a file, handing the command
	 * off to a remote processor for execution on another
	 * machine, etc.
	 * 
	 * @param cmds build commands to process.
	 */
	void processCommands(ICBuildCmd[] cmds);

}
