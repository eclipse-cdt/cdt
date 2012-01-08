/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Symbian - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import org.eclipse.cdt.core.templateengine.TemplateCore;

public class TemplateClassWizard extends TemplatesChoiceWizard implements INewWizard, IExecutableExtension {

	public static final String WIZARD_ID = TemplateClassWizard.class.getName();

	private IWizardDataPage[] pagesBeforeTemplatePages;
	private IWizardDataPage[] pagesAfterTemplatePages;
	private IWizardDataPage[] pagesAfterTemplateSelection;

	private ProjectSelectionPage projectSelectionPage;

	private IConfigurationElement configElement;

	public TemplateClassWizard() {
		super();
		setWindowTitle(Messages.getString("TemplateClassWizard.0")); //$NON-NLS-1$
		//TODO: Fix the imagedescriptor later.
		//setDefaultPageImageDescriptor(TemplateEnginePlugin.imageDescriptorFromPlugin(TemplateEnginePlugin.getDefault().getWizardIconPluginID(), TemplateEnginePlugin.getDefault().getWizardIconFile()));
	}

	@Override
	public String getListSelectionTitle()
	{
		return Messages.getString("TemplateClassWizard.1"); //$NON-NLS-1$
	}

	@Override
	public String getListSelectionDescription()
	{
		return Messages.getString("TemplateClassWizard.2"); //$NON-NLS-1$
	}

	@Override
	public String getListSelectionLabel()
	{
		return Messages.getString("TemplateClassWizard.3"); //$NON-NLS-1$
	}

	@Override
	protected IWizardDataPage[] getPagesBeforeTemplatePages() {
		if (pagesBeforeTemplatePages == null) {
			projectSelectionPage = new ProjectSelectionPage();
			projectSelectionPage.setTitle(Messages.getString("TemplateClassWizard.4")); //$NON-NLS-1$
			projectSelectionPage.setDescription(Messages.getString("TemplateClassWizard.5")); //$NON-NLS-1$
			projectSelectionPage.init(selection);
			pagesBeforeTemplatePages = new IWizardDataPage[] {projectSelectionPage};
		}
		return pagesBeforeTemplatePages;
	}

	@Override
	protected IWizardDataPage[] getPagesAfterTemplatePages() {
		if (pagesAfterTemplatePages == null) {
			pagesAfterTemplatePages = new IWizardDataPage[] {};
		}
		return pagesAfterTemplatePages;
	}

	@Override
	public Template[] getTemplates() {
		SortedSet<TemplateCore> templateList = new TreeSet<TemplateCore>(TemplateCore.TEMPLATE_ID_CASE_INSENSITIVE_COMPARATOR);
		templateList.addAll(Arrays.asList(TemplateEngineUI.getDefault().getTemplates()));
		return templateList.toArray(new Template[templateList.size()]);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		configElement = config;
	}

	@Override
	public boolean performFinish() {
		boolean retVal = super.performFinish();
		BasicNewProjectResourceWizard.updatePerspective(configElement);
		return retVal;
	}

	@Override
	protected IWizardDataPage[] getPagesAfterTemplateSelection() {
		if (pagesAfterTemplateSelection == null) {
			pagesAfterTemplateSelection = new IWizardDataPage[] {};
		}
		return pagesAfterTemplateSelection;
	}

	@Override
	public String getDescription(Object object) {
		if (object instanceof Template)
		{
			return ((Template)object).getDescription();
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public boolean showTemplatesInTreeView() {
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getTemplates();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}
}
