/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyHelpContextIds;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * DisassemblyPreferencePage
 */
public class DisassemblyPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private List<Button> fCheckBoxes = new ArrayList<Button>();
	private List<Combo> fComboBoxes = new ArrayList<Combo>();
	private ArrayList<Text> fNumberFields = new ArrayList<Text>();
	private ModifyListener fNumberFieldListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			numberFieldChanged((Text)e.widget);
		}
	};
	private Combo fAddressFormatCombo;
	private final static String[] fcRadixItems = {
		DisassemblyMessages.DisassemblyPreferencePage_radix_octal,
		DisassemblyMessages.DisassemblyPreferencePage_radix_decimal,
		DisassemblyMessages.DisassemblyPreferencePage_radix_hexadecimal,
	};
	private final static int[] fcRadixValues = {
		8, 10, 16
	};

	/**
	 * Create the Disassembly preference page.
	 */
	public DisassemblyPreferencePage() {
		super();
		setPreferenceStore(DsfUIPlugin.getDefault().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDisassemblyHelpContextIds.DISASSEMBLY_PREFERENCE_PAGE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		String label;

//		label = DisassemblyMessages.DisassemblyPreferencePage_startAddress; //$NON-NLS-1$
//		addTextField(composite, label, DisassemblyPreferenceConstants.START_ADDRESS, 20, 0, true);
//		label = DisassemblyMessages.DisassemblyPreferencePage_endAddress; //$NON-NLS-1$
//		addTextField(composite, label, DisassemblyPreferenceConstants.END_ADDRESS, 20, 0, true);

		label = DisassemblyMessages.DisassemblyPreferencePage_showAddress;
		final Button showAddressCB = addCheckBox(composite, label, DisassemblyPreferenceConstants.SHOW_ADDRESS_RULER, 0);
		showAddressCB.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fAddressFormatCombo.setEnabled(showAddressCB.getSelection());
			}
			});
		showAddressCB.setToolTipText(DisassemblyMessages.DisassemblyPreferencePage_showAddressTooltip);
		label = DisassemblyMessages.DisassemblyPreferencePage_addressRadix;
		fAddressFormatCombo = addComboBox(composite, label, DisassemblyPreferenceConstants.ADDRESS_RADIX, fcRadixItems);
		fAddressFormatCombo.setToolTipText(DisassemblyMessages.DisassemblyPreferencePage_addressFormatTooltip);
//		label = DisassemblyMessages.DisassemblyPreferencePage_instructionRadix;
//		addComboBox(composite, label, DisassemblyPreferenceConstants.INSTRUCTION_RADIX, fcRadixItems);

		label = DisassemblyMessages.DisassemblyPreferencePage_showSource;
		Button showSourceCB = addCheckBox(composite, label, DisassemblyPreferenceConstants.SHOW_SOURCE, 0);
		showSourceCB.setToolTipText(DisassemblyMessages.DisassemblyPreferencePage_showSourceTooltip);
		label = DisassemblyMessages.DisassemblyPreferencePage_showSymbols;
		Button showSymbolsCB = addCheckBox(composite, label, DisassemblyPreferenceConstants.SHOW_SYMBOLS, 0);
		showSymbolsCB.setToolTipText(DisassemblyMessages.DisassemblyPreferencePage_showSymbolsTooltip);
//		label = DisassemblyMessages.DisassemblyPreferencePage_simplifiedMnemonics;
//		addCheckBox(composite, label, DisassemblyPreferenceConstants.SIMPLIFIED, 0);
		label = DisassemblyMessages.DisassemblyPreferencePage_showAddressRadix;
		Button showRadixCB = addCheckBox(composite, label, DisassemblyPreferenceConstants.SHOW_ADDRESS_RADIX, 0);
		showRadixCB.setToolTipText(DisassemblyMessages.DisassemblyPreferencePage_showRadixTooltip);
		label = DisassemblyMessages.DisassemblyPreferencePage_showFunctionOffsets;
		Button showFunctionOffsets = addCheckBox(composite, label, DisassemblyPreferenceConstants.SHOW_FUNCTION_OFFSETS, 0);
		showFunctionOffsets.setToolTipText(DisassemblyMessages.DisassemblyPreferencePage_showFunctionOffsetsTooltip);
//		label = DisassemblyMessages.DisassemblyPreferencePage_avoidReadBeforePC;
//		addCheckBox(composite, label, DisassemblyPreferenceConstants.AVOID_READ_BEFORE_PC, 0);

		// horizontal line
