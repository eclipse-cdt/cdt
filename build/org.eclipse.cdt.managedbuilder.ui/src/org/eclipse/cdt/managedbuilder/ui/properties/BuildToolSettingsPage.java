/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.internal.macros.MbsMacroSupplier;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Point;

public class BuildToolSettingsPage extends BuildSettingsPage {
	// all build options field editor label
	private static final String ALL_OPTIONS = ManagedBuilderUIMessages.getResourceString("BuildToolSettingsPage.alloptions"); //$NON-NLS-1$
	// Field editor label
	private static final String COMMAND = "FieldEditors.tool.command"; //$NON-NLS-1$

	private static final String DEFAULT_SEPERATOR = ";"; //$NON-NLS-1$
	// Whitespace character
	private static final String WHITESPACE = " "; //$NON-NLS-1$
	// Empty String
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	// field editor that displays all the build options for a particular tool
	private MultiLineTextFieldEditor allOptionFieldEditor;
	//tool command command
	private StringFieldEditor commandStringField;
	// A list of safe options to put unrecognized values in
	private Vector defaultOptionNames;
	// Map that holds all string options and its values
	private HashMap stringOptionsMap;

	private ITool clonedTool;
	// Map that holds all user object options and its values
	private HashMap userObjsMap;
	
	private boolean isItResourceConfigPage;

	private AbstractBuildPropertyPage buildPropPage;
	
	public BuildToolSettingsPage(AbstractBuildPropertyPage page,
			IConfiguration clonedCfg, ITool clonedTool) {
		// Cache the configuration and tool this page is for
		super(clonedCfg);
		this.clonedTool = clonedTool;
		buildPropPage = page;
		stringOptionsMap = new HashMap();
		userObjsMap = new HashMap();
		isItResourceConfigPage = false;
	}
	
