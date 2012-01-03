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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTCopyLocation;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Visits all nodes, prints leading comments and handles macro expansions. The
 * source code generation is delegated to severals {@code NodeWriter}s.
 *
 * @see MacroExpansionHandler
 *
 * @author Emanuel Graf IFS
 */
public class ASTWriterVisitor extends ASTVisitor {
	protected final Scribe scribe = new Scribe();
	protected NodeCommentMap commentMap;
	protected ExpressionWriter expWriter;
	protected DeclSpecWriter declSpecWriter;
	protected StatementWriter statementWriter;
	protected DeclaratorWriter declaratorWriter;
	protected DeclarationWriter declarationWriter;
	protected InitializerWriter initializerWriter;
	protected NameWriter nameWriter;
	protected TemplateParameterWriter tempParameterWriter;
	protected MacroExpansionHandler macroHandler;
	private boolean insertLeadingBlankLine;
	private boolean suppressLeadingBlankLine;
	private boolean spaceNeededBeforeName;

	{
		shouldVisitArrayModifiers = true;
		shouldVisitBaseSpecifiers = true;
		shouldVisitDeclarations = true;
		shouldVisitDeclarators = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitExpressions = true;
		shouldVisitInitializers = true;
		shouldVisitNames = true;
		shouldVisitNamespaces = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitPointerOperators = true;
		shouldVisitStatements = true;
		shouldVisitTemplateParameters = true;
		shouldVisitTranslationUnit = true;
		shouldVisitTypeIds = true;
	}

	public ASTWriterVisitor(NodeCommentMap commentMap) {
		super();
		init(commentMap);
		this.commentMap = commentMap;
		this.suppressLeadingBlankLine = true;
	}

	private void init(NodeCommentMap commentMap) {
		macroHandler = new MacroExpansionHandler(scribe);
		statementWriter = new StatementWriter(scribe, this, commentMap);
		declaratorWriter = new DeclaratorWriter(scribe, this, commentMap);
		declarationWriter = new DeclarationWriter(scribe, this, commentMap);
		declSpecWriter = new DeclSpecWriter(scribe, this, commentMap);
		expWriter = new ExpressionWriter(scribe, this, macroHandler, commentMap);
		initializerWriter = new InitializerWriter (scribe, this, commentMap);
//		ppStmtWriter = new PreprocessorStatementWriter(scribe, this, commentMap);
		nameWriter = new NameWriter(scribe, this, commentMap);
		tempParameterWriter = new TemplateParameterWriter(scribe, this, commentMap);
	}

	@Override
	public String toString() {
		return scribe.toString();
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		for (IASTComment comment : commentMap.getFreestandingCommentsForNode(tu)) {
			scribe.print(comment.getComment());
			scribe.newLine();
		}
		return super.leave(tu);
	}

	private void writeLeadingComments(IASTNode node) {
		for (IASTComment comment : getLeadingComments(node)) {
			scribe.print(comment.getComment());
			scribe.newLine();
		}
	}

	private List<IASTComment> getLeadingComments(IASTNode node) {
		List<IASTComment> leadingComments = commentMap.getLeadingCommentsForNode(node);
		IASTNodeLocation[] locs = node.getNodeLocations();
		if (locs != null && locs.length > 0 && locs[0] instanceof IASTCopyLocation) {
			IASTCopyLocation copyLoc = (IASTCopyLocation) locs[0];
			leadingComments.addAll(commentMap.getLeadingCommentsForNode(copyLoc.getOriginalNode()));
		}
		return leadingComments;
	}

	public void visit(ASTLiteralNode lit) {
		insertBlankLineIfNeeded(lit);
		scribe.print(lit.getRawSignature());
	}

