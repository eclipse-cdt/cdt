/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tools.templates.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.internal.Activator;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

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
			Activator.log(e);
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
			Activator.errorDialog(getShell(), "Error Creating Project", "Project cannot be created", e, true);
		} catch (InvocationTargetException e) {
			Activator.errorDialog(getShell(), "Error Creating Project", "Project cannot be created", e.getTargetException(), true);
		}
		return true;
	}

}
