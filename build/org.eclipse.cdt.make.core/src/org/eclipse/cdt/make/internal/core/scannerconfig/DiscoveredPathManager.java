/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.PathEntryContainerChanged;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigScope;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;

public class DiscoveredPathManager implements IDiscoveredPathManager, IResourceChangeListener {

	private Map<IProject, DiscoveredInfoHolder> fDiscoveredInfoHolderMap = new HashMap<IProject, DiscoveredInfoHolder>();
	private List<IDiscoveredInfoListener> listeners = Collections.synchronizedList(new ArrayList<IDiscoveredInfoListener>());

	private static final int INFO_CHANGED = 1;
	private static final int INFO_REMOVED = 2;

	private static class DiscoveredInfoHolder {
		Map<InfoContext, IDiscoveredPathInfo> fInfoMap = new HashMap<InfoContext, IDiscoveredPathInfo>();
//		PathSettingsContainer fContainer = PathSettingsContainer.createRootContainer();

		public IDiscoveredPathInfo getInfo(InfoContext context){
			return fInfoMap.get(context);
		}

//		private Map getMap(IPath path, boolean create, boolean exactPath){
//			PathSettingsContainer child = fContainer.getChildContainer(path, create, exactPath);
//			Map map = null;
//			if(child != null){
//				map = (Map)child.getValue();
//				if(map == null && create){
//					map = new HashMap();
//					child.setValue(map);
//				}
//			}
//
//			return map;
//		}

//		public IDiscoveredPathInfo getInfo(IFile file, String instanceId){
//			IPath path = file.getProjectRelativePath();
//			Map map = getMap(path, false, false);
//			for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
//				Map.Entry entry = (Map.Entry)iter.next();
//				InfoContext context = (InfoContext)entry.getKey();
//				if(context.matches(file))
//					return (IDiscoveredPathInfo)entry.getValue();
//			}
//			return null;
//		}

		public IDiscoveredPathInfo setInfo(InfoContext context, IDiscoveredPathInfo info){
			if(info != null)
				return fInfoMap.put(context, info);
			return fInfoMap.remove(context);
		}

	}

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
	@Override
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
						//TODO: better handlind of resource remove/rename
						fDiscoveredInfoHolderMap.remove(resource);
						ScannerConfigProfileManager.getInstance().handleProjectRemoved(resource.getProject());
					}
					break;
			}
		}
	}



	@Override
	public IDiscoveredPathInfo getDiscoveredInfo(IProject project) throws CoreException {
		return getDiscoveredInfo(project, new InfoContext(project));
	}

	@Override
	public IDiscoveredPathInfo getDiscoveredInfo(IProject project, InfoContext context) throws CoreException{
		return getDiscoveredInfo(project, context, true);
	}

	@Override
	public IDiscoveredPathInfo getDiscoveredInfo(IProject project, InfoContext context, boolean defaultToProjectSettings) throws CoreException{
		DiscoveredInfoHolder holder = getHolder(project, true);
		IDiscoveredPathInfo info = holder.getInfo(context);

		if(info == null){
			info = loadPathInfo(project, context, defaultToProjectSettings);
			holder.setInfo(context, info);
		}

		return info;
	}

	private DiscoveredInfoHolder getHolder(IProject project, boolean create){
		DiscoveredInfoHolder holder = fDiscoveredInfoHolderMap.get(project);
		if(holder == null && create){
			holder = new DiscoveredInfoHolder();
			fDiscoveredInfoHolderMap.put(project, holder);
		}
		return holder;
	}

	private IDiscoveredPathInfo loadPathInfo(IProject project, InfoContext context, boolean defaultToProjectSettings) throws CoreException {
        IDiscoveredPathInfo pathInfo = null;

        IScannerConfigBuilderInfo2Set container = ScannerConfigProfileManager.createScannerConfigBuildInfo2Set(project);
        IScannerConfigBuilderInfo2 buildInfo = container.getInfo(context);
        if(buildInfo == null && defaultToProjectSettings)
        	buildInfo = container.getInfo(new InfoContext(project));

        if(buildInfo != null){
	        String profileId = buildInfo.getSelectedProfileId();
	        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
	                getSCProfileInstance(project, context, profileId);
	        IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();

	        if (collector instanceof IScannerInfoCollector2) {
	            IScannerInfoCollector2 collector2 = (IScannerInfoCollector2) collector;
	            pathInfo = collector2.createPathInfoObject();
	        }
        }
        if(pathInfo == null) {
            pathInfo = new DiscoveredPathInfo(project);
        }
		return pathInfo;
	}




