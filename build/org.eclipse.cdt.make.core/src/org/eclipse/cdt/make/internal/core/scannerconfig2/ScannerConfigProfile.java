/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;


/**
 * SCD Profile extension point object model
 * 
 * @author vhirsl
 */
public class ScannerConfigProfile {
	/**
	 * scannerInfoCollector element
	 * 
	 * @author vhirsl
	 */
	public class ScannerInfoCollector {
		private IConfigurationElement configElem;
		
		public ScannerInfoCollector(IConfigurationElement configElem) {
			this.configElem = configElem;
		}
		public Object createScannerInfoCollector() {
			try {
				return configElem.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
				return null;
			}
		}
	}
	/**
	 * scannerInfoConsoleParser element
	 * 
	 * @author vhirsl
	 */
	public final class ScannerInfoConsoleParser {
		private IConfigurationElement configElem;
		
		/**
		 * @param scannerInfoConsoleParser
		 */
		public ScannerInfoConsoleParser(IConfigurationElement configElem) {
			this.configElem = configElem;
		}
		public Object createScannerInfoConsoleParser() {
			try {
				return configElem.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
				return null;
			}
		}
	}
	/**
	 * tag interface, a placeholder for either run or open element
	 * 
	 * @author vhirsl
	 */
	protected abstract class Action {
		protected IConfigurationElement configElem;

		protected Action(IConfigurationElement configElem) {
			this.configElem = configElem;
		}
		
		public Object createExternalScannerInfoProvider() {
			if (configElem.getAttribute("class") != null) { //$NON-NLS-1$
				try {
					return configElem.createExecutableExtension("class"); //$NON-NLS-1$
				} catch (CoreException e) {
					MakeCorePlugin.log(e);
				}
			}
			return null;
		}
        
        public String getAttribute(String name) {
            return configElem.getAttribute(name);
        }
	}
	/**
	 * run element
	 * 
	 * @author vhirsl
	 */
	public final class Run extends Action {
		/**
		 * @param run
		 */
		public Run(IConfigurationElement run) {
			super(run);
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager.IAction#getNewExternalScannerInfoProvider()
		 */
		public Object createExternalScannerInfoProvider() {
			Object provider = super.createExternalScannerInfoProvider();
			if (provider == null) {
				// use the default one
				provider = new DefaultRunSIProvider();
			}
			return provider;
		}
	}
	/**
	 * open element
	 * 
	 * @author vhirsl
	 */
	public final class Open extends Action {
		/**
		 * @param open
		 */
		public Open(IConfigurationElement open) {
			super(open);
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager.IAction#getNewExternalScannerInfoProvider()
		 */
		public Object createExternalScannerInfoProvider() {
			Object provider = super.createExternalScannerInfoProvider();
			if (provider == null) {
				// use the default one
				provider = new DefaultSIFileReader();
			}
			return provider;
		}
	}
	/**
	 * buildOutputProvider element
	 * 
	 * @author vhirsl
	 */
	public final class BuildOutputProvider {
		private Open openFileAction;
		private ScannerInfoConsoleParser scannerInfoConsoleParser;
	
		public BuildOutputProvider(IConfigurationElement provider) {
			IConfigurationElement[] actions = provider.getChildren("open"); //$NON-NLS-1$
			// take the first one
			if (actions.length > 0) {
				this.openFileAction = new ScannerConfigProfile.Open(actions[0]);
			}
			IConfigurationElement[] parsers = provider.getChildren("scannerInfoConsoleParser"); //$NON-NLS-1$
			// take the first one
			this.scannerInfoConsoleParser = new ScannerConfigProfile.ScannerInfoConsoleParser(parsers[0]);
		}
	
		public Action getAction() {
			return openFileAction;
		}
		public ScannerInfoConsoleParser getScannerInfoConsoleParser() {
			return scannerInfoConsoleParser;
		}
	}
	/**
	 * scannerInfoProvider element
	 * 
	 * @author vhirsl
	 */
	public final class ScannerInfoProvider {
		public static final String RUN = "run";//$NON-NLS-1$
		public static final String OPEN = "open";//$NON-NLS-1$
		
