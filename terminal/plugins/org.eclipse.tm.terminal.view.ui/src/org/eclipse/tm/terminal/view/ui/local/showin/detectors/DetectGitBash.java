/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.local.showin.detectors;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.terminal.view.ui.interfaces.IExternalExecutablesProperties;
import org.eclipse.tm.terminal.view.ui.local.showin.ExternalExecutablesUtils;
import org.eclipse.tm.terminal.view.ui.local.showin.IDetectExternalExecutable;

public class DetectGitBash implements IDetectExternalExecutable {

	private static final String GIT_BASH = "Git Bash"; //$NON-NLS-1$
	private static boolean gitBashSearchDone = false;

	@Override
	public boolean hasEntries() {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			// the order of operations here is different than getEntries because we just
			// want to know if anything can match. We avoid iterating over the whole PATH
			// if the user has git installed in the default location. This gets us the
			// correct answer to hasEntries quickly without having to iterate over the whole
			// PATH (especially if PATH is long!).
			return getGitInstallDirectory().or(this::getGitInstallDirectoryFromPATH).isPresent();
		}
		return false;
	}

	@Override
	public List<Map<String, String>> getEntries(List<Map<String, String>> externalExecutables) {
		// Lookup git bash (Windows Hosts only)
		if (!gitBashSearchDone && Platform.OS_WIN32.equals(Platform.getOS())) {
			// Do not search again for git bash while the session is running
			gitBashSearchDone = true;

			// Check the existing entries first
			// Find a entry labeled "Git Bash"
			if (externalExecutables.stream().map(m -> m.get(IExternalExecutablesProperties.PROP_NAME))
					.anyMatch(Predicate.isEqual(GIT_BASH))) {
				return Collections.emptyList();
			}

			// If not found in the existing entries, check the path, then
			// if it is not found in the PATH, check the default install locations
			Optional<File> result = getGitInstallDirectoryFromPATH().or(this::getGitInstallDirectory);
			Optional<String> gitPath = result.map(f -> new File(f, "bin/sh.exe").getAbsolutePath()); //$NON-NLS-1$
			Optional<String> iconPath = result.flatMap(f -> getGitIconPath(f));

			return gitPath.map(path -> {
				Map<String, String> m = new HashMap<>();
				m.put(IExternalExecutablesProperties.PROP_NAME, GIT_BASH);
				m.put(IExternalExecutablesProperties.PROP_PATH, path);
				m.put(IExternalExecutablesProperties.PROP_ARGS, "--login -i"); //$NON-NLS-1$
				iconPath.ifPresent(icon -> m.put(IExternalExecutablesProperties.PROP_ICON, icon));
				m.put(IExternalExecutablesProperties.PROP_TRANSLATE, Boolean.TRUE.toString());

				return List.of(m);
			}).orElse(Collections.emptyList());

		}
		return Collections.emptyList();
	}

	private Optional<File> getGitInstallDirectoryFromPATH() {
		return ExternalExecutablesUtils.visitPATH(entry -> {
			File f = new File(entry, "git.exe"); //$NON-NLS-1$
			if (f.canRead()) {
				File check = f.getParentFile().getParentFile();
				if (new File(check, "bin/sh.exe").canExecute()) { //$NON-NLS-1$
					return Optional.of(check);
				}
			}
			return Optional.empty();
		});
	}

	private Optional<File> getGitInstallDirectory() {
		// If 32-bit and 64-bit are both present, prefer the 64-bit one
		// as it is probably newer and more likely to be present
		// for the fast check required by hasEntries
		File f = new File("C:/Program Files/Git/bin/sh.exe"); //$NON-NLS-1$
		if (!f.exists()) {
			f = new File("C:/Program Files (x86)/Git/bin/sh.exe"); //$NON-NLS-1$
		}
		if (f.exists() && f.canExecute()) {
			return Optional.of(f.getParentFile().getParentFile());
		}
		return Optional.empty();
	}

	private static Optional<String> getGitIconPath(File parent) {
		File f = new File(parent, "etc/git.ico"); //$NON-NLS-1$
		if (f.canRead()) {
			return Optional.of(f.getAbsolutePath());
		}

		// check for icon in newer versions of Git for Windows 32 bit
		f = new File(parent, "mingw32/share/git/git-for-windows.ico"); //$NON-NLS-1$
		if (f.canRead()) {
			return Optional.of(f.getAbsolutePath());
		}

		// check for icon in newer versions of Git for Windows 64 bit
		f = new File(parent, "mingw64/share/git/git-for-windows.ico"); //$NON-NLS-1$
		if (f.canRead()) {
			return Optional.of(f.getAbsolutePath());
		}

		return Optional.empty();
	}
}
