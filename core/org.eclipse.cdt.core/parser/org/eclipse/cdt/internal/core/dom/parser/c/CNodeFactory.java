/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Mike Kucera (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.internal.core.dom.parser.NodeFactory;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;

/**
 * Abstract factory implementation that creates AST nodes for C99.
 * These can be overridden in subclasses to change the 
 * implementations of the nodes.
 */
public class CNodeFactory extends NodeFactory implements ICNodeFactory {

	private static final CNodeFactory DEFAULT_INSTANCE = new CNodeFactory();
	
	public static CNodeFactory getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	public IASTArrayDeclarator newArrayDeclarator(IASTName name) {
		return new CASTArrayDeclarator(name);
	}
	
	public ICASTArrayDesignator newArrayDesignator(IASTExpression exp) {
		return new CASTArrayDesignator(exp);
	}
	
	public ICASTArrayModifier newArrayModifier(IASTExpression expr) {
		return new CASTArrayModifier(expr);
	}
	
	public IGCCASTArrayRangeDesignator newArrayRangeDesignatorGCC(IASTExpression floor, IASTExpression ceiling) {
		return new CASTArrayRangeDesignator(floor, ceiling);
	}
	
	public IASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript) {
		return new CASTArraySubscriptExpression(arrayExpr, subscript);
	}
	
	public IASTASMDeclaration newASMDeclaration(String assembly) {
		return new CASTASMDeclaration(assembly);
	}
	
	public IASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2) {
		return new CASTBinaryExpression(op, expr1, expr2);
	}
	
	public IASTBreakStatement newBreakStatement() {
		return new CASTBreakStatement();
	}
	
	public IASTCaseStatement newCaseStatement(IASTExpression expression) {
		return new CASTCaseStatement(expression);
	}
		
	/**
	 * @param operator  
	 */
	public IASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand) {
		return new CASTCastExpression(typeId, operand);
	}

	public ICASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name) {
		return new CASTCompositeTypeSpecifier(key, name);
	}
	
	public IASTCompoundStatement newCompoundStatement() {
		return new CASTCompoundStatement();
	}
	
	public IASTConditionalExpression newConditionalExpession(IASTExpression condition, IASTExpression positive, IASTExpression negative) {
		return new CASTConditionalExpression(condition, positive, negative);
	}
	
	public IASTContinueStatement newContinueStatement() {
		return new CASTContinueStatement();
	}
	
	public IASTDeclarationStatement newDeclarationStatement(IASTDeclaration declaration) {
		return new CASTDeclarationStatement(declaration);
	}
	
	public IASTDeclarator newDeclarator(IASTName name) {
		return new CASTDeclarator(name);
	}
	
	public IASTDefaultStatement newDefaultStatement() {
		return new CASTDefaultStatement();
	}
	
	@Deprecated
	public ICASTDesignatedInitializer newDesignatedInitializer(IASTInitializer operandInitializer) {
		CASTDesignatedInitializer result = new CASTDesignatedInitializer();
		result.setOperandInitializer(operandInitializer);
		return result;
	}
	
	public ICASTDesignatedInitializer newDesignatedInitializer(IASTInitializerClause clause) {
		return new CASTDesignatedInitializer(clause);
	}
	
	public IASTDoStatement newDoStatement(IASTStatement body, IASTExpression condition) {
		return new CASTDoStatement(body, condition);
	}
	
	public ICASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name) {
		return new CASTElaboratedTypeSpecifier(kind, name);
	}
	
	public ICASTEnumerationSpecifier newEnumerationSpecifier(IASTName name) {
		return new CASTEnumerationSpecifier(name);
	}
	
	public IASTEnumerator newEnumerator(IASTName name, IASTExpression value) {
		return new CASTEnumerator(name, value);
	}
	
	public IASTEqualsInitializer newEqualsInitializer(IASTInitializerClause initClause) {
		return new CASTEqualsInitializer(initClause);
	}
	
	public IASTExpressionList newExpressionList() {
		return new CASTExpressionList();
	}
	
	public IASTExpressionStatement newExpressionStatement(IASTExpression expr) {
		return new CASTExpressionStatement(expr);
	}

	public IASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize) {
		return new CASTFieldDeclarator(name, bitFieldSize);
	}

	public ICASTFieldDesignator newFieldDesignator(IASTName name) {
		return new CASTFieldDesignator(name);
	}

	public IASTFieldReference newFieldReference(IASTName name, IASTExpression owner) {
		return new CASTFieldReference(name, owner);
	}
	
	public IASTForStatement newForStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body) {
		return new CASTForStatement(init, condition, iterationExpression, body);
	}
	
	@Deprecated
	public IASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTExpression argList) {
		CASTFunctionCallExpression result = new CASTFunctionCallExpression(idExpr, null);
		result.setParameterExpression(argList);
		return result;
	}
	
	public IASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTInitializerClause[] arguments) {
		return new CASTFunctionCallExpression(idExpr, arguments);
	}
	
	public IASTStandardFunctionDeclarator newFunctionDeclarator(IASTName name) {
		return new CASTFunctionDeclarator(name);
	}
	
	public IASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement) {
		return new CASTFunctionDefinition(declSpecifier, declarator, bodyStatement);
	}
	
	public IGNUASTCompoundStatementExpression newGNUCompoundStatementExpression(IASTCompoundStatement compoundStatement) {
		return new CASTCompoundStatementExpression(compoundStatement);
	}
	
	public IASTGotoStatement newGotoStatement(IASTName name) {
		return new CASTGotoStatement(name);
	}
	
	public IASTIdExpression newIdExpression(IASTName name) {
		return new CASTIdExpression(name);
	}
	
	public IASTIfStatement newIfStatement(IASTExpression expr, IASTStatement thenStat, IASTStatement elseClause) {
		return new CASTIfStatement(expr, thenStat, elseClause);
	}
	
	@Deprecated
	public  org.eclipse.cdt.core.dom.ast.IASTInitializerExpression newInitializerExpression(IASTExpression expression) {
		return new CASTInitializerExpression(expression);
	}
	
	public IASTInitializerList newInitializerList() {
		return new CASTInitializerList();
	}
	
	public ICASTKnRFunctionDeclarator newKnRFunctionDeclarator(IASTName[] parameterNames, IASTDeclaration[] parameterDeclarations) {
		return new CASTKnRFunctionDeclarator(parameterNames, parameterDeclarations);
	}
	
	public IASTLabelStatement newLabelStatement(IASTName name, IASTStatement nestedStatement) {
		return new CASTLabelStatement(name, nestedStatement);
	}
	
	public IASTLiteralExpression newLiteralExpression(int kind, String rep) {
		return new CASTLiteralExpression(kind, rep.toCharArray());
	}
	
	public IASTName newName() {
		return new CASTName();
	}
	
	public IASTName newName(char[] name) {
		return new CASTName(name);
	}
	
	public IASTNullStatement newNullStatement() {
		return new CASTNullStatement();
	}
	
	public IASTParameterDeclaration newParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		return new CASTParameterDeclaration(declSpec, declarator);
	}
	
	public ICASTPointer newPointer() {
		return new CASTPointer();
	}
	
	public IASTProblem newProblem(int id, char[] arg, boolean error) {
		return new CASTProblem(id, arg, error);
	}
	
	public IASTProblemDeclaration newProblemDeclaration(IASTProblem problem) {
		return new CASTProblemDeclaration(problem);
	}
	
	public IASTProblemExpression newProblemExpression(IASTProblem problem) {
		return new CASTProblemExpression(problem);
	}
	
	public IASTProblemStatement newProblemStatement(IASTProblem problem) {
		return new CASTProblemStatement(problem);
	}
	
	public IASTReturnStatement newReturnStatement(IASTExpression retValue) {
		return new CASTReturnStatement(retValue);
	}
	
	public IASTSimpleDeclaration newSimpleDeclaration(IASTDeclSpecifier declSpecifier) {
		return new CASTSimpleDeclaration(declSpecifier);
	}
	
	public ICASTSimpleDeclSpecifier newSimpleDeclSpecifier() {
		return new CASTSimpleDeclSpecifier();
	}
	
	@Deprecated 
	public org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier newSimpleDeclSpecifierGCC(IASTExpression typeofExpression) {
		return new GCCASTSimpleDeclSpecifier(typeofExpression);
	}
	
	public IASTSwitchStatement newSwitchStatement(IASTExpression controller, IASTStatement body) {
		return new CASTSwitchStatement(controller, body);
	}
	
	public IASTTranslationUnit newTranslationUnit() {
		return newTranslationUnit(null);
	}

	public IASTTranslationUnit newTranslationUnit(IScanner scanner) {
		CASTTranslationUnit tu = new CASTTranslationUnit();
		
		if (scanner != null) {
			tu.setLocationResolver(scanner.getLocationResolver());
			if (scanner instanceof CPreprocessor) {
				tu.setIsForContentAssist(((CPreprocessor) scanner).isContentAssistMode());
			}
		}
		tu.setASTNodeFactory(this);
		return tu;
	}

	public ICASTTypedefNameSpecifier newTypedefNameSpecifier(IASTName name) {
		return new CASTTypedefNameSpecifier(name);
	}

	public IASTTypeId newTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
		return new CASTTypeId(declSpecifier, declarator);
	}

	public IASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId) {
		return new CASTTypeIdExpression(operator, typeId);
	}

	public ICASTTypeIdInitializerExpression newTypeIdInitializerExpression(IASTTypeId typeId, IASTInitializer initializer) {
		return new CASTTypeIdInitializerExpression(typeId, initializer);
	}

	public IASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand) {
		return new CASTUnaryExpression(operator, operand);
	}

	public IASTWhileStatement newWhileStatement(IASTExpression condition, IASTStatement body) {
		return new CASTWhileStatement(condition, body);
	}
} 

















