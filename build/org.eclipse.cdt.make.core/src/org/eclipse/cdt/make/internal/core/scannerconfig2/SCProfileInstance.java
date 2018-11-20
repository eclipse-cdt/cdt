/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.Action;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.BuildOutputProvider;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.ScannerInfoCollector;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.ScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.ScannerInfoProvider;
import org.eclipse.core.resources.IProject;

/**
 * Instantiated scanner config profile
 *
 * @author vhirsl
 */
public class SCProfileInstance {
	private IProject project;
	private ScannerConfigProfile profile;
	private IScannerInfoCollector collector;
	private InfoContext context;

	/**
	 *
	 */
	public SCProfileInstance(IProject project, ScannerConfigProfile profile) {
		this(project, new InfoContext(project), profile);
	}

	public SCProfileInstance(IProject project, InfoContext context, ScannerConfigProfile profile) {
		this.project = project;
		this.profile = profile;
		this.context = context;
	}

	/**
	 *
	 */
	private void instantiateCollector() {
		// create collector object
		collector = createScannerInfoCollector();
		if (collector != null) {
			// call collector.setProject(project) if class supports it
			Class<? extends IScannerInfoCollector> clazz = collector.getClass();
			try {
				//				Method setProjectMethod = clazz.getMethod("setProject", new Class[] {IProject.class});//$NON-NLS-1$
				//				setProjectMethod.invoke(collector, new Object[] {project});
				Object[] args = null;
				Method setMethod = null;
				if (context != null) {
					try {
						setMethod = clazz.getMethod("setInfoContext", new Class[] { InfoContext.class });//$NON-NLS-1$
						args = new Object[] { context };
					} catch (NoSuchMethodException e) {
					}
				}

				if (setMethod == null) {
					try {
						setMethod = clazz.getMethod("setProject", new Class[] { IProject.class });//$NON-NLS-1$
						args = new Object[] { project };
					} catch (NoSuchMethodException e) {
					}
				}
				if (setMethod != null)
					setMethod.invoke(collector, args);

			} catch (SecurityException e) {
				MakeCorePlugin.log(e);
				//			} catch (NoSuchMethodException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
				MakeCorePlugin.log(e.getCause());
			}
		}
		// all other objects are created on request
	}

	public ScannerConfigProfile getProfile() {
		return profile;
	}

	/**
	 * @return a single scannerInfoCollector object
	 */
	public IScannerInfoCollector getScannerInfoCollector() {
		if (collector == null) {
			instantiateCollector();
		}
		return collector;
	}

	public IScannerInfoCollector createScannerInfoCollector() {
		ScannerInfoCollector collector = profile.getScannerInfoCollectorElement();
		if (collector != null) {
			return collector.createScannerInfoCollector();
		}
		return null;
	}

	/**
	 * @return Creates new buildOutputProvider user object.
	 */
	public IExternalScannerInfoProvider createBuildOutputProvider() {
		BuildOutputProvider bop = profile.getBuildOutputProviderElement();
		if (bop != null) {
			Action action = bop.getAction();
			if (action != null) {
				return (IExternalScannerInfoProvider) action.createExternalScannerInfoProvider();
			}
		}
		return null;
	}

	/**
	 * @return Creates new buildOutputParser user object.
	 */
	public IScannerInfoConsoleParser createBuildOutputParser() {
		BuildOutputProvider bop = profile.getBuildOutputProviderElement();
		if (bop != null) {
			ScannerInfoConsoleParser parserElement = bop.getScannerInfoConsoleParser();
			if (parserElement != null) {
				return (IScannerInfoConsoleParser) parserElement.createScannerInfoConsoleParser();
			}
		}
		return null;
	}

	/**
	 * @return Creates new externalSIProvider user object.
	 */
	public IExternalScannerInfoProvider createExternalScannerInfoProvider(String providerId) {
		ScannerInfoProvider provider = profile.getScannerInfoProviderElement(providerId);
		if (provider != null) {
			return (IExternalScannerInfoProvider) provider.getAction().createExternalScannerInfoProvider();
		}
		return null;
	}

	/**
	 * @return Creates new esiProviderOutputParser user object.
	 */
	public IScannerInfoConsoleParser createExternalScannerInfoParser(String providerId) {
		ScannerInfoProvider provider = profile.getScannerInfoProviderElement(providerId);
		if (provider != null) {
			return (IScannerInfoConsoleParser) provider.getScannerInfoConsoleParser().createScannerInfoConsoleParser();
		}
		return null;
	}
}
