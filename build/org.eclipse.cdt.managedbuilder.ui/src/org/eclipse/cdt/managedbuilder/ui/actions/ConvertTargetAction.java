/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.actions;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

public class ConvertTargetAction
	extends ActionDelegate
	implements IObjectActionDelegate {

	private IProject selectedProject = null;
	
	public static final String PREFIX = "ProjectConvert";	//$NON-NLS-1$
	public static final String PROJECT_CONVERTER_DIALOG = PREFIX + ".title";	//$NON-NLS-1$
	
	public static void initStartup() {
		return;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object obj = sel.getFirstElement();
			if (obj instanceof IProject) {
				 IProject project = (IProject)obj;				 
				 // Save the selected project.
				 setSelectedProject(project);
				 return;
			}
		}
		setSelectedProject(null);
	}

	private IProjectType getProjectType(IProject project) {
		IProjectType projectType = null;

		// Get the projectType from project.
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		if (info != null) {
			IManagedProject managedProject = info.getManagedProject();
			if ( managedProject != null )
				projectType = managedProject.getProjectType();
		}
		return projectType;
	}
	

	public void run(IAction action) {
		Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
		
		// Check whether the converters available for the selected project
		// If there are no converters display error dialog otherwise display converters list
		
		if( ManagedBuildManager.hasTargetConversionElements(getProjectType(getSelectedProject())) == true ) {			
			handleConvertTargetAction();
		} else {
			MessageDialog.openError(shell,ManagedBuilderUIMessages.getResourceString("ProjectConvert.noConverterErrordialog.title"), //$NON-NLS-1$
			        ManagedBuilderUIMessages.getFormattedString("ProjectConvert.noConverterErrordialog.message", new String[] {getSelectedProject().getName()}) );  //$NON-NLS-1$
		}
	}

	private void handleConvertTargetAction() {
		Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
		
		String title = ManagedBuilderUIMessages.getFormattedString(PROJECT_CONVERTER_DIALOG, new String(getSelectedProject().getName())); 
		ConvertTargetDialog dialog = new ConvertTargetDialog(shell, getSelectedProject(), title);
		if ( dialog.open() == ConvertTargetDialog.OK ) {
			if ( ConvertTargetDialog.isConversionSuccessful() == false) {									
				MessageDialog.openError(
								shell,
								ManagedBuilderUIMessages
										.getResourceString("ProjectConvert.conversionErrordialog.title"), //$NON-NLS-1$
								ManagedBuilderUIMessages
										.getFormattedString(
												"ProjectConvert.conversionErrordialog.message", new String[] { getSelectedProject().getName() })); //$NON-NLS-1$
			}
		}
		return;
	}
		
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub	
	}

	/**
	 * @return Returns the selectedProject.
	 */
	private IProject getSelectedProject() {
		return selectedProject;
	}

	/**
	 * @param selectedProject The selectedProject to set.
	 */
	private void setSelectedProject(IProject selectedProject) {
		this.selectedProject = selectedProject;
	}
}
