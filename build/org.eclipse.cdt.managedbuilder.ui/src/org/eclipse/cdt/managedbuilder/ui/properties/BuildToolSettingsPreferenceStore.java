/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Option;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.internal.macros.MbsMacroSupplier;
import org.eclipse.cdt.managedbuilder.internal.ui.ToolsSettingsBlock;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;

public class BuildToolSettingsPreferenceStore implements IPreferenceStore {
	public static final String DEFAULT_SEPERATOR = ";"; //$NON-NLS-1$
	private final static String EMPTY_STRING = new String();
	private final static String WHITESPACE = " "; //$NON-NLS-1$
	
	public final static String ALL_OPTIONS_ID = EMPTY_STRING;
	public final static String COMMAND_LINE_PATTERN_ID = "org.eclipse.commandLinePatternId";
	private IConfiguration config;
	private IResourceConfiguration rcConfig;
	private IOptionCategory optCategory;
	private ToolListElement selectedElement;
	private ListenerList listenerList;
	private boolean dirtyFlag;
	
	private ToolsSettingsBlock block;
	
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

	
	public BuildToolSettingsPreferenceStore(ToolsSettingsBlock block){
		this.block = block;
	}
	
	public void setSelection(IConfiguration cfg, ToolListElement element, IOptionCategory category){
		selectedElement = element;
		optCategory = category;
		rcConfig = null;
		config = cfg;
	}

	public void setSelection(IResourceConfiguration cfg, ToolListElement element, IOptionCategory category){
		selectedElement = element;
		optCategory = category;
		rcConfig = cfg;
		config = cfg.getParent();
	}
	
	public IConfiguration getSelectedConfig(){
		return config;
	}
	
	public String getOptionPrefName(IOption option){
		IOption extOption = getExtensionOption(option);
		if(extOption != null)
			return extOption.getId();
		return option.getId();
	}

