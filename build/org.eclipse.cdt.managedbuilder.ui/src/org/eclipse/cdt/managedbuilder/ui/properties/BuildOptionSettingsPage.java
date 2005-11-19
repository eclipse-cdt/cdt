/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * ARM Ltd. - basic tooltip support
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class BuildOptionSettingsPage extends BuildSettingsPage {
	private Map fieldsMap = new HashMap();
	private IOptionCategory clonedCategory;
	private boolean isItResourceConfigPage;
	private Map fieldEditorsToParentMap = new HashMap();
	private AbstractBuildPropertyPage buildPropPage;

	public BuildOptionSettingsPage(AbstractBuildPropertyPage page,
			IConfiguration clonedConfig, IOptionCategory clonedCategory) {
		// Cache the configuration and option category this page is created for
		super(clonedConfig);
		this.clonedCategory = clonedCategory;
		isItResourceConfigPage = false;
		buildPropPage = page;
	}
	
	public BuildOptionSettingsPage(AbstractBuildPropertyPage page,
			IResourceConfiguration clonedResConfig, IOptionCategory clonedCategory) {
		// Cache the configuration and option category this page is created for
		super(clonedResConfig);
		this.clonedCategory = clonedCategory;
		isItResourceConfigPage = true;
		buildPropPage = page;
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
			options = clonedCategory.getOptions(clonedResConfig);
		} else {
			options = clonedCategory.getOptions(clonedConfig);
		}
		
		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions)options[index][0];
			if (holder == null) break;	//  The array may not be full
			IOption opt = (IOption)options[index][1];
			String prefName = getToolSettingsPreferenceStore().getOptionPrefName(opt); 

			
			// check to see if the option has an applicability calculator
			IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();
			
			// is the option visible?
			IBuildObject config;
			if ( isItResourceConfigPage ) {
				config = clonedResConfig;
			} else {
				config = clonedConfig;
			}

			if (applicabilityCalculator == null || applicabilityCalculator.isOptionVisible(config, holder, opt)) {
		
				try {
					// Figure out which type the option is and add a proper field
					// editor for it
					Composite fieldEditorParent = getFieldEditorParent();
					FieldEditor fieldEditor;

					switch (opt.getValueType()) {
						case IOption.STRING: {
							StringFieldEditor stringField;
							
							// If browsing is set, use a field editor that has a
							// browse button of the appropriate type.
							switch (opt.getBrowseType()) {
								case IOption.BROWSE_DIR: {
									stringField = new DirectoryFieldEditor(prefName, opt.getName(), fieldEditorParent);
								} break;
		
								case IOption.BROWSE_FILE: {
									stringField = new FileFieldEditor(prefName, opt.getName(), fieldEditorParent);
								} break;
		
								case IOption.BROWSE_NONE: {
									stringField = new StringFieldEditor(prefName, opt.getName(), fieldEditorParent);
								} break;
		
								default: {
									throw new BuildException(null);
								}
							}

							stringField.getTextControl(fieldEditorParent).setToolTipText(opt.getToolTip());
							stringField.getLabelControl(fieldEditorParent).setToolTipText(opt.getToolTip());
	
							fieldEditor = stringField;
						} break;
						
						case IOption.BOOLEAN: {
							class TooltipBooleanFieldEditor extends BooleanFieldEditor {
								public TooltipBooleanFieldEditor(String name, String labelText, String tooltip, Composite parent) {
									super(name, labelText, parent);
									getChangeControl(parent).setToolTipText(tooltip);
								}
							}
							
							fieldEditor = new TooltipBooleanFieldEditor(prefName, opt.getName(), opt.getToolTip(), fieldEditorParent);
						} break;
						
						case IOption.ENUMERATED: {
							String selId = opt.getSelectedEnum();
							String sel = opt.getEnumName(selId);

							// Get all applicable values for this enumerated Option, But display
							// only the enumerated values that are valid (static set of enumerated values defined
							// in the plugin.xml file) in the UI Combobox. This refrains the user from selecting an
							// invalid value and avoids issuing an error message.
							String[] enumNames = opt.getApplicableValues();
							Vector enumValidList = new Vector();
							for (int i = 0; i < enumNames.length; ++i) {
								if (opt.getValueHandler().isEnumValueAppropriate(config, 
										opt.getOptionHolder(), opt, opt.getValueHandlerExtraArgument(), enumNames[i])) {
									enumValidList.add(enumNames[i]);
								}
							}
							String[] enumValidNames = new String[enumValidList.size()];
							enumValidList.copyInto(enumValidNames);
	
							fieldEditor = new BuildOptionComboFieldEditor(prefName, opt.getName(), opt.getToolTip(), enumValidNames, sel, fieldEditorParent);
						} break;
						
						case IOption.INCLUDE_PATH:
						case IOption.STRING_LIST:
						case IOption.PREPROCESSOR_SYMBOLS:
						case IOption.LIBRARIES:
						case IOption.OBJECTS: {
							fieldEditor = new FileListControlFieldEditor(prefName, opt.getName(), opt.getToolTip(), fieldEditorParent, opt.getBrowseType());
						} break;
						
						default:
							throw new BuildException(null);
					}

					setFieldEditorEnablement(holder, opt, applicabilityCalculator, fieldEditor, fieldEditorParent);

					addField(fieldEditor);
					fieldsMap.put(prefName, fieldEditor);
					fieldEditorsToParentMap.put(fieldEditor, fieldEditorParent);

				} catch (BuildException e) {
				}
			}
		}
	}
	
	/**
	 * Answers <code>true</code> if the settings page has been created for the
	 * option category specified in the argument.
	 * 
	 * @param category
	 * @return
	 */
	public boolean isForCategory(IOptionCategory category) {
		if (category != null) {
			return category.equals(this.clonedCategory);
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
		
		Object[][] clonedOptions;
		IResourceConfiguration realRcCfg = null;
		IConfiguration realCfg = null;
		IBuildObject handler = null;
		
		if (isItResourceConfigPage){
			realRcCfg = buildPropPage.getRealRcConfig(clonedResConfig);
			if(realRcCfg == null)
				return false;
			handler = realRcCfg;
			clonedOptions = clonedCategory.getOptions(clonedResConfig);
		} else {
			realCfg = buildPropPage.getRealConfig(clonedConfig);
			if(realCfg == null)
				return false;
			handler = realCfg;
			clonedOptions = clonedCategory.getOptions(clonedConfig);
		}
		
		for (int i = 0; i < clonedOptions.length; i++) {
			IHoldsOptions clonedHolder = (IHoldsOptions)clonedOptions[i][0];
			if (clonedHolder == null) break;	//  The array may not be full
			IOption clonedOption = (IOption)clonedOptions[i][1];
			
			IHoldsOptions realHolder = buildPropPage.getRealHoldsOptions(clonedHolder);
			if(realHolder == null) continue;
			IOption realOption = buildPropPage.getRealOption(clonedOption, clonedHolder);
			if(realOption == null) continue;

			try {
				// Transfer value from preference store to options
				IOption setOption = null;
				switch (clonedOption.getValueType()) {
					case IOption.BOOLEAN :
						boolean boolVal = clonedOption.getBooleanValue();
						if(isItResourceConfigPage) {
							setOption = ManagedBuildManager.setOption(realRcCfg, realHolder, realOption, boolVal);
						} else {
							setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, boolVal);
						}
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPreferenceStore().setValue(setOption.getId(), boolVal);
//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//							fe.setPreferenceName(setOption.getId());
//						}
						break;
					case IOption.ENUMERATED :
						String enumVal = clonedOption.getStringValue();
						String enumId = clonedOption.getEnumeratedId(enumVal);
						if(isItResourceConfigPage) {
							setOption = ManagedBuildManager.setOption(realRcCfg, realHolder, realOption, 
									(enumId != null && enumId.length() > 0) ? enumId : enumVal);
						} else {
							setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, 
									(enumId != null && enumId.length() > 0) ? enumId : enumVal);
						}
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPreferenceStore().setValue(setOption.getId(), enumVal);
//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//							fe.setPreferenceName(setOption.getId());
//						}
						break;
					case IOption.STRING :
						String strVal = clonedOption.getStringValue();
						if(isItResourceConfigPage){
							setOption = ManagedBuildManager.setOption(realRcCfg, realHolder, realOption, strVal);
						} else {
							setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, strVal);	
						}
						
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPreferenceStore().setValue(setOption.getId(), strVal);
//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//							fe.setPreferenceName(setOption.getId());
//						}
						break;
					case IOption.STRING_LIST :
					case IOption.INCLUDE_PATH :
					case IOption.PREPROCESSOR_SYMBOLS :
					case IOption.LIBRARIES :
					case IOption.OBJECTS :
						String[] listVal = (String[])((List)clonedOption.getValue()).toArray(new String[0]);
						if( isItResourceConfigPage){
							setOption = ManagedBuildManager.setOption(realRcCfg, realHolder, realOption, listVal);
						}else {
							setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, listVal);	
						}
						
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPreferenceStore().setValue(setOption.getId(), listStr);
//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//							fe.setPreferenceName(setOption.getId());
//						}
						break;
					default :
						break;
				}

				// Call an MBS CallBack function to inform that Settings related to Apply/OK button 
				// press have been applied.
				if (setOption == null)
					setOption = realOption;
				
				if (setOption.getValueHandler().handleValue(
						handler, 
						setOption.getOptionHolder(), 
						setOption,
						setOption.getValueHandlerExtraArgument(), 
						IManagedOptionValueHandler.EVENT_APPLY)) {
					// TODO : Event is handled successfully and returned true.
					// May need to do something here say log a message.
				} else {
					// Event handling Failed. 
				} 
			} catch (BuildException e) {
			} catch (ClassCastException e) {
			}
			

		}
		return ok;
	}
	
	/**
	 * Update field editors in this page when the page is loaded.
	 */
	public void updateFields() {
		Object[][] options;
		if (isItResourceConfigPage) {
			options = clonedCategory.getOptions(clonedResConfig);
		} else {
			options = clonedCategory.getOptions(clonedConfig);
		}

		// some option has changed on this page... update enabled/disabled state for all options

		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions) options[index][0];
			if (holder == null)
				break; //  The array may not be full
			IOption opt = (IOption) options[index][1];
			String prefName = getToolSettingsPreferenceStore().getOptionPrefName(opt); 


			// is the option on this page?
			if (fieldsMap.containsKey(prefName)) {
				// check to see if the option has an applicability calculator
				IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();

				if (applicabilityCalculator != null) {
					FieldEditor fieldEditor = (FieldEditor) fieldsMap.get(prefName);
					Composite parent = (Composite) fieldEditorsToParentMap.get(fieldEditor);
					setFieldEditorEnablement(holder, opt, applicabilityCalculator, fieldEditor, parent);
				}
			}

		}
		
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
//		super.performOk();
	}
	
	private void setFieldEditorEnablement(IHoldsOptions holder, IOption option,
			IOptionApplicability optionApplicability, FieldEditor fieldEditor, Composite parent) {
		if (optionApplicability == null)
			return;

		// if the option is not enabled then disable it
		IBuildObject config;
		if ( isItResourceConfigPage ) {
			config = clonedResConfig;
		} else {
			config = clonedConfig;
		}
		if (!optionApplicability.isOptionEnabled(config, holder, option )) {
			fieldEditor.setEnabled(false, parent);
		} else {
			fieldEditor.setEnabled(true, parent);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		// allow superclass to handle as well
		super.propertyChange(event);
		
		Object source = event.getSource();
		IOption changedOption = null;
		IHoldsOptions changedHolder = null;
		IOption newOption = null;
		String id = null;

		if(source instanceof FieldEditor){
			FieldEditor fe = (FieldEditor)source;
			
			id = fe.getPreferenceName();
			
			Object option[] = this.getToolSettingsPreferenceStore().getOption(id);
			
			if(option != null){
				changedOption = (IOption)option[1];
				changedHolder = (IHoldsOptions)option[0];
				try {
					switch(changedOption.getValueType()){
					case IOption.STRING:
						if(fe instanceof StringFieldEditor){
							String val = ((StringFieldEditor)fe).getStringValue();
							if (isItResourceConfigPage) {
								newOption = clonedResConfig.setOption(changedHolder,changedOption,val);
							} else {
								newOption = clonedConfig.setOption(changedHolder,changedOption,val);
							}
						}
						break;
					case IOption.BOOLEAN:
						if(fe instanceof BooleanFieldEditor){
							boolean val = ((BooleanFieldEditor)fe).getBooleanValue();
							if (isItResourceConfigPage) {
								newOption = clonedResConfig.setOption(changedHolder,changedOption,val);
							} else {
								newOption = clonedConfig.setOption(changedHolder,changedOption,val);
							}
						}
						break;
					case IOption.ENUMERATED:
						if(fe instanceof BuildOptionComboFieldEditor){
							String name = ((BuildOptionComboFieldEditor)fe).getSelection();
							String enumId = changedOption.getEnumeratedId(name);
							if(isItResourceConfigPage) {
								newOption = clonedResConfig.setOption(changedHolder, changedOption, 
										(enumId != null && enumId.length() > 0) ? enumId : name);
							} else {
								newOption = clonedConfig.setOption(changedHolder, changedOption, 
										(enumId != null && enumId.length() > 0) ? enumId : name);
							}
	
						}
						break;
					case IOption.INCLUDE_PATH:
					case IOption.STRING_LIST:
					case IOption.PREPROCESSOR_SYMBOLS:
					case IOption.LIBRARIES:
					case IOption.OBJECTS:
						if(fe instanceof FileListControlFieldEditor){
							String val[] =((FileListControlFieldEditor)fe).getStringListValue();
							if (isItResourceConfigPage) {
								newOption = clonedResConfig.setOption(changedHolder,changedOption,val);
							} else {
								newOption = clonedConfig.setOption(changedHolder,changedOption,val);
							}
						}
						break;
					default:
						break;
	
					}
				} catch (BuildException e) {
				}
				
			}
		}
		

		Object[][] options;
		if (isItResourceConfigPage) {
			options = clonedCategory.getOptions(clonedResConfig);
		} else {
			options = clonedCategory.getOptions(clonedConfig);
		}

		// some option has changed on this page... update enabled/disabled state for all options

		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions) options[index][0];
			if (holder == null)
				break; //  The array may not be full
			IOption opt = (IOption) options[index][1];
			String prefName = getToolSettingsPreferenceStore().getOptionPrefName(opt); 


			// is the option on this page?
			if (fieldsMap.containsKey(prefName)) {
				// check to see if the option has an applicability calculator
				IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();

				if (applicabilityCalculator != null) {
					FieldEditor fieldEditor = (FieldEditor) fieldsMap.get(prefName);
					Composite parent = (Composite) fieldEditorsToParentMap.get(fieldEditor);
					setFieldEditorEnablement(holder, opt, applicabilityCalculator, fieldEditor, parent);
				}
			}

		}
		
		Iterator iter = fieldsMap.values().iterator();
		while (iter.hasNext()) {
			FieldEditor editor = (FieldEditor) iter.next();
			if(id == null || !id.equals(editor.getPreferenceName()))
				editor.load();
		}

	}

}
