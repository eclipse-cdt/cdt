/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.build.internal.core.scannerconfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.build.internal.core.scannerconfig.PerFileSettingsCalculator.ILangSettingInfo;
import org.eclipse.cdt.build.internal.core.scannerconfig.PerFileSettingsCalculator.IRcSettingInfo;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildFileData;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildFolderData;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildLanguageData;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;


public class CfgDiscoveredPathManager implements IResourceChangeListener {

	public static CfgDiscoveredPathManager fInstance;

	private IDiscoveredPathManager fBaseMngr;
	
	/** Deadlock-safe mutex lock */
	private ILock lock = Job.getJobManager().newLock();

	private static class ContextInfo {
		
		public ContextInfo() {
		}
		CfgInfoContext fInitialContext;
		CfgInfoContext fCacheContext;
		CfgInfoContext fLoadContext;
		ICfgScannerConfigBuilderInfo2Set fCfgInfo;
		IScannerConfigBuilderInfo2 fInfo;
		boolean fIsPerFileCache;
	}
	
	public static class PathInfoCache{
		private PathInfo fPathInfo;
		private String fProfileId;
		
		public PathInfo getPathInfo(){
			return fPathInfo;
		}
		
		private PathInfoCache(String profileId, PathInfo pathInfo){
			this.fProfileId = profileId;
			this.fPathInfo = pathInfo;
		}
	}
	
	private CfgDiscoveredPathManager() {
        fBaseMngr = MakeCorePlugin.getDefault().getDiscoveryManager();
	}
	
	public static CfgDiscoveredPathManager getInstance(){
		if(fInstance == null){
			fInstance = new CfgDiscoveredPathManager();
			fInstance.startup();
		}
		return fInstance;
	}
	
	public static void stop(){
		if(fInstance != null)
			fInstance.shutdown();
	}
	
	public void startup() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			IResource resource = event.getResource();

			switch (event.getType()) {
                case IResourceChangeEvent.POST_CHANGE :
//                    DiscoveredScannerInfoStore.getInstance().updateScannerConfigStore(event.getDelta());
                    break;
				case IResourceChangeEvent.PRE_DELETE :
				case IResourceChangeEvent.PRE_CLOSE :
					if (resource.getType() == IResource.PROJECT) {
//						fDiscoveredMap.remove(resource);
					}
					break;
			}
		}
	}

	public void updateCoreSettings(final IProject project, final IConfiguration cfgs[]) {
        try {
        	IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
        		public void run(IProgressMonitor monitor) throws CoreException {
       				ManagedBuildManager.updateCoreSettings(project, cfgs, true);
        		}
        	};
        	CoreModel.run(runnable, null);
        }
        catch (CoreException e) {
        	ManagedBuilderCorePlugin.log(e);
        }
	}


	public PathInfo getDiscoveredInfo(IProject project,
			CfgInfoContext context) throws CoreException {

		ContextInfo cInfo = getContextInfo(context);

        PathInfo info = getCachedPathInfo(cInfo);
		if (info == null) {
			try {
				lock.acquire();
				info = getCachedPathInfo(cInfo);

				if(info == null){
					IDiscoveredPathManager.IDiscoveredPathInfo baseInfo = loadPathInfo(project, context.getConfiguration(), cInfo);
					info = resolveCacheBaseDiscoveredInfo(cInfo, baseInfo);
				}
			} finally {
				lock.release();
			}
		}
		return info;
	}
	
