/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class ArduinoManager {

	// Build tool ids
	public static final String BOARD_OPTION_ID = "org.eclipse.cdt.arduino.option.board"; //$NON-NLS-1$
	public static final String PLATFORM_OPTION_ID = "org.eclipse.cdt.arduino.option.platform"; //$NON-NLS-1$
	public static final String PACKAGE_OPTION_ID = "org.eclipse.cdt.arduino.option.package"; //$NON-NLS-1$
	public static final String AVR_TOOLCHAIN_ID = "org.eclipse.cdt.arduino.toolChain.avr"; //$NON-NLS-1$

	public static final String LIBRARIES_URL = "http://downloads.arduino.cc/libraries/library_index.json"; //$NON-NLS-1$
	public static final String LIBRARIES_FILE = "library_index.json"; //$NON-NLS-1$

	private static final String LIBRARIES = "libraries"; //$NON-NLS-1$

	// arduinocdt install properties
	private static final String VERSION_KEY = "version"; //$NON-NLS-1$
	private static final String ACCEPTED_KEY = "accepted"; //$NON-NLS-1$
	private static final String VERSION = "2"; //$NON-NLS-1$

	private Properties props;

	private Path arduinoHome = ArduinoPreferences.getArduinoHome();
	private Map<String, ArduinoPackage> packages;
	private Map<String, ArduinoLibrary> installedLibraries;

	private Path getVersionFile() {
		return ArduinoPreferences.getArduinoHome().resolve(".version"); //$NON-NLS-1$
	}

	private synchronized void init() throws CoreException {
		if (!arduinoHome.equals(ArduinoPreferences.getArduinoHome())) {
			// Arduino Home changed, reset.
			props = null;
			packages = null;
			installedLibraries = null;
			arduinoHome = ArduinoPreferences.getArduinoHome();
		}

		if (props == null) {
			if (!Files.exists(ArduinoPreferences.getArduinoHome())) {
				try {
					Files.createDirectories(ArduinoPreferences.getArduinoHome());
				} catch (IOException e) {
					throw Activator.coreException(e);
				}
			}

			props = new Properties();
			Path propsFile = getVersionFile();
			if (Files.exists(propsFile)) {
				try (FileReader reader = new FileReader(propsFile.toFile())) {
					props.load(reader);
				} catch (IOException e) {
					throw Activator.coreException(e);
				}
			}

			// See if we need a conversion
			int version = Integer.parseInt(props.getProperty(VERSION_KEY, "1")); //$NON-NLS-1$
			if (version < Integer.parseInt(VERSION)) {
				props.setProperty(VERSION_KEY, VERSION);
				try (FileWriter writer = new FileWriter(getVersionFile().toFile())) {
					props.store(writer, ""); //$NON-NLS-1$
				} catch (IOException e) {
					throw Activator.coreException(e);
				}
			}
		}
	}

	public void convertLibrariesDir() throws CoreException {
		Path librariesDir = ArduinoPreferences.getArduinoHome().resolve("libraries"); //$NON-NLS-1$
		if (!Files.isDirectory(librariesDir)) {
			return;
		}

		try {
			Path tmpDir = Files.createTempDirectory("alib"); //$NON-NLS-1$
			Path tmpLibDir = tmpDir.resolve("libraries"); //$NON-NLS-1$
			Files.move(librariesDir, tmpLibDir);
			Files.list(tmpLibDir).forEach(path -> {
				try {
					Optional<Path> latest = Files.list(path)
							.reduce((path1, path2) -> compareVersions(path1.getFileName().toString(),
									path2.getFileName().toString()) > 0 ? path1 : path2);
					if (latest.isPresent()) {
						Files.move(latest.get(), librariesDir.resolve(path.getFileName()));
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			recursiveDelete(tmpDir);
		} catch (RuntimeException | IOException e) {
			throw Activator.coreException(e);
		}

	}

	public boolean licenseAccepted() throws CoreException {
		init();
		return Boolean.getBoolean(props.getProperty(ACCEPTED_KEY, Boolean.FALSE.toString()));
	}

	public void acceptLicense() throws CoreException {
		init();
		props.setProperty(ACCEPTED_KEY, Boolean.TRUE.toString());
		try (FileWriter writer = new FileWriter(getVersionFile().toFile())) {
			props.store(writer, ""); //$NON-NLS-1$
		} catch (IOException e) {
			throw Activator.coreException(e);
		}
	}

	public Collection<ArduinoPlatform> getInstalledPlatforms() throws CoreException {
		List<ArduinoPlatform> platforms = new ArrayList<>();
		for (ArduinoPackage pkg : getPackages()) {
			platforms.addAll(pkg.getInstalledPlatforms());
		}
		return platforms;
	}

	public ArduinoPlatform getInstalledPlatform(String packageName, String architecture) throws CoreException {
		ArduinoPackage pkg = getPackage(packageName);
		return pkg != null ? pkg.getInstalledPlatform(architecture) : null;
	}

	public synchronized Collection<ArduinoPlatform> getAvailablePlatforms(IProgressMonitor monitor)
			throws CoreException {
		List<ArduinoPlatform> platforms = new ArrayList<>();
		URL[] urls = ArduinoPreferences.getBoardUrlList();
		SubMonitor sub = SubMonitor.convert(monitor, urls.length + 1);

		sub.beginTask("Downloading package descriptions", urls.length); //$NON-NLS-1$
		for (URL url : urls) {
			Path packagePath = ArduinoPreferences.getArduinoHome().resolve(Paths.get(url.getPath()).getFileName());
			try {
				Files.createDirectories(ArduinoPreferences.getArduinoHome());
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("User-Agent", //$NON-NLS-1$
						"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2"); //$NON-NLS-1$
				try (InputStream in = connection.getInputStream()) {
					Files.copy(in, packagePath, StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException e) {
				// Log and continue, URLs sometimes come and go
				Activator.log(e);
			}
			sub.worked(1);
		}

		sub.beginTask("Loading available packages", 1); //$NON-NLS-1$
		resetPackages();
		for (ArduinoPackage pkg : getPackages()) {
			platforms.addAll(pkg.getAvailablePlatforms());
		}
		sub.done();

		return platforms;
	}

	public synchronized Collection<ArduinoPlatform> getPlatformUpdates(IProgressMonitor monitor) throws CoreException {
		List<ArduinoPlatform> platforms = new ArrayList<>();
		URL[] urls = ArduinoPreferences.getBoardUrlList();
		SubMonitor sub = SubMonitor.convert(monitor, urls.length + 1);

		sub.beginTask("Downloading package descriptions", urls.length); //$NON-NLS-1$
		for (URL url : urls) {
			Path packagePath = ArduinoPreferences.getArduinoHome().resolve(Paths.get(url.getPath()).getFileName());
			try {
				Files.createDirectories(ArduinoPreferences.getArduinoHome());
				try (InputStream in = url.openStream()) {
					Files.copy(in, packagePath, StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException e) {
				throw Activator.coreException(String.format("Error loading %s", url.toString()), e); //$NON-NLS-1$
			}
			sub.worked(1);
		}

		sub.beginTask("Loading available package updates", 1); //$NON-NLS-1$
		resetPackages();
		for (ArduinoPackage pkg : getPackages()) {
			platforms.addAll(pkg.getPlatformUpdates());
		}
		sub.done();

		return platforms;
	}

	public void installPlatforms(Collection<ArduinoPlatform> platforms, IProgressMonitor monitor) throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, platforms.size());
		for (ArduinoPlatform platform : platforms) {
			sub.setTaskName(String.format("Installing %s %s", platform.getName(), platform.getVersion())); //$NON-NLS-1$
			platform.install(sub);
			sub.worked(1);
		}
		sub.done();
	}

	public void uninstallPlatforms(Collection<ArduinoPlatform> platforms, IProgressMonitor monitor) {
		SubMonitor sub = SubMonitor.convert(monitor, platforms.size());
		for (ArduinoPlatform platform : platforms) {
			sub.setTaskName(String.format("Uninstalling %s", platform.getName())); //$NON-NLS-1$
			platform.uninstall(sub);
			sub.worked(1);
		}
		sub.done();
	}

	public static List<ArduinoPlatform> getSortedPlatforms(Collection<ArduinoPlatform> platforms) {
		List<ArduinoPlatform> result = new ArrayList<>(platforms);
		Collections.sort(result, (plat1, plat2) -> {
			int c1;
			String p1 = plat1.getPackage().getName();
			String p2 = plat2.getPackage().getName();
			if (p1.equals(p2)) {
				c1 = 0;
			} else if (p1.equals("arduino")) {
				c1 = -1;
			} else if (p2.equals("arduino")) {
				c1 = 1;
			} else {
				c1 = plat1.getPackage().getName().compareToIgnoreCase(plat2.getPackage().getName());
			}

			if (c1 > 0) {
				return 1;
			} else if (c1 < 0) {
				return -1;
			} else {
				return plat1.getArchitecture().compareToIgnoreCase(plat2.getArchitecture());
			}
		});
		return result;
	}

	public static List<ArduinoLibrary> getSortedLibraries(Collection<ArduinoLibrary> libraries) {
		List<ArduinoLibrary> result = new ArrayList<>(libraries);
		Collections.sort(result, (lib1, lib2) -> {
			return lib1.getName().compareToIgnoreCase(lib2.getName());
		});
		return result;
	}

	private synchronized void initPackages() throws CoreException {
		init();
		if (packages == null) {
			packages = new HashMap<>();

			try {
				Files.list(ArduinoPreferences.getArduinoHome())
						.filter(path -> path.getFileName().toString().startsWith("package_")) //$NON-NLS-1$
						.forEach(path -> {
							try (Reader reader = new FileReader(path.toFile())) {
								PackageIndex index = new Gson().fromJson(reader, PackageIndex.class);
								for (ArduinoPackage pkg : index.getPackages()) {
									ArduinoPackage p = packages.get(pkg.getName());
									if (p == null) {
										pkg.init();
										packages.put(pkg.getName(), pkg);
									} else {
										p.merge(pkg);
									}
								}
							} catch (IOException e) {
								Activator.log(e);
							}
						});
			} catch (IOException e) {
				throw Activator.coreException(e);
			}
		}
	}

	private Collection<ArduinoPackage> getPackages() throws CoreException {
		initPackages();
		return packages.values();
	}

	public void resetPackages() {
		packages = null;
	}

	public ArduinoPackage getPackage(String packageName) throws CoreException {
		if (packageName == null) {
			return null;
		} else {
			initPackages();
			return packages.get(packageName);
		}
	}

	public Collection<ArduinoBoard> getInstalledBoards() throws CoreException {
		List<ArduinoBoard> boards = new ArrayList<>();
		for (ArduinoPlatform platform : getInstalledPlatforms()) {
			boards.addAll(platform.getBoards());
		}
		return boards;
	}

	public ArduinoBoard getBoard(String packageName, String architecture, String boardId) throws CoreException {
		for (ArduinoPlatform platform : getInstalledPlatforms()) {
			if (platform.getPackage().getName().equals(packageName)
					&& platform.getArchitecture().equals(architecture)) {
				return platform.getBoard(boardId);
			}
		}

		// For backwards compat, check platform name
		for (ArduinoPlatform platform : getInstalledPlatforms()) {
			if (platform.getPackage().getName().equals(packageName) && platform.getName().equals(architecture)) {
				return platform.getBoardByName(boardId);
			}
		}

		return null;
	}

	public ArduinoTool getTool(String packageName, String toolName, String version) {
		ArduinoPackage pkg = packages.get(packageName);
		return pkg != null ? pkg.getTool(toolName, version) : null;
	}

	private void initInstalledLibraries() throws CoreException {
		init();
		if (installedLibraries == null) {
			installedLibraries = new HashMap<>();

			Path librariesDir = ArduinoPreferences.getArduinoHome().resolve("libraries"); //$NON-NLS-1$
			if (Files.isDirectory(librariesDir)) {
				try {
					Files.find(librariesDir, 3,
							(path, attrs) -> path.getFileName().toString().equals("library.properties")) //$NON-NLS-1$
							.forEach(path -> {
								try {
									ArduinoLibrary library = new ArduinoLibrary(path);
									installedLibraries.put(library.getName(), library);
								} catch (CoreException e) {
									throw new RuntimeException(e);
								}
							});
				} catch (IOException e) {
					throw Activator.coreException(e);
				}
			}
		}
	}

	public Collection<ArduinoLibrary> getInstalledLibraries() throws CoreException {
		initInstalledLibraries();
		return installedLibraries.values();
	}

	public ArduinoLibrary getInstalledLibrary(String name) throws CoreException {
		initInstalledLibraries();
		return installedLibraries.get(name);
	}

	public Collection<ArduinoLibrary> getAvailableLibraries(IProgressMonitor monitor) throws CoreException {
		try {
			initInstalledLibraries();
			Map<String, ArduinoLibrary> libs = new HashMap<>();

			SubMonitor sub = SubMonitor.convert(monitor, "Downloading library index", 2);
			Path librariesPath = ArduinoPreferences.getArduinoHome().resolve(LIBRARIES_FILE);
			URL librariesUrl = new URL(LIBRARIES_URL);
			Files.createDirectories(ArduinoPreferences.getArduinoHome());
			Files.copy(librariesUrl.openStream(), librariesPath, StandardCopyOption.REPLACE_EXISTING);
			sub.worked(1);

			try (Reader reader = new FileReader(librariesPath.toFile())) {
				sub.setTaskName("Calculating available libraries");
				LibraryIndex libraryIndex = new Gson().fromJson(reader, LibraryIndex.class);
				for (ArduinoLibrary library : libraryIndex.getLibraries()) {
					String libraryName = library.getName();
					if (!installedLibraries.containsKey(libraryName)) {
						ArduinoLibrary current = libs.get(libraryName);
						if (current == null || compareVersions(library.getVersion(), current.getVersion()) > 0) {
							libs.put(libraryName, library);
						}
					}
				}
			}
			sub.done();
			return libs.values();
		} catch (IOException e) {
			throw Activator.coreException(e);
		}
	}

	public Collection<ArduinoLibrary> getLibraryUpdates(IProgressMonitor monitor) throws CoreException {
		try {
			initInstalledLibraries();
			Map<String, ArduinoLibrary> libs = new HashMap<>();

			SubMonitor sub = SubMonitor.convert(monitor, "Downloading library index", 2);
			Path librariesPath = ArduinoPreferences.getArduinoHome().resolve(LIBRARIES_FILE);
			URL librariesUrl = new URL(LIBRARIES_URL);
			Files.createDirectories(ArduinoPreferences.getArduinoHome());
			Files.copy(librariesUrl.openStream(), librariesPath, StandardCopyOption.REPLACE_EXISTING);
			sub.worked(1);

			try (Reader reader = new FileReader(librariesPath.toFile())) {
				sub.setTaskName("Calculating library updates");
				LibraryIndex libraryIndex = new Gson().fromJson(reader, LibraryIndex.class);
				for (ArduinoLibrary library : libraryIndex.getLibraries()) {
					String libraryName = library.getName();
					ArduinoLibrary installed = installedLibraries.get(libraryName);
					if (installed != null && compareVersions(library.getVersion(), installed.getVersion()) > 0) {
						ArduinoLibrary current = libs.get(libraryName);
						if (current == null || compareVersions(library.getVersion(), current.getVersion()) > 0) {
							libs.put(libraryName, library);
						}
					}
				}
			}
			sub.done();
			return libs.values();
		} catch (IOException e) {
			throw Activator.coreException(e);
		}
	}

	public void installLibraries(Collection<ArduinoLibrary> libraries, IProgressMonitor monitor) throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, libraries.size());
		for (ArduinoLibrary library : libraries) {
			sub.setTaskName(String.format("Installing %s", library.getName())); //$NON-NLS-1$
			library.install(sub);
			try {
				ArduinoLibrary newLibrary = new ArduinoLibrary(library.getInstallPath().resolve("library.properties")); //$NON-NLS-1$
				installedLibraries.put(newLibrary.getName(), newLibrary);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
			sub.worked(1);
		}
		sub.done();
	}

	public void uninstallLibraries(Collection<ArduinoLibrary> libraries, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, libraries.size());
		for (ArduinoLibrary library : libraries) {
			sub.setTaskName(String.format("Installing %s", library.getName())); //$NON-NLS-1$
			library.uninstall(sub);
			installedLibraries.remove(library.getName());
			sub.worked(1);
		}
		sub.done();
	}

	public Collection<ArduinoLibrary> getLibraries(IProject project) throws CoreException {
		initInstalledLibraries();
		IEclipsePreferences settings = getSettings(project);
		String librarySetting = settings.get(LIBRARIES, "[]"); //$NON-NLS-1$
		JsonArray libArray = new JsonParser().parse(librarySetting).getAsJsonArray();

		List<ArduinoLibrary> libraries = new ArrayList<>(libArray.size());
		for (JsonElement libElement : libArray) {
			if (libElement.isJsonPrimitive()) {
				String libName = libElement.getAsString();
				ArduinoLibrary lib = installedLibraries.get(libName);
				if (lib != null) {
					libraries.add(lib);
				}
			} else {
				JsonObject libObj = libElement.getAsJsonObject();
				String packageName = libObj.get("package").getAsString(); //$NON-NLS-1$
				String platformName = libObj.get("platform").getAsString(); //$NON-NLS-1$
				String libName = libObj.get("library").getAsString(); //$NON-NLS-1$
				ArduinoPackage pkg = getPackage(packageName);
				if (pkg != null) {
					ArduinoPlatform platform = pkg.getInstalledPlatform(platformName);
					if (platform != null) {
						ArduinoLibrary lib = platform.getLibrary(libName);
						if (lib != null) {
							libraries.add(lib);
						}
					}
				}
			}
		}
		return libraries;
	}

	public void setLibraries(final IProject project, final Collection<ArduinoLibrary> libraries) throws CoreException {
		JsonArray elements = new JsonArray();
		for (ArduinoLibrary library : libraries) {
			ArduinoPlatform platform = library.getPlatform();
			if (platform != null) {
				JsonObject libObj = new JsonObject();
				libObj.addProperty("package", platform.getPackage().getName()); //$NON-NLS-1$
				libObj.addProperty("platform", platform.getArchitecture()); //$NON-NLS-1$
				libObj.addProperty("library", library.getName()); //$NON-NLS-1$
				elements.add(libObj);
			} else {
				elements.add(new JsonPrimitive(library.getName()));
			}
		}
		IEclipsePreferences settings = getSettings(project);
		settings.put(LIBRARIES, new Gson().toJson(elements));
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			throw Activator.coreException(e);
		}
	}

	private IEclipsePreferences getSettings(IProject project) {
		return new ProjectScope(project).getNode(Activator.getId());
	}

	public static void downloadAndInstall(String url, String archiveFileName, Path installPath,
			IProgressMonitor monitor) throws IOException {
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
						Path entryPath;
						switch (path.getName(0).toString()) {
						case "i586":
						case "i686":
							// Cheat for Intel
							entryPath = installPath.resolve(path);
							break;
						default:
							entryPath = installPath.resolve(path.subpath(1, path.getNameCount()));
						}

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
				return;
			} catch (IOException | CompressorException | ArchiveException e) {
				error = e;
				// retry
			}
		}

		// out of retries
		if (error instanceof IOException) {
			throw (IOException) error;
		} else {
			throw new IOException(error);
		}
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
