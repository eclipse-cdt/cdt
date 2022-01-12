/*******************************************************************************
 * Copyright (c) 2020 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer;
import org.eclipse.cdt.internal.ui.cview.IncludeReferenceProxy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.ChangeConfigAction;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.CompoundContributionItem;

/**
 * A dynamic contribution of items to change the build configuration of selected projects.
 *
 */
public class ChangeBuildConfigContribution extends CompoundContributionItem {

	@Override
	protected IContributionItem[] getContributionItems() {
		HashSet<IProject> projects = findProjects();

		SortedSet<String> configNames = new TreeSet<>();
		String sCurrentConfig = null;
		boolean bCurrentConfig = true;
		for (IProject prj : projects) {
			ICConfigurationDescription[] cfgDescs = getCfgs(prj);

			String sActiveConfig = null;
			// Store names and detect active configuration
			for (ICConfigurationDescription cfgDesc : cfgDescs) {
				String s = cfgDesc.getName();
				if (!configNames.contains(s))
					configNames.add(s);
				if (cfgDesc.isActive())
					sActiveConfig = s;
			}

			// Check whether all projects have the same active configuration
			if (bCurrentConfig) {
				if (sCurrentConfig == null)
					sCurrentConfig = sActiveConfig;
				else {
					if (!sCurrentConfig.equals(sActiveConfig))
						bCurrentConfig = false;
				}
			}
		}

		List<ActionContributionItem> actions = new ArrayList<>();
		int accel = 0;
		for (String sName : configNames) {
			String sDesc = null;
			boolean commonName = true;
			boolean commonDesc = true;
			boolean firstProj = true;
			for (IProject prj : projects) {
				ICConfigurationDescription[] cfgDescs = getCfgs(prj);
				int i = 0;
				for (; i < cfgDescs.length; i++) {
					if (cfgDescs[i].getName().equals(sName)) {
						String sNewDesc = cfgDescs[i].getDescription();
						if (sNewDesc != null && sNewDesc.length() == 0) {
							sNewDesc = null;
						}
						if (commonDesc) {
							if (firstProj) {
								sDesc = sNewDesc;
								firstProj = false;
							} else if (sNewDesc == null && sDesc != null
									|| sNewDesc != null && !sNewDesc.equals(sDesc)) {
								commonDesc = false;
							}
						}
						break;
					}
				}
				if (i == cfgDescs.length) {
					commonName = false;
					break;
				}
			}
			if (commonName) {
				StringBuffer builder = new StringBuffer(sName);
				if (commonDesc) {
					if (sDesc != null) {
						builder.append(" ("); //$NON-NLS-1$
						builder.append(sDesc);
						builder.append(")"); //$NON-NLS-1$
					}
				} else {
					builder.append(" (...)"); //$NON-NLS-1$
				}

				IAction action = new ChangeConfigAction(projects, sName, builder.toString(), accel + 1);
				if (bCurrentConfig && sCurrentConfig != null && sCurrentConfig.equals(sName)) {
					action.setChecked(true);
				}
				ActionContributionItem item = new ActionContributionItem(action);
				actions.add(item);
				accel++;
			}
		}
		return actions.toArray(new IContributionItem[0]);
	}

	private static HashSet<IProject> findProjects() {
		HashSet<IProject> fProjects = new LinkedHashSet<>();
		ISelection selection = CUIPlugin.getActivePage().getSelection();
		boolean badObject = addProjectsFromSelection(selection, fProjects);

		if (badObject || fProjects.isEmpty()) {
			// Check for lone CDT project in workspace
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			if (projects != null && projects.length == 1) {
				IProject project = projects[0];
				if (CoreModel.getDefault().isNewStyleProject(project) && (getCfgs(project).length > 0)) {
					fProjects.add(project);
				}
			}

			// Check the three supported views
			IWorkbenchPage page = CUIPlugin.getActivePage();
			int viewCount = 0;
			if (page != null) {
				IViewReference theViewRef = null;
				IViewReference viewRef = null;

				theViewRef = page.findViewReference("org.eclipse.cdt.ui.CView"); //$NON-NLS-1$
				viewCount += (theViewRef != null) ? 1 : 0;

				viewRef = page.findViewReference("org.eclipse.ui.navigator.ProjectExplorer"); //$NON-NLS-1$
				viewCount += (viewRef != null) ? 1 : 0;
				theViewRef = (theViewRef == null) ? viewRef : theViewRef;

				viewRef = page.findViewReference("org.eclipse.ui.views.ResourceNavigator"); //$NON-NLS-1$
				viewCount += (viewRef != null) ? 1 : 0;
				theViewRef = (theViewRef == null) ? viewRef : theViewRef;

				if (theViewRef != null && viewCount == 1) {
					IViewPart view = theViewRef.getView(false);
					if (view != null) {
						ISelection cdtSelection = view.getSite().getSelectionProvider().getSelection();
						if (cdtSelection != null) {
							if (!cdtSelection.isEmpty()) {
								if (!cdtSelection.equals(selection)) {
									addProjectsFromSelection(cdtSelection, fProjects);
								}
							}
						}
					}
				}
			}
		}
		return fProjects;
	}

