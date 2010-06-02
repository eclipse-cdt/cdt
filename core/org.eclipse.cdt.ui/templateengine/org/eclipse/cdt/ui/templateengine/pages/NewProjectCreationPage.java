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
package org.eclipse.cdt.ui.templateengine.pages;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;

/**
 * The first page in a NewProjectWizard. This is the wizard page that
 * asks the user for the name and location of the new project.
 */
public class NewProjectCreationPage extends WizardNewProjectCreationPage implements IWizardDataPage {	
	private static final String ERROR_SUFFIX = Messages.getString("NewProjectCreationPage.0"); //$NON-NLS-1$
	private static final String ERROR_SUFFIX_TOO_LONG = Messages.getString("NewProjectCreationPage.1"); //$NON-NLS-1$
	private static final Status OK_STATUS = new Status(IStatus.OK, CUIPlugin.getPluginId(), 0, "", null); //$NON-NLS-1$

	private Map<String, String> data;
	private IWizardPage next;
	
	public NewProjectCreationPage(String name) {
		super(name);
		data= new HashMap<String, String>();
		this.setDescription(Messages.getString("NewProjectCreationPage.3"));	 //$NON-NLS-1$
	}

	public Map<String, String> getPageData() {
		String projName = super.getProjectName().trim();
		data.put("projectName", projName); //$NON-NLS-1$
		data.put("baseName", getBaseName(projName)); //$NON-NLS-1$
		data.put("baseNameUpper", getBaseName(projName).toUpperCase() ); //$NON-NLS-1$
		data.put("baseNameLower", getBaseName(projName).toLowerCase() ); //$NON-NLS-1$
		data.put("location", super.getLocationPath().toPortableString()); //$NON-NLS-1$
		return data;
	}

	private String getBaseName(String projName) {
		String baseName = projName;
		int dot = baseName.lastIndexOf('.');
		if (dot != -1) {
			baseName = baseName.substring(dot + 1);
		}
		dot = baseName.indexOf(' ');
		if (dot != -1) {
			baseName = baseName.substring(0, dot);
		}
		return baseName;
	}
	
	@Override
	protected boolean validatePage() {
		if (super.validatePage() == true) {
			IStatus validName = isValidName(getProjectName());
			if (!validName.isOK()) {
				setErrorMessage(validName.getMessage());
				return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Projects names should only be alphanumeric and can contain ' ' and '_' chars. 
	 * Names are limited to 31 chars. Names cannot end in a '.' char.  Note that '.'
	 * characters currently not allowed as many of the command line generators get
	 * the name wrong for generated files.  e.g. my.foo project name results in
	 * foo.rsc, foo.rsg, etc.. rather than the expected my.foo.rsc.
	 * @param projectName - The unmodified project name from the project wizard. 
	 * @return an IStatus message on error.
	 * 
	 * Note: Platform may have a different project name constraints. Please subclass this
	 * to add your own versions of validnames for template projects.
	 */
	private IStatus isValidName(String projectName) {
		//String baseName = getBaseName(projectName);
		String baseName = projectName;
		
		if (!Character.isLetter(baseName.charAt(0))) {
			return new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR, projectName + ERROR_SUFFIX, null);
		}
		
		if (baseName.length() > 31) {
			return new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR, projectName + ERROR_SUFFIX_TOO_LONG, null);
		}
		
		for (int i = 1, l = baseName.length(); i < l; i++) {
			char c = baseName.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '_' && c != ' ') {
				return new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR, projectName + ERROR_SUFFIX, null);
			}
		}
		
		return OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.ui.templateengine.IWizardDataPage#setNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public void setNextPage(IWizardPage next) {
		this.next= next;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {
		if(next != null) {
			return next;
		}
		return super.getNextPage();
	}
}
