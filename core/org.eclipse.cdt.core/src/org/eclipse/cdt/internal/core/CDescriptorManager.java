/*******************************************************************************
 * Copyright (c) 2002, 2003, 2004 QNX Software Systems Ltd. and others. All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorListener;
import org.eclipse.cdt.core.ICDescriptorManager;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class CDescriptorManager implements ICDescriptorManager, IResourceChangeListener {

	HashMap fOperationMap = new HashMap();
	HashMap fDescriptorMap = new HashMap();
	List listeners = new Vector();

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
				getWorkspace().run(new IWorkspaceRunnable() {

					public void run(IProgressMonitor mon) throws CoreException {
						fDescriptor.save();
					}
				}, getRule(), IWorkspace.AVOID_UPDATE, monitor);

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
					IProject project = (IProject) resource;
					try {
						if (project.isAccessible() && project.hasNature(CProjectNature.C_NATURE_ID)) {
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
				IResourceChangeEvent.PRE_AUTO_BUILD | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE);
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
					try {
						if (resource.getType() == IResource.PROJECT && ((IProject) resource).hasNature(CProjectNature.C_NATURE_ID)) {
							CDescriptor descriptor = (CDescriptor) fDescriptorMap.remove(resource);
							if (descriptor != null) {
								fireEvent(new CDescriptorEvent(descriptor, CDescriptorEvent.CDTPROJECT_REMOVED, 0));
							}
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
					break;
				case IResourceChangeEvent.PRE_AUTO_BUILD :
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
										IProject project = (IProject) dResource;
										if (project.isAccessible() && project.hasNature(CProjectNature.C_NATURE_ID)) {
											getDescriptor(project);
										} else {
											CDescriptor descriptor = (CDescriptor) fDescriptorMap.remove(project);
											if (descriptor != null) {
												fireEvent(new CDescriptorEvent(descriptor, CDescriptorEvent.CDTPROJECT_REMOVED, 0));
											}
										}
										return false;
									}
									return true;
								} else if (dResource.getType() == IResource.FILE) {
									if (dResource.getName().equals(CDescriptor.DESCRIPTION_FILE_NAME)) {
										CDescriptor descriptor = (CDescriptor) fDescriptorMap.get(dResource.getProject());
										if (descriptor != null) {
											if ((delta.getKind() & IResourceDelta.REMOVED) != 0) {
												// the file got deleted lets
												// try
												// and restore for memory.
												descriptor.updateOnDisk();
											} else if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
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

	public synchronized ICDescriptor getDescriptor(IProject project) throws CoreException {
		CDescriptor descriptor = (CDescriptor) fDescriptorMap.get(project);
		if (descriptor == null) {
			descriptor = new CDescriptor(this, project);
			fDescriptorMap.put(project, descriptor);
		}
		return descriptor;
	}

	public void configure(IProject project, String id) throws CoreException {
		CDescriptor descriptor;
		synchronized (this) {
			descriptor = (CDescriptor) fDescriptorMap.get(project);
			if (descriptor != null) {
				if (!descriptor.getProjectOwner().getID().equals(id)) {
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_CDTPROJECT_EXISTS,
							CCorePlugin.getResourceString("CDescriptorManager.exception.alreadyConfigured"), //$NON-NLS-1$
							(Throwable) null);
					throw new CoreException(status);
				}
				return;
			}
			try {
				descriptor = new CDescriptor(this, project, id);
			} catch (CoreException e) { // if .cdtproject already exists will
				// use that
				IStatus status = e.getStatus();
				if (status.getCode() == CCorePlugin.STATUS_CDTPROJECT_EXISTS) {
					descriptor = new CDescriptor(this, project);
				} else
					throw e;
			}
			fDescriptorMap.put(project, descriptor);
		}
		fireEvent(new CDescriptorEvent(descriptor, CDescriptorEvent.CDTPROJECT_ADDED, 0));
	}

	public void convert(IProject project, String id) throws CoreException {
		CDescriptor descriptor;
		synchronized (this) {
			descriptor = new CDescriptor(this, project, new COwner(id));
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
			// ADD/REMOVE shouldn' every receive the remove but....
			// OWNER_CHANGED
			// EXT_CHANGED
			// other
			if (event.getType() != CDescriptorEvent.CDTPROJECT_ADDED) {
				fOperationMap.put(event.getDescriptor(), event);
			} else if (event.getType() == CDescriptorEvent.CDTPROJECT_REMOVED) {
				fOperationMap.put(event.getDescriptor(), event);
			} else {
				CDescriptorEvent ev = (CDescriptorEvent) fOperationMap.get(event.getDescriptor());
				if (ev.getType() == CDescriptorEvent.CDTPROJECT_CHANGED) {
					if (ev.getFlags() == 0) {
						fOperationMap.put(event.getDescriptor(), event);
					} else if (ev.getFlags() != CDescriptorEvent.OWNER_CHANGED) {
						fOperationMap.put(event.getDescriptor(), event);
					}
				}
			}
			return;
		}
		final Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			Platform.run(new ISafeRunnable() {

				public void handleException(Throwable exception) {
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
							CCorePlugin.getResourceString("CDescriptorManager.exception.listenerError"), exception); //$NON-NLS-1$
					CCorePlugin.log(status);
				}

				public void run() throws Exception {
					((ICDescriptorListener) iterator.next()).descriptorChanged(event);
				}
			});
		}
	}

	public void runDescriptorOperation(IProject project, ICDescriptorOperation op, IProgressMonitor monitor) throws CoreException {
		ICDescriptor descriptor = getDescriptor(project);
		synchronized (descriptor) {
			beginOperation(descriptor);
			try {
				op.execute(descriptor, monitor);
			} finally {
				endOperation(descriptor);
			}
		}
	}

	private void beginOperation(ICDescriptor descriptor) {
		fOperationMap.put(descriptor, null);
	}

	private void endOperation(ICDescriptor descriptor) {
		CDescriptorEvent event = (CDescriptorEvent) fOperationMap.remove(descriptor);
		if (event != null) {
			fireEvent(event);
		}
	}

	/*
	 * Perform a update of the ondisk .cdtproject file. This is nessecary to
	 * avoid deadlocking when the descriptor has change from a call to
	 * ICDescriptor.get(project, true) which may update the descriptor via the
	 * owner update method, while the workspace is locked (ie during a
	 * resourceChange event).
	 */
	protected void updateDescriptor(CDescriptor descriptor) {
		new CDescriptorUpdater(descriptor).schedule();
	}
}