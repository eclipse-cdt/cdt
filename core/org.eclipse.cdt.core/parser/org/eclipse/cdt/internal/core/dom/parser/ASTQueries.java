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

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Base class for {@link CVisitor} and {@link CPPVisitor}
 */
public class ASTQueries {
	/**
	 * Tests whether the given expression can contain ast-names, suitable to be used before ambiguity 
	 * resolution.
	 */
	public static boolean canContainName(IASTExpression expr) {
		if (expr == null || expr instanceof IASTLiteralExpression) 
			return false;

		if (expr instanceof IASTAmbiguousExpression) 
			return true;
		if (expr instanceof IASTIdExpression) 
			return true;
		if (expr instanceof IASTCastExpression)
			return true;
		
		if (expr instanceof IASTUnaryExpression) {
			IASTUnaryExpression uexpr= (IASTUnaryExpression) expr;
			return canContainName(uexpr.getOperand());
		}
		if (expr instanceof IASTBinaryExpression) {
			IASTBinaryExpression bexpr= (IASTBinaryExpression) expr;
			return canContainName(bexpr.getOperand1()) || canContainName(bexpr.getOperand2());
		}
		if (expr instanceof IASTConditionalExpression) {
			IASTConditionalExpression cexpr= (IASTConditionalExpression) expr;
			return canContainName(cexpr.getLogicalConditionExpression()) || 
					canContainName(cexpr.getPositiveResultExpression()) || canContainName(cexpr.getNegativeResultExpression());
		}
		if (expr instanceof IASTExpressionList) {
			IASTExpressionList lexpr= (IASTExpressionList) expr;
			IASTExpression[] subexprs= lexpr.getExpressions();
			for (IASTExpression subexpr : subexprs) {
				if (canContainName(subexpr))
					return true;
			}
		}
		return true;
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
}
