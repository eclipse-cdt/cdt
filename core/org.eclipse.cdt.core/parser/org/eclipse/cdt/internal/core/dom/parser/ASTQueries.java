/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Base class for {@link CVisitor} and {@link CPPVisitor}
 */
public class ASTQueries {
	private static class NameSearch extends ASTVisitor {
		private boolean fFound;
		NameSearch() {
			super(false);
			shouldVisitAmbiguousNodes= true;
			shouldVisitNames= true;
		}
		public void reset() {
			fFound= false;
		}
		public boolean foundName() {
			return fFound;
		}
		@Override
		public int visit(IASTName name) {
			fFound= true;
			return PROCESS_ABORT;
		}
		@Override
		public int visit(ASTAmbiguousNode node) {
			IASTNode[] alternatives= node.getNodes();
			for (IASTNode alt : alternatives) {
				if (!alt.accept(this))
					return PROCESS_ABORT;
			}
			return PROCESS_CONTINUE;
		}
	}
	private static NameSearch NAME_SEARCH= new NameSearch();
	
	/**
	 * Tests whether the given expression can contain ast-names, suitable to be used before ambiguity 
	 * resolution.
	 */
	public static boolean canContainName(IASTExpression expr) {
		if (expr == null)
			return false;
		
		NAME_SEARCH.reset();
		expr.accept(NAME_SEARCH);
		return NAME_SEARCH.foundName();
	}
	
	/** 
	 * Returns the outermost declarator the given <code>declarator</code> nests within, or
	 * <code>declarator</code> itself.
	 */
	public static IASTDeclarator findOutermostDeclarator(IASTDeclarator declarator) {
		IASTDeclarator outermost= null;
		IASTNode candidate= declarator;
		while (candidate instanceof IASTDeclarator) {
			outermost= (IASTDeclarator) candidate;
			candidate= outermost.getParent();
		}
		return outermost;
	}

	/** 
	 * Returns the innermost declarator nested within the given <code>declarator</code>, or
	 * <code>declarator</code> itself.
	 */
	public static IASTDeclarator findInnermostDeclarator(IASTDeclarator declarator) {
		IASTDeclarator innermost= null;
		while (declarator != null) {
			innermost= declarator;
			declarator= declarator.getNestedDeclarator();
		}
		return innermost;
	}

	/**
	 * Searches for the innermost declarator that contributes the the type declared.
	 */
	public static IASTDeclarator findTypeRelevantDeclarator(IASTDeclarator declarator) {
		if (declarator == null)
			return null;
		
		IASTDeclarator result= findInnermostDeclarator(declarator);
		while (result.getPointerOperators().length == 0 
				&& !(result instanceof IASTFieldDeclarator)
				&& !(result instanceof IASTFunctionDeclarator)
				&& !(result instanceof IASTArrayModifier)) {
			final IASTNode parent= result.getParent();
			if (parent instanceof IASTDeclarator) {
				result= (IASTDeclarator) parent;
			} else {
				return result;
			}
		}
		return result;
	}
	
	/**
	 * Extracts the active declarations from an array of declarations.
	 */
	public static IASTDeclaration[] extractActiveDeclarations(final IASTDeclaration[] allDeclarations, final int size) {
		IASTDeclaration[] active;
		if (size == 0) {
			active= IASTDeclaration.EMPTY_DECLARATION_ARRAY;
		} else {
			active= new IASTDeclaration[size];
			int j= 0;
			for (int i = 0; i < size; i++) {
				IASTDeclaration d= allDeclarations[i];
				if (d.isActive()) {
					active[j++]= d;
				}
			}
			active= ArrayUtil.trimAt(IASTDeclaration.class, active, j-1);
		}
		return active;
	}

	public static boolean isSameType(IType type1, IType type2) {
		if (type1 == type2)
			return true;
		if (type1 == null || type2 == null)
			return false;
		return type1.isSameType(type2);
	}
	
	protected static IType isCompatibleArray(IType t1, IType t2) {
		if (t1 instanceof IArrayType && t2 instanceof IArrayType) {
			IArrayType a1 = (IArrayType) t1;
			IArrayType a2 = (IArrayType) t2;
			if (!isSameType(a1.getType(), a2.getType())) {
				return null;
			}
			if (a1.getSize() == null) {
				if (a2.getSize() != null) {
					return a2;
				}
			} else if (a2.getSize() == null) {
				return a1;
			}
		}
		return null;
	}
}
