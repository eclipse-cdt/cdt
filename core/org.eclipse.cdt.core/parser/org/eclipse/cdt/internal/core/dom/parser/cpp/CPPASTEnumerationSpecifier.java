/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalEnumerationSpecifier;

/**
 * AST node for C++ enumeration specifiers.
 */
public class CPPASTEnumerationSpecifier extends CPPASTBaseDeclSpecifier
		implements IASTInternalEnumerationSpecifier, ICPPASTEnumerationSpecifier {
	private ScopeStyle fScopeStyle;
	private boolean fIsOpaque;
	private IASTName fName;
	private ICPPASTDeclSpecifier fBaseType;

	private IASTEnumerator[] fEnumerators = IASTEnumerator.EMPTY_ENUMERATOR_ARRAY;
	private int fNumEnumerators;

	private Boolean fValuesComputed;
	private CPPEnumScope fScope;

	public CPPASTEnumerationSpecifier() {
	}

	@Deprecated
	public CPPASTEnumerationSpecifier(boolean isScoped, IASTName name, ICPPASTDeclSpecifier baseType) {
		this(isScoped ? ScopeStyle.CLASS : ScopeStyle.NONE, name, baseType);
	}

	public CPPASTEnumerationSpecifier(ScopeStyle scopeStyle, IASTName name, ICPPASTDeclSpecifier baseType) {
		setScopeStyle(scopeStyle);
		setName(name);
		setBaseType(baseType);
	}

	@Override
	public CPPASTEnumerationSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTEnumerationSpecifier copy(CopyStyle style) {
		CPPASTEnumerationSpecifier copy = new CPPASTEnumerationSpecifier(fScopeStyle,
				fName == null ? null : fName.copy(style), fBaseType == null ? null : fBaseType.copy(style));
		copy.fIsOpaque = fIsOpaque;
		for (IASTEnumerator enumerator : getEnumerators()) {
			copy.addEnumerator(enumerator == null ? null : enumerator.copy(style));
		}
		return super.copy(copy, style);
	}

	@Override
	public boolean startValueComputation() {
		if (fValuesComputed != null)
			return false;

		fValuesComputed = Boolean.FALSE;
		return true;
	}

	@Override
	public void finishValueComputation() {
		fValuesComputed = Boolean.TRUE;
	}

	@Override
	public boolean isValueComputationInProgress() {
		return fValuesComputed != null && !fValuesComputed;
	}

	@Override
	public void addEnumerator(IASTEnumerator enumerator) {
		assertNotFrozen();
		if (enumerator != null) {
			enumerator.setParent(this);
			enumerator.setPropertyInParent(ENUMERATOR);
			fEnumerators = ArrayUtil.appendAt(fEnumerators, fNumEnumerators++, enumerator);
		}
	}

	@Override
	public IASTEnumerator[] getEnumerators() {
		fEnumerators = ArrayUtil.trim(fEnumerators, fNumEnumerators);
		return fEnumerators;
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		fName = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ENUMERATION_NAME);
		}
	}

	@Override
	public IASTName getName() {
		return fName;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (fName != null && !fName.accept(action))
			return false;

		if (fBaseType != null && !fBaseType.accept(action)) {
			return false;
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;

		for (IASTEnumerator e : getEnumerators()) {
			if (!e.accept(action))
				return false;
		}

		if (action.shouldVisitDeclSpecifiers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public int getRoleForName(IASTName n) {
		if (fName == n)
			return isOpaque() ? r_declaration : r_definition;
		return r_unclear;
	}

	@Override
	@Deprecated
	public void setIsScoped(boolean isScoped) {
		setScopeStyle(isScoped ? ScopeStyle.CLASS : ScopeStyle.NONE);
	}

	@Override
	public void setScopeStyle(ScopeStyle scopeStyle) {
		assertNotFrozen();
		fScopeStyle = scopeStyle;
	}

	@Override
	public ScopeStyle getScopeStyle() {
		return fScopeStyle;
	}

	@Override
	public boolean isScoped() {
		return fScopeStyle != ScopeStyle.NONE;
	}

	@Override
	public void setBaseType(ICPPASTDeclSpecifier baseType) {
		assertNotFrozen();
		fBaseType = baseType;
		if (baseType != null) {
			baseType.setParent(this);
			baseType.setPropertyInParent(BASE_TYPE);
		}
	}

	@Override
	public ICPPASTDeclSpecifier getBaseType() {
		return fBaseType;
	}

	@Override
	public void setIsOpaque(boolean isOpaque) {
		assertNotFrozen();
		fIsOpaque = isOpaque;
	}

	@Override
	public boolean isOpaque() {
		return fIsOpaque;
	}

	@Override
	public ICPPScope getScope() {
		if (isOpaque())
			return null;
		if (fScope == null) {
			fScope = new CPPEnumScope(this);
		}
		return fScope;
	}
}
