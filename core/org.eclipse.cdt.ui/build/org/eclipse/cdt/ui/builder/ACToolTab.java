/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Base tool tab implementation.
 */
public abstract class ACToolTab implements ICToolTab {

	/**
	 * The control for this page, or <code>null</code>
	 */
	private Control fControl;

	/**
	 * The configuration dialog this tab is contained in.
	 */
	private ICBuildConfigDialog fConfigurationDialog;
	
	/**
	 * Current error message, or <code>null</code>
	 */
	private String fErrorMessage;
	
	/**
	 * Current message, or <code>null</code>
	 */
	private String fMessage;
		
	/**
	 * Returns the dialog this tab is contained in, or
	 * <code>null</code> if not yet set.
	 * 
	 * @return configuration dialog, or <code>null</code>
	 */
	protected ICBuildConfigDialog getConfigurationDialog() {
		return fConfigurationDialog;
	}	
		
	/**
	 * Updates the buttons and message in this page's configuration dialog.
	 */
	protected void updateConfigurationDialog() {
		if (getConfigurationDialog() != null) {
			getConfigurationDialog().updateButtons();
			getConfigurationDialog().updateMessage();
		}
	}
				
	/**
	 * @see ICToolTab#getControl()
	 */
	public Control getControl() {
		return fControl;
	}

	/**
	 * Sets the control to be displayed in this tab.
	 * 
	 * @param control the control for this tab
	 */
	protected void setControl(Control control) {
		fControl = control;
	}

	/**
	 * @see ICToolTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		return fErrorMessage;
	}

	/**
	 * @see ICToolTab#getMessage()
	 */
	public String getMessage() {
		return fMessage;
	}

	/**
	 * @see ICToolTab#setConfigurationDialog(ICBuildConfigDialog)
	 */
	public void setConfigurationDialog(ICBuildConfigDialog dialog) {
		fConfigurationDialog = dialog;
	}
	
	/**
	 * Sets this page's error message, possibly <code>null</code>.
	 * 
	 * @param errorMessage the error message or <code>null</code>
	 */
	protected void setErrorMessage(String errorMessage) {
		fErrorMessage = errorMessage;
	}

	/**
	 * Sets this page's message, possibly <code>null</code>.
	 * 
	 * @param message the message or <code>null</code>
	 */
	protected void setMessage(String message) {
		fMessage = message;
	}
	
	/**
	 * By default, do nothing.
	 * 
	 * @see ICToolTab#dispose()
	 */
	public void dispose() {
	}
	
	/**
	 * Returns the shell this tab is contained in, or <code>null</code>.
	 * 
	 * @return the shell this tab is contained in, or <code>null</code>
	 */
	protected Shell getShell() {
		Control control = getControl();
		if (control != null) {
			return control.getShell();
		}
		return null;
	}
	
	/**
	 * Creates and returns a new push button with the given
	 * label and/or image.
	 * 
	 * @param parent parent control
	 * @param label button label or <code>null</code>
	 * @param image image of <code>null</code>
	 * 
	 * @return a new push button
	 */
	protected Button createPushButton(Composite parent, String label, Image image) {
		return SWTUtil.createPushButton(parent, label, image);	
	}
	
	/**
	 * Creates and returns a new radio button with the given
	 * label and/or image.
	 * 
	 * @param parent parent control
	 * @param label button label or <code>null</code>
	 * 
	 * @return a new radio button
	 */
	protected Button createRadioButton(Composite parent, String label) {
		return SWTUtil.createRadioButton(parent, label);	
	}	
	
	/**
	 * @see ICToolTab#canSave()
	 */
	public boolean canSave() {
		return true;
	}
	
	/**
	 * Create some empty space.
	 */
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
	}	

	/**
	 * @see ICToolTab#getImage()
	 */
	public Image getImage() {
		return null;
	}

}
