/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;

/**
 * Models functions used without declarations.
 */
public class CExternalFunction extends CFunction implements ICExternalBinding {
	private static final IType VOID_TYPE = new CBasicType(Kind.eVoid, 0);

	private IASTName name;
	private IASTTranslationUnit tu;

	public CExternalFunction(IASTTranslationUnit tu, IASTName name) {
		super(null);
		this.name = name;
		this.tu = tu;
	}

	@Override
	public IFunctionType getType() {
		if (type == null) {
			// Bug 321856: Prevent recursions
			type = new CPPFunctionType(VOID_TYPE, IType.EMPTY_TYPE_ARRAY, null);
			IFunctionType computedType = createType();
			if (computedType != null) {
				type = computedType;
			}
		}
		return type;
	}

	@Override
	public IParameter[] getParameters() {
		return IParameter.EMPTY_PARAMETER_ARRAY;
	}

	@Override
	protected IASTTranslationUnit getTranslationUnit() {
		return tu;
	}

	@Override
	public String getName() {
		return name.toString();
	}

	@Override
	public char[] getNameCharArray() {
		return name.toCharArray();
	}

	@Override
	public IScope getScope() {
		return tu.getScope();
	}

	@Override
	public boolean isExtern() {
		return true;
	}
}
