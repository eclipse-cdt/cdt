/*******************************************************************************
 * Copyright (c) 2015, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - modified for use in Container build
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher.ui.launchbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.build.ICBuildCommandLauncher;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChain2;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.docker.launcher.ContainerCommandLauncher;
import org.eclipse.cdt.docker.launcher.ContainerTargetTypeProvider;
import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.linuxtools.docker.ui.Activator;

/**
 * The Container GCC toolchain. It represents a GCC that will run in a Docker
 * Container. It can be overridden to change environment variable settings.
 *
 * @since 1.2
 */
public class ContainerGCCToolChain extends PlatformObject implements IToolChain, IToolChain2 {

	public static final String TYPE_ID = "org.eclipse.cdt.docker.launcher.gcc"; //$NON-NLS-1$

	private final IToolChainProvider provider;
	private final String id;
	private final Path path;
	private final IEnvironmentVariable[] envVars;
	private final Map<String, String> properties = new HashMap<>();

	private String cCommand;
	private String cppCommand;
	private String[] commands;

	public ContainerGCCToolChain(String id, IToolChainProvider provider, Map<String, String> properties,
			IEnvironmentVariable[] envVars) {
		this.provider = provider;
		this.path = new File("gcc").toPath(); //$NON-NLS-1$

		// We include arch in the id since a compiler can support multiple arches.
		StringBuilder idBuilder = new StringBuilder("container-gcc-"); //$NON-NLS-1$
		idBuilder.append(properties.get(Platform.getOSArch()));
		idBuilder.append('-');
		idBuilder.append(path.toString());
		this.id = id;

		this.properties.putAll(properties);
		this.envVars = envVars;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	public Path getPath() {
		return path;
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
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		StringBuilder name = new StringBuilder(); // $NON-NLS-1$
		String os = getProperty(ATTR_OS);
		if (os != null) {
			name.append(os);
			name.append(' ');
		}

		String arch = getProperty(ATTR_ARCH);
		if (arch != null) {
			name.append(arch);
			name.append(' ');
		}

		if (path != null) {
			name.append(path.toString());
		}

		return name.toString();
	}

	@Override
	public String getProperty(String key) {
		String value = properties.get(key);
		if (value != null) {
			return value;
		}

		switch (key) {
		case ATTR_OS:
			return ContainerTargetTypeProvider.CONTAINER_LINUX;
		case ATTR_ARCH:
			return Platform.getOSArch();
		}

		return null;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public String getBinaryParserId() {
		return CCorePlugin.PLUGIN_ID + ".ELF"; //$NON-NLS-1$
	}

	protected void addDiscoveryOptions(List<String> command) {
		command.add("-E"); //$NON-NLS-1$
		command.add("-P"); //$NON-NLS-1$
		command.add("-v"); //$NON-NLS-1$
		command.add("-dD"); //$NON-NLS-1$
	}

	@Override
	synchronized public IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig,
			List<String> commandStrings, IExtendedScannerInfo baseScannerInfo, IResource resource,
			URI buildDirectoryURI) {
		try {
			Path buildDirectory = Paths.get(buildDirectoryURI);

			int offset = 0;
			Path command = Paths.get(commandStrings.get(offset));

			// look for ccache being used
			if (command.toString().contains("ccache")) { //$NON-NLS-1$
				command = Paths.get(commandStrings.get(++offset));
			}

			List<String> commandLine = new ArrayList<>();
			if (command.isAbsolute()) {
				commandLine.add(command.toString());
			} else {
				commandLine.add(getCommandPath(command).toString());
			}

			if (baseScannerInfo != null) {
				if (baseScannerInfo.getIncludePaths() != null) {
					for (String includePath : baseScannerInfo.getIncludePaths()) {
						commandLine.add("-I" + includePath); //$NON-NLS-1$
					}
				}

				if (baseScannerInfo.getDefinedSymbols() != null) {
					for (Map.Entry<String, String> macro : baseScannerInfo.getDefinedSymbols().entrySet()) {
						if (macro.getValue() != null && !macro.getValue().isEmpty()) {
							commandLine.add("-D" + macro.getKey() + "=" + macro.getValue()); //$NON-NLS-1$
						} else {
							commandLine.add("-D" + macro.getKey()); //$NON-NLS-1$
						}
					}
				}
			}

			addDiscoveryOptions(commandLine);
			commandLine.addAll(commandStrings.subList(offset + 1, commandStrings.size()));

			// Strip surrounding quotes from the args on Windows
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				for (int i = 0; i < commandLine.size(); i++) {
					String arg = commandLine.get(i);
					if (arg.startsWith("\"") && arg.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
						commandLine.set(i, arg.substring(1, arg.length() - 1));
					}
				}
			}

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
				String arg = commandLine.get(i);
				if (!arg.startsWith("-")) { //$NON-NLS-1$
					Path filePath;
					try {
						filePath = buildDirectory.resolve(commandLine.get(i)).normalize();
					} catch (InvalidPathException e) {
						continue;
					}
					IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(filePath.toUri());
					if (files.length > 0 && files[0].exists()) {
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
				} else if (arg.equals("-o")) { //$NON-NLS-1$
					// skip over the next arg
					// TODO handle other args like this
					i++;
				}
			}
			if (tmpFile == null) {
				// Have to assume there wasn't a source file. Add one in the
				// resource's container
				// TODO really?
				IPath parentPath = resource instanceof IFile ? resource.getParent().getLocation()
						: resource.getLocation();
				if (parentPath.toFile().exists()) {
					tmpFile = Files.createTempFile(parentPath.toFile().toPath(), ".sc", ".cpp"); //$NON-NLS-1$ //$NON-NLS-2$
					commandLine.add(tmpFile.toString());
				}
			}

			return getScannerInfo(buildConfig, commandLine, buildDirectory, tmpFile);
		} catch (IOException e) {
			Activator.log(e);
			return null;
		}
	}

