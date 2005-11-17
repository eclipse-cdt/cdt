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
	public static final String TYPE_ALL = "ALL"; 	//$NON-NLS-1$

	public static final String FLAG_DELIMITER = "|"; 	//$NON-NLS-1$

	public static final int FLAG_UI_VISIBILITY = 0x01;
	public static final int FLAG_UI_ENABLEMENT = 0x02;
	public static final int FLAG_CMD_USAGE = 0x04;
	public static final int FLAG_ALL = ~0;

	private int fEnablementFlags;

	public OptionEnablementExpression(IManagedConfigElement element) {
		super(element);
		
		fEnablementFlags = calculateFlags(element.getAttribute(TYPE));
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
		if(!checkFlags(flags) || evaluate(configuration, holder, option))
			return true;
		return false;
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
}
