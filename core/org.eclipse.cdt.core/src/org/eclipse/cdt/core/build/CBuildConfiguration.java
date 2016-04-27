/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Root class for CDT build configurations. Provides access to the build
 * settings for subclasses.
 * 
 * @since 6.0
 * @noextend This class is provisional and should be subclassed with caution.
 */
public abstract class CBuildConfiguration extends PlatformObject
		implements ICBuildConfiguration, IMarkerGenerator, IConsoleParser {

	private static final String TOOLCHAIN_TYPE = "cdt.toolChain.type"; //$NON-NLS-1$
	private static final String TOOLCHAIN_NAME = "cdt.toolChain.name"; //$NON-NLS-1$

	private final String name;
	private final IBuildConfiguration config;
	private IToolChain toolChain;

	protected CBuildConfiguration(IBuildConfiguration config, String name) {
		this.config = config;
		this.name = name;
	}

	@Override
	public IBuildConfiguration getBuildConfiguration() {
		return config;
	}

	public String getName() {
		return name;
	}

	public IProject getProject() {
		return config.getProject();
	}

	public IContainer getBuildContainer() throws CoreException {
		// TODO should really be passing a monitor in here or create this in
		// a better spot. should also throw the core exception
		// TODO make the name of this folder a project property
		IFolder buildRootFolder = getProject().getFolder("build"); //$NON-NLS-1$
		if (!buildRootFolder.exists()) {
			buildRootFolder.create(IResource.FORCE | IResource.DERIVED, true, new NullProgressMonitor());
		}
		IFolder buildFolder = buildRootFolder.getFolder(name);
		if (!buildFolder.exists()) {
			buildFolder.create(true, true, new NullProgressMonitor());
			buildFolder.setDerived(true, null);
			ICProject cproject = CoreModel.getDefault().create(getProject());
			IOutputEntry output = CoreModel.newOutputEntry(buildFolder.getFullPath());
			IPathEntry[] oldEntries = cproject.getRawPathEntries();
			IPathEntry[] newEntries = new IPathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length] = output;
			cproject.setRawPathEntries(newEntries, null);
		}

		return buildFolder;
	}

	public URI getBuildDirectoryURI() throws CoreException {
		return getBuildContainer().getLocationURI();
	}

	public Path getBuildDirectory() throws CoreException {
		return Paths.get(getBuildDirectoryURI());
	}

	public void setBuildEnvironment(Map<String, String> env) {
		CCorePlugin.getDefault().getBuildEnvironmentManager().setEnvironment(env, config, true);
	}

	public void setActive(IProgressMonitor monitor) throws CoreException {
		IProject project = config.getProject();
		if (config.equals(project.getActiveBuildConfig())) {
			// already set
			return;
		}

		IProjectDescription projectDesc = project.getDescription();
		projectDesc.setActiveBuildConfig(config.getName());
		project.setDescription(projectDesc, monitor);
	}

	protected Preferences getSettings() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node("config") //$NON-NLS-1$
				.node(getProject().getName()).node(config.getName());
	}

	@Override
	public IToolChain getToolChain() throws CoreException {
		if (toolChain == null) {
			Preferences settings = getSettings();
			String typeId = settings.get(TOOLCHAIN_TYPE, ""); //$NON-NLS-1$
			String id = settings.get(TOOLCHAIN_NAME, ""); //$NON-NLS-1$
			IToolChainManager toolChainManager = CCorePlugin.getService(IToolChainManager.class);
			toolChain = toolChainManager.getToolChain(typeId, id);

			if (toolChain == null) {
				CCorePlugin.log(String.format("Toolchain missing for config: %s", config.getName()));
			}
		}

		return toolChain;
	}

	protected void setToolChain(IToolChain toolChain) {
		this.toolChain = toolChain;

		Preferences settings = getSettings();
		settings.put(TOOLCHAIN_TYPE, toolChain.getProvider().getId());
		settings.put(TOOLCHAIN_NAME, toolChain.getName());
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public IEnvironmentVariable getVariable(String name) {
		// By default, none
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables() {
		// by default, none
		return null;
	}

	@Override
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		addMarker(new ProblemMarkerInfo(file, lineNumber, errorDesc, severity, errorVar, null));
	}

	@Override
	public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		try {
			IProject project = config.getProject();
			IResource markerResource = problemMarkerInfo.file;
			if (markerResource == null) {
				markerResource = project;
			}
			String externalLocation = null;
			if (problemMarkerInfo.externalPath != null && !problemMarkerInfo.externalPath.isEmpty()) {
				externalLocation = problemMarkerInfo.externalPath.toOSString();
			}

			// Try to find matching markers and don't put in duplicates
			IMarker[] markers = markerResource.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true,
					IResource.DEPTH_ONE);
			for (IMarker m : markers) {
				int line = m.getAttribute(IMarker.LINE_NUMBER, -1);
				int sev = m.getAttribute(IMarker.SEVERITY, -1);
				String msg = (String) m.getAttribute(IMarker.MESSAGE);
				if (line == problemMarkerInfo.lineNumber
						&& sev == mapMarkerSeverity(problemMarkerInfo.severity)
						&& msg.equals(problemMarkerInfo.description)) {
					String extloc = (String) m.getAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
					if (extloc == externalLocation || (extloc != null && extloc.equals(externalLocation))) {
						if (project == null || project.equals(markerResource.getProject())) {
							return;
						}
						String source = (String) m.getAttribute(IMarker.SOURCE_ID);
						if (project.getName().equals(source)) {
							return;
						}
					}
				}
			}

			String type = problemMarkerInfo.getType();
			if (type == null) {
				type = ICModelMarker.C_MODEL_PROBLEM_MARKER;
			}

			IMarker marker = markerResource.createMarker(type);
			marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
			marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(problemMarkerInfo.severity));
			marker.setAttribute(IMarker.LINE_NUMBER, problemMarkerInfo.lineNumber);
			marker.setAttribute(IMarker.CHAR_START, problemMarkerInfo.startChar);
			marker.setAttribute(IMarker.CHAR_END, problemMarkerInfo.endChar);
			if (problemMarkerInfo.variableName != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, problemMarkerInfo.variableName);
			}
			if (externalLocation != null) {
				URI uri = URIUtil.toURI(externalLocation);
				if (uri.getScheme() != null) {
					marker.setAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION, externalLocation);
					String locationText = NLS.bind(
							CCorePlugin.getResourceString("ACBuilder.ProblemsView.Location"), //$NON-NLS-1$
							problemMarkerInfo.lineNumber, externalLocation);
					marker.setAttribute(IMarker.LOCATION, locationText);
				}
			} else if (problemMarkerInfo.lineNumber == 0) {
				marker.setAttribute(IMarker.LOCATION, " "); //$NON-NLS-1$
			}
			// Set source attribute only if the marker is being set to a file
			// from different project
			if (project != null && !project.equals(markerResource.getProject())) {
				marker.setAttribute(IMarker.SOURCE_ID, project.getName());
			}

			// Add all other client defined attributes.
			Map<String, String> attributes = problemMarkerInfo.getAttributes();
			if (attributes != null) {
				for (Entry<String, String> entry : attributes.entrySet()) {
					marker.setAttribute(entry.getKey(), entry.getValue());
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
	}

	private int mapMarkerSeverity(int severity) {
		switch (severity) {
		case SEVERITY_ERROR_BUILD:
		case SEVERITY_ERROR_RESOURCE:
			return IMarker.SEVERITY_ERROR;
		case SEVERITY_INFO:
			return IMarker.SEVERITY_INFO;
		case SEVERITY_WARNING:
			return IMarker.SEVERITY_WARNING;
		}
		return IMarker.SEVERITY_ERROR;
	}

	protected Path findCommand(String command) {
		if (Platform.getOS().equals(Platform.OS_WIN32) && !command.endsWith(".exe")) { //$NON-NLS-1$
			command += ".exe"; //$NON-NLS-1$
		}

		Path cmdPath = Paths.get(command);
		if (cmdPath.isAbsolute()) {
			return cmdPath;
		}

		Map<String, String> env = new HashMap<>(System.getenv());
		setBuildEnvironment(env);

		String[] path = env.get("PATH").split(File.pathSeparator); //$NON-NLS-1$
		for (String dir : path) {
			Path commandPath = Paths.get(dir, command);
			if (Files.exists(commandPath)) {
				return commandPath;
			}
		}
		return null;
	}

	protected int watchProcess(Process process, IConsoleParser[] consoleParsers, IConsole console)
			throws CoreException {
		new ReaderThread(process.getInputStream(), consoleParsers, console.getOutputStream()).start();
		new ReaderThread(process.getErrorStream(), consoleParsers, console.getErrorStream()).start();
		try {
			return process.waitFor();
		} catch (InterruptedException e) {
			CCorePlugin.log(e);
			return -1;
		}
	}

	private static class ReaderThread extends Thread {

		private final BufferedReader in;
		private final PrintStream out;
		private final IConsoleParser[] consoleParsers;

		public ReaderThread(InputStream in, IConsoleParser[] consoleParsers, OutputStream out) {
			this.in = new BufferedReader(new InputStreamReader(in));
			this.consoleParsers = consoleParsers;
			this.out = new PrintStream(out);
		}

		@Override
		public void run() {
			try {
				for (String line = in.readLine(); line != null; line = in.readLine()) {
					for (IConsoleParser consoleParser : consoleParsers) {
						// Synchronize to avoid interleaving of lines
						synchronized (consoleParser) {
							consoleParser.processLine(line);
						}
					}
					out.println(line);
				}
			} catch (IOException e) {
				CCorePlugin.log(e);
			}
		}

	}

	private Map<IResource, IExtendedScannerInfo> cheaterInfo;
	private boolean infoChanged = false;

	private void initScannerInfo() {
		if (cheaterInfo == null) {
			cheaterInfo = new HashMap<>();
		}
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		initScannerInfo();
		return cheaterInfo.get(resource);
	}

	@Override
	public boolean processLine(String line) {
		// TODO smarter line parsing to deal with quoted arguments
		String[] command = line.split("\\s+"); //$NON-NLS-1$

		// Make sure it's a compile command
		boolean found = false;
		String[] compileCommands = toolChain.getCompileCommands();
		for (String arg : command) {
			if (arg.startsWith("-")) { //$NON-NLS-1$
				// option found, missed our command
				break;
			}

			for (String cc : compileCommands) {
				if (arg.equals(cc)) {
					found = true;
					break;
				}
			}
		}

		if (!found) {
			return false;
		}

		try {
			IResource[] resources = toolChain.getResourcesFromCommand(command, getBuildDirectoryURI());
			if (resources != null) {
				for (IResource resource : resources) {
					initScannerInfo();
					cheaterInfo.put(resource,
							getToolChain().getScannerInfo(getBuildConfiguration(), findCommand(command[0]),
									Arrays.copyOfRange(command, 1, command.length), null, resource,
									getBuildDirectoryURI()));
					infoChanged = true;
				}
				return true;
			} else {
				return false;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	@Override
	public void shutdown() {
		// TODO persist changes

		// Trigger a reindex if anything changed
		if (infoChanged) {
			CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(getProject()));
			infoChanged = false;
		}
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO for IScannerInfoProvider
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO for IScannerInfoProvider
	}

}
