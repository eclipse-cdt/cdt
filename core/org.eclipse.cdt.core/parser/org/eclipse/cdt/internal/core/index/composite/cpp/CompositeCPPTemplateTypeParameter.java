/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPTemplateTypeParameter extends CompositeCPPBinding
		implements ICPPTemplateTypeParameter, ICPPUnknownBinding, ICPPUnknownType, IIndexType {

	private ICPPScope unknownScope;

	public CompositeCPPTemplateTypeParameter(ICompositesFactory cf, ICPPTemplateTypeParameter binding) {
		super(cf, binding);
	}

	@Override
	public IType getDefault() throws DOMException {
		IType preresult = ((ICPPTemplateTypeParameter) rbinding).getDefault();
		return cf.getCompositeType(preresult);
	}

	@Override
	public short getParameterPosition() {
		return ((ICPPTemplateParameter) rbinding).getParameterPosition();
	}

	@Override
	public short getTemplateNestingLevel() {
		return ((ICPPTemplateParameter) rbinding).getTemplateNestingLevel();
	}

	@Override
	public int getParameterID() {
		return ((ICPPTemplateParameter) rbinding).getParameterID();
	}

	@Override
	public boolean isParameterPack() {
		return ((ICPPTemplateParameter) rbinding).isParameterPack();
	}

	@Override
	public boolean isSameType(IType type) {
		return ((IType) rbinding).isSameType(type);
	}

	@Override
	public Object clone() {
		fail();
		return null;
	}

	@Override
	public ICPPScope asScope() {
		if (unknownScope == null) {
			unknownScope = new CompositeCPPUnknownScope(this, new CPPASTName(getNameCharArray()));
		}
		return unknownScope;
	}

	@Override
	public ICPPTemplateArgument getDefaultValue() {
		try {
			return TemplateInstanceUtil.convert(cf, ((ICPPTemplateTypeParameter) rbinding).getDefaultValue());
		} catch (DOMException e) {
			return null;
		}
	}
}
