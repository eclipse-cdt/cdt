/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build.gcc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.CConsoleParser;
import org.eclipse.cdt.core.build.CToolChain;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.prefs.Preferences;

/**
 * The GCC toolchain. Placing it in cdt.core for now.
 * 
 * TODO move to it's own plug-in.
 * 
 * @since 5.12
 */
public class GCCToolChain extends CToolChain {

	public GCCToolChain(String id, Preferences settings) {
		super(id, settings);
	}

	public GCCToolChain(String name) {
		super(name);
	}

	@Override
	public String getFamily() {
		return "GCC"; //$NON-NLS-1$
	}

	@Override
	public IFile getResource(IFolder buildFolder, String[] commandLine) {
		for (String arg : commandLine) {
			if (!arg.startsWith("-")) { //$NON-NLS-1$
				// TODO optimize by dealing with multi arg options like -o
				IFile file = buildFolder.getFile(arg);
				if (file.exists() && CoreModel.isTranslationUnit(file)) {
					return file;
				}
			}
		}

		return null;
	}

	@Override
	public ExtendedScannerInfo getScannerInfo(IFolder buildFolder, List<String> cmd) throws CoreException {
		try {
			String[] commandLine = cmd.toArray(new String[cmd.size()]);

			// Change output to stdout
			for (int i = 0; i < commandLine.length - 1; ++i) {
				if (commandLine[i].equals("-o")) { //$NON-NLS-1$
					commandLine[i + 1] = "-"; //$NON-NLS-1$
					break;
				}
			}

			// Change source file to a tmp file (needs to be empty)
			Path tmpFile = null;
			for (int i = 1; i < commandLine.length; ++i) {
				if (!commandLine[i].startsWith("-")) { //$NON-NLS-1$
					// TODO optimize by dealing with multi arg options like -o
					IFile file = buildFolder.getFile(commandLine[i]);
					if (file.exists() && CoreModel.isTranslationUnit(file)) {
						// replace it with a temp file
						Path parentPath = new File(((IFolder) file.getParent()).getLocationURI()).toPath();
						int n = 0;
						while (true) {
							tmpFile = parentPath.resolve(".sc" + n + "." + file.getFileExtension()); //$NON-NLS-1$ //$NON-NLS-2$
							commandLine[i] = tmpFile.toString();
							try {
								Files.createFile(tmpFile);
								break;
							} catch (FileAlreadyExistsException e) {
								// try again
								++n;
							}
						}
						break;
					}
				}
			}

			// Add in the magic potion: -E -P -v -dD
			String[] fullCmd = new String[commandLine.length + 4];
			fullCmd[0] = commandLine[0];
			fullCmd[1] = "-E"; //$NON-NLS-1$
			fullCmd[2] = "-P"; //$NON-NLS-1$
			fullCmd[3] = "-v"; //$NON-NLS-1$
			fullCmd[4] = "-dD"; //$NON-NLS-1$
			System.arraycopy(commandLine, 1, fullCmd, 5, commandLine.length - 1);
			fixPaths(fullCmd);

			File buildDir = new File(buildFolder.getLocationURI());
			Files.createDirectories(buildDir.toPath());

			// Startup the command
			ProcessBuilder processBuilder = new ProcessBuilder(fullCmd).directory(buildDir)
					.redirectErrorStream(true);
			setEnvironment(processBuilder.environment());
			Process process = processBuilder.start();

			// Scan for the scanner info
			Map<String, String> symbols = new HashMap<>();
			List<String> includePath = new ArrayList<>();
			Pattern definePattern = Pattern.compile("#define (.*)\\s(.*)"); //$NON-NLS-1$
			boolean inIncludePaths = false;
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()))) {
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					if (inIncludePaths) {
						if (line.equals("End of search list.")) { //$NON-NLS-1$
							inIncludePaths = false;
						} else {
							includePath.add(line.trim());
						}
					} else if (line.startsWith("#define ")) { //$NON-NLS-1$
						Matcher matcher = definePattern.matcher(line);
						if (matcher.matches()) {
							symbols.put(matcher.group(1), matcher.group(2));
						}
					} else if (line.equals("#include <...> search starts here:")) { //$NON-NLS-1$
						inIncludePaths = true;
					}
				}
			}

			return new ExtendedScannerInfo(symbols, includePath.toArray(new String[includePath.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "scanner info", e)); //$NON-NLS-1$
		}
	}

	@Override
	public CConsoleParser[] getConsoleParsers() {
		// ../src/Test.cpp:4:1: error: 'x' was not declared in this scope

		return new CConsoleParser[] { new CConsoleParser("(.*?):(\\d+):(\\d+:)? (fatal )?error: (.*)") { //$NON-NLS-1$
			@Override
			protected int getSeverity(Matcher matcher) {
				return IMarker.SEVERITY_ERROR;
			}

			@Override
			protected String getMessage(Matcher matcher) {
				return matcher.group(5);
			}

			@Override
			protected int getLineNumber(Matcher matcher) {
				return Integer.parseInt(matcher.group(2));
			}

			@Override
			protected String getFileName(Matcher matcher) {
				return matcher.group(1);
			}

			@Override
			protected int getLinkOffset(Matcher matcher) {
				return 0;
			}

			@Override
			protected int getLinkLength(Matcher matcher) {
				return matcher.group(1).length() + 1 + matcher.group(2).length() + 1
						+ matcher.group(3).length();
			}
		} };
	}

}
