/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.core.CConsoleParser;
import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.build.core.IToolChainType;
import org.eclipse.cdt.build.gcc.core.internal.Activator;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.osgi.service.prefs.Preferences;

/**
 * The GCC toolchain. Placing it in cdt.core for now.
 * 
 * TODO move to it's own plug-in.
 * 
 * @since 5.12
 */
public class GCCToolChain implements IToolChain {

	private final IToolChainType type;
	private final String name;
	private String command;
	private String version;
	private String target;

	public GCCToolChain(IToolChainType type, Path path, String command) {
		this.type = type;
		getVersion(path.resolve(command).toString());
		this.name = command + '-' + version;
		this.command = command;
	}

	protected GCCToolChain(IToolChainType type, String name) {
		this.type = type;
		this.name = name;
		// TODO need to pull the other info out of preferences
	}

	@Override
	public IToolChainType getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getCommand() {
		return command;
	}

	private static Pattern versionPattern = Pattern.compile(".*(gcc|LLVM) version .*"); //$NON-NLS-1$
	private static Pattern targetPattern = Pattern.compile("Target: (.*)"); //$NON-NLS-1$

	private void getVersion(String command) {
		try {
			Process proc = new ProcessBuilder(new String[] { command, "-v" }).redirectErrorStream(true) //$NON-NLS-1$
					.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					Matcher versionMatcher = versionPattern.matcher(line);
					if (versionMatcher.matches()) {
						version = line.trim();
						continue;
					}
					Matcher targetMatcher = targetPattern.matcher(line);
					if (targetMatcher.matches()) {
						target = targetMatcher.group(1);
						continue;
					}
				}
			}
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	@Override
	public void setEnvironment(Map<String, String> env) {
		// TODO Auto-generated method stub
		// The base one could just assume the toolchain is already set up in the
		// user's env
	}

	protected void addDiscoveryOptions(List<String> command) {
		command.add("-E"); //$NON-NLS-1$
		command.add("-P"); //$NON-NLS-1$
		command.add("-v"); //$NON-NLS-1$
		command.add("-dD"); //$NON-NLS-1$
	}

	@Override
	public IExtendedScannerInfo getScannerInfo(String command, List<String> args, List<String> includePaths,
			IResource resource, Path buildDirectory) throws IOException {
		List<String> commandLine = new ArrayList<>();
		commandLine.add(command);

		for (String includePath : includePaths) {
			commandLine.add("-I" + includePath); //$NON-NLS-1$
		}

		addDiscoveryOptions(commandLine);
		commandLine.addAll(args);

		// Change output to stdout
		for (int i = 0; i < commandLine.size() - 1; ++i) {
			if (commandLine.get(i).equals("-o")) { //$NON-NLS-1$
				commandLine.set(i + 1, "-"); //$NON-NLS-1$
				break;
			}
		}

		// Change source file to a tmp file (needs to be empty)
		Path tmpFile = null;
		for (int i = 1; i < commandLine.size(); ++i) {
			if (!commandLine.get(i).startsWith("-")) { //$NON-NLS-1$
				// TODO optimize by dealing with multi arg options like -o
				Path filePath = buildDirectory.resolve(commandLine.get(i));
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(filePath.toUri());
				if (files.length > 0) {
					// replace it with a temp file
					Path parentPath = filePath.getParent();
					String extension = files[0].getFileExtension();
					if (extension == null) {
						// Not sure if this is a reasonable choice when there's
						// no extension
						extension = ".cpp"; //$NON-NLS-1$
					} else {
						extension = '.' + extension;
					}
					tmpFile = Files.createTempFile(parentPath, ".sc", extension); //$NON-NLS-1$
					commandLine.set(i, tmpFile.toString());
				}
			}
		}
		if (tmpFile == null) {
			// Have to assume there wasn't a source file. Add one in the
			// resource's container
			IPath parentPath = resource instanceof IFile ? resource.getParent().getLocation() : resource.getLocation();
			tmpFile = Files.createTempFile(parentPath.toFile().toPath(), ".sc", ".cpp"); //$NON-NLS-1$ //$NON-NLS-2$
			commandLine.add(tmpFile.toString());
		}

		Files.createDirectories(buildDirectory);

		// Startup the command
		ProcessBuilder processBuilder = new ProcessBuilder(commandLine).directory(buildDirectory.toFile())
				.redirectErrorStream(true);
		setEnvironment(processBuilder.environment());
		Process process = processBuilder.start();

		// Scan for the scanner info
		Map<String, String> symbols = new HashMap<>();
		List<String> includePath = new ArrayList<>();
		Pattern definePattern = Pattern.compile("#define (.*)\\s(.*)"); //$NON-NLS-1$
		boolean inIncludePaths = false;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			Activator.log(e);
		}
		Files.delete(tmpFile);

		return new ExtendedScannerInfo(symbols, includePath.toArray(new String[includePath.size()]));
	}

	@Override
	public Collection<CConsoleParser> getConsoleParsers() {
		// ../src/Test.cpp:4:1: error: 'x' was not declared in this scope
		return Arrays.asList(new CConsoleParser("(.*?):(\\d+):(\\d+:)? (fatal )?error: (.*)") { //$NON-NLS-1$
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
				return matcher.group(1).length() + 1 + matcher.group(2).length() + 1 + matcher.group(3).length();
			}
		});
	}

	@Override
	public boolean supports(ILaunchTarget target) {
		if (target.getTypeId().equals(ILaunchTargetManager.localLaunchTargetTypeId)) {
			switch (Platform.getOS()) {
			case Platform.OS_MACOSX:
				return this.target.contains("apple-darwin"); //$NON-NLS-1$
			}
			return true;
		}
		return false;
	}

	@Override
	public void save(Preferences properties) {
		// TODO Auto-generated method stub

	}

}
