/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.wizards;

 
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 *
 * ConvertToStdMakeProjectWizardPage
 * Standard main page for a wizard that adds a C project Nature to a project with no nature associated with it.
 * This conversion is one way in that the project cannot be converted back (i.e have the nature removed).
 *
 * @author Judy N. Green
 * @since Aug 6, 2002
 *<p>
 * Example useage:
 * <pre>
 * mainPage = new ConvertToStdMakeProjectWizardPage("ConvertProjectPage");
 * mainPage.setTitle("Project Conversion");
 * mainPage.setDescription("Add C or C++ a Nature to a project.");
 * </pre>
 * </p>
 */
public class ConvertToMakeProjectWizardPage extends ConvertProjectWizardPage {
    
    private static final String WZ_TITLE = "WizardMakeProjectConversion.title"; //$NON-NLS-1$
    private static final String WZ_DESC = "WizardMakeProjectConversion.description"; //$NON-NLS-1$
    
	/**
	 * Constructor for ConvertToStdMakeProjectWizardPage.
	 * @param pageName
	 */
	public ConvertToMakeProjectWizardPage(String pageName) {
		super(pageName);
	}
    
    /**
     * Method getWzTitleResource returns the correct Title Label for this class
     * overriding the default in the superclass.
     */
    protected String getWzTitleResource(){
        return MakeUIPlugin.getResourceString(WZ_TITLE);
    }
    
    /**
     * Method getWzDescriptionResource returns the correct description
     * Label for this class overriding the default in the superclass.
     */
    protected String getWzDescriptionResource(){
        return MakeUIPlugin.getResourceString(WZ_DESC);
    }
       
    /**
     * Method isCandidate returns true for all projects.
     * 
     * @param project
     * @return boolean
     */
    public boolean isCandidate(IProject project) { 
		return true; // all 
    }    
    
	public void convertProject(IProject project, IProgressMonitor monitor, String projectID) throws CoreException {
		monitor.beginTask(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), 3); //$NON-NLS-1$
		try {
			super.convertProject(project, new SubProgressMonitor(monitor, 1), projectID);
			MakeProjectNature.addNature(project, new SubProgressMonitor(monitor, 1));
			ScannerConfigNature.addScannerConfigNature(project);
			CCorePlugin.getDefault().mapCProjectOwner(project, projectID, true);
		} finally {
			monitor.done();
		}
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		IStructuredSelection sel = ((BasicNewResourceWizard)getWizard()).getSelection();
		if ( sel != null) {
			tableViewer.setCheckedElements(sel.toArray());
			setPageComplete(validatePage());
		}
	}

}
