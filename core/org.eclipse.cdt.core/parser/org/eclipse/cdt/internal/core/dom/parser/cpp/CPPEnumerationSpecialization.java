/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumerationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Binding for a specialization of an enumeration.
 */
public class CPPEnumerationSpecialization extends CPPSpecialization implements ICPPEnumerationSpecialization {
	private IEnumerator[] fEnumerators;
	private final IType fFixedType;

	public CPPEnumerationSpecialization(ICPPEnumeration specialized, IBinding owner,
			ICPPTemplateParameterMap argumentMap, IType fixedType) {
		super(specialized, owner, argumentMap);
		fFixedType = fixedType;
	}

	public void setEnumerators(IEnumerator[] enumerators) {
		fEnumerators = enumerators;
	}

	@Override
	public ICPPEnumeration getSpecializedBinding() {
		return (ICPPEnumeration) super.getSpecializedBinding();
	}

	@Override
	public IEnumerator[] getEnumerators() {
		return fEnumerators;
	}

	@Override
	public long getMinValue() {
		return SemanticUtil.computeMinValue(this);
	}

	@Override
	public long getMaxValue() {
		return SemanticUtil.computeMaxValue(this);
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);
		if (!(type instanceof ICPPEnumerationSpecialization))
			return false;
		ICPPEnumerationSpecialization otherEnumSpec = (ICPPEnumerationSpecialization) type;
		return getSpecializedBinding().isSameType(otherEnumSpec.getSpecializedBinding())
			&& ((IType) getOwner()).isSameType((IType) otherEnumSpec.getOwner());
	}

	@Override
	public boolean isScoped() {
		return getSpecializedBinding().isScoped();
	}

	@Override
	public IType getFixedType() {
		return fFixedType;
	}

	@Override
	public ICPPScope asScope() {
		// TODO(nathanridge): Do we need a CPPEnumSpecializationScope?
		return getSpecializedBinding().asScope();
	}

	@Override
	public Object clone() {
    	throw new IllegalArgumentException("Enums must not be cloned"); //$NON-NLS-1$
	}

	@Override
	public IEnumerator specializeEnumerator(IEnumerator enumerator) {
		if (enumerator instanceof ICPPSpecialization && ((ICPPSpecialization) enumerator).getOwner() == this) {
			return enumerator;
		}

		// The specialized enumerators are already computed, just need
		// to look up the right one.
		IEnumerator[] unspecializedEnumerators = getSpecializedBinding().getEnumerators();
		for (int i = 0; i < fEnumerators.length; ++i) {
			if (enumerator.equals(unspecializedEnumerators[i]))
				return fEnumerators[i];
		}
		return null;
	}
}
