/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.osgi.service.prefs.Preferences;

/**
 * Root class for CDT toolchains.
 *
 * @since 5.12
 */
public abstract class CToolChain extends PlatformObject {

	public static final String FAMILY = "family"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$

	private String id;
	private String name;

	protected CToolChain(String id, Preferences settings) {
		this.id = id;
		this.name = settings.get(NAME, "<Unknown>"); //$NON-NLS-1$
	}

	protected CToolChain(String name) {
		this.name = name;
	}

	public abstract String getFamily();

	public String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void save(Preferences settings) {
		settings.put(FAMILY, getFamily());
		settings.put(NAME, name);
	}

	public static String[] splitCommand(String command) {
		// TODO deal with quotes properly, for now just strip
		return command.replace("\"", "").split("\\s+"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public static String[] fixPaths(String[] command) {
		for (int i = 0; i < command.length; ++i) {
			if (command[i].indexOf('\\') >= 0) {
				command[i] = command[i].replace('\\', '/');
			}
		}
		return command;
	}

	/**
	 * Update the given environment to run the toolchain.
	 * 
	 * @param env
	 */
	public void setEnvironment(Map<String, String> env) {
		// default, nothing
	}

	/**
	 * Find the file mentioned in the command line.
	 * 
	 * @param buildFolder
	 * @param commandLine
	 * @return the file in the command line or null if can't be found.
	 */
	public IFile getResource(IFolder buildFolder, String[] commandLine) {
		// default, not found
		return null;
	}

	public IFile getResource(IFolder buildFolder, String commandLine) {
		return getResource(buildFolder, splitCommand(commandLine));
	}

	/**
	 * Calculate the scanner info from the given command line
	 * 
	 * @param buildFolder
	 * @param commandLine
	 * @return scanner info, or null if can't be calculated
	 * @throws CoreException
	 */
	public ExtendedScannerInfo getScannerInfo(IFolder buildFolder, List<String> commandLine)
			throws CoreException {
		// default, null
		return null;
	}

	/**
	 * Return the console parsers to be used when this toolchain is being used
	 * for a build.
	 * 
	 * @return console parsers, or null if there aren't any
	 */
	public CConsoleParser[] getConsoleParsers() {
		return null;
	}

}
