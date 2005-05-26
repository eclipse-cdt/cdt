/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/
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
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.internal.macros.MbsMacroSupplier;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.jface.preference.StringFieldEditor;
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
	// all build options preference store id
	private String allOptionsId = ""; //$NON-NLS-1$
	// A list of safe options to put unrecognized values in
	private Vector defaultOptionNames;
	// Map that holds all string options and its values
	private HashMap stringOptionsMap;
	// Tool the settings belong to
	private ITool tool;
	// Map that holds all user object options and its values
	private HashMap userObjsMap;
	
	private boolean isItResourceConfigPage;

	//the build macro provider to be used for macro resolution
	private BuildMacroProvider fProvider;
	
	//macro substitutor used in the macro resolution in UI
	//resolves all macros except for the option-specific macros 
	//and the explicit file macros
	public class UIMacroSubstitutor extends DefaultMacroSubstitutor {
		private BuildMacroProvider fProvider;
		
		public UIMacroSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter, BuildMacroProvider provider){
			super(contextType,contextData,inexistentMacroValue,listDelimiter);
			fProvider = provider;
		}

		public UIMacroSubstitutor(IMacroContextInfo contextInfo, String inexistentMacroValue, String listDelimiter){
			super(contextInfo,inexistentMacroValue,listDelimiter);
		}
		
		protected IMacroContextInfo getMacroContextInfo(int contextType, Object contextData){
			if(fProvider != null)
				return fProvider.getMacroContextInfo(contextType,contextData);
			return super.getMacroContextInfo();
		}

		
		protected ResolvedMacro resolveMacro(IBuildMacro macro) throws BuildMacroException{
			if(macro instanceof MbsMacroSupplier.FileContextMacro){
				MbsMacroSupplier.FileContextMacro fileMacro = (MbsMacroSupplier.FileContextMacro)macro;
				if(fileMacro.isExplicit()){
					String name = macro.getName();
					return new ResolvedMacro(name,MacroResolver.createMacroReference(name));
				}
			} else if (macro instanceof MbsMacroSupplier.OptionMacro) {
				String name = macro.getName();
				return new ResolvedMacro(name,MacroResolver.createMacroReference(name));
			}
			return super.resolveMacro(macro);
		}

	}

	public BuildToolSettingsPage(IConfiguration configuration, ITool tool, BuildMacroProvider provider) {
		// Cache the configuration and tool this page is for
		super(configuration);
		this.tool = tool;
		allOptionsId = tool.getId() + ".allOptions"; //$NON-NLS-1$
		stringOptionsMap = new HashMap();
		userObjsMap = new HashMap();
		isItResourceConfigPage = false;
		fProvider = provider;
	}
	public BuildToolSettingsPage(IResourceConfiguration resConfig, ITool tool, BuildMacroProvider provider) {
		// Cache the configuration and tool this page is for
		super(resConfig);
		this.tool = tool;
		allOptionsId = tool.getId() + ".allOptions"; //$NON-NLS-1$
		stringOptionsMap = new HashMap();
		userObjsMap = new HashMap();
		isItResourceConfigPage = true;
		fProvider = provider;
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
		allOptionFieldEditor.getTextControl().setEditable(false);
		getToolSettingsPreferenceStore().setValue(allOptionsId, ""); //$NON-NLS-1$
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
	private String evaluateCommand( String command, String values ) {
	    if( command == null ) return values.trim();
	    if( command.indexOf( "${" ) >= 0 ) {	//$NON-NLS-1$ 
	    	return command.replaceAll( "\\$\\{[vV][aA][lL][uU][eE]\\}", values.trim() ).trim(); //$NON-NLS-1$
	    }
	    else {
	    	return (new String(command + values)).trim();
	    }
	}

	/**
	 * Returns all the build options string
	 * 
	 * @return String 
	 * @throws BuildException
	 */
	private String getToolFlags() throws BuildException {
		StringBuffer buf = new StringBuffer();
		ArrayList flags = new ArrayList();
		// get the options for this tool
		IOption[] options = tool.getOptions();
		String listStr = ""; //$NON-NLS-1$
		String[] listVal = null;
		IBuildObject parent = configuration != null ? (IBuildObject)configuration.getToolChain() : (IBuildObject)resConfig;
		IMacroSubstitutor macroSubstitutor = new UIMacroSubstitutor(0,null,EMPTY_STRING,WHITESPACE,fProvider); 
		for (int k = 0; k < options.length; k++) {
			IOption option = options[k];
			buf.setLength( 0 );

			// check to see if the option has an applicability calculator
			IOptionApplicability applicabilityCalculator = option.getApplicabilityCalculator();
			if (applicabilityCalculator == null || applicabilityCalculator.isOptionUsedInCommandLine(tool)) {
			
			try{
			switch (option.getValueType()) {
				case IOption.BOOLEAN :
					String boolCmd;
					if (getToolSettingsPreferenceStore().getBoolean(option.getId())) {
						boolCmd = option.getCommand();
					} else {
						// Note: getCommandFalse is new with CDT 2.0
						boolCmd = option.getCommandFalse();
					}
					if (boolCmd != null && boolCmd.length() > 0) {
						buf.append(boolCmd);
					}
					break;
				case IOption.ENUMERATED :
					String enumCommand = getToolSettingsPreferenceStore().getString(
							option.getId());
					if (enumCommand.indexOf(DEFAULT_SEPERATOR) != -1)
						enumCommand = option.getSelectedEnum();
					String enumeration = option.getEnumCommand(enumCommand);
					if (enumeration.length() > 0) {
						buf.append(enumeration);
					}
					break;
				case IOption.STRING :
					String strCmd = option.getCommand();
					String val = getToolSettingsPreferenceStore().getString(option.getId());
					// add this string option value to the list
					stringOptionsMap.put(option, val);
						macroSubstitutor.setMacroContextInfo(IBuildMacroProvider.CONTEXT_FILE,new FileContextData(null,null,option,parent));
						if (val.length() > 0 && (val = MacroResolver.resolveToString(val,macroSubstitutor)).length() > 0) {
					    buf.append(evaluateCommand( strCmd, val ));
					}
					break;
				case IOption.STRING_LIST :
				case IOption.INCLUDE_PATH :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.LIBRARIES :
				case IOption.OBJECTS :
					String cmd = option.getCommand();
					listStr = getToolSettingsPreferenceStore().getString(option.getId());
					if (cmd == null)
						userObjsMap.put(option, listStr);
						macroSubstitutor.setMacroContextInfo(IBuildMacroProvider.CONTEXT_FILE,new FileContextData(null,null,option,parent));
						listVal = MacroResolver.resolveStringListValues(BuildToolsSettingsStore.parseString(listStr),macroSubstitutor,true);
					for (int j = 0; j < listVal.length; j++) {
						String temp = listVal[j];
							if(temp.length() > 0)
						buf.append( evaluateCommand( cmd, temp ) + ITool.WHITE_SPACE);
					}
					break;
				default :
					break;
	
			}
			if( buf.toString().trim().length() > 0 ) flags.add( buf.toString().trim() );
			} catch (BuildMacroException e) {
				
			}
			
			}
		}
		
		String outputName = "temp";		//$NON-NLS-1$
		if (tool.getDefaultInputExtension() != null) { 
		    outputName += tool.getDefaultInputExtension();
		}
		String[] f = new String[ flags.size() ];
		String cmd = tool.getToolCommand();
		try{
			macroSubstitutor.setMacroContextInfo(IBuildMacroProvider.CONTEXT_FILE,new FileContextData(null,null,null,parent));
			String resolved = MacroResolver.resolveToString(cmd,macroSubstitutor);
			if ((resolved = resolved.trim()).length() > 0)
				cmd = resolved;
		} catch (BuildMacroException e) {
		}
		IManagedCommandLineGenerator gen = tool.getCommandLineGenerator();
        IManagedCommandLineInfo info = gen.generateCommandLineInfo( tool, cmd, (String[])flags.toArray( f ), 
                tool.getOutputFlag(), tool.getOutputPrefix(), outputName, new String[0], tool.getCommandLinePattern() );
		return info.getFlags();
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
	
	/**
	 * This method parses the string that is entered in the all build option
	 * field editor and stores the options to the corresponding option fields.
	 */
	public void parseAllOptions() {
		// Get the all build options string from all options field
		String alloptions = getToolSettingsPreferenceStore().getString(allOptionsId);
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
					try {
						// check whether the option value is already exist
						// and also change the preference store based on
						// the option value
						switch (opt.getValueType()) {
							case IOption.BOOLEAN :
								String boolCommand;
								boolCommand = opt.getCommand();
								if (boolCommand != null && boolCommand.equals(optionValue)) {
									getToolSettingsPreferenceStore()
											.setValue(opt.getId(), true);
									optionValueExist = true;
								}
								boolCommand = opt.getCommandFalse();
								if (boolCommand != null && boolCommand.equals(optionValue)) {
									getToolSettingsPreferenceStore()
											.setValue(opt.getId(), false);
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
									getToolSettingsPreferenceStore()
											.setValue(opt.getId(), enumeration);
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
					getToolSettingsPreferenceStore().setValue(((IOption) key).getId(),
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
				getToolSettingsPreferenceStore().setValue(((IOption) key).getId(), liststr);
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
			try {
				switch (opt.getValueType()) {
					case IOption.BOOLEAN :
						ArrayList optsList = new ArrayList(optionsArr);
						if (opt.getCommand() != null 
								&& opt.getCommand().length() > 0  
								&& !optsList.contains(opt.getCommand()))
							getToolSettingsPreferenceStore().setValue(opt.getId(), false);
						if (opt.getCommandFalse() != null 
								&& opt.getCommandFalse().length() > 0  
								&& !optsList.contains(opt.getCommandFalse()))
							getToolSettingsPreferenceStore().setValue(opt.getId(), true);
						break;
					case IOption.STRING :
						// TODO create a lst of valid default string options for the tool
						if (getDefaultOptionNames().contains(opt.getName())) {
							String newOptions = getToolSettingsPreferenceStore().getString(
									opt.getId());
							if (addnOptions.length() > 0) {
								newOptions = newOptions + ITool.WHITE_SPACE
										+ addnOptions.toString().trim();
							}
							getToolSettingsPreferenceStore().setValue(opt.getId(), newOptions);
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
						getToolSettingsPreferenceStore().setValue(opt.getId(),
								BuildToolsSettingsStore.createList(strlist));
						break;
					default :
						break;
				}
			} catch (BuildException e) {}
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
		IOptionCategory category = (IOptionCategory)tool;
		Object[][] options;
		if ( isItResourceConfigPage ) {
			options = category.getOptions(resConfig);
		} else {
			options = category.getOptions(configuration);
		}
		if ( options == null)
			return true;
		
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
						setOption = ManagedBuildManager.setOption(configuration, tool, option, boolVal);
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), boolVal);
						}
						break;
					case IOption.ENUMERATED :
						String enumVal = getToolSettingsPreferenceStore().getString(option.getId());
						String enumId = option.getEnumeratedId(enumVal);
						setOption = ManagedBuildManager.setOption(configuration, tool, option, 
							(enumId != null && enumId.length() > 0) ? enumId : enumVal);
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), enumVal);
						}
						break;
					case IOption.STRING :
						String strVal = getToolSettingsPreferenceStore().getString(option.getId());
						setOption = ManagedBuildManager.setOption(configuration, tool, option, strVal);
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), strVal);
						}
						break;
					case IOption.STRING_LIST :
					case IOption.INCLUDE_PATH :
					case IOption.PREPROCESSOR_SYMBOLS :
					case IOption.LIBRARIES :
					case IOption.OBJECTS :
						String listStr = getToolSettingsPreferenceStore().getString(option.getId());
						String[] listVal = BuildToolsSettingsStore.parseString(listStr);
						setOption = ManagedBuildManager.setOption(configuration, tool, option, listVal);
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), listStr);
						}
						break;
					default :
						break;
				}
			} catch (BuildException e) {}
		}
		
		// Save the tool command if it has changed
		// Get the actual value out of the field editor
		String command = getToolSettingsPreferenceStore().getString(tool.getId());
		if (command.length() > 0 &&
			(!command.equals(tool.getToolCommand()))) {
			if ( isItResourceConfigPage ) {
				ManagedBuildManager.setToolCommand(resConfig, tool, command);
			} else {
				ManagedBuildManager.setToolCommand(configuration, tool, command);
			}
		}
		
		return result;
	}
	
	/**
	 * saves all field editors
	 */
	public void storeSettings() {
		super.performOk();
	}
	
	/**
	 * Update the field editor that displays all the build options
	 */
	public void updateAllOptionField() {
		try {
			String flags = getToolFlags();
			if (flags != null) {
				getToolSettingsPreferenceStore().setValue(allOptionsId, flags);
				allOptionFieldEditor.load();
			}
		} catch (BuildException e) {
		}
	}
}
