package org.eclipse.cdt.ui.build.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.IWizardTab;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.RadioButtonsArea;
import org.eclipse.cdt.utils.ui.swt.IValidation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ConfigurationBlock implements IWizardTab {
	/* (non-Javadoc)
	 * String constants
	 */
	private static final String PREFIX = "ConfigurationBlock"; //$NON-NLS-1$
	private static final String TYPE = PREFIX + ".type"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String APP = TYPE + ".app"; //$NON-NLS-1$
	private static final String DLL = TYPE + ".shared"; //$NON-NLS-1$
	private static final String LIB = TYPE + ".static"; //$NON-NLS-1$
	private static final String BUILD = PREFIX + ".build"; //$NON-NLS-1$
	private static final String BUILD_LABEL = BUILD + ".label"; //$NON-NLS-1$
	private static final String CONT = BUILD + ".continue"; //$NON-NLS-1$
	private static final String STOP = BUILD + ".stop"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * Bookeeping variables
	 */
	private IValidation page;
	private ManagedProjectWizard fWizard;

	/* (non-Javadoc)
	 * Widgets used on the tab
	 */
	protected Composite composite;
	protected GridData gd;
	protected RadioButtonsArea typeRadioButtons;
	private String [][] types;
	private static final String APP_ARG = "exe"; //$NON-NLS-1$
	private static final String DLL_ARG = "dll"; //$NON-NLS-1$
	private static final String LIB_ARG = "lib"; //$NON-NLS-1$
	protected RadioButtonsArea optRadioButtons;
	private String [][] opts;
	private static final String CONT_ARG = "cont"; //$NON-NLS-1$
	private static final String STOP_ARG = "stop"; //$NON-NLS-1$
	
	public ConfigurationBlock(IValidation valid, ManagedProjectWizard wizard) {
		page = valid;
		fWizard = wizard;
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getLabel()
	 */
	public String getLabel() {
		return CUIPlugin.getResourceString(LABEL);
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getImage()
	 */
	public Image getImage() {
//		return CPluginImages.get(CPluginImages.IMG_BUILD_CONFIG);
		return null;
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public Composite getControl(Composite parent) {
		// Create the composite control for the tab
		composite = ControlFactory.createComposite(parent, 2);
		
		// Create the application type selection area and select the application option
		types = new String [][] {
			{CUIPlugin.getResourceString(APP), APP_ARG}, 
			{CUIPlugin.getResourceString(DLL), DLL_ARG},
			{CUIPlugin.getResourceString(LIB), LIB_ARG}
		};
		typeRadioButtons = new RadioButtonsArea(composite, CUIPlugin.getResourceString(TYPE), 1, types);
		typeRadioButtons.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				page.setComplete(isValid());
			}
		});
		
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		typeRadioButtons.setLayoutData(gd);
		
		// Create the build option buttons
		opts = new String [][] {
			{CUIPlugin.getResourceString(CONT), CONT_ARG},
			{CUIPlugin.getResourceString(STOP), STOP_ARG}
		};
		optRadioButtons = new RadioButtonsArea(composite, CUIPlugin.getResourceString(BUILD_LABEL), 1, opts);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		optRadioButtons.setLayoutData(gd);

		// Return the widget
		return composite;
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#isValid()
	 */
	public boolean isValid() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {

		// Set the executable radio button by default
		typeRadioButtons.setSelectedButton(0);

		// Set the build option radio button based on the platform default
		optRadioButtons.setSelectedButton(0);
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#doRun(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doRun(IProject project, IProgressMonitor monitor) {
		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask("Configuration", 1);
			
			// Get the project nature;
			CProjectNature nature =	(CProjectNature) project.getNature(CProjectNature.C_NATURE_ID);
			
			// Set the build options on the project nature
			if (nature != null) {
				nature.setStopOnError(isStopOnError());
			}
		}
		catch (CoreException e) {
		}
	}

	/**
	 * Method isStopOnError.
	 * @return boolean
	 */
	private boolean isStopOnError() {
		if (optRadioButtons != null) {
			return (optRadioButtons.getSelectedValue() == STOP_ARG);
		}
		return false;
	}

}
