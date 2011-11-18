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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.DeclarationWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.Scribe;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTDeclarationWriter extends DeclarationWriter {
	private final ASTModificationHelper modificationHelper;
	
	public ModifiedASTDeclarationWriter(Scribe scribe, ASTWriterVisitor visitor,
			ModificationScopeStack stack, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
		this.modificationHelper = new ASTModificationHelper(stack);
	}

	@Override
	protected void writeDeclarationsInNamespace(ICPPASTNamespaceDefinition namespaceDefinition,
			IASTDeclaration[] declarations) {
		IASTDeclaration[] modifiedDeclarations = modificationHelper.createModifiedChildArray(
				namespaceDefinition, declarations, IASTDeclaration.class, commentMap);
		super.writeDeclarationsInNamespace(namespaceDefinition, modifiedDeclarations);
	}
	
	@Override
	protected void writeCtorChainInitializer(ICPPASTFunctionDefinition funcDec,
			ICPPASTConstructorChainInitializer[] ctorInitChain) {
		ICPPASTConstructorChainInitializer[] modifiedInitializer =
				modificationHelper.createModifiedChildArray(funcDec, ctorInitChain,
						ICPPASTConstructorChainInitializer.class, commentMap);
		super.writeCtorChainInitializer(funcDec, modifiedInitializer);
	}
}
