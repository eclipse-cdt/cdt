package org.eclipse.cdt.docker.build.gcc.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.docker.launcher.CBuildContainerCommandLauncher;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;

public class DockerGCCToolChain implements IToolChain {

	private GCCToolChain gccToolChain;

	private String connectionName = System.getProperty("cbuild.docker.connectionName","unix:///var/run/docker.sock");
	private String imageName = System.getProperty("cbuild.docker.imageName");
	
	public DockerGCCToolChain(GCCToolChain gccToolChain) {
		this.gccToolChain = gccToolChain;
		// XXX docker/commandlauncher support
		this.gccToolChain.setProperty("remote", "true");
	}

	public <T> T getAdapter(Class<T> adapter) {
		return gccToolChain.getAdapter(adapter);
	}

	public IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, Path command, String[] args,
			IExtendedScannerInfo baseScannerInfo, IResource resource, URI buildDirectoryURI) {
		return gccToolChain.getScannerInfo(buildConfig, command, args, baseScannerInfo, resource, buildDirectoryURI);
	}

	public Path getPath() {
		return gccToolChain.getPath();
	}

	public IToolChainProvider getProvider() {
		return gccToolChain.getProvider();
	}

	public String getId() {
		return String.valueOf(Objects.hashCode(this))+gccToolChain.getId();
	}

	public String getVersion() {
		return gccToolChain.getVersion();
	}

	public String getName() {
		return gccToolChain.getName();
	}

	public String getProperty(String key) {
		return gccToolChain.getProperty(key);
	}

	public Map<String, String> getProperties() {
		return gccToolChain.getProperties();
	}

	public void setProperty(String key, String value) {
		gccToolChain.setProperty(key, value);
	}

	public String getBinaryParserId() {
		return gccToolChain.getBinaryParserId();
	}

	public IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, List<String> commandStrings,
			IExtendedScannerInfo baseScannerInfo, IResource resource, URI buildDirectoryURI) {
		return gccToolChain.getScannerInfo(buildConfig, commandStrings, baseScannerInfo, resource, buildDirectoryURI);
	}

	public IResource[] getResourcesFromCommand(String[] command, URI buildDirectoryURI) {
		return gccToolChain.getResourcesFromCommand(command, buildDirectoryURI);
	}

	public boolean matches(Map<String, String> properties) {
		return gccToolChain.matches(properties);
	}

	public ICommandLauncher getCommandLauncher() {
		return new CBuildContainerCommandLauncher(connectionName,imageName);
	}

	public String toString() {
		return gccToolChain.toString();
	}

	protected String[] convertEnvironment(IEnvironmentVariable[] vars) {
		String[] results = new String[vars.length];
		for(int i=0; i < vars.length; i++) 
			results[i] = vars[i].getName()+"="+vars[i].getValue();
		return results;
	}
	
	protected void addDiscoveryOptions(List<String> command) {
		command.add("-E"); //$NON-NLS-1$
		command.add("-P"); //$NON-NLS-1$
		command.add("-v"); //$NON-NLS-1$
		command.add("-dD"); //$NON-NLS-1$
	}

	protected Path findCommand(String command, String[] env) {
		if (Platform.getOS().equals(Platform.OS_WIN32)
				&& !(command.endsWith(".exe") || command.endsWith(".bat"))) { //$NON-NLS-1$ //$NON-NLS-2$
			command += ".exe"; //$NON-NLS-1$
		}

		Path cmdPath = Paths.get(command);
		if (cmdPath.isAbsolute()) {
			return cmdPath;
		}

		
		Map<String, String> envMap = new HashMap<>();
		for(String var: env) {
			String[] nv = var.split("=");
			if (nv != null)
				envMap.put(nv[0], nv[1]);
		}
		String pathStr = envMap.get("PATH"); //$NON-NLS-1$
		if (pathStr == null) {
			pathStr = envMap.get("Path"); // for Windows //$NON-NLS-1$
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

	@Override
	public IExtendedScannerInfo getDefaultScannerInfo(IBuildConfiguration buildConfig,
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
				Path p = getCommandPath(command);
				commandLine.add(p == null?command.toString():p.toString());
			}

			if (baseScannerInfo != null && baseScannerInfo.getIncludePaths() != null) {
				for (String includePath : baseScannerInfo.getIncludePaths()) {
					commandLine.add("-I" + includePath); //$NON-NLS-1$
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
			e.printStackTrace();
			return null;
		}
	}

	private IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, List<String> commandLine,
			Path buildDirectory, Path tmpFile) throws IOException {
		Files.createDirectories(buildDirectory);

		ICommandLauncher cm = getCommandLauncher();
		cm.setProject(buildConfig.getProject());
		
		String[] env = convertEnvironment(getVariables());
		
		String command = commandLine.get(0);
		org.eclipse.core.runtime.Path commandPath = new org.eclipse.core.runtime.Path(command);
		String[] args = commandLine.subList(1,commandLine.size()).toArray(new String[commandLine.size()-1]);
		org.eclipse.core.runtime.Path buildTargetDirPath = new org.eclipse.core.runtime.Path(
				buildDirectory.toFile().getPath());
		
		Map<String, String> symbols = new HashMap<>();
		List<String> includePath = new ArrayList<>();

/*
		Process process = null;
		try {
			process = cm.execute(commandPath, args, env, buildTargetDirPath, null);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Scan for the scanner info
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
			e.printStackTrace();
		}
		*/
		Files.delete(tmpFile);

		return new ExtendedScannerInfo(symbols, includePath.toArray(new String[includePath.size()]));

	}

	public String[] getErrorParserIds() {
		return gccToolChain.getErrorParserIds();
	}

	public IEnvironmentVariable getVariable(String name) {
		return gccToolChain.getVariable(name);
	}

	public IEnvironmentVariable[] getVariables() {
		return gccToolChain.getVariables();
	}

	public Path getCommandPath(Path command) {
		return findCommand(command.toString(),convertEnvironment(getVariables()));
	}

	public String[] getCompileCommands() {
		return gccToolChain.getCompileCommands();
	}

	public String[] getCompileCommands(ILanguage language) {
		return gccToolChain.getCompileCommands(language);
	}

	public IResource[] getResourcesFromCommand(List<String> cmd, URI buildDirectoryURI) {
		return gccToolChain.getResourcesFromCommand(cmd, buildDirectoryURI);
	}

	public List<String> stripCommand(List<String> command, IResource[] resources) {
		return gccToolChain.stripCommand(command, resources);
	}
	
}
