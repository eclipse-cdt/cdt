package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2004 IBM Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ToolReference;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.graphics.Point;

public class BuildToolSettingsPage extends BuildSettingsPage {
	// Field editor label
	private static final String COMMAND = "FieldEditors.tool.command"; //$NON-NLS-1$
	// option names that stores additional options
	private static final String COMPILER_FLAGS = ManagedBuilderUIMessages.getResourceString("BuildToolSettingsPage.compilerflags"); //$NON-NLS-1$
	private static final String LINKER_FLAGS = ManagedBuilderUIMessages.getResourceString("BuildToolSettingsPage.linkerflags"); //$NON-NLS-1$
	// all build options field editor label
	private static final String ALL_OPTIONS = ManagedBuilderUIMessages.getResourceString("BuildToolSettingsPage.alloptions"); //$NON-NLS-1$
	// Whitespace character
	private static final String WHITESPACE = " "; //$NON-NLS-1$
	// field editor that displays all the build options for a particular tool
	private MultiLineTextFieldEditor allOptionFieldEditor;
	private static final String DEFAULT_SEPERATOR = ";"; //$NON-NLS-1$
	// Map that holds all string options and its values
	private HashMap stringOptionsMap;
	// Map that holds all user object options and its values
	private HashMap userObjsMap;
	// Tool the settings belong to
	private ITool tool;
	// all build options preference store id
	private String allOptionsId = ""; //$NON-NLS-1$

	BuildToolSettingsPage(IConfiguration configuration, ITool tool) {
		// Cache the configuration and tool this page is for
		super(configuration);
		this.tool = tool;
		allOptionsId = tool.getId() + ".allOptions"; //$NON-NLS-1$
		stringOptionsMap = new HashMap();
		userObjsMap = new HashMap();
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
		StringFieldEditor stringField = new StringFieldEditor(tool.getId(),
				ManagedBuilderUIMessages.getResourceString(COMMAND),
				getFieldEditorParent());
		stringField.setEmptyStringAllowed(false);
		addField(stringField);
		// Add a field editor that displays over all build options
		allOptionFieldEditor = new MultiLineTextFieldEditor(allOptionsId,
				ALL_OPTIONS, getFieldEditorParent());
		getPreferenceStore().setValue(allOptionsId, ""); //$NON-NLS-1$
		addField(allOptionFieldEditor);
	}
	
	/**
	 * Update the field editor that displays all the build options
	 */
	public void updateAllOptionField() {
		try {
			if (getToolFlags() != null) {
				getPreferenceStore().setValue(allOptionsId, getToolFlags());
				allOptionFieldEditor.load();
			}
		} catch (BuildException e) {
		}
	}
	
	/**
	 * saves all field editors
	 */
	public void storeSettings() {
		super.performOk();
	}

