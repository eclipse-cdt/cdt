package org.eclipse.cdt.build.gcc.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.build.gcc.core.internal.Activator;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.build.IToolChainType;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

public class Msys2ToolChainProvider implements IToolChainProvider {

	@Override
	public Collection<IToolChain> getToolChains() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
			String subkey;
			List<IToolChain> toolChains = null;
			IToolChainType type = null;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName");
				if ("MSYS2 64bit".equals(displayName)) {
					String installLocation = registry.getCurrentUserValue(compKey, "InstallLocation");
					Path gccPath = Paths.get(installLocation + "\\mingw64\\bin\\gcc.exe");
					if (Files.exists(gccPath)) {
						if (toolChains == null) {
							toolChains = new ArrayList<>();
						}
						if (type == null) {
							type = Activator.getService(IToolChainManager.class).getToolChainType(GCCToolChainType.ID);
						}
						toolChains.add(
								new GCCToolChain(type, gccPath.getParent(), gccPath.getFileName().toString()));
					}
				}
			}
			
			if (toolChains != null) {
				return toolChains;
			}
		}
		// default
		return Collections.emptyList();
	}

}
