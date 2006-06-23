/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.wizards.UpdateMakeProjectWizard;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class UpdateMakeProjectAction implements IWorkbenchWindowActionDelegate {

	private ISelection fSelection;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		if (fSelection instanceof IStructuredSelection) {
			Object[] elems = ((IStructuredSelection) fSelection).toArray();
			ArrayList projects = new ArrayList(elems.length);

			for (int i = 0; i < elems.length; i++) {
				Object elem = elems[i];
				IProject project = null;

				if (elem instanceof IFile) {
					IFile file = (IFile) elem;
					project = file.getProject();
				} else if (elem instanceof IProject) {
					project = (IProject) elem;
				} else if (elem instanceof ICProject) {
					project = ((ICProject) elem).getProject();
				}
				if (project != null) {
					projects.add(project);
				}
			}

			final IProject[] projectArray = (IProject[]) projects.toArray(new IProject[projects.size()]);

			UpdateMakeProjectWizard wizard = new UpdateMakeProjectWizard(projectArray);
			WizardDialog dialog = new WizardDialog(MakeUIPlugin.getActiveWorkbenchShell(), wizard);
			dialog.open();
		}

	}

	public static IProject[] getOldProjects() {
		IProject[] project = MakeUIPlugin.getWorkspace().getRoot().getProjects();
		Vector result = new Vector();
		try {
			for (int i = 0; i < project.length; i++) {
				if (isOldProject(project[i])) {
					result.add(project[i]);
				}
			}
		} catch (CoreException e) {
			MakeUIPlugin.logException(e);
		}

		return (IProject[]) result.toArray(new IProject[result.size()]);
	}

	protected static boolean isOldProject(IProject project) throws CoreException {
		if (project.isAccessible()) {
			IProjectDescription desc = project.getDescription();
			ICommand builder[] = desc.getBuildSpec();
			for (int j = 0; j < builder.length; j++) {
				if (builder[j].getBuilderName().equals(MakeCorePlugin.OLD_BUILDER_ID)) {
					return true;
				}
			}
		}
		return false;
	}

	static public void run(boolean fork, IRunnableContext context, final IProject[] projects) {
		try {
			context.run(fork, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) throws CoreException {
								doProjectUpdate(monitor, projects);
							}
						};
						MakeUIPlugin.getWorkspace().run(runnable, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} catch (OperationCanceledException e) {
						throw new InterruptedException(e.getMessage());
					}
				}
			});
		} catch (InterruptedException e) {
			return;
		} catch (InvocationTargetException e) {
			MakeUIPlugin.logException(e, MakeUIPlugin.getResourceString("UpdateMakeProjectAction.exception.error"), MakeUIPlugin.getResourceString("UpdateMakeProjectAction.eception.message")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static class TargetConvertVisitor implements IResourceProxyVisitor {
		private final int TOTAL_WORK = 100;
		private int halfWay = TOTAL_WORK / 2;
		private int currentIncrement = 4;
		private int nextProgress = currentIncrement;
		private int worked = 0;
		IProgressMonitor monitor;

		public TargetConvertVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
			monitor.beginTask(MakeUIPlugin.getResourceString("UpdateMakeProjectAction.monitor.convert"), TOTAL_WORK); //$NON-NLS-1$
		}

		public boolean visit(IResourceProxy proxy) throws CoreException {
			try {
				if (proxy.getType() != IResource.FOLDER && proxy.getType() != IResource.PROJECT) {
					return false;
				}
				IContainer container = (IContainer) proxy.requestResource();
				monitor.subTask(container.getProjectRelativePath().toString());
				QualifiedName qName = new QualifiedName("org.eclipse.cdt.make", "goals"); //$NON-NLS-1$ //$NON-NLS-2$
				String goal = container.getPersistentProperty(qName);
				if (goal != null) {
					goal = goal.trim();
					IMakeTargetManager manager = MakeCorePlugin.getDefault().getTargetManager();
					String[] builder = manager.getTargetBuilders(container.getProject());
					IMakeTarget target = manager.createTarget(container.getProject(), goal, builder[0]);
					target.setBuildTarget(goal);
					manager.addTarget(container, target);
					container.setPersistentProperty(qName, null);
				}
				return true;
			} finally {
				if (--nextProgress <= 0) {
					//we have exhausted the current increment, so report progress
					monitor.worked(1);
					worked++;
					if (worked >= halfWay) {
						//we have passed the current halfway point, so double the
						//increment and reset the halfway point.
						currentIncrement *= 2;
						halfWay += (TOTAL_WORK - halfWay) / 2;
					}
					//reset the progress counter to another full increment
					nextProgress = currentIncrement;
				}

			}
		}
	}

	protected static void doProjectUpdate(IProgressMonitor monitor, IProject[] project) throws CoreException {
		monitor.beginTask(MakeUIPlugin.getResourceString("UpdateMakeProjectAction.monitor.update"), project.length * 4); //$NON-NLS-1$
		try {
			for (int i = 0; i < project.length; i++) {
				// remove old builder
				project[i].refreshLocal(IResource.DEPTH_ONE, new SubProgressMonitor(monitor, 1));
				MakeProjectNature.removeFromBuildSpec(
					project[i],
					MakeCorePlugin.OLD_BUILDER_ID,
					new SubProgressMonitor(monitor, 1));

				// convert .cdtproject
				CCorePlugin.getDefault().mapCProjectOwner(project[i], MakeCorePlugin.MAKE_PROJECT_ID, true);
				// add new nature
				MakeProjectNature.addNature(project[i], new SubProgressMonitor(monitor, 1));

				// move existing build properties to new
				IMakeBuilderInfo newInfo = MakeCorePlugin.createBuildInfo(project[i], MakeBuilder.BUILDER_ID);
				final int LOCATION = 0, FULL_ARGS = 1, INC_ARGS = 2, STOP_ERORR = 3, USE_DEFAULT = 4;
				QualifiedName[] qName = new QualifiedName[USE_DEFAULT + 1];
				qName[LOCATION] = new QualifiedName(CCorePlugin.PLUGIN_ID, "buildLocation"); //$NON-NLS-1$
				qName[FULL_ARGS] = new QualifiedName(CCorePlugin.PLUGIN_ID, "buildFullArguments"); //$NON-NLS-1$
				qName[INC_ARGS] = new QualifiedName(CCorePlugin.PLUGIN_ID, "buildIncrementalArguments"); //$NON-NLS-1$
				qName[STOP_ERORR] = new QualifiedName(CCorePlugin.PLUGIN_ID, "stopOnError"); //$NON-NLS-1$
				qName[USE_DEFAULT] = new QualifiedName(CCorePlugin.PLUGIN_ID, "useDefaultBuildCmd"); //$NON-NLS-1$

				String property = project[i].getPersistentProperty(qName[LOCATION]);
				if (property != null) {
					newInfo.setBuildCommand(new Path(property));
				}
				property = project[i].getPersistentProperty(qName[FULL_ARGS]);
				if (property != null) {
					newInfo.setBuildArguments(property);
				}
				property = project[i].getPersistentProperty(qName[STOP_ERORR]);
				if (property != null) {
					newInfo.setStopOnError(Boolean.valueOf(property).booleanValue());
				}
				property = project[i].getPersistentProperty(qName[USE_DEFAULT]);
				if (property != null) {
					newInfo.setUseDefaultBuildCmd(Boolean.valueOf(property).booleanValue());
				}
				for (int j = 0; j < qName.length; j++) {
					project[i].setPersistentProperty(qName[j], null);
				}

				IProgressMonitor subMon = new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				project[i].accept(new TargetConvertVisitor(subMon), 0);
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		boolean enabled = false;
		fSelection = selection;
		if (fSelection instanceof IStructuredSelection) {
			Object sel = ((IStructuredSelection) fSelection).getFirstElement();
			if (sel instanceof IAdaptable) {
				IResource res = (IResource) ((IAdaptable) sel).getAdapter(IResource.class);
				try {
					if (res instanceof IProject && isOldProject((IProject) res)) {
						enabled = true;
					}
				} catch (CoreException e) {
				}
			}
		}
		action.setEnabled(enabled);
	}
}
