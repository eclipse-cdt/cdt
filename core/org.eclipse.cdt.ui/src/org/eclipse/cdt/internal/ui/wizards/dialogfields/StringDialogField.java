package org.eclipse.cdt.internal.ui.wizards.dialogfields;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.internal.ui.wizards.swt.MGridData;


public class StringDialogField extends DialogField {
		
	private String fText;
	private Text fTextControl;
	private ModifyListener fModifyListener;
	
	// ------- layout helpers
		
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);
		
		Label label= getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		Text text= getTextControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 1));
		
		return new Control[] { label, text };
	}
	private void doModifyText(ModifyEvent e) {
		if (isOkToUse(fTextControl)) {
			fText= fTextControl.getText();
		}
		dialogFieldChanged();
	}
	public int getNumberOfControls() {
		return 2;	
	}
	// ------- ui creation			
		
	public Text getTextControl(Composite parent) {
		if (fTextControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener= new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					doModifyText(e);
				}
			};
			
			fTextControl= new Text(parent, SWT.SINGLE | SWT.BORDER);
			// moved up due to 1GEUNW2
			fTextControl.setText(fText);
			fTextControl.setFont(parent.getFont());
			fTextControl.addModifyListener(fModifyListener);
			
			
			fTextControl.setEnabled(isEnabled());
		}
		return fTextControl;
	}
	// ------ text access 
	
	/**
	 * Get the text
	 */	
	public String getText() {
		return fText;
	}
	protected static MGridData gridDataForText(int span) {
		MGridData gd= new MGridData();
		gd.horizontalAlignment= gd.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.grabColumn= 0;
		gd.horizontalSpan= span;
		return gd;
	}
	// ------- focus methods
	
	public boolean setFocus() {
		if (isOkToUse(fTextControl)) {
			fTextControl.setFocus();
			fTextControl.setSelection(0, fTextControl.getText().length());
		}
		return true;
	}
	/**
	 * Set the text. Triggers an dialog-changed event
	 */
	public void setText(String text) {
		fText= text;
		if (isOkToUse(fTextControl)) {
			fTextControl.setText(text);
		} else {
			dialogFieldChanged();
		}	
	}
	/**
	 * Set the text without triggering a dialog-changed event
	 */
	public void setTextWithoutUpdate(String text) {
		fText= text;
		if (isOkToUse(fTextControl)) {
			fTextControl.removeModifyListener(fModifyListener);
			fTextControl.setText(text);
			fTextControl.addModifyListener(fModifyListener);
		}
	}
	// ------ enable / disable management
	
	protected void updateEnableState() {
		super.updateEnableState();		
		if (isOkToUse(fTextControl)) {
			fTextControl.setEnabled(isEnabled());
		}	
	}
	public StringDialogField() {
		super();
		fText= ""; //$NON-NLS-1$
	}
}
