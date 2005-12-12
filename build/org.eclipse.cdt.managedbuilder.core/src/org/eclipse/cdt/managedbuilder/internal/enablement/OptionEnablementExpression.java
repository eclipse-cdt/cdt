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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;

public class OptionEnablementExpression extends AndExpression{
	public static final String NAME = "enablement"; 	//$NON-NLS-1$
	public static final String TYPE = "type"; 	//$NON-NLS-1$

	public static final String TYPE_UI_VISIBILITY = "UI_VISIBILITY"; 	//$NON-NLS-1$
	public static final String TYPE_UI_ENABLEMENT = "UI_ENABLEMENT"; 	//$NON-NLS-1$
	public static final String TYPE_CMD_USAGE = "CMD_USAGE"; 	//$NON-NLS-1$
	public static final String TYPE_CONTAINER_ATTRIBUTE = "CONTAINER_ATTRIBUTE"; 	//$NON-NLS-1$
	public static final String TYPE_ALL = "ALL"; 	//$NON-NLS-1$

	public static final String FLAG_DELIMITER = "|"; 	//$NON-NLS-1$

	public static final String ATTRIBUTE = "attribute"; 	//$NON-NLS-1$
	public static final String VALUE = "value"; 	//$NON-NLS-1$
	
	private static final String fSupportedAttributes[] = {
		IOption.COMMAND,
		IOption.COMMAND_FALSE,
	};
	
	public static final int FLAG_UI_VISIBILITY = 0x01;
	public static final int FLAG_UI_ENABLEMENT = 0x02;
	public static final int FLAG_CMD_USAGE = 0x04;
	public static final int FLAG_CONTAINER_ATTRIBUTE = 0x08;
	public static final int FLAG_ALL = ~0;

	private int fEnablementFlags;
	private String fAttribute;
	private String fValue;

	public OptionEnablementExpression(IManagedConfigElement element) {
		super(element);
		
		fEnablementFlags = calculateFlags(element.getAttribute(TYPE));
		
		fAttribute = element.getAttribute(ATTRIBUTE);
		fValue = element.getAttribute(VALUE);
		adjustAttributeSupport();
	}
	
	private void adjustAttributeSupport(){
		boolean cleanAttrFlag = true;
		if(fAttribute != null && fValue != null){
			for(int i = 0; i < fSupportedAttributes.length; i++){
				if(fAttribute.equals(fSupportedAttributes[i])){
					cleanAttrFlag = false;
					break;
				}
			}
		}
		
		if(cleanAttrFlag){
			fEnablementFlags &= ~FLAG_CONTAINER_ATTRIBUTE;
			fAttribute = null;
			fValue = null;
		}
	}

	public String[] convertToList(String value, String delimiter){
		List list = new ArrayList();
		int delLength = delimiter.length();
		int valLength = value.length();

		if(delLength == 0){
			list.add(value);
		}
		else{
			int start = 0;
			int stop;
			while(start < valLength){
				stop = value.indexOf(delimiter,start);
				if(stop == -1)
					stop = valLength;
				String subst = value.substring(start,stop);
				list.add(subst);
				start = stop + delLength;
			}
		}

		return (String[])list.toArray(new String[list.size()]);
	}
	
	protected int calculateFlags(String flagsString){
		int flags = 0;
		
		if(flagsString != null){
			String strings[] = convertToList(flagsString,FLAG_DELIMITER);
			
			for(int i = 0; i < strings.length; i++){
				String str = strings[i].trim();
				if(TYPE_UI_VISIBILITY.equals(str))
					flags |= FLAG_UI_VISIBILITY;
				else if(TYPE_UI_ENABLEMENT.equals(str))
					flags |= FLAG_UI_ENABLEMENT;
				else if(TYPE_CMD_USAGE.equals(str))
					flags |= FLAG_CMD_USAGE;
				else if(TYPE_CONTAINER_ATTRIBUTE.equals(str))
					flags |= FLAG_CONTAINER_ATTRIBUTE;
				else if(TYPE_ALL.equals(str))
					flags |= FLAG_ALL;
			}
		}
		
		if(flags == 0)
			flags = FLAG_ALL;
		return flags;
	}
	
	public boolean evaluate(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option,
            int flags){
		return evaluate(configuration, holder, option, flags, (FLAG_CONTAINER_ATTRIBUTE & flags) == 0);
	}

	public boolean evaluate(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option,
            int flags,
            boolean bDefault){
		return checkFlags(flags) ? evaluate(configuration, holder, option)
				: bDefault;
	}

/*	public boolean evaluate(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option) {
		if(getChildren().length == 0)
			return false;
		return super.evaluate(configuration,holder,option);
	}
*/	
	public boolean checkFlags(int flags){
		return (fEnablementFlags & flags) == flags;
	}
	
	public int getFlags(){
		return fEnablementFlags;
	}
	
	public boolean performAdjustment(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option){
		if(evaluate(configuration,holder,option,FLAG_CONTAINER_ATTRIBUTE)){
			try{
				if(IOption.COMMAND.equals(fAttribute)){
					IOption setOption = holder.getOptionToSet(option, true);
					setOption.setCommand(fValue);
				}else if(IOption.COMMAND_FALSE.equals(fAttribute)){
					IOption setOption = holder.getOptionToSet(option, true);
					setOption.setCommandFalse(fValue);
				}else
					return false;
			}catch (BuildException e){
				return false;
			}
			return true;
		}
		return false;
	}
}
