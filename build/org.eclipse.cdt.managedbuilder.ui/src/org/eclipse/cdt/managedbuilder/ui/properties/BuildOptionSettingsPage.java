package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2003,2004 IBM Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import java.util.ArrayList;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.graphics.Point;

public class BuildOptionSettingsPage extends BuildSettingsPage {
	private ArrayList fieldsList = new ArrayList();
	private IOptionCategory category;

	BuildOptionSettingsPage(IConfiguration configuration, IOptionCategory category) {
		// Cache the configuration and option category this page is created for
		super(configuration);
		this.category = category;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#computeSize()
	 */
	public Point computeSize() {
		return super.computeSize();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		// Get the preference store for the build settings
		super.createFieldEditors();
		// Iterate over the options in the category and create a field editor
		// for each
		IOption[] options = category.getOptions(configuration);
		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IOption opt = options[index];
			// Figure out which type the option is and add a proper field
			// editor for it
			switch (opt.getValueType()) {
				case IOption.STRING :
					StringFieldEditor stringField = new StringFieldEditor(opt
							.getId(), opt.getName(), getFieldEditorParent());
					addField(stringField);
					fieldsList.add(stringField);
					break;
				case IOption.BOOLEAN :
					BooleanFieldEditor booleanField = new BooleanFieldEditor(
							opt.getId(), opt.getName(), getFieldEditorParent());
					addField(booleanField);
					fieldsList.add(booleanField);
					break;
				case IOption.ENUMERATED :
					String selId;
					String sel;
					try {
						selId = opt.getSelectedEnum();
						sel = opt.getEnumName(selId);
					} catch (BuildException e) {
						// If we get this exception, then the option type is
						// wrong
						break;
					}
					BuildOptionComboFieldEditor comboField = new BuildOptionComboFieldEditor(
							opt.getId(), opt.getName(), opt
									.getApplicableValues(), sel,
							getFieldEditorParent());
					addField(comboField);
					fieldsList.add(comboField);
					break;
				case IOption.INCLUDE_PATH :
				case IOption.STRING_LIST :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.LIBRARIES :
				case IOption.OBJECTS :
					FileListControlFieldEditor listField =
						new FileListControlFieldEditor(
							opt.getId(),
							opt.getName(),
							getFieldEditorParent(),
							opt.getBrowseType());
					addField(listField);
					fieldsList.add(listField);
					break;
				default :
					break;
			}
		}
	}
	
	/**
	 * Answers <code>true</code> if the settings page has been created for 
	 * the option category specified in the argument.
	 * 
	 * @param category
	 * @return
	 */
	public boolean isForCategory(IOptionCategory category) {
		if (category != null) {
			return category.equals(this.category);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		// Write the field editor contents out to the preference store
		boolean ok = super.performOk();
		// Write the preference store values back to the build model
		IOption[] options = category.getOptions(configuration);
		for (int i = 0; i < options.length; i++) {
			IOption option = options[i];

			// Transfer value from preference store to options
			switch (option.getValueType()) {
				case IOption.BOOLEAN :
					boolean boolVal = getPreferenceStore().getBoolean(
							option.getId());
					ManagedBuildManager.setOption(configuration, option,
							boolVal);
					break;
				case IOption.ENUMERATED :
					String enumVal = getPreferenceStore().getString(
							option.getId());
					String enumId = option.getEnumeratedId(enumVal);
					ManagedBuildManager.setOption(configuration, option, 
						(enumId.length() > 0) ? enumId : enumVal);
					break;
				case IOption.STRING :
					String strVal = getPreferenceStore().getString(
							option.getId());
					ManagedBuildManager
							.setOption(configuration, option, strVal);
					break;
				case IOption.STRING_LIST :
				case IOption.INCLUDE_PATH :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.LIBRARIES :
				case IOption.OBJECTS :
					String listStr = getPreferenceStore().getString(
							option.getId());
					String[] listVal = BuildToolsSettingsStore
							.parseString(listStr);
					ManagedBuildManager.setOption(configuration, option,
							listVal);
					break;
				default :
					break;
			}
		}
		return ok;
	}
	
	/**
	 * Update field editors in this page when the page is loaded.
	 */
	public void updateFields() {
		for (int i = 0; i < fieldsList.size(); i++) {
			FieldEditor editor = (FieldEditor) fieldsList.get(i);
			editor.loadDefault();
			editor.load();
		}
	}
	
	/**
	 * saves all field editors
	 */
	public void storeSettings() {
		super.performOk();
	}
}
