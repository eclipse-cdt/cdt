/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;

public class CheckOptionExpression implements IBooleanExpression {
	public static final String NAME = "checkOption"; 	//$NON-NLS-1$

	public static final String OPTION_ID = "optionId"; 	//$NON-NLS-1$
	public static final String HOLDER_ID = "holderId"; 	//$NON-NLS-1$
	public static final String VALUE = "value"; 	//$NON-NLS-1$
	public static final String IS_REGEX = "isRegex";	//$NON-NLS-1$
	public static final String OTHER_OPTION_ID = "otherOptionId"; 	//$NON-NLS-1$
	public static final String OTHER_HOLDER_ID = "otherHolderId"; 	//$NON-NLS-1$

	public static final String YES = "yes"; 	//$NON-NLS-1$
	public static final String TRUE = "true"; 	//$NON-NLS-1$

	private String fOptionId;
	private String fHolderId;
	private String fValue;
	private boolean fIsRegex;
	private String fOtherOptionId;
	private String fOtherHolderId;
	
	public CheckOptionExpression(IManagedConfigElement element){
		fOptionId = element.getAttribute(OPTION_ID);
		fHolderId = element.getAttribute(HOLDER_ID);
		fValue = element.getAttribute(VALUE);
		fIsRegex = getBooleanValue(element.getAttribute(IS_REGEX));
		fOtherOptionId = element.getAttribute(OTHER_OPTION_ID);
		fOtherHolderId = element.getAttribute(OTHER_HOLDER_ID);
	}

	public boolean evaluate(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option) {
		boolean result = false;
		IBuildObject ho[] = getHolderAndOption(fOptionId, fHolderId,
				configuration, holder, option);
		
		if(ho != null){
			if(fValue != null)
				result = evaluate((IOption)ho[1],((IHoldsOptions)ho[0]),fValue);
			else {
				IBuildObject otherHo[] = getHolderAndOption(fOtherOptionId, fOtherHolderId,
						configuration, holder, option);
				if(otherHo != null)
					result = evaluate((IOption)ho[1],((IHoldsOptions)ho[0]),
							(IOption)otherHo[1],((IHoldsOptions)otherHo[0]));
			}
		}
		
		return result;
	}
	
	public boolean evaluate(IOption option, IHoldsOptions holder, String value){
		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
		String delimiter = ManagedBuildManager.getEnvironmentVariableProvider().getDefaultDelimiter();
		String inexVal = " "; 	//$NON-NLS-1$
		try {
			String resolvedValue = provider.resolveValue(value, inexVal, delimiter,
					IBuildMacroProvider.CONTEXT_OPTION,
					new OptionContextData(option,holder));
			
			switch(option.getValueType()){
				case IOption.STRING:
				case IOption.ENUMERATED:{
					String stringValue = option.getStringValue();
					stringValue = provider.resolveValue(stringValue, inexVal, delimiter,
							IBuildMacroProvider.CONTEXT_OPTION,
							new OptionContextData(option,holder));
					if(fIsRegex){
						Pattern pattern = Pattern.compile(resolvedValue);
						Matcher matcher = pattern.matcher(stringValue);
						return matcher.matches();
					}
					return stringValue.equals(resolvedValue);
				}
				case IOption.BOOLEAN:
					return option.getBooleanValue() == getBooleanValue(resolvedValue);
				case IOption.INCLUDE_PATH:
				case IOption.STRING_LIST:
				case IOption.PREPROCESSOR_SYMBOLS:
				case IOption.LIBRARIES:
				case IOption.OBJECTS:{
					List list = (List)option.getValue();
					String listValue = provider.convertStringListToString((String[])list.toArray(new String[list.size()]),delimiter);

					listValue = provider.resolveValue(listValue, inexVal, delimiter,
							IBuildMacroProvider.CONTEXT_OPTION,
							new OptionContextData(option,holder));
					
					if(fIsRegex){
						Pattern pattern = Pattern.compile(resolvedValue);
						Matcher matcher = pattern.matcher(listValue);
						return matcher.matches();
					}
					return listValue.equals(resolvedValue);
				}
				default:
					break;
			}
		} catch (BuildException e) {
		} catch (BuildMacroException e) {
		}
		return false;
	}
	
	protected boolean getBooleanValue(String value){
		if(TRUE.equalsIgnoreCase(value))
			return true;
		else if(YES.equalsIgnoreCase(value))
			return true;
		return false;
	}

