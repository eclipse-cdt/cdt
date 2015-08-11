/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.gson.Gson;

// Closeable isn't API yet but it's recommended.
@SuppressWarnings("restriction")
public class ArduinoBoardManager {

	public static final ArduinoBoardManager instance = new ArduinoBoardManager();

	// Build tool ids
	public static final String BOARD_OPTION_ID = "org.eclipse.cdt.arduino.option.board"; //$NON-NLS-1$
	public static final String PLATFORM_OPTION_ID = "org.eclipse.cdt.arduino.option.platform"; //$NON-NLS-1$
	public static final String PACKAGE_OPTION_ID = "org.eclipse.cdt.arduino.option.package"; //$NON-NLS-1$
	public static final String AVR_TOOLCHAIN_ID = "org.eclipse.cdt.arduino.toolChain.avr"; //$NON-NLS-1$

	private Path packageIndexPath = ArduinoPreferences.getArduinoHome().resolve("package_index.json"); //$NON-NLS-1$
	private PackageIndex packageIndex;

	public ArduinoBoardManager() {
		new Job(Messages.ArduinoBoardManager_0) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					URL url = new URL("http://downloads.arduino.cc/packages/package_index.json"); //$NON-NLS-1$
					Files.createDirectories(packageIndexPath.getParent());
					Files.copy(url.openStream(), packageIndexPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					return new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	public PackageIndex getPackageIndex() throws CoreException {
		if (packageIndex == null) {
			try (FileReader reader = new FileReader(packageIndexPath.toFile())) {
				packageIndex = new Gson().fromJson(reader, PackageIndex.class);
				packageIndex.setOwners(this);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Reading package index", e));
			}
		}
		return packageIndex;
	}

	public ArduinoBoard getBoard(String boardName, String platformName, String packageName) throws CoreException {
		return getPackageIndex().getPackage(packageName).getPlatform(platformName).getBoard(boardName);
	}

	public List<ArduinoBoard> getBoards() throws CoreException {
		List<ArduinoBoard> boards = new ArrayList<>();
		for (ArduinoPackage pkg : packageIndex.getPackages()) {
			for (ArduinoPlatform platform : pkg.getPlatforms()) {
				boards.addAll(platform.getBoards());
			}
		}
		return boards;
	}

	public ArduinoTool getTool(String packageName, String toolName, String version) {
		ArduinoPackage pkg = packageIndex.getPackage(packageName);
		return pkg != null ? pkg.getTool(toolName, version) : null;
	}

	public static IStatus downloadAndInstall(String url, String archiveFileName, Path installPath,
			IProgressMonitor monitor) {
		try {
			URL dl = new URL(url);
			Path dlDir = ArduinoPreferences.getArduinoHome().resolve("downloads");
			Files.createDirectories(dlDir);
			Path archivePath = dlDir.resolve(archiveFileName);
			Files.copy(dl.openStream(), archivePath, StandardCopyOption.REPLACE_EXISTING);

			// extract
			ArchiveInputStream archiveIn = null;
			try {
				String compressor = null;
				String archiver = null;
				if (archiveFileName.endsWith("tar.bz2")) { //$NON-NLS-1$
					compressor = CompressorStreamFactory.BZIP2;
					archiver = ArchiveStreamFactory.TAR;
				} else if (archiveFileName.endsWith(".tar.gz") || archiveFileName.endsWith(".tgz")) { //$NON-NLS-1$ //$NON-NLS-2$
					compressor = CompressorStreamFactory.GZIP;
					archiver = ArchiveStreamFactory.TAR;
				} else if (archiveFileName.endsWith(".tar.xz")) { //$NON-NLS-1$
					compressor = CompressorStreamFactory.XZ;
					archiver = ArchiveStreamFactory.TAR;
				} else if (archiveFileName.endsWith(".zip")) { //$NON-NLS-1$
					archiver = ArchiveStreamFactory.ZIP;
				}

				InputStream in = new BufferedInputStream(new FileInputStream(archivePath.toFile()));
				if (compressor != null) {
					in = new CompressorStreamFactory().createCompressorInputStream(compressor, in);
				}
				archiveIn = new ArchiveStreamFactory().createArchiveInputStream(archiver, in);

				for (ArchiveEntry entry = archiveIn.getNextEntry(); entry != null; entry = archiveIn.getNextEntry()) {
					if (entry.isDirectory()) {
						continue;
					}

					// TODO check for soft links in tar files.
					Path entryPath = installPath.resolve(entry.getName());
					Files.createDirectories(entryPath.getParent());
					Files.copy(archiveIn, entryPath, StandardCopyOption.REPLACE_EXISTING);
				}
			} finally {
				if (archiveIn != null) {
					archiveIn.close();
				}
			}

			// Fix up directory
			File[] children = installPath.toFile().listFiles();
			if (children.length == 1 && children[0].isDirectory()) {
				// make that directory the install path
				Path childPath = children[0].toPath();
				Path tmpPath = installPath.getParent().resolve("_t"); //$NON-NLS-1$
				Files.move(childPath, tmpPath);
				Files.delete(installPath);
				Files.move(tmpPath, installPath);
			}
			return Status.OK_STATUS;
		} catch (IOException | CompressorException | ArchiveException e) {
			return new Status(IStatus.ERROR, Activator.getId(), "Installing Platform", e);
		}
	}
}
