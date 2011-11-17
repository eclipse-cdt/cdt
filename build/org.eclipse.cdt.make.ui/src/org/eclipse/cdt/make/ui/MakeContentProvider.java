/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andrew Gvozdev - some improvements such as adding source folders bug 339015
 *******************************************************************************/
package org.eclipse.cdt.make.ui;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Content provider for Make Targets view and for Make Targets dialog from
 * "Make Targets"->"Build..." in project context menu.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MakeContentProvider implements ITreeContentProvider, IMakeTargetListener,
		IResourceChangeListener, ICProjectDescriptionListener, IPreferenceChangeListener {

	/** presentation of the content, i.e. for MakeView tree of for BuildTargetDialog table */
	protected boolean bFlatten;

	protected StructuredViewer viewer;

	/**
	 * Default constructor.
	 */
	public MakeContentProvider() {
		this(false);
	}

	/**
	 * Constructor.
	 *
	 * @param flat - {@code true} for "flat" representation for a table
	 *    or {@code false} to represent as a tree.
	 */
	public MakeContentProvider(boolean flat) {
		bFlatten = flat;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object obj) {
		if (obj instanceof IWorkspaceRoot) {
			try {
				return MakeCorePlugin.getDefault().getTargetManager().getTargetBuilderProjects();
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
			}
		} else if (obj instanceof IContainer) {
			IContainer container = (IContainer)obj;
			ArrayList<Object> children = new ArrayList<Object>();

			boolean isAddingSourceRoots = !bFlatten && (container instanceof IProject) && CCorePlugin.showSourceRootsAtTopOfProject();

			// add source roots if necessary
			if (isAddingSourceRoots) {
				IProject project = (IProject) container;
				ICSourceEntry[] srcEntries = getSourceEntries(project);
				for (ICSourceEntry srcEntry : srcEntries) {
					if (!srcEntry.getFullPath().equals(project.getFullPath())) {
						children.add(new TargetSourceContainer(srcEntry));
					}
				}
			}

			// add regular folders
			try {
				IResource[] resources = container.members();
				for (IResource rc : resources) {
					if (rc instanceof IContainer) {
						if (!(isAddingSourceRoots && isSourceEntry(rc))) {
							children.add(rc);
						}
					}
				}
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
			}

			// finally add targets
			try {
				IMakeTarget[] targets = MakeCorePlugin.getDefault().getTargetManager().getTargets(container);
				children.addAll(Arrays.asList(targets));
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
			}
			return children.toArray();

		} else if (obj instanceof TargetSourceContainer) {
			ArrayList<Object> children = new ArrayList<Object>();
			try {
				IContainer container = ((TargetSourceContainer) obj).getContainer();
				IResource[] resources = container.members();
				for (IResource rc : resources) {
					if (rc instanceof IContainer) {
						children.add(rc);
					}
				}
				children.addAll(Arrays.asList(MakeCorePlugin.getDefault().getTargetManager().getTargets(container)));
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
			}
			return children.toArray();
		}
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object obj) {
		if (obj instanceof IMakeTarget) {
			// this is ambiguous as make target can sit in 2 places, in its container
			// or source folder represented by TargetSourceContainer
			return ((IMakeTarget)obj).getContainer();
		} else if (obj instanceof IContainer) {
			return ((IContainer)obj).getParent();
		} else if (obj instanceof TargetSourceContainer) {
			IContainer container = ((TargetSourceContainer)obj).getContainer();
			// TargetSourceContainer sits at project root
			return container.getProject();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object obj) {
		return getChildren(obj).length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object obj) {
		if (bFlatten) {
			List<Object> list = new ArrayList<Object>();
			Object[] children = getChildren(obj);
			for (int i = 0; i < children.length; i++) {
				list.add(children[i]);
				list.addAll(Arrays.asList(getElements(children[i])));
			}
			return list.toArray();
		}
		return getChildren(obj);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		if (viewer != null) {
			MakeCorePlugin.getDefault().getTargetManager().removeListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.viewer == null) {
			MakeCorePlugin.getDefault().getTargetManager().addListener(this);
		}
		this.viewer = (StructuredViewer) viewer;
		IWorkspace oldWorkspace = null;
		IWorkspace newWorkspace = null;
		if (oldInput instanceof IWorkspace) {
			oldWorkspace = (IWorkspace) oldInput;
		} else if (oldInput instanceof IContainer) {
			oldWorkspace = ((IContainer) oldInput).getWorkspace();
		} else if (oldInput instanceof TargetSourceContainer) {
			oldWorkspace = ((TargetSourceContainer) oldInput).getContainer().getWorkspace();
		}
		if (newInput instanceof IWorkspace) {
			newWorkspace = (IWorkspace) newInput;
		} else if (newInput instanceof IContainer) {
			newWorkspace = ((IContainer) newInput).getWorkspace();
		} else if (newInput instanceof TargetSourceContainer) {
			newWorkspace = ((TargetSourceContainer) newInput).getContainer().getWorkspace();
		}
		if (oldWorkspace != newWorkspace) {
			ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
			if (oldWorkspace != null) {
				InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).removePreferenceChangeListener(this);
				mngr.removeCProjectDescriptionListener(this);
				oldWorkspace.removeResourceChangeListener(this);
			}
			if (newWorkspace != null) {
				newWorkspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
				mngr.addCProjectDescriptionListener(this, CProjectDescriptionEvent.APPLIED);
				InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).addPreferenceChangeListener(this);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.IMakeTargetListener#targetChanged(org.eclipse.cdt.make.core.MakeTargetEvent)
	 */
	@Override
	public void targetChanged(final MakeTargetEvent event) {
		final Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			switch (event.getType()) {
				case MakeTargetEvent.PROJECT_ADDED :
				case MakeTargetEvent.PROJECT_REMOVED :
					ctrl.getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (!ctrl.isDisposed()) {
								viewer.refresh();
							}
						}
					});
					break;
				case MakeTargetEvent.TARGET_ADD :
				case MakeTargetEvent.TARGET_CHANGED :
				case MakeTargetEvent.TARGET_REMOVED :
					ctrl.getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (!ctrl.isDisposed()) {
								if (bFlatten) {
									viewer.refresh();
								} else {
									//We can't just call refresh on the container target that
									//has been created since it may be that the container has
									//been filtered out and the filters in the viewer don't know
									//any better how to call out to the filter selection again.
									//Instead we walk to the root project container and refresh it.
									Set<IContainer> containers = new HashSet<IContainer>();
									IMakeTarget[] targets = event.getTargets();
									for (IMakeTarget target : targets) {
										IContainer container = target.getContainer();
										while(!(container instanceof IProject) && container.getParent()!=null) {
											container = container.getParent();
										}
										containers.add(container);
									}
									for (IContainer container : containers) {
										viewer.refresh(container);
									}
								}
							}
						}
					});
					break;
			}
		}
	}

	private void processDelta(IResourceDelta delta) {
		// Bail out if the widget was disposed.
		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed() || delta == null) {
			return;
		}

		IResourceDelta[] affectedChildren = delta.getAffectedChildren(IResourceDelta.CHANGED);

		// Not interested in Content changes.
		for (int i = 0; i < affectedChildren.length; i++) {
			if ((affectedChildren[i].getFlags() & IResourceDelta.TYPE) != 0) {
				return;
			}
		}

		// Handle changed children recursively.
		for (int i = 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}

		// Get the affected resource
		final IResource resource = delta.getResource();

		// Handle removed children. Issue one update for all removals.
		affectedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED);
		if (affectedChildren.length > 0) {
			final ArrayList<IResource> affected = new ArrayList<IResource>(affectedChildren.length);
			for (int i = 0; i < affectedChildren.length; i++) {
				if (affectedChildren[i].getResource().getType() == IResource.FOLDER) {
					affected.add(affectedChildren[i].getResource());
				}
			}
			if (!affected.isEmpty()) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed())
							return;
						if (viewer instanceof AbstractTreeViewer) {
							((AbstractTreeViewer) viewer).remove(affected.toArray());
						} else {
							viewer.refresh(resource);
						}
					}
				});
			}
		}

		// Handle added children. Issue one update for all insertions.
		affectedChildren = delta.getAffectedChildren(IResourceDelta.ADDED);
		if (affectedChildren.length > 0) {
			final ArrayList<IResource> affected = new ArrayList<IResource>(affectedChildren.length);
			for (int i = 0; i < affectedChildren.length; i++) {
				if (affectedChildren[i].getResource().getType() == IResource.FOLDER) {
					affected.add(affectedChildren[i].getResource());
				}
			}
			if (!affected.isEmpty()) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed())
							return;
						if (viewer instanceof AbstractTreeViewer) {
							((AbstractTreeViewer) viewer).add(resource, affected.toArray());
						} else {
							viewer.refresh(resource);
						}
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		final IResourceDelta delta = event.getDelta();
		Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed())
			processDelta(delta);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 7.1
	 */
	@Override
	public void handleEvent(final CProjectDescriptionEvent event) {
		Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				ICDescriptionDelta delta = event.getDefaultSettingCfgDelta();
				if (delta==null)
					return;

				int flags = delta.getChangeFlags();
				if ( ((flags & ICDescriptionDelta.SOURCE_ADDED) != 0) ||
					 ((flags & ICDescriptionDelta.SOURCE_REMOVED) != 0) ) {

					IProject project = null;
					ICSettingObject setting = delta.getOldSetting();
					if (setting==null)
						setting = delta.getNewSetting();

					if (setting instanceof ICConfigurationDescription)
						project = ((ICConfigurationDescription) setting).getProjectDescription().getProject();

					if (project!=null)
						viewer.refresh(project);
					else
						viewer.refresh();
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 7.1
	 */
	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(CCorePreferenceConstants.SHOW_SOURCE_ROOTS_AT_TOP_LEVEL_OF_PROJECT)) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});
		}
	}

	/**
	 * Get source entries for default setting configuration (i.e. configuration shown in UI).
	 */
	private static ICSourceEntry[] getSourceEntries(IProject project) {
		ICProjectDescriptionManager mgr = CCorePlugin.getDefault().getProjectDescriptionManager();
		ICProjectDescription prjDescription = mgr.getProjectDescription(project, false);
		if (prjDescription!=null) {
			ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
			if (cfgDescription!=null) {
				ICSourceEntry[] srcEntries = cfgDescription.getResolvedSourceEntries();
				return srcEntries;
			}
		}

		return new ICSourceEntry[0];
	}

	/**
	 * Check if the resource is in the list of source entries.

	 * @param rc - resource to check.
	 * @return {@code true} if the resource is a source folder, {@code false} otherwise.
	 *
	 * @since 7.1
	 */
	public static boolean isSourceEntry(IResource rc) {
		IProject project = rc.getProject();
		ICSourceEntry[] srcEntries = getSourceEntries(project);
		for (ICSourceEntry srcEntry : srcEntries) {
			if (srcEntry.getFullPath().equals(rc.getFullPath()))
				return true;
		}
		return false;
	}

}
