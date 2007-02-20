/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.make.core.scannerconfig.IConfigurationScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigScope;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
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
import org.eclipse.cdt.newmake.internal.core.MakeMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


public class DiscoveredPathManager implements IDiscoveredPathManager, IResourceChangeListener {

//	private Map fDiscoveredMap = new HashMap();
	private List listeners = Collections.synchronizedList(new ArrayList());

	private static final int INFO_CHANGED = 1;
	private static final int INFO_REMOVED = 2;

	public DiscoveredPathManager() {
        
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
                    DiscoveredScannerInfoStore.getInstance().updateScannerConfigStore(event.getDelta());
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

	public IDiscoveredPathInfo getDiscoveredInfo(IProject project) throws CoreException {
//	    IDiscoveredPathInfo info = (IDiscoveredPathInfo)fDiscoveredMap.get(project);
//		if (info == null) {
//			info = loadPathInfo(project);
//			fDiscoveredMap.put(project, info);
//		}
//		return info;
//		InfoContext c = ScannerConfigUtil.createContextForProject(project);
//		if(c != null)
			return getDiscoveredInfo(project, null);
//		return null;
	}
	
	private IDiscoveredPathInfo loadPathInfo(IConfiguration cfg, InfoContext c) throws CoreException {
        IDiscoveredPathInfo pathInfo = null;
        
        IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(c);
        IScannerInfoCollector collector = null;
        if(buildInfo != null){
	        String profileId = buildInfo.getSelectedProfileId();
	        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
	                getSCProfileInstance(c, profileId);
	        collector = profileInstance.getScannerInfoCollector();
	        
	        if (collector instanceof IScannerInfoCollector2) {
	            IScannerInfoCollector2 collector2 = (IScannerInfoCollector2) collector;
	            pathInfo = collector2.createPathInfoObject();
	            if(collector2 instanceof IScannerInfoCollector3){
	            	IScannerInfoCollector3 collector3 = (IScannerInfoCollector3)collector2;
	            	if(!collector3.isInfoCollected()){
	    	            setCachedPathInfo(c, pathInfo);
	            		SCProfileInstance instance = ScannerConfigBuilder.build(c, buildInfo, 0, new NullProgressMonitor());
	            		if(instance != null){
	            			IScannerInfoCollector newC = instance.getScannerInfoCollector();
	            			if(newC instanceof IScannerInfoCollector2){
	            				pathInfo = ((IScannerInfoCollector2)newC).createPathInfoObject();
	            			}
	            		}
	            	}
	            }
	        }
        }
        
        if(pathInfo == null) {
	        	IProject project = c.getConfiguration().getOwner().getProject();
	            pathInfo = new DiscoveredPathInfo(project, collector);
        }
		return pathInfo;
	}
	
//	private IDiscoveredPathInfo createPathInfo(IProject project, InfoContext context) throws CoreException{
//        IConfigurationScannerConfigBuilderInfo cfgInfo = ScannerConfigProfileManager.getCfgScannerConfigBuildInfo(context.getConfiguration());
//        
//        IScannerConfigBuilderInfo2 info2 =cfgInfo.getInfo(context);
//        if(info2 != null){
//        	ScannerConfigBuilder.build(context, info2, 0, new NullProgressMonitor());
//        }
//        info = new DiscoveredPathInfo(project);
//		return info;
//	}

	public void removeDiscoveredInfo(IProject project) {
		removeDiscoveredInfo(project, null);
		
//		IDiscoveredPathInfo info = (IDiscoveredPathInfo)fDiscoveredMap.remove(project);
//		if (info != null) {
//			fireUpdate(INFO_REMOVED, info);
//		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager#updateDiscoveredInfo(org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo, java.util.List)
     */
    public void updateDiscoveredInfo(IDiscoveredPathInfo info, List changedResources) throws CoreException {
//    	InfoContext c = ScannerConfigUtil.createContextForProject(info.getProject());
//    	if(c != null)
    		updateDiscoveredInfo(info, null, changedResources);
//		if (fDiscoveredMap.get(info.getProject()) != null) {
//            IDiscoveredScannerInfoSerializable serializable = info.getSerializable();
//			if (serializable != null) {
//                IProject project = info.getProject();
//				DiscoveredScannerInfoStore.getInstance().saveDiscoveredScannerInfoToState(project, serializable);
//				fireUpdate(INFO_CHANGED, info);
//                
////				ICProject cProject = CoreModel.getDefault().create(info.getProject());
////				if (cProject != null) {
////					CoreModel.setPathEntryContainer(new ICProject[]{cProject},
////							new DiscoveredPathContainer(info.getProject()), null);
////				}
//                IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);
//                String profileId = buildInfo.getSelectedProfileId();
//                ScannerConfigScope profileScope = ScannerConfigProfileManager.getInstance().
//                        getSCProfileConfiguration(profileId).getProfileScope();
//                changeDiscoveredContainer(project, profileScope, changedResources);
//			}
//			else {
//		        throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
//		                MakeMessages.getString("DiscoveredPathManager.Info_Not_Serializable"), null)); //$NON-NLS-1$
//			}
//		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager#changeDiscoveredContainer(org.eclipse.core.resources.IProject, java.lang.String)
     */
    public void changeDiscoveredContainer(final IProject project, final ScannerConfigScope profileScope, final List changedResources) {
//    	InfoContext c = ScannerConfigUtil.createContextForProject(project);
//    	if(c != null){
    		changeDiscoveredContainer(project, null, profileScope, changedResources);
//    	}
//        // order here is of essence
//        // 1. clear DiscoveredPathManager's path info cache
//        IDiscoveredPathInfo oldInfo = (IDiscoveredPathInfo) fDiscoveredMap.remove(project);
//        
//        // 2. switch the containers
//        try {
//        	IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
//        		public void run(IProgressMonitor monitor) throws CoreException {
//        			ICProject cProject = CoreModel.getDefault().create(project);
//        			if (ScannerConfigScope.PROJECT_SCOPE.equals(profileScope)) {
//        				CoreModel.setPathEntryContainer(new ICProject[]{cProject},
//        						new DiscoveredPathContainer(project), null);
//        			}
//        			else if (ScannerConfigScope.FILE_SCOPE.equals(profileScope)) {
//        				PerFileDiscoveredPathContainer container = new PerFileDiscoveredPathContainer(project);
//        				CoreModel.setPathEntryContainer(new ICProject[]{cProject},
//        						container, null);
//        				if (changedResources != null) {
//        					List changeDelta = new ArrayList(changedResources.size());
//        					for (Iterator i = changedResources.iterator(); i.hasNext(); ) {
//        						IResource resource = (IResource) i.next();
//        						IPath path = resource.getFullPath();
//        						changeDelta.add(new PathEntryContainerChanged(path, 
//        								PathEntryContainerChanged.INCLUDE_CHANGED | 
//        								PathEntryContainerChanged.MACRO_CHANGED)); // both include paths and symbols changed
//        					}
//        					CoreModel.pathEntryContainerUpdates(container, 
//        							(PathEntryContainerChanged[]) changeDelta.toArray(new PathEntryContainerChanged[changeDelta.size()]), 
//        							null);
//        				}
//        			}
//        			else {
//        				ManagedBuilderCorePlugin.log(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), 1,
//        						MakeMessages.getString("DiscoveredContainer.ScopeErrorMessage"), null)); //$NON-NLS-1$
//        			}
//        			
//        		}
//        	};
//        	CoreModel.run(runnable, null);
//        }
//        catch (CoreException e) {
//        	ManagedBuilderCorePlugin.log(e);
//        }
//        
//        // 3. clear the container's path entry cache
//        if (oldInfo != null) {
//            fireUpdate(INFO_REMOVED, oldInfo);
//        }
    }

	private void fireUpdate(final int type, final IDiscoveredPathInfo info) {
		Object[] list = listeners.toArray();
		for (int i = 0; i < list.length; i++) {
			final IDiscoveredInfoListener listener = (IDiscoveredInfoListener)list[i];
			if (listener != null) {
				Platform.run(new ISafeRunnable() {
		
					public void handleException(Throwable exception) {
						IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
								CCorePlugin.getResourceString("CDescriptorManager.exception.listenerError"), exception); //$NON-NLS-1$
						CCorePlugin.log(status);
					}
		
					public void run() throws Exception {
						switch (type) {
							case INFO_CHANGED :
								listener.infoChanged(info);
								break;
							case INFO_REMOVED :
								listener.infoRemoved(info);
								break;
						}
					}
				});
			}
		}
	}

	public void addDiscoveredInfoListener(IDiscoveredInfoListener listener) {
		listeners.add(listener);
	}

	public void removeDiscoveredInfoListener(IDiscoveredInfoListener listener) {
		listeners.remove(listener);
	}

	public void changeDiscoveredContainer(final IProject project,
			InfoContext c, final ScannerConfigScope profileScope,
			final List changedResources) {
        // order here is of essence
        // 1. clear DiscoveredPathManager's path info cache
        final InfoContext context = adjustContext(c);
//		Tool tool = (Tool)context.getTool();
        IDiscoveredPathInfo oldInfo = setCachedPathInfo(context, null);
//        if(context == null)
//        	context = ScannerConfigUtil.createContextForProject(project);
        // 2. switch the containers
        try {
        	IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
        		public void run(IProgressMonitor monitor) throws CoreException {
//        			ICProject cProject = CoreModel.getDefault().create(project);
//        			if (ScannerConfigScope.PROJECT_SCOPE.equals(profileScope)) {
//        				CoreModel.setPathEntryContainer(new ICProject[]{cProject},
//        						new DiscoveredPathContainer(project), null);
//        			}
//        			else if (ScannerConfigScope.FILE_SCOPE.equals(profileScope)) {
//        				PerFileDiscoveredPathContainer container = new PerFileDiscoveredPathContainer(project);
//        				CoreModel.setPathEntryContainer(new ICProject[]{cProject},
//        						container, null);
//        				if (changedResources != null) {
//        					List changeDelta = new ArrayList(changedResources.size());
//        					for (Iterator i = changedResources.iterator(); i.hasNext(); ) {
//        						IResource resource = (IResource) i.next();
//        						IPath path = resource.getFullPath();
//        						changeDelta.add(new PathEntryContainerChanged(path, 
//        								PathEntryContainerChanged.INCLUDE_CHANGED | 
//        								PathEntryContainerChanged.MACRO_CHANGED)); // both include paths and symbols changed
//        					}
//        					CoreModel.pathEntryContainerUpdates(container, 
//        							(PathEntryContainerChanged[]) changeDelta.toArray(new PathEntryContainerChanged[changeDelta.size()]), 
//        							null);
//        				}
//        			}
//        			else {
//        				ManagedBuilderCorePlugin.log(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), 1,
//        						MakeMessages.getString("DiscoveredContainer.ScopeErrorMessage"), null)); //$NON-NLS-1$
//        			}
//
        			if(context == null)
        				ManagedBuildManager.updateCoreSettings(project);
        			else
        				ManagedBuildManager.updateCoreSettings(context.getConfiguration());
        		}
        	};
        	CoreModel.run(runnable, null);
        }
        catch (CoreException e) {
        	ManagedBuilderCorePlugin.log(e);
        }
        
        // 3. clear the container's path entry cache
        if (oldInfo != null) {
            fireUpdate(INFO_REMOVED, oldInfo);
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


	public IDiscoveredPathInfo getDiscoveredInfo(IProject project,
			InfoContext context) throws CoreException {
        if(context == null)
        	context = ScannerConfigUtil.createContextForProject(project);

        context = adjustContext(context);

	    IDiscoveredPathInfo info = getCachedPathInfo(context);
		if (info == null) {
			info = loadPathInfo(context.getConfiguration(), context);
			setCachedPathInfo(context, info);
//			if(info instanceof DiscoveredPathInfo && !((DiscoveredPathInfo)info).isLoadded()){
//				info = createPathInfo(project, context);
//				setCachedPathInfo(context, info);
//			}
		}
		return info;
	}
	
	private IDiscoveredPathInfo getCachedPathInfo(InfoContext context){
        IConfigurationScannerConfigBuilderInfo cfgInfo = ScannerConfigProfileManager.getCfgScannerConfigBuildInfo(context.getConfiguration());
        IDiscoveredPathInfo info = null;
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

	private InfoContext adjustContext(InfoContext context){
		return adjustContext(context, null);
		
	}

	private InfoContext adjustContext(InfoContext context, IConfigurationScannerConfigBuilderInfo cfgInfo){
		if(cfgInfo == null)
			cfgInfo = ScannerConfigProfileManager.getCfgScannerConfigBuildInfo(context.getConfiguration());

        boolean queryCfg = !cfgInfo.isPerRcTypeDiscovery();
        
        Tool tool = (Tool)context.getTool();
        IResourceInfo rcInfo = context.getResourceInfo();
        IInputType inType = context.getInputType();
        boolean adjust = false;
        InfoContext newContext = context;
        
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
        		newContext = new InfoContext(context.getConfiguration());
        	else
        		newContext = new InfoContext(rcInfo, tool, inType);
        }
        
        return newContext;
	}
	
	private IDiscoveredPathInfo setCachedPathInfo(InfoContext context, IDiscoveredPathInfo info){
        IConfigurationScannerConfigBuilderInfo cfgInfo = ScannerConfigProfileManager.getCfgScannerConfigBuildInfo(context.getConfiguration());
        boolean cacheOnCfg = !cfgInfo.isPerRcTypeDiscovery();
        IDiscoveredPathInfo oldInfo = null;
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

	public void removeDiscoveredInfo(IProject project, InfoContext context) {
        if(context == null)
        	context = ScannerConfigUtil.createContextForProject(project);

        context = adjustContext(context);

		IDiscoveredPathInfo info = setCachedPathInfo(context, null);
		if (info != null) {
			fireUpdate(INFO_REMOVED, info);
		}
	}

	public void updateDiscoveredInfo(IDiscoveredPathInfo info,
			InfoContext context, List changedResources) throws CoreException {
//		if(context == null){
//			updateDiscoveredInfo(info, changedResources);
//			return;
//		}
        if(context == null)
        	context = ScannerConfigUtil.createContextForProject(info.getProject());
        context = adjustContext(context);

		IDiscoveredPathInfo oldInfo = getCachedPathInfo(context);
		if (oldInfo != null) {
            IDiscoveredScannerInfoSerializable serializable = info.getSerializable();
			if (serializable != null) {
				DiscoveredScannerInfoStore.getInstance().saveDiscoveredScannerInfoToState(context, serializable);
				fireUpdate(INFO_CHANGED, info);
                
//				ICProject cProject = CoreModel.getDefault().create(info.getProject());
//				if (cProject != null) {
//					CoreModel.setPathEntryContainer(new ICProject[]{cProject},
//							new DiscoveredPathContainer(info.getProject()), null);
//				}
                IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(context);
                String profileId = buildInfo.getSelectedProfileId();
                ScannerConfigScope profileScope = ScannerConfigProfileManager.getInstance().
                        getSCProfileConfiguration(profileId).getProfileScope();
//                changeDiscoveredContainer(context.getConfiguration().getOwner().getProject(), 
//                		context,
//                		profileScope, 
//                		changedResources);
			}
			else {
		        throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
		                MakeMessages.getString("DiscoveredPathManager.Info_Not_Serializable"), null)); //$NON-NLS-1$
			}
			setCachedPathInfo(context, info);
		}
	}

}