	private static boolean addProjectsFromSelection(ISelection selection, HashSet<IProject> fProjects) {
		boolean badObject = false;
		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				if (selection.isEmpty()) {
					// could be a form editor or something.  try to get the project from the active part
					IWorkbenchPage page = CUIPlugin.getActivePage();
					if (page != null) {
						IWorkbenchPart part = page.getActivePart();
						if (part != null) {
							Object o = part.getAdapter(IResource.class);
							if (o != null && o instanceof IResource) {
								fProjects.add(((IResource) o).getProject());
							}
						}
					}
				}
				Iterator<?> iter = ((IStructuredSelection) selection).iterator();
				while (iter.hasNext()) {
					Object selItem = iter.next();
					IProject project = null;
					if (selItem instanceof ICElement) {
						ICProject cproject = ((ICElement) selItem).getCProject();
						if (cproject != null)
							project = cproject.getProject();
					} else if (selItem instanceof IResource) {
						project = ((IResource) selItem).getProject();
					} else if (selItem instanceof IncludeRefContainer) {
						ICProject fCProject = ((IncludeRefContainer) selItem).getCProject();
						if (fCProject != null)
							project = fCProject.getProject();
					} else if (selItem instanceof IncludeReferenceProxy) {
						IncludeRefContainer irc = ((IncludeReferenceProxy) selItem).getIncludeRefContainer();
						if (irc != null) {
							ICProject fCProject = irc.getCProject();
							if (fCProject != null)
								project = fCProject.getProject();
						}
					} else if (selItem instanceof IAdaptable) {
						Object adapter = ((IAdaptable) selItem).getAdapter(IProject.class);
						if (adapter != null && adapter instanceof IProject) {
							project = (IProject) adapter;
						}
					}
					// Check whether the project is CDT project
					if (project != null) {
						if (!CoreModel.getDefault().isNewStyleProject(project))
							project = null;
						else {
							ICConfigurationDescription[] tmp = getCfgs(project);
							if (tmp.length == 0)
								project = null;
						}
					}
					if (project != null) {
						fProjects.add(project);
					} else {
						badObject = true;
						break;
					}
				}
			} else if (selection instanceof ITextSelection) {
				// If a text selection check the selected part to see if we can find
				// an editor part that we can adapt to a resource and then
				// back to a project.
				IWorkbenchWindow window = CUIPlugin.getActiveWorkbenchWindow();
				if (window != null) {
					IWorkbenchPage page = window.getActivePage();
					if (page != null) {
						IWorkbenchPart part = page.getActivePart();
						if (part instanceof IEditorPart) {
							IEditorPart epart = (IEditorPart) part;
							IResource resource = epart.getEditorInput().getAdapter(IResource.class);
							if (resource != null) {
								IProject project = resource.getProject();
								badObject = !(project != null && CoreModel.getDefault().isNewStyleProject(project));

								if (!badObject) {
									fProjects.add(project);
								}
							}
						}
					}
				}

			}
		}
		return badObject;
	}

	private static ICConfigurationDescription[] getCfgs(IProject prj) {
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, false);
		if (prjd != null) {
			ICConfigurationDescription[] cfgs = prjd.getConfigurations();
			if (cfgs != null) {
				return cfgs;
			}
		}

		return new ICConfigurationDescription[0];
	}
}
