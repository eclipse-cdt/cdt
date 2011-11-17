/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

import org.eclipse.cdt.core.templateengine.TemplateInfo;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.ui.CUIPlugin;


/**
 * A wizard intending to show a choice of templates (@see org.eclipse.cdt.core.templateenginee.Template)
 * before switching to the pages driven by the chosen template should extend from TemplatesChoiceWizard.
 * Alternatively, when a choice of templates needn't be shown, TemplateDrivenWizard is a better fit.
 * (@see org.eclipse.cdt.ui.templateengine.TemplateDrivenWizard)
 *
 */
public abstract class TemplatesChoiceWizard extends Wizard implements ITemplatesListProvider, IWorkbenchWizard {
	private static final boolean DEBUG = false;
	private TemplateListSelectionPage templateListSelectionPage;
	protected IWorkbench workbench;
	protected IStructuredSelection selection;

	@Override
	public final void addPages() {
		IWizardPage[] pages = getPagesBeforeTemplatePages();
		for (IWizardPage page : pages) {
			addPage(page);
		}

		templateListSelectionPage = new TemplateListSelectionPage(this);
		addPage(templateListSelectionPage);

		pages = getPagesAfterTemplatePages();
		for (IWizardPage page : pages) {
			addPage(page);
		}
	}

	public String getListSelectionTitle()
	{
		return Messages.getString("TemplatesChoiceWizard.0"); //$NON-NLS-1$
	}

	public String getListSelectionDescription()
	{
		return Messages.getString("TemplatesChoiceWizard.1"); //$NON-NLS-1$
	}

	public String getListSelectionLabel()
	{
		return Messages.getString("TemplatesChoiceWizard.2"); //$NON-NLS-1$
	}

	protected abstract IWizardDataPage[] getPagesBeforeTemplatePages();

	protected abstract IWizardDataPage[] getPagesAfterTemplatePages();

	protected abstract IWizardDataPage[] getPagesAfterTemplateSelection();

	IWizardDataPage[] getPagesAfterTemplateSelectionWithExtraPages(Template template) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		IWizardDataPage[] pages = getPagesAfterTemplateSelection();
		TemplateInfo templateInfo = template.getTemplateInfo();
		IPagesAfterTemplateSelectionProvider extraPagesProvider = (IPagesAfterTemplateSelectionProvider) templateInfo.getExtraPagesProvider();
		if (extraPagesProvider != null) {
			List<IWizardDataPage> pageList = new ArrayList<IWizardDataPage>(Arrays.asList(pages));
			IWizardDataPage[] extraPages = extraPagesProvider.createAdditionalPages(this, workbench, selection);
			pageList.addAll(Arrays.asList(extraPages));
			pages = pageList.toArray(new IWizardDataPage[pageList.size()]);
		}
		return pages;
	}

	IWizardDataPage[] getExtraCreatedPages(Template template) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		TemplateInfo templateInfo = template.getTemplateInfo();
		IPagesAfterTemplateSelectionProvider extraPagesProvider = (IPagesAfterTemplateSelectionProvider) templateInfo.getExtraPagesProvider();
		if (extraPagesProvider != null) {
			List<IWizardDataPage> pageList = new ArrayList<IWizardDataPage>();
			IWizardDataPage[] extraPages = extraPagesProvider.getCreatedPages(this);
			pageList.addAll(Arrays.asList(extraPages));
			return pageList.toArray(new IWizardDataPage[pageList.size()]);
		}
		return new IWizardDataPage[0];
	}

	@Override
	public boolean performFinish() {
		IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				finishPage(monitor);
			}
		});

		try {
			getContainer().run(true, false, op);
		} catch (InvocationTargetException e) {
			return false;
		} catch  (InterruptedException e) {
			return false;
		}
		return true;

	}

	private boolean finishPage(IProgressMonitor monitor) {
		IStatus[] statuses = templateListSelectionPage.getTemplate().executeTemplateProcesses(monitor, false);
		if (statuses.length == 1 && statuses[0].getException() instanceof ProcessFailureException) {
			TemplateEngineUIUtil.showError(statuses[0].getMessage(), statuses[0].getException());
			return false;
		}
		if (DEBUG) {
			String msg = Messages.getString("TemplatesChoiceWizard.3"); //$NON-NLS-1$
			TemplateEngineUIUtil.showStatusDialog(msg, new MultiStatus(CUIPlugin.getPluginId(), IStatus.OK, statuses, msg, null));
		}
		return true;
	}

	/**
	 * Returns the Data in Non-Template Pages.
	 * @return Map,
	 */
	public Map<String, String> getAllDataInNonTemplatePages() {
		Map<String, String> map = new HashMap<String, String>();

		IWizardDataPage[] pages = getPagesBeforeTemplatePages();
		for (IWizardDataPage page : pages) {
			map.putAll(page.getPageData());
		}

		pages = getPagesAfterTemplateSelection();
		for (IWizardDataPage page : pages) {
			map.putAll(page.getPageData());
		}

		try {
			pages = getExtraCreatedPages(getSelectedTemplate());
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (IWizardDataPage page : pages) {
			map.putAll(page.getPageData());
		}

		pages = getPagesAfterTemplatePages();
		for (IWizardDataPage page : pages) {
			map.putAll(page.getPageData());
		}

		return map;
	}

	/**
	 * initializes the workbench
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.workbench = workbench;
		this.selection = currentSelection;
		initializeDefaultPageImageDescriptor();
	}

	protected void initializeDefaultPageImageDescriptor() {
		// setDefaultPageImageDescriptor(descriptor);
	}

	public Template getSelectedTemplate() {
		return templateListSelectionPage.getTemplate();
	}

	public void adjustTemplateValues(Template template) {
		// Give the wizard a chance to adjust template values before they go into the page controls.
	}
}
