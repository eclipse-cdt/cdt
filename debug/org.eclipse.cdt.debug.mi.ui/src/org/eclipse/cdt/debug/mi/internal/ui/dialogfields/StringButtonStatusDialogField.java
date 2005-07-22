/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog field containing a label, text control, status label and a button control.
 * The status label can be either a image or text label, and can be usd to give
 * additional information about the current element chosen.
 */
public class StringButtonStatusDialogField extends StringButtonDialogField {
		
	private Label fStatusLabelControl;
	private Object fStatus;  // String or ImageDescriptor
	
	private String fWidthHintString;
	private int fWidthHint;	
	
	public StringButtonStatusDialogField(IStringButtonAdapter adapter) {
		super(adapter);
		fStatus= null;
		fWidthHintString= null;
		fWidthHint= -1;
	}
	
	// ------ set status

	/**
	 * Sets the status string.
	 */	
	public void setStatus(String status) {
		if (isOkToUse(fStatusLabelControl)) {
			fStatusLabelControl.setText(status);
		}
		fStatus= status;		
	}
	
	/**
	 * Sets the status image.
	 * Caller is responsible to dispose image
	 */
	public void setStatus(Image image) {
		if (isOkToUse(fStatusLabelControl)) {
			if (image == null) {
				fStatusLabelControl.setImage(null);
			} else {
				fStatusLabelControl.setImage(image);
			}
		}
		fStatus= image;		
	}	

	/**
	 * Sets the staus string hint of the status label.
	 * The string is used to calculate the size of the status label.
	 */		
	public void setStatusWidthHint(String widthHintString) {
		fWidthHintString= widthHintString;
		fWidthHint= -1;
	}
	
	/**
	 * Sets the width hint of the status label.
	 */
	public void setStatusWidthHint(int widthHint) {
		fWidthHint= widthHint;
		fWidthHintString= null;
	}
	
	// ------- layout helpers	

	/*
	 * @see DialogField#doFillIntoGrid
	 */		
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);
		
		Label label= getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		Text text= getTextControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 3));
		Label status= getStatusLabelControl(parent);
		status.setLayoutData(gridDataForStatusLabel(parent, 1));
		Button button= getChangeControl(parent);
		button.setLayoutData(gridDataForButton(button, 1));
		
		return new Control[] { label, text, status, button };
	}
	
	/*
	 * @see DialogField#getNumberOfControls
	 */
	public int getNumberOfControls() {
		return 4;	
	}
	
	protected GridData gridDataForStatusLabel(Control aControl, int span) {
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalIndent= 0;
		if (fWidthHintString != null) {
			GC gc= new GC(aControl);
			gd.widthHint= gc.textExtent(fWidthHintString).x;
			gc.dispose();
		} else if (fWidthHint != -1) {
			gd.widthHint= fWidthHint;
		} else {
			gd.widthHint= SWT.DEFAULT;
		}		
		return gd;
	}
	
	// ------- ui creation	

	/**
	 * Creates or returns the created status label widget.
	 * @param parent The parent composite or <code>null</code> when the widget has
	 * already been created.
	 */			
	public Label getStatusLabelControl(Composite parent) {
		if (fStatusLabelControl == null) {
			assertCompositeNotNull(parent);			
			fStatusLabelControl= new Label(parent, SWT.LEFT);
			fStatusLabelControl.setFont(parent.getFont());
			fStatusLabelControl.setEnabled(isEnabled());
			if (fStatus instanceof Image) {
				fStatusLabelControl.setImage((Image)fStatus);
			} else if (fStatus instanceof String) {
				fStatusLabelControl.setText((String)fStatus);
			} else {
				// must be null
			}
		}
		return fStatusLabelControl;
	}
	
	// ------ enable / disable management

	/*
	 * @see DialogField#updateEnableState
	 */	
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fStatusLabelControl)) {
			fStatusLabelControl.setEnabled(isEnabled());
		}
	}	
}