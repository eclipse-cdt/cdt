/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mike Kucera (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thoams Corbat (IFS)
 *     Anders Dahlberg (Ericsson) - bug 84144
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.gnu.IGCCASTAttributeList;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.ms.IMSASTDeclspecList;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.IASTInactiveCompletionName;

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
 * @since 5.1
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface INodeFactory {
	/** @since 5.10 */
	public IASTAlignmentSpecifier newAlignmentSpecifier(IASTExpression expression);

	/** @since 5.10 */
	public IASTAlignmentSpecifier newAlignmentSpecifier(IASTTypeId typeId);

	public IASTArrayDeclarator newArrayDeclarator(IASTName name);

	public IASTArrayModifier newArrayModifier(IASTExpression expr);

	public IASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript);

	public IASTASMDeclaration newASMDeclaration(String assembly);

	/** @since 5.4 */
	public IASTAttribute newAttribute(char[] name, IASTToken argumentClause);

	public IASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2);

	public IASTBreakStatement newBreakStatement();

	public IASTCaseStatement newCaseStatement(IASTExpression expr);

	public IASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand);

	public IASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name);

	public IASTCompoundStatement newCompoundStatement();

	public IASTConditionalExpression newConditionalExpession(IASTExpression condition, IASTExpression positive,
			IASTExpression negative);

	public IASTContinueStatement newContinueStatement();

	public IASTDeclarationStatement newDeclarationStatement(IASTDeclaration declaration);

	public IASTDeclarator newDeclarator(IASTName name);

	public IASTDefaultStatement newDefaultStatement();

	public IASTDoStatement newDoStatement(IASTStatement body, IASTExpression condition);

	public IASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name);

	public IASTEnumerationSpecifier newEnumerationSpecifier(IASTName name);

	public IASTEnumerator newEnumerator(IASTName name, IASTExpression value);

	/**
	 * @since 5.2
	 */
	public IASTEqualsInitializer newEqualsInitializer(IASTInitializerClause initClause);

	public IASTExpressionList newExpressionList();

	public IASTExpressionStatement newExpressionStatement(IASTExpression expression);

	public IASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize);

	public IASTFieldReference newFieldReference(IASTName name, IASTExpression owner);

	public IASTForStatement newForStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body);

	/**
	 * @since 5.2
	 */
	public IASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr,
			IASTInitializerClause[] arguments);

	public IASTStandardFunctionDeclarator newFunctionDeclarator(IASTName name);

	public IASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement);

	/**
	 * @since 6.0
	 */
	public IGCCASTAttributeList newGCCAttributeList();

	/**
	 * @since 6.6
	 */
	public IMSASTDeclspecList newMSDeclspecList();

	public IGNUASTCompoundStatementExpression newGNUCompoundStatementExpression(
			IASTCompoundStatement compoundStatement);

	public IASTGotoStatement newGotoStatement(IASTName name);

	/**
	 * @since 5.8
	 */
	public IASTStatement newGotoStatement(IASTExpression expression);

	public IASTIdExpression newIdExpression(IASTName name);

	public IASTIfStatement newIfStatement(IASTExpression condition, IASTStatement then, IASTStatement elseClause);

	/**
	 * @since 6.3
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public IASTInactiveCompletionName newInactiveCompletionName(char[] name, IASTTranslationUnit ast);

	public IASTInitializerList newInitializerList();

	public IASTLabelStatement newLabelStatement(IASTName name, IASTStatement nestedStatement);

	public IASTLiteralExpression newLiteralExpression(int kind, String rep);

	/**
	 * Creates a "dummy" name using an empty char array.
	 */
	public IASTName newName();

	public IASTName newName(char[] name);

	/** @since 5.11 */
	public IASTName newName(String name);

	public IASTNullStatement newNullStatement();

	public IASTParameterDeclaration newParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator);

	public IASTPointer newPointer();

	public IASTProblem newProblem(int id, char[] arg, boolean error);

	public IASTProblemDeclaration newProblemDeclaration(IASTProblem problem);

	public IASTProblemExpression newProblemExpression(IASTProblem problem);

	public IASTProblemStatement newProblemStatement(IASTProblem problem);

	public IASTReturnStatement newReturnStatement(IASTExpression retValue);

	public IASTSimpleDeclaration newSimpleDeclaration(IASTDeclSpecifier declSpecifier);

	public IASTSimpleDeclSpecifier newSimpleDeclSpecifier();

	public IASTSwitchStatement newSwitchStatement(IASTExpression controller, IASTStatement body);

	/** @since 5.4 */
	public IASTToken newToken(int tokenType, char[] tokenImage);

	/** @since 5.4 */
	public IASTTokenList newTokenList();

	/**
	 * Creates a new translation unit that cooperates with the given scanner in order
	 * to track macro-expansions and location information.
	 * @param scanner the preprocessor the translation unit interacts with.
	 * @since 5.2
	 */
	public IASTTranslationUnit newTranslationUnit(IScanner scanner);

	public IASTNamedTypeSpecifier newTypedefNameSpecifier(IASTName name);

	public IASTTypeId newTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator);

	public IASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId);

	public IASTTypeIdInitializerExpression newTypeIdInitializerExpression(IASTTypeId typeId,
			IASTInitializer initializer);

	public IASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand);

	public IASTWhileStatement newWhileStatement(IASTExpression condition, IASTStatement body);

	/**
	 * Adjusts the end-offset of a node to be the same as the end-offset of a given node.
	 * <par> May throw an exception when either one of the nodes provided was not created by this factory.
	 * @param node a node created by this factory
	 * @param endNode a node created by this factory defining the end for the other node.
	 * @since 5.2
	 */
	void setEndOffset(IASTNode node, IASTNode endNode);

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
	 * Provides the offsets for a node. The offsets are artificial numbers that identify the
	 * position of a node in the translation unit. They are not file-offsets. You can obtain
	 * valid offsets via {@link IToken#getOffset()} or {@link IToken#getEndOffset()} from tokens
	 * provided by the scanner for this translation unit.
	 * <par> May throw an exception when the node provided was not created by this factory.
	 * @param node a node created by this factory
	 * @param offset the offset (inclusive) for the node
	 * @param endOffset the end offset (exclusive) for the node
	 * @see #newTranslationUnit(IScanner)
	 * @since 5.2
	 */
	public void setOffsets(IASTNode node, int offset, int endOffset);

	/**
	 * @deprecated Replaced by {@link #newFunctionCallExpression(IASTExpression, IASTInitializerClause[])}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTExpression argList);

	/**
	 * @deprecated Replaced by {@link #newEqualsInitializer(IASTInitializerClause)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IASTInitializerExpression newInitializerExpression(IASTExpression expression);

	/**
	 * @deprecated Use newGCCAttributeList() instead.
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.7
	 */
	@Deprecated
	public org.eclipse.cdt.core.dom.ast.gnu.IGCCASTAttributeSpecifier newGCCAttributeSpecifier();
}
