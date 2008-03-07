/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.ChangeGeneratorWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * @author Emanuel Graf
 *
 */
public class ASTWriter {
	
	private ChangeGeneratorWriterVisitor transformationVisitor;
	private ASTModificationStore modificationStore = new ASTModificationStore();
	private String givenIndentation = ""; //$NON-NLS-1$
	

	public ASTWriter() {
		super();
	}

	public ASTWriter(String givenIndentation) {
		super();
		this.givenIndentation = givenIndentation;
	}

	public String write(IASTNode rootNode) throws ProblemRuntimeException {
		return write(rootNode, null, new NodeCommentMap());
	}
	
	public String write(IASTNode rootNode, String fileScope, NodeCommentMap commentMap) throws ProblemRuntimeException {
		transformationVisitor = new ChangeGeneratorWriterVisitor(modificationStore, givenIndentation, fileScope, commentMap);
		rootNode.accept(transformationVisitor);
		String str = transformationVisitor.toString();
		transformationVisitor.cleanCache();
		return str;
	}
	
	

	public void setModificationStore(ASTModificationStore modificationStore) {
		this.modificationStore = modificationStore;
	}	

}
