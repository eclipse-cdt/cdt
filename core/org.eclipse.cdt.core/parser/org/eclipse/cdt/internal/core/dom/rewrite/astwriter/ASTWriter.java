/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
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
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * 
 * ASTWriter main class. Generates source code from <code>IASTNode</code>. Uses a
 * <codeC>hangeGeneratorWriterVisitor</code> to generate the code for the given
 * nodes.
 * 
 * @see ChangeGeneratorWriterVisitor
 * 
 * @author Emanuel Graf
 * 
 */
public class ASTWriter {
	
	private ChangeGeneratorWriterVisitor transformationVisitor;
	private ASTModificationStore modificationStore = new ASTModificationStore();
	private String givenIndentation = ""; //$NON-NLS-1$
	

	/**
	 * Creates a <code>ASTWriter</code>.
	 */
	public ASTWriter() {
		super();
	}

	/**
	 * Creates a <code>ASTWriter</code> that indents the code.
	 * 
	 * @param givenIndentation The indentation added to each line
	 */
	public ASTWriter(String givenIndentation) {
		super();
		this.givenIndentation = givenIndentation;
	}

	/**
	 * 
	 * Genereates the source code representing this node.
	 * 
	 * @param rootNode Node to write.
	 * @return A <code>String</code> representing the source code for the node.
	 * @throws ProblemRuntimeException if the node or one of it's children is a <code>IASTProblemNode</code>.
	 */
	public String write(IASTNode rootNode) throws ProblemRuntimeException {
		return write(rootNode, null, new NodeCommentMap());
	}
	
	/**
	 * 
	 * Generates the source code representing this node including comments.
	 * 
	 * @param rootNode Node to write.
	 * @param fileScope
	 * @param commentMap Node Comment Map <code>ASTCommenter</code>
	 * @return A <code>String</code> representing the source code for the node.
	 * @throws ProblemRuntimeException if the node or one of it's children is a <code>IASTProblemNode</code>.
	 * 
	 * @see ASTCommenter#getCommentedNodeMap(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
	 */
	public String write(IASTNode rootNode, String fileScope, NodeCommentMap commentMap) throws ProblemRuntimeException {
		transformationVisitor = new ChangeGeneratorWriterVisitor(modificationStore, givenIndentation, fileScope, commentMap);
		if(rootNode != null){
			rootNode.accept(transformationVisitor);
		}
		String str = transformationVisitor.toString();
		transformationVisitor.cleanCache();
		return str;
	}
	

	public void setModificationStore(ASTModificationStore modificationStore) {
		this.modificationStore = modificationStore;
	}	

}
