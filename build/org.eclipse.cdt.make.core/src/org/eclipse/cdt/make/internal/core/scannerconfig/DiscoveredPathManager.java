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
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
                    ScannerConfigUtil.updateScannerConfigStore(event.getDelta());
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
		DiscoveredPathInfo info = (DiscoveredPathInfo)fDiscoveredMap.get(project);
		if (info == null) {
			info = loadPathInfo(project);
			fDiscoveredMap.put(project, info);
		}
		return info;
	}

	private DiscoveredPathInfo loadPathInfo(IProject project) throws CoreException {
		DiscoveredPathInfo info = new DiscoveredPathInfo(project);
		DiscoveredScannerInfoStore.getInstance().loadDiscoveredScannerInfoFromState(project, info);
		return info;
	}

	public void removeDiscoveredInfo(IProject project) {
		ScannerConfigUtil.getDiscoveredScannerConfigStore(project, true);
		DiscoveredPathInfo info = (DiscoveredPathInfo)fDiscoveredMap.remove(project);
		if (info != null) {
			fireUpdate(INFO_REMOVED, info);
		}
	}

	public void updateDiscoveredInfo(IDiscoveredPathInfo info) throws CoreException {
		if (fDiscoveredMap.get(info.getProject()) != null) {
			if (info instanceof IDiscoveredScannerInfoSerializable) {
				IDiscoveredScannerInfoSerializable serializable = (IDiscoveredScannerInfoSerializable) info;
				DiscoveredScannerInfoStore.getInstance().saveDiscoveredScannerInfoToState(info.getProject(), serializable);
				fireUpdate(INFO_CHANGED, info);
				ICProject cProject = CoreModel.getDefault().create(info.getProject());
				if (cProject != null) {
					CoreModel.setPathEntryContainer(new ICProject[]{cProject},
							new DiscoveredPathContainer(info.getProject()), null);
				}
			}
			else {
		        throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
		                MakeMessages.getString("DiscoveredPathManager.Info_Not_Serializable"), null)); //$NON-NLS-1$
			}
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
								listener.infoRemoved(info.getProject());
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