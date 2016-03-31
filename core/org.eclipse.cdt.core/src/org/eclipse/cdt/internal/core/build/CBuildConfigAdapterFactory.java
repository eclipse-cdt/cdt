package org.eclipse.cdt.internal.core.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

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

}
