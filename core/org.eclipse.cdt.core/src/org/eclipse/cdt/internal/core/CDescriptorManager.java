/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorListener;
import org.eclipse.cdt.core.ICDescriptorManager;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class CDescriptorManager implements ICDescriptorManager, IResourceChangeListener {

	Map fOperationMap = new HashMap(1);
	Map fDescriptorMap = new HashMap();
	Map fOwnerConfigMap = null;
	List listeners = Collections.synchronizedList(new Vector());

	private static final COwnerConfiguration NULLCOwner = new COwnerConfiguration("", //$NON-NLS-1$
			CCorePlugin.getResourceString("CDescriptorManager.internal_owner")); //$NON-NLS-1$

	class CDescriptorUpdater extends Job {

		CDescriptor fDescriptor;

		public CDescriptorUpdater(CDescriptor descriptor) {
			super(CCorePlugin.getResourceString("CDescriptorManager.async_updater")); //$NON-NLS-1$
			fDescriptor = descriptor;
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			setRule(descriptor.getProject());
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				fDescriptor.save();
			} catch (CoreException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		}

	}

	IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public void shutdown() {
		getWorkspace().removeResourceChangeListener(this);
	}

	public void startup() throws CoreException {
		getWorkspace().getRoot().accept(new IResourceVisitor() {

			public boolean visit(IResource resource) {
				if (resource.getType() == IResource.PROJECT) {
					IProject project = (IProject)resource;
					try { // seed in memory descriptor map
						if (project.isAccessible() && project.findMember(CDescriptor.DESCRIPTION_FILE_NAME) != null) {
							getDescriptor(project);
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
					return false;
				}
				return resource.getType() == IResource.ROOT;
			}
		});
		getWorkspace().addResourceChangeListener(this,
				IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE);
	}

	/**
	 * Watch for changes/deletions of the .cdtproject file.
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			IResource resource = event.getResource();

			switch (event.getType()) {
				case IResourceChangeEvent.PRE_DELETE :
				case IResourceChangeEvent.PRE_CLOSE :
					if (resource.getType() == IResource.PROJECT) {
						CDescriptor descriptor = (CDescriptor)fDescriptorMap.remove(resource);
						if (descriptor != null) {
							fireEvent(new CDescriptorEvent(descriptor, CDescriptorEvent.CDTPROJECT_REMOVED, 0));
						}
					}
					break;
				case IResourceChangeEvent.PRE_BUILD :
					IResourceDelta resDelta = event.getDelta();
					if (resDelta == null) {
						break;
					}
					try {
						resDelta.accept(new IResourceDeltaVisitor() {

							public boolean visit(IResourceDelta delta) throws CoreException {
								IResource dResource = delta.getResource();
								if (dResource.getType() == IResource.PROJECT) {
									if (0 != (delta.getFlags() & IResourceDelta.OPEN)) {
										IProject project = (IProject)dResource;
										if (project.isAccessible() && project.findMember(CDescriptor.DESCRIPTION_FILE_NAME) != null
												&& fDescriptorMap.get(project) == null) {
											getDescriptor(project); // file on disk but not in memory...read
										} else {
											CDescriptor descriptor = (CDescriptor)fDescriptorMap.remove(project);
											if (descriptor != null) {
												fireEvent(new CDescriptorEvent(descriptor, CDescriptorEvent.CDTPROJECT_REMOVED, 0));
											}
										}
										return false;
									}
									return true;
								} else if (dResource.getType() == IResource.FILE) {
									if (dResource.getName().equals(CDescriptor.DESCRIPTION_FILE_NAME)) {
										CDescriptor descriptor = (CDescriptor)fDescriptorMap.get(dResource.getProject());
										if (descriptor != null) {
											if ( (delta.getKind() & IResourceDelta.REMOVED) != 0) {
												// the file got deleted lets try
												// and restore for memory.
												descriptor.updateOnDisk();
											} else if ( (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
												// content change lets try to
												// read and update
												descriptor.updateFromDisk();
											}
										}
										return false;
									}
									return true;
								}
								return dResource.getType() == IResource.ROOT;
							}
						});
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
					break;
			}
		}
	}

	private void initializeOwnerConfiguration() {
        IExtensionPoint extpoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "CProject"); //$NON-NLS-1$
		IExtension extension[] = extpoint.getExtensions();
		fOwnerConfigMap = new HashMap(extension.length);
		for (int i = 0; i < extension.length; i++) {
			IConfigurationElement element[] = extension[i].getConfigurationElements();
			for (int j = 0; j < element.length; j++) {
				if (element[j].getName().equalsIgnoreCase("cproject")) { //$NON-NLS-1$
					fOwnerConfigMap.put(extension[i].getUniqueIdentifier(), new COwnerConfiguration(element[j]));
					break;
				}
			}
		}
	}

	COwnerConfiguration getOwnerConfiguration(String id) {
		if (id.equals(NULLCOwner.getOwnerID())) { //$NON-NLS-1$
			return NULLCOwner;
		}
		if (fOwnerConfigMap == null) {
			initializeOwnerConfiguration();
		}
		COwnerConfiguration config = (COwnerConfiguration)fOwnerConfigMap.get(id);
		if (config == null) { // no install owner, lets create place holder config for it.
			config = new COwnerConfiguration(id, CCorePlugin.getResourceString("CDescriptorManager.owner_not_Installed")); //$NON-NLS-1$
			fOwnerConfigMap.put(id, config);
		}
		return config;
	}

	COwnerConfiguration getOwnerConfiguration(IProject project) throws CoreException {
		if (fOwnerConfigMap == null) {
			initializeOwnerConfiguration();
		}
		IProjectDescription description = project.getDescription();
		String natureIDs[] = description.getNatureIds();
		Iterator configs = fOwnerConfigMap.entrySet().iterator();
		while (configs.hasNext()) {
			Entry entry = (Entry)configs.next();
			COwnerConfiguration config = (COwnerConfiguration)entry.getValue();
			if (config.getNature() != null) {
				if (Arrays.asList(natureIDs).lastIndexOf(config.getNature()) != -1) {
					return config;
				}
			}
		}
		return NULLCOwner;
	}

	synchronized public ICDescriptor getDescriptor(IProject project) throws CoreException {
		return getDescriptor(project, true);
	}
	
	synchronized public ICDescriptor getDescriptor(IProject project, boolean create) throws CoreException {
		CDescriptor descriptor = (CDescriptor)fDescriptorMap.get(project);
		if (descriptor == null) {
			if (create) {
				descriptor = new CDescriptor(this, project);
				fDescriptorMap.put(project, descriptor);
			} else {
				IPath projectLocation = project.getDescription().getLocation();

				if (projectLocation == null) {
					projectLocation = Platform.getLocation().append(project.getFullPath());
				}
				IPath descriptionPath = projectLocation.append(CDescriptor.DESCRIPTION_FILE_NAME);

				if (descriptionPath.toFile().exists()) {
					descriptor = new CDescriptor(this, project);
					fDescriptorMap.put(project, descriptor);
				}
			}
		}
		return descriptor;
	}

	public void configure(IProject project, String id) throws CoreException {
		CDescriptor descriptor;
		if (id.equals(NULLCOwner.getOwnerID())) { //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
					CCorePlugin.getResourceString("CDescriptorManager.exception.invalid_ownerID"), //$NON-NLS-1$
					(Throwable)null);
			throw new CoreException(status);
		}
		synchronized (this) {
			descriptor = (CDescriptor)fDescriptorMap.get(project);
			if (descriptor != null) {
				if (descriptor.getProjectOwner().getID().equals(NULLCOwner.getOwnerID())) { //$NON-NLS-1$
					// non owned descriptors are simply configure to the new owner no questions ask!
					descriptor = new CDescriptor(this, project, new COwner(getOwnerConfiguration(id)));
				} else if (!descriptor.getProjectOwner().getID().equals(id)) {
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_CDTPROJECT_EXISTS,
							CCorePlugin.getResourceString("CDescriptorManager.exception.alreadyConfigured"), //$NON-NLS-1$
							(Throwable)null);
					throw new CoreException(status);
				} else {
					return; // already configured with same owner.
				}
			} else {
				try {
					descriptor = new CDescriptor(this, project, id);
				} catch (CoreException e) { // if .cdtproject already exists we'll use that
					IStatus status = e.getStatus();
					if (status.getCode() == CCorePlugin.STATUS_CDTPROJECT_EXISTS) {
						descriptor = new CDescriptor(this, project);
					} else
						throw e;
				}
			}
			fDescriptorMap.put(project, descriptor);
		}
		fireEvent(new CDescriptorEvent(descriptor, CDescriptorEvent.CDTPROJECT_ADDED, 0));
	}

	public void convert(IProject project, String id) throws CoreException {
		CDescriptor descriptor;
		synchronized (this) {
			descriptor = new CDescriptor(this, project, new COwner(getOwnerConfiguration(id)));
			fDescriptorMap.put(project, descriptor);
		}
		fireEvent(new CDescriptorEvent(descriptor, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.OWNER_CHANGED));
	}

	public void addDescriptorListener(ICDescriptorListener listener) {
		listeners.add(listener);
	}

	public void removeDescriptorListener(ICDescriptorListener listener) {
		listeners.remove(listener);
	}

	protected void fireEvent(final CDescriptorEvent event) {
		if (fOperationMap.containsKey(event.getDescriptor())) {
			// lets just hold on to the important event in order of;
			// ADD/REMOVE should not receive the remove but....
			// OWNER_CHANGED
			// EXT_CHANGED
			// other
			if (event.getType() == CDescriptorEvent.CDTPROJECT_ADDED) {
				fOperationMap.put(event.getDescriptor(), event);
			} else if (event.getType() == CDescriptorEvent.CDTPROJECT_REMOVED) {
				fOperationMap.put(event.getDescriptor(), event);
			} else {
				CDescriptorEvent ev = (CDescriptorEvent)fOperationMap.get(event.getDescriptor());
				if (ev == null) {
					fOperationMap.put(event.getDescriptor(), event);
				} else if ( (ev.getFlags() & event.getFlags()) != event.getFlags()) {
					fOperationMap.put(event.getDescriptor(), new CDescriptorEvent(event.getDescriptor(), event.getType(),
							ev.getFlags() | event.getFlags()));
				}
			}
			return;
		}
		final ICDescriptorListener[] listener;
		synchronized (listeners) {
			listener = (ICDescriptorListener[])listeners.toArray(new ICDescriptorListener[listeners.size()]);
		}
		for (int i = 0; i < listener.length; i++) {
			final int index = i;
			Platform.run(new ISafeRunnable() {

				public void handleException(Throwable exception) {
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
							CCorePlugin.getResourceString("CDescriptorManager.exception.listenerError"), exception); //$NON-NLS-1$
					CCorePlugin.log(status);
				}

				public void run() throws Exception {
					listener[index].descriptorChanged(event);
				}
			});
		}
	}

	public void runDescriptorOperation(IProject project, ICDescriptorOperation op, IProgressMonitor monitor) throws CoreException {
		ICDescriptor descriptor = getDescriptor(project, false);
		if (descriptor == null) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, "Project does not have descriptor", null)); //$NON-NLS-1$
		}
		CDescriptorEvent event = null;
		synchronized (descriptor) {
			beginOperation(descriptor);
			try {
				op.execute(descriptor, monitor);
			} finally {
				event = endOperation(descriptor);
			}
		}
		if (event != null) {
			fireEvent(event);
		}
	}

	private void beginOperation(ICDescriptor descriptor) {
		fOperationMap.put(descriptor, null);
	}

	private CDescriptorEvent endOperation(ICDescriptor descriptor) {
		return (CDescriptorEvent)fOperationMap.remove(descriptor);
	}

	/*
	 * Perform a update of the ondisk .cdtproject file. This is nessecary to avoid deadlocking when the descriptor has change from a
	 * call to ICDescriptor.get(project, true) which may update the descriptor via the owner update method, while the workspace is
	 * locked (ie during a resourceChange event).
	 */
	protected void updateDescriptor(CDescriptor descriptor) {
		new CDescriptorUpdater(descriptor).schedule();
	}
}
