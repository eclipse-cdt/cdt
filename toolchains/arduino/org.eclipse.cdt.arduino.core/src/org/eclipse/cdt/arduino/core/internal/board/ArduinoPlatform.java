/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
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

	private transient ArduinoPackage pkg;
	private transient HierarchicalProperties boardsFile;

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

	public List<ArduinoBoard> getBoards() throws CoreException {
		if (isInstalled() && boardsFile == null) {
			Properties boardProps = new Properties();
			try (Reader reader = new FileReader(getInstallPath().resolve("boards.txt").toFile())) { //$NON-NLS-1$
				boardProps.load(reader);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Loading boards.txt", e));
			}

			boardsFile = new HierarchicalProperties(boardProps);

			// Replace the boards with a real ones
			boards = new ArrayList<>();
			for (Map.Entry<String, HierarchicalProperties> entry : boardsFile.getChildren().entrySet()) {
				if (entry.getValue().getChild("name") != null) { //$NON-NLS-1$
					// assume things with names are boards
					boards.add(new ArduinoBoard(entry.getKey(), entry.getValue()).setOwners(this));
				}
			}
		}
		return boards;
	}

	public ArduinoBoard getBoard(String name) throws CoreException {
		for (ArduinoBoard board : getBoards()) {
			if (name.equals(board.getName())) {
				return board;
			}
		}
		return null;
	}

	public List<ToolDependency> getToolsDependencies() {
		return toolsDependencies;
	}

	public Properties getPlatformProperties() throws CoreException {
		Properties properties = new Properties();
		try (Reader reader = new FileReader(getInstallPath().resolve("boards.txt").toFile())) { //$NON-NLS-1$
			properties.load(reader);
			return properties;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Loading platform.txt", e));
		}
	}

	public boolean isInstalled() {
		return getInstallPath().resolve("boards.txt").toFile().exists(); //$NON-NLS-1$
	}

	private Path getInstallPath() {
		return ArduinoPreferences.getArduinoHome().resolve("hardware").resolve(pkg.getName()).resolve(architecture) //$NON-NLS-1$
				.resolve(version);
	}

	public IStatus install(IProgressMonitor monitor) {
		// Check if we're installed already
		if (isInstalled()) {
			return Status.OK_STATUS;
		}

		// Download platform archive
		IStatus status = ArduinoBoardManager.downloadAndInstall(url, archiveFileName, getInstallPath(), monitor);
		if (!status.isOK()) {
			return status;
		}

		// Install the tools
		MultiStatus mstatus = null;
		for (ToolDependency toolDep : toolsDependencies) {
			status = toolDep.install(monitor);
			if (!status.isOK()) {
				if (mstatus == null) {
					mstatus = new MultiStatus(status.getPlugin(), status.getCode(), status.getMessage(),
							status.getException());
				} else {
					mstatus.add(status);
				}
			}
		}

		// TODO on Windows install make from equations.org

		return mstatus != null ? mstatus : Status.OK_STATUS;
	}

}
