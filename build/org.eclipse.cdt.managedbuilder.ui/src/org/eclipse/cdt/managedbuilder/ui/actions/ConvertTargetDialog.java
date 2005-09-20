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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class ConvertTargetDialog extends StatusDialog {

	final private String title;
	protected List convertersList;
	private IProject project;
	private Map conversionElements;
	private IConfigurationElement selectedConversionElement;
	private static boolean isConversionSuccessful = false;
	
	public static final String PREFIX = "ProjectConvert";	//$NON-NLS-1$
	public static final String CONVERTERS_LIST = PREFIX +".convertersList";	//$NON-NLS-1$
	
	
	/**
	 * @param parentShell
	 * @param project	  
	 * @param title The title of the dialog
	 */
	protected ConvertTargetDialog(Shell parentShell, IProject project, String title) {
		super(parentShell);
		this.title = title;
		setProject(project);
		
		conversionElements = ManagedBuildManager.getConversionElements(getProjectType());
		
		setShellStyle(getShellStyle()|SWT.RESIZE);		
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
		
			handleConverterSelection();
			IConvertManagedBuildObject convertBuildObject = null;
			try {
				convertBuildObject = (IConvertManagedBuildObject) getSelectedConversionElement()
						.createExecutableExtension("class");	//$NON-NLS-1$
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (convertBuildObject != null) {
				String fromId = getSelectedConversionElement().getAttribute(
						"fromId");	//$NON-NLS-1$
				String toId = getSelectedConversionElement().getAttribute(
						"toId");	//$NON-NLS-1$
				
				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
				if (info != null) {
					IManagedProject managedProject = info.getManagedProject();
					if (managedProject != null) {
						if (convertBuildObject.convert(managedProject, fromId,
								toId, true) == null) {
							setConversionSuccessful(false);
						} else {
							setConversionSuccessful(true);
						}
					} else {
						setConversionSuccessful(false);
					}
				} else {
					setConversionSuccessful(false);
				}				
			} else {
				setConversionSuccessful(false);
			}
		} 
		super.buttonPressed(buttonId);
	}

	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}

	protected Control createDialogArea(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setFont(parent.getFont());
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Create the converters list group area
		final Group convertersListGroup = new Group(comp, SWT.NONE);
		convertersListGroup.setFont(parent.getFont());
		convertersListGroup.setText(ManagedBuilderUIMessages.getResourceString(CONVERTERS_LIST));
		convertersListGroup.setLayout(new GridLayout(1, false));
		convertersListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		// Create the current config List
		convertersList = new List(convertersListGroup, SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);
		convertersList.setFont(convertersListGroup.getFont());
		GridData data = new GridData(GridData.FILL_BOTH);
		convertersList.setLayoutData(data);
		convertersList.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				convertersList = null;
			}
		});
		convertersList.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				validateState();
			}			
		});
		Object [] objs = getConversionElements().keySet().toArray();
		String [] names = new String[objs.length];
		for (int i = 0; i < objs.length; i++) {
			Object object = objs[i];
			names[i] = (String)object;			
		}
	    convertersList.setItems(names);
	    validateState();
		return comp;
	}
	
	private void handleConverterSelection() {
		// Determine which configuration was selected
		int selectionIndex = convertersList.getSelectionIndex();

		String selectedConverterName = convertersList
				.getItem(selectionIndex);

		IConfigurationElement selectedElement = (IConfigurationElement) getConversionElements()
				.get(selectedConverterName);
		setSelectedConversionElement(selectedElement);
		return;
	}
	
	private void validateState() {
		StatusInfo status= new StatusInfo();
		if ( convertersList.getSelectionIndex() == -1 ) {
			// No error, just disable 'Ok' button
			status.setError("");	//$NON-NLS-1$
		}
		updateStatus(status);
		return;
	}
	
	private Map getConversionElements() {
		if (conversionElements == null) {
			conversionElements = new HashMap(); 
		}
		return conversionElements;
	}

	private IProjectType getProjectType() {
		IProjectType projectType = null;

		// Get the projectType from project.
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		if (info != null) {
			IManagedProject managedProject = info.getManagedProject();
			if ( managedProject != null) {
				projectType = managedProject.getProjectType();
			}
		}
		return projectType;
	}
	
	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public IConfigurationElement getSelectedConversionElement() {
		return selectedConversionElement;
	}

	public void setSelectedConversionElement(
			IConfigurationElement selectedConversionElement) {
		this.selectedConversionElement = selectedConversionElement;
	}

	public static boolean isConversionSuccessful() {
		return isConversionSuccessful;
	}

	public void setConversionSuccessful(boolean isConversionSuccessful) {
		ConvertTargetDialog.isConversionSuccessful = isConversionSuccessful;
	}
}

