/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;

/**
 * A visitor for the comments. Calls the NodeCommenter to assign the comments.
 * 
 * @see org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommenter 
 *   
 * @author Guido Zgraggen IFS 
 */
public class ASTCommenterVisitor extends ASTVisitor {
	protected CommentHandler commHandler;
	protected NodeCommentMap commentMap;
		
	private NodeCommenter nodeCommenter;
	
	{
		shouldVisitBaseSpecifiers = true;
		shouldVisitDeclarations = true;
		shouldVisitDeclarators = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitExpressions = true;
		shouldVisitInitializers = true;
		shouldVisitNames = true;
		shouldVisitNamespaces = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitStatements = true;
		shouldVisitTemplateParameters = true;
		shouldVisitTypeIds = true;
	}
	
	public ASTCommenterVisitor(CommentHandler commHandler, NodeCommentMap commentMap) {
		this.commHandler = commHandler;
		this.commentMap = commentMap;
		init();
	}

	private void init() {
		nodeCommenter = new NodeCommenter(this, commHandler, commentMap);
	}
	
	public void addRemainingComments(IASTDeclaration declaration) {
		nodeCommenter.appendRemainingComments(declaration);
	}
	
	@Override
	public int visit(IASTName name) {
		return nodeCommenter.appendComments((ASTNode) name);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		return nodeCommenter.appendComments((ASTNode) declSpec);
	}

	@Override
	public int visit(IASTExpression expression) {
		return nodeCommenter.appendComments((ASTNode) expression);
	}

	@Override
	public int visit(IASTStatement statement) {
		return nodeCommenter.appendComments((ASTNode) statement);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		return nodeCommenter.appendComments((ASTNode) typeId);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		return nodeCommenter.appendComments((ASTNode) declaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		return nodeCommenter.appendComments((ASTNode) declarator);		
	}

	@Override
	public int visit(IASTInitializer initializer) {
		return nodeCommenter.appendComments((ASTNode) initializer);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return nodeCommenter.appendComments((ASTNode) parameterDeclaration);
	}
	
	@Override
	public int visit(ICPPASTNamespaceDefinition namespace) {
		return nodeCommenter.appendComments((ASTNode) namespace);
	}

	@Override
	public int visit(ICPPASTTemplateParameter parameter) {
		return nodeCommenter.appendComments((ASTNode) parameter);
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		nodeCommenter.appendComments((ASTNode) tu);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTName name) {
		nodeCommenter.appendComments((ASTNode) name);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTDeclaration declaration) {
		nodeCommenter.appendComments((ASTNode) declaration);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
		return nodeCommenter.appendFreestandingComments((ASTNode) namespaceDefinition);
	}

	@Override
	public int leave(IASTInitializer initializer) {
		nodeCommenter.appendComments((ASTNode) initializer);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTParameterDeclaration parameterDeclaration) {
		nodeCommenter.appendComments((ASTNode) parameterDeclaration);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTDeclarator declarator) {
		nodeCommenter.appendComments((ASTNode) declarator);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		if(declSpec instanceof CPPASTCompositeTypeSpecifier) {
			return nodeCommenter.appendFreestandingComments((ASTNode) declSpec);
		}
		nodeCommenter.appendComments((ASTNode) declSpec);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTExpression expression) {
		nodeCommenter.appendComments((ASTNode) expression);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTStatement statement) {
		if(statement instanceof IASTCompoundStatement) {
			return nodeCommenter.appendFreestandingComments((ASTNode) statement);
		}
		nodeCommenter.appendComments((ASTNode) statement);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTTypeId typeId) {
		nodeCommenter.appendComments((ASTNode) typeId);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTEnumerator enumerator) {
		nodeCommenter.appendComments((ASTNode) enumerator);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTProblem problem){
		nodeCommenter.appendComments((ASTNode) problem);
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave( IASTComment comment){
		nodeCommenter.appendComments((ASTNode) comment);
		return PROCESS_CONTINUE;
	}	
}
