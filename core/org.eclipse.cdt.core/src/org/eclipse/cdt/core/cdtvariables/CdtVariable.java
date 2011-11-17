/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.core.cdtvariables;

import org.eclipse.cdt.internal.core.SafeStringInterner;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;

/**
 * This is the trivial implementation of the IBuildMacro used internaly by the MBS
 *
 * @since 3.0
 */
public class CdtVariable implements ICdtVariable {
	protected String fName;
	protected int fType;
	protected String fStringValue;
	protected String fStringListValue[];

	protected CdtVariable(){

	}

	public CdtVariable(String name, int type, String value){
		fName = SafeStringInterner.safeIntern(name);
		fType = type;
		fStringValue = SafeStringInterner.safeIntern(value);
	}

	public CdtVariable(String name, int type, String value[]){
		fName = SafeStringInterner.safeIntern(name);
		fType = type;
		fStringListValue = value;
	}

	public CdtVariable(ICdtVariable var){
		fName = var.getName();
		fType = var.getValueType();
		try {
			if(CdtVariableResolver.isStringListVariable(fType))
				fStringListValue = var.getStringListValue();
			else
				fStringValue = var.getStringValue();
		} catch (CdtVariableException e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getMacroValueType()
	 */
	@Override
	public int getValueType() {
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringValue()
	 */
	@Override
	public String getStringValue() throws CdtVariableException {
		if(CdtVariableResolver.isStringListVariable(fType))
			throw new CdtVariableException(ICdtVariableStatus.TYPE_MACRO_NOT_STRING,fName,null,fName);

		return fStringValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringListValue()
	 */
	@Override
	public String[] getStringListValue() throws CdtVariableException {
		if(!CdtVariableResolver.isStringListVariable(fType))
			throw new CdtVariableException(ICdtVariableStatus.TYPE_MACRO_NOT_STRINGLIST,fName,null,fName);

		return fStringListValue;
	}

}
