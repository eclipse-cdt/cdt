/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.inlinetemp;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

public class ExpressionOperatorCounter {
	
	public static int count(IASTNode start) {
		while (start instanceof IASTExpression && start.getParent() != null) {
			start = start.getParent();
		}
		final Set<Integer> unOps = new HashSet<Integer>();
		final Set<Integer> binOps = new HashSet<Integer>();
		start.accept(new ASTVisitor() {
			{
				this.shouldVisitExpressions = true;
			}
			
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTUnaryExpression) {
					final IASTUnaryExpression unary = (IASTUnaryExpression) expression;
					unOps.add(unary.getOperator());
				} else if (expression instanceof IASTBinaryExpression) {
					final IASTBinaryExpression binary = (IASTBinaryExpression) expression;
					binOps.add(binary.getOperator());
				}
				return super.visit(expression);
			}
		});
		return unOps.size() + binOps.size();
	}
}