	public boolean evaluate(IOption option, IHoldsOptions holder, 
			IOption otherOption, IHoldsOptions otherHolder){
		try {
			if(option.getValueType() != otherOption.getValueType())
				return false;
			
			BuildMacroProvider provider = (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
			String delimiter = ManagedBuildManager.getEnvironmentVariableProvider().getDefaultDelimiter();
			String inexVal = " "; 	//$NON-NLS-1$
			
			switch(option.getValueType()){
				case IOption.STRING:
				case IOption.ENUMERATED:{
					String stringValue = option.getStringValue();
					stringValue = provider.resolveValue(stringValue, inexVal, delimiter,
							IBuildMacroProvider.CONTEXT_OPTION,
							new OptionContextData(option,holder));
					
					String str = otherOption.getStringValue();
					str = provider.resolveValue(str, inexVal, delimiter,
								IBuildMacroProvider.CONTEXT_OPTION,
								new OptionContextData(otherOption,otherHolder));

					return stringValue.equals(str);
				}
				case IOption.BOOLEAN:
					return option.getBooleanValue() == otherOption.getBooleanValue();
				case IOption.INCLUDE_PATH:
				case IOption.STRING_LIST:
				case IOption.PREPROCESSOR_SYMBOLS:
				case IOption.LIBRARIES:
				case IOption.OBJECTS:{
					List list = (List)option.getValue();
					String listValue[] = (String[])list.toArray(new String[list.size()]);
					
					list = (List)otherOption.getValue();
					String otherValue[] = (String[])list.toArray(new String[list.size()]);

					IMacroContextInfo info = provider.getMacroContextInfo(IBuildMacroProvider.CONTEXT_OPTION,
							new OptionContextData(option,holder));
					IMacroSubstitutor subst = provider.getMacroSubstitutor(info,inexVal,delimiter);
					
					listValue = MacroResolver.resolveStringListValues(listValue,subst,false);
					
					info = provider.getMacroContextInfo(IBuildMacroProvider.CONTEXT_OPTION,
							new OptionContextData(otherOption,otherHolder));
					subst = provider.getMacroSubstitutor(info,inexVal,delimiter);

					otherValue = MacroResolver.resolveStringListValues(otherValue,subst,false);
					
					if(listValue.length == otherValue.length){
						for(int i = 0; i < listValue.length; i++){
							if(!listValue[i].equals(otherValue[i]))
								return false;
						}
						return true;
					}
					return false;
				}
				default:
					break;
			}
		} catch (BuildException e) {
		} catch (BuildMacroException e) {
		} catch (ClassCastException e) {
		}
		return false;		
	}
	
	protected IBuildObject[] getHolderAndOption(String optionId,
			String holderId,
			IBuildObject configuration, 
	        IHoldsOptions holder, 
	        IOption option
			){
		IBuildObject result[] = null;
		if(optionId == null)
			result = new IBuildObject[]{holder,option};
		else {
			IHoldsOptions hld = null;
			if(holderId == null)
				hld = holder;
			else
				hld = getHolder(holderId,configuration);
			
			if(hld != null) {
				IOption opt = getOption(optionId,hld);
				if(opt != null)
					result = new IBuildObject[]{hld,opt};
			}
		}
		return result;
	}

	protected IOption getOption(String optionId,
            IHoldsOptions holder){
		return holder.getOptionBySuperClassId(optionId);
	}
	
	protected IHoldsOptions getHolder(String id, 
			IBuildObject cfg){
		IHoldsOptions holder = null;
		if(cfg instanceof IResourceConfiguration){
			IHoldsOptions holders[] = ((IResourceConfiguration)cfg).getTools();
			for(int i = 0; i < holders.length; i++){
				if(isHolder(id,holders[i])){
					holder = holders[i];
					break;
				}
			}
		} else if (cfg instanceof IConfiguration){
			IToolChain tc = ((IConfiguration)cfg).getToolChain();
			if(isHolder(id,tc))
				holder = tc;
			else {
				IHoldsOptions holders[] = tc.getTools();
				for(int i = 0; i < holders.length; i++){
					if(isHolder(id,holders[i])){
						holder = holders[i];
						break;
					}
				}
			}
		}
		
		return holder;
	}
	
	protected boolean isHolder(String id, IHoldsOptions holder){
		do {
			if(id.equals(holder.getId()))
				return true;
			if(holder instanceof IToolChain)
				holder = ((IToolChain)holder).getSuperClass();
			else if(holder instanceof ITool)
				holder = ((ITool)holder).getSuperClass();
			else
				holder = null;
		} while(holder != null);
		
		return false;
	}
}
