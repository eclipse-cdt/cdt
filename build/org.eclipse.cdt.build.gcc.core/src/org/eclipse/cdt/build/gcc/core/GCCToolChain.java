/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.gcc.core.internal.Activator;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;

/**
 * The GCC toolchain. This is the base class for all GCC toolchains. It represents GCC as found on
 * the user's PATH. It can be overriden to change environment variable settings.
 */
public class GCCToolChain extends PlatformObject implements IToolChain {

	private final IToolChainProvider provider;
	private final String id;
	private final String version;
	private final String name;
	private final Path path;
	private final String prefix;
	private final IEnvironmentVariable pathVar;
	private final IEnvironmentVariable[] envVars;

	protected String[] compileCommands;

	public GCCToolChain(IToolChainProvider provider, String id, String version) {
		this(provider, id, version, null, null);
	}

	public GCCToolChain(IToolChainProvider provider, String id, String version, Path path) {
		this(provider, id, version, path, null);
	}

	public GCCToolChain(IToolChainProvider provider, String id, String version, Path path, String prefix) {
		this.provider = provider;
		this.id = id;
		this.version = version;
		this.name = id + " - " + version; //$NON-NLS-1$
		this.path = path;
		this.prefix = prefix;

		if (path != null) {
			pathVar = new EnvironmentVariable("PATH", path.toString(), IEnvironmentVariable.ENVVAR_PREPEND, //$NON-NLS-1$
					File.pathSeparator);
			envVars = new IEnvironmentVariable[] { pathVar };
		} else {
			pathVar = null;
			envVars = new IEnvironmentVariable[0];
		}
	}

	@Override
	public IToolChainProvider getProvider() {
		return provider;
	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public String getVersion() {
		return version;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getProperty(String key) {
		// this class represents a local toolchain
		switch (key) {
		case ATTR_OS:
			return Platform.getOS();
		case ATTR_ARCH:
			return Platform.getOSArch();
		}
		return null;
	}
	
	@Override
	public String getBinaryParserId() {
		// Assume local builds
		// TODO be smarter and use the id which should be the target
		switch (Platform.getOS()) {
		case Platform.OS_WIN32:
			return CCorePlugin.PLUGIN_ID + ".PE"; //$NON-NLS-1$
		case Platform.OS_MACOSX:
			return CCorePlugin.PLUGIN_ID + ".MachO64"; //$NON-NLS-1$
		default:
			return CCorePlugin.PLUGIN_ID + ".ELF"; //$NON-NLS-1$
		}
	}

	protected void addDiscoveryOptions(List<String> command) {
		command.add("-E"); //$NON-NLS-1$
		command.add("-P"); //$NON-NLS-1$
		command.add("-v"); //$NON-NLS-1$
		command.add("-dD"); //$NON-NLS-1$
	}

	@Override
	public IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, Path command, String[] args,
			IExtendedScannerInfo baseScannerInfo, IResource resource, URI buildDirectoryURI) {
		try {
			Path buildDirectory = Paths.get(buildDirectoryURI);

			List<String> commandLine = new ArrayList<>();
			if (command.isAbsolute()) {
				commandLine.add(command.toString());
			} else {
				commandLine.add(getCommandPath(command).toString());
			}

			if (baseScannerInfo != null && baseScannerInfo.getIncludePaths() != null) {
				for (String includePath : baseScannerInfo.getIncludePaths()) {
					commandLine.add("-I" + includePath); //$NON-NLS-1$
				}
			}

			addDiscoveryOptions(commandLine);
			commandLine.addAll(Arrays.asList(args));

			// Change output to stdout
			boolean haveOut = false;
			for (int i = 0; i < commandLine.size() - 1; ++i) {
				if (commandLine.get(i).equals("-o")) { //$NON-NLS-1$
					commandLine.set(i + 1, "-"); //$NON-NLS-1$
					haveOut = true;
					break;
				}
			}
			if (!haveOut) {
				commandLine.add("-o"); //$NON-NLS-1$
				commandLine.add("-"); //$NON-NLS-1$
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
							// Not sure if this is a reasonable choice when
							// there's
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
				IPath parentPath = resource instanceof IFile ? resource.getParent().getLocation()
						: resource.getLocation();
				tmpFile = Files.createTempFile(parentPath.toFile().toPath(), ".sc", ".cpp"); //$NON-NLS-1$ //$NON-NLS-2$
				commandLine.add(tmpFile.toString());
			}

			Files.createDirectories(buildDirectory);

			// Startup the command
			ProcessBuilder processBuilder = new ProcessBuilder(commandLine).directory(buildDirectory.toFile())
					.redirectErrorStream(true);
			CCorePlugin.getDefault().getBuildEnvironmentManager().setEnvironment(processBuilder.environment(),
					buildConfig, true);
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
		} catch (IOException e) {
			Activator.log(e);
			return null;
		}
	}

	@Override
	public String[] getErrorParserIds() {
		return new String[] { "org.eclipse.cdt.core.GCCErrorParser", //$NON-NLS-1$
				"org.eclipse.cdt.core.GASErrorParser", //$NON-NLS-1$
				"org.eclipse.cdt.core.GLDErrorParser", //$NON-NLS-1$
				"org.eclipse.cdt.core.GmakeErrorParser", //$NON-NLS-1$
				"org.eclipse.cdt.core.CWDLocator" //$NON-NLS-1$
		};
	}

	@Override
	public IEnvironmentVariable getVariable(String name) {
		if (path != null && name.equals("PATH")) { //$NON-NLS-1$
			return pathVar;
		}
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables() {
		return envVars;
	}

	@Override
	public Path getCommandPath(Path command) {
		if (command.isAbsolute()) {
			return command;
		}

		if (path != null) {
			return path.resolve(command);
		}

		// Look for it in the path environment var
		String path = System.getenv("PATH"); //$NON-NLS-1$
		for (String entry : path.split(File.pathSeparator)) {
			Path entryPath = Paths.get(entry);
			Path cmdPath = entryPath.resolve(command);
			if (Files.isExecutable(cmdPath)) {
				return cmdPath;
			}
		}

		return null;
	}

	@Override
	public String[] getCompileCommands() {
		if (compileCommands == null) {
			List<String> cmds = new ArrayList<>();
			for (String cmd : new String[] { "gcc", "g++", "clang", "clang++" }) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				cmd = prefix != null ? cmd : prefix + cmd;
				Path cmdPath = getCommandPath(Paths.get(cmd));
				if (cmdPath != null) {
					cmds.add(cmd);
				}
			}
			compileCommands = cmds.toArray(new String[compileCommands.length]);
		}
		return compileCommands;
	}

	@Override
	public IResource[] getResourcesFromCommand(String[] cmd, URI buildDirectoryURI) {
		// Start at the back looking for arguments
		List<IResource> resources = new ArrayList<>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = cmd.length - 1; i >= 0; --i) {
			String arg = cmd[i];
			if (arg.startsWith("-")) { //$NON-NLS-1$
				// ran into an option, we're done.
				break;
			}
			for (IFile resource : root.findFilesForLocationURI(buildDirectoryURI.resolve(arg))) {
				resources.add(resource);
			}
		}

		return resources.toArray(new IResource[resources.size()]);
	}

}
