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
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfiguration;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ArduinoManager {

	// Build tool ids
	public static final String BOARD_OPTION_ID = "org.eclipse.cdt.arduino.option.board"; //$NON-NLS-1$
	public static final String PLATFORM_OPTION_ID = "org.eclipse.cdt.arduino.option.platform"; //$NON-NLS-1$
	public static final String PACKAGE_OPTION_ID = "org.eclipse.cdt.arduino.option.package"; //$NON-NLS-1$
	public static final String AVR_TOOLCHAIN_ID = "org.eclipse.cdt.arduino.toolChain.avr"; //$NON-NLS-1$

	public static final String LIBRARIES_URL = "http://downloads.arduino.cc/libraries/library_index.json"; //$NON-NLS-1$

	private List<PackageIndex> packageIndices;
	private LibraryIndex libraryIndex;

	public void loadIndices() {
		new Job(Messages.ArduinoBoardManager_0) {
			protected IStatus run(IProgressMonitor monitor) {
				synchronized (ArduinoManager.this) {
					String[] boardUrls = ArduinoPreferences.getBoardUrls().split("\n"); //$NON-NLS-1$
					packageIndices = new ArrayList<>(boardUrls.length);
					for (String boardUrl : boardUrls) {
						loadPackageIndex(boardUrl, true);
					}

					loadLibraryIndex(true);
					return Status.OK_STATUS;
				}
			}
		}.schedule();
	}

	private void loadPackageIndex(String url, boolean download) {
		try {
			URL packageUrl = new URL(url.trim());
			Path packagePath = ArduinoPreferences.getArduinoHome()
					.resolve(Paths.get(packageUrl.getPath()).getFileName());
			File packageFile = packagePath.toFile();
			if (download) {
				Files.createDirectories(ArduinoPreferences.getArduinoHome());
				Files.copy(packageUrl.openStream(), packagePath, StandardCopyOption.REPLACE_EXISTING);
			}
			if (packageFile.exists()) {
				try (Reader reader = new FileReader(packageFile)) {
					PackageIndex index = new Gson().fromJson(reader, PackageIndex.class);
					index.setOwners(ArduinoManager.this);
					packageIndices.add(index);
				}
			}
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	public synchronized List<PackageIndex> getPackageIndices() {
		if (packageIndices == null) {
			String[] boardUrls = ArduinoPreferences.getBoardUrls().split("\n"); //$NON-NLS-1$
			packageIndices = new ArrayList<>(boardUrls.length);
			for (String boardUrl : boardUrls) {
				loadPackageIndex(boardUrl, false);
			}
		}
		return packageIndices;
	}

	public void loadLibraryIndex(boolean download) {
		try {
			URL librariesUrl = new URL(LIBRARIES_URL);
			Path librariesPath = ArduinoPreferences.getArduinoHome()
					.resolve(Paths.get(librariesUrl.getPath()).getFileName());
			File librariesFile = librariesPath.toFile();
			if (download) {
				Files.createDirectories(ArduinoPreferences.getArduinoHome());
				Files.copy(librariesUrl.openStream(), librariesPath, StandardCopyOption.REPLACE_EXISTING);
			}
			if (librariesFile.exists()) {
				try (Reader reader = new FileReader(librariesFile)) {
					libraryIndex = new Gson().fromJson(reader, LibraryIndex.class);
					libraryIndex.resolve();
				}
			}
		} catch (IOException e) {
			Activator.log(e);
		}

	}

	public LibraryIndex getLibraryIndex() throws CoreException {
		if (libraryIndex == null) {
			loadLibraryIndex(false);
		}
		return libraryIndex;
	}

	public ArduinoBoard getBoard(String boardName, String platformName, String packageName) throws CoreException {
		for (PackageIndex index : getPackageIndices()) {
			ArduinoPackage pkg = index.getPackage(packageName);
			if (pkg != null) {
				ArduinoPlatform platform = pkg.getPlatform(platformName);
				if (platform != null) {
					ArduinoBoard board = platform.getBoard(boardName);
					if (board != null) {
						return board;
					}
				}
			}
		}
		return null;
	}

	public List<ArduinoBoard> getInstalledBoards() throws CoreException {
		List<ArduinoBoard> boards = new ArrayList<>();
		for (PackageIndex index : getPackageIndices()) {
			for (ArduinoPackage pkg : index.getPackages()) {
				for (ArduinoPlatform platform : pkg.getInstalledPlatforms().values()) {
					boards.addAll(platform.getBoards());
				}
			}
		}
		return boards;
	}

	public ArduinoPackage getPackage(String packageName) throws CoreException {
		for (PackageIndex index : getPackageIndices()) {
			ArduinoPackage pkg = index.getPackage(packageName);
			if (pkg != null) {
				return pkg;
			}
		}
		return null;
	}

	public ArduinoTool getTool(String packageName, String toolName, String version) throws CoreException {
		for (PackageIndex index : getPackageIndices()) {
			ArduinoPackage pkg = index.getPackage(packageName);
			if (pkg != null) {
				ArduinoTool tool = pkg.getTool(toolName, version);
				if (tool != null) {
					return tool;
				}
			}
		}
		return null;
	}

	private static final String LIBRARIES = "libraries"; //$NON-NLS-1$

	private IEclipsePreferences getSettings(IProject project) {
		return (IEclipsePreferences) new ProjectScope(project).getNode(Activator.getId());
	}

	public Collection<ArduinoLibrary> getLibraries(IProject project) throws CoreException {
		IEclipsePreferences settings = getSettings(project);
		String librarySetting = settings.get(LIBRARIES, "[]"); //$NON-NLS-1$
		Type stringSet = new TypeToken<Set<String>>() {
		}.getType();
		Set<String> libraryNames = new Gson().fromJson(librarySetting, stringSet);
		LibraryIndex index = Activator.getService(ArduinoManager.class).getLibraryIndex();

		ArduinoPlatform platform = project.getActiveBuildConfig().getAdapter(ArduinoBuildConfiguration.class).getBoard()
				.getPlatform();
		List<ArduinoLibrary> libraries = new ArrayList<>(libraryNames.size());
		for (String name : libraryNames) {
			ArduinoLibrary lib = index.getLibrary(name);
			if (lib == null) {
				lib = platform.getLibrary(name);
			}
			if (lib != null) {
				libraries.add(lib);
			}
		}
		return libraries;
	}

	public void setLibraries(final IProject project, final Collection<ArduinoLibrary> libraries) throws CoreException {
		List<String> libraryNames = new ArrayList<>(libraries.size());
		for (ArduinoLibrary library : libraries) {
			libraryNames.add(library.getName());
		}
		IEclipsePreferences settings = getSettings(project);
		settings.put(LIBRARIES, new Gson().toJson(libraryNames));
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}

		new Job(Messages.ArduinoManager_0) {
			protected IStatus run(IProgressMonitor monitor) {
				MultiStatus mstatus = new MultiStatus(Activator.getId(), 0, Messages.ArduinoManager_1, null);
				for (ArduinoLibrary library : libraries) {
					IStatus status = library.install(monitor);
					if (!status.isOK()) {
						mstatus.add(status);
					}
				}

				// Clear the scanner info caches to pick up new includes
				try {
					for (IBuildConfiguration config : project.getBuildConfigs()) {
						ArduinoBuildConfiguration arduinoConfig = config.getAdapter(ArduinoBuildConfiguration.class);
						arduinoConfig.clearScannerInfoCache();
					}
				} catch (CoreException e) {
					mstatus.add(e.getStatus());
				}
				return mstatus;
			}
		}.schedule();
	}

	public static IStatus downloadAndInstall(String url, String archiveFileName, Path installPath,
			IProgressMonitor monitor) {
		Exception error = null;
		for (int retries = 3; retries > 0 && !monitor.isCanceled(); --retries) {
			try {
				URL dl = new URL(url);
				Path dlDir = ArduinoPreferences.getArduinoHome().resolve("downloads"); //$NON-NLS-1$
				Files.createDirectories(dlDir);
				Path archivePath = dlDir.resolve(archiveFileName);
				URLConnection conn = dl.openConnection();
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(10000);
				Files.copy(conn.getInputStream(), archivePath, StandardCopyOption.REPLACE_EXISTING);

				boolean isWin = Platform.getOS().equals(Platform.OS_WIN32);

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

					for (ArchiveEntry entry = archiveIn.getNextEntry(); entry != null; entry = archiveIn
							.getNextEntry()) {
						if (entry.isDirectory()) {
							continue;
						}

						// Magic file for git tarballs
						Path path = Paths.get(entry.getName());
						if (path.endsWith("pax_global_header")) { //$NON-NLS-1$
							continue;
						}

						// Strip the first directory of the path
						Path entryPath = installPath.resolve(path.subpath(1, path.getNameCount()));

						Files.createDirectories(entryPath.getParent());

						if (entry instanceof TarArchiveEntry) {
							TarArchiveEntry tarEntry = (TarArchiveEntry) entry;
							if (tarEntry.isLink()) {
								Path linkPath = Paths.get(tarEntry.getLinkName());
								linkPath = installPath.resolve(linkPath.subpath(1, linkPath.getNameCount()));
								Files.deleteIfExists(entryPath);
								Files.createSymbolicLink(entryPath, entryPath.getParent().relativize(linkPath));
							} else if (tarEntry.isSymbolicLink()) {
								Path linkPath = Paths.get(tarEntry.getLinkName());
								Files.deleteIfExists(entryPath);
								Files.createSymbolicLink(entryPath, linkPath);
							} else {
								Files.copy(archiveIn, entryPath, StandardCopyOption.REPLACE_EXISTING);
							}
							if (!isWin && !tarEntry.isSymbolicLink()) {
								int mode = tarEntry.getMode();
								Files.setPosixFilePermissions(entryPath, toPerms(mode));
							}
						} else {
							Files.copy(archiveIn, entryPath, StandardCopyOption.REPLACE_EXISTING);
						}
					}
				} finally {
					if (archiveIn != null) {
						archiveIn.close();
					}
				}

				return Status.OK_STATUS;
			} catch (IOException | CompressorException | ArchiveException e) {
				error = e;
				// retry
			}
		}
		// out of retries
		return new Status(IStatus.ERROR, Activator.getId(), Messages.ArduinoManager_2, error);
	}

	public static int compareVersions(String version1, String version2) {
		if (version1 == null) {
			return version2 == null ? 0 : -1;
		}

		if (version2 == null) {
			return 1;
		}

		String[] v1 = version1.split("\\."); //$NON-NLS-1$
		String[] v2 = version2.split("\\."); //$NON-NLS-1$
		for (int i = 0; i < Math.max(v1.length, v2.length); ++i) {
			if (v1.length <= i) {
				return v2.length < i ? 0 : -1;
			}

			if (v2.length <= i) {
				return 1;
			}

			try {
				int vi1 = Integer.parseInt(v1[i]);
				int vi2 = Integer.parseInt(v2[i]);
				if (vi1 < vi2) {
					return -1;
				}

				if (vi1 > vi2) {
					return 1;
				}
			} catch (NumberFormatException e) {
				// not numbers, do string compares
				int c = v1[i].compareTo(v2[i]);
				if (c < 0) {
					return -1;
				}
				if (c > 0) {
					return 1;
				}
			}
		}

		return 0;
	}

	private static Set<PosixFilePermission> toPerms(int mode) {
		Set<PosixFilePermission> perms = new HashSet<>();
		if ((mode & 0400) != 0) {
			perms.add(PosixFilePermission.OWNER_READ);
		}
		if ((mode & 0200) != 0) {
			perms.add(PosixFilePermission.OWNER_WRITE);
		}
		if ((mode & 0100) != 0) {
			perms.add(PosixFilePermission.OWNER_EXECUTE);
		}
		if ((mode & 0040) != 0) {
			perms.add(PosixFilePermission.GROUP_READ);
		}
		if ((mode & 0020) != 0) {
			perms.add(PosixFilePermission.GROUP_WRITE);
		}
		if ((mode & 0010) != 0) {
			perms.add(PosixFilePermission.GROUP_EXECUTE);
		}
		if ((mode & 0004) != 0) {
			perms.add(PosixFilePermission.OTHERS_READ);
		}
		if ((mode & 0002) != 0) {
			perms.add(PosixFilePermission.OTHERS_WRITE);
		}
		if ((mode & 0001) != 0) {
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
		}
		return perms;
	}

	public static void recursiveDelete(Path directory) throws IOException {
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});
	}
}
