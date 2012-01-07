/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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

	@Override
	public boolean hasChildren(Object obj) {
		return getChildren(obj).length > 0;
	}

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

	@Override
	public void dispose() {
		if (viewer != null) {
			MakeCorePlugin.getDefault().getTargetManager().removeListener(this);
		}
	}

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

	/**
	 * Refresh the whole view.
	 */
	private void refreshView() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}

	/**
	 * Refresh the project tree or the project subtree (in case of drill-down adapter) in the view.
	 */
	private void refreshProjectTree(final IProject project) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed())
					return;

				int itemCount = 0;
				if (viewer instanceof TreeViewer) {
					((TreeViewer) viewer).getTree().getItemCount();
				} else if (viewer instanceof TableViewer) {
					((TableViewer) viewer).getTable().getItemCount();
				}
				if (itemCount <= 0) {
					return;
				}

				Object firstItem = null;
				if (viewer instanceof TreeViewer) {
					firstItem = ((TreeViewer) viewer).getTree().getItem(0).getData();
				} else if (viewer instanceof TableViewer) {
					firstItem = ((TableViewer) viewer).getTable().getItem(0).getData();
				}

				IContainer parentContainer = null;

				boolean isDrilledDown = !(firstItem instanceof IProject);
				if (!isDrilledDown) {
					// view shows projects
					viewer.refresh(project);
				} else {
					// drill-down adapter in the game
					if (firstItem instanceof IResource) {
						parentContainer = ((IResource) firstItem).getParent();
					} else if (firstItem instanceof TargetSourceContainer) {
						parentContainer = ((TargetSourceContainer) firstItem).getContainer().getParent();
					} else if (firstItem instanceof IMakeTarget) {
						parentContainer = ((IMakeTarget) firstItem).getContainer();
					}

					if (parentContainer != null && project.equals(parentContainer.getProject())) {
						viewer.refresh();
					}
				}
			}
		});
	}

	@Override
	public void targetChanged(MakeTargetEvent event) {
		// Additions/removal of projects. Only notifications for projects having applicable builder come here.
		int type = event.getType();
		if (type == MakeTargetEvent.PROJECT_ADDED || type == MakeTargetEvent.PROJECT_REMOVED) {
			refreshView();
			return;
		}

		IMakeTarget[] targets = event.getTargets();
		if (targets == null) {
			return;
		}

		Set<IProject> affectedProjects = new HashSet<IProject>();
		for (IMakeTarget target : event.getTargets()) {
			IContainer container = target.getContainer();
			affectedProjects.add(container.getProject());
		}

		// If the view is being filtered, adding/removing targets can
		// result in showing or hiding containers or the project itself
		for (IProject project : affectedProjects) {
			refreshProjectTree(project);
		}
	}

	private void collectAffectedProjects(IResourceDelta delta, Set<IProject> affectedProjects) {
		if (affectedProjects.contains(delta.getResource().getProject())) {
			return;
		}

		for (IResourceDelta d : delta.getAffectedChildren(IResourceDelta.ADDED | IResourceDelta.REMOVED)) {
			// handle folders only, additions/removal of projects are dealt with in #targetChanged(MakeTargetEvent)
			IResource rc = d.getResource();
			if (rc.getType() == IResource.FOLDER) {
				IProject project = rc.getProject();
				if (MakeCorePlugin.getDefault().getTargetManager().hasTargetBuilder(project)) {
					affectedProjects.add(project);
					return;
				}
			}
		}

		for (IResourceDelta d : delta.getAffectedChildren(IResourceDelta.CHANGED)) {
			collectAffectedProjects(d, affectedProjects);
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta == null) {
			return;
		}

		Set<IProject> affectedProjects = new HashSet<IProject>();
		collectAffectedProjects(delta, affectedProjects);

		// If the view is being filtered or source roots shown,
		// adding/removing resources can structurally affect the tree
		// starting with the project
		for (IProject project : affectedProjects) {
			refreshProjectTree(project);
		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 7.1
	 */
	@Override
	public void handleEvent(CProjectDescriptionEvent event) {
		ICDescriptionDelta delta = event.getDefaultSettingCfgDelta();
		if (delta==null)
			return;

		int flags = delta.getChangeFlags();
		if ( ((flags & ICDescriptionDelta.SOURCE_ADDED) != 0) ||
			 ((flags & ICDescriptionDelta.SOURCE_REMOVED) != 0) ) {

			IProject project = null;
			ICSettingObject setting = delta.getOldSetting();
			if (setting == null) {
				setting = delta.getNewSetting();
			}

			if (setting instanceof ICConfigurationDescription) {
				project = ((ICConfigurationDescription) setting).getProjectDescription().getProject();
				if (project != null) {
					// refresh source roots under the project
					refreshProjectTree(project);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 7.1
	 */
	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(CCorePreferenceConstants.SHOW_SOURCE_ROOTS_AT_TOP_LEVEL_OF_PROJECT)) {
			refreshView();
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
