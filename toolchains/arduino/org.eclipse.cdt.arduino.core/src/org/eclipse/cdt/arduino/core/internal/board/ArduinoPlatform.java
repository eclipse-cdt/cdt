/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class ArduinoPlatform {

	private String name;
	private String architecture;
	private String version;
	private String category;
	private String url;
	private String archiveFileName;
	private String checksum;
	private String size;
	private List<ArduinoBoard> boards;
	private List<ToolDependency> toolsDependencies;

	private ArduinoPackage pkg;
	private HierarchicalProperties boardsProperties;
	private Properties platformProperties;
	private Map<String, String> menus = new HashMap<>();
	private Map<String, ArduinoLibrary> libraries;

	void setOwner(ArduinoPackage pkg) {
		this.pkg = pkg;
		for (ArduinoBoard board : boards) {
			board.setOwners(this);
		}
		for (ToolDependency toolDep : toolsDependencies) {
			toolDep.setOwner(this);
		}
	}

	public ArduinoPackage getPackage() {
		return pkg;
	}

	public String getName() {
		return name;
	}

	public String getArchitecture() {
		return architecture;
	}

	public String getVersion() {
		return version;
	}

	public String getCategory() {
		return category;
	}

	public String getUrl() {
		return url;
	}

	public String getArchiveFileName() {
		return archiveFileName;
	}

	public String getChecksum() {
		return checksum;
	}

	public String getSize() {
		return size;
	}

	public List<ArduinoBoard> getBoards() {
		if (isInstalled() && boardsProperties == null) {
			Properties boardProps = new Properties();

			try (InputStream is = new FileInputStream(getInstallPath().resolve("boards.txt").toFile()); //$NON-NLS-1$
					Reader reader = new InputStreamReader(is, "UTF-8")) { //$NON-NLS-1$
				boardProps.load(reader);
			} catch (IOException e) {
				Activator.log(e);
			}

			boardsProperties = new HierarchicalProperties(boardProps);

			// Replace the boards with a real ones
			boards = new ArrayList<>();
			for (Map.Entry<String, HierarchicalProperties> entry : boardsProperties.getChildren().entrySet()) {
				if (entry.getValue().getChild("name") != null) { //$NON-NLS-1$
					// assume things with names are boards
					boards.add(new ArduinoBoard(entry.getKey(), entry.getValue()).setOwners(this));
				}
			}

			// Build the menu
			HierarchicalProperties menuProp = boardsProperties.getChild("menu"); //$NON-NLS-1$
			if (menuProp != null) {
				for (Map.Entry<String, HierarchicalProperties> entry : menuProp.getChildren().entrySet()) {
					menus.put(entry.getKey(), entry.getValue().getValue());
				}
			}
		}
		return boards;
	}

	public HierarchicalProperties getBoardsProperties() {
		return boardsProperties;
	}

	public ArduinoBoard getBoard(String name) throws CoreException {
		for (ArduinoBoard board : getBoards()) {
			if (name.equals(board.getName())) {
				return board;
			}
		}
		return null;
	}

	public String getMenuText(String id) {
		return menus.get(id);
	}

	public List<ToolDependency> getToolsDependencies() {
		return toolsDependencies;
	}

	public ArduinoTool getTool(String name) throws CoreException {
		for (ToolDependency toolDep : toolsDependencies) {
			if (toolDep.getName().equals(name)) {
				return toolDep.getTool();
			}
		}
		return null;
	}

	public Properties getPlatformProperties() throws CoreException {
		if (platformProperties == null) {
			platformProperties = new Properties();
			try (BufferedReader reader = new BufferedReader(
					new FileReader(getInstallPath().resolve("platform.txt").toFile()))) { //$NON-NLS-1$
				// There are regex's here and need to preserve the \'s
				StringBuffer buffer = new StringBuffer();
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					buffer.append(line.replace("\\", "\\\\")); //$NON-NLS-1$ //$NON-NLS-2$
					buffer.append('\n');
				}
				try (Reader reader1 = new StringReader(buffer.toString())) {
					platformProperties.load(reader1);
				}
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Loading platform.txt", e)); //$NON-NLS-1$
			}
		}
		return platformProperties;
	}

	public boolean isInstalled() {
		return getInstallPath().resolve("boards.txt").toFile().exists(); //$NON-NLS-1$
	}

	public Path getInstallPath() {
		// TODO remove migration in Neon
		Path oldPath = ArduinoPreferences.getArduinoHome().resolve("hardware").resolve(pkg.getName()) //$NON-NLS-1$
				.resolve(architecture).resolve(version);
		Path newPath = getPackage().getInstallPath().resolve("hardware").resolve(pkg.getName()).resolve(architecture) //$NON-NLS-1$
				.resolve(version);
		if (Files.exists(oldPath)) {
			try {
				Files.createDirectories(newPath.getParent());
				Files.move(oldPath, newPath);
				for (Path parent = oldPath.getParent(); parent != null; parent = parent.getParent()) {
					if (Files.newDirectoryStream(parent).iterator().hasNext()) {
						break;
					} else {
						Files.delete(parent);
					}
				}
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		return newPath;
	}

	public List<Path> getIncludePath() {
		Path installPath = getInstallPath();
		return Arrays.asList(installPath.resolve("cores/{build.core}"), //$NON-NLS-1$
				installPath.resolve("variants/{build.variant}")); //$NON-NLS-1$
	}

	private void getSources(Collection<String> sources, Path dir, boolean recurse) {
		for (File file : dir.toFile().listFiles()) {
			if (file.isDirectory()) {
				if (recurse) {
					getSources(sources, file.toPath(), recurse);
				}
			} else {
				if (ArduinoBuildConfiguration.isSource(file.getName())) {
					sources.add(ArduinoBuildConfiguration.pathString(file.toPath()));
				}
			}
		}
	}

	public Collection<String> getSources(String core, String variant) {
		List<String> sources = new ArrayList<>();
		Path srcPath = getInstallPath().resolve("cores").resolve(core); //$NON-NLS-1$
		if (srcPath.toFile().isDirectory()) {
			getSources(sources, srcPath, true);
		}
		Path variantPath = getInstallPath().resolve("variants").resolve(variant); //$NON-NLS-1$
		if (variantPath.toFile().isDirectory()) {
			getSources(sources, variantPath, true);
		}
		return sources;
	}

	private void initLibraries() throws CoreException {
		libraries = new HashMap<>();
		File[] libraryDirs = getInstallPath().resolve("libraries").toFile().listFiles(); //$NON-NLS-1$
		if (libraryDirs != null) {
			for (File libraryDir : libraryDirs) {
				Path propsPath = libraryDir.toPath().resolve("library.properties"); //$NON-NLS-1$
				if (propsPath.toFile().exists()) {
					try {
						ArduinoLibrary lib = new ArduinoLibrary(propsPath);
						libraries.put(lib.getName(), lib);
					} catch (IOException e) {
						throw new CoreException(
								new Status(IStatus.ERROR, Activator.getId(), "Loading " + propsPath, e)); //$NON-NLS-1$
					}
				}
			}
		}
	}

	public synchronized Collection<ArduinoLibrary> getLibraries() throws CoreException {
		if (libraries == null && isInstalled()) {
			initLibraries();
		}
		return libraries.values();
	}

	public synchronized ArduinoLibrary getLibrary(String name) throws CoreException {
		if (libraries == null && isInstalled()) {
			initLibraries();
		}
		return libraries != null ? libraries.get(name) : null;
	}

	public IStatus install(IProgressMonitor monitor) {
		// Check if we're installed already
		if (isInstalled()) {
			try {
				ArduinoManager.recursiveDelete(getInstallPath());
			} catch (IOException e) {
				// just log it, shouldn't break the install
				Activator.log(e);
			}
		}

		// Install the tools
		for (ToolDependency toolDep : toolsDependencies) {
			IStatus status = toolDep.install(monitor);
			if (!status.isOK()) {
				return status;
			}
		}

		// On Windows install make from bintray
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			try {
				Path makePath = ArduinoPreferences.getArduinoHome().resolve("make.exe"); //$NON-NLS-1$
				if (!makePath.toFile().exists()) {
					Files.createDirectories(makePath.getParent());
					URL makeUrl = new URL("https://bintray.com/artifact/download/cdtdoug/tools/make.exe"); //$NON-NLS-1$
					Files.copy(makeUrl.openStream(), makePath);
					makePath.toFile().setExecutable(true, false);
				}
			} catch (IOException e) {
				return new Status(IStatus.ERROR, Activator.getId(), Messages.ArduinoPlatform_0, e);
			}
		}

		// Download platform archive
		IStatus status = ArduinoManager.downloadAndInstall(url, archiveFileName, getInstallPath(), monitor);
		if (!status.isOK()) {
			return status;
		}

		return Status.OK_STATUS;
	}

	public IStatus uninstall(IProgressMonitor monitor) {
		try {
			ArduinoManager.recursiveDelete(getInstallPath());
			// TODO delete tools that aren't needed any more
			return Status.OK_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.getId(), Messages.ArduinoPlatform_1, e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pkg == null) ? 0 : pkg.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArduinoPlatform other = (ArduinoPlatform) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pkg == null) {
			if (other.pkg != null)
				return false;
		} else if (!pkg.equals(other.pkg))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