	@Override
	synchronized public IExtendedScannerInfo getDefaultScannerInfo(IBuildConfiguration buildConfig,
			IExtendedScannerInfo baseScannerInfo, ILanguage language, URI buildDirectoryURI) {
		try {
			String[] commands = getCompileCommands(language);
			if (commands == null || commands.length == 0) {
				// no default commands
				return null;
			}

			Path buildDirectory = Paths.get(buildDirectoryURI);

			// Pick the first one
			Path command = Paths.get(commands[0]);
			List<String> commandLine = new ArrayList<>();
			if (command.isAbsolute()) {
				commandLine.add(command.toString());
			} else {
				commandLine.add(getCommandPath(command).toString());
			}

			if (baseScannerInfo != null) {
				if (baseScannerInfo.getIncludePaths() != null) {
					for (String includePath : baseScannerInfo.getIncludePaths()) {
						commandLine.add("-I" + includePath); //$NON-NLS-1$
					}
				}

				if (baseScannerInfo.getDefinedSymbols() != null) {
					for (Map.Entry<String, String> macro : baseScannerInfo.getDefinedSymbols().entrySet()) {
						if (macro.getValue() != null && !macro.getValue().isEmpty()) {
							commandLine.add("-D" + macro.getKey() + "=" + macro.getValue()); //$NON-NLS-1$
						} else {
							commandLine.add("-D" + macro.getKey()); //$NON-NLS-1$
						}
					}
				}
			}

			addDiscoveryOptions(commandLine);

			// output to stdout
			commandLine.add("-o"); //$NON-NLS-1$
			commandLine.add("-"); //$NON-NLS-1$

			// Source is an empty tmp file
			String extension;
			if (GPPLanguage.ID.equals(language.getId())) {
				extension = ".cpp"; //$NON-NLS-1$
			} else if (GCCLanguage.ID.equals(language.getId())) {
				extension = ".c"; //$NON-NLS-1$
			} else {
				// In theory we shouldn't get here
				return null;
			}

			Path tmpFile = Files.createTempFile(buildDirectory, ".sc", extension); //$NON-NLS-1$
			commandLine.add(tmpFile.toString());

			return getScannerInfo(buildConfig, commandLine, buildDirectory, tmpFile);
		} catch (IOException e) {
			Activator.log(e);
			return null;
		}
	}

