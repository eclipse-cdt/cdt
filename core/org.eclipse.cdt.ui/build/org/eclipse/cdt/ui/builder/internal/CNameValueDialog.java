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

package org.eclipse.cdt.ui.builder.internal;

import org.eclipse.cdt.core.builder.util.CUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author sam.robb
 *
 * Simple dialog for creating/editing name-value pairs.
 */
public class CNameValueDialog extends Dialog {

	/**
	 * Dialog title.
	 */
	String fTitle = "Variable";

	/**
	 * Description of property name (ex, "Definition")
	 */
	String fNameLabel = "Name";

	/**
	 * Description of property value (ex, "Value")
	 */
	String fValueLabel = "Value";

	/**
	 * Property name.
	 */
	String fName = "";
	
	/**
	 * Property value.
	 */
	String fValue = "";
	
	/** 
	 * Internal list to keep track of existing macro names.
	 */
	private List reservedNames;

	private Button btnOK = null;
	private Button btnCancel = null;
	private Text textName = null;
	private Text textValue = null;

	/**
	 * Constructor for CEntryDialog.
	 * 
	 * @param parent
	 */
	public CNameValueDialog(Shell parent) {
		super(parent);
		reservedNames =  new List (parent, parent.getStyle());
	}
	
	/**
	 * Second Constructor 
	 * @param Shell parent, List list
	 */
	public CNameValueDialog (Shell parent, List list) {
		super (parent);
		reservedNames = list;
	}

	/**
	 * Set the title for the dialog.
	 * 
	 * @param title Title to use for the dialog.
	 */ 
	public void setTitle(String title) {
		fTitle = title;
	}

	/**
	 * Set the label for the "Name" edit field in the dialog
	 * 
	 * @param nameLabel Label to use for the "Name" edit field.
	 */ 
	public void setNameLabel(String nameLabel) {
		fNameLabel = nameLabel;
	}
	
	/**
	 * Set the label for the "Value" edit field in the dialog
	 * 
	 * @param valueLabel Label to use for the "Value" edit field.
	 */ 
	public void setValueLabel(String valueLabel) {
		fValueLabel = valueLabel;
	}

	/**
	 * Set the inital contents of the "Name" edit field in the dialog
	 * 
	 * @param name Initial value for the "Name" edit field.
	 */ 
	public void setName(String name) {
		fName = name;
	}
	
	/**
	 * Set the inital contents of the "Value" edit field in the dialog
	 * 
	 * @param value Initial value for the "Value" edit field.
	 */ 
	public void setValue(String value) {
		fValue = value;
	}

	/**
	 * Returns the contents of the "Name" edit field in the dialog.
	 * 
	 * @return Property name.
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Returns the contents of the "Value" edit field in the dialog.
	 * 
	 * @return Property value.
	 */
	public String getValue() {
		return fValue;
	}

	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		updateButtonsState();
		return result;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(fTitle);
	}

	protected Control createDialogArea(Composite parent) {
		Composite 	composite 	= new Composite(parent, SWT.NONE);
		GridLayout 	layout 		= new GridLayout(2, false);

		layout.marginWidth	= 5;
		layout.numColumns	= 2;
		
		composite.setLayout(layout);
		
		GC gc = new GC(composite);
		gc.setFont(composite.getFont());
		FontMetrics metrics = gc.getFontMetrics();
		gc.dispose();
		
		int 		fieldWidthHint 	= convertWidthInCharsToPixels(metrics, 50);
		GridData 	gd				= null;
		Label 		label			= null;

		label = new Label(composite, SWT.NONE);
		label.setText(fNameLabel + ":");
		
		textName 	= new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd 			= new GridData(GridData.FILL_BOTH);
		
		gd.grabExcessHorizontalSpace 	= true;
		gd.widthHint 					= fieldWidthHint;
		
		textName.setLayoutData(gd);

		label = new Label(composite, SWT.NONE);
		label.setText(fValueLabel + ":");
		
		textValue 	= new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd 			= new GridData(GridData.FILL_BOTH);
		
		gd.grabExcessHorizontalSpace 	= true;
		gd.widthHint 					= fieldWidthHint;
		
		textValue.setLayoutData(gd);
		
		textName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtonsState();
			}
		});
		
		textValue.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtonsState();
			}
		});
		
		textName.setText(fName);
		textValue.setText(fValue);

		return composite;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		btnOK 		= createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		btnCancel 	= createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	private void updateButtonsState() {
		if (btnOK != null) {
			btnOK.setEnabled(textName.getText().trim().length() > 0);
		}
	}

	protected void okPressed() {
		fName 	= textName.getText().trim();
		fValue 	= textValue.getText().trim();
		textValue.setText(fValue);
		
		// Let's first check if this name already exists or not.
		if (reservedNames.getItemCount() > 0) {
			boolean exists = checkExistance();
			if (exists) {
				return;
			}
		}
		
		// Validate the user input here.
		boolean isValid = CUtil.isValidCIdentifier(fName);
		if (!isValid) {
			String errorMsg = fName + " is not a valid identifier name.";
			MessageDialog.openError(this.getShell(), "Problem with Identifier name", errorMsg);				
			return;
		}
		
		setReturnCode(OK);
		close();
	}

	
	
	private boolean checkExistance () {
		String[] existingItems = reservedNames.getItems();
		for (int i = 0; i < existingItems.length; i++) {
			if (existingItems[i].toString().equals(fName)) {
				String errorMsg = "This Identifier already exists in the Preprocessor definitions for this project";
				MessageDialog.openError(this.getShell(), "Naming problems", errorMsg);				
				return true;
			}
		}	
		return false;	
	} 
}
