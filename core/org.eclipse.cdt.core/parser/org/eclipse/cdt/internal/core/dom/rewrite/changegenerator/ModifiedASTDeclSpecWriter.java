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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.DeclSpecWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.Scribe;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTDeclSpecWriter extends DeclSpecWriter {
	private final ASTModificationHelper modificationHelper;
	
	public ModifiedASTDeclSpecWriter(Scribe scribe, ASTWriterVisitor visitor,
			ModificationScopeStack stack, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
		this.modificationHelper = new ASTModificationHelper(stack);
	}

	@Override
	protected IASTDeclaration[] getMembers(IASTCompositeTypeSpecifier compDeclSpec) {
		return modificationHelper.createModifiedChildArray(compDeclSpec, compDeclSpec.getMembers(),
				IASTDeclaration.class, commentMap);
	}
}
