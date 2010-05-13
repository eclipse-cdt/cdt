/*******************************************************************************
 * Copyright (c) 2005, 2010 Freescale Semiconductor, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.memory;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog CDT puts up when adding a memory monitor to the memory view for a 
 * debug target that supports memory spaces. 
 * <p>
 * It differs from the platform one in that you can enter an expression or
 * an address + memory space pair.
 *  
 * @since 3.2
 */
public class AddMemoryBlockDialog extends TrayDialog implements ModifyListener, SelectionListener {

	private Combo fAddressInput;
	private Button fAddressRadio;
	private Combo fMemorySpaceInput;
	private Combo fExpressionInput;
	private String fExpression;
	private Button fExpressionRadio;
	private String fAddress;
	private String fMemorySpace;
	private boolean fEnteredExpression;	// basically, which of the two radio buttons was selected when OK was hit
	
	/** The memory spaces to expose. Given to use at instantiation time. */
	final private String[] fMemorySpaces;

	/**
	 * For improved usability, we persist the memory space selection from one
	 * invocation of the dialog to another, but we need not worry about
	 * persisting it from one instantiation of Eclipse to the next
	 */
	private static String fPreviousMemorySpaceSelection;

	private static List<String> sAddressHistory = new ArrayList<String>();
	private static List<String> sExpressionHistory = new ArrayList<String>();
	
	private static boolean sDefaultToExpression = true;

	public AddMemoryBlockDialog(Shell parentShell, String[] memorySpaces) {
		super(parentShell);

		setShellStyle(getShellStyle() | SWT.RESIZE);
		fMemorySpaces = memorySpaces;

		// We shouldn't be using this custom dialog if there are none or only
		// one memory spaces available.
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=309032#c50
		assert memorySpaces != null && memorySpaces.length >= 2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
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

		fExpressionRadio = new Button(parent, SWT.RADIO);
		final int radioButtonWidth = fExpressionRadio.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		fExpressionRadio.setText(Messages.AddMemBlockDlg_enterExpression);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		fExpressionRadio.setLayoutData(gridData);
		fExpressionRadio.addSelectionListener(this);
		
		fExpressionInput = new Combo(parent, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = radioButtonWidth;
		fExpressionInput.setLayoutData(gridData);

		fAddressRadio = new Button(parent, SWT.RADIO);
		fAddressRadio.setText(Messages.AddMemBlockDlg_enterAddrAndMemSpace);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		fAddressRadio.setLayoutData(gridData);
		fAddressRadio.addSelectionListener(this);

		fMemorySpaceInput = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalIndent = radioButtonWidth; 
		fMemorySpaceInput.setLayoutData(gridData);
		fMemorySpaceInput.addSelectionListener(this);

		fMemorySpaceInput.setItems(fMemorySpaces);
		
		// Try to persist the mem space selection from one invocation of the
		// dialog to the next
		String memSpaceSelection = null; 
		if (fPreviousMemorySpaceSelection != null) {
			String[] items = fMemorySpaceInput.getItems();
			for (String item : items) {
				if (item.equals(fPreviousMemorySpaceSelection)) {
					memSpaceSelection = fPreviousMemorySpaceSelection;
				}
			}
		}
		if (memSpaceSelection != null) {
			fMemorySpaceInput.setText(memSpaceSelection);
		}
		else {
			fMemorySpaceInput.select(0); // the n/a entry
		}

		fAddressInput = new Combo(parent, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		GC gc = new GC(fAddressInput);
		FontMetrics fm = gc.getFontMetrics();
		// Give enough room for a 64 bit hex address (25 is a guess at the combobox selector) 
		gridData.minimumWidth = gridData.minimumWidth = 18 * fm.getAverageCharWidth() + 25;
		gc.dispose();
		fAddressInput.setLayoutData(gridData);
		fAddressInput.addModifyListener(this);
		fAddressInput.addVerifyListener(new VerifyListener() {
			// limit entry to hex or decimal 
			public void verifyText(VerifyEvent e) {
				e.doit = false;
				final char c = e.character; 
				if (Character.isDigit(c) || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') ||
					c == 'x' ||	Character.isISOControl(e.character)) {
					e.doit = true;
				}
			}
		});

		// add the history into the combo boxes 
		String[] history = getHistory(sExpressionHistory);
		for (int i = 0; i < history.length; i++)
			fExpressionInput.add(history[i]);

		history = getHistory(sAddressHistory);
		for (int i = 0; i < history.length; i++)
			fAddressInput.add(history[i]);

		fExpressionInput.addModifyListener(this);

		if (sDefaultToExpression) {
			fExpressionRadio.setSelection(true);
			fAddressRadio.setSelection(false);
			fExpressionInput.setFocus();
		}
		else {
			fAddressRadio.setSelection(false);
			fAddressRadio.setSelection(true);
			fAddressInput.setFocus();
		}
			
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		// use the same title used by the platform dialog
		newShell.setText(Messages.AddMemBlockDlg_MonitorMemory);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		fExpression = fExpressionInput.getText();
		fAddress = fAddressInput.getText();
		fMemorySpace = fMemorySpaceInput.getText();
		
		// add to HISTORY list; add to the platform dialog's for the expression
		if (fExpression.length() > 0)
			addHistory(sExpressionHistory, fExpression);
		if (fAddress.length() > 0)
			addHistory(sAddressHistory, fAddress);

		fEnteredExpression = fExpressionRadio.getSelection();
		
		fPreviousMemorySpaceSelection = fMemorySpace;
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		// if user enters text into either the address field or the expression one, automatically
		// select its associated radio button (and deselect the other, these are mutually exclusive) 
		if (e.widget == fAddressInput ||
			e.widget == fExpressionInput) {

			fAddressRadio.setSelection(e.widget != fExpressionInput);
			fExpressionRadio.setSelection(e.widget == fExpressionInput);
			sDefaultToExpression = (e.widget == fExpressionInput);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
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

	public boolean enteredExpression() {
		return fEnteredExpression;
	}
	
	private static void addHistory(List<String> list, String item)	{		
		if (!list.contains(item))
			list.add(0, item);

		if (list.size() > 5)
			list.remove(list.size()-1);
	}
	
	private static String[] getHistory(List<String> list)	{
		return list.toArray(new String[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		// if user selects a memory space, select its associated radio button (and deselect the 
		// other, these are mutually exclusive) 
		if (e.widget == fExpressionRadio) {
			fExpressionRadio.setSelection(true);
			fAddressRadio.setSelection(false);
			fExpressionInput.setFocus();
		}
		else {
			fExpressionRadio.setSelection(false);
			fAddressRadio.setSelection(true);
			fAddressInput.setFocus();
		}
		
		sDefaultToExpression = (e.widget == fExpressionInput);
	}
}
