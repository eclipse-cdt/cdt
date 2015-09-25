package org.eclipse.cdt.core.build.gcc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.build.CToolChain;
import org.eclipse.cdt.core.build.IToolChainFactory;
import org.osgi.service.prefs.Preferences;

/**
 * @since 5.12
 */
public class GCCToolChainFactory implements IToolChainFactory {

	private static Pattern gccPattern = Pattern.compile("(.*-)?(gcc|g\\+\\+|clang|clang\\+\\+)(-[0-9].*)?"); //$NON-NLS-1$

	/**
	 * Discover gcc installs that exist on the path.
	 */
	@Override
	public void discover() {
		String path = null;
		for (Entry<String, String> entry : System.getenv().entrySet()) {
			if (entry.getKey().equalsIgnoreCase("PATH")) { //$NON-NLS-1$
				path = entry.getValue();
				break;
			}
		}

		if (path != null) {
			Map<String, List<String>> installs = new HashMap<>();

			for (String dirStr : path.split(File.pathSeparator)) {
				File dir = new File(dirStr);
				for (String file : dir.list()) {
					Matcher matcher = gccPattern.matcher(file);
					if (matcher.matches()) {
						String prefix = matcher.group(1);
						String suffix = matcher.group(3);
						String command = dirStr + File.separatorChar + file;
						String version = getVersion(command);
						if (version != null) {
							List<String> commands = installs.get(version);
							if (commands == null) {
								commands = new ArrayList<>();
								installs.put(version, commands);
							}
							commands.add(command);
						}
					}
				}
			}

			for (Entry<String, List<String>> entry : installs.entrySet()) {
				System.out.println(entry.getKey());
				for (String command : entry.getValue()) {
					System.out.println("\t" + command);
				}
			}
		}
	}

	private static Pattern versionPattern = Pattern.compile(".*(gcc|LLVM) version .*"); //$NON-NLS-1$
	private static Pattern targetPattern = Pattern.compile("Target: (.*)"); //$NON-NLS-1$

	private String getVersion(String command) {
		try {
			Process proc = new ProcessBuilder(new String[] { command, "-v" }).redirectErrorStream(true) //$NON-NLS-1$
					.start();
			String version = null;
			String target = null;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					Matcher versionMatcher = versionPattern.matcher(line);
					if (versionMatcher.matches()) {
						version = line.trim();
						continue;
					}
					Matcher targetMatcher = targetPattern.matcher(line);
					if (targetMatcher.matches()) {
						target = targetMatcher.group(1);
						continue;
					}
				}
			}
			if (version != null) {
				if (target != null) {
					return version + " " + target; // $NON-NLS-1$
				} else {
					return version;
				}
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public CToolChain createToolChain(String id, Preferences settings) {
		return new GCCToolChain(id, settings);
	}

}
