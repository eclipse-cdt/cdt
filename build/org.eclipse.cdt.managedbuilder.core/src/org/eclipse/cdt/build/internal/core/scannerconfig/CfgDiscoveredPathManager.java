/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.internal.core.scannerconfig;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;


public class CfgDiscoveredPathManager implements IResourceChangeListener {

	public static CfgDiscoveredPathManager fInstance;

	private IDiscoveredPathManager fBaseMngr;
	
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
       				ManagedBuildManager.updateCoreSettings(project, cfgs);
        		}
        	};
        	CoreModel.run(runnable, null);
        }
        catch (CoreException e) {
        	ManagedBuilderCorePlugin.log(e);
        }
	}


	public IDiscoveredPathManager.IDiscoveredPathInfo getDiscoveredInfo(IProject project,
			CfgInfoContext context) throws CoreException {

        context = adjustContext(context);

        IDiscoveredPathManager.IDiscoveredPathInfo info = getCachedPathInfo(context);
		if (info == null) {
			info = loadPathInfo(project, context.getConfiguration(), context);
			setCachedPathInfo(context, info);
//			if(info instanceof DiscoveredPathInfo && !((DiscoveredPathInfo)info).isLoadded()){
//				info = createPathInfo(project, context);
//				setCachedPathInfo(context, info);
//			}
		}
		return info;
	}
	
	private IDiscoveredPathManager.IDiscoveredPathInfo loadPathInfo(IProject project, IConfiguration cfg, CfgInfoContext context) throws CoreException{
		IDiscoveredPathManager.IDiscoveredPathInfo info = fBaseMngr.getDiscoveredInfo(cfg.getOwner().getProject(), context.toInfoContext());
		if(!DiscoveredScannerInfoStore.getInstance().hasInfo(project, context.toInfoContext(), info.getSerializable())){
			setCachedPathInfo(context, info);
			ICfgScannerConfigBuilderInfo2Set container = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(context.getConfiguration());
			IScannerConfigBuilderInfo2 buildInfo = container.getInfo(context);
			if(buildInfo != null){
        		SCProfileInstance instance = ScannerConfigBuilder.build(context, buildInfo, 0, null, new NullProgressMonitor());
        		if(instance != null){
        			IScannerInfoCollector newC = instance.getScannerInfoCollector();
        			if(newC instanceof IScannerInfoCollector2){
        				info = ((IScannerInfoCollector2)newC).createPathInfoObject();
//        				setCachedPathInfo(context, info);
        			}
        		}
			}
		}
		return info;
	}
	
	private IDiscoveredPathManager.IDiscoveredPathInfo getCachedPathInfo(CfgInfoContext context){
        ICfgScannerConfigBuilderInfo2Set cfgInfo = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(context.getConfiguration());
        IDiscoveredPathManager.IDiscoveredPathInfo info = null;
        boolean queryCfg = !cfgInfo.isPerRcTypeDiscovery();
        if(!queryCfg){
    		Tool tool = (Tool)context.getTool();
    		if(tool != null){
    			info = tool.getDiscoveredPathInfo(context.getInputType());
    		} else {
    			queryCfg = true;
    		}
        } 
        if(queryCfg) {
        	info = ((Configuration)context.getConfiguration()).getDiscoveredPathInfo();
        }
        return info;
	}

	private CfgInfoContext adjustContext(CfgInfoContext context){
		return adjustContext(context, null);
		
	}

	private CfgInfoContext adjustContext(CfgInfoContext context, ICfgScannerConfigBuilderInfo2Set cfgInfo){
		if(cfgInfo == null)
			cfgInfo = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(context.getConfiguration());

        boolean queryCfg = !cfgInfo.isPerRcTypeDiscovery();
        
        Tool tool = (Tool)context.getTool();
        IResourceInfo rcInfo = context.getResourceInfo();
        IInputType inType = context.getInputType();
        boolean adjust = false;
        CfgInfoContext newContext = context;
        
        if(!queryCfg){
    		if(tool != null){
    			if(inType != null){
	        		if(!tool.hasScannerConfigSettings(inType)){
//	        			tool = null;
	        			inType = null;
	        			adjust = true;
	        		}
    			}
    			if(inType == null){
	        		if(!tool.hasScannerConfigSettings(null)){
	        			tool = null;
	        			adjust = true;
	        		}
    			}
    		}
    		if(tool == null){
    			if(inType != null){
    				inType = null;
    				adjust = true;
    			}
    			
    			if(rcInfo != null){
	    			ToolChain tc = rcInfo instanceof FolderInfo ? 
	    					(ToolChain)((FolderInfo)rcInfo).getToolChain()
	    					: (ToolChain)((ResourceConfiguration)rcInfo).getBaseToolChain();
	    					
	    			if(tc != null){
	    				if(!tc.hasScannerConfigSettings()){
	    					adjust = true;
	    					rcInfo = null;
	    				}
	    			}
    			}
    		}
        } else {
        	if(tool != null){
        		tool = null;
        		adjust = true;
        	}
        	if(rcInfo != null){
        		rcInfo = null;
        		adjust = true;
        	}
        	if(inType != null){
        		inType = null;
        		adjust = true;
        	}
        }
        
        if(adjust){
        	if(rcInfo == null)
        		newContext = new CfgInfoContext(context.getConfiguration());
        	else
        		newContext = new CfgInfoContext(rcInfo, tool, inType);
        }
        
        return newContext;
	}
	
	private IDiscoveredPathManager.IDiscoveredPathInfo setCachedPathInfo(CfgInfoContext context, IDiscoveredPathManager.IDiscoveredPathInfo info){
        ICfgScannerConfigBuilderInfo2Set cfgInfo = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(context.getConfiguration());
        boolean cacheOnCfg = !cfgInfo.isPerRcTypeDiscovery();
        IDiscoveredPathManager.IDiscoveredPathInfo oldInfo = null;
        if(!cacheOnCfg){
    		Tool tool = (Tool)context.getTool();
    		if(tool != null){
    			if(info != null)
    				oldInfo = tool.setDiscoveredPathInfo(context.getInputType(), info);
    			else
    				oldInfo = tool.clearDiscoveredPathInfo(context.getInputType());
    		}
        } else {
        	if(info != null)
        		oldInfo = ((Configuration)context.getConfiguration()).setDiscoveredPathInfo(info);
        	else
        		oldInfo = ((Configuration)context.getConfiguration()).clearDiscoveredPathInfo();
        }
        return oldInfo;
	}

	public void removeDiscoveredInfo(IProject project, CfgInfoContext context) {
//        if(context == null)
//        	context = ScannerConfigUtil.createContextForProject(project);

        context = adjustContext(context);

        IDiscoveredPathManager.IDiscoveredPathInfo info = setCachedPathInfo(context, null);
        fBaseMngr.removeDiscoveredInfo(project, context.toInfoContext());
//		if (info != null) {
//			fireUpdate(INFO_REMOVED, info);
//		}
	}
}
