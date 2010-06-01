/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
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

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Option;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildfileMacroSubstitutor;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ToolSettingsPrefStore implements IPreferenceStore {
	public static final String DEFAULT_SEPERATOR = ";"; //$NON-NLS-1$
	private final static String EMPTY_STRING = new String();

	static ToolSettingsPrefStore store = null;
	
	public final static String ALL_OPTIONS_ID = EMPTY_STRING;
	public final static String COMMAND_LINE_SUFFIX = "org.eclipse.commandLinePatternId"; //$NON-NLS-1$
	private IResourceInfo rcInfo = null; 
	private IOptionCategory optCategory;
	private ToolListElement selectedElement;
	private ListenerList listenerList;
	private boolean dirtyFlag;

	public static ToolSettingsPrefStore getDefault() {
		if (store == null)
			store = new ToolSettingsPrefStore();
		return store;
	}
	
	public void setSelection(ICResourceDescription rd, ToolListElement element, IOptionCategory category){
		selectedElement = element;
		optCategory = category;
		rcInfo = get(rd);
	}

	public IConfiguration getSelectedConfig(){
		return rcInfo.getParent();
	}
	
	public String getOptionId(IOption option){
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
			else if((optCategory.getId() + COMMAND_LINE_SUFFIX).equals(name))
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

	public boolean getDefaultBoolean(String name) {	return false; }
	public double getDefaultDouble(String name) { return 0; }
	public float getDefaultFloat(String name) { return 0; }
	public int getDefaultInt(String name) {	return 0; }
	public long getDefaultLong(String name) { return 0; }
	public String getDefaultString(String name) { return EMPTY_STRING; }

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

	@SuppressWarnings("unchecked")
	public String getString(String name) {
		if(optCategory instanceof Tool){
			if(optCategory.getId().equals(name))
				return ((Tool)optCategory).getToolCommand();
			else if((optCategory.getId() + COMMAND_LINE_SUFFIX).equals(name))
				return ((Tool)optCategory).getCommandLinePattern();
			else if(ALL_OPTIONS_ID.equals(name)){
				SupplierBasedCdtVariableSubstitutor macroSubstitutor = new BuildfileMacroSubstitutor(null, EMPTY_STRING, " ");  //$NON-NLS-1$
				Tool tool = (Tool)optCategory;
				String[] flags = tool.getToolCommandFlags(
						null,
						null,
						macroSubstitutor, 
						obtainMacroProvider());
				IManagedCommandLineGenerator cmdLGen = tool.getCommandLineGenerator();
				IManagedCommandLineInfo cmdLInfo = cmdLGen.generateCommandLineInfo(tool,
						EMPTY_STRING, flags, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,
						null,tool.getCommandLinePattern());
				return cmdLInfo.getFlags();
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
	
	public BuildMacroProvider obtainMacroProvider(){
		return (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
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
	
	public Object[] getOption(String id){
		if (selectedElement == null) 
			return null;
		
		IHoldsOptions selectedHolder = selectedElement.getHoldOptions();
		if (selectedHolder == null) 
			selectedHolder = selectedElement.getTool();
		Object options[][] = optCategory.getOptions(rcInfo, selectedHolder);
		if (options == null)
			return null;
		
		for(int i = 0; i < options.length; i++){
			IHoldsOptions ho = (IHoldsOptions)options[i][0];
			if(ho == null) 
				break;
			
			IOption option = (IOption)options[i][1];
			
			if( ( option.getId().equals(id))
					|| ((!option.isExtensionElement() || ((Option)option).isAdjustedExtension() || ((Option)option).wasOptRef())
						&& option.getSuperClass() != null
						&& option.getSuperClass().getId().equals(id)))
				return options[i];
		}
		return null;
		
	}

	public boolean isDefault(String name) {	return false;}
	public boolean needsSaving() {	return dirtyFlag;}
	public void putValue(String name, String value) {setValue(name,value);}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.remove(listener);
	}
	public void setDefault(String name, double value) {}
	public void setDefault(String name, float value) {}
	public void setDefault(String name, int value) {}
	public void setDefault(String name, long value) {}
	public void setDefault(String name, String defaultObject) {}
	public void setDefault(String name, boolean value) {}
	public void setToDefault(String name) {}
	protected void setDirty( boolean isDirty )	{dirtyFlag = isDirty;}
	public void setValue(String name, double value) {}
	public void setValue(String name, float value) {}
	public void setValue(String name, int value) {}
	public void setValue(String name, long value) {}

	public void setValue(String name, String value) {
		if(optCategory instanceof Tool){
			if(optCategory.getId().equals(name))
				((Tool)optCategory).setToolCommand(value);
			else if ((optCategory.getId() + COMMAND_LINE_SUFFIX).equals(name))
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
							newOption = rcInfo.setOption(holder, option, (String)value);
						}
						break;
					case IOption.BOOLEAN:
						if(value instanceof Boolean){
							boolean val = ((Boolean)value).booleanValue();
							newOption = rcInfo.setOption(holder,option,val);
						}
						break;
					case IOption.ENUMERATED:
						if(value instanceof String){
							String val = (String)value;
							String enumId = option.getEnumeratedId(val);
							newOption = rcInfo.setOption(holder, option, 
									(enumId != null && enumId.length() > 0) ? enumId : val);
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
						if(value instanceof String){
							String val[] = parseString((String)value);
							newOption = rcInfo.setOption(holder,option,val);
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
	private IResourceInfo get(ICResourceDescription cfgd) {
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd.getConfiguration());
		if (cfgd.getType() == ICSettingBase.SETTING_PROJECT ||
			cfgd.getType() == ICSettingBase.SETTING_CONFIGURATION) 
			return cfg.getRootFolderInfo();
		
		IPath p = cfgd.getPath();
		IResourceInfo ri = cfg.getResourceInfo(p, true);
		if (ri != null && p.equals(ri.getPath())) {
			return ri;
		}
		
		if (cfgd.getType() == ICSettingBase.SETTING_FILE) {
			ri = cfg.createFileInfo(p);
		} else if (cfgd.getType() == ICSettingBase.SETTING_FOLDER) {
			ri = cfg.createFolderInfo(p);
		}
		return ri;
	}
	
}
