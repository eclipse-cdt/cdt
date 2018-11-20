/*******************************************************************************
 * Copyright (c) 2013, 2014 Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *     Marc-Andre Laperle (Ericsson)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumerationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Binding for a specialization of an enumeration.
 */
public class CPPEnumerationSpecialization extends CPPSpecialization implements ICPPEnumerationSpecialization {
	private final IEnumerator[] fEnumerators;
	private final IType fFixedType;
	private boolean fInitializationComplete;

	public static IBinding createInstance(ICPPEnumeration enumeration, ICPPClassSpecialization owner,
			ICPPTemplateParameterMap tpMap) {
		IType fixedType = enumeration.getFixedType();
		if (fixedType != null) {
			ICPPClassSpecialization within = CPPTemplates.getSpecializationContext(owner);
			InstantiationContext context = new InstantiationContext(tpMap, within);
			fixedType = CPPTemplates.instantiateType(fixedType, context);
		}
		CPPEnumerationSpecialization specializedEnumeration = new CPPEnumerationSpecialization(enumeration, owner,
				tpMap, fixedType);
		specializedEnumeration.initialize();
		return specializedEnumeration;
	}

	private CPPEnumerationSpecialization(ICPPEnumeration specialized, IBinding owner,
			ICPPTemplateParameterMap argumentMap, IType fixedType) {
		super(specialized, owner, argumentMap);
		fFixedType = fixedType;
		fEnumerators = new IEnumerator[specialized.getEnumerators().length];
	}

	private void initialize() {
		ICPPTemplateParameterMap tpMap = getTemplateParameterMap();
		IEnumerator[] enumerators = getSpecializedBinding().getEnumerators();
		IType previousInternalType = CPPBasicType.INT;
		for (int i = 0; i < enumerators.length; ++i) {
			IEnumerator enumerator = enumerators[i];
			InstantiationContext context = new InstantiationContext(tpMap, this);
			IValue specializedValue = CPPTemplates.instantiateValue(enumerator.getValue(), context,
					IntegralValue.MAX_RECURSION_DEPTH);
			IType internalType = null;
			if (fFixedType == null && enumerator instanceof ICPPInternalEnumerator) {
				internalType = ((ICPPInternalEnumerator) enumerator).getInternalType();
				if (internalType != null) {
					internalType = CPPTemplates.instantiateType(internalType, context);
				} else if (previousInternalType instanceof IBasicType) {
					internalType = ASTEnumerator.getTypeOfIncrementedValue((IBasicType) previousInternalType,
							specializedValue);
				}
				if (internalType != null) {
					previousInternalType = internalType;
				}
			}
			fEnumerators[i] = new CPPEnumeratorSpecialization(enumerator, this, tpMap, specializedValue, internalType);
		}
		fInitializationComplete = true;
	}

	public boolean isInitializing() {
		return !fInitializationComplete;
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

		// The specialized enumerators are already computed, just need to look up the right one.
		IEnumerator[] unspecializedEnumerators = getSpecializedBinding().getEnumerators();
		for (int i = 0; i < fEnumerators.length; ++i) {
			if (enumerator.equals(unspecializedEnumerators[i])) {
				IEnumerator specializedEnumerator = fEnumerators[i];
				return specializedEnumerator == null ? enumerator : specializedEnumerator;
			}
		}
		return enumerator;
	}
}