		private String providerId;
		private String providerKind; // derived attribute
		private Action action;
		private ScannerInfoConsoleParser scannerInfoConsoleParser;
		
		public ScannerInfoProvider(IConfigurationElement provider) {
			providerId = provider.getAttribute("providerId"); //$NON-NLS-1$
			IConfigurationElement[] actions = provider.getChildren();
			providerKind = actions[0].getName();
			if (providerKind.equals(RUN)) {
				this.action = new ScannerConfigProfile.Run(actions[0]);
			}
			else if (providerKind.equals(OPEN)) { //$NON-NLS-1$
				this.action = new ScannerConfigProfile.Open(actions[0]);
			}
			else {
				// TODO Vmir generate an error
			}
			IConfigurationElement[] parsers = provider.getChildren("scannerInfoConsoleParser"); //$NON-NLS-1$
			// take the first one
			scannerInfoConsoleParser = new ScannerConfigProfile.ScannerInfoConsoleParser(parsers[0]);
		}
		
		public String getProviderId() {
			return providerId;
		}
		public String getProviderKind() {
			return providerKind;
		}
		public Action getAction() {
			return action;
		}
		public ScannerInfoConsoleParser getScannerInfoConsoleParser() {
			return scannerInfoConsoleParser;
		}
	}
	
	// ScannerConfigProfile members
	private final String id;
	
	private ScannerInfoCollector scannerInfoCollector;
	private BuildOutputProvider buildOutputProvider;
	private Map scannerInfoProviders = new LinkedHashMap();

	/**
	 * @param profileId
	 */
	public ScannerConfigProfile(String profileId) {
		id = profileId;
		load();
	}
	/**
	 * loads the profile from the manifest file.
	 */
	private void load() {
		String[] empty = new String[0];
		IExtensionPoint extension = Platform.getExtensionRegistry().
				getExtensionPoint(MakeCorePlugin.PLUGIN_ID, ScannerConfigProfileManager.SI_PROFILE_SIMPLE_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			List rProfileIds = new ArrayList(extensions.length);
			for (int i = 0; i < extensions.length; ++i) {
				String rProfileId = extensions[i].getUniqueIdentifier();
				if (rProfileId != null && rProfileId.equals(getId())) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; ++j) {
						String name = configElements[j].getName();
						if (scannerInfoCollector == null && 
								name.equals("scannerInfoCollector")) { //$NON-NLS-1$
							scannerInfoCollector = new ScannerConfigProfile.ScannerInfoCollector(configElements[j]);
						}
						else if (name.equals("buildOutputProvider")) { //$NON-NLS-1$
							buildOutputProvider = new ScannerConfigProfile.BuildOutputProvider(configElements[j]);
						}
						else if (name.equals("scannerInfoProvider")) { //$NON-NLS-1$
							String providerId = configElements[j].getAttribute("providerId"); //$NON-NLS-1$
							if (providerId != null && scannerInfoProviders.get(providerId) == null) {
								scannerInfoProviders.put(providerId, 
										new ScannerConfigProfile.ScannerInfoProvider(configElements[j]));
							}
						}
					}
					break;
				}
			}
		}
	}
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	
	// access to model objects
	/**
	 * @return Returns the list of providerIds
	 */
	public List getSIProviderIds() {
		return new ArrayList(scannerInfoProviders.keySet());
	}
	/**
	 * @return Returns the buildOutputProvider.
	 */
	public BuildOutputProvider getBuildOutputProviderElement() {
		return buildOutputProvider;
	}
	/**
	 * @return Returns the scannerInfoCollector.
	 */
	public ScannerInfoCollector getScannerInfoCollectorElement() {
		return scannerInfoCollector;
	}
	/**
	 * @return Returns the scannerInfoProviders.
	 */
	public ScannerInfoProvider getScannerInfoProviderElement(String providerId) {
		return (ScannerInfoProvider) scannerInfoProviders.get(providerId);
	}
}
