/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.core.runtime.Assert;

/**
 * Implementation of template arguments, used by ast and index.
 */
public class CPPTemplateArgument implements ICPPTemplateArgument {
	private final IType fType;
	private final IValue fValue;

	public CPPTemplateArgument(IValue value, IType type) {
		Assert.isNotNull(value);
		fType= type;
		fValue= value;
	}
	
	public CPPTemplateArgument(IType type) {
		Assert.isNotNull(type);
		fType= type;
		fValue= null;
	}
	
	@Override
	public boolean isTypeValue() {
		return fValue == null;
	}

	@Override
	public boolean isNonTypeValue() {
		return fValue != null;
	}

	@Override
	public IType getTypeValue() {
		return isTypeValue() ? fType : null;
	}

	@Override
	public IValue getNonTypeValue() {
		return fValue;
	}
	
	@Override
	public IType getTypeOfNonTypeValue() {
		return isNonTypeValue() ? fType : null;
	}
	
	@Override
	public boolean isPackExpansion() {
		return fType instanceof ICPPParameterPackType;
	}

	@Override
	public ICPPTemplateArgument getExpansionPattern() {
		if (fType instanceof ICPPParameterPackType) {
			IType t= ((ICPPParameterPackType) fType).getType();
			if (t != null) {
				if (fValue != null) {
					return new CPPTemplateArgument(fValue, t);
				}
				return new CPPTemplateArgument(t);
			}
		}
		return null;
	}

	@Override
	public boolean isSameValue(ICPPTemplateArgument arg) {
		if (fValue != null) {
			return fValue.equals(arg.getNonTypeValue());
		}
		return fType.isSameType(arg.getTypeValue());
	}

	@Override
	public String toString() {
		if (fValue != null)
			return fValue.toString();
		return fType.toString();
	}
}