	@Override
	public int visit(IASTName name) {
		if (spaceNeededBeforeName && name.getSimpleID().length != 0) {
			scribe.printSpace();
			spaceNeededBeforeName = false;
		}
		writeLeadingComments(name);
		if (!macroHandler.checkisMacroExpansionNode(name)) {
			nameWriter.writeName(name);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		writeLeadingComments(declSpec);
		declSpecWriter.writeDelcSpec(declSpec);
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(IASTExpression expression) {
		writeLeadingComments(expression);
		if (!macroHandler.checkisMacroExpansionNode(expression)) {
			if (expression instanceof IGNUASTCompoundStatementExpression) {
				IGNUASTCompoundStatementExpression gnuCompStmtExp =
						(IGNUASTCompoundStatementExpression) expression;
				gnuCompStmtExp.getCompoundStatement().accept(this);
			} else {
				expWriter.writeExpression(expression);
			}
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(IASTStatement statement) {
		insertBlankLineIfNeeded(statement);
		writeLeadingComments(statement);
		try {
			if (macroHandler.isStatementWithMixedLocation(statement) &&
					!(statement instanceof IASTCompoundStatement)) {
				return statementWriter.writeMixedStatement(statement);
			}
			if (macroHandler.checkisMacroExpansionNode(statement)) {
				return ASTVisitor.PROCESS_SKIP;
			}
			return statementWriter.writeStatement(statement, true);
		} finally {
			setLeadingBlankLineFlags(statement);
		}
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		insertBlankLineIfNeeded(declaration);
		writeLeadingComments(declaration);
		if (!macroHandler.checkisMacroExpansionNode(declaration)) {
			declarationWriter.writeDeclaration(declaration);
			setLeadingBlankLineFlags(declaration);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		writeLeadingComments(declarator);
		if (!macroHandler.checkisMacroExpansionNode(declarator)) {
			declaratorWriter.writeDeclarator(declarator);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(IASTArrayModifier amod) {
		if (!macroHandler.checkisMacroExpansionNode(amod)) {
			declaratorWriter.writeArrayModifier(amod);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(IASTInitializer initializer) {
		writeLeadingComments(initializer);
		if (!macroHandler.checkisMacroExpansionNode(initializer)) {
			initializerWriter.writeInitializer(initializer);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		writeLeadingComments(parameterDeclaration);
		if (!macroHandler.checkisMacroExpansionNode(parameterDeclaration)) {
			parameterDeclaration.getDeclSpecifier().accept(this);
			IASTDeclarator declarator = getParameterDeclarator(parameterDeclaration);

			spaceNeededBeforeName = true;
			declarator.accept(this);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(IASTPointerOperator pointerOperator) {
		writeLeadingComments(pointerOperator);
		if (!macroHandler.checkisMacroExpansionNode(pointerOperator)) {
			declaratorWriter.writePointerOperator(pointerOperator);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	protected IASTName getParameterName(IASTDeclarator declarator) {
		return declarator.getName();
	}

	protected IASTDeclarator getParameterDeclarator(IASTParameterDeclaration parameterDeclaration) {
		return parameterDeclaration.getDeclarator();
	}

	@Override
	public int visit(ICPPASTNamespaceDefinition namespace) {
		insertBlankLineIfNeeded(namespace);
		writeLeadingComments(namespace);
		if (!macroHandler.checkisMacroExpansionNode(namespace)) {
			declarationWriter.writeDeclaration(namespace);
			setLeadingBlankLineFlags(namespace);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(ICPPASTTemplateParameter parameter) {
		writeLeadingComments(parameter);
		if (!macroHandler.checkisMacroExpansionNode(parameter)) {
			tempParameterWriter.writeTemplateParameter(parameter);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	public void cleanCache() {
		scribe.cleanCache();
		macroHandler.reset();
	}

	private void insertBlankLineIfNeeded(IASTNode node) {
		if (!suppressLeadingBlankLine &&
				(insertLeadingBlankLine || ASTWriter.requiresLeadingBlankLine(node))) {
			scribe.newLine();
		}
		insertLeadingBlankLine = false;
		suppressLeadingBlankLine = false;
	}

	private void setLeadingBlankLineFlags(IASTNode node) {
		insertLeadingBlankLine = ASTWriter.requiresTrailingBlankLine(node);
		suppressLeadingBlankLine = ASTWriter.suppressesTrailingBlankLine(node);
	}

	public boolean isSuppressLeadingBlankLine() {
		return suppressLeadingBlankLine;
	}

	public void setSuppressLeadingBlankLine(boolean value) {
		this.suppressLeadingBlankLine = value;
	}

	public boolean isSpaceNeededBeforeName() {
		return spaceNeededBeforeName;
	}

	public void setSpaceNeededBeforeName(boolean value) {
		this.spaceNeededBeforeName = value;
	}

	public Scribe getScribe() {
		return scribe;
	}

	public void newLine() {
		scribe.newLine();
	}
}
