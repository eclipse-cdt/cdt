/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser.BinaryOperator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.NameOrTemplateIDVariants.BranchPoint;
import org.eclipse.cdt.internal.core.dom.parser.cpp.NameOrTemplateIDVariants.Variant;

/**
 * Models expression variants for the ambiguity of a template id.
 */
public class CPPASTTemplateIDAmbiguity extends ASTAmbiguousNode implements IASTAmbiguousExpression {

	private BinaryOperator fLastOperator;
	private IASTInitializerClause fLastExpression;
	private final BranchPoint fVariants;
	private IASTNode[] fNodes;
	private AbstractGNUSourceCodeParser fParser;

	public CPPASTTemplateIDAmbiguity(AbstractGNUSourceCodeParser parser, BinaryOperator lastOperator, IASTInitializerClause expr,
			BranchPoint variants) {
		fParser= parser;
		fLastOperator= lastOperator;
		fLastExpression= expr;
		fVariants= variants;
	}

	@Override
	protected final IASTNode doResolveAmbiguity(ASTVisitor resolver) {
		final IASTAmbiguityParent owner= (IASTAmbiguityParent) getParent();
		IASTNode nodeToReplace= this;
		
		// Try all variants and under the ones with correct template-ids select the one with 
		// the most template-ids.
		int minOffset= -1;
		for (BranchPoint v= fVariants; v != null; v= v.getNext()) {
			Variant selected= null;
			int bestCount= 0;
			for (Variant q= v.getFirstVariant(); q != null ; q=q.getNext()) {
				final IASTName[] templateNames = q.getTemplateNames();
				if (templateNames.length > bestCount) {
					// Don't check branch-points inside of a selected variant.
					final IASTExpression expression = q.getExpression();
					if (((ASTNode) expression).getOffset() < minOffset)
						break;

					// Setup the ast to use the alternative
					owner.replace(nodeToReplace, expression);
					nodeToReplace= resolveNestedAmbiguities(expression, resolver);

					int count= checkNames(templateNames);
					if (count > bestCount) {
						selected= q;
						bestCount= count;
					}
				}
			}
			
			// Adjust the operator sequence
			if (selected != null) {
				minOffset= selected.getRightOffset();
				BinaryOperator targetOp = selected.getTargetOperator();
				if (targetOp == null) {
					fLastExpression= selected.getExpression();
					fLastOperator= v.getLeftOperator();
				} else {
					targetOp.exchange(selected.getExpression());
					targetOp.setNext(v.getLeftOperator());
				}
			}
		}
		
		// Important: Before building the expression remove it from the owner
		owner.replace(nodeToReplace, this);
		
		// Create the expression and replace it
		IASTExpression expr = fParser.buildExpression(fLastOperator, fLastExpression);
		owner.replace(this, expr);
		
		// Resolve further ambiguities within the new expression.
		expr.accept(resolver);
		return expr;
	}

	private int checkNames(final IASTName[] templateNames) {
		int count= 0;
		for (IASTName templateName : templateNames) {
			if (templateName.getTranslationUnit() != null) {
				IBinding b= templateName.resolveBinding();
				if (b instanceof IProblemBinding) {
					if (!containsFunctionTemplate(((IProblemBinding) b).getCandidateBindings())) 
						return -1;
					count++;
				} else if (b instanceof ICPPSpecialization || b instanceof ICPPTemplateDefinition
						|| b instanceof ICPPConstructor 
						|| (b instanceof IFunction && b instanceof ICPPUnknownBinding)) {
					count++;
				} else {
					return -1;
				}
			}
		}
		return count;
	}
	
	private boolean containsFunctionTemplate(IBinding[] candidateBindings) {
		for (IBinding cand : candidateBindings) {
			if (cand instanceof ICPPFunctionTemplate || 
					(cand instanceof ICPPFunction && cand instanceof ICPPSpecialization)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IASTNode[] getNodes() {
		if (fNodes == null) {
			List<IASTNode> nl= new ArrayList<IASTNode>();
			nl.add(fLastExpression);
			BinaryOperator op= fLastOperator;
			while (op != null) {
				nl.add(op.getExpression());
				op= op.getNext();
			}
			Collections.reverse(nl);
			fNodes= nl.toArray(new IASTNode[nl.size()]);
		}
		return fNodes;
	}

	@Override
	public IASTExpression copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTExpression copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addExpression(IASTExpression e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTExpression[] getExpressions() {
		throw new UnsupportedOperationException();
	}
}
