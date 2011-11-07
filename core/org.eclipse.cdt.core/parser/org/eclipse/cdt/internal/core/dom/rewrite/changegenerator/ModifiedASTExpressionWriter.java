/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ExpressionWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.MacroExpansionHandler;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.Scribe;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTExpressionWriter extends ExpressionWriter {
	private final ASTModificationHelper modificationHelper;

	public ModifiedASTExpressionWriter(Scribe scribe, ASTWriterVisitor visitor, MacroExpansionHandler macroHandler,
			ModificationScopeStack stack, NodeCommentMap commentMap) {
		super(scribe, visitor, macroHandler, commentMap);
		this.modificationHelper = new ASTModificationHelper(stack);
	}

	@Override
	protected void writeExpressions(IASTExpressionList expList, IASTExpression[] expressions) {
		IASTExpression[] modifiedExpressions = modificationHelper.createModifiedChildArray(expList,
				expressions, IASTExpression.class, commentMap);
		super.writeExpressions(expList, modifiedExpressions);
	}

	@Override
	protected IASTInitializer getNewInitializer(ICPPASTNewExpression newExp) {
		IASTInitializer initializer = newExp.getInitializer();
		if (initializer != null) {
			for (ASTModification childModification : modificationHelper.modificationsForNode(initializer)) {
				switch (childModification.getKind()) {
				case REPLACE:
					if (childModification.getNewNode() instanceof IASTInitializer) {
						return (IASTInitializer) childModification.getNewNode();
					}
					break;
				case INSERT_BEFORE:
					throw new UnhandledASTModificationException(childModification);

				case APPEND_CHILD:
					throw new UnhandledASTModificationException(childModification);
				}
			}
		} else {
			for (ASTModification parentModification : modificationHelper.modificationsForNode(newExp)) {
				if (parentModification.getKind() == ModificationKind.APPEND_CHILD) {
					IASTNode newNode = parentModification.getNewNode();
					if (newNode instanceof IASTInitializer) {
						return (IASTInitializer) newNode;
					}
				}
			}
		}
		return initializer;
	}
}
