/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.upc;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.c99.IASTNodeFactory;
import org.eclipse.cdt.core.dom.c99.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.parser.c99.ASTStack;
import org.eclipse.cdt.core.dom.parser.c99.C99ParserAction;
import org.eclipse.cdt.core.dom.parser.c99.ITokenMap;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTDeclSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTForallStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSizeofExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym;


/**
 * Extension to the C99ParserAction that adds support fot building
 * an AST with UPC specific nodes.
 */
public class UPCParserAction extends C99ParserAction {
	
	private final UPCASTNodeFactory nodeFactory;
	
		
	public UPCParserAction(IParserActionTokenProvider parser) {
		super(parser);
		super.setTokenMap(UPCParsersym.orderedTerminalSymbols);
		this.nodeFactory = (UPCASTNodeFactory) super.getNodeFactory();
	}

	
	/**
	 * Adds support for UPC specific nodes.
	 * Some of the methods in UPCASTNodeFactory are overrides
	 * and are called up in C99ParserAction.
	 */
	protected IASTNodeFactory createNodeFactory() {
		return new UPCASTNodeFactory();
	}
	
	
	/**************************************************************************************
	 * Semantic actions
	 **************************************************************************************/
	
	
	/**
	 * constant ::= 'MYTHREAD' | 'THREADS' | 'UPC_MAX_BLOCKSIZE'
	 */
	public void consumeKeywordExpression(int keywordKind) {
		IUPCASTKeywordExpression expr = nodeFactory.newKeywordExpression();
		expr.setKeywordKind(keywordKind);
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * unary_expression ::= 'upc_localsizeof' unary_expression
     *                    | 'upc_blocksizeof' unary_expression
     *                    | 'upc_elemsizeof'  unary_expression
	 */
	public void consumeExpressionUpcSizeofOperator(int sizeofOp) {
		IUPCASTSizeofExpression expr = nodeFactory.newSizeofExpression();
		expr.setUPCSizeofOperator(sizeofOp);
		
		IASTExpression operand = (IASTExpression) astStack.pop();
		expr.setOperand(operand);
		operand.setParent(expr);
		operand.setPropertyInParent(IASTUnaryExpression.OPERAND);
		
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * unary_expression ::= 'upc_localsizeof' '(' type_name ')'
     *                    | 'upc_blocksizeof' '(' type_name ')'
     *                    | 'upc_elemsizeof'  '(' type_name ')'
	 */
	public void consumeExpressionUpcSizeofTypeName(int sizeofOp) {
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTTypeIdExpression expr = nodeFactory.newTypeIdExpression();
		
		expr.setTypeId(typeId);
		typeId.setParent(expr);
		typeId.setPropertyInParent(IASTTypeIdExpression.TYPE_ID);
		setOffsetAndLength(expr);
		
		astStack.push(expr);
		consumeExpressionUpcSizeofOperator(sizeofOp);
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
		IUPCASTSynchronizationStatement statement = nodeFactory.newSyncronizationStatment();
		statement.setStatementKind(statementKind);
		
		if(hasBarrierExpr) {
			IASTExpression barrierExpression = (IASTExpression) astStack.pop();
			statement.setBarrierExpression(barrierExpression);
			barrierExpression.setParent(statement);
			barrierExpression.setPropertyInParent(IUPCASTSynchronizationStatement.BARRIER_EXPRESSION);
		}
		
		setOffsetAndLength(statement);
		astStack.push(statement);
	}
	
	
	/**
	 * iteration_statement
     *     ::= 'upc_forall' '(' expression ';' expression ';' expression ';' affinity ')' statement
     *       | 'upc_forall' '(' declaration expression ';' expression ';' affinity ')' statement
	 */
	public void consumeStatementUPCForallLoop(boolean hasExpr1, boolean hasExpr2, boolean hasExpr3, boolean hasAffinity) {
		IUPCASTForallStatement forStat = nodeFactory.newForallStatement();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		forStat.setBody(body);
		body.setParent(forStat);
		body.setPropertyInParent(IUPCASTForallStatement.BODY);
		
		if(hasAffinity) {
			Object o = astStack.pop();
			if(o instanceof IASTExpression) {
				IASTExpression expr = (IASTExpression)o;
				forStat.setAffinityExpression(expr);
				expr.setParent(forStat);
				expr.setPropertyInParent(IUPCASTForallStatement.AFFINITY);
			}
			if(o instanceof IToken) {
				IToken token = (IToken) o;
				assert token.getKind() == UPCParsersym.TK_continue;
				forStat.setAffinityContinue(true);
			}
		}
		
		if(hasExpr3) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			forStat.setIterationExpression(expr);
			expr.setParent(forStat);
			expr.setPropertyInParent(IUPCASTForallStatement.ITERATION);
		}
		
		if(hasExpr2) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			forStat.setConditionExpression(expr);
			expr.setParent(forStat);
			expr.setPropertyInParent(IUPCASTForallStatement.CONDITION);
		}
		
		if(hasExpr1) { // may be an expression or a declaration
			IASTNode node = (IASTNode) astStack.pop();
			
			if(node instanceof IASTExpression) {
				IASTExpressionStatement stat = nodeFactory.newExpressionStatement();
				IASTExpression expr = (IASTExpression)node;
				stat.setExpression(expr);
				expr.setParent(stat);
				expr.setPropertyInParent(IASTExpressionStatement.EXPFRESSION);
				
				forStat.setInitializerStatement(stat);
				stat.setParent(forStat);
				stat.setPropertyInParent(IUPCASTForallStatement.INITIALIZER);
			}
			else if(node instanceof IASTDeclaration) {
				IASTDeclarationStatement stat = nodeFactory.newDeclarationStatement();
				IASTDeclaration declaration = (IASTDeclaration)node;
				stat.setDeclaration(declaration);
				declaration.setParent(stat);
				declaration.setPropertyInParent(IASTDeclarationStatement.DECLARATION);
				
				forStat.setInitializerStatement(stat);
				stat.setParent(forStat);
				stat.setPropertyInParent(IUPCASTForallStatement.INITIALIZER);
			}
		}
		else {
			forStat.setInitializerStatement(nodeFactory.newNullStatement());
		}
		
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
		if(hasExpression) {
			layoutQualifier.expression = (IASTExpression) astStack.pop();
		}
		astStack.push(layoutQualifier);
	}
	
	
	
