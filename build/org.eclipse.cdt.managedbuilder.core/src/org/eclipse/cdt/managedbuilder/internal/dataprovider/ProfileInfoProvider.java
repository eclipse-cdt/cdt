/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.internal.core.scannerconfig.CfgDiscoveredPathManager;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ProfileInfoProvider {
	static class DiscoveredEntry {
		private String fName;
		private String fValue;

		public DiscoveredEntry(IPath path){
			fName = path.toString();
			fValue = fName;
		}

		public DiscoveredEntry(String name){
			fName = name;
			fValue = fName;
		}

		public DiscoveredEntry(String name, String value){
			fName = name;
			fValue = value;
		}
		
		public String getName(){
			return fName;
		}
		
		public String getValue(){
			return fValue;
		}
	}

	private BuildLanguageData fLlanguageData;
//	private String fProfileId;
//    private IScannerInfoCollector fCollector;
//    private boolean fDataCollected;
//    private IProject fProject;
//    private IResource fResource;
//    private IPath fRcPath;
	private IProject fProject;
    private CfgInfoContext fContext;
    private CfgDiscoveredPathManager fMngr;
	
	public ProfileInfoProvider(BuildLanguageData lData){
		fLlanguageData = lData;
		IResourceInfo rcInfo = lData.getTool().getParentResourceInfo();
		fContext = new CfgInfoContext(rcInfo, lData.getTool(), lData.getInputType());
		fMngr = CfgDiscoveredPathManager.getInstance();
		IResource rc = rcInfo.getParent().getOwner(); 
		fProject = rc != null ? rc.getProject() : null;

//		clear();
	}
	
	void checkUpdateInputType(IInputType inType){
		if(inType != fContext.getInputType()){
//			IResourceInfo rcInfo = fContext.getResourceInfo();
//			if(rcInfo == null){
//				rcInfo = fContext.getConfiguration().getRootFolderInfo();
//			}
			fContext = new CfgInfoContext(fContext.getResourceInfo(), fContext.getTool(), inType);
		}
	}
	
//	public void clear(){
//		fDataCollected = false;
//	}
//	
//	private void invoke(){
//		if(fDataCollected)
//			return;
//		fDataCollected = true;
//		
//		fProfileId = fLlanguageData.getDiscoveryProfileId();
//
//		if (fProfileId != null){
//
//	        SCProfileInstance profileInstance = null;
//	        IResourceInfo rcInfo = fLlanguageData.getTool().getParentResourceInfo();
//	        fProject = rcInfo.getParent().getOwner().getProject();
//	        fRcPath = rcInfo.getPath();
//	        fResource = fProject.findMember(fRcPath);
//
//	        if(fResource != null){
//	        	//FIXME:
//	        	InfoContext context = ScannerConfigUtil.createContextForProject(fProject);
//		        profileInstance = ScannerConfigProfileManager.getInstance().
//			                getSCProfileInstance(fProject, context, fProfileId);
//		        fCollector = profileInstance.createScannerInfoCollector();
//		        
//		//        synchronized(this) {
//					if (fCollector != null) {
//						if(fCollector instanceof IManagedScannerInfoCollector)
//							((IManagedScannerInfoCollector)fCollector).setProject(fProject);
//						calculateEntriesDynamically(fProject, profileInstance, fCollector);
//					}
//		//		}
//	        }
//		}
//	}
//    
	public DiscoveredEntry[] getEntryValues(int kind){
//		ScannerInfoTypes type = kindToType(kind);
//		if(type != null){
		if(fProject != null){
			try {
				PathInfo info = fMngr.getDiscoveredInfo(fProject, fContext);
				if(info != null){
					return entriesForKind(info, kind);
				}
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
//		}
		return new DiscoveredEntry[0];
	}
	
//	private ScannerInfoTypes kindToType(int kind){
//		switch (kind) {
//		case ICLanguageSettingEntry.INCLUDE_PATH:
//			return ScannerInfoTypes.INCLUDE_PATHS;
//		case ICLanguageSettingEntry.MACRO:
//			return ScannerInfoTypes.SYMBOL_DEFINITIONS;
//		}
//		return null;
//	}
	
	private DiscoveredEntry[] entriesForKind(PathInfo info, int kind){
		switch (kind) {
		case ICLanguageSettingEntry.INCLUDE_PATH:
			DiscoveredEntry[] incPaths = calculateEntries(info.getIncludePaths());
			IPath[] quotedPaths = info.getQuoteIncludePaths();
			if(quotedPaths.length != 0){
				if(incPaths.length != 0){
					DiscoveredEntry quotedEntries[] = calculateEntries(quotedPaths);
					DiscoveredEntry[] tmp = new DiscoveredEntry[incPaths.length + quotedEntries.length];
					System.arraycopy(incPaths, 0, tmp, 0, incPaths.length);
					System.arraycopy(quotedEntries, 0, tmp, incPaths.length, quotedEntries.length);
					incPaths = tmp;
				} else {
					incPaths = calculateEntries(quotedPaths);
				}
			}
			return incPaths;
		case ICLanguageSettingEntry.MACRO:
			return calculateEntries(info.getSymbols());
		case ICLanguageSettingEntry.MACRO_FILE:
			return calculateEntries(info.getMacroFiles());
		case ICLanguageSettingEntry.INCLUDE_FILE:
			return calculateEntries(info.getIncludeFiles());
		}
		return new DiscoveredEntry[0];
	}
	
	private DiscoveredEntry[] calculateEntries(Map map){
		DiscoveredEntry entries[] = new DiscoveredEntry[map.size()];
		int num = 0;
		for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			String name = (String)entry.getKey();
			String value = (String)entry.getValue();
			entries[num++] = new DiscoveredEntry(name, value);
		}
		return entries;
	}

	private DiscoveredEntry[] calculateEntries(String[] values){
		DiscoveredEntry entries[] = new DiscoveredEntry[values.length];
		for(int i = 0; i < values.length; i++){
			String name = values[i];
			entries[i] = new DiscoveredEntry(name);
		}
		return entries;
	}

	private DiscoveredEntry[] calculateEntries(IPath[] values){
		DiscoveredEntry entries[] = new DiscoveredEntry[values.length];
		for(int i = 0; i < values.length; i++){
			String name = values[i].toString();
			entries[i] = new DiscoveredEntry(name);
		}
		return entries;
	}

	private DiscoveredEntry[] calculateEntries(List list){
		DiscoveredEntry entries[] = new DiscoveredEntry[list.size()];
		int num = 0;
		for(Iterator iter = list.iterator(); iter.hasNext();){
			String name = (String)iter.next();
			entries[num++] = new DiscoveredEntry(name);
		}
		return entries;
	}

//
//	private void calculateEntriesDynamically(final IProject project, 
//            SCProfileInstance profileInstance, 
//            final IScannerInfoCollector collector) {
//		// TODO Get the provider from the toolchain specification
//		
//		final IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.
//								createScannerConfigBuildInfo2(ManagedBuilderCorePlugin.getDefault().getPluginPreferences(),
//								profileInstance.getProfile().getId(), false);
//		List providerIds = buildInfo.getProviderIdList();
//		for (Iterator i = providerIds.iterator(); i.hasNext(); ) {
//			final String providerId = (String) i.next();
//			final IExternalScannerInfoProvider esiProvider = profileInstance.createExternalScannerInfoProvider(providerId);
//		
//			// Set the arguments for the provider
//			
//			ISafeRunnable runnable = new ISafeRunnable() {
//				public void run() {
//				IProgressMonitor monitor = new NullProgressMonitor();
//				esiProvider.invokeProvider(monitor, project, providerId, buildInfo, collector);
//				}
//		
//				public void handleException(Throwable exception) {
//					if (exception instanceof OperationCanceledException) {
//						throw (OperationCanceledException) exception;
//					}
//				}
//			};
//			Platform.run(runnable);
//		}
//	}
//  
//	
}
