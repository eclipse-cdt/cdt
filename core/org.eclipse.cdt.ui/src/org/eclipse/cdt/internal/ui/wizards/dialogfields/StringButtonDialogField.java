package org.eclipse.cdt.internal.ui.wizards.dialogfields;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.internal.ui.wizards.swt.MGridData;

public class StringButtonDialogField extends StringDialogField {
		
	private Button fBrowseButton;
	private String fBrowseButtonLabel;
	private IStringButtonAdapter fStringButtonAdapter;
	
	private boolean fButtonEnabled;
	
	// ------ adapter communication
	
	public void changeControlPressed() {
		fStringButtonAdapter.changeControlPressed(this);
	}
	// ------- layout helpers
		
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);
		
		Label label= getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		Text text= getTextControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 2));
		Control button= getChangeControl(parent);
		button.setLayoutData(gridDataForControl(1));
		
		return new Control[] { label, text, button };
	}
	// ------ enable / disable management
	
	public void enableButton(boolean enable) {
		if (isOkToUse(fBrowseButton)) {
			fBrowseButton.setEnabled(isEnabled() && enable);
		}
		fButtonEnabled= enable;
	}
	// ------- ui creation	
	
	public Control getChangeControl(Composite parent) {
		if (fBrowseButton == null) {
			assertCompositeNotNull(parent);
			
			fBrowseButton= new Button(parent, SWT.PUSH);
			fBrowseButton.setText(fBrowseButtonLabel);
			fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
			fBrowseButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					changeControlPressed();
				}
				public void widgetSelected(SelectionEvent e) {
					changeControlPressed();
				}
			});	
			
		}
		return fBrowseButton;
	}
	public int getNumberOfControls() {
		return 3;	
	}
	protected static MGridData gridDataForControl(int span) {
		MGridData gd= new MGridData();
		gd.horizontalAlignment= MGridData.FILL;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= span;
		return gd;
	}
	public StringButtonDialogField(IStringButtonAdapter adapter) {
		super();
		fStringButtonAdapter= adapter;
		fBrowseButtonLabel= "!Browse...!"; //$NON-NLS-1$
		fButtonEnabled= true;
	}
	public void setButtonLabel(String label) {
		fBrowseButtonLabel= label;
	}
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fBrowseButton)) {
			fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
		}
	}
}
