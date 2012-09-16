/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.core.runtime.Assert;

/**
 * Implementation of type template arguments, used by AST and index.
 */
public class CPPTemplateTypeArgument implements ICPPTemplateArgument {
	private final IType fType;

	public CPPTemplateTypeArgument(IType type) {
		Assert.isNotNull(type);
		fType= type;
	}
	
	@Override
	public boolean isTypeValue() {
		return true;
	}

	@Override
	public boolean isNonTypeValue() {
		return false;
	}

	@Override
	public IType getTypeValue() {
		return fType;
	}

	@Override
	public ICPPEvaluation getNonTypeEvaluation() {
		return null;
	}
	
	@Override
	public IValue getNonTypeValue() {
		return null;
	}
	
	@Override
	public IType getTypeOfNonTypeValue() {
		return null;
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
				return new CPPTemplateTypeArgument(t);
			}
		}
		return null;
	}

	@Override
	public boolean isSameValue(ICPPTemplateArgument arg) {
		return fType.isSameType(arg.getTypeValue());
	}

	@Override
	public String toString() {
		return fType.toString();
	}
}
