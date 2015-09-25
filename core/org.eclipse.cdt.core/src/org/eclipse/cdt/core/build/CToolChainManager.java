package org.eclipse.cdt.core.build;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Manager that manages the list of toolchains available.
 * 
 * @since 5.12
 */
public class CToolChainManager {

	private static final String TOOLCHAINS = "toolchains"; //$NON-NLS-1$

	public static final CToolChainManager instance = new CToolChainManager();

	private Map<String, IConfigurationElement> toolChainFamilies = new HashMap<>();
	private Map<String, CToolChain> toolChains = new HashMap<>();

	private CToolChainManager() {
		new Job("Load toolchains") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// Load up the families
					IExtensionRegistry registry = Platform.getExtensionRegistry();
					IExtensionPoint point = registry
							.getExtensionPoint("org.eclipse.cdt.core.ToolChainFactory"); //$NON-NLS-1$
					for (IExtension extension : point.getExtensions()) {
						for (IConfigurationElement element : extension.getConfigurationElements()) {
							String family = element.getAttribute(CToolChain.FAMILY);
							if (family != null) {
								toolChainFamilies.put(family, element);
							}
						}
					}

					// Load up the toolchains
					Preferences toolChainsPref = getToolChainSettings();
					for (String toolChainId : toolChainsPref.childrenNames()) {
						Preferences toolChainPref = toolChainsPref.node(toolChainId);
						String family = toolChainPref.get(CToolChain.FAMILY, ""); //$NON-NLS-1$
						if (!family.isEmpty()) {
							IConfigurationElement element = toolChainFamilies.get(family);
							if (element != null) {
								IToolChainFactory factory = (IToolChainFactory) element
										.createExecutableExtension("class"); //$NON-NLS-1$
								CToolChain toolChain = factory.createToolChain(toolChainId, toolChainPref);
								toolChains.put(toolChain.getName(), toolChain);
							}
						}
					}
					return Status.OK_STATUS;
				} catch (BackingStoreException e) {
					return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "loading toolchains", e);
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
		}.schedule();
	}

	private Preferences getToolChainSettings() {
		return ConfigurationScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node(TOOLCHAINS);
	}

	Preferences getSettings(String id) {
		return getToolChainSettings().node(id);
	}

	public Collection<CToolChain> getToolChains() {
		return toolChains.values();
	}

	public void addToolChain(CToolChain toolChain) throws CoreException {
		// First find an open id for the toolchain
		String id = null;
		for (int i = 0; i < toolChains.size(); ++i) {
			String istr = String.valueOf(i);
			if (toolChains.containsKey(istr)) {
				id = istr;
				break;
			}
		}

		if (id == null) {
			id = String.valueOf(toolChains.size());
		}

		toolChain.setId(id);
		toolChains.put(id, toolChain);

		// save
		try {
			Preferences toolChainsPref = getToolChainSettings();
			toolChain.save(toolChainsPref.node(id));
			toolChainsPref.flush();
		} catch (BackingStoreException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "saving toolchain " + id, e));
		}
	}

}
