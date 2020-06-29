/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tools.templates.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.osgi.framework.FrameworkUtil;

/**
 * The wizard component of a template. Takes over when the template is selected in the from the
 * Template Selection Page in the parent wizard.
 */
public abstract class TemplateWizard extends BasicNewResourceWizard {

	public TemplateWizard() {
		setNeedsProgressMonitor(true);
	}

	/**
	 * The generator to be called when the wizard is finished.
	 *
	 * @return generator
	 */
	protected abstract IGenerator getGenerator();

	/**
	 * Populate the model.
	 *
	 * @param model
	 * @deprecated The subclass should initialize the generator with information in the
	 *             getGenerator() method.
	 */
	@Deprecated
	protected void populateModel(Map<String, Object> model) {
		// nothing by default
	}

	/**
	 * Perform additional UI actions after the generation is complete.
	 *
	 * @param generator
	 */
	protected void postProcess(IGenerator generator) {
		try {
			IWorkbenchPage activePage = getWorkbench().getActiveWorkbenchWindow().getActivePage();
			for (IFile file : generator.getFilesToOpen()) {
				IDE.openEditor(activePage, file);
			}
		} catch (PartInitException e) {
			log("Failed to open editor", e); //$NON-NLS-1$
		}
	}

	@Override
	public boolean performFinish() {
		IGenerator generator = getGenerator();
		// TODO remove the model in 2.0. The getGenerator method should have
		// initialized this.
		Map<String, Object> model = new HashMap<>();
		populateModel(model);

		try {
			getContainer().run(true, true, new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor)
						throws CoreException, InvocationTargetException, InterruptedException {
					SubMonitor sub = SubMonitor.convert(monitor, "Generating", 1);
					generator.generate(model, sub);
					getWorkbench().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							postProcess(generator);
						}
					});
					sub.done();
				}

				@Override
				public ISchedulingRule getRule() {
					return ResourcesPlugin.getWorkspace().getRoot();
				}
			});
		} catch (InterruptedException e) {
			handle(e);
		} catch (InvocationTargetException e) {
			handle(e.getTargetException());
		}
		return true;
	}

	private void handle(Throwable target) {
		String message = "Project cannot be created";
		log(message, target);
		IStatus status;
		if (target instanceof CoreException) {
			status = ((CoreException) target).getStatus();
		} else {
			status = new Status(IStatus.ERROR, FrameworkUtil.getBundle(getClass()).getSymbolicName(),
					"Internal Error: ", target);
		}
		ErrorDialog.openError(getShell(), "Error Creating Project", message, status);
	}

	private void log(String message, Throwable e) {
		ILog log = Platform.getLog(getClass());
		log.log(new Status(IStatus.ERROR, log.getBundle().getSymbolicName(), message, e));
	}
}
