package org.eclipse.cdt.build.internal.core.scannerconfig2;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;

public class CfgScannerConfigProfileManager {
	
	public static ICfgScannerConfigBuilderInfo2Set getCfgScannerConfigBuildInfo(IConfiguration cfg){
		return CfgScannerConfigInfoFactory2.create(cfg);
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
