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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IncludeExportPatterns;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.build.Messages;
import org.eclipse.cdt.internal.core.model.BinaryRunner;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.parser.ParserSettings2;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.PHdr;
import org.eclipse.cdt.utils.elf.parser.ElfBinaryShared;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
	private static final String TOOLCHAIN_ID = "cdt.toolChain.id"; //$NON-NLS-1$
	private static final String TOOLCHAIN_VERSION = "cdt.toolChain.version"; //$NON-NLS-1$
	private static final String LAUNCH_MODE = "cdt.launchMode"; //$NON-NLS-1$

	private static final List<String> DEFAULT_COMMAND = new ArrayList<>(0);

	private final String name;
	private final IBuildConfiguration config;
	private final IToolChain toolChain;
	private String launchMode;

	private final Map<IResource, List<IScannerInfoChangeListener>> scannerInfoListeners = new HashMap<>();
	private ScannerInfoCache scannerInfoCache;

	private Map<String, String> properties;

	protected CBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		this.config = config;
		this.name = name;

		Preferences settings = getSettings();
		String typeId = settings.get(TOOLCHAIN_TYPE, ""); //$NON-NLS-1$
		String id = settings.get(TOOLCHAIN_ID, ""); //$NON-NLS-1$
		String version = settings.get(TOOLCHAIN_VERSION, ""); //$NON-NLS-1$
		IToolChainManager toolChainManager = CCorePlugin.getService(IToolChainManager.class);
		IToolChain tc = toolChainManager.getToolChain(typeId, id, version);

		if (tc == null) {
			// check for other versions
			Collection<IToolChain> tcs = toolChainManager.getToolChains(typeId, id);
			if (!tcs.isEmpty()) {
				// TODO grab the newest version
				tc = tcs.iterator().next();
			} else {
				throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID,
						String.format(Messages.CBuildConfigurationtoolchainMissing, config.getName())));
			}
		}
		toolChain = tc;

		launchMode = settings.get(LAUNCH_MODE, null); // $NON-NLS-1$
	}

	protected CBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		this(config, name, toolChain, "run"); //$NON-NLS-1$
	}

	/**
	 * @since 6.2
	 */
	protected CBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			String launchMode) {
		this.config = config;
		this.name = name;
		this.toolChain = toolChain;
		this.launchMode = launchMode;

		Preferences settings = getSettings();
		settings.put(TOOLCHAIN_TYPE, toolChain.getProvider().getId());
		settings.put(TOOLCHAIN_ID, toolChain.getId());
		settings.put(TOOLCHAIN_VERSION, toolChain.getVersion());
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
	}

	protected CBuildConfiguration(IBuildConfiguration config, IToolChain toolChain) {
		this(config, DEFAULT_NAME, toolChain);
	}

	@Override
	public IBuildConfiguration getBuildConfiguration() {
		return config;
	}

	public String getName() {
		return name;
	}

	/**
	 * @since 6.2
	 */
	public String getLaunchMode() {
		return launchMode;
	}

	/**
	 * @since 6.2
	 */
	protected void setLaunchMode(String launchMode) {
		this.launchMode = launchMode;
		Preferences settings = getSettings();
		settings.put(LAUNCH_MODE, launchMode);
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
	}

	public IProject getProject() {
		return config.getProject();
	}

	@Override
	public String getBinaryParserId() throws CoreException {
		return toolChain != null ? toolChain.getBinaryParserId() : CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID;
	}

	public IContainer getBuildContainer() throws CoreException {
		// TODO should really be passing a monitor in here or create this in
		// a better spot. should also throw the core exception
		// TODO make the name of this folder a project property
		IProgressMonitor monitor = new NullProgressMonitor();
		IProject project = getProject();
		IFolder buildRootFolder = project.getFolder("build"); //$NON-NLS-1$
		if (!buildRootFolder.exists()) {
			buildRootFolder.create(IResource.FORCE | IResource.DERIVED, true, monitor);
		}
		IFolder buildFolder = buildRootFolder.getFolder(name);
		if (!buildFolder.exists()) {
			buildFolder.create(IResource.FORCE | IResource.DERIVED, true, monitor);
		}

		return buildFolder;
	}

	public URI getBuildDirectoryURI() throws CoreException {
		return getBuildContainer().getLocationURI();
	}

	public Path getBuildDirectory() throws CoreException {
		return Paths.get(getBuildDirectoryURI());
	}

	@Override
	public void setBuildEnvironment(Map<String, String> env) {
		CCorePlugin.getDefault().getBuildEnvironmentManager().setEnvironment(env, config, true);
	}

	/**
	 * @since 6.1
	 */
	@Override
	public IBinary[] getBuildOutput() throws CoreException {
		ICProject cproject = CoreModel.getDefault().create(config.getProject());
		IBinaryContainer binaries = cproject.getBinaryContainer();
		IPath outputPath = getBuildContainer().getFullPath();
		List<IBinary> outputs = new ArrayList<>();
		for (IBinary binary : binaries.getBinaries()) {
			if (outputPath.isPrefixOf(binary.getPath())) {
				if (binary.isExecutable()) {
					outputs.add(binary);
				} else if (binary.isSharedLib()) {
					// Special case of PIE executable that looks like shared
					// library
					IBinaryParser.IBinaryObject bin = binary.getAdapter(IBinaryParser.IBinaryObject.class);
					if (bin instanceof ElfBinaryShared) {
						try {
							Elf elf = new Elf(bin.getPath().toOSString());
							for (PHdr phdr : elf.getPHdrs()) {
								if (phdr.p_type == PHdr.PT_INTERP) {
									outputs.add(binary);
									break;
								}
							}
						} catch (IOException e) {
							CCorePlugin.log(e);
						}
					}
				}
			}
		}

		if (outputs.isEmpty()) {
			// Give the binary runner a kick and try again.
			BinaryRunner runner = CModelManager.getDefault().getBinaryRunner(cproject);
			runner.start();
			runner.waitIfRunning();
			
			for (IBinary binary : binaries.getBinaries()) {
				if (binary.isExecutable() && outputPath.isPrefixOf(binary.getPath())) {
					outputs.add(binary);
				}
			}
		}

		return outputs.toArray(new IBinary[outputs.size()]);
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
		return toolChain;
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

		String pathStr = env.get("PATH"); //$NON-NLS-1$
		if (pathStr == null) {
			pathStr = env.get("Path"); // for Windows //$NON-NLS-1$
			if (pathStr == null) {
				return null; // no idea
			}
		}
		String[] path = pathStr.split(File.pathSeparator);
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

	private File getScannerInfoCacheFile() {
		return CCorePlugin.getDefault().getStateLocation().append("infoCache") //$NON-NLS-1$
				.append(getProject().getName()).append(name + ".json").toFile(); //$NON-NLS-1$
	}

	private static class IExtendedScannerInfoCreator implements JsonDeserializer<IExtendedScannerInfo> {
		@Override
		public IExtendedScannerInfo deserialize(JsonElement element, Type arg1,
				JsonDeserializationContext arg2) throws JsonParseException {
			JsonObject infoObj = element.getAsJsonObject();

			Map<String, String> definedSymbols = null;
			if (infoObj.has("definedSymbols")) { //$NON-NLS-1$
				JsonObject definedSymbolsObj = infoObj.get("definedSymbols").getAsJsonObject(); //$NON-NLS-1$
				definedSymbols = new HashMap<>();
				for (Entry<String, JsonElement> entry : definedSymbolsObj.entrySet()) {
					definedSymbols.put(entry.getKey(), entry.getValue().getAsString());
				}
			}

			String[] includePaths = null;
			if (infoObj.has("includePaths")) { //$NON-NLS-1$
				JsonArray includePathsArray = infoObj.get("includePaths").getAsJsonArray(); //$NON-NLS-1$
				List<String> includePathsList = new ArrayList<>(includePathsArray.size());
				for (Iterator<JsonElement> i = includePathsArray.iterator(); i.hasNext();) {
					includePathsList.add(i.next().getAsString());
				}
				includePaths = includePathsList.toArray(new String[includePathsList.size()]);
			}

			IncludeExportPatterns includeExportPatterns = null;
			if (infoObj.has("includeExportPatterns")) { //$NON-NLS-1$
				JsonObject includeExportPatternsObj = infoObj.get("includeExportPatterns").getAsJsonObject(); //$NON-NLS-1$
				String exportPattern = null;
				if (includeExportPatternsObj.has("includeExportPattern")) { //$NON-NLS-1$
					exportPattern = includeExportPatternsObj.get("includeExportPattern") //$NON-NLS-1$
							.getAsJsonObject().get("pattern").getAsString(); //$NON-NLS-1$
				}

				String beginExportsPattern = null;
				if (includeExportPatternsObj.has("includeBeginExportPattern")) { //$NON-NLS-1$
					beginExportsPattern = includeExportPatternsObj.get("includeBeginExportPattern") //$NON-NLS-1$
							.getAsJsonObject().get("pattern").getAsString(); //$NON-NLS-1$
				}

				String endExportsPattern = null;
				if (includeExportPatternsObj.has("includeEndExportPattern")) { //$NON-NLS-1$
					endExportsPattern = includeExportPatternsObj.get("includeEndExportPattern") //$NON-NLS-1$
							.getAsJsonObject().get("pattern").getAsString(); //$NON-NLS-1$
				}

				includeExportPatterns = new IncludeExportPatterns(exportPattern, beginExportsPattern,
						endExportsPattern);
			}

			ExtendedScannerInfo info = new ExtendedScannerInfo(definedSymbols, includePaths);
			info.setIncludeExportPatterns(includeExportPatterns);
			info.setParserSettings(new ParserSettings2());
			return info;
		}
	}

	/**
	 * @since 6.1
	 */
	protected void loadScannerInfoCache() {
		if (scannerInfoCache == null) {
			File cacheFile = getScannerInfoCacheFile();
			if (cacheFile.exists()) {
				try (FileReader reader = new FileReader(cacheFile)) {
					GsonBuilder gsonBuilder = new GsonBuilder();
					gsonBuilder.registerTypeAdapter(IExtendedScannerInfo.class,
							new IExtendedScannerInfoCreator());
					Gson gson = gsonBuilder.create();
					scannerInfoCache = gson.fromJson(reader, ScannerInfoCache.class);
				} catch (IOException e) {
					CCorePlugin.log(e);
					scannerInfoCache = new ScannerInfoCache();
				}
			} else {
				scannerInfoCache = new ScannerInfoCache();
			}
			scannerInfoCache.initCache();
		}
	}

	/**
	 * @since 6.1
	 */
	protected void saveScannerInfoCache() {
		File cacheFile = getScannerInfoCacheFile();
		if (!cacheFile.getParentFile().exists()) {
			try {
				Files.createDirectories(cacheFile.getParentFile().toPath());
			} catch (IOException e) {
				CCorePlugin.log(e);
				return;
			}
		}

		try (FileWriter writer = new FileWriter(getScannerInfoCacheFile())) {
			Gson gson = new Gson();
			gson.toJson(scannerInfoCache, writer);
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
	}

	/**
	 * @since 6.1
	 */
	protected ScannerInfoCache getScannerInfoCache() {
		return scannerInfoCache;
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		loadScannerInfoCache();
		IExtendedScannerInfo info = scannerInfoCache.getScannerInfo(resource);
		if (info == null) {
			ICElement celement = CCorePlugin.getDefault().getCoreModel().create(resource);
			if (celement instanceof ITranslationUnit) {
				ITranslationUnit tu = (ITranslationUnit) celement;
				try {
					info = getToolChain().getDefaultScannerInfo(getBuildConfiguration(), null,
							tu.getLanguage(), getBuildDirectoryURI());
					scannerInfoCache.addScannerInfo(DEFAULT_COMMAND, info, resource);
					saveScannerInfoCache();
				} catch (CoreException e) {
					CCorePlugin.log(e.getStatus());
				}
			}
		}
		return info;
	}

	private boolean infoChanged = false;

	@Override
	public boolean processLine(String line) {
		// TODO smarter line parsing to deal with quoted arguments
		List<String> command = Arrays.asList(line.split("\\s+")); //$NON-NLS-1$

		// Make sure it's a compile command
		String[] compileCommands = toolChain.getCompileCommands();
		loop:
		for (String arg : command) {
			// TODO we should really ask the toolchain, not all args start with '-'
			if (arg.startsWith("-")) { //$NON-NLS-1$
				// option found, missed our command
				return false;
			}

			for (String cc : compileCommands) {
				if (arg.endsWith(cc)
						&& (arg.equals(cc) || arg.endsWith("/" + cc) || arg.endsWith("\\" + cc))) { //$NON-NLS-1$ //$NON-NLS-2$
					break loop;
				}
			}
		}

		try {
			IResource[] resources = toolChain.getResourcesFromCommand(command, getBuildDirectoryURI());
			if (resources != null) {
				List<String> commandStrings = toolChain.stripCommand(command, resources);

				for (IResource resource : resources) {
					loadScannerInfoCache();
					if (scannerInfoCache.hasCommand(commandStrings)) {
						if (!scannerInfoCache.hasResource(commandStrings, resource)) {
							scannerInfoCache.addResource(commandStrings, resource);
							infoChanged = true;
						}
					} else {
						Path commandPath = findCommand(command.get(0));
						command.set(0, commandPath.toString());
						IExtendedScannerInfo info = getToolChain().getScannerInfo(getBuildConfiguration(),
								command, null, resource, getBuildDirectoryURI());
						scannerInfoCache.addScannerInfo(commandStrings, info, resource);
						infoChanged = true;
					}
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
		// TODO be more surgical
		if (infoChanged) {
			saveScannerInfoCache();
			CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(getProject()));
			infoChanged = false;
		}
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		List<IScannerInfoChangeListener> listeners = scannerInfoListeners.get(resource);
		if (listeners == null) {
			listeners = new ArrayList<>();
			scannerInfoListeners.put(resource, listeners);
		}
		listeners.add(listener);
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		List<IScannerInfoChangeListener> listeners = scannerInfoListeners.get(resource);
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty()) {
				scannerInfoListeners.remove(resource);
			}
		}
	}

	/**
	 * Takes a command path and returns either the command path itself if it is
	 * absolute or the path to the command as it appears in the PATH environment
	 * variable. Also adjusts the command for Windows's .exe extension.
	 * 
	 * @since 6.1
	 */
	public static Path getCommandFromPath(Path command) {
		if (command.isAbsolute()) {
			return command;
		}

		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			if (!command.toString().endsWith(".exe")) { //$NON-NLS-1$
				command = Paths.get(command.toString() + ".exe"); //$NON-NLS-1$
			}
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

	/**
	 * @since 6.2
	 */
	@Override
	public boolean setProperties(Map<String, String> properties) {
		if (this.properties == null || !this.properties.equals(properties)) {
			this.properties = properties;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @since 6.2
	 */
	@Override
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = getDefaultProperties();
		}
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * @since 6.2
	 */
	@Override
	public Map<String, String> getDefaultProperties() {
		return new HashMap<>();
	}

}
