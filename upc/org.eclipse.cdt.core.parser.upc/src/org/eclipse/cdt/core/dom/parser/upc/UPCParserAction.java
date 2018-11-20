/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.upc;

import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_Completion;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_relaxed;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_shared;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.TK_strict;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99BuildASTParserAction;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTDeclSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTForallStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;

import lpg.lpgjavaruntime.IToken;

/**
 * Extension to the C99ParserAction that adds support fot building
 * an AST with UPC specific nodes.
 */
public class UPCParserAction extends C99BuildASTParserAction {

	private IUPCNodeFactory nodeFactory;

	/**
	 * TODO why is the nodeFactory passed as an argument
	 * @param nodeFactory
	 * @param parser
	 * @param tu
	 */
	public UPCParserAction(ITokenStream parser, ScopedStack<Object> astStack, IUPCNodeFactory nodeFactory,
			ISecondaryParserFactory parserFactory) {
		super(parser, astStack, nodeFactory, parserFactory);
		this.nodeFactory = nodeFactory;
		nodeFactory.setUseC99SizeofExpressions();
	}

	@Override
	protected boolean isCompletionToken(IToken token) {
		return token.getKind() == TK_Completion;
	}

	/**************************************************************************************
	 * Semantic actions
	 **************************************************************************************/

	public void consumeExpressionUnarySizeofOperator(int upcOp) {
		nodeFactory.setUseUPCSizeofExpressions(upcOp);
		super.consumeExpressionUnaryOperator(IASTUnaryExpression.op_sizeof);
		nodeFactory.setUseC99SizeofExpressions();
	}

	public void consumeExpressionSizeofTypeId(int upcOp) {
		nodeFactory.setUseUPCSizeofExpressions(upcOp);
		super.consumeExpressionTypeId(IASTTypeIdExpression.op_sizeof);
		nodeFactory.setUseC99SizeofExpressions();
	}

	/**
	 * constant ::= 'MYTHREAD' | 'THREADS' | 'UPC_MAX_BLOCKSIZE'
	 */
	public void consumeKeywordExpression(int keywordKind) {
		IUPCASTKeywordExpression expr = nodeFactory.newKeywordExpression(keywordKind);
		setOffsetAndLength(expr);
		astStack.push(expr);
	}

	/**
	 * synchronization_statement ::= 'upc_notify' expression ';'
	 *                             | 'upc_notify' ';'
	 *                             | 'upc_wait' expression ';'
	 *                             | 'upc_wait' ';'
	 *                             | 'upc_barrier' expression ';'
	 *                             | 'upc_barrier' ';'
	 *                             | 'upc_fence' ';'
	 */
	public void consumeStatementSynchronizationStatement(int statementKind, boolean hasBarrierExpr) {
		IASTExpression barrierExpression = hasBarrierExpr ? (IASTExpression) astStack.pop() : null;
		IUPCASTSynchronizationStatement statement = nodeFactory.newSyncronizationStatment(barrierExpression,
				statementKind);
		setOffsetAndLength(statement);
		astStack.push(statement);
	}

	/**
	 * iteration_statement
	 *     ::= 'upc_forall' '(' expression ';' expression ';' expression ';' affinity ')' statement
	 *       | 'upc_forall' '(' declaration expression ';' expression ';' affinity ')' statement
	 */
	public void consumeStatementUPCForallLoop(boolean hasExpr1, boolean hasExpr2, boolean hasExpr3,
			boolean hasAffinity) {
		IASTStatement body = (IASTStatement) astStack.pop();

		boolean affinityContinue = false;
		IASTExpression affinity = null;
		if (hasAffinity) {
			Object o = astStack.pop();
			if (o instanceof IASTExpression)
				affinity = (IASTExpression) o;
			else if (o instanceof IToken)
				affinityContinue = true;
		}

		IASTExpression expr3 = hasExpr3 ? (IASTExpression) astStack.pop() : null;
		IASTExpression expr2 = hasExpr2 ? (IASTExpression) astStack.pop() : null;

		IASTStatement initializer = nodeFactory.newNullStatement();
		if (hasExpr1) { // may be an expression or a declaration
			Object node = astStack.pop();
			if (node instanceof IASTExpression)
				initializer = nodeFactory.newExpressionStatement((IASTExpression) node);
			else if (node instanceof IASTDeclaration)
				initializer = nodeFactory.newDeclarationStatement((IASTDeclaration) node);
		}

		IUPCASTForallStatement forStat = nodeFactory.newForallStatement(initializer, expr2, expr3, body, affinity);
		forStat.setAffinityContinue(affinityContinue);
		setOffsetAndLength(forStat);
		astStack.push(forStat);
	}

	/**
	 * Temporary object used during the parsing of UPC declaration specifiers.
	 * Stored temporarily on the astStack, but does not become part of the AST.
	 * Makes parsing of layout qualifiers easier.
	 *
	 * @author Mike
	 */
	private static class UPCParserActionLayoutQualifier {
		public boolean hasStar = false;
		public IASTExpression expression = null;
	}

	/**
	 * layout_qualifier ::= '[' constant_expression ']'
	 *                    | '[' '*' ']'
	 *                    | '[' ']'
	 */
	public void consumeLayoutQualifier(boolean hasExpression, boolean hasStar) {
		UPCParserActionLayoutQualifier layoutQualifier = new UPCParserActionLayoutQualifier();
		layoutQualifier.hasStar = hasStar;
		if (hasExpression) {
			layoutQualifier.expression = (IASTExpression) astStack.pop();
		}
		astStack.push(layoutQualifier);
	}

	/**
	 * Overrides setSpecifier to add support for temporary layout qualifier nodes.
	 */
	@Override
	public void setSpecifier(ICASTDeclSpecifier declSpec, Object specifier) {
		if (specifier instanceof IToken)
			setTokenSpecifier((IUPCASTDeclSpecifier) declSpec, (IToken) specifier);
		else
			setLayoutQualifier((IUPCASTDeclSpecifier) declSpec, (UPCParserActionLayoutQualifier) specifier);
	}

	/**
	 * Support for new declaration specifier keywords.
	 *
	 * 'shared' without [] is handled here
	 * 'shared' with [] is handled in setLayoutQualifier().
	 */
	protected void setTokenSpecifier(IUPCASTDeclSpecifier node, IToken token) {
		switch (token.getKind()) {
		case TK_relaxed:
			node.setReferenceType(IUPCASTDeclSpecifier.rt_relaxed);
			break;
		case TK_strict:
			node.setReferenceType(IUPCASTDeclSpecifier.rt_strict);
			break;
		case TK_shared:
			node.setSharedQualifier(IUPCASTDeclSpecifier.sh_shared_default_block_size);
			break;
		default:
			super.setSpecifier(node, token);
		}
	}

	/**
	 * Handles layout qualifiers with block size specified.
	 */
	protected void setLayoutQualifier(IUPCASTDeclSpecifier node, UPCParserActionLayoutQualifier layoutQualifier) {
		if (layoutQualifier.hasStar) {
			node.setSharedQualifier(IUPCASTDeclSpecifier.sh_shared_pure_allocation);
		} else if (layoutQualifier.expression != null) {
			node.setSharedQualifier(IUPCASTDeclSpecifier.sh_shared_constant_expression);
			node.setBlockSizeExpression(layoutQualifier.expression);
		} else {
			node.setSharedQualifier(IUPCASTDeclSpecifier.sh_shared_indefinite_allocation);
		}
	}

}
