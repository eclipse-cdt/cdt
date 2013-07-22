/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;

/**
 * A specialization of a type template parameter. This is needed when a nested template
 * has a type template parameter whose default value is dependent on a template
 * parameter of an enclosing template.
 *
 * This class can represent a specialization of either an AST or a PDOM template parameter.
 */
public class CPPTemplateTypeParameterSpecialization extends CPPTemplateParameterSpecialization
		implements ICPPTemplateTypeParameter {

	public CPPTemplateTypeParameterSpecialization(ICPPSpecialization owner, ICPPScope scope,
			ICPPTemplateTypeParameter specialized, ICPPTemplateArgument defaultValue) {
		super(owner, scope, specialized, defaultValue);
	}

	@Override
	public ICPPTemplateTypeParameter getSpecializedBinding() {
		return (ICPPTemplateTypeParameter) super.getSpecializedBinding();
	}

	@Override
	public IType getDefault() throws DOMException {
		return getDefaultValue().getTypeValue();
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);
		if (!(type instanceof ICPPTemplateTypeParameter))
			return false;

		return getParameterID() == ((ICPPTemplateParameter) type).getParameterID();
	}

	@Override
	public Object clone() {
        Object o = null;
   		try {
            o = super.clone();
        } catch (CloneNotSupportedException e) {
            //not going to happen
        }
        return o;
    }
}
