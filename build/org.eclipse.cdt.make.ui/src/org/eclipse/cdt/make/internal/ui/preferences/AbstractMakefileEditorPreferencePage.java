/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * AbstraceMakeEditorPreferencePage
 */
public abstract class AbstractMakefileEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	OverlayPreferenceStore fOverlayStore;
	
	Map fCheckBoxes= new HashMap();
	private SelectionListener fCheckBoxListener= new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			Button button= (Button) e.widget;
			fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
		}
	};
	
	Map fTextFields= new HashMap();
	private ModifyListener fTextFieldListener= new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Text text= (Text) e.widget;
			fOverlayStore.setValue((String) fTextFields.get(text), text.getText());
		}
	};

	private Map fNumberFields= new HashMap();
	private ModifyListener fNumberFieldListener= new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			numberFieldChanged((Text) e.widget);
		}
	};
			
	public AbstractMakefileEditorPreferencePage() {
		super();
		setPreferenceStore(MakeUIPlugin.getDefault().getPreferenceStore());
		fOverlayStore= createOverlayStore();
	}
	
	protected abstract OverlayPreferenceStore createOverlayStore();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	protected void initializeFields() {
		Map checkBoxes= getCheckBoxes();
		Map textFields= getTextFields();
		Iterator e= checkBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b= (Button) e.next();
			String key= (String) checkBoxes.get(b);
			b.setSelection(getOverlayStore().getBoolean(key));
		}
		
		e= textFields.keySet().iterator();
		while (e.hasNext()) {
			Text t= (Text) e.next();
			String key= (String) textFields.get(t);
			t.setText(getOverlayStore().getString(key));
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		getOverlayStore().propagate();
		MakeUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	protected OverlayPreferenceStore getOverlayStore() {
		return fOverlayStore;
	}
	
	protected OverlayPreferenceStore setOverlayStore() {
		return fOverlayStore;
	}
	
	protected Map getCheckBoxes() {
		return fCheckBoxes;
	}
	
	protected Map getTextFields() {
		return fTextFields;
	}
	
	protected Map getNumberFields() {
		return fNumberFields;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		getOverlayStore().loadDefaults();
		initializeFields();
		handleDefaults();
		super.performDefaults();
	}
	
	protected abstract void handleDefaults();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		if (getOverlayStore() != null) {
			getOverlayStore().stop();
			fOverlayStore= null;
		}
		super.dispose();
	}
	
	protected Button addCheckBox(Composite parent, String labelText, String key, int indentation) {		
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(labelText);
		checkBox.setFont(parent.getFont());
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);
		
		getCheckBoxes().put(checkBox, key);
		
		return checkBox;
	}
	
	protected Control addTextField(Composite composite, String labelText, String key, int textLimit, int indentation, String[] errorMessages) {
		Font font= composite.getFont();
		
		Label label= new Label(composite, SWT.NONE);
		label.setText(labelText);
		label.setFont(font);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		label.setLayoutData(gd);
		
		Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);
		textControl.setFont(font);		
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		getTextFields().put(textControl, key);
		if (errorMessages != null) {
			getNumberFields().put(textControl, errorMessages);
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}
			
		return textControl;
	}
	
	void numberFieldChanged(Text textControl) {
		String number= textControl.getText();
		IStatus status= validatePositiveNumber(number, (String[])getNumberFields().get(textControl));
		if (!status.matches(IStatus.ERROR)) {
			getOverlayStore().setValue((String) getTextFields().get(textControl), number);
		}
		updateStatus(status);
	}
	
	private IStatus validatePositiveNumber(String number, String[] errorMessages) {
		StatusInfo status= new StatusInfo();
		if (number.length() == 0) {
			status.setError(errorMessages[0]);
		} else {
			try {
				int value= Integer.parseInt(number);
				if (value < 0)
					status.setError(MessageFormat.format(errorMessages[1], new String[]{number})); //$NON-NLS-1$
			} catch (NumberFormatException e) {
				status.setError(MessageFormat.format(errorMessages[1], new String[]{number})); //$NON-NLS-1$
			}
		}
		return status;
	}
	
	private void updateStatus(IStatus status) {
		if (!status.matches(IStatus.ERROR)) {
			Set keys= getNumberFields().keySet();
			for (Iterator iter = keys.iterator(); iter.hasNext();) {
				Text text = (Text) iter.next();
				IStatus s= validatePositiveNumber(text.getText(), (String[])getNumberFields().get(text));
				status= s.getSeverity() > status.getSeverity() ? s : status;
			}
		}	
		setValid(!status.matches(IStatus.ERROR));
		applyToStatusLine(this, status);
	}

	/*
	 * Applies the status to the status line of a dialog page.
	 */
	private void applyToStatusLine(DialogPage page, IStatus status) {
		String message= status.getMessage();
		switch (status.getSeverity()) {
			case IStatus.OK:
				page.setMessage(message, IMessageProvider.NONE);
				page.setErrorMessage(null);
				break;
			case IStatus.WARNING:
				page.setMessage(message, IMessageProvider.WARNING);
				page.setErrorMessage(null);
				break;				
			case IStatus.INFO:
				page.setMessage(message, IMessageProvider.INFORMATION);
				page.setErrorMessage(null);
				break;			
			default:
				if (message.length() == 0) {
					message= null;
				}
				page.setMessage(null);
				page.setErrorMessage(message);
				break;		
		}
	}
	
	/**
	 * Returns an array of size 2:
	 *  - first element is of type <code>Label</code>
	 *  - second element is of type <code>Text</code>
	 * Use <code>getLabelControl</code> and <code>getTextControl</code> to get the 2 controls.
	 */
	protected Control[] addLabelledTextField(Composite composite, String label, String key, int textLimit, int indentation, String[] errorMessages) {
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(label);
		labelControl.setFont(composite.getFont());
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);
	
		Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);		
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		textControl.setFont(composite.getFont());
		fTextFields.put(textControl, key);
		if (errorMessages != null) {
			fNumberFields.put(textControl, errorMessages);
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}
		
		return new Control[]{labelControl, textControl};
	}
	
	protected String loadPreviewContentFromFile(String filename) {
		String line;
		String separator= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuffer buffer= new StringBuffer(512);
		BufferedReader reader= null;
		try {
			reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			while ((line= reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			MakeUIPlugin.log(io);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
		}
		return buffer.toString();
	}
}
