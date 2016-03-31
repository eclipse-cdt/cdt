package org.eclipse.cdt.internal.core.build;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class CBuildConfigAdapterFactory implements IAdapterFactory {

	private static class Provider {
		private String natureId;
		private IConfigurationElement element;
		private ICBuildConfigurationProvider provider;

		public Provider(IConfigurationElement element) {
			this.natureId = element.getAttribute("natureId"); //$NON-NLS-1$
			this.element = element;
		}

		public ICBuildConfiguration getCBuildConfig(IBuildConfiguration config) {
			try {
				if (config.getProject().hasNature(natureId)) {
					if (provider == null) {
						provider = (ICBuildConfigurationProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
					}
					return provider.getCBuildConfiguration(config);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e.getStatus());
			}
			return null;
		}
	}

	private List<Provider> providers;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (ICBuildConfiguration.class.equals(adapterType)
				&& adaptableObject instanceof IBuildConfiguration) {
			IBuildConfiguration config = (IBuildConfiguration) adaptableObject;

			if (providers == null) {
				providers = new ArrayList<>();

				IExtensionPoint point = Platform.getExtensionRegistry()
						.getExtensionPoint(CCorePlugin.PLUGIN_ID, "CBuildConfigProvider"); //$NON-NLS-1$
				for (IConfigurationElement element : point.getConfigurationElements()) {
					providers.add(new Provider(element));
				}
			}
			
			for (Provider provider : providers) {
				ICBuildConfiguration cconfig = provider.getCBuildConfig(config);
				if (cconfig != null) {
					return (T) cconfig;
				}
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { ICBuildConfiguration.class };
	}

	public static void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
			if (event.getResource().getType() == IResource.PROJECT) {
				IProject project = event.getResource().getProject();
				try {
					if (!project.hasNature(CProjectNature.C_NATURE_ID))
						return;
				} catch (CoreException e) {
					CCorePlugin.log(e.getStatus());
					return;
				}

				// Clean up the configMap
				try {
					for (IBuildConfiguration buildConfig : project.getBuildConfigs()) {
						//configMap.remove(buildConfig);
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}

				// Clean up the config settings
				Preferences parentNode = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node("config"); //$NON-NLS-1$
				if (parentNode != null) {
					Preferences projectNode = parentNode.node(project.getName());
					if (projectNode != null) {
						try {
							projectNode.removeNode();
							parentNode.flush();
						} catch (BackingStoreException e) {
							CCorePlugin.log(e);
						}
					}
				}

				// Clean up the scanner info data
				IPath stateLoc = CCorePlugin.getDefault().getStateLocation();
				IPath scannerInfoPath = stateLoc.append(project.getName());
				Path directory = scannerInfoPath.toFile().toPath();
				try {
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
				} catch (IOException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}


}
