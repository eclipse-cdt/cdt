/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.cdtvariables;

import org.eclipse.cdt.core.cdtvariables.CdtVariableStatus;

public class SupplierBasedCdtVariableStatus extends CdtVariableStatus {
	private IVariableContextInfo fContextInfo;
	
	public SupplierBasedCdtVariableStatus(int severity, int code,
			String message, Throwable exception, String macroName,
			String expression, String referencedName, IVariableContextInfo info) {
		super(severity, code, message, exception, macroName, expression, referencedName);

		fContextInfo = info;
	}

	public SupplierBasedCdtVariableStatus(int code, String macroName,
			String expression, String referencedName, IVariableContextInfo info) {
		super(code, macroName, expression, referencedName);

		fContextInfo = info;
	}

	public SupplierBasedCdtVariableStatus(int code, String message,
			Throwable exception, String macroName, String expression,
			String referencedName, IVariableContextInfo info) {
		super(code, message, exception, macroName, expression, referencedName);

		fContextInfo = info;
	}
	
	public IVariableContextInfo getVariableContextInfo(){
		return fContextInfo;
	}

}
