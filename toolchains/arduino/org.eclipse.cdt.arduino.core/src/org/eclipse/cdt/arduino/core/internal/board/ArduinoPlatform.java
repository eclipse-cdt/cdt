/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;
import org.eclipse.cdt.arduino.core.internal.LinkedProperties;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

public class ArduinoPlatform {

	// JSON fields
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
	// end JSON fields

	private Path installPath;
	private ArduinoPackage pkg;
	private HierarchicalProperties boardsProperties;
	private HierarchicalProperties platformProperties;
	private HierarchicalProperties programmerProperties;
	private Map<String, String> menus = new HashMap<>();
	private Map<String, ArduinoLibrary> libraries;

	void init(ArduinoPackage pkg) {
		this.pkg = pkg;

		for (ArduinoBoard board : boards) {
			if (board != null) {
				board.setOwners(this);
			}
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
		return version.replace('+', '_');
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

	public void setPlatformProperties(HierarchicalProperties platformProperties) {
		this.platformProperties = platformProperties;
	}

	public List<ArduinoBoard> getBoards() {
		if (boardsProperties == null) {
			LinkedProperties boardProps = new LinkedProperties();

			if (Files.exists(getInstallPath())) {
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
		}
		return boards;
	}

	public HierarchicalProperties getBoardsProperties() {
		return boardsProperties;
	}

	public ArduinoBoard getBoard(String id) throws CoreException {
		for (ArduinoBoard board : getBoards()) {
			if (id.equals(board.getId())) {
				return board;
			}
		}
		return null;
	}

	public ArduinoBoard getBoardByName(String name) throws CoreException {
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

	public HierarchicalProperties getPlatformProperties() throws CoreException {
		if (platformProperties == null) {
			LinkedProperties rawPlatformProps = new LinkedProperties();
			try (BufferedReader reader = new BufferedReader(
					new FileReader(getInstallPath().resolve("platform.txt").toFile()))) { //$NON-NLS-1$
				// There are regex's here and need to preserve the \'s
				StringBuilder buffer = new StringBuilder();
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					buffer.append(line.replace("\\", "\\\\")); //$NON-NLS-1$ //$NON-NLS-2$
					buffer.append('\n');
				}
				try (Reader reader1 = new StringReader(buffer.toString())) {
					rawPlatformProps.load(reader1);
				}
			} catch (IOException e) {
				throw Activator.coreException(e);
			}
			platformProperties = new HierarchicalProperties(rawPlatformProps);
		}
		return platformProperties;
	}

	public HierarchicalProperties getProgrammers() throws CoreException {
		if (programmerProperties == null) {
			LinkedProperties props = new LinkedProperties();
			Path programmersTxt = getInstallPath().resolve("programmers.txt"); //$NON-NLS-1$
			if (Files.exists(programmersTxt)) {
				try (FileInputStream in = new FileInputStream(programmersTxt.toFile())) {
					props.load(in);
					programmerProperties = new HierarchicalProperties(props);
				} catch (IOException e) {
					throw Activator.coreException(e);
				}
			} else {
				// TODO for now, grab the one from the arduino package
				ArduinoManager manager = Activator.getService(ArduinoManager.class);
				ArduinoPackage arduinoPkg = manager.getPackage("arduino"); //$NON-NLS-1$
				if (arduinoPkg != null) {
					ArduinoPlatform arduinoPlat = arduinoPkg.getInstalledPlatform(getArchitecture());
					if (arduinoPlat != null) {
						programmerProperties = arduinoPlat.getProgrammers();
					}
				}
			}
		}
		return programmerProperties;
	}

	public Path getInstallPath() {
		if (installPath == null) {
			Path oldPath = getPackage().getInstallPath().resolve("hardware").resolve(getPackage().getName()) //$NON-NLS-1$
					.resolve(getArchitecture()).resolve(getVersion());
			if (Files.exists(oldPath)) {
				installPath = oldPath;
			} else {
				installPath = getPackage().getInstallPath().resolve("hardware").resolve(getArchitecture()) //$NON-NLS-1$
						.resolve(getVersion());
			}
		}
		return installPath;
	}

	private void initLibraries() throws CoreException {
		if (libraries == null) {
			libraries = new HashMap<>();
			if (Files.exists(getInstallPath())) {
				File[] libraryDirs = getInstallPath().resolve("libraries").toFile().listFiles(); //$NON-NLS-1$
				if (libraryDirs != null) {
					for (File libraryDir : libraryDirs) {
						Path propsPath = libraryDir.toPath().resolve("library.properties"); //$NON-NLS-1$
						if (propsPath.toFile().exists()) {
							ArduinoLibrary lib = new ArduinoLibrary(propsPath, this);
							libraries.put(lib.getName(), lib);
						}
					}
				}
			}
		}
	}

	public synchronized Collection<ArduinoLibrary> getLibraries() throws CoreException {
		initLibraries();
		return libraries.values();
	}

	public synchronized ArduinoLibrary getLibrary(String name) throws CoreException {
		initLibraries();
		return libraries != null ? libraries.get(name) : null;
	}

	public void install(IProgressMonitor monitor) throws CoreException {
		int work = 1 + toolsDependencies.size();

		boolean exists = Files.exists(getInstallPath());
		if (exists)
			work++;

		Path makePath = ArduinoPreferences.getArduinoHome().resolve("make.exe"); //$NON-NLS-1$
		boolean needMake = Platform.getOS().equals(Platform.OS_WIN32) && !Files.exists(makePath);
		if (needMake)
			work++;

		SubMonitor sub = SubMonitor.convert(monitor, work);

		// Check if we're installed already
		if (exists) {
			try {
				sub.setTaskName(String.format("Removing old package %s", getName()));
				ArduinoManager.recursiveDelete(getInstallPath());
				sub.worked(1);
			} catch (IOException e) {
				// just log it, shouldn't break the install
				Activator.log(e);
			}
		}

		// Install the tools
		for (ToolDependency toolDep : toolsDependencies) {
			sub.setTaskName(String.format("Installing tool %s", toolDep.getName()));
			toolDep.install(monitor);
			sub.worked(1);
		}

		// On Windows install make from bintray
		if (needMake) {
			try {
				sub.setTaskName("Installing make");
				Files.createDirectories(makePath.getParent());
				URL makeUrl = new URL("https://bintray.com/artifact/download/cdtdoug/tools/make.exe"); //$NON-NLS-1$
				Files.copy(makeUrl.openStream(), makePath);
				makePath.toFile().setExecutable(true, false);
				sub.worked(1);
			} catch (IOException e) {
				throw Activator.coreException(e);
			}
		}

		// Download platform archive
		sub.setTaskName(String.format("Downloading and installing %s", getName()));
		try {
			ArduinoManager.downloadAndInstall(url, archiveFileName, getInstallPath(), monitor);
		} catch (IOException e) {
			throw Activator.coreException(e);
		}
		sub.done();

		pkg.platformInstalled(this);
	}

	public IStatus uninstall(IProgressMonitor monitor) {
		try {
			ArduinoManager.recursiveDelete(getInstallPath());
			pkg.platformUninstalled(this);
			// TODO delete tools that aren't needed any more
			return Status.OK_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.getId(), Messages.ArduinoPlatform_1, e);
		}
	}

	@Override
	public String toString() {
		return name;
	}

	private String fixText(String text) {
		String fixed = text.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		fixed = fixed.replaceAll("<", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		fixed = fixed.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		return fixed;
	}

	public String toFormText() {
		StringBuilder text = new StringBuilder();

		text.append("<form>"); //$NON-NLS-1$
		text.append(String.format("<p><b>%s: %s</b></p>", "Package", getName())); //$NON-NLS-1$
		text.append(String.format("<p>%s: %s</p>", "Maintainer", fixText(getPackage().getMaintainer()))); //$NON-NLS-1$

		ArduinoHelp help = getPackage().getHelp();
		if (help != null && help.getOnline() != null) {
			text.append(String.format("<p><a href=\"%s\">%s</a></p>", help.getOnline(), "Online help")); //$NON-NLS-1$
		}

		text.append(String.format("<p>%s:</p>", "Supported boards")); //$NON-NLS-1$
		for (ArduinoBoard board : getBoards()) {
			text.append(String.format("<li>%s</li>", fixText(board.getName()))); //$NON-NLS-1$
		}

		text.append("</form>"); //$NON-NLS-1$

		return text.toString();
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
