/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
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
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class BuildOptionSettingsUI extends AbstractToolSettingUI {
	private Map fieldsMap = new HashMap();
	private IOptionCategory category;
	private IHoldsOptions optionHolder;
	private Map fieldEditorsToParentMap = new HashMap();

	public BuildOptionSettingsUI(AbstractCBuildPropertyTab page,
			IResourceInfo info, IHoldsOptions optionHolder, 
			IOptionCategory _category) {
		super(info);
		this.category = _category;
		this.optionHolder = optionHolder;
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
		Object[][] options = category.getOptions(fInfo, optionHolder);
		
		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions)options[index][0];
			if (holder == null) break;	//  The array may not be full
			IOption opt = (IOption)options[index][1];
			String optId = getToolSettingsPrefStore().getOptionId(opt);
			
			// check to see if the option has an applicability calculator
			IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();
			IBuildObject config = fInfo;

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
									stringField = new DirectoryFieldEditor(optId, opt.getName(), fieldEditorParent);
								} break;
		
								case IOption.BROWSE_FILE: {
									stringField = new FileFieldEditor(optId, opt.getName(), fieldEditorParent);
								} break;
		
								case IOption.BROWSE_NONE: {
									stringField = new StringFieldEditor(optId, opt.getName(), fieldEditorParent);
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
							
							fieldEditor = new TooltipBooleanFieldEditor(optId, opt.getName(), opt.getToolTip(), fieldEditorParent);
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
	
							fieldEditor = new BuildOptionComboFieldEditor(optId, opt.getName(), opt.getToolTip(), enumValidNames, sel, fieldEditorParent);
						} break;
						
						case IOption.INCLUDE_PATH:
						case IOption.STRING_LIST:
						case IOption.PREPROCESSOR_SYMBOLS:
						case IOption.LIBRARIES:
						case IOption.OBJECTS:
						case IOption.INCLUDE_FILES:
						case IOption.LIBRARY_PATHS:
						case IOption.LIBRARY_FILES:
						case IOption.MACRO_FILES:
						case IOption.UNDEF_INCLUDE_PATH:
						case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
						case IOption.UNDEF_INCLUDE_FILES:
						case IOption.UNDEF_LIBRARY_PATHS:
						case IOption.UNDEF_LIBRARY_FILES:
						case IOption.UNDEF_MACRO_FILES:						{
							fieldEditor = new FileListControlFieldEditor(optId, opt.getName(), opt.getToolTip(), fieldEditorParent, opt.getBrowseType());
						} break;
						
						default:
							throw new BuildException(null);
					}

					setFieldEditorEnablement(holder, opt, applicabilityCalculator, fieldEditor, fieldEditorParent);

					addField(fieldEditor);
					fieldsMap.put(optId, fieldEditor);
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
	public boolean isFor(Object holder, Object cat) {
		if (holder instanceof IHoldsOptions && cat != null && cat instanceof IOptionCategory) {
			if (this.optionHolder == optionHolder && cat.equals(this.category))
				return true;
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
//		IResourceConfiguration realRcCfg = null;
		IConfiguration realCfg = null;
		IBuildObject handler = null;
		
		realCfg = buildPropPage.getCfg(); //.getRealConfig(clonedConfig);
		if(realCfg == null)	return false;
		handler = realCfg;
		clonedOptions = category.getOptions(fInfo.getParent(), optionHolder);
		
		for (int i = 0; i < clonedOptions.length; i++) {
			IHoldsOptions clonedHolder = (IHoldsOptions)clonedOptions[i][0];
			if (clonedHolder == null) break;	//  The array may not be full
			IOption clonedOption = (IOption)clonedOptions[i][1];
			
			IHoldsOptions realHolder = clonedHolder; // buildPropPage.getRealHoldsOptions(clonedHolder);
			if(realHolder == null) continue;
			IOption realOption = clonedOption; // buildPropPage.getRealOption(clonedOption, clonedHolder);
			if(realOption == null) continue;

			try {
				// Transfer value from preference store to options
				IOption setOption = null;
				switch (clonedOption.getValueType()) {
					case IOption.BOOLEAN :
						boolean boolVal = clonedOption.getBooleanValue();
						setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, boolVal);
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPrefStore().setValue(setOption.getId(), boolVal);
//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//							fe.setPreferenceName(setOption.getId());
//						}
						break;
					case IOption.ENUMERATED :
						String enumVal = clonedOption.getStringValue();
						String enumId = clonedOption.getEnumeratedId(enumVal);
						setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, 
								(enumId != null && enumId.length() > 0) ? enumId : enumVal);
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPrefStore().setValue(setOption.getId(), enumVal);
//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//							fe.setPreferenceName(setOption.getId());
//					}
						break;
					case IOption.STRING :
						String strVal = clonedOption.getStringValue();
						setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, strVal);	
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPrefStore().setValue(setOption.getId(), strVal);
//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//							fe.setPreferenceName(setOption.getId());
//						}
						break;
					case IOption.STRING_LIST :
					case IOption.INCLUDE_PATH :
					case IOption.PREPROCESSOR_SYMBOLS :
					case IOption.LIBRARIES :
					case IOption.OBJECTS :
					case IOption.INCLUDE_FILES:
					case IOption.LIBRARY_PATHS:
					case IOption.LIBRARY_FILES:
					case IOption.MACRO_FILES:
					case IOption.UNDEF_INCLUDE_PATH:
					case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
					case IOption.UNDEF_INCLUDE_FILES:
					case IOption.UNDEF_LIBRARY_PATHS:
					case IOption.UNDEF_LIBRARY_FILES:
					case IOption.UNDEF_MACRO_FILES:	
						String[] listVal = (String[])((List)clonedOption.getValue()).toArray(new String[0]);
						setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, listVal);	
						
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPrefStore().setValue(setOption.getId(), listStr);
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
		Object[][] options = category.getOptions(fInfo.getParent(), optionHolder);
		// some option has changed on this page... update enabled/disabled state for all options

		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions) options[index][0];
			if (holder == null)
				break; //  The array may not be full
			IOption opt = (IOption) options[index][1];
			String prefName = getToolSettingsPrefStore().getOptionId(opt); 

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
			editor.load();
		}
	}
	
	private void setFieldEditorEnablement(IHoldsOptions holder, IOption option,
			IOptionApplicability optionApplicability, FieldEditor fieldEditor, Composite parent) {
		if (optionApplicability == null)
			return;

		// if the option is not enabled then disable it
		IBuildObject config = fInfo;
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
		String id = null;

		if(source instanceof FieldEditor){
			FieldEditor fe = (FieldEditor)source;
			
			id = fe.getPreferenceName();
			
			Object[] option = this.getToolSettingsPrefStore().getOption(id);
			
			if(option != null){
				changedOption = (IOption)option[1];
				changedHolder = (IHoldsOptions)option[0];
				try {
					switch(changedOption.getValueType()){
					case IOption.STRING:
						if(fe instanceof StringFieldEditor){
							String val = ((StringFieldEditor)fe).getStringValue();
							ManagedBuildManager.setOption(fInfo,changedHolder,changedOption,val);
						}
						break;
					case IOption.BOOLEAN:
						if(fe instanceof BooleanFieldEditor){
							boolean val = ((BooleanFieldEditor)fe).getBooleanValue();
							ManagedBuildManager.setOption(fInfo,changedHolder,changedOption,val);
						}
						break;
					case IOption.ENUMERATED:
						if(fe instanceof BuildOptionComboFieldEditor){
							String name = ((BuildOptionComboFieldEditor)fe).getSelection();
							String enumId = changedOption.getEnumeratedId(name);
							ManagedBuildManager.setOption(fInfo,changedHolder,changedOption,
									(enumId != null && enumId.length() > 0) ? enumId : name);
	
						}
						break;
					case IOption.INCLUDE_PATH:
					case IOption.STRING_LIST:
					case IOption.PREPROCESSOR_SYMBOLS:
					case IOption.LIBRARIES:
					case IOption.OBJECTS:
					case IOption.INCLUDE_FILES:
					case IOption.LIBRARY_PATHS:
					case IOption.LIBRARY_FILES:
					case IOption.MACRO_FILES:
					case IOption.UNDEF_INCLUDE_PATH:
					case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
					case IOption.UNDEF_INCLUDE_FILES:
					case IOption.UNDEF_LIBRARY_PATHS:
					case IOption.UNDEF_LIBRARY_FILES:
					case IOption.UNDEF_MACRO_FILES:
						if(fe instanceof FileListControlFieldEditor){
							String val[] =((FileListControlFieldEditor)fe).getStringListValue();
							ManagedBuildManager.setOption(fInfo, changedHolder, changedOption, val);
						}
						break;
					default:
						break;
					}
				} catch (BuildException e) {}
			}
		}

		Object[][] options = category.getOptions(fInfo.getParent(), optionHolder);

		// some option has changed on this page... update enabled/disabled state for all options

		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions) options[index][0];
			if (holder == null)
				break; //  The array may not be full
			IOption opt = (IOption) options[index][1];
			String optId = getToolSettingsPrefStore().getOptionId(opt); 

			// is the option on this page?
			if (fieldsMap.containsKey(optId)) {
				// check to see if the option has an applicability calculator
				IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();

				FieldEditor fieldEditor = (FieldEditor) fieldsMap.get(optId);
				try {
					if ( opt.getValueType() == IOption.ENUMERATED ) {
						// the item list of this enumerated option may have changed, update it
						updateEnumList( fieldEditor, opt, holder, fInfo );
					}
				} catch ( BuildException be ) {}
				
				if (applicabilityCalculator != null) {
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
	
	public void setValues() {
		updateFields();
	}

	/**
	 * The items shown in an enumerated option may depend on other option values.
	 * Whenever an option changes, check and update the valid enum values in
	 * the combo fieldeditor.
	 * 
	 * See also https://bugs.eclipse.org/bugs/show_bug.cgi?id=154053
	 * 
	 * @param fieldEditor enumerated combo fieldeditor
	 * @param opt         enumerated option type to update
	 * @param holder      the option holder
	 * @param config      project or resource info
	 * @throws BuildException
	 */
	protected void updateEnumList( FieldEditor fieldEditor, IOption opt, IHoldsOptions holder, IResourceInfo config ) throws BuildException	{
		// Get all applicable values for this enumerated Option, and filter out
		// the disable values
		String[] enumNames = opt.getApplicableValues();

		// get the currently selected enum value, the updated enum list may not contain
		// it, in that case a new value has to be selected
		String selectedEnum = opt.getSelectedEnum();
		String selectedEnumName = opt.getEnumName(selectedEnum);

		// get the default value for this enumerated option
		String defaultEnumId = (String)opt.getDefaultValue();
		String defaultEnumName = opt.getEnumName(defaultEnumId);

		boolean selectNewEnum = true;
		boolean selectDefault = false;

		Vector enumValidList = new Vector();
		for (int i = 0; i < enumNames.length; ++i) {
			if (opt.getValueHandler().isEnumValueAppropriate(config, 
					opt.getOptionHolder(), opt, opt.getValueHandlerExtraArgument(), enumNames[i])) {
				if ( selectedEnumName.equals(enumNames[i]) ) {
					// the currently selected enum is part of the new item list, no need to select a new value.
					selectNewEnum = false;
				}
				if ( defaultEnumName.equals(enumNames[i]) ) {
					// the default enum value is part of new item list
					selectDefault = true;
				}
				enumValidList.add(enumNames[i]);
			}
		}
		String[] enumValidNames = new String[enumValidList.size()];
		enumValidList.copyInto(enumValidNames);

		if ( selectNewEnum ) {
			// apparantly the currently selected enum value is not part anymore of the enum list
			// select a new value.
			String selection = null;
			if ( selectDefault ) {
				// the default enum value is part of the item list, use it
				selection = (String)opt.getDefaultValue();
			} else if ( enumValidNames.length > 0 ) {
				// select the first item in the item list
				selection = opt.getEnumeratedId(enumValidNames[0]);
			}
			ManagedBuildManager.setOption(config,holder,opt,selection);
		}
		((BuildOptionComboFieldEditor)fieldEditor).setOptions(enumValidNames);
		fieldEditor.load();
	}
}
