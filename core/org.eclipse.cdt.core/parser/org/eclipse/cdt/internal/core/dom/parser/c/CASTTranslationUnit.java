/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * C-specific implementation of a translation unit.
 */
public class CASTTranslationUnit extends ASTTranslationUnit implements IASTAmbiguityParent {
	private CScope compilationUnit = null;
	private CStructMapper fStructMapper;

	public CASTTranslationUnit() {
		fStructMapper= new CStructMapper(this);
	}
	
	public CASTTranslationUnit copy() {
		CASTTranslationUnit copy = new CASTTranslationUnit();
		copyAbstractTU(copy);
		return copy;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getScope()
	 */
	public IScope getScope() {
		if (compilationUnit == null)
			compilationUnit = new CScope(this, EScopeKind.eGlobal);
		return compilationUnit;
	}


	public IASTName[] getDeclarationsInAST(IBinding binding) {
		if (binding instanceof IMacroBinding) {
			return getMacroDefinitionsInAST((IMacroBinding) binding);
        }
		return CVisitor.getDeclarations(this, binding);
	}

    public IASTName[] getDefinitionsInAST(IBinding binding) {   
		if (binding instanceof IMacroBinding) {
			return getMacroDefinitionsInAST((IMacroBinding) binding);
        }
    	IName[] names = CVisitor.getDeclarations(this, binding);
    	for (int i = 0; i < names.length; i++) {
    		if (!names[i].isDefinition())
    			names[i] = null;
    	}
    	// nulls can be anywhere, don't use trim()
    	return (IASTName[])ArrayUtil.removeNulls(IASTName.class, names);
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getReferences(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public IASTName[] getReferences(IBinding binding) {
        if (binding instanceof IMacroBinding)
        	return getMacroReferencesInAST((IMacroBinding) binding);
		return CVisitor.getReferences(this, binding);
	}

	@Deprecated
    public ParserLanguage getParserLanguage() {
    	return ParserLanguage.C;
    }

	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public void resolveAmbiguities() {
		accept(new CASTAmbiguityResolver()); 
	}

	/**
	 * Maps structs from the index into this AST.
	 */
	public IType mapToASTType(ICompositeType type) {
		return fStructMapper.mapToAST(type);
	}
}
