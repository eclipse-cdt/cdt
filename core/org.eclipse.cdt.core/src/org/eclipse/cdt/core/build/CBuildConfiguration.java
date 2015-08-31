package org.eclipse.cdt.core.build;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Root class for CDT build configurations. Provides access to the build
 * settings for subclasses.
 * 
 * @since 5.12
 */
public abstract class CBuildConfiguration extends PlatformObject {

	private static final String TOOLCHAIN = "cdt.toolChain"; //$NON-NLS-1$

	private final IBuildConfiguration config;
	private CToolChain toolChain;

	protected CBuildConfiguration(IBuildConfiguration config) {
		this.config = config;
	}

	public IBuildConfiguration getBuildConfiguration() {
		return config;
	}

	public String getName() {
		return config.getName();
	}

	public IProject getProject() {
		return config.getProject();
	}

	public void setActive(IProgressMonitor monitor) throws CoreException {
		IProject project = config.getProject();
		if (config.equals(project.getActiveBuildConfig())) {
			// already set
			return;
		}

		IProjectDescription projectDesc = project.getDescription();
		projectDesc.setActiveBuildConfig(config.getName());
		project.setDescription(projectDesc, monitor);
	}

	protected IEclipsePreferences getSettings() {
		return (IEclipsePreferences) new ProjectScope(config.getProject()).getNode("org.eclipse.cdt.core") //$NON-NLS-1$
				.node("config") //$NON-NLS-1$
				.node(config.getName());
	}

	private synchronized CToolChain getToolChain(String id) throws CoreException {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID + ".ToolChain"); //$NON-NLS-1$
		for (IExtension extension : point.getExtensions()) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				String eid = element.getAttribute("id"); //$NON-NLS-1$
				if (id.equals(eid)) {
					String clsName = element.getAttribute("adaptor"); //$NON-NLS-1$
					if (clsName != null) {
						try {
							Class<?> cls = Class.forName(clsName);
							return (CToolChain) getAdapter(cls);
						} catch (ClassNotFoundException e) {
							throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID,
									"creating toolchain", e)); //$NON-NLS-1$
						}
					}
				}
			}
		}
		return null;
	}

	public synchronized void setToolChain(String id) throws CoreException {
		CToolChain newtc = getToolChain(id);
		if (newtc == null) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "unknown toolchain: " + id)); //$NON-NLS-1$
		}

		IEclipsePreferences settings = getSettings();
		settings.put(TOOLCHAIN, id);
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "saving toolchain id", e)); //$NON-NLS-1$
		}

		toolChain = newtc;
	}

	public CToolChain getToolChain() throws CoreException {
		if (toolChain == null) {
			IEclipsePreferences settings = getSettings();
			String id = settings.get(TOOLCHAIN, ""); //$NON-NLS-1$
			if (id.isEmpty()) {
				return null;
			} else {
				toolChain = getToolChain(id);
			}
		}
		return toolChain;
	}

	public IScannerInfo getScannerInfo(IResource resource) throws CoreException {
		// By default, get it from the toolchain.
		CToolChain toolChain = getToolChain();
		return toolChain != null ? toolChain.getScannerInfo(resource) : null;
	}

	public void clearScannerInfo() throws CoreException {
	}

	public CConsoleParser[] getConsoleParsers() throws CoreException {
		CToolChain toolChain = getToolChain();
		return toolChain != null ? toolChain.getConsoleParsers() : null;
	}

}
