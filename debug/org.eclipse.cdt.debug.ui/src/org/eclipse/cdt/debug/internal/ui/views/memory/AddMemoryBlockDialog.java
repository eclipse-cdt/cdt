/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation 
 *     							 
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.memory;

import java.util.ArrayList;

import org.eclipse.cdt.debug.internal.core.CMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog CDT puts up when adding a memory monitor to the memory view for a 
 * debug target that supports memory spaces. 
 * <p>
 * It differs from the platform one in that you can enter an expression or
 * an memory space + address pair.
 *  
 * @since 3.2
 */
public class AddMemoryBlockDialog extends TrayDialog implements ModifyListener, FocusListener, VerifyListener {

	private Combo fAddressInput;
	private Combo fMemorySpaceInput;
	private Combo fExpressionInput;
	private String fExpression;
	private String fAddress;
	private String fMemorySpace;
	private boolean fExpressionEntered;
	private CMemoryBlockRetrievalExtension fMemRetrieval;
	private Button fEnterAddr;
	private Button fEnterExpression;
	
	private static boolean sfExpressionSetLast = false; // used to persist the default entry-type selection  
	
	private static ArrayList sAddressHistory = new ArrayList();
	private static ArrayList sExpressionHistory = new ArrayList();

	public AddMemoryBlockDialog(Shell parentShell,
			IMemoryBlockRetrieval memRetrieval) {
		super(parentShell);

		setShellStyle(getShellStyle() | SWT.RESIZE);

		if (memRetrieval instanceof CMemoryBlockRetrievalExtension) {
			fMemRetrieval = (CMemoryBlockRetrievalExtension)memRetrieval;  
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				parent,
				IDebugUIConstants.PLUGIN_ID
				+ ".MonitorMemoryBlockDialog_context"); //$NON-NLS-1$

		// The button bar will work better if we make the parent composite
		// a single column grid layout. For the widgets we add, we want a 
		// a two-column grid, so we just create a sub composite for that.
		GridLayout gridLayout = new GridLayout();
		parent.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gridData);
		Composite composite = new Composite(parent, SWT.None);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);
		parent = composite;  // for all our widgets, the two-column composite is the real parent

		fEnterAddr = new Button(parent, SWT.RADIO);
		
		// take this opportunity to get the width of just the radion buton 
		// (w/no text), as we'll need it below 
		int buttonWidth = fEnterAddr.computeSize(SWT.DEFAULT, SWT.DEFAULT,  false).x;
		
		fEnterAddr.setText(Messages.AddMemBlockDlg_enterMemSpaceAndAddr);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		fEnterAddr.setLayoutData(gridData);

		fMemorySpaceInput = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalIndent = buttonWidth;
		fMemorySpaceInput.setLayoutData(gridData);
		fMemorySpaceInput.addFocusListener(this);

		// Populate the memory space combobox with the available spaces
		if (fMemRetrieval != null) {
			String [] memorySpaces = fMemRetrieval.getMemorySpaces();
			for (int i = 0; i < memorySpaces.length; i++)
				fMemorySpaceInput.add(memorySpaces[i]);

			if (memorySpaces.length > 0)
				fMemorySpaceInput.select(0);
		}

		fAddressInput = new Combo(parent, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fAddressInput.setLayoutData(gridData);
		fAddressInput.addModifyListener(this);
		fAddressInput.addFocusListener(this);
		fAddressInput.addVerifyListener(this);
		
		fEnterExpression = new Button(parent, SWT.RADIO);
		fEnterExpression.setText(Messages.AddMemBlockDlg_enterExpression);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		fEnterExpression.setLayoutData(gridData);
		
		fExpressionInput = new Combo(parent, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = buttonWidth;
		fExpressionInput.setLayoutData(gridData);
		fExpressionInput.addModifyListener(this);
		fExpressionInput.addFocusListener(this);
		
		// add the history into the combo boxes 
		String[] history = getHistory(sExpressionHistory);
		for (int i = 0; i < history.length; i++)
			fExpressionInput.add(history[i]);

		history = getHistory(sAddressHistory);
		for (int i = 0; i < history.length; i++)
			fAddressInput.add(history[i]);

		fExpressionInput.addFocusListener(this);

		fEnterExpression.setSelection(sfExpressionSetLast);
		fEnterAddr.setSelection(!sfExpressionSetLast);
		if (sfExpressionSetLast) {
			fExpressionInput.forceFocus();
		} else {
			fAddressInput.forceFocus();
		}
		
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		// use the same title used by the platform dialog
		newShell.setText(Messages.AddMemBlockDlg_MonitorMemory);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		fExpressionEntered = fEnterExpression.getSelection();
		fExpression = fExpressionInput.getText();
		fAddress = fAddressInput.getText();
		fMemorySpace = fMemorySpaceInput.getText();

		// add to HISTORY list; add to the platform dialog's for the expression
		if (fExpression.length() > 0)
			addHistory(sExpressionHistory, fExpression);
		if (fAddress.length() > 0)
			addHistory(sAddressHistory, fAddress);

		// this will persist the entry type from one dialog invocation to another
		sfExpressionSetLast = fExpressionEntered;
		
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		// if user enters text into one of the two input options, disable/gray the other
		// to make the mutual exlusivity obvious
		fAddressInput.setEnabled(fExpressionInput.getText().length() == 0);
		fEnterAddr.setEnabled(fExpressionInput.getText().length() == 0);
		fMemorySpaceInput.setEnabled(fExpressionInput.getText().length() == 0);
		fExpressionInput.setEnabled(fAddressInput.getText().length() == 0);
		fEnterExpression.setEnabled(fAddressInput.getText().length() == 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		return super.createButtonBar(parent);
	}

	public String getExpression() {
		return fExpression;
	}

	public String getAddress() {
		return fAddress;
	}

	public String getMemorySpace() {
		return fMemorySpace;
	}

	public boolean wasExpressionEntered() {
		return fExpressionEntered;
	}
	
	private static void addHistory(ArrayList list, String item)	{		
		if (!list.contains(item))
			list.add(0, item);

		if (list.size() > 5)
			list.remove(list.size()-1);
	}
	
	private static String[] getHistory(ArrayList list)	{
		return (String[])list.toArray(new String[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		// when the user gives focus to one of the combo boxes, shift the radio button state accordingly
		if (e.widget == fAddressInput || e.widget == fMemorySpaceInput) {
			fEnterAddr.setSelection(true);
			fEnterExpression.setSelection(false);			
		}
		else if (e.widget == fExpressionInput) {
			fEnterAddr.setSelection(false);
			fEnterExpression.setSelection(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	public void verifyText(VerifyEvent event) {
		if (event.widget == fAddressInput) {
			// assume we won't allow it
			event.doit = false;

			char c = event.character;

			String textAlreadyInControl = ((Combo)event.widget).getText();
			
			if ((c == 'x') && (textAlreadyInControl.length() == 1)
					&& textAlreadyInControl.charAt(0) == '0') {
				// allow 'x' if it's the second character and the first is zero.
				// Note that this level of verification has a hole; user can use 
				// ISO control characters (e.g., the Delete key) to move the 'x' 
				// in the first slot. Oh well; this doesn't need to be bullet proof
				event.doit = true;
			} else if ((c == '\b') || // allow backspace
					Character.isDigit(c) || ('a' <= c && c <= 'f')
					|| ('A' <= c && c <= 'F')) {
				event.doit = true;
			} else if (Character.isISOControl(c)) {
				event.doit = true;
			}
		}
	}
}
