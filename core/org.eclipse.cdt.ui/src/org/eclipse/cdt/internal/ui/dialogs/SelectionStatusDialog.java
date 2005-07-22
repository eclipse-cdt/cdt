/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.dialogs.SelectionDialog;

import org.eclipse.core.runtime.IStatus;

/**
 * An abstract base class for dialogs with a status bar and ok/cancel buttons.
 * The status message must be passed over as StatusInfo object and can be
 * an error, warning or ok. The OK button is enabled / disabled depending
 * on the status.
 */ 
public abstract class SelectionStatusDialog extends SelectionDialog {
	
	private MessageLine fStatusLine;
	private IStatus fLastStatus;
	private Image fImage;
	private boolean fInitialSelectionSet;
	private boolean fStatusLineAboveButtons= false;
	
	
	/**
	 * Compute the result and return it.
	 */
	protected abstract void computeResult();
	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (fImage != null)
			shell.setImage(fImage);
	}
	/* (non-Javadoc)
	 * Method declared in Dialog.
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		if (fStatusLineAboveButtons) {
			layout.verticalSpacing= 0;
		} else {
			layout.numColumns= 2;
		}
		layout.marginHeight= 0; layout.marginWidth= 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fStatusLine= new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		fStatusLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fStatusLine.setMessage(""); //$NON-NLS-1$

		super.createButtonBar(composite);
		return composite;
	}
	/* (non-Javadoc)
	 * Method declared in Dialog.
	 */
	public void create() {
		super.create();
		if (fLastStatus != null) {
			updateStatus(fLastStatus);
		}
	}
	/**
	 * Returns the first element of the initial selection or <code>null<code>
	 * if there isn't any initial selection.
	 * @return the first element of the initial selection.
	 */
	protected Object getPrimaryInitialSelection() {
		List result= getInitialElementSelections();
		if (result == null || result.size() == 0)
			return null;
		return result.get(0);	
	}
	/**
	 * Returns the first element from the list of results. Returns <code>null</code>
	 * if no element has been selected.
	 *
	 * @return the first result element if one exists. Otherwise <code>null</code> is
	 *  returned.
	 */
	public Object getPrimaryResult() {
		Object[] result= getResult();
		if (result == null || result.length == 0)
			return null;
		return result[0];	
	}
	public SelectionStatusDialog(Shell parent) {
		super(parent);
		fInitialSelectionSet= false;
	}
	/* (non-Javadoc)
	 * Method declared in Dialog.
	 */
	protected void okPressed() {
		computeResult();
		super.okPressed();
	}
	/**
	 * Sets the image for this dialog.
	 *
	 * @param image the dialog's image
	 */
	public void setImage(Image image) {
		fImage= image;
	}
	protected void setInitialSelection(int position, Object element) {
		List l= getInitialElementSelections();
		l.set(position, element);
		fInitialSelectionSet= true;
	}
	/**
	 * Sets the initial selection to the given element.
	 */
	public void setInitialSelection(Object element) {
		// Allow clients to use set their own initial selection(s)
		if (fInitialSelectionSet && element != null && element.equals("A")) //$NON-NLS-1$
			return;

		if (element != null) {
			setInitialSelections(new Object[] { element });
		} else {
			setInitialSelections(new Object[0]);
		}
	}
	public void setInitialSelections(Object[] selectedElements) {
		super.setInitialSelections(selectedElements);
		fInitialSelectionSet= true;
	}
	/**
	 * Sets a result element at the given position.
	 */
	protected void setResult(int position, Object element) {
		Object[] result= getResult();
		result[position]= element;
		setResult(Arrays.asList(result));
	}
	/**
	 * Controls whether status line appears to the left of the buttons (default)
	 * or above them.
	 *
	 * @param aboveButtons if <code>true</code> status line is placed above buttons; if
	 * 	<code>false</code> to the right
	 */
	public void setStatusLineAboveButtons(boolean aboveButtons) {
		fStatusLineAboveButtons= aboveButtons;
	}
	/**
	 * Update the status of the ok button to reflect the given status. Subclasses
	 * may override this method to update additional buttons.
	 */
	protected void updateButtonsEnableState(IStatus status) {
		Button okButton= getOkButton();
		if (okButton != null && !okButton.isDisposed())
			okButton.setEnabled(!status.matches(IStatus.ERROR));
	}
	/**
	 * Update the dialog's status line to reflect the given status. It is safe to call
	 * this method before the dialog has been opened.
	 */
	protected void updateStatus(IStatus status) {
		fLastStatus= status;
		if (fStatusLine != null && !fStatusLine.isDisposed()) {
		    updateButtonsEnableState(status);
		    StatusTool.applyToStatusLine(fStatusLine, status);			
		}
	}
}
