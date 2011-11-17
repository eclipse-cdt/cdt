/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableStatus;

/**
 * This class implements the IBuildMacroStatus interface
 *
 * @since 3.0
 */
public class BuildMacroStatus extends SupplierBasedCdtVariableStatus implements IBuildMacroStatus {

	public BuildMacroStatus(int severity, int code, String message,
			Throwable exception, String macroName, String expression,
			String referencedName, IVariableContextInfo info) {
		super(severity, code, message, exception, macroName, expression,
				referencedName, info);
	}

	public BuildMacroStatus(int code, String macroName, String expression,
			String referencedName, IVariableContextInfo info) {
		super(code, macroName, expression, referencedName, info);
	}

	public BuildMacroStatus(int code, String message, Throwable exception,
			String macroName, String expression, String referencedName,
			IVariableContextInfo info) {
		super(code, message, exception, macroName, expression, referencedName, info);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus#getContextType()
	 */
	@Override
	public int getContextType() {
		IMacroContextInfo info = getMacroContextInfo();
		if(info != null){
			return info.getContextType();
		}
		return 0;
	}

	private IMacroContextInfo getMacroContextInfo(){
		IVariableContextInfo info = getVariableContextInfo();
		if(info instanceof IMacroContextInfo)
			return (IMacroContextInfo)info;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus#getContextData()
	 */
	@Override
	public Object getContextData() {
		IMacroContextInfo info = getMacroContextInfo();
		if(info != null){
			return info.getContextData();
		}
		return null;
	}

	@Override
	public String getMacroName() {
		return getVariableName();
	}
}
