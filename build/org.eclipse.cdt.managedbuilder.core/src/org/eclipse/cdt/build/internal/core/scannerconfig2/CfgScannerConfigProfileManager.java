/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.internal.core.scannerconfig2;

import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigScope;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;

public class CfgScannerConfigProfileManager {
	
	public static ICfgScannerConfigBuilderInfo2Set getCfgScannerConfigBuildInfo(IConfiguration cfg){
		return CfgScannerConfigInfoFactory2.create(cfg);
	}
	
	public static boolean isPerFileProfile(String profileId){
		ScannerConfigProfile profile = ScannerConfigProfileManager.getInstance().getSCProfileConfiguration(profileId);
		ScannerConfigScope scope = profile.getProfileScope();
		return scope!=null && scope.equals(ScannerConfigScope.FILE_SCOPE);
	}
	
	public static InfoContext createDefaultContext(IProject project){
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration cfg = null;
		if(info != null && info.isValid()){
			cfg = info.getDefaultConfiguration();
		}
		
		if(cfg != null)
			return new CfgInfoContext(cfg).toInfoContext();
		return new InfoContext(project);
	}

	public static boolean disableScannerDiscovery(IConfiguration cfg) {
		boolean isChanged = false;

		ICfgScannerConfigBuilderInfo2Set info2set = getCfgScannerConfigBuildInfo(cfg);
		Map<CfgInfoContext, IScannerConfigBuilderInfo2> infoMap = info2set.getInfoMap();
		Collection<IScannerConfigBuilderInfo2> infos = infoMap.values();
		for (IScannerConfigBuilderInfo2 info2 : infos) {
			isChanged = isChanged || info2.isAutoDiscoveryEnabled();
			info2.setAutoDiscoveryEnabled(false);
		}
		return isChanged;
	}

	public static boolean disableScannerDiscovery(ICProjectDescription prjDescription) {
		boolean isChanged = false;

		ICConfigurationDescription[] cfgDescs = prjDescription.getConfigurations();
		if (cfgDescs!=null) {
			for (ICConfigurationDescription cfgDesc : cfgDescs) {
				IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDesc);
				boolean changed=CfgScannerConfigProfileManager.disableScannerDiscovery(cfg);
				if (changed) {
					isChanged = true;
				}

			}
		}
		return isChanged;
	}
}
