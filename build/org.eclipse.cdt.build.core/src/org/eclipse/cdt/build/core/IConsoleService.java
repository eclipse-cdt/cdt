/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This may be temporary. It's uses the TextConsole's parsing and hyperlink
 * framework to parse build output for errors.
 * 
 * TODO Should we replace all CDT build consoles with this.
 * 
 * @since 5.12
 */
public interface IConsoleService {

	/**
	 * Display the stdout and stderr of the process in the console. Use the
	 * console parsers to parse that output to mark errors and warnings and
	 * such. The build directory helps to find resources for markers.
	 * 
	 * @param process
	 * @param consoleParsers
	 * @param buildDirectory
	 * @throws IOException
	 */
	void monitor(Process process, CConsoleParser[] consoleParsers, Path buildDirectory) throws IOException;

	/**
	 * Write a message on the console stdout.
	 * 
	 * @param msg
	 * @throws IOException
	 */
	void writeOutput(String msg) throws IOException;

	/**
	 * Write a message on the console stderr.
	 * 
	 * @param msg
	 * @throws IOException
	 */
	void writeError(String msg) throws IOException;

}
