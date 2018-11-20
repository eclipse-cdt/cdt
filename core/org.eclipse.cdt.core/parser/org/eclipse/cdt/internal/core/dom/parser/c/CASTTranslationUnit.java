/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * C-specific implementation of a translation unit.
 */
public class CASTTranslationUnit extends ASTTranslationUnit implements IASTAmbiguityParent {
	private CScope compilationUnit;
	private final CStructMapper fStructMapper;

	public CASTTranslationUnit() {
		fStructMapper = new CStructMapper(this);
	}

	@Override
	public CASTTranslationUnit copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTTranslationUnit copy(CopyStyle style) {
		CASTTranslationUnit copy = new CASTTranslationUnit();
		return copy(copy, style);
	}

	@Override
	public IScope getScope() {
		if (compilationUnit == null)
			compilationUnit = new CScope(this, EScopeKind.eGlobal);
		return compilationUnit;
	}

	@Override
	public IASTName[] getDeclarationsInAST(IBinding binding) {
		if (binding instanceof IMacroBinding) {
			return getMacroDefinitionsInAST((IMacroBinding) binding);
		}
		return CVisitor.getDeclarations(this, binding);
	}

	@Override
	public IASTName[] getDefinitionsInAST(IBinding binding) {
		return getDefinitionsInAST(binding, false);
	}

	@Override
	public IASTName[] getDefinitionsInAST(IBinding binding, boolean permissive) {
		if (binding instanceof IMacroBinding) {
			return getMacroDefinitionsInAST((IMacroBinding) binding);
		}
		// TODO: Implement support for permissive=true.
		IASTName[] names = CVisitor.getDeclarations(this, binding);
		for (int i = 0; i < names.length; i++) {
			if (!names[i].isDefinition())
				names[i] = null;
		}
		// nulls can be anywhere, don't use trim()
		return ArrayUtil.removeNulls(IASTName.class, names);
	}

	@Override
	public IASTName[] getReferences(IBinding binding) {
		if (binding instanceof IMacroBinding)
			return getMacroReferencesInAST((IMacroBinding) binding);
		return CVisitor.getReferences(this, binding);
	}

	@Override
	@Deprecated
	public ParserLanguage getParserLanguage() {
		return ParserLanguage.C;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public void resolveAmbiguities() {
		accept(new CASTAmbiguityResolver());
	}

	@Override
	public IScope mapToASTScope(IScope scope) {
		if (scope instanceof IIndexScope) {
			if (scope.getKind() == EScopeKind.eGlobal)
				return getScope();
			if (scope instanceof ICCompositeTypeScope) {
				ICompositeType type = ((ICCompositeTypeScope) scope).getCompositeType();
				type = mapToASTType(type);
				return type.getCompositeScope();
			}
		}
		return scope;
	}

	/**
	 * Maps structs from the index into this AST.
	 */
	public ICompositeType mapToASTType(ICompositeType type) {
		return fStructMapper.mapToAST(type);
	}

	@Override
	protected IType createType(IASTTypeId typeid) {
		return CVisitor.createType(typeid.getAbstractDeclarator());
	}
}