//	private DiscoveredInfoHolder getHolder

//	private IDiscoveredPathInfo loadPathInfo(IProject project) throws CoreException {
//        IDiscoveredPathInfo pathInfo = null;
//
//        IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);
//        String profileId = buildInfo.getSelectedProfileId();
//        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
//                getSCProfileInstance(project, profileId);
//        IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
//
//        if (collector instanceof IScannerInfoCollector2) {
//            IScannerInfoCollector2 collector2 = (IScannerInfoCollector2) collector;
//            pathInfo = collector2.createPathInfoObject();
//        }
//        else {
//            pathInfo = new DiscoveredPathInfo(project);
//        }
//		return pathInfo;
//	}

	@Override
	public void removeDiscoveredInfo(IProject project) {
		removeDiscoveredInfo(project, new InfoContext(project));
	}

	@Override
	public void removeDiscoveredInfo(IProject project, InfoContext context) {
		DiscoveredInfoHolder holder = getHolder(project, false);
		if(holder != null){
			IDiscoveredPathInfo info = holder.setInfo(context, null);
			if (info != null) {
				fireUpdate(INFO_REMOVED, info);
			}
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager#updateDiscoveredInfo(org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo, java.util.List)
     */
    @Override
	public void updateDiscoveredInfo(IDiscoveredPathInfo info, List<IResource> changedResources) throws CoreException {
    	updateDiscoveredInfo(new InfoContext(info.getProject()), info, true, changedResources);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager#updateDiscoveredInfo(org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo, java.util.List)
     */
    @Override
	public void updateDiscoveredInfo(InfoContext context, IDiscoveredPathInfo info, boolean updateContainer, List<IResource> changedResources) throws CoreException {
    	DiscoveredInfoHolder holder = getHolder(info.getProject(), true);
    	IDiscoveredPathInfo oldInfo = holder.getInfo(context);
		if (oldInfo != null) {
            IDiscoveredScannerInfoSerializable serializable = info.getSerializable();
			if (serializable != null) {
				holder.setInfo(context, info);
                IProject project = info.getProject();
				DiscoveredScannerInfoStore.getInstance().saveDiscoveredScannerInfoToState(project, context, serializable);
				fireUpdate(INFO_CHANGED, info);

				if(updateContainer){

	                IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);
	                String profileId = buildInfo.getSelectedProfileId();
	                ScannerConfigScope profileScope = ScannerConfigProfileManager.getInstance().
	                        getSCProfileConfiguration(profileId).getProfileScope();
	                changeDiscoveredContainer(project, profileScope, changedResources);
				}
			}
			else {
		        throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
		                MakeMessages.getString("DiscoveredPathManager.Info_Not_Serializable"), null)); //$NON-NLS-1$
			}
		}
	}

    /**
     * Allows one to update the discovered information for a particular scanner discovery profile ID.
     * TODO:  This should be made API in IDiscoveredPathManager, or in an interface derived there from.
     *
     * @param context
     * @param info
     * @param updateContainer
     * @param changedResources
     * @param profileId
     * @throws CoreException
     */
    public void updateDiscoveredInfo(InfoContext context, IDiscoveredPathInfo info, boolean updateContainer, List<IResource> changedResources, String profileId) throws CoreException {
    	DiscoveredInfoHolder holder = getHolder(info.getProject(), true);
    	IDiscoveredPathInfo oldInfo = holder.getInfo(context);
		if (oldInfo != null) {
            IDiscoveredScannerInfoSerializable serializable = info.getSerializable();
			if (serializable != null) {
				holder.setInfo(context, info);
                IProject project = info.getProject();
				DiscoveredScannerInfoStore.getInstance().saveDiscoveredScannerInfoToState(project, context, serializable);
				fireUpdate(INFO_CHANGED, info);

				if(updateContainer){

	                IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);

	                ScannerConfigScope profileScope = ScannerConfigProfileManager.getInstance().
	                        getSCProfileConfiguration(profileId).getProfileScope();
	                changeDiscoveredContainer(project, profileScope, changedResources);
				}
			}
			else {
		        throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
		                MakeMessages.getString("DiscoveredPathManager.Info_Not_Serializable"), null)); //$NON-NLS-1$
			}
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager#changeDiscoveredContainer(org.eclipse.core.resources.IProject, java.lang.String)
     */
    @Override
	public void changeDiscoveredContainer(final IProject project, final ScannerConfigScope profileScope, final List<IResource> changedResources) {
        // order here is of essence
        // 1. clear DiscoveredPathManager's path info cache
    	DiscoveredInfoHolder holder = getHolder(project, false);
    	InfoContext context = new InfoContext(project);
        IDiscoveredPathInfo oldInfo = holder.getInfo(context);

        // 2. switch the containers
        try {
        	IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
        		@Override
				public void run(IProgressMonitor monitor) throws CoreException {
        			ICProject cProject = CoreModel.getDefault().create(project);
        			if (ScannerConfigScope.PROJECT_SCOPE.equals(profileScope)) {
        				CoreModel.setPathEntryContainer(new ICProject[]{cProject},
        						new DiscoveredPathContainer(project), null);
        			}
        			else if (ScannerConfigScope.FILE_SCOPE.equals(profileScope)) {
        				PerFileDiscoveredPathContainer container = new PerFileDiscoveredPathContainer(project);
        				CoreModel.setPathEntryContainer(new ICProject[]{cProject},
        						container, null);
        				if (changedResources != null) {
        					List<PathEntryContainerChanged> changeDelta = new ArrayList<PathEntryContainerChanged>(changedResources.size());
        					for (IResource resource : changedResources) {
        						IPath path = resource.getFullPath();
        						changeDelta.add(new PathEntryContainerChanged(path,
        								PathEntryContainerChanged.INCLUDE_CHANGED |
        								PathEntryContainerChanged.MACRO_CHANGED)); // both include paths and symbols changed
        					}
        					CoreModel.pathEntryContainerUpdates(container,
        							changeDelta.toArray(new PathEntryContainerChanged[changeDelta.size()]),
        							null);
        				}
        			}
        			else {
        				MakeCorePlugin.log(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), 1,
        						MakeMessages.getString("DiscoveredContainer.ScopeErrorMessage"), null)); //$NON-NLS-1$
        			}

        		}
        	};
        	CoreModel.run(runnable, null);
        }
        catch (CoreException e) {
        	MakeCorePlugin.log(e);
        }

        // 3. clear the container's path entry cache
        if (oldInfo != null) {
            fireUpdate(INFO_REMOVED, oldInfo);
        }
    }

	private void fireUpdate(final int type, final IDiscoveredPathInfo info) {
		Object[] list = listeners.toArray();
		for (int i = 0; i < list.length; i++) {
			final IDiscoveredInfoListener listener = (IDiscoveredInfoListener)list[i];
			if (listener != null) {
				SafeRunner.run(new ISafeRunnable() {

					@Override
					public void handleException(Throwable exception) {
						IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
								CCorePlugin.getResourceString("CDescriptorManager.exception.listenerError"), exception); //$NON-NLS-1$
						CCorePlugin.log(status);
					}

					@Override
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

	@Override
	public void addDiscoveredInfoListener(IDiscoveredInfoListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeDiscoveredInfoListener(IDiscoveredInfoListener listener) {
		listeners.remove(listener);
	}
}
