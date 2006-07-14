/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Michael Berger (IBM Canada) - 148434 Better F1 help.
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.logging.ui;

import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.rse.logging.IRemoteSystemsLogging;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;

/**
 * An abstract preference page for all remote system logging.<br/>
 * Use a subclass of this page if you need a preference page to control
 * logging.
 */
public abstract class LoggingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button radioButton0;
	private Button radioButton1;
	private Button radioButton2;
	private Button radioButton3;

	/**
	 * Creates composite control and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created coposite
	 */
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NONE);
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);

		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	/** 
	 * Method declared on PreferencePage
	 */
	protected Control createContents(Composite parent) {
		Bundle bundle = getBundle();
		Composite composite_tab = createComposite(parent, 2);
		String bundleName = (String) (bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_NAME));
		String topLabel1 = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_TOPLABEL1;
		topLabel1 = MessageFormat.format(topLabel1, new Object[] { bundleName });
		createLabel(composite_tab, topLabel1);
		forceSpace(composite_tab);
		String topLabel2 = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_TOPLABEL2;
		createLabel(composite_tab, topLabel2);
		tabForward(composite_tab);
		Composite composite1_radioButton = createComposite(composite_tab, 1);
		String text = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_ERRORS_ONLY;
		Set used = LabelUtil.usedFromString("ad"); // the mnemonics already used on preference page (in English)
		radioButton0 = createRadioButton(composite1_radioButton, LabelUtil.assignMnemonic(text, used));
		text = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_WARNINGS_ERRORS;
		radioButton1 = createRadioButton(composite1_radioButton, LabelUtil.assignMnemonic(text, used));
		text = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_INFO_DEBUG;
		radioButton2 = createRadioButton(composite1_radioButton, LabelUtil.assignMnemonic(text, used));
		text = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_FULL_DEBUG;
		radioButton3 = createRadioButton(composite1_radioButton, LabelUtil.assignMnemonic(text, used));
		initializeValues();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.rse.logging.rsel0000");
		return new Composite(parent, SWT.NULL);
	}

	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Utility method that creates a radio button instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new button
	 * @param label  the label for the new button
	 * @return the newly-created button
	 */
	private Button createRadioButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	/** 
	 * 
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		Bundle bundle = getBundle();
		if (bundle != null) {
			AbstractUIPlugin plugin = getPlugin();
			if (plugin != null) {
				return plugin.getPreferenceStore();
			} else {
				return new PreferenceStore();
			}
		} else {
			return new PreferenceStore();
		}
	}

	protected abstract AbstractUIPlugin getPlugin();

	private Bundle getBundle() {
		Plugin plugin = getPlugin();
		Bundle bundle = plugin.getBundle();
		return bundle;
	}
	
	/** 
	 * Method declared on IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * Initializes states of the controls using current values
	 * in the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		radioButton0.setSelection(false);
		radioButton1.setSelection(false);
		radioButton2.setSelection(false);
		if (null != radioButton3) radioButton3.setSelection(false);
		int choice = store.getInt(IRemoteSystemsLogging.DEBUG_LEVEL);
		switch (choice) {
		case 0:
			radioButton0.setSelection(true);
			break;
		case 1:
			radioButton1.setSelection(true);
			break;
		case 2:
			radioButton2.setSelection(true);
			break;
		case 3:
			if (null != radioButton3)
				radioButton3.setSelection(true);
			else
				radioButton2.setSelection(true);
			break;
		}
	}

	/**
	 * Initializes states of the controls using default values
	 * in the preference store.
	 */
	private void initializeDefaults() {
		IPreferenceStore store = getPreferenceStore();
		radioButton0.setSelection(false);
		radioButton1.setSelection(false);
		radioButton2.setSelection(false);
		if (null != radioButton3) radioButton3.setSelection(false);
		int choice = store.getDefaultInt(IRemoteSystemsLogging.DEBUG_LEVEL);
		switch (choice) {
		case 0:
			radioButton0.setSelection(true);
			break;
		case 1:
			radioButton1.setSelection(true);
			break;
		case 2:
			radioButton2.setSelection(true);
			break;
		case 3:
			if (null != radioButton3)
				radioButton3.setSelection(true);
			else
				radioButton2.setSelection(true);
			break;
		}
	}

	/**
	 * Method declared on PreferencePage
	 */
	protected void performDefaults() {
		super.performDefaults();
		initializeDefaults();
	}

	/** 
	 * Method declared on PreferencePage
	 */
	public boolean performOk() {
		storeValues();
		return true;
	}

	/**
	 * Stores the values of the controls back to the preference store.
	 */
	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		int choice = 0;
		if (radioButton1.getSelection())
			choice = 1;
		else if (radioButton2.getSelection())
			choice = 2;
		else if (null != radioButton3 && radioButton3.getSelection()) choice = 3;
		store.setValue(IRemoteSystemsLogging.DEBUG_LEVEL, choice);
	}

	/**
	 * Creates a tab of one horizontal span.
	 *
	 * @param parent  the parent in which the tab should be created
	 */
	private void tabForward(Composite parent) {
		Label vfiller = new Label(parent, SWT.LEFT);
		GridData gridData = new GridData();
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessVerticalSpace = false;
		vfiller.setLayoutData(gridData);
	}

	/**
	 * Create a horizontal space line.
	 */
	private void forceSpace(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}
}