//	private void adjustPerRcContextInfo(ContextInfo cInfo){
//		cInfo.fIsFerFileCache = true;
//		cInfo.fCacheContext = cInfo.fInitialContext;
//		cInfo.fLoadContext = new CfgInfoContext(cInfo.fInitialContext.getConfiguration());
//	}
	
	private PathInfo resolveCacheBaseDiscoveredInfo(ContextInfo cInfo, IDiscoveredPathManager.IDiscoveredPathInfo baseInfo){
		if(cInfo.fIsPerFileCache){
			if(baseInfo instanceof IDiscoveredPathManager.IPerFileDiscoveredPathInfo2){
				resolveCachePerFileInfo(cInfo, (IDiscoveredPathManager.IPerFileDiscoveredPathInfo2)baseInfo);
			}
			return getCachedPathInfo(cInfo);
		}
		
		((FolderInfo)cInfo.fLoadContext.getConfiguration().getRootFolderInfo()).setContainsDiscoveredScannerInfo(true);
		Map map = baseInfo.getSymbols();
		IPath paths[] = baseInfo.getIncludePaths();
		
		PathInfo info = new PathInfo(paths, null, map, null, null);
		setCachedPathInfo(cInfo, info);
		return info;
	}
	
	private void resolveCachePerFileInfo(ContextInfo cInfo, IDiscoveredPathManager.IPerFileDiscoveredPathInfo2 info){
		CConfigurationData data = cInfo.fLoadContext.getConfiguration().getConfigurationData();
		if(data == null)
			return;
		
		PerFileSettingsCalculator calculator = new PerFileSettingsCalculator();
		IRcSettingInfo[] rcInfos = calculator.getSettingInfos(cInfo.fLoadContext.getConfiguration().getOwner().getProject(), data, info, true);
		
		CResourceData rcDatas[] = data.getResourceDatas();
		Map rcDataMap = new HashMap();
		CResourceData rcData;
		for(int i = 0; i < rcDatas.length; i++){
			rcData = rcDatas[i];
			rcDataMap.put(rcData.getPath(), rcData);
		}
		
		IRcSettingInfo rcInfo;
		IPath path; 
		boolean rootSettingFound = false;
		boolean fileSettingFound = false;
		for(int i = 0; i < rcInfos.length; i++){
			rcInfo = rcInfos[i];
			rcData = rcInfo.getResourceData();
			path = rcData.getPath();
			if(path.segmentCount() != 0)
				fileSettingFound = true;
			else
				rootSettingFound = true;
			
			rcDataMap.remove(path);
			cache(cInfo, rcInfo);
		}
		
		if(rootSettingFound && fileSettingFound)
			((BuildFolderData)data.getRootFolderData()).setContainsDiscoveredScannerInfo(false);
		
		if(!rcDataMap.isEmpty()){
			CResourceData tmpRcData;
			for(Iterator iter = rcDataMap.values().iterator(); iter.hasNext();){
				tmpRcData = (CResourceData)iter.next();
				if(tmpRcData.getPath().segmentCount() == 0 && tmpRcData.getType() == ICSettingBase.SETTING_FOLDER){
					cache(cInfo, PerFileSettingsCalculator.createEmptyRcSettingInfo((CFolderData)tmpRcData));
				} else {
					clearCache(tmpRcData);
				}
			}
		}
	}
	
	private void cache(ContextInfo cInfo, IRcSettingInfo rcSetting){
		CResourceData rcData = rcSetting.getResourceData();
		clearCache(rcData);
		ILangSettingInfo lInfos[] = rcSetting.getLangInfos();
		for(int i = 0; i < lInfos.length; i++){
			cache(cInfo, lInfos[i]);
		}
	}
	
	private void cache(ContextInfo cInfo, ILangSettingInfo lInfo){
		BuildLanguageData bld = (BuildLanguageData)lInfo.getLanguageData();
		setCachedPathInfo(cInfo, (Configuration)bld.getConfiguration(), (Tool)bld.getTool(), bld.getInputType(), lInfo.getFilePathInfo());
	}
		
	private void clearCache(CResourceData rcData){
		if(rcData.getType() == ICSettingBase.SETTING_FILE){
			IFileInfo fiInfo = ((BuildFileData)rcData).getFileInfo();
			ITool tools[] = fiInfo.getTools();
			clearCache(tools);
		} else {
			IFolderInfo foInfo = ((BuildFolderData)rcData).getFolderInfo();
			ITool[] tools = foInfo.getTools();
			clearCache(tools);
		}
	}
	
	private void clearCache(ITool[] tools){
		for(int i = 0; i < tools.length; i++){
			((Tool)tools[i]).clearAllDiscoveredInfo();
		}
	}
	
	private IDiscoveredPathManager.IDiscoveredPathInfo loadPathInfo(IProject project, IConfiguration cfg, ContextInfo cInfo) throws CoreException{
		IDiscoveredPathManager.IDiscoveredPathInfo info = fBaseMngr.getDiscoveredInfo(cfg.getOwner().getProject(), cInfo.fLoadContext.toInfoContext(), false);
		if(!DiscoveredScannerInfoStore.getInstance().hasInfo(project, cInfo.fLoadContext.toInfoContext(), info.getSerializable())){
//			setCachedPathInfo(context, info);
			ICfgScannerConfigBuilderInfo2Set container = cInfo.fCfgInfo;
			IScannerConfigBuilderInfo2 buildInfo = container.getInfo(cInfo.fLoadContext);
			if(buildInfo != null){
        		SCProfileInstance instance = ScannerConfigBuilder.build(cInfo.fLoadContext, buildInfo, 0, null, new NullProgressMonitor());
        		if(instance != null){
        			
        			IScannerInfoCollector newC = instance.getScannerInfoCollector();
        			if(newC instanceof IScannerInfoCollector2
        					&& !(newC instanceof PerFileSICollector)){
        				info = ((IScannerInfoCollector2)newC).createPathInfoObject();
//        				setCachedPathInfo(context, info);
        			}
        		}
			}
		}
		return info;
	}
	
	private PathInfo getCachedPathInfo(ContextInfo cInfo){
        PathInfo info = getCachedPathInfo(cInfo, true, true, false);
        return info;
	}

	private void removeCachedPathInfo(ContextInfo cInfo){
//      ICfgScannerConfigBuilderInfo2Set cfgInfo = cInfo.fCfgInfo;
		if(cInfo.fIsPerFileCache){
			Configuration cfg = (Configuration)cInfo.fInitialContext.getConfiguration();
			cfg.clearDiscoveredPathInfo();
			
			IResourceInfo[] infos = cfg.getResourceInfos();
			for(int i = 0; i < infos.length; i++){
				IResourceInfo rcInfo = infos[i];
				ITool[] tools = rcInfo.getTools();
				for(int k = 0; k < tools.length; k++){
					Tool tool = (Tool)tools[k];
					tool.clearAllDiscoveredPathInfo();
				}
			}
		} else {
			setCachedPathInfo(cInfo, null);
		}
			
//      PathInfo info = getCachedPathInfo(cInfo, true, true, true);
	}

	private PathInfo getCachedPathInfo(ContextInfo cInfo, boolean queryParent, boolean clearIfInvalid, boolean clear){
		return getCachedPathInfo(cInfo, (Configuration)cInfo.fCacheContext.getConfiguration(), (Tool)cInfo.fCacheContext.getTool(), cInfo.fCacheContext.getInputType(), queryParent, clearIfInvalid, clear);
	}

	private PathInfo getCachedPathInfo(ContextInfo cInfo, Configuration cfg, Tool tool, IInputType inType, boolean queryParent, boolean clearIfInvalid, boolean clear){
		PathInfoCache infoCache = getPathInfoCache(cInfo, cfg, tool, inType, queryParent, clearIfInvalid, clear);
		if(infoCache != null && isCacheValid(cInfo, infoCache))
			return infoCache.fPathInfo;
		return null;
	}
	
	private PathInfoCache getPathInfoCache(ContextInfo cInfo, Configuration cfg, Tool tool, IInputType inType, boolean queryParent, boolean clearIfInvalid, boolean clear){
		PathInfoCache info = null;
//		boolean queryCfg = false;
		if(tool != null){
			info = tool.getDiscoveredPathInfo(inType);
			if(info != null){
				if(clear || (clearIfInvalid && !isCacheValid(cInfo, info))){
					tool.clearDiscoveredPathInfo(inType);
//					fBaseMngr.removeDiscoveredInfo(cfg.getOwner().getProject(), cInfo.fLoadContext.toInfoContext());
				}
			} else if(queryParent){
//				IResourceInfo rcInfo = tool.getParentResourceInfo();
				ITool superTool = tool.getSuperClass();
				if(!superTool.isExtensionElement()){
					if(inType != null){
						IInputType superInType = null;
						String exts[] = inType.getSourceExtensions(tool);
						for(int i = 0; i < exts.length; i++){
							superInType = superTool.getInputType(exts[i]);
							if(superInType != null)
								break;
						}
						if(superInType != null){
							info = getPathInfoCache(cInfo, cfg, (Tool)superTool, superInType, true, clearIfInvalid, clear);
						}
					} else {
						info = getPathInfoCache(cInfo, cfg, (Tool)superTool, null, true, clearIfInvalid, clear);
					}
				} else {
					info = getPathInfoCache(cInfo, cfg, null, null, true, clearIfInvalid, clear);
				}
			}
		} else {
			info = cfg.getDiscoveredPathInfo();
			if(clear || (clearIfInvalid && !isCacheValid(cInfo, info))){
				cfg.clearDiscoveredPathInfo();
//				fBaseMngr.removeDiscoveredInfo(cfg.getOwner().getProject(), cInfo.fLoadContext.toInfoContext());
			}
		}
		
		return info;
	}
	
	private boolean isCacheValid(ContextInfo cInfo, PathInfoCache cache){
		if(cache == null)
			return true;
		
		if(cInfo.fInfo != null){
			String id = cInfo.fInfo.getSelectedProfileId();
			return id.equals(cache.fProfileId);
		}
		return false;
	}

	private ContextInfo getContextInfo(CfgInfoContext context){
		return getContextInfo(context, null);
	}

	private ContextInfo getContextInfo(CfgInfoContext context, ICfgScannerConfigBuilderInfo2Set cfgInfo){
		if(cfgInfo == null)
			cfgInfo = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(context.getConfiguration());

		boolean isPerRcType = cfgInfo.isPerRcTypeDiscovery();
		ContextInfo contextInfo = new ContextInfo();
		contextInfo.fInitialContext = context;
		contextInfo.fCfgInfo = cfgInfo;
		if(isPerRcType){
			contextInfo.fLoadContext = CfgScannerConfigUtil.adjustPerRcTypeContext(contextInfo.fInitialContext);
			contextInfo.fCacheContext = contextInfo.fLoadContext;
			contextInfo.fIsPerFileCache = false;
			contextInfo.fInfo = cfgInfo.getInfo(contextInfo.fLoadContext);
		} else {
			contextInfo.fLoadContext = new CfgInfoContext(context.getConfiguration());
			contextInfo.fInfo = cfgInfo.getInfo(contextInfo.fLoadContext);
			contextInfo.fIsPerFileCache = CfgScannerConfigProfileManager.isPerFileProfile(contextInfo.fInfo.getSelectedProfileId());
			contextInfo.fCacheContext = contextInfo.fIsPerFileCache ? contextInfo.fInitialContext : contextInfo.fLoadContext;
		}
        
        return contextInfo;
	}
	
	private PathInfo setCachedPathInfo(ContextInfo cInfo, PathInfo info){
		CfgInfoContext cacheContext = cInfo.fCacheContext;
		return setCachedPathInfo(cInfo, (Configuration)cacheContext.getConfiguration(), (Tool)cacheContext.getTool(), cacheContext.getInputType(), info);
	}

	
	private PathInfo setCachedPathInfo(ContextInfo cInfo, Configuration cfg, Tool tool, IInputType inType, PathInfo info){
		PathInfoCache oldInfo;
		PathInfoCache cache;
		if(info != null){
			String id = cInfo.fInfo != null ? cInfo.fInfo.getSelectedProfileId() : null;
			cache =  new PathInfoCache(id, info); 
		} else {
			cache = null;
		}
		
   		if(tool != null){
   			if(info != null)
   				oldInfo = tool.setDiscoveredPathInfo(inType, cache);
   			else
   				oldInfo = tool.clearDiscoveredPathInfo(inType);
        } else {
        	if(info != null)
        		oldInfo = cfg.setDiscoveredPathInfo(cache);
        	else
        		oldInfo = cfg.clearDiscoveredPathInfo();
        }
        return oldInfo != null ? oldInfo.fPathInfo : null;
	}

	public void removeDiscoveredInfo(IProject project, CfgInfoContext context) {
		removeDiscoveredInfo(project, context, true);
	}

	public void removeDiscoveredInfo(IProject project, CfgInfoContext context, boolean removeBaseCache) {
//        if(context == null)
//        	context = ScannerConfigUtil.createContextForProject(project);
		
		ContextInfo cInfo = getContextInfo(context);

        removeCachedPathInfo(cInfo);

        if(removeBaseCache)
        	fBaseMngr.removeDiscoveredInfo(project, cInfo.fLoadContext.toInfoContext());
	}
}
