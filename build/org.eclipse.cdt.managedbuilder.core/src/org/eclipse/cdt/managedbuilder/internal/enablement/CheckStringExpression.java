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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;

public class CheckStringExpression implements IBooleanExpression {
	public static final String NAME = "checkString"; 	//$NON-NLS-1$

	public static final String STRING = "string"; 	//$NON-NLS-1$
	public static final String VALUE = "value"; 	//$NON-NLS-1$
	public static final String IS_REGEX = "isRegex"; 	//$NON-NLS-1$
	
	public static final String YES = "yes"; 	//$NON-NLS-1$
	public static final String TRUE = "true"; 	//$NON-NLS-1$

	private String fString;
	private String fValue;
	private boolean fIsRegex;
	
	public CheckStringExpression(IManagedConfigElement element){
		fString = element.getAttribute(STRING);
		if(fString == null)
			fString = new String();
		
		fValue = element.getAttribute(VALUE);
		if(fValue == null)
			fValue = new String();
		
		fIsRegex = getBooleanValue(element.getAttribute(IS_REGEX));
	}
	
	protected boolean getBooleanValue(String value){
		if(TRUE.equalsIgnoreCase(value))
			return true;
		else if(YES.equalsIgnoreCase(value))
			return true;
		return false;
	}

	public boolean evaluate(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option) {
		
		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
		IEnvironmentVariableProvider env = ManagedBuildManager.getEnvironmentVariableProvider();
		String delimiter = env.getDefaultDelimiter();
		try {
			String resolvedString = provider.resolveValue(fString,
					" ",	//$NON-NLS-1$
					delimiter,
					IBuildMacroProvider.CONTEXT_OPTION,
					new OptionContextData(option,holder)
					);
			
			String resolvedValue =  provider.resolveValue(fValue,
					" ",	//$NON-NLS-1$
					delimiter,
					IBuildMacroProvider.CONTEXT_OPTION,
					new OptionContextData(option,holder)
					);
			
			if(fIsRegex){
				Pattern pattern = Pattern.compile(resolvedValue);
				Matcher matcher = pattern.matcher(resolvedString);
				return matcher.matches();
			}
			return resolvedString.equals(resolvedValue);
		} catch (BuildMacroException e) {
		}
		return false;
	}

}
