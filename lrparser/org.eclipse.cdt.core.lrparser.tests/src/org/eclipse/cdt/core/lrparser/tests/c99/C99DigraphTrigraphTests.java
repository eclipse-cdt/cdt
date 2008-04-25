/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests.c99;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.lrparser.tests.ParseHelper;

@SuppressWarnings("nls")
public class C99DigraphTrigraphTests extends TestCase {

	
	public C99DigraphTrigraphTests() { }
	public C99DigraphTrigraphTests(String name) { super(name); }


	protected IASTTranslationUnit parse(String code) {	
		return ParseHelper.parse(code, getLanguage(), true);
	}
	
	
	protected BaseExtensibleLanguage getLanguage() {
		return C99Language.getDefault();
	}
	
	
	public void testTrigraphSequences() {
		StringBuffer sb = new StringBuffer();
		sb.append("??=define SIZE  ??/ \n"); // trigraph used as backslash to ignore newline
		sb.append("99 \n");
		sb.append("int main(void)??<  \n");
		sb.append("    int arr??(SIZE??);  \n");
		sb.append("    arr??(4??) = '0' - (??-0 ??' 1 ??! 2);  \n");
		sb.append("    printf(\"%c??/n\", arr??(4??)); \n");
		sb.append("??> \n");
		String code = sb.toString();
		
		IASTTranslationUnit tu = parse(code);
		assertNotNull(tu);
		
		IASTPreprocessorStatement[] defines = tu.getAllPreprocessorStatements();
		assertEquals(1, defines.length);
		IASTPreprocessorMacroDefinition macro = (IASTPreprocessorMacroDefinition)defines[0];
		assertEquals("SIZE", macro.getName().toString());
		//assertEquals("99", macro.getExpansion());
		
		IASTFunctionDefinition main = (IASTFunctionDefinition)tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) main.getBody();
		IASTStatement[] statements = body.getStatements();
		assertEquals(3, statements.length);
		
		// int arr??(SIZE??);
		IASTSimpleDeclaration arr = (IASTSimpleDeclaration)((IASTDeclarationStatement)statements[0]).getDeclaration();
		IASTArrayDeclarator arr_decl = (IASTArrayDeclarator)arr.getDeclarators()[0];
		IASTArrayModifier modifier = arr_decl.getArrayModifiers()[0];
		IASTLiteralExpression lit = (IASTLiteralExpression)modifier.getConstantExpression();
		assertEquals(IASTLiteralExpression.lk_integer_constant, lit.getKind());
		
		// arr??(4??) = '0' - (??-0 ??' 1 ??! 2);
		IASTBinaryExpression expr = (IASTBinaryExpression)((IASTExpressionStatement)statements[1]).getExpression();
		assertEquals(IASTBinaryExpression.op_assign, expr.getOperator());
		IASTArraySubscriptExpression arr_op = (IASTArraySubscriptExpression)expr.getOperand1();
		assertEquals("4", ((IASTLiteralExpression)arr_op.getSubscriptExpression()).toString());
		IASTBinaryExpression cond = (IASTBinaryExpression)((IASTUnaryExpression)((IASTBinaryExpression)expr.getOperand2()).getOperand2()).getOperand();
		assertEquals(IASTBinaryExpression.op_binaryOr, cond.getOperator());
		IASTBinaryExpression cond2 = (IASTBinaryExpression)cond.getOperand1();
		assertEquals(IASTBinaryExpression.op_binaryXor, cond2.getOperator());
		IASTUnaryExpression not = (IASTUnaryExpression)cond2.getOperand1();
		assertEquals(IASTUnaryExpression.op_tilde, not.getOperator());
		
		// printf(\"%c??/n\", arr??(4??));
		IASTFunctionCallExpression expr2 = (IASTFunctionCallExpression)((IASTExpressionStatement)statements[2]).getExpression();
		IASTExpressionList params = (IASTExpressionList) expr2.getParameterExpression();
		IASTArraySubscriptExpression arr_op2 = (IASTArraySubscriptExpression)params.getExpressions()[1];
		assertEquals("4", ((IASTLiteralExpression)arr_op2.getSubscriptExpression()).toString());
	}

	
	public void testTrigraphEscapeSequences() {
		// a ??/ trigraph should act just like a backslash in a string literal
		StringBuffer sb = new StringBuffer();
		sb.append("int main(void)??<  \n");
		sb.append("   char str[] = \"??/\"??/n\"; \n");
		sb.append("   char c = '??/u0000'; \n");
		sb.append("??> \n");
		String code = sb.toString();
		parse(code); // will throw an exception if there are parse errors
	}
	
	
	public void testDigraphSequences() {
		StringBuffer sb = new StringBuffer();
		sb.append("%:define join(a, b) a %:%: b \n");
		sb.append("int main() <% \n");
		sb.append("	   int arr<:5:>; \n");
		sb.append("%> \n");
		String code = sb.toString();
		
		IASTTranslationUnit tu = parse(code); // will throw an exception if there are parse errors
		
		IASTFunctionDefinition main = (IASTFunctionDefinition)tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) main.getBody();
		IASTStatement[] statements = body.getStatements();
		assertEquals(1, statements.length);
		
		IASTSimpleDeclaration arr = (IASTSimpleDeclaration)((IASTDeclarationStatement)statements[0]).getDeclaration();
		IASTArrayDeclarator arr_decl = (IASTArrayDeclarator)arr.getDeclarators()[0];
		IASTArrayModifier modifier = arr_decl.getArrayModifiers()[0];
		IASTLiteralExpression lit = (IASTLiteralExpression)modifier.getConstantExpression();
		assertEquals("5", lit.toString());
		
	}
	
	
	
	public void testTrigraphAndDigraphSequecesInPreprocessorDirectives() {
		StringBuffer sb = new StringBuffer();
		sb.append("%:define join1(a, b) a %:%: b \n");
		sb.append("%:define str1(a) %: a \n");
		sb.append("??=define join2(a, b) a ??=??= b \n");
		sb.append("??=define str2(a) ??= a \n");
		sb.append("int main() <% \n");
		sb.append("	   int join1(x, y) = str1(its all good); \n");
		sb.append("	   int join2(a, b) = str2(its still good); \n");
		sb.append("%> \n");
		String code = sb.toString();
		
		IASTTranslationUnit tu = parse(code); // will throw an exception if there are parse errors
		
		IASTFunctionDefinition main = (IASTFunctionDefinition)tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) main.getBody();
		IASTStatement[] statements = body.getStatements();
		assertEquals(2, statements.length);
		
		IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration)((IASTDeclarationStatement)statements[0]).getDeclaration();
		IASTDeclarator declarator1 = decl1.getDeclarators()[0];
		assertEquals("xy", declarator1.getName().toString());
		IASTLiteralExpression expr1 = (IASTLiteralExpression)((IASTInitializerExpression)declarator1.getInitializer()).getExpression();
		assertEquals(IASTLiteralExpression.lk_string_literal, expr1.getKind());
		assertEquals("\"its all good\"", expr1.toString());
		
		IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration)((IASTDeclarationStatement)statements[1]).getDeclaration();
		IASTDeclarator declarator2 = decl2.getDeclarators()[0];
		assertEquals("ab", declarator2.getName().toString());
		IASTLiteralExpression expr2 = (IASTLiteralExpression)((IASTInitializerExpression)declarator2.getInitializer()).getExpression();
		assertEquals(IASTLiteralExpression.lk_string_literal, expr2.getKind());
		assertEquals("\"its still good\"", expr2.toString());
	}
}
