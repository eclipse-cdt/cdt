/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;


/**
 * Factory for creating AST nodes. This interface contains factory methods
 * for nodes that are available for both C and C++.
 * 
 * Extending interfaces should use covariant return types where appropriate to
 * allow the construction of language-specific versions of certain nodes. 
 * 
 * Most methods accept child nodes as parameters when constructing a new node.
 * For convenience it is always allowed to pass null for any of these parameters.
 * In this case the newly constructed node may be initialized using its 
 * set() and add() methods instead.
 * 
 * Nodes created by this factory are not frozen, i.e. for any node created by this
 * factory the following holds <code> node.isFrozen() == false </code>.
 * 
 * None of the factory methods should return null.
 * 
 * @author Mike Kucera
 * @since 5.1
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface INodeFactory {

	/**
	 * Creates a "dummy" name using an empty char array.
	 */
	public IASTName newName();
	
	public IASTName newName(char[] name);
	
	/**
	 * @deprecated use {@link #newTranslationUnit(IScanner)}, instead.
	 */
	@Deprecated
	public IASTTranslationUnit newTranslationUnit();

	/**
	 * Creates a new translation unit that cooperates with the given scanner in order
	 * to track macro-expansions and location information.
	 * @scanner the preprocessor the translation unit interacts with.
	 * @since 5.2
	 */
	public IASTTranslationUnit newTranslationUnit(IScanner scanner);

	public IASTLiteralExpression newLiteralExpression(int kind, String rep);
	
	public IASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand);
	
	public IASTIdExpression newIdExpression(IASTName name);
	
	public IASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript);

	public IASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTExpression argList);

	public IASTExpressionList newExpressionList();
	
	public IASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand);
	
	public IASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2);

	public IASTConditionalExpression newConditionalExpession(IASTExpression expr1, IASTExpression expr2, IASTExpression expr3);

	public IASTTypeIdInitializerExpression newTypeIdInitializerExpression(IASTTypeId typeId, IASTInitializer initializer);

	public IASTLabelStatement newLabelStatement(IASTName name, IASTStatement nestedStatement);

	public IASTCaseStatement newCaseStatement(IASTExpression expr);

	public IASTDefaultStatement newDefaultStatement();
	
	public IASTExpressionStatement newExpressionStatement(IASTExpression expression);

	public IASTNullStatement newNullStatement();
	
	public IASTCompoundStatement newCompoundStatement();
	
	public IASTSwitchStatement newSwitchStatement(IASTExpression controller, IASTStatement body);

	public IASTIfStatement newIfStatement(IASTExpression condition, IASTStatement then, IASTStatement elseClause);

	public IASTWhileStatement newWhileStatement(IASTExpression condition, IASTStatement body);

	public IASTDoStatement newDoStatement(IASTStatement body, IASTExpression condition);

	public IASTForStatement newForStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body);
	
	public IASTGotoStatement newGotoStatement(IASTName name);

	public IASTContinueStatement newContinueStatement();

	public IASTBreakStatement newBreakStatement();

	public IASTReturnStatement newReturnStatement(IASTExpression retValue);
	
	public IASTDeclarationStatement newDeclarationStatement(IASTDeclaration declaration);
	
	public IASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId);
	
	public IASTTypeId newTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator);

	public IASTDeclarator newDeclarator(IASTName name);
	
	public IASTSimpleDeclaration newSimpleDeclaration(IASTDeclSpecifier declSpecifier);
	
	public IASTInitializerExpression newInitializerExpression(IASTExpression expression);
	
	public IASTInitializerList newInitializerList();
	
	public IASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement);
	
	public IASTStandardFunctionDeclarator newFunctionDeclarator(IASTName name);
	
	public IASTASMDeclaration newASMDeclaration(String assembly);
	
	public IASTProblemDeclaration newProblemDeclaration(IASTProblem problem);

	public IASTProblemStatement newProblemStatement(IASTProblem problem);

	public IASTProblemExpression newProblemExpression(IASTProblem problem);

	public IASTProblem newProblem(int id, char[] arg, boolean error);

	public IASTEnumerationSpecifier newEnumerationSpecifier(IASTName name);
	
	public IASTEnumerator newEnumerator(IASTName name, IASTExpression value);
	
	public IASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name);
	
	public IASTArrayModifier newArrayModifier(IASTExpression expr);
	
	public IASTArrayDeclarator newArrayDeclarator(IASTName name);
	
	public IASTParameterDeclaration newParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator);
	
	public IASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize);

	public IASTSimpleDeclSpecifier newSimpleDeclSpecifier();
	
	public IGNUASTCompoundStatementExpression newGNUCompoundStatementExpression(IASTCompoundStatement compoundStatement);
	
	public IASTPointer newPointer();
	
	public IASTFieldReference newFieldReference(IASTName name, IASTExpression owner);
	
	public IASTNamedTypeSpecifier newTypedefNameSpecifier(IASTName name);
	
	public IASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name);

	/**
	 * Provides the offsets for a node. The offsets are artificial numbers that identify the
	 * position of a node in the translation unit. They are not file-offsets. You can obtain
	 * valid offsets via {@link IToken#getOffset()} or {@link IToken#getEndOffset()} from tokens
	 * provided by the scanner for this translation unit.
	 * <par> May throw an exception when the node provided was not created by this factory.
	 * @param node a node created by this factory
	 * @offset the offset (inclusive) for the node
	 * @param endOffset the end offset (exclusive) for the node
	 * @see #newTranslationUnit(IScanner)
	 * @since 5.2
	 */
	public void setOffsets(IASTNode node, int offset, int endOffset);

	/**
	 * Provides the end offset for a node. The offset is an artificial numbers that identifies the
	 * position of a node in the translation unit. It is not a file-offset. You can obtain a
	 * valid offset via {@link IToken#getEndOffset()} from a token provided by the scanner for 
	 * this translation unit.
	 * <par> May throw an exception when the node provided was not created by this factory.
	 * @param node a node created by this factory
	 * @param endOffset the end offset (exclusive) for the node
	 * @see #newTranslationUnit(IScanner)
	 * @since 5.2
	 */
	void setEndOffset(IASTNode node, int endOffset);   
	
	/**
	 * Adjusts the end-offset of a node to be the same as the end-offset of a given node.
	 * <par> May throw an exception when either one of the nodes provided was not created by this factory.
	 * @param node a node created by this factory
	 * @param endNode a node created by this factory defining the end for the other node.
	 * @since 5.2
	 */
	void setEndOffset(IASTNode node, IASTNode endNode);
}
