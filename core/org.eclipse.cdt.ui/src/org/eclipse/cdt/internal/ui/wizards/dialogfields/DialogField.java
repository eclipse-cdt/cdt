package org.eclipse.cdt.internal.ui.wizards.dialogfields;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.util.Assert;

import org.eclipse.cdt.internal.ui.wizards.swt.MGridData;

public class DialogField {

	private Label fLabel;
	protected String fLabelText;
	
	private IDialogFieldListener fDialogFieldListener;
	
	private boolean fEnabled;

	protected final void assertCompositeNotNull(Composite comp) {
		Assert.isNotNull(comp, "uncreated control requested with composite null"); //$NON-NLS-1$
	}
	protected final void assertEnoughColumns(int nColumns) {
		Assert.isTrue(nColumns >= getNumberOfControls(), "given number of columns is too small"); //$NON-NLS-1$
	}
	public static Control createEmptySpace(Composite parent, int span) {
		Label label= new Label(parent, SWT.LEFT);
		MGridData gd= new MGridData();
		gd.horizontalAlignment= MGridData.BEGINNING;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= span;
		gd.horizontalIndent= 0;
		gd.widthHint= 0;
		gd.heightHint= 0;
		label.setLayoutData(gd);
		return label;
	}
	public static Control createEmptySpace(Composite parent) {
		return createEmptySpace(parent, 1);
	}
	public void dialogFieldChanged() {
		if (fDialogFieldListener != null) {
			fDialogFieldListener.dialogFieldChanged(this);
		}
	}
	// ------- layout helpers
	
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);
		
		Label label= getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(nColumns));
		
		return new Control[] { label };
	}
	// ------- ui creation
		
	public Label getLabelControl(Composite parent) {
		if (fLabel == null) {
			assertCompositeNotNull(parent);
			
			fLabel= new Label(parent, SWT.LEFT);
			fLabel.setFont(parent.getFont());
			fLabel.setEnabled(fEnabled);		
			if (fLabelText != null && !"".equals(fLabelText)) { //$NON-NLS-1$
				fLabel.setText(fLabelText);
			} else {
				// XXX: to avoid a 16 pixel wide empty label - revisit
				fLabel.setText("."); //$NON-NLS-1$
				fLabel.setVisible(false);
			}			
		}
		return fLabel;
	}
	public int getNumberOfControls() {
		return 1;	
	}
	protected static MGridData gridDataForLabel(int span) {
		MGridData gd= new MGridData();
		gd.horizontalSpan= span;
		return gd;
	}
	public boolean isEnabled() {
		return fEnabled;
	}
	protected boolean isOkToUse(Control control) {
		return (control != null) && !(control.isDisposed());
	}
	public void postSetFocusOnDialogField(Display display) {
		if (display != null) {
			display.asyncExec(
				new Runnable() {
					public void run() {
						setFocus();
					}
				}
			);
		}
	}
	// ------ change listener
	
	public void setDialogFieldListener(IDialogFieldListener listener) {
		fDialogFieldListener= listener;
	}
	// --------- enable / disable management
	
	public void setEnabled(boolean enabled) {
		if (enabled != fEnabled) {
			fEnabled= enabled;
			updateEnableState();
		}
	}
	// ------- focus management
	
	public boolean setFocus() {
		return false;
	}
	public void setLabelText(String labeltext) {
		fLabelText= labeltext;
	}
	protected void updateEnableState() {
		if (fLabel != null) {
			fLabel.setEnabled(fEnabled);
		}
	}
	public DialogField() {
		fEnabled= true;
		fLabel= null;
		fLabelText= ""; //$NON-NLS-1$
	}
}