	/**
	 * Overrides setSpecifier to add support for temporary layout qualifier nodes.
	 */
	protected void setSpecifier(ICASTDeclSpecifier declSpec, Object o) {
		if(o instanceof IToken)
			setTokenSpecifier((IUPCASTDeclSpecifier)declSpec, (IToken)o);
		else 
			setLayoutQualifier((IUPCASTDeclSpecifier)declSpec, (UPCParserActionLayoutQualifier) o);
	}
	
	
	/**
	 * Support for new declaration specifier keywords.
	 * 
	 * 'shared' without [] is handled here
	 * 'shared' with [] is handled in setLayoutQualifier().
	 */
	protected void setTokenSpecifier(IUPCASTDeclSpecifier node, IToken token) {
		switch(token.getKind()) {
			case UPCParsersym.TK_relaxed:
				node.setReferenceType(IUPCASTDeclSpecifier.rt_relaxed);
				break;
			case UPCParsersym.TK_strict:
				node.setReferenceType(IUPCASTDeclSpecifier.rt_strict);
				break;
			case UPCParsersym.TK_shared:
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
		if(layoutQualifier.hasStar) {
			node.setSharedQualifier(IUPCASTDeclSpecifier.sh_shared_pure_allocation);
		}
		else if(layoutQualifier.expression != null) {
			node.setSharedQualifier(IUPCASTDeclSpecifier.sh_shared_constant_expression);
			IASTExpression expr = layoutQualifier.expression;
			node.setBlockSizeExpression(expr);
			expr.setParent(node);
			expr.setPropertyInParent(IUPCASTDeclSpecifier.BLOCK_SIZE_EXPRESSION);
		}
		else {
			node.setSharedQualifier(IUPCASTDeclSpecifier.sh_shared_indefinite_allocation);
		}
	}
	
}


