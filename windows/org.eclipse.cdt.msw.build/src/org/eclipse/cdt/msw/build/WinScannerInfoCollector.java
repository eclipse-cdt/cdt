/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.msw.build;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
public class WinScannerInfoCollector implements IScannerInfoCollector3 {

	@Override
	public void contributeToScannerConfig(Object resource, Map scannerInfo) {
	}

	@Override
	public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
		return null;
	}

	@Override
	public IDiscoveredPathInfo createPathInfoObject() {
		return new WinDiscoveredPathInfo();
	}

	@Override
	public void setInfoContext(InfoContext context) {
	}

	@Override
	public void setProject(IProject project) {
	}

	@Override
	public void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException {
	}

}
