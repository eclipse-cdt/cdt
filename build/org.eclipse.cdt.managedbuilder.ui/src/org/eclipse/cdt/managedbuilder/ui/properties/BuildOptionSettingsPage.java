/**********************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.graphics.Point;

public class BuildOptionSettingsPage extends BuildSettingsPage {
	private Map fieldsMap = new HashMap();
	private IOptionCategory category;
	private boolean isItResourceConfigPage;

	public BuildOptionSettingsPage(IConfiguration configuration, IOptionCategory category) {
		// Cache the configuration and option category this page is created for
		super(configuration);
		this.category = category;
		isItResourceConfigPage = false;
	}
	
	public BuildOptionSettingsPage(IResourceConfiguration resConfig, IOptionCategory category) {
		// Cache the configuration and option category this page is created for
		super(resConfig);
		this.category = category;
		isItResourceConfigPage = true;
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
		Object[][] options;
		if ( isItResourceConfigPage ) {
			options = category.getOptions(resConfig);
		} else {
			options = category.getOptions(configuration);
		}
		
		for (int index = 0; index < options.length; ++index) {
			// Get the option
			ITool tool = (ITool)options[index][0];
			if (tool == null) break;	//  The array may not be full
			IOption opt = (IOption)options[index][1];
			try {
				// Figure out which type the option is and add a proper field
				// editor for it
				switch (opt.getValueType()) {
					case IOption.STRING :
						StringFieldEditor stringField = new StringFieldEditor(
								opt.getId(), opt.getName(), getFieldEditorParent());
						addField(stringField);
						fieldsMap.put(opt.getId(), stringField);
						break;
					case IOption.BOOLEAN :
						BooleanFieldEditor booleanField = new BooleanFieldEditor(
								opt.getId(), opt.getName(), getFieldEditorParent());
						addField(booleanField);
						fieldsMap.put(opt.getId(),booleanField);
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
						fieldsMap.put(opt.getId(), comboField);
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
						fieldsMap.put(opt.getId(), listField);
						break;
					default :
						break;
				}
			} catch (BuildException e) {}
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
		
		Object[][] options;
		if (isItResourceConfigPage){
			options = category.getOptions(resConfig);
		} else {
			options = category.getOptions(configuration);
		}
		
		for (int i = 0; i < options.length; i++) {
			ITool tool = (ITool)options[i][0];
			if (tool == null) break;	//  The array may not be full
			IOption option = (IOption)options[i][1];
			try {
				// Transfer value from preference store to options
				IOption setOption;
				switch (option.getValueType()) {
					case IOption.BOOLEAN :
						boolean boolVal = getToolSettingsPreferenceStore().getBoolean(option.getId());
						if(isItResourceConfigPage) {
							setOption = ManagedBuildManager.setOption(resConfig, tool, option, boolVal);
						} else {
							setOption = ManagedBuildManager.setOption(configuration, tool, option, boolVal);
						}
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), boolVal);
							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
							fe.setPreferenceName(setOption.getId());
						}
						break;
					case IOption.ENUMERATED :
						String enumVal = getToolSettingsPreferenceStore().getString(option.getId());
						String enumId = option.getEnumeratedId(enumVal);
						if(isItResourceConfigPage) {
							setOption = ManagedBuildManager.setOption(resConfig, tool, option, 
									(enumId != null && enumId.length() > 0) ? enumId : enumVal);
						} else {
							setOption = ManagedBuildManager.setOption(configuration, tool, option, 
									(enumId != null && enumId.length() > 0) ? enumId : enumVal);
						}
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), enumVal);
							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
							fe.setPreferenceName(setOption.getId());
						}
						break;
					case IOption.STRING :
						String strVal = getToolSettingsPreferenceStore().getString(option.getId());
						if(isItResourceConfigPage){
							setOption = ManagedBuildManager.setOption(resConfig, tool, option, strVal);
						} else {
							setOption = ManagedBuildManager.setOption(configuration, tool, option, strVal);	
						}
						
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), strVal);
							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
							fe.setPreferenceName(setOption.getId());
						}
						break;
					case IOption.STRING_LIST :
					case IOption.INCLUDE_PATH :
					case IOption.PREPROCESSOR_SYMBOLS :
					case IOption.LIBRARIES :
					case IOption.OBJECTS :
						String listStr = getToolSettingsPreferenceStore().getString(option.getId());
						String[] listVal = BuildToolsSettingsStore.parseString(listStr);
						if( isItResourceConfigPage){
							setOption = ManagedBuildManager.setOption(resConfig, tool, option, listVal);
						}else {
							setOption = ManagedBuildManager.setOption(configuration, tool, option, listVal);	
						}
						
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), listStr);
							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
							fe.setPreferenceName(setOption.getId());
						}
						break;
					default :
						break;
				}
			} catch (BuildException e) {}
		}
		return ok;
	}
	
	/**
	 * Update field editors in this page when the page is loaded.
	 */
	public void updateFields() {
		Collection fieldsList = fieldsMap.values();
		Iterator iter = fieldsList.iterator();
		while (iter.hasNext()) {
			FieldEditor editor = (FieldEditor) iter.next();
			//  TODO: Why was loadDefault called before load?  It results in String fields 
			//        setting the "dirty" flag
			//editor.loadDefault();
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