	/**
	 * Returns all the build options string
	 * 
	 * @return String 
	 * @throws BuildException
	 */
	private String getToolFlags() throws BuildException {
		ITool[] tools = configuration.getTools();
		for (int i = 0; i < tools.length; ++i) {
			if (tools[i] instanceof ToolReference) {
				if (((ToolReference) tools[i]).references(tool)) {
					tool = tools[i];
					break;
				}
			} else if (tools[i].equals(tool))
				break;
		}
		StringBuffer buf = new StringBuffer();
		// get the options for this tool
		IOption[] options = tool.getOptions();
		String listStr = ""; //$NON-NLS-1$
		String[] listVal = null;
		for (int k = 0; k < options.length; k++) {
			IOption option = options[k];
			switch (option.getValueType()) {
				case IOption.BOOLEAN :
					if (getPreferenceStore().getBoolean(option.getId())) {
						buf.append(option.getCommand() + ITool.WHITE_SPACE);
					}
					break;
				case IOption.ENUMERATED :
					String enumCommand = getPreferenceStore().getString(
							option.getId());
					if (enumCommand.indexOf(DEFAULT_SEPERATOR) != -1)
						enumCommand = option.getSelectedEnum();
					String enum = option.getEnumCommand(enumCommand);
					if (enum.length() > 0) {
						buf.append(enum + ITool.WHITE_SPACE);
					}
					break;
				case IOption.STRING :
					String val = getPreferenceStore().getString(option.getId());
					// add this string option value to the list
					stringOptionsMap.put(option, val);
					if (val.length() > 0) {
						buf.append(val + ITool.WHITE_SPACE);
					}
					break;
				case IOption.STRING_LIST :
				case IOption.INCLUDE_PATH :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.LIBRARIES :
				case IOption.OBJECTS :
					String cmd = option.getCommand();
					listStr = getPreferenceStore().getString(option.getId());
					if (cmd == null)
						userObjsMap.put(option, listStr);
					listVal = BuildToolsSettingsStore.parseString(listStr);
					for (int j = 0; j < listVal.length; j++) {
						String temp = listVal[j];
						if (cmd != null)
							buf.append(cmd + temp + ITool.WHITE_SPACE);
						else
							buf.append(temp + ITool.WHITE_SPACE);
					}
					break;
				default :
					break;
			}
		}
		return buf.toString().trim();
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

	/* (non-Javadoc)
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
	 * This method parses the string that is entered in the all build option
	 * field editor and stores the options to the corresponding option fields.
	 */
	public void parseAllOptions() {
		ITool[] tools = configuration.getTools();
		for (int i = 0; i < tools.length; ++i) {
			if (tools[i] instanceof ToolReference) {
				if (((ToolReference) tools[i]).references(tool)) {
					tool = tools[i];
					break;
				}
			} else if (tools[i].equals(tool))
				break;
		}
		// Get the all build options string from all options field
		String alloptions = getPreferenceStore().getString(allOptionsId);
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
			IOption[] options = tool.getOptions();
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
					// check whether the option value is already exist
					// and also change the preference store based on
					// the option value
					switch (opt.getValueType()) {
						case IOption.BOOLEAN :
							if (opt.getCommand().equals(optionValue)) {
								getPreferenceStore()
										.setValue(opt.getId(), true);
								optionValueExist = true;
							}
							break;
						case IOption.ENUMERATED :
							String enum = ""; //$NON-NLS-1$
							String[] enumValues = opt.getApplicableValues();
							for (int i = 0; i < enumValues.length; i++) {
								if (opt.getEnumCommand(enumValues[i]).equals(
										optionValue)) {
									enum = enumValues[i];
									optionValueExist = true;
								}
							}
							if (!enum.equals("")) //$NON-NLS-1$
								getPreferenceStore()
										.setValue(opt.getId(), enum);
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
					getPreferenceStore().setValue(((IOption) key).getId(),
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
				String[] vals = BuildToolsSettingsStore.parseString(val);
				for (int t = 0; t < vals.length; t++) {
					if (alloptions.indexOf(vals[t]) != -1)
						list.add(vals[t]);
				}
				String listArr[] = new String[list.size()];
				list.toArray(listArr);
				String liststr = BuildToolsSettingsStore.createList(listArr);
				getPreferenceStore().setValue(((IOption) key).getId(), liststr);
			}
		}
		// Now update the preference store with parsed options
		// Get the options for this tool
		IOption[] options = tool.getOptions();
		for (int k = 0; k < options.length; ++k) {
			IOption opt = options[k];
			String name = opt.getId();
			String listStr = ""; //$NON-NLS-1$
			String[] listVal = null;
			switch (opt.getValueType()) {
				case IOption.BOOLEAN :
					ArrayList optsList = new ArrayList(/*Arrays.asList(*/optionsArr)/*)*/;
					if (opt.getCommand() != null
							&& !optsList.contains(opt.getCommand()))
						getPreferenceStore().setValue(opt.getId(), false);
					break;
				case IOption.STRING :
					// put the additional options in the compiler flag or
					// linker flag field
					if (opt.getName().equals(COMPILER_FLAGS)
							|| opt.getName().equals(LINKER_FLAGS)) {
						String newOptions = getPreferenceStore().getString(
								opt.getId());
						if (addnOptions.length() > 0) {
							newOptions = newOptions + ITool.WHITE_SPACE
									+ addnOptions.toString().trim();
						}
						getPreferenceStore().setValue(opt.getId(), newOptions);
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
					getPreferenceStore().setValue(opt.getId(),
							BuildToolsSettingsStore.createList(strlist));
					break;
				default :
					break;
			}
		}
	}

	/**
	 * Answers <code>true</code> if the receiver manages settings for the
	 * argument
	 * 
	 * @param tool
	 * @return
	 */
	public boolean isForTool(ITool tool) {
		if (tool != null) {
			return tool.equals(this.tool);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	public boolean performOk() {
		// Do the super-class thang
		boolean result =  super.performOk();
		
		//parse and store all build options in the corresponding preference store
		parseAllOptions();

		// Write the preference store values back to the build model
		ITool[] tools = configuration.getTools();
		for (int i = 0; i < tools.length; ++i) {
			if (tools[i] instanceof ToolReference) {
				if (((ToolReference) tools[i]).references(tool)) {
					tool = tools[i];
					break;
				}
			} else if (tools[i].equals(tool)) {
				break;
			}
		}
		IOption[] options = tool.getOptions();
		for (int i = 0; i < options.length; i++) {
			IOption option = options[i];
			// Transfer value from preference store to options
			switch (option.getValueType()) {
				case IOption.BOOLEAN :
					boolean boolVal = getPreferenceStore().getBoolean(option.getId());
					ManagedBuildManager.setOption(configuration, option, boolVal);
					break;
				case IOption.ENUMERATED :
					String enumVal = getPreferenceStore().getString(option.getId());
					String enumId = option.getEnumeratedId(enumVal);
					ManagedBuildManager.setOption(configuration, option, 
							(enumId != null && enumId.length() > 0) ? enumId : enumVal);
					break;
				case IOption.STRING :
					String strVal = getPreferenceStore().getString(option.getId());
					ManagedBuildManager.setOption(configuration, option, strVal);
					break;
				case IOption.STRING_LIST :
				case IOption.INCLUDE_PATH :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.LIBRARIES :
				case IOption.OBJECTS :
					String listStr = getPreferenceStore().getString(option.getId());
					String[] listVal = BuildToolsSettingsStore.parseString(listStr);
					ManagedBuildManager.setOption(configuration, option, listVal);
					break;
				default :
					break;
			}
		}
		
		// Get the actual value out of the field editor
		String command = getPreferenceStore().getString(tool.getId());
		if (command.length() == 0) {
			return result;
		}
		
		// Ask the build system manager to change the tool command
		ManagedBuildManager.setToolCommand(configuration, tool, command);
		
		return result;
	}
}
