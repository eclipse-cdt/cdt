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

import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.Action;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.BuildOutputProvider;
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
	/**
	 * 
	 */
	public SCProfileInstance(IProject project, ScannerConfigProfile profile) {
		this.project = project;
		this.profile = profile;
		instantiate();
	}
	/**
	 * 
	 */
	private void instantiate() {
		// create collector object
		collector = (IScannerInfoCollector) profile.getScannerInfoCollectorElement().createScannerInfoCollector();
        if (collector instanceof IScannerInfoCollector2) {
            ((IScannerInfoCollector2) collector).setProject(project);
        }
		// all other objects are created on request
	}

	/**
	 * @return
	 */
	public ScannerConfigProfile getProfile() {
		return profile;
	}

	/**
	 * @return a single scannerInfoCollector object
	 */
	public IScannerInfoCollector getScannerInfoCollector() {
		return collector;
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
