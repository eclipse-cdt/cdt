/*******************************************************************************
 * Copyright (c) 2002, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *     Anton Leherbauer (Wind River Systems)
 *     Serge Beauchamp (Freescale Semiconductor) - Bug 418817
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * AbstractPreferencePage
 */
public abstract class AbstractPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	protected OverlayPreferenceStore fOverlayStore;

	/**
	 * Unique list of masters for control dependencies.
	 * @see #createDependency(Button, String, Control)
	 */
	private Set<Button> fMasters = new LinkedHashSet<>();

	protected Map<Object, String> fTextFields = new HashMap<>();
	private ModifyListener fTextFieldListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			Text text = (Text) e.widget;
			fOverlayStore.setValue(fTextFields.get(text), text.getText());
		}
	};

	protected Map<Object, String> fComboBoxes = new HashMap<>();
	private ModifyListener fComboBoxListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			Combo combo = (Combo) e.widget;
			String state = ProposalFilterPreferencesUtil.comboStateAsString(combo);
			fOverlayStore.setValue(fComboBoxes.get(combo), state);
		}
	};

	protected Map<Object, String> fCheckBoxes = new HashMap<>();
	private SelectionListener fCheckBoxListener = new SelectionListener() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.widget;
			String value = (String) button.getData();
			if (value == null) {
				fOverlayStore.setValue(fCheckBoxes.get(button), button.getSelection());
			} else if (button.getSelection()) {
				fOverlayStore.setValue(fCheckBoxes.get(button), value);
			}
		}
	};

	protected ArrayList<Text> fNumberFields = new ArrayList<>();
	private ModifyListener fNumberFieldListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			numberFieldChanged((Text) e.widget);
		}
	};

	protected Map<Object, String> fColorButtons = new HashMap<>();
	private SelectionListener fColorButtonListener = new SelectionListener() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			ColorSelector editor = (ColorSelector) e.widget.getData();
			PreferenceConverter.setValue(fOverlayStore, fColorButtons.get(editor), editor.getColorValue());
		}
	};

	protected static final int NO_TEXT_LIMIT = -1;

	protected Button addRadioButton(Composite parent, String label, String key, int indentation) {
		return addRadioButton(parent, label, key, null, indentation);
	}

	protected Button addRadioButton(Composite parent, String label, String key, String value, int indentation) {
		Button radioButton = new Button(parent, SWT.RADIO);
		radioButton.setText(label);
		if (value != null)
			radioButton.setData(value);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = 2;
		radioButton.setLayoutData(gd);

		if (key != null) {
			radioButton.addSelectionListener(fCheckBoxListener);
			fCheckBoxes.put(radioButton, key);
		}

		return radioButton;
	}

	protected Button addCheckBox(Composite parent, String label, String key, int indentation) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);

		fCheckBoxes.put(checkBox, key);

		return checkBox;
	}

	protected Group addGroupBox(Composite parent, String label, int nColumns) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(label);
		GridLayout layout = new GridLayout();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		layout.numColumns = nColumns;
		group.setLayout(layout);
		group.setLayoutData(gd);
		return group;
	}

	protected Control addTextField(Composite composite, String label, String key, int textLimit, int indentation,
			boolean isNumber) {
		Label labelControl = new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indentation;
		labelControl.setLayoutData(gd);

		Text textControl = new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		fTextFields.put(textControl, key);
		if (isNumber) {
			fNumberFields.add(textControl);
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}

		return textControl;
	}

	protected void addComboBox(Composite composite, String label, String key, int textLimit, int indentation) {
		Label labelControl = new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indentation;
		labelControl.setLayoutData(gd);

		Combo comboControl = new Combo(composite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY); // TODO: When will the combo be disposed?
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		if (textLimit != NO_TEXT_LIMIT) {
			gd.widthHint = convertWidthInCharsToPixels(textLimit + 1);
			comboControl.setTextLimit(textLimit);
		}
		comboControl.setLayoutData(gd);
		fComboBoxes.put(comboControl, key);
		comboControl.addModifyListener(fComboBoxListener); // TODO: When will the listener be removed?
	}

	protected void addFiller(Composite composite) {
		PixelConverter pixelConverter = new PixelConverter(composite);
		Label filler = new Label(composite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	protected void createDependency(final Button master, String masterKey, final Control slave) {
		indent(slave);
		boolean masterState = fOverlayStore.getBoolean(masterKey);
		slave.setEnabled(masterState);
		SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				slave.setEnabled(master.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		master.addSelectionListener(listener);
		fMasters.add(master);
	}

	protected void numberFieldChanged(Text textControl) {
		String number = textControl.getText();
		IStatus status = validatePositiveNumber(number);
		if (!status.matches(IStatus.ERROR))
			fOverlayStore.setValue(fTextFields.get(textControl), number);
		updateStatus(status);
	}

	private IStatus validatePositiveNumber(String number) {
		StatusInfo status = new StatusInfo();
		if (number.length() == 0) {
			status.setError(PreferencesMessages.CEditorPreferencePage_empty_input);
		} else {
			try {
				int value = Integer.parseInt(number);
				if (value < 0)
					status.setError(NLS.bind(PreferencesMessages.CEditorPreferencePage_invalid_input, number));
			} catch (NumberFormatException e) {
				status.setError(NLS.bind(PreferencesMessages.CEditorPreferencePage_invalid_input, number));
			}
		}
		return status;
	}

	protected void updateStatus(IStatus status) {
		if (!status.matches(IStatus.ERROR)) {
			for (int i = 0; i < fNumberFields.size(); i++) {
				Text text = fNumberFields.get(i);
				IStatus s = validatePositiveNumber(text.getText());
				status = StatusUtil.getMoreSevere(s, status);
			}
		}
		//status= StatusUtil.getMoreSevere(fCEditorHoverConfigurationBlock.getStatus(), status);
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

	protected void indent(Control control) {
		GridData gridData = new GridData();
		gridData.horizontalIndent = 20;
		control.setLayoutData(gridData);
	}

	protected Control addColorButton(Composite parent, String label, String key, int indentation) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		composite.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		Label labelControl = new Label(composite, SWT.NONE);
		labelControl.setText(label);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		labelControl.setLayoutData(gd);

		ColorSelector editor = new ColorSelector(composite);
		Button button = editor.getButton();
		button.setData(editor);

		gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		button.setLayoutData(gd);
		button.addSelectionListener(fColorButtonListener);

		fColorButtons.put(editor, key);

		return composite;
	}

	public AbstractPreferencePage() {
		super();
		setPreferenceStore(PreferenceConstants.getPreferenceStore());
		fOverlayStore = new OverlayPreferenceStore(getPreferenceStore(), createOverlayStoreKeys());
	}

	protected abstract OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys();

	protected void initializeFields() {
		Iterator<Object> e = fColorButtons.keySet().iterator();
		while (e.hasNext()) {
			ColorSelector c = (ColorSelector) e.next();
			String key = fColorButtons.get(c);
			RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
			c.setColorValue(rgb);
		}

		e = fCheckBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b = (Button) e.next();
			String key = fCheckBoxes.get(b);
			String value = (String) b.getData();
			if (value == null) {
				b.setSelection(fOverlayStore.getBoolean(key));
			} else if (value.equals(fOverlayStore.getString(key))) {
				b.setSelection(true);
			}
		}

		e = fTextFields.keySet().iterator();
		while (e.hasNext()) {
			Text t = (Text) e.next();
			String key = fTextFields.get(t);
			t.setText(fOverlayStore.getString(key));
		}

		e = fComboBoxes.keySet().iterator();
		while (e.hasNext()) {
			Combo c = (Combo) e.next();
			String key = fComboBoxes.get(c);
			String state = fOverlayStore.getString(key);
			// Interpret the state string as a Combo state description
			ProposalFilterPreferencesUtil.restoreComboFromString(c, state);
		}

		// Notify listeners for control dependencies.
		Iterator<Button> buttonIter = fMasters.iterator();
		while (buttonIter.hasNext()) {
			Button b = buttonIter.next();
			b.notifyListeners(SWT.Selection, new Event());
		}
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		fOverlayStore.propagate();
		return true;
	}

	@Override
	protected void performDefaults() {
		fOverlayStore.loadDefaults();
		initializeFields();
		super.performDefaults();
	}

	@Override
	public void dispose() {
		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore = null;
		}
		super.dispose();
	}
}