	private IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, List<String> commandLine,
			Path buildDirectory, Path tmpFile) throws IOException {
		Files.createDirectories(buildDirectory);

		// Startup the command
		ContainerCommandLauncher commandLauncher = new ContainerCommandLauncher();
		ICBuildConfiguration cconfig = buildConfig.getAdapter(ICBuildConfiguration.class);
		commandLauncher.setBuildConfiguration(cconfig);
		commandLauncher.setProject(buildConfig.getProject());
		// CCorePlugin.getDefault().getBuildEnvironmentManager().setEnvironment(processBuilder.environment(),
		// buildConfig, true);
		org.eclipse.core.runtime.IPath commandPath = new org.eclipse.core.runtime.Path(commandLine.get(0));
		String[] args = commandLine.subList(1, commandLine.size()).toArray(new String[0]);
		org.eclipse.core.runtime.IPath workingDirectory = new org.eclipse.core.runtime.Path(buildDirectory.toString());

		Process process;
		try (ByteArrayOutputStream stdout = new ByteArrayOutputStream();
				ByteArrayOutputStream stderr = new ByteArrayOutputStream()) {
			process = commandLauncher.execute(commandPath, args, new String[0], workingDirectory,
					new NullProgressMonitor());
			if (process != null
					&& commandLauncher.waitAndRead(stdout, stderr, new NullProgressMonitor()) != ICommandLauncher.OK) {
				String errMsg = commandLauncher.getErrorMessage();
				DockerLaunchUIPlugin.logErrorMessage(errMsg);
				return null;
			}

			// Scan for the scanner info
			Map<String, String> symbols = new HashMap<>();
			List<String> includePath = new ArrayList<>();
			Pattern definePattern = Pattern.compile("#define ([^\\s]*)\\s(.*)"); //$NON-NLS-1$
			boolean inIncludePaths = false;

			// concatenate stdout after stderr as stderr has the include paths
			// and stdout has the defines
			String[] outlines = stdout.toString(StandardCharsets.UTF_8.name()).split("\\r?\\n"); //$NON-NLS-1$
			String[] errlines = stderr.toString(StandardCharsets.UTF_8.name()).split("\\r?\\n"); //$NON-NLS-1$
			String[] lines = new String[errlines.length + outlines.length];
			System.arraycopy(errlines, 0, lines, 0, errlines.length);
			System.arraycopy(outlines, 0, lines, errlines.length, outlines.length);

			for (String line : lines) {
				line = line.trim();
				if (inIncludePaths) {
					if (line.equals("End of search list.")) { //$NON-NLS-1$
						inIncludePaths = false;
					} else {
						String include = line.trim();
						org.eclipse.core.runtime.IPath path = new org.eclipse.core.runtime.Path(include);
						if (!path.isAbsolute()) {
							org.eclipse.core.runtime.IPath newPath = workingDirectory.append(path);
							include = newPath.makeAbsolute().toPortableString();
						}
						includePath.add(include);
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

			// Process include paths for scanner info and point to any copied
			// header directories
			includePath = CommandLauncherManager.getInstance().processIncludePaths(cconfig, includePath);

			ExtendedScannerInfo info = new ExtendedScannerInfo(symbols,
					includePath.toArray(new String[includePath.size()]));
			return info;
		} catch (CoreException e1) {
			return null;
		} finally {
			Files.delete(tmpFile);
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
		if (envVars != null) {
			for (IEnvironmentVariable var : envVars) {
				if (var.getName().equals(name)) {
					return var;
				}
			}
		}
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables() {
		return envVars;
	}

	@Override
	public Path getCommandPath(Path command) {
		return command;
	}

	private void initCompileCommands() {
		if (commands == null) {
			cCommand = path.getFileName().toString();
			cppCommand = null;
			if (cCommand.contains("gcc")) { //$NON-NLS-1$
				cppCommand = cCommand.replace("gcc", "g++"); //$NON-NLS-1$ //$NON-NLS-2$
				// Also recognize c++ as an alias for g++
				commands = new String[] { cCommand, cppCommand, cCommand.replace("gcc", "cc"), //$NON-NLS-1$ //$NON-NLS-2$
						cCommand.replace("gcc", "c++") }; //$NON-NLS-1$ //$NON-NLS-2$
			} else if (cCommand.contains("clang")) { //$NON-NLS-1$
				cppCommand = cCommand.replace("clang", "clang++"); //$NON-NLS-1$ //$NON-NLS-2$
				commands = new String[] { cCommand, cppCommand };
			} else if (cCommand.contains("emcc")) { //$NON-NLS-1$
				// TODO Hack for emscripten. Can we generalize?
				cppCommand = cCommand.replace("emcc", "em++"); //$NON-NLS-1$ //$NON-NLS-2$
				commands = new String[] { cCommand, cppCommand };
			} else {
				commands = new String[] { cCommand };
			}
		}
	}

	@Override
	public String[] getCompileCommands() {
		initCompileCommands();
		return commands;
	}

	@Override
	public String[] getCompileCommands(ILanguage language) {
		initCompileCommands();
		if (GPPLanguage.ID.equals(language.getId())) {
			return new String[] { cppCommand != null ? cppCommand : cCommand };
		} else if (GCCLanguage.ID.equals(language.getId())) {
			return new String[] { cCommand };
		} else {
			return new String[0];
		}
	}

	@Override
	public IResource[] getResourcesFromCommand(List<String> cmd, URI buildDirectoryURI) {
		// Start at the back looking for arguments
		List<IResource> resources = new ArrayList<>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = cmd.size() - 1; i >= 0; --i) {
			String arg = cmd.get(i);
			if (arg.startsWith("-")) { //$NON-NLS-1$
				// ran into an option, we're done.
				break;
			}
			if (i > 1 && cmd.get(i - 1).equals("-o")) { //$NON-NLS-1$
				// this is an output file
				--i;
				continue;
			}
			try {
				Path srcPath = Paths.get(arg);
				URI uri;
				if (srcPath.isAbsolute()) {
					uri = srcPath.toUri();
				} else {
					if (arg.startsWith("/") && Platform.getOS().equals(Platform.OS_WIN32)) { //$NON-NLS-1$
						String drive = srcPath.getName(0).toString();
						if (drive.length() == 1) {
							srcPath = Paths.get(drive + ":\\").resolve(srcPath.subpath(1, srcPath.getNameCount())); //$NON-NLS-1$
						}
					}
					uri = Paths.get(buildDirectoryURI).resolve(srcPath).toUri().normalize();
				}

				for (IFile resource : root.findFilesForLocationURI(uri)) {
					resources.add(resource);
				}
			} catch (IllegalArgumentException e) {
				// Bad URI
				continue;
			}
		}

		return resources.toArray(new IResource[resources.size()]);
	}

	@Override
	public List<String> stripCommand(List<String> command, IResource[] resources) {
		List<String> newCommand = new ArrayList<>();

		for (int i = 0; i < command.size() - resources.length; ++i) {
			String arg = command.get(i);
			if (arg.startsWith("-o")) { //$NON-NLS-1$
				if (arg.equals("-o")) { //$NON-NLS-1$
					i++;
				}
				continue;
			}
			newCommand.add(arg);
		}

		return newCommand;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContainerGCCToolChain tc = (ContainerGCCToolChain) obj;
		if (tc.id != this.id)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public Process startBuildProcess(ICBuildConfiguration config, List<String> command, String buildDirectory,
			IEnvironmentVariable[] envVars, IConsole console, IProgressMonitor monitor)
			throws CoreException, IOException {

		IPath cmdPath = new org.eclipse.core.runtime.Path("/usr/bin/env"); //$NON-NLS-1$

		List<String> argList = new ArrayList<>();
		for (IEnvironmentVariable var : envVars) {
			argList.add(var.getName() + "=" + var.getValue()); //$NON-NLS-1$
		}

		argList.add("sh"); //$NON-NLS-1$
		argList.add("-c"); //$NON-NLS-1$

		StringBuffer buf = new StringBuffer();
		for (String s : command) {
			buf.append(s);
			buf.append(" "); //$NON-NLS-1$
		}
		buf.deleteCharAt(buf.length() - 1); // remove last blank;
		argList.add(buf.toString());

		ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(config);

		// Bug 536884 - following is a kludge to allow us to check if the
		// Container headers have been deleted by the user in which case
		// we need to re-perform scanner info collection and copy headers
		// to the host.
		// TODO: make this cleaner
		CommandLauncherManager.getInstance().processIncludePaths(config, Collections.emptyList());

		launcher.setProject(config.getBuildConfiguration().getProject());
		if (launcher instanceof ICBuildCommandLauncher) {
			((ICBuildCommandLauncher) launcher).setBuildConfiguration(config);
			console.getOutputStream().write(((ICBuildCommandLauncher) launcher).getConsoleHeader());
		}

		org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(buildDirectory);

		Process p = launcher.execute(cmdPath, argList.toArray(new String[0]), new String[0], workingDir, monitor);

		return p;
	}

}
