/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Intel corporation    - customization for New Project model.
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

 
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class ConvertToMakeWizardPage extends ConvertProjectWizardPage {
    
    private static final String WZ_TITLE = "WizardMakeProjectConversion.title"; //$NON-NLS-1$
    private static final String WZ_DESC = "WizardMakeProjectConversion.description"; //$NON-NLS-1$
    
	/**
	 * Constructor for ConvertToStdMakeProjectWizardPage.
	 * @param pageName
	 */
	public ConvertToMakeWizardPage(String pageName) {
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
     * Method isCandidate returns true for:
     * - non-CDT projects
     * - old style Make CDT projects
     * So new model projects and 
     * old style managed projects
     * are refused.
     */
    public boolean isCandidate(IProject project) {
    	boolean a = !AbstractPage.isCDTPrj(project); 
    	boolean b = ManagedBuilderCorePlugin.getDefault().isOldStyleMakeProject(project);
		return a || b; 
    }    

    public void convertProject(IProject project, String bsId, IProgressMonitor monitor) throws CoreException{
		monitor.beginTask(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), 3); //$NON-NLS-1$
		try {
			if (ManagedBuilderCorePlugin.getDefault().isOldStyleMakeProject(project)) {
				ManagedBuilderCorePlugin.getDefault().convertOldStdMakeToNewStyle(project, monitor);
			} else { 
				super.convertProject(project, bsId, new SubProgressMonitor(monitor, 1));
			}
		} finally {
			monitor.done();
		}
    }

	public void convertProject(IProject project, IProgressMonitor monitor, String projectID) throws CoreException {
		monitor.beginTask(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), 3); //$NON-NLS-1$
		try {
			if (ManagedBuilderCorePlugin.getDefault().isOldStyleMakeProject(project)) {
				ManagedBuilderCorePlugin.getDefault().convertOldStdMakeToNewStyle(project, monitor);
			} else { 
				super.convertProject(project, new SubProgressMonitor(monitor, 1), projectID);
			}
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
