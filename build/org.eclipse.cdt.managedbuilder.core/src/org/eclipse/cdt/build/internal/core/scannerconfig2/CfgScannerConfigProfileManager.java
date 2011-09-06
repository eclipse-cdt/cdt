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

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
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
}
