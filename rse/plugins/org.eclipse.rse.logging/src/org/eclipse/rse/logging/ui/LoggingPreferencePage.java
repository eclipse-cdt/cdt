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
import org.eclipse.rse.logging.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
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
	private Composite createComposite(Composite parent, int span, int numColumns) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = span;
		composite.setLayoutData(data);
		return composite;
	}

	/**
	 * Creates group control and sets the default layout data.
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created coposite
	 */
	private Group createGroup(Composite parent, int span, int numColumns, String text) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		group.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = span;
		group.setLayoutData(data);
		group.setText(text);
		return group;
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
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	private Label createLabel(Composite parent, int span, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	/** 
	 * Method declared on PreferencePage
	 */
	protected Control createContents(Composite parent) {
		Composite composite_tab = createComposite(parent, 1, 1);
//		String bundleName = (String)(getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_NAME));
//		String topLabel1 = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_TOPLABEL1;
//		topLabel1 = MessageFormat.format(topLabel1, new Object[] {bundleName});
//		createLabel(composite_tab, 1, topLabel1);
		String topLabel2 = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_TOPLABEL2;
		Group group1 = createGroup(composite_tab, 1, 1, topLabel2);
		Set used = LabelUtil.usedFromString("ad"); // the mnemonics already used on preference page (in English)
		String text = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_ERRORS_ONLY;
		radioButton0 = createRadioButton(group1, LabelUtil.assignMnemonic(text, used));
		text = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_WARNINGS_ERRORS;
		radioButton1 = createRadioButton(group1, LabelUtil.assignMnemonic(text, used));
		text = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_INFO_DEBUG;
		radioButton2 = createRadioButton(group1, LabelUtil.assignMnemonic(text, used));
		if (Logger.DEBUG) {
			text = LoggingPreferenceLabels.LOGGING_PREFERENCE_PAGE_FULL_DEBUG;
			radioButton3 = createRadioButton(group1, LabelUtil.assignMnemonic(text, used));
		}
		initializeValues();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.rse.logging.rsel0000");		
		return composite_tab;
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

}