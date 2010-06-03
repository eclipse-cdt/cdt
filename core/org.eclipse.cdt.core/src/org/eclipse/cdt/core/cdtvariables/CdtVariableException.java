/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.cdtvariables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * This exception is thrown in the case of some build macros-related operation failure
 * The exception typically contains one or more IBuildMacroStatus statuses 
 * 
 * @since 3.0
 */
public class CdtVariableException extends CoreException {
	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 3976741380246681395L;

	/**
	 * Creates a new exception with the given status object.  
	 *
	 * @param status the status object to be associated with this exception. 
	 * Typically this is either the IBuildMacroStatus or the MultiStatus that holds
	 * the list of the IBuildMacroStatus statuses
	 */
	public CdtVariableException(IStatus status) {
		super(status);
	}

	/**
	 * Creates an exception containing a single IBuildMacroStatus status with the IStatus.ERROR severity
	 * 
	 * @param code one of the IBuildMacroStatus.TYPE_xxx statusses
	 * @param message message, can be null. In this case the default message will
	 *  be generated base upon the other status info
	 * @param exception a low-level exception, or <code>null</code> if not
	 *    applicable 
	 * @param macroName the name of the build macro whose resolution caused this status creation or null if none
	 * @param expression the string whose resolutinon caused caused this status creation or null if none
	 * @param referencedName the macro name referenced in the resolution string that caused this this status creation or null if none
	 */
	public CdtVariableException(int code, 
			String message, 
			Throwable exception, 
			String macroName, 
			String expression, 
			String referencedName//,
			//int contextType,
			//Object contextData
			) {
		super(new CdtVariableStatus(code, message, exception, macroName, expression, referencedName/*, contextType, contextData*/));
	}

	/**
	 * Creates an exception containing a single IBuildMacroStatus status with the IStatus.ERROR severity and with the default message
	 * 
	 * @param code one of the IBuildMacroStatus.TYPE_xxx statusses
	 * @param macroName the name of the build macro whose resolution caused this status creation or null if none
	 * @param expression the string whose resolutinon caused caused this status creation or null if none
	 * @param referencedName the macro name referenced in the resolution string that caused this this status creation or null if none
	 */
	public CdtVariableException(int code, 
			String macroName, 
			String expression, 
			String referencedName//,
			//int contextType,
			//Object contextData
			) {
		super(new CdtVariableStatus(code, macroName, expression, referencedName/*, contextType, contextData*/));
	}
	
	/**
	 * Returns an array of the IBuildMacroStatus statuses this exception holds
	 * 
	 * @return IBuildMacroStatus[]
	 */
	public ICdtVariableStatus[] getVariableStatuses(){
		IStatus status = getStatus();
		if(status instanceof ICdtVariableStatus)
			return new ICdtVariableStatus[]{(ICdtVariableStatus)status};
		else if(status.isMultiStatus()){
			IStatus children[] = status.getChildren();
			ICdtVariableStatus result[] = new ICdtVariableStatus[children.length];
			int num = 0;
			for (IStatus element : children) {
				if(element instanceof ICdtVariableStatus)
					result[num++]=(ICdtVariableStatus)element;
			}
			if(num != children.length){
				ICdtVariableStatus tmp[] = new ICdtVariableStatus[num];
				for(int i = 0; i < num; i++)
					tmp[i] = result[i];
				result = tmp;
			}
			return result;
		}
		return new ICdtVariableStatus[0];
	}

}
