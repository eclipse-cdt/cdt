/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.flatpak.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.ICommandLauncherFactory;
import org.eclipse.cdt.core.ICommandLauncherFactory2;
import org.eclipse.cdt.core.ICommandLauncherFactory3;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

public class FlatpakCommandLauncherFactory
		implements ICommandLauncherFactory, ICommandLauncherFactory2, ICommandLauncherFactory3 {

	private static Set<String> copiedDirs = null;
	private static Set<String> removedDirs = new HashSet<>();
	private static Object lockObject = new Object();
	private final static String HEADERS = "HEADERS"; //$NON-NLS-1$
	private final static String COPIED = ".copied"; //$NON-NLS-1$
	private final static String REMOVED = ".removed"; //$NON-NLS-1$

	public FlatpakCommandLauncherFactory() {
		initialize();
	}

	private void initialize() {
		synchronized (lockObject) {
			if (copiedDirs == null) {
				copiedDirs = new HashSet<>();
				IPath pluginPath = Platform.getStateLocation(Platform.getBundle(FlatpakLaunchPlugin.PLUGIN_ID))
						.append(HEADERS);
				IPath copiedPath = pluginPath.append(COPIED);
				File copiedFile = copiedPath.toFile();
				if (copiedFile.exists()) {
					try (FileReader reader = new FileReader(copiedFile);
							BufferedReader bufferedReader = new BufferedReader(reader)) {
						String dir = bufferedReader.readLine();
						while (dir != null) {
							if (!dir.isEmpty()) {
								copiedDirs.add(dir);
							}
							dir = bufferedReader.readLine();
						}
					} catch (IOException e) {
						FlatpakLaunchPlugin.log(e);
					}
				}
				IPath removedPath = pluginPath.append(REMOVED);
				File removedFile = removedPath.toFile();
				if (removedFile.exists()) {
					try (FileReader reader = new FileReader(removedFile);
							BufferedReader bufferedReader = new BufferedReader(reader)) {
						String dir = bufferedReader.readLine();
						while (dir != null) {
							if (!dir.isEmpty()) {
								removedDirs.add(dir);
							}
							dir = bufferedReader.readLine();
						}
					} catch (IOException e) {
						FlatpakLaunchPlugin.log(e);
					}
				}
			}
		}
	}

	public static void removeDir(String path) {
		synchronized (lockObject) {
			List<String> removedEntries = new ArrayList<>();
			for (String copiedDir : copiedDirs) {
				if (copiedDir.startsWith(path)) {
					removedEntries.add(copiedDir);
				}
			}
			for (String removedEntry : removedEntries) {
				copiedDirs.remove(removedEntry);
				removedDirs.add(removedEntry);
			}
		}
	}

	public static List<String> getDirs() {
		synchronized (lockObject) {
			List<String> newDirs = new ArrayList<>(copiedDirs);
			return newDirs;
		}
	}

	private IProject project;

	@Override
	public ICommandLauncher getCommandLauncher(IProject project) {
		this.project = project;

		if (System.getenv("FLATPAK_SANDBOX_DIR") != null) { //$NON-NLS-1$
			return new FlatpakCommandLauncher();
		}
		return null;
	}

	@Override
	public ICommandLauncher getCommandLauncher(ICConfigurationDescription cfgd) {
		if (System.getenv("FLATPAK_SANDBOX_DIR") != null) { //$NON-NLS-1$
			return new FlatpakCommandLauncher();
		}
		return null;
	}

	@Override
	public ICommandLauncher getCommandLauncher(ICBuildConfiguration cfgd) {
		if (System.getenv("FLATPAK_SANDBOX_DIR") != null) { //$NON-NLS-1$
			return new FlatpakCommandLauncher();
		}
		return null;
	}

	@Override
	public void registerLanguageSettingEntries(IProject project, List<? extends ICLanguageSettingEntry> langEntries) {
		synchronized (lockObject) {
			@SuppressWarnings("unchecked")
			List<ICLanguageSettingEntry> entries = (List<ICLanguageSettingEntry>) langEntries;
			List<String> paths = new ArrayList<>();
			for (ICLanguageSettingEntry entry : entries) {
				if (entry instanceof ICIncludePathEntry) {
					paths.add(entry.getValue());
				}
			}
			if (paths.size() > 0) {
				// Create a directory to put the header files for
				// the host.
				IPath pluginPath = Platform.getStateLocation(Platform.getBundle(FlatpakLaunchPlugin.PLUGIN_ID))
						.append(HEADERS);
				pluginPath.toFile().mkdir();
				for (String path : paths) {
					if (path.startsWith(project.getWorkspace().getRoot().getLocation().toString())) {
						continue;
					}
					try {
						Process p1 = ProcessFactory.getFactory()
								.exec("mkdir -p " + pluginPath.append(path).toOSString()); //$NON-NLS-1$
						int rc1 = waitFor(p1);
						if (rc1 == 0) {
							Process p2 = ProcessFactory.getFactory().exec("cp -ru " + path //$NON-NLS-1$
									+ " " + pluginPath.append(path).removeLastSegments(1).toOSString()); //$NON-NLS-1$
							int rc2 = waitFor(p2);
							if (rc2 == 0) {
								copiedDirs.add(path);
								for (String removedDir : removedDirs) {
									if (removedDir.startsWith(path)) {
										removedDirs.remove(removedDir);
										copiedDirs.add(removedDir);
									}
								}
							}
						}
					} catch (IOException e) {
						FlatpakLaunchPlugin.log(e);
					}
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		IPath pluginPath = Platform.getStateLocation(Platform.getBundle(FlatpakLaunchPlugin.PLUGIN_ID))
				.append(HEADERS);
		pluginPath.toFile().mkdir();
		IPath copiedListPath = pluginPath.append(COPIED);
		try (FileWriter writer = new FileWriter(copiedListPath.toFile());
				BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
			for (String copiedDir : copiedDirs) {
				bufferedWriter.write(copiedDir);
				bufferedWriter.newLine();
			}
		}
		IPath removedListPath = pluginPath.append(REMOVED);
		try (FileWriter writer = new FileWriter(removedListPath.toFile());
				BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
			for (String removedDir : removedDirs) {
				bufferedWriter.write(removedDir);
				bufferedWriter.newLine();
			}
		}
	}

	private int waitFor(Process p) {
		int rc = -1;
		boolean finished = false;
		try {
			Thread.sleep(100);
			while (!finished) {
				rc = p.exitValue();
				finished = true;
			}
		} catch (IllegalThreadStateException e) {
			// do nothing
		} catch (InterruptedException e) {
			finished = true;
		}
		return rc;
	}

	@Override
	public List<String> verifyIncludePaths(ICBuildConfiguration cfgd, List<String> includePaths) {
		synchronized (lockObject) {
			if (includePaths.size() > 0) {
				IPath copiedPath = Platform.getStateLocation(Platform.getBundle(FlatpakLaunchPlugin.PLUGIN_ID))
						.append(HEADERS);
				copiedPath.toFile().mkdir();
				List<String> newEntries = new ArrayList<>();
				for (String path : includePaths) {
					if (path.startsWith(project.getWorkspace().getRoot().getLocation().toString())) {
						continue;
					}
					if (!copiedDirs.contains(path)) {
						try {
							Process p1 = ProcessFactory.getFactory()
									.exec("mkdir -p " + copiedPath.append(path).toOSString()); //$NON-NLS-1$
							int rc1 = waitFor(p1);
							if (rc1 == 0) {
								Process p2 = ProcessFactory.getFactory().exec("cp -ru " + path //$NON-NLS-1$
										+ " " + copiedPath.append(path).removeLastSegments(1).toOSString()); //$NON-NLS-1$
								int rc2 = waitFor(p2);
								if (rc2 == 0) {
									copiedDirs.add(path);
									newEntries.add(copiedPath.append(path).toOSString());
									for (String removedDir : removedDirs) {
										if (removedDir.startsWith(path)) {
											removedDirs.remove(removedDir);
											copiedDirs.add(removedDir);
										}
									}
								} else {
									newEntries.add(path);
								}
							}
						} catch (IOException e) {
							FlatpakLaunchPlugin.log(e);
						}
					} else {
						newEntries.add(copiedPath.append(path).toOSString());
					}
				}
				return newEntries;
			}
			return includePaths;
		}
	}

	@Override
	public List<ICLanguageSettingEntry> verifyLanguageSettingEntries(IProject project,
			List<ICLanguageSettingEntry> entries) {
		if (entries == null) {
			return null;
		}
		synchronized (lockObject) {
			List<ICLanguageSettingEntry> newEntries = new ArrayList<>();
			IPath pluginPath = Platform.getStateLocation(Platform.getBundle(FlatpakLaunchPlugin.PLUGIN_ID));
			IPath hostDir = pluginPath.append(HEADERS);

			for (ICLanguageSettingEntry entry : entries) {
				if (entry instanceof ICIncludePathEntry) {
					String path = entry.getName().toString();
					if (removedDirs.contains(path)) {
						try {
							Process p1 = ProcessFactory.getFactory()
									.exec("mkdir -p " + hostDir.append(path).toOSString()); //$NON-NLS-1$
							int rc1 = waitFor(p1);
							if (rc1 == 0) {
								Process p2 = ProcessFactory.getFactory().exec("cp -ru " + path //$NON-NLS-1$
										+ " " + hostDir.append(path).removeLastSegments(1).toOSString()); //$NON-NLS-1$
								int rc2 = waitFor(p2);
								if (rc2 == 0) {
									copiedDirs.add(path);
									for (String removedDir : removedDirs) {
										if (removedDir.startsWith(path)) {
											removedDirs.remove(removedDir);
											copiedDirs.add(removedDir);
										}
									}
								}
							}
						} catch (IOException e) {
							FlatpakLaunchPlugin.log(e);
						}

					}
					if (copiedDirs.contains(path)) {
						// //$NON-NLS-2$
						IPath newPath = hostDir.append(entry.getName());
						CIncludePathEntry newEntry = new CIncludePathEntry(newPath.toString(), entry.getFlags());
						newEntries.add(newEntry);
						continue;
					} else {
						newEntries.add(entry);
					}
				} else {
					newEntries.add(entry);
				}
			}
			return newEntries;
		}
	}

	@Override
	public boolean checkIfIncludesChanged(ICBuildConfiguration cfg) {
		synchronized (lockObject) {
			return !removedDirs.isEmpty();
		}
	}

}
