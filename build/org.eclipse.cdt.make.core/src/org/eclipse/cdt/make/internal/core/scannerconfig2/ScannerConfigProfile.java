/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigScope;
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

		public IScannerInfoCollector createScannerInfoCollector() {
			try {
				return (IScannerInfoCollector) configElem.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
				return null;
			}
		}

		public String getScope() {
			return configElem.getAttribute("scope"); //$NON-NLS-1$
		}
	}

	/**
	 * scannerInfoConsoleParser element
	 *
	 * @author vhirsl
	 */
	public final class ScannerInfoConsoleParser {
		private IConfigurationElement configElem;

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

		public String getCompilerCommands() {
			return configElem.getAttribute("compilerCommands"); //$NON-NLS-1$
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
		public Run(IConfigurationElement run) {
			super(run);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager.IAction#getNewExternalScannerInfoProvider()
		 */
		@Override
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
		public Open(IConfigurationElement open) {
			super(open);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager.IAction#getNewExternalScannerInfoProvider()
		 */
		@Override
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
			} else if (providerKind.equals(OPEN)) {
				this.action = new ScannerConfigProfile.Open(actions[0]);
			} else {
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
	private Map<String, ScannerInfoProvider> scannerInfoProviders = new LinkedHashMap<>();

	private Boolean supportsContext;

	public ScannerConfigProfile(String profileId) {
		id = profileId;
		load();
	}

	/**
	 * loads the profile from the manifest file.
	 */
	private void load() {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(MakeCorePlugin.PLUGIN_ID,
				ScannerConfigProfileManager.SI_PROFILE_SIMPLE_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (int i = 0; i < extensions.length; ++i) {
				String rProfileId = extensions[i].getUniqueIdentifier();
				if (rProfileId != null && rProfileId.equals(getId())) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; ++j) {
						String name = configElements[j].getName();
						if (scannerInfoCollector == null && name.equals("scannerInfoCollector")) { //$NON-NLS-1$
							scannerInfoCollector = new ScannerConfigProfile.ScannerInfoCollector(configElements[j]);
						} else if (name.equals("buildOutputProvider")) { //$NON-NLS-1$
							buildOutputProvider = new ScannerConfigProfile.BuildOutputProvider(configElements[j]);
						} else if (name.equals("scannerInfoProvider")) { //$NON-NLS-1$
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
	public List<String> getSIProviderIds() {
		return new ArrayList<>(scannerInfoProviders.keySet());
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

	public ScannerConfigScope getProfileScope() {
		ScannerConfigScope scope = null;
		if (scannerInfoCollector != null) {
			if (scannerInfoCollector.getScope().equals(ScannerConfigScope.PROJECT_SCOPE.toString())) {
				scope = ScannerConfigScope.PROJECT_SCOPE;
			} else if (scannerInfoCollector.getScope().equals(ScannerConfigScope.FILE_SCOPE.toString())) {
				scope = ScannerConfigScope.FILE_SCOPE;
			}
		}
		return scope;
	}

	/**
	 * @return Returns the scannerInfoProviders.
	 */
	public ScannerInfoProvider getScannerInfoProviderElement(String providerId) {
		return scannerInfoProviders.get(providerId);
	}

	public boolean supportsContext() {
		if (supportsContext == null) {
			ScannerInfoCollector cr = getScannerInfoCollectorElement();
			if (cr != null) {
				IScannerInfoCollector collector = cr.createScannerInfoCollector();
				if (collector != null) {
					Class<? extends IScannerInfoCollector> clazz = collector.getClass();
					try {
						clazz.getMethod("setInfoContext", new Class[] { InfoContext.class }); //$NON-NLS-1$
						supportsContext = Boolean.TRUE;
					} catch (SecurityException e) {
					} catch (NoSuchMethodException e) {
					}
				}
			}

			if (supportsContext == null)
				supportsContext = Boolean.FALSE;
		}
		return supportsContext.booleanValue();
	}
}
