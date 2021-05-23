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

import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.terminal.view.ui.interfaces.IExternalExecutablesProperties;
import org.eclipse.tm.terminal.view.ui.local.showin.IDetectExternalExecutable;

public class DetectWSL implements IDetectExternalExecutable {

	/**
	 * Don't access directly, use {@link #getEntries()}
	 */
	List<Map<String, String>> result = null;

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
		if (result == null) {
			result = Collections.emptyList();
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				String windir = System.getenv("windir"); //$NON-NLS-1$
				if (windir == null) {
					return result;
				}
				String wsl = windir + "\\System32\\wsl.exe"; //$NON-NLS-1$
				if (!Files.isExecutable(Paths.get(wsl))) {
					return result;
				}

				ProcessBuilder pb = new ProcessBuilder(wsl, "--list", "--quiet"); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					Process process = pb.start();
					try (InputStream is = process.getErrorStream()) {
						// drain the error stream
						if (is.readAllBytes().length != 0) {
							return result;
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

				} catch (IOException e) {
				}
			}
		}
		return result;
	}

}
