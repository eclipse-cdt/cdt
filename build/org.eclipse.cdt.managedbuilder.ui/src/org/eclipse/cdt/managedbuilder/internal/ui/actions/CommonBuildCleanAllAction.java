/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     LSI Corporation	 - added symmetric project clean action
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * Abstract action which builds or cleans the build configurations of the current project.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 7.0
 */
public abstract class CommonBuildCleanAllAction extends ActionDelegate implements
	IWorkbenchWindowPulldownDelegate2, IObjectActionDelegate, IMenuCreator {

	protected ArrayList<IProject> projects = null;
	private ActionContributionItem it_all = null;
	private ActionContributionItem it_sel = null;

	/*
	 * Get message strings
	 */
	protected abstract String getTIP_ALL();
	protected abstract String getLBL_ALL();
	protected abstract String getJOB_MSG();
	protected abstract String getERR_MSG();
	protected abstract String getLBL_SEL();
	protected abstract String getTIP_SEL();
	protected abstract String getDLG_TEXT();
	protected abstract String getDLG_TITLE();
	/**
	 * Perform the requested build
	 * @param configs
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void performAction(IConfiguration[] configs, IProgressMonitor monitor) throws CoreException;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		projects = null;

		if (!selection.isEmpty()) {
	    	// case for context menu
			if (selection instanceof IStructuredSelection) {
				Object[] obs = ((IStructuredSelection)selection).toArray();
				if (obs.length > 0) {
					for (int i=0; i<obs.length; i++) {
						IProject prj = null;
						if (obs[i] instanceof IProject)
							prj = (IProject)obs[i];
						else if (obs[i] instanceof ICProject)
							prj = ((ICProject)obs[i]).getProject();
						if (prj != null) {
							if (!CoreModel.getDefault().isNewStyleProject(prj))	continue;
							ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, false);
							if (prjd == null) continue;
							ICConfigurationDescription[] cfgds = prjd.getConfigurations();
							if (cfgds != null && cfgds.length > 0) {
								if (projects == null) projects = new ArrayList<IProject>();
								projects.add(prj);
							}
						}
					}
				}
			}
		}
		action.setEnabled(projects != null);
		if (projects != null && it_sel != null)
			it_sel.getAction().setEnabled(projects.size() == 1);
		action.setMenuCreator(this);
	}

	@Override
	public void run(IAction action) {} // do nothing - show menus only
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

	// doing nothing
	public void init(IWorkbenchWindow window) { }

	private final class BuildCleanFilesJob extends Job {
		Object[] cfs;

		BuildCleanFilesJob(Object[] _cfs) {
			super(getJOB_MSG() +	((ICConfigurationDescription)_cfs[0]).getProjectDescription().getName());
			cfs = _cfs;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IConfiguration[] cf = new IConfiguration[cfs.length];
			for (int i=0; i<cfs.length; i++)
				cf[i] = ManagedBuildManager.getConfigurationForDescription((ICConfigurationDescription)cfs[i]);
			try {
				performAction(cf, monitor);
			} catch (CoreException e) {
				return new Status(IStatus.ERROR, getERR_MSG(), e.getLocalizedMessage());
			}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
		 */
		@Override
		public boolean belongsTo(Object family) {
			return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
		}

	}

	public Menu getMenu(Control parent) {
		return fillMenu(new Menu(parent));
	}

	public Menu getMenu(Menu parent) {
		return fillMenu(new Menu(parent));
	}
	protected Menu fillMenu(Menu menu)	{
		it_all = new ActionContributionItem(new LocalAction(true));
		it_sel = new ActionContributionItem(new LocalAction(false));
		if (projects != null)
			it_sel.getAction().setEnabled(projects.size() == 1);

		it_all.fill(menu, -1);
		it_sel.fill(menu, -1);

		return menu;
	}

	private class LocalAction extends Action {
		boolean forAll;
		LocalAction(boolean mode) {
			super();
			forAll = mode;
			setText(forAll ? getLBL_ALL() : getLBL_SEL());
			setToolTipText(forAll ? getTIP_ALL() : getTIP_SEL());
		}

		@Override
		public void run() {
			if (projects == null || projects.isEmpty())
				return;
			Iterator<IProject> it = projects.iterator();
			if (forAll) {
				while(it.hasNext())
					processProject(it.next());
			} else {
				if (it.hasNext())
					processProject(it.next());
			}
		}

		private void processProject(IProject prj) {
			ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, false);
			if (prjd == null) return;
			Object[] cfgds = prjd.getConfigurations();
			if (!forAll) cfgds = openDialog(cfgds);
			if (cfgds == null || cfgds.length == 0) return;
			Job buildCleanFilesJob = new BuildCleanFilesJob(cfgds);
			buildCleanFilesJob.schedule();
		}
	}

	private Object[] openDialog(Object[] cfgds) {
		if (cfgds == null || cfgds.length == 0) return null;
		ListSelectionDialog dialog = new ListSelectionDialog(
				CUIPlugin.getActiveWorkbenchShell(),
				cfgds,
				new IStructuredContentProvider() {
					public Object[] getElements(Object inputElement) { return (Object[])inputElement; }
					public void dispose() {}
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
				},
				new LabelProvider() {
					@Override
					public String getText(Object element) {
						if (element == null || !(element instanceof ICConfigurationDescription)) return null;
						return ((ICConfigurationDescription)element).getName();
					}
				},
				getDLG_TEXT());
		dialog.setTitle(getDLG_TITLE());
		dialog.setInitialSelections(cfgds);
		return (dialog.open() == Window.OK) ? dialog.getResult() : null;
	}
}
