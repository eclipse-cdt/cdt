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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tm.terminal.view.ui.interfaces.IExternalExecutablesProperties;
import org.eclipse.tm.terminal.view.ui.local.showin.IDetectExternalExecutable;

public class DetectWSL implements IDetectExternalExecutable {

	private List<Map<String, String>> result = null;
	private WslDetectJob detectJob = null;

	public DetectWSL() {
		if (!Platform.OS_WIN32.equals(Platform.getOS())) {
			result = Collections.emptyList();
		}
	}

	@Override
	public boolean hasEntries() {
		return !getEntries().isEmpty();
	}

	@Override
	public List<Map<String, String>> getEntries(List<Map<String, String>> externalExecutables) {
		List<Map<String, String>> newEntries = new ArrayList<>();
		var entries = getEntries();
		for (var map : entries) {
			String name = map.get(IExternalExecutablesProperties.PROP_NAME);
			if (externalExecutables.stream().map(m -> m.get(IExternalExecutablesProperties.PROP_NAME))
					.noneMatch(Predicate.isEqual(name))) {
				newEntries.add(map);
			}
		}
		return newEntries;
	}

	private synchronized List<Map<String, String>> getEntries() {
		// getEntries can be called in many contexts, even from within
		// menu creation (see Bug 574519). Therefore we spawn a job to
		// get the real entries, which means until the job is done, this
		// method will return no entries.
		if (result != null) {
			return result;
		}
		if (detectJob == null) {
			detectJob = new WslDetectJob();
			detectJob.schedule();
		}
		try {
			if (detectJob.join(10, null) && detectJob.result != null) { // Suspended jobs return early from join()
				result = detectJob.result;
				detectJob = null;
			} else {
				return Collections.emptyList();
			}
		} catch (OperationCanceledException | InterruptedException e) {
			result = Collections.emptyList();
			detectJob = null;
		}
		return result;
	}

	private static class WslDetectJob extends Job {
		private List<Map<String, String>> result = null;

		public WslDetectJob() {
			super("Detect WSL Instances"); //$NON-NLS-1$
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (result == null) {
				result = Collections.emptyList();
				String windir = System.getenv("windir"); //$NON-NLS-1$
				if (windir == null) {
					return Status.OK_STATUS;
				}
				String wsl = windir + "\\System32\\wsl.exe"; //$NON-NLS-1$
				if (!Files.isExecutable(Paths.get(wsl))) {
					return Status.OK_STATUS;
				}

				ProcessBuilder pb = new ProcessBuilder(wsl, "--list", "--quiet"); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					Process process = pb.start();
					try (InputStream is = process.getErrorStream()) {
						// drain the error stream
						if (is.readAllBytes().length != 0) {
							return Status.OK_STATUS;
						}
					}

					try (BufferedReader reader = new BufferedReader(
							new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_16LE))) {
						result = new ArrayList<>();
						String line = null;
						while ((line = reader.readLine()) != null) {
							String distribution = line.trim();
							if (distribution.isBlank()) {
								continue;
							}
							// docker-desktop entries are not "real" so shouldn't be shown in UI
							if (distribution.startsWith("docker-desktop")) { //$NON-NLS-1$
								continue;
							}

							String name = distribution + " (WSL)"; //$NON-NLS-1$
							Map<String, String> m = new HashMap<>();
							m.put(IExternalExecutablesProperties.PROP_NAME, name);
							m.put(IExternalExecutablesProperties.PROP_PATH, wsl);
							m.put(IExternalExecutablesProperties.PROP_ARGS, "--distribution " + distribution); //$NON-NLS-1$
							m.put(IExternalExecutablesProperties.PROP_TRANSLATE, Boolean.TRUE.toString());
							result.add(m);
						}
					}

					try {
						// lets get the return code to make sure that the process did not produce anything unexpected. As the streams
						// are closed, the waitFor shouldn't block.
						if (process.waitFor() != 0) {
							// WSL can send errors to stdout, so discard results if the exit value ends up being non-zero
							result.clear();
						}
					} catch (InterruptedException e) {
						// we've been interrupted, give up on the output we have (probably unreachable in practice)
						result.clear();
					}

				} catch (IOException e) {
				}
			}
			return Status.OK_STATUS;
		}

	}

}
