/*******************************************************************************
 * Copyright (c) 2008, 2012 Symbian Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPTemplateNonTypeParameter extends CompositeCPPVariable implements ICPPTemplateNonTypeParameter {
	public CompositeCPPTemplateNonTypeParameter(ICompositesFactory cf, ICPPTemplateNonTypeParameter binding) {
		super(cf, binding);
	}

	public boolean isSameType(IType type) {
		return ((IType) rbinding).isSameType(type);
	}

	@Override
	public Object clone() {
		fail();
		return null;
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
	public ICPPTemplateArgument getDefaultValue() {
		try {
			return TemplateInstanceUtil.convert(cf, ((ICPPTemplateNonTypeParameter) rbinding).getDefaultValue());
		} catch (DOMException e) {
			return null;
		}
	}

	@Override
	@Deprecated
	public IASTExpression getDefault() {
		return null;
	}
}