	public BuildToolSettingsPage(AbstractBuildPropertyPage page,
			IResourceConfiguration clonedRcCfg, ITool clonedTool) {
		// Cache the configuration and tool this page is for
		super(clonedRcCfg);
		this.clonedTool = clonedTool;
		buildPropPage = page;
		stringOptionsMap = new HashMap();
		userObjsMap = new HashMap();
		isItResourceConfigPage = true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#computeSize()
	 */
	public Point computeSize() {
		return super.computeSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		// Load up the preference store
		super.createFieldEditors();
		// Add a string editor to edit the tool command
		commandStringField = new StringFieldEditor(clonedTool.getId(),
				ManagedBuilderUIMessages.getResourceString(COMMAND),
				getFieldEditorParent());
		commandStringField.setEmptyStringAllowed(false);
		addField(commandStringField);
		// Add a field editor that displays over all build options
		allOptionFieldEditor = new MultiLineTextFieldEditor(BuildToolSettingsPreferenceStore.ALL_OPTIONS_ID,
				ALL_OPTIONS, getFieldEditorParent());
		allOptionFieldEditor.getTextControl().setEditable(false);
		addField(allOptionFieldEditor);
	}

	/**
	 * Creates single string from the string array with a separator
	 * 
	 * @param items
	 * @return
	 */
	private String createList(String[] items) {
		StringBuffer path = new StringBuffer(""); //$NON-NLS-1$
		for (int i = 0; i < items.length; i++) {
			path.append(items[i]);
			if (i < (items.length - 1)) {
				path.append(DEFAULT_SEPERATOR);
			}
		}
		return path.toString();
	}
	
	/**
	 * @return
	 */
	private Vector getDefaultOptionNames() {
		if (defaultOptionNames == null) {
			defaultOptionNames = new Vector();
			defaultOptionNames.add("Other flags"); //$NON-NLS-1$
			defaultOptionNames.add("Linker flags"); //$NON-NLS-1$
			defaultOptionNames.add("Archiver flags"); //$NON-NLS-1$
			defaultOptionNames.add("Assembler flags"); //$NON-NLS-1$
		}
		return defaultOptionNames;
	}

	/**
	 * The raw option string can contain path information that has spaces 
	 * in them. For example, 
	 * <p> <pre>
	 * -I"path\with a\couple of spaces" -O2 -g -fPIC
	 * </pre><p>
	 * would yeild
	 * <p><pre>-I"path\with | a\couple | of | spaces" | -O2 | -g | -fPIC</pre>
	 * <p>
	 * As you can see, simply splitting at the whitespaces will yeild a result 
	 * containing garbage, so the logic of this method must consider whether a 
	 * token contains the &quot; character. If so, then add it and all of the 
	 * subsequent tokens until the enclosing &quot; is found. 
	 *  
	 * @param rawOptionString
	 * @return Vector containing all options
	 */
	private Vector getOptionVector(String rawOptionString){
		Vector tokens = new Vector(Arrays.asList(rawOptionString.split("\\s")));	//$NON-NLS-1$
		Vector output = new Vector(tokens.size());

		Iterator iter = tokens.iterator();
		while(iter.hasNext()){
			String token = (String)iter.next();
			int firstIndex = token.indexOf("\"");	//$NON-NLS-1$
			int lastIndex = token.lastIndexOf("\"");	//$NON-NLS-1$
			if (firstIndex != -1 && firstIndex == lastIndex) {
				// Keep looking
				while (iter.hasNext()) {
					String nextToken = (String) iter.next();
					token += WHITESPACE + nextToken;
					if (nextToken.indexOf("\"") != -1) break;	//$NON-NLS-1$
				}
			}
			output.add(token);
		}
		
		return output;
	}
	
	/**
	 * Look for ${VALUE} in the command string
	 */
	//private String evaluateCommand( String command, String values ) {
	//	return (((Tool)clonedTool).evaluateCommand(command, values));
	//}

	/**
	 * Answers <code>true</code> if the receiver manages settings for the
	 * argument
	 * 
	 * @param tool
	 * @return
	 */
	public boolean isForTool(ITool tool) {
		if (tool != null) {
			return tool.equals(this.clonedTool);
		}
		return false;
	}
	
	/**
	 * This method parses the string that is entered in the all build option
	 * field editor and stores the options to the corresponding option fields.
	 */
	public void parseAllOptions() {
		// Get the all build options string from all options field
		String alloptions = getToolSettingsPreferenceStore().getString(BuildToolSettingsPreferenceStore.ALL_OPTIONS_ID);
		// list that holds the options for the option type other than
		// boolean,string and enumerated
		List optionsList = new ArrayList();
		// additional options buffer
		StringBuffer addnOptions = new StringBuffer();
		// split all build options string
		Vector optionsArr = getOptionVector(alloptions);
		Iterator optIter = optionsArr.iterator();
		while(optIter.hasNext()) {
			String optionValue = (String)optIter.next();
			boolean optionValueExist = false;
			// get the options for this tool
			IOption[] options = clonedTool.getOptions();
			for (int k = 0; k < options.length; ++k) {
				IOption opt = options[k];
				String name = opt.getId();
				// check whether the option value is "STRING" type
				Iterator stringOptsIter = stringOptionsMap.values().iterator();
				while (stringOptsIter.hasNext()) {
					if (((String) stringOptsIter.next()).indexOf(optionValue) != -1)
						optionValueExist = true;
				}
				// check whether the option value is "OBJECTS" type
				Iterator userObjsIter = userObjsMap.values().iterator();
				while (userObjsIter.hasNext()) {
					if (((String) userObjsIter.next()).indexOf(optionValue) != -1)
						optionValueExist = true;
				}
				// if the value does not exist in string option or user objects
				// option
				if (!optionValueExist) {
					try {
						// check whether the option value is already exist
						// and also change the preference store based on
						// the option value
						switch (opt.getValueType()) {
							case IOption.BOOLEAN :
								String boolCommand;
								boolCommand = opt.getCommand();
								if (boolCommand != null && boolCommand.equals(optionValue)) {
											setOption(opt, true);
									optionValueExist = true;
								}
								boolCommand = opt.getCommandFalse();
								if (boolCommand != null && boolCommand.equals(optionValue)) {
											setOption(opt, false);
									optionValueExist = true;
								}
								break;
							case IOption.ENUMERATED :
								String enumeration = ""; //$NON-NLS-1$
								String[] enumValues = opt.getApplicableValues();
								for (int i = 0; i < enumValues.length; i++) {
									if (opt.getEnumCommand(enumValues[i]).equals(
											optionValue)) {
										enumeration = enumValues[i];
										optionValueExist = true;
									}
								}
								if (!enumeration.equals("")) //$NON-NLS-1$
											setOption(opt, enumeration);
								break;
							case IOption.STRING_LIST :
							case IOption.INCLUDE_PATH :
							case IOption.PREPROCESSOR_SYMBOLS :
							case IOption.LIBRARIES :
								if (opt.getCommand() != null
										&& optionValue.startsWith(opt
												.getCommand())) {
									optionsList.add(optionValue);
									optionValueExist = true;
								}
								break;
							default :
								break;
						}
					} catch (BuildException e) {}
				}
			}
			// If the parsed string does not match with any previous option
			// values then consider this option as a additional build option
			if (!optionValueExist) {
				addnOptions.append(optionValue + ITool.WHITE_SPACE);
			}
		}
		// check whether some of the "STRING" option value or "OBJECTS" type
		// option value removed
		// by the user from all build option field
		Set set = stringOptionsMap.keySet();
		for (int s = 0; s < set.size(); s++) {
			Iterator iterator = set.iterator();
			while (iterator.hasNext()) {
				Object key = iterator.next();
				String val = (String) stringOptionsMap.get(key);
				if (alloptions.indexOf(val) == -1) {
					StringBuffer buf = new StringBuffer();
					String[] vals = val.split(WHITESPACE);
					for (int t = 0; t < vals.length; t++) {
						if (alloptions.indexOf(vals[t]) != -1)
							buf.append(vals[t] + ITool.WHITE_SPACE);
					}
					setOption(((IOption) key),
							buf.toString().trim());
				}
			}
		}
		// "OBJECTS" type
		Set objSet = userObjsMap.keySet();
		for (int s = 0; s < objSet.size(); s++) {
			Iterator iterator = objSet.iterator();
			while (iterator.hasNext()) {
				Object key = iterator.next();
				String val = (String) userObjsMap.get(key);
				ArrayList list = new ArrayList();
				String[] vals = parseString(val);
				for (int t = 0; t < vals.length; t++) {
					if (alloptions.indexOf(vals[t]) != -1)
						list.add(vals[t]);
				}
				String listArr[] = new String[list.size()];
				list.toArray(listArr);
				setOption(((IOption) key), listArr);
			}
		}
		// Now update the preference store with parsed options
		// Get the options for this tool
		IOption[] options = clonedTool.getOptions();
		for (int k = 0; k < options.length; ++k) {
			IOption opt = options[k];
			String name = opt.getId();
			String listStr = ""; //$NON-NLS-1$
			String[] listVal = null;
			try {
				switch (opt.getValueType()) {
					case IOption.BOOLEAN :
						ArrayList optsList = new ArrayList(optionsArr);
						if (opt.getCommand() != null 
								&& opt.getCommand().length() > 0  
								&& !optsList.contains(opt.getCommand()))
							setOption(opt, false);
						if (opt.getCommandFalse() != null 
								&& opt.getCommandFalse().length() > 0  
								&& !optsList.contains(opt.getCommandFalse()))
							setOption(opt, true);
						break;
					case IOption.STRING :
						// TODO create a lst of valid default string options for the tool
						if (getDefaultOptionNames().contains(opt.getName())) {
							String newOptions = opt.getStringValue();
							if (addnOptions.length() > 0) {
								newOptions = newOptions + ITool.WHITE_SPACE
										+ addnOptions.toString().trim();
							}
							setOption(opt, newOptions);
						}
						break;
					case IOption.STRING_LIST :
					case IOption.INCLUDE_PATH :
					case IOption.PREPROCESSOR_SYMBOLS :
					case IOption.LIBRARIES :
						ArrayList newList = new ArrayList();
						for (int i = 0; i < optionsList.size(); i++) {
							if (opt.getCommand() != null
									&& ((String) optionsList.get(i)).startsWith(opt
											.getCommand())) {
								newList.add(((String) optionsList.get(i))
										.substring(opt.getCommand().length()));
							}
						}
						String[] strlist = new String[newList.size()];
						newList.toArray(strlist);
						newList.clear();
						setOption(opt, strlist);
						break;
					default :
						break;
				}
			} catch (BuildException e) {}
		}
	}

	public static String[] parseString(String stringList) {
		if (stringList == null || stringList.length() == 0) {
			return new String[0];
		} else {
			return stringList.split(DEFAULT_SEPERATOR);
		}
	}

	protected void setOption(IOption option, boolean value){
		try{
			if(isItResourceConfigPage)
				clonedResConfig.setOption(clonedTool,option,value);
			else
				clonedConfig.setOption(clonedTool,option,value);
		} catch (BuildException e){
		}
	}
	
	protected void setOption(IOption option, String value){
		try{
			if(isItResourceConfigPage)
				clonedResConfig.setOption(clonedTool,option,value);
			else
				clonedConfig.setOption(clonedTool,option,value);
		} catch (BuildException e){
		}
	}

	protected void setOption(IOption option, String value[]){
		try{
			if(isItResourceConfigPage)
				clonedResConfig.setOption(clonedTool,option,value);
			else
				clonedConfig.setOption(clonedTool,option,value);
		} catch (BuildException e){
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	public boolean performOk() {
		// Do the super-class thang
		boolean result =  super.performOk();
		
		//parse and store all build options in the corresponding preference store
		//parseAllOptions();

		// Write the preference store values back to the build model
		IOptionCategory clonedCategory = (IOptionCategory)clonedTool;
		ITool tool = buildPropPage.getRealTool(clonedTool);
		if(tool == null)
			return false;
		Object[][] clonedOptions;
		IResourceConfiguration realRcCfg = null;
		IConfiguration realCfg = null;
//		IBuildObject handler = null;

		if (isItResourceConfigPage){
			realRcCfg = buildPropPage.getRealRcConfig(clonedResConfig);
			if(realRcCfg == null)
				return false;
//			handler = realRcCfg;
			clonedOptions = clonedCategory.getOptions(clonedResConfig);
		} else {
			realCfg = buildPropPage.getRealConfig(clonedConfig);
			if(realCfg == null)
				return false;
//			handler = realCfg;
			clonedOptions = clonedCategory.getOptions(clonedConfig);
		}

		if ( clonedOptions == null)
			return true;
		
		for (int i = 0; i < clonedOptions.length; i++) {
			ITool clonedTool = (ITool)clonedOptions[i][0];
			if (clonedTool == null) break;	//  The array may not be full
			IOption clonedOption = (IOption)clonedOptions[i][1];
			
			ITool realTool = buildPropPage.getRealTool(clonedTool);
			if(realTool == null) continue;
			IOption realOption = buildPropPage.getRealOption(clonedOption, clonedTool);
			if(realOption == null) continue;

			
			try {
				// Transfer value from preference store to options
				IOption setOption = null;
				switch (clonedOption.getValueType()) {
					case IOption.BOOLEAN :
						boolean boolVal = clonedOption.getBooleanValue();;
						setOption = ManagedBuildManager.setOption(realCfg, realTool, realOption, boolVal);
						// Reset the preference store since the Id may have changed
	//					if (setOption != option) {
	//						getToolSettingsPreferenceStore().setValue(setOption.getId(), boolVal);
	//					}
						break;
					case IOption.ENUMERATED :
						String enumVal = clonedOption.getStringValue();
						String enumId = clonedOption.getEnumeratedId(enumVal);
						setOption = ManagedBuildManager.setOption(realCfg, realTool, realOption, 
							(enumId != null && enumId.length() > 0) ? enumId : enumVal);
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPreferenceStore().setValue(setOption.getId(), enumVal);
//						}
						break;
					case IOption.STRING :
						String strVal = clonedOption.getStringValue();
						setOption = ManagedBuildManager.setOption(realCfg, realTool, realOption, strVal);
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPreferenceStore().setValue(setOption.getId(), strVal);
//						}
						break;
					case IOption.STRING_LIST :
					case IOption.INCLUDE_PATH :
					case IOption.PREPROCESSOR_SYMBOLS :
					case IOption.LIBRARIES :
					case IOption.OBJECTS :
//						String listStr = getToolSettingsPreferenceStore().getString(option.getId());
						String[] listVal = (String[])((List)clonedOption.getValue()).toArray(new String[0]);
						setOption = ManagedBuildManager.setOption(realCfg, realTool, realOption, listVal);
						// Reset the preference store since the Id may have changed
//						if (setOption != option) {
//							getToolSettingsPreferenceStore().setValue(setOption.getId(), listStr);
//						}
						break;
					default :
						break;
				}

				// Call an MBS CallBack function to inform that Settings related to Apply/OK button 
				// press have been applied.
				if (setOption == null)
					setOption = realOption;
/*				
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
*/ 
			} catch (BuildException e) {
			} catch (ClassCastException e) {
			}
		}
		
		// Save the tool command if it has changed
		// Get the actual value out of the field editor
		String command = clonedTool.getToolCommand();
		if (command.length() > 0 &&
			(!command.equals(tool.getToolCommand()))) {
			if ( isItResourceConfigPage ) {
				ManagedBuildManager.setToolCommand(realRcCfg, tool, command);
			} else {
				ManagedBuildManager.setToolCommand(realCfg, tool, command);
			}
		}
		
		return result;
	}
	
	/**
	 * saves all field editors
	 */
	public void storeSettings() {
//		super.performOk();
	}
	
	/**
	 * Update the field editor that displays all the build options
	 */
	public void updateAllOptionField() {
		allOptionFieldEditor.load();
	}
	
	public void setValues(){
		commandStringField.load();
		updateAllOptionField();
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		// allow superclass to handle as well
		super.propertyChange(event);
		
		if(event.getSource() == commandStringField){
			clonedTool.setToolCommand(commandStringField.getStringValue());
			updateAllOptionField();
		}
	}
}
