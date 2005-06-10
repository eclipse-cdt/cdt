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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
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

	private String  converterId = null;
	private String	fromId = null;
	private String	toId = null;
	private IConvertManagedBuildObject  convertBuildObject = null;
	private IProject selectedProject = null;
	
	public static void initStartup() {
		return;
	}
	
	public void initConvertAction(IAction action) {
		convertBuildObject = null;
		String id = action.getId();
		try {
			
			// Get the Converter Extension Point
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
					.getExtensionPoint("org.eclipse.cdt.managedbuilder.core",	//$NON-NLS-1$
							"projectConverter");	//$NON-NLS-1$
			if (extensionPoint != null) {
				// Get the Converter Extensions
				IExtension[] extensions = extensionPoint.getExtensions();
				List list = new ArrayList(extensions.length);
				for (int i = 0; i < extensions.length; i++) {
					
					// Get the configuration elements for each extension
					IConfigurationElement[] configElements = extensions[i]
							.getConfigurationElements();

					for (int j = 0; j < configElements.length; j++) {
						IConfigurationElement element = configElements[j];

						if (element.getName().equals("converter")) {	//$NON-NLS-1$
							// Get the converter 'id'
							String tmpConverterID = element.getAttribute("id"); //$NON-NLS-1$
							// If the converter 'id' and action 'id' are same.
							if (id.equals(tmpConverterID)) {

								convertBuildObject = (IConvertManagedBuildObject) element
									.createExecutableExtension("class"); //$NON-NLS-1$
								fromId = element.getAttribute("fromId"); //$NON-NLS-1$
								toId = element.getAttribute("toId"); //$NON-NLS-1$
								return;
							}
						}
					}
				}
			}
		} catch (CoreException e) {
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		initConvertAction(action);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object obj = sel.getFirstElement();
			if (obj instanceof IProject) {
				 IProject project = (IProject)obj;
				 
				 // Save the selected project.
				 setSelectedProject(project);
				 
				 // If the project does not have managed build nature then disable the action.
				 try {
					if(!project.hasNature("org.eclipse.cdt.managedbuilder.core.managedBuildNature")) {	//$NON-NLS-1$
						 action.setEnabled(false);
						 return;
					 }
				} catch (CoreException e) {
//					 e.printStackTrace();
				}
					 
				// Get the projectType of the project.
				IProjectType projectType = getProjectType(project);

				// Check whether the Converter can convert the selected project.
				 if( isProjectConvertable(projectType))
					 action.setEnabled(true);
				 else
					 action.setEnabled(false);
			} else {
				action.setEnabled(false);
			}
		}
	}
	
	private IProjectType getProjectType(IProject project) {
		IProjectType projectType = null;

		// Get the projectType from project.
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		if (info != null) {
			IManagedProject managedProject = info.getManagedProject();
			projectType = managedProject.getProjectType();
		}
		return projectType;
	}
	
	
	private boolean isProjectConvertable(IProjectType projectType) {
		
		IProjectType  tmpProjectType = projectType;
		
//		Check whether the converter can convert the given projectType
		if(fromId == null)
			return false;

		while( tmpProjectType != null) {
			String id = tmpProjectType.getId();
			
			if (fromId.equals(id))
				return true;
			else
				tmpProjectType = tmpProjectType.getSuperClass();
		}
		return false;
	}


	
	public void run(IAction action) {
		if( convertBuildObject != null) {
//			 Get the confirmation from user before converting the selected project
			Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
			boolean shouldConvert = MessageDialog.openQuestion(shell,
			        ManagedBuilderUIMessages.getResourceString("ProjectConvert.confirmdialog.title"), //$NON-NLS-1$
			        ManagedBuilderUIMessages.getFormattedString("ProjectConvert.confirmdialog.message",  //$NON-NLS-1$
			                new String[] {getSelectedProject().getName()}));
			
			if (shouldConvert) {
				convertBuildObject.convert( getProjectType(getSelectedProject()), getFromId(), getToId(), true);
			}
		}
		
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		
	}

	private IConvertManagedBuildObject getConvertBuildObject() {
		return convertBuildObject;
	}

	private void setConvertBuildObject(IConvertManagedBuildObject convertBuildObject) {
		this.convertBuildObject = convertBuildObject;
	}

	private String getConverterId() {
		return converterId;
	}

	private void setConverterId(String converterId) {
		this.converterId = converterId;
	}

	private String getFromId() {
		return fromId;
	}

	private void setFromId(String fromId) {
		this.fromId = fromId;
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

	/**
	 * @return Returns the toId.
	 */
	private String getToId() {
		return toId;
	}

	/**
	 * @param toId The toId to set.
	 */
	private void setToId(String toId) {
		this.toId = toId;
	}
}
