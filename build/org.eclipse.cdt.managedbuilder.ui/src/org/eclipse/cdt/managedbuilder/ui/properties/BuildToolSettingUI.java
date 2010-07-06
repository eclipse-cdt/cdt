/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.core.MultiResourceInfo;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.MultiLineTextFieldEditor;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class BuildToolSettingUI extends AbstractToolSettingUI {
	
	//  Label class for a preference page.
	class LabelFieldEditor extends FieldEditor {
		private String fTitle;
		private Label fTitleLabel;

		public LabelFieldEditor( Composite parent, String title ) {
			fTitle = title;
			this.createControl( parent );
		}

		@Override
		protected void adjustForNumColumns( int numColumns ) {
			((GridData)fTitleLabel.getLayoutData()).horizontalSpan = 2;
		}

		@Override
		protected void doFillIntoGrid( Composite parent, int numColumns ) {
			fTitleLabel = new Label( parent, SWT.WRAP );
			fTitleLabel.setText( fTitle );
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			gd.grabExcessHorizontalSpace = false;
			gd.horizontalSpan = 2;
			fTitleLabel.setLayoutData( gd );
		}

		@Override
		public int getNumberOfControls() {	return 1; }
		/**
		 * The label field editor is only used to present a text label on a preference page.
		 */
		@Override
		protected void doLoad() {}
		@Override
		protected void doLoadDefault() {}
		@Override
		protected void doStore() {}
	}

	// Data members
	
	// all build options field editor label
	private static final String ALL_OPTIONS = Messages.BuildToolSettingsPage_alloptions;

	private static final String DEFAULT_SEPERATOR = ";"; //$NON-NLS-1$
	// Whitespace character
	private static final String WHITESPACE = " "; //$NON-NLS-1$
	// Empty String
	//private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	// field editor that displays all the build options for a particular tool
	private MultiLineTextFieldEditor allOptionFieldEditor;
	//tool command field
	private StringFieldEditor commandStringField;
	//tool command-line-pattern field
	private StringFieldEditor commandLinePatternField;
	// A list of safe options to put unrecognized values in
	private Vector<String> defaultOptionNames;
	// Map that holds all string options and its values
	private HashMap<String, String> stringOptionsMap;

	private ITool fTool;
	// Map that holds all user object options and its values
	private HashMap<IOption, String> userObjsMap;
	
	public BuildToolSettingUI(AbstractCBuildPropertyTab page,
			IResourceInfo info, ITool _tool) {
		// Cache the configuration and tool this page is for
		//TODO: 
		super(info);
		this.fTool = _tool;
		buildPropPage = page;
		stringOptionsMap = new HashMap<String, String>();
		userObjsMap = new HashMap<IOption, String>();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#computeSize()
	 */
	@Override
	public Point computeSize() {
		return super.computeSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		
		// Load up the preference store
		super.createFieldEditors();
		// Add a string editor to edit the tool command
		Composite parent = getFieldEditorParent();
		FontMetrics fm = AbstractCPropertyTab.getFontMetrics(parent);
		commandStringField = new StringFieldEditor(fTool.getId(),
				Messages.BuildToolSettingsPage_tool_command,
				parent);
		commandStringField.setEmptyStringAllowed(false);
		GridData gd = ((GridData)commandStringField.getTextControl(parent).getLayoutData());
		gd.grabExcessHorizontalSpace = true;
		gd.minimumWidth = Dialog.convertWidthInCharsToPixels(fm, 3);
		addField(commandStringField);
		// Add a field editor that displays overall build options
		Composite par = getFieldEditorParent();
		allOptionFieldEditor = new MultiLineTextFieldEditor(ToolSettingsPrefStore.ALL_OPTIONS_ID,
				ALL_OPTIONS, par);
		allOptionFieldEditor.getTextControl(par).setEditable(false);
//		gd = ((GridData)allOptionFieldEditor.getTextControl().getLayoutData());
		gd.grabExcessHorizontalSpace = true;
		gd.minimumWidth = Dialog.convertWidthInCharsToPixels(fm, 20);
		addField(allOptionFieldEditor);
		
		addField( createLabelEditor( getFieldEditorParent(), WHITESPACE ) );
		addField( createLabelEditor( getFieldEditorParent(), Messages.BuildToolSettingsPage_tool_advancedSettings ) );
		
		// Add a string editor to edit the tool command line pattern
		parent = getFieldEditorParent();
		commandLinePatternField = new StringFieldEditor(fTool.getId() + ToolSettingsPrefStore.COMMAND_LINE_SUFFIX,
				Messages.BuildToolSettingsPage_tool_commandLinePattern,
				parent);
		gd = ((GridData)commandLinePatternField.getTextControl(parent).getLayoutData());
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint =  Dialog.convertWidthInCharsToPixels(fm,30);
		gd.minimumWidth = Dialog.convertWidthInCharsToPixels(fm, 20);
		addField(commandLinePatternField);
	}		

	protected FieldEditor createLabelEditor( Composite parent, String title ) {
		return new LabelFieldEditor( parent, title );
	}

	/**
	 * @return
	 */
	private Vector<String> getDefaultOptionNames() {
		if (defaultOptionNames == null) {
			defaultOptionNames = new Vector<String>();
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
	private Vector<String> getOptionVector(String rawOptionString){
		Vector<String> tokens = new Vector<String>(Arrays.asList(rawOptionString.split("\\s")));	//$NON-NLS-1$
		Vector<String> output = new Vector<String>(tokens.size());

		Iterator<String> iter = tokens.iterator();
		while(iter.hasNext()){
			String token = iter.next();
			int firstIndex = token.indexOf("\"");	//$NON-NLS-1$
			int lastIndex = token.lastIndexOf("\"");	//$NON-NLS-1$
			if (firstIndex != -1 && firstIndex == lastIndex) {
				// Keep looking
				while (iter.hasNext()) {
					String nextToken = iter.next();
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
	 * @return <code>true</code> if the receiver manages settings for the
	 * argument
	 * 
	 * @param tool
	 * 
	 */
	@Override
	public boolean isFor(Object tool, Object unused) {
		if (tool != null && tool instanceof ITool && unused == null) {
			return tool.equals(fTool);
		}
		return false;
	}
	
	/**
	 * This method parses the string that is entered in the all build option
	 * field editor and stores the options to the corresponding option fields.
	 */
	public void parseAllOptions() {
		// Get the all build options string from all options field
		String alloptions = getToolSettingsPrefStore().getString(ToolSettingsPrefStore.ALL_OPTIONS_ID);
		// list that holds the options for the option type other than
		// boolean,string and enumerated
		List<String> optionsList = new ArrayList<String>();
		// additional options buffer
		StringBuffer addnOptions = new StringBuffer();
		// split all build options string
		Vector<String> optionsArr = getOptionVector(alloptions);
		for (String optionValue : optionsArr) {
			boolean optionValueExist = false;
			// get the options for this tool
			IOption[] options = fTool.getOptions();
			for (IOption opt : options) {
				//String name = opt.getId();
				// check whether the option value is "STRING" type
				for (String s : stringOptionsMap.values()) {
					if (s.indexOf(optionValue) != -1)
						optionValueExist = true;
				}
				// check whether the option value is "OBJECTS" type
				for (String s : userObjsMap.values()) {
					if (s.indexOf(optionValue) != -1)
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
		Set<String> set = stringOptionsMap.keySet();
		for (int i = 0; i < set.size(); i++) {
			Iterator<String> iterator = set.iterator();
			while (iterator.hasNext()) {
				Object key = iterator.next();
				String val = stringOptionsMap.get(key);
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
		Set<IOption> objSet = userObjsMap.keySet();
		for (int s = 0; s < objSet.size(); s++) {
			for (IOption op : objSet) {
				String val = userObjsMap.get(op);
				ArrayList<String> list = new ArrayList<String>();
				for (String v : parseString(val)) {
					if (alloptions.indexOf(v) != -1)
						list.add(v);
				}
				String listArr[] = new String[list.size()];
				list.toArray(listArr);
				setOption(op, listArr);
			}
		}
		// Now update the preference store with parsed options
		// Get the options for this tool
		IOption[] options = fTool.getOptions();
		for (int k = 0; k < options.length; ++k) {
			IOption opt = options[k];
			//String name = opt.getId();
			//String listStr = ""; //$NON-NLS-1$
			//String[] listVal = null;
			try {
				switch (opt.getValueType()) {
					case IOption.BOOLEAN :
						ArrayList<String> optsList = new ArrayList<String>(optionsArr);
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
						ArrayList<String> newList = new ArrayList<String>();
						for (String s : optionsList) {
							if (opt.getCommand() != null
									&& s.startsWith(opt.getCommand())) {
								newList.add(s.substring(opt.getCommand().length()));
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
			fInfo.setOption(fTool,option,value);
		} catch (BuildException e){}
	}
	
	protected void setOption(IOption option, String value){
		try{ 
			fInfo.setOption(fTool,option,value);
		} catch (BuildException e){}
	}

	protected void setOption(IOption option, String value[]){
		try{
			fInfo.setOption(fTool,option,value);
		} catch (BuildException e){	}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		return super.performOk();
	}
	
	/**
	 * Update the field editor that displays all the build options
	 */
	@Override
	public void updateFields() {
		allOptionFieldEditor.load();
	}
	
	@Override
	public void setValues(){
		commandStringField.load();
		commandLinePatternField.load();
		updateFields();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// allow superclass to handle as well
		super.propertyChange(event);
		
		if (fInfo instanceof MultiResourceInfo) {
			MultiResourceInfo mri = (MultiResourceInfo)fInfo;
			if(event.getSource() == commandStringField){
				mri.setToolsCommand(fTool, commandStringField.getStringValue());
				updateFields();
			}
			else if(event.getSource() == commandLinePatternField){
				mri.setCommandLinePattern(fTool, commandLinePatternField.getStringValue());
			}
		} else {
			if(event.getSource() == commandStringField){
				fTool.setToolCommand(commandStringField.getStringValue());
				updateFields();
			}
			else if(event.getSource() == commandLinePatternField){
				fTool.setCommandLinePattern(commandLinePatternField.getStringValue());
			}
		}
	}
}