	private static IOption getExtensionOption(IOption option){
		for(;option != null && (!option.isExtensionElement() 
				|| ((Option)option).isAdjustedExtension()
				|| ((Option)option).wasOptRef());
			option = option.getSuperClass()){}
			
		return option;
	}
	
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.add(listener);
	}

	public boolean contains(String name) {
		if(optCategory instanceof Tool){
			if(optCategory.getId().equals(name))
				return true;
			else if(COMMAND_LINE_PATTERN_ID.equals(name))
				return true;
			else if(ALL_OPTIONS_ID.equals(name))
				return true;
		} else if(getOptionValue(name) != null){
			return true;
		}
		return false;
	}

	public void firePropertyChangeEvent(String name, Object oldValue,
			Object newValue) {
		Object[] listeners = listenerList.getListeners();
		if (listeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) 
		{
			PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
			for (int i = 0; i < listeners.length; ++i) 
			{
				IPropertyChangeListener l = (IPropertyChangeListener)listeners[i];
				l.propertyChange( pe );
			}
		}
	}

	public boolean getBoolean(String name) {
		Object val = getOptionValue(name);
		if(val instanceof Boolean)
			return ((Boolean)val).booleanValue();
		return getDefaultBoolean(name);
	}

	public boolean getDefaultBoolean(String name) {
		return false;
	}

	public double getDefaultDouble(String name) {
		return 0;
	}

	public float getDefaultFloat(String name) {
		return 0;
	}

	public int getDefaultInt(String name) {
		return 0;
	}

	public long getDefaultLong(String name) {
		return 0;
	}

	public String getDefaultString(String name) {
		return EMPTY_STRING;
	}

	public double getDouble(String name) {
		return getDefaultDouble(name);
	}

	public float getFloat(String name) {
		return getDefaultFloat(name);
	}

	public int getInt(String name) {
		return getDefaultInt(name);
	}

	public long getLong(String name) {
		return getDefaultLong(name);
	}

	public String getString(String name) {
		if(optCategory instanceof Tool){
			if(optCategory.getId().equals(name))
				return ((Tool)optCategory).getToolCommand();
			else if(COMMAND_LINE_PATTERN_ID.equals(name))
				return ((Tool)optCategory).getCommandLinePattern();
			else if(ALL_OPTIONS_ID.equals(name)){
				try {
					return listToString(((Tool)optCategory).getToolCommandFlags(
							null,
							null,
							new UIMacroSubstitutor(
									0,
									null,
									EMPTY_STRING,
									WHITESPACE,
									block.obtainMacroProvider())),
							WHITESPACE);
				} catch (BuildException e) {
				}
			}
		} else {
			Object val = getOptionValue(name);
			if(val instanceof String)
				return (String)val;
			else if(val instanceof Collection)
				return listToString((String[])((Collection)val).toArray(new String[0]));
		}
			
		return getDefaultString(name);
	}
	
	public static String listToString(String[] items) {
		return listToString(items,DEFAULT_SEPERATOR);
	}

	
	protected Object getOptionValue(String name){
		Object option[] = getOption(name);
		if(option != null){
			try {
				IOption opt = (IOption)option[1]; 
				Object val = opt.getValue();
				if(opt.getValueType() == IOption.ENUMERATED && val instanceof String)
					val = opt.getEnumName((String)val);
				return val;
			} catch (BuildException e) {
			}
		}
		return null;
	}
	
	public Object[] getOption(String name){
		Object options[][];
        
		IHoldsOptions selectedHolder = selectedElement.getHoldOptions();
		if (selectedHolder == null) selectedHolder = selectedElement.getTool();
		if(rcConfig != null)
			options = optCategory.getOptions(rcConfig, selectedHolder);
		else
			options = optCategory.getOptions(config, selectedHolder);

		for(int i = 0; i < options.length; i++){
			IHoldsOptions ho = (IHoldsOptions)options[i][0];
			if(ho == null) break;
			
			IOption option = (IOption)options[i][1];
			
			if(option.getId().equals(name) 
					|| ((!option.isExtensionElement() || ((Option)option).isAdjustedExtension() || ((Option)option).wasOptRef())
						&& option.getSuperClass() != null
						&& option.getSuperClass().getId().equals(name)))
				return options[i];
		}
		return null;
		
	}

	public boolean isDefault(String name) {
		return false;
	}

	public boolean needsSaving() {
		return dirtyFlag;
	}

	public void putValue(String name, String value) {
		setValue(name,value);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.remove(listener);
	}

	public void setDefault(String name, double value) {
	}

	public void setDefault(String name, float value) {
	}

	public void setDefault(String name, int value) {
	}

	public void setDefault(String name, long value) {
	}

	public void setDefault(String name, String defaultObject) {
	}

	public void setDefault(String name, boolean value) {
	}

	public void setToDefault(String name) {
	}

	protected void setDirty( boolean isDirty )
	{
		dirtyFlag = isDirty;
	}

	public void setValue(String name, double value) {
	}

	public void setValue(String name, float value) {
	}

	public void setValue(String name, int value) {
	}

	public void setValue(String name, long value) {
	}

	public void setValue(String name, String value) {
		if(optCategory instanceof Tool){
			if(optCategory.getId().equals(name))
				((Tool)optCategory).setToolCommand(value);
			else if (COMMAND_LINE_PATTERN_ID.equals(name))
				((Tool)optCategory).setCommandLinePattern(value);
		} else
			setOptionValue(name,value);
	}

	public void setValue(String name, boolean value) {
		setOptionValue(name,new Boolean(value));
	}
	
	protected void setOptionValue(String name, Object value){
		Object opt[] = getOption(name);
		if(opt != null){
			IOption option = (IOption)opt[1];
			IHoldsOptions holder = (IHoldsOptions)opt[0];
			IOption newOption = null;
			try{
				switch(option.getValueType()){
					case IOption.STRING:
						if(value instanceof String){
							if (rcConfig != null) {
								newOption = rcConfig.setOption(holder,option,(String)value);
							} else {
								newOption = config.setOption(holder,option,(String)value);
							}
						}
						break;
					case IOption.BOOLEAN:
						if(value instanceof Boolean){
							boolean val = ((Boolean)value).booleanValue();
							if (rcConfig != null) {
								newOption = rcConfig.setOption(holder,option,val);
							} else {
								newOption = config.setOption(holder,option,val);
							}
						}
						break;
					case IOption.ENUMERATED:
						if(value instanceof String){
							String val = (String)value;
							String enumId = option.getEnumeratedId(val);
							if(rcConfig != null) {
								newOption = rcConfig.setOption(holder, option, 
										(enumId != null && enumId.length() > 0) ? enumId : val);
							} else {
								newOption = config.setOption(holder, option, 
										(enumId != null && enumId.length() > 0) ? enumId : val);
							}
	
						}
						break;
					case IOption.INCLUDE_PATH:
					case IOption.STRING_LIST:
					case IOption.PREPROCESSOR_SYMBOLS:
					case IOption.LIBRARIES:
					case IOption.OBJECTS:
						if(value instanceof String){
							String val[] = parseString((String)value);
							if (rcConfig != null) {
								newOption = rcConfig.setOption(holder,option,val);
							} else {
								newOption = config.setOption(holder,option,val);
							}
						}
						break;
					default:
						break;
				}
				
				if(newOption != option){
					//TODO: ???
				}
			} catch (BuildException e){
			}
		}
	}
	
	public static String[] parseString(String stringList) {
		if (stringList == null || stringList.length() == 0) {
			return new String[0];
		} else {
			return stringList.split(DEFAULT_SEPERATOR);
		}
	}
	
	public static String listToString(String items[], String separator){
		StringBuffer path = new StringBuffer(""); //$NON-NLS-1$
		
		for (int i = 0; i < items.length; i++) {
			path.append(items[i]);
			if (i < (items.length - 1)) {
				path.append(separator);
			}
		}
		return path.toString();

	}
	
}
