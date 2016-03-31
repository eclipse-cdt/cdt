package org.eclipse.cdt.qt.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.build.gcc.core.GCCToolChainType;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.build.IToolChainType;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

public class QtMinGWToolChainProvider implements IToolChainProvider {

	@Override
	public Collection<IToolChain> getToolChains() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"; //$NON-NLS-1$
			String subkey;
			IToolChainType type = Activator.getService(IToolChainManager.class).getToolChainType(GCCToolChainType.ID);
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				if ("Qt".equals(displayName)) { //$NON-NLS-1$
					String installLocation = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
					Path gcc = Paths.get("\\bin\\gcc.exe"); //$NON-NLS-1$
					try {
						return Files.walk(Paths.get(installLocation).resolve("Tools"), 1) //$NON-NLS-1$
								.filter((path) -> Files.exists(path.resolve(gcc)))
								.map((path) -> new GCCToolChain(type, path.resolve("bin"), "gcc.exe")) //$NON-NLS-1$ //$NON-NLS-2$
								.collect(Collectors.toList());
					} catch (IOException e) {
						Activator.log(e);
					}
				}
			}
		}
		// default
		return Collections.emptyList();
	}

}