//		Label separator = new Label(composite, SWT.SEPARATOR|SWT.HORIZONTAL);
//		GridData data;
//		data = new GridData(GridData.FILL_HORIZONTAL);
//		data.horizontalSpan = layout.numColumns;
//		separator.setLayoutData(data);
//
//		label = DisassemblyMessages.DisassemblyPreferencePage_useSourceOnlyMode;
//		addCheckBox(composite, label, DisassemblyPreferenceConstants.USE_SOURCE_ONLY_MODE, 0);
//
//		// note
//		String noteTitle = DisassemblyMessages.DisassemblyPreferencePage_useSourceOnlyMode_noteTtitle;
//		String noteMessage = DisassemblyMessages.DisassemblyPreferencePage_useSourceOnlyMode_noteMessage;
//		Composite note = createNoteComposite(composite.getFont(), composite, noteTitle, noteMessage);
//		data = (GridData)note.getLayoutData();
//		if (data == null) {
//			data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//			note.setLayoutData(data);
//		}
//		data.horizontalSpan = layout.numColumns;
//		Control msgControl = note.getChildren()[1];
//		data = new GridData(GridData.FILL_HORIZONTAL);
//		data.widthHint = convertWidthInCharsToPixels(65);
//		data.heightHint = convertHeightInCharsToPixels(3);
//		msgControl.setLayoutData(data);
		
		Dialog.applyDialogFont(parent);

		initialize();

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	private Button addCheckBox(Composite parent, String label, String key, int indentation) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = 3;
		checkBox.setLayoutData(gd);
		checkBox.setData(key);
		fCheckBoxes.add(checkBox);

		return checkBox;
	}

	private Combo addComboBox(Composite parent, String label, String key, String[] items) {
		Label labelControl= new Label(parent, SWT.NONE);
		labelControl.setText(""); //$NON-NLS-1$
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		labelControl.setLayoutData(gd);

		labelControl= new Label(parent, SWT.NONE);
		labelControl.setText(label);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		labelControl.setLayoutData(gd);
		
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		combo.setLayoutData(gd);
		combo.setItems(items);
		combo.setData(key);
		fComboBoxes.add(combo);

		return combo;
	}

	protected Text addTextField(Composite composite, String label, String key, int textLimit, int indentation, boolean isNumber) {
		return getTextControl(addLabelledTextField(composite, label, key, textLimit, indentation, isNumber));
	}
	
//	private static Label getLabelControl(Control[] labelledTextField){
//		return (Label)labelledTextField[0];
//	}

	private static Text getTextControl(Control[] labelledTextField){
		return (Text)labelledTextField[1];
	}

	/**
	 * Returns an array of size 2:
	 *  - first element is of type <code>Label</code>
	 *  - second element is of type <code>Text</code>
	 * Use <code>getLabelControl</code> and <code>getTextControl</code> to get the 2 controls.
	 */
	private Control[] addLabelledTextField(Composite composite, String label, String key, int textLimit, int indentation, boolean isNumber) {
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);
		
		Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		textControl.setData(key);
		if (isNumber) {
			fNumberFields.add(textControl);
			textControl.addModifyListener(fNumberFieldListener);
		}
			
		return new Control[]{labelControl, textControl};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		for (Iterator<Button> iter = fCheckBoxes.iterator(); iter.hasNext();) {
			Button btn = iter.next();
			store.setValue((String)btn.getData(), btn.getSelection());
		}
		for (Iterator<Text> iter = fNumberFields.iterator(); iter.hasNext();) {
			Text field = iter.next();
			store.setValue((String)field.getData(), Long.decode(field.getText()).longValue());
		}
		for (Iterator<Combo> iter = fComboBoxes.iterator(); iter.hasNext();) {
			Combo combo = iter.next();
			store.setValue((String)combo.getData(), fcRadixValues[combo.getSelectionIndex()]);
		}
		return super.performOk();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		for (Iterator<Button> iter = fCheckBoxes.iterator(); iter.hasNext();) {
			Button btn = iter.next();
			btn.setSelection(store.getDefaultBoolean((String)btn.getData()));
		}
		for (Iterator<Text> iter = fNumberFields.iterator(); iter.hasNext();) {
			Text field = iter.next();
			long value = store.getDefaultLong((String)field.getData());
			field.setText("0x"+Long.toHexString(value)); //$NON-NLS-1$
		}
		for (Iterator<Combo> iter = fComboBoxes.iterator(); iter.hasNext();) {
			Combo combo = iter.next();
			int value = store.getDefaultInt((String)combo.getData());
			for (int i = 0; i < fcRadixValues.length; i++) {
				if (fcRadixValues[i] == value) {
					combo.select(i);
				}
			}
		}
		super.performDefaults();
	}
	/**
	 * Initialize widget values from preference store.
	 */
	private void initialize() {
		IPreferenceStore store = getPreferenceStore();
		for (Iterator<Button> iter = fCheckBoxes.iterator(); iter.hasNext();) {
			Button btn = iter.next();
			btn.setSelection(store.getBoolean((String)btn.getData()));
		}
		for (Iterator<Text> iter = fNumberFields.iterator(); iter.hasNext();) {
			Text field = iter.next();
			long value = store.getLong((String)field.getData());
			field.setText("0x"+Long.toHexString(value)); //$NON-NLS-1$
		}
		for (Iterator<Combo> iter = fComboBoxes.iterator(); iter.hasNext();) {
			Combo combo = iter.next();
			int value = store.getInt((String)combo.getData());
			for (int i = 0; i < fcRadixValues.length; i++) {
				if (fcRadixValues[i] == value) {
					combo.select(i);
					break;
				}
			}
		}
	}

	/**
	 * @param text
	 */
	protected void numberFieldChanged(Text text) {
		try {
			long value = Long.decode(text.getText()).longValue();
			if (value < 0) {
				setErrorMessage(DisassemblyMessages.DisassemblyPreferencePage_error_negative_number);
			} else {
				setErrorMessage(null);
			}
		} catch(NumberFormatException nfe) {
			setErrorMessage(DisassemblyMessages.DisassemblyPreferencePage_error_not_a_number);
		}
	}

}
