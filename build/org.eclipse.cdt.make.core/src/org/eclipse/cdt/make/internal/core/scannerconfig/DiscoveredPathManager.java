/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.PathEntryContainerChanged;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigScope;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


public class DiscoveredPathManager implements IDiscoveredPathManager, IResourceChangeListener {

	private Map fDiscoveredMap = new HashMap();
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
						fDiscoveredMap.remove(resource);
					}
					break;
			}
		}
	}

	public IDiscoveredPathInfo getDiscoveredInfo(IProject project) throws CoreException {
	    IDiscoveredPathInfo info = (IDiscoveredPathInfo)fDiscoveredMap.get(project);
		if (info == null) {
			info = loadPathInfo(project);
			fDiscoveredMap.put(project, info);
		}
		return info;
	}

	private IDiscoveredPathInfo loadPathInfo(IProject project) throws CoreException {
        IDiscoveredPathInfo pathInfo = null;
        
        IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);
        String profileId = buildInfo.getSelectedProfileId();
        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                getSCProfileInstance(project, profileId);
        IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
        
        if (collector instanceof IScannerInfoCollector2) {
            IScannerInfoCollector2 collector2 = (IScannerInfoCollector2) collector;
            pathInfo = collector2.createPathInfoObject();
        }
        else {
            pathInfo = new DiscoveredPathInfo(project);
        }
		return pathInfo;
	}

	public void removeDiscoveredInfo(IProject project) {
		IDiscoveredPathInfo info = (IDiscoveredPathInfo)fDiscoveredMap.remove(project);
		if (info != null) {
			fireUpdate(INFO_REMOVED, info);
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager#updateDiscoveredInfo(org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo, java.util.List)
     */
    public void updateDiscoveredInfo(IDiscoveredPathInfo info, List changedResources) throws CoreException {
		if (fDiscoveredMap.get(info.getProject()) != null) {
            IDiscoveredScannerInfoSerializable serializable = info.getSerializable();
			if (serializable != null) {
                IProject project = info.getProject();
				DiscoveredScannerInfoStore.getInstance().saveDiscoveredScannerInfoToState(project, serializable);
				fireUpdate(INFO_CHANGED, info);
                
//				ICProject cProject = CoreModel.getDefault().create(info.getProject());
//				if (cProject != null) {
//					CoreModel.setPathEntryContainer(new ICProject[]{cProject},
//							new DiscoveredPathContainer(info.getProject()), null);
//				}
                IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);
                String profileId = buildInfo.getSelectedProfileId();
                ScannerConfigScope profileScope = ScannerConfigProfileManager.getInstance().
                        getSCProfileConfiguration(profileId).getProfileScope();
                changeDiscoveredContainer(project, profileScope, changedResources);
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
    public void changeDiscoveredContainer(IProject project, ScannerConfigScope profileScope, List changedResources) {
        // order here is of essence
        // 1. clear DiscoveredPathManager's path info cache
        IDiscoveredPathInfo oldInfo = (IDiscoveredPathInfo) fDiscoveredMap.remove(project);
        
        // 2. switch the containers
        ICProject cProject = CoreModel.getDefault().create(project);
        try {
            if (ScannerConfigScope.PROJECT_SCOPE.equals(profileScope)) {
                    CoreModel.setPathEntryContainer(new ICProject[]{cProject},
                            new DiscoveredPathContainer(project), null);
            }
            else if (ScannerConfigScope.FILE_SCOPE.equals(profileScope)) {
                PerFileDiscoveredPathContainer container = new PerFileDiscoveredPathContainer(project);
                CoreModel.setPathEntryContainer(new ICProject[]{cProject},
                        container, null);
                if (changedResources != null) {
                    List changeDelta = new ArrayList(changedResources.size());
                    for (Iterator i = changedResources.iterator(); i.hasNext(); ) {
                        IPath path = (IPath) i.next();
                        changeDelta.add(new PathEntryContainerChanged(path, 3)); // both include paths and symbols changed
                    }
                    CoreModel.pathEntryContainerUpdates(container, 
                            (PathEntryContainerChanged[]) changeDelta.toArray(new PathEntryContainerChanged[changeDelta.size()]), 
                            null);
                }
            }
            else {
                MakeCorePlugin.log(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), 1,
                    MakeMessages.getString("DiscoveredContainer.ScopeErrorMessage"), null)); //$NON-NLS-1$
            }
        }
        catch (CModelException e) {
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

}