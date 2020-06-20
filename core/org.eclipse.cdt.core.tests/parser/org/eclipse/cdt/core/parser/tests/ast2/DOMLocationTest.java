/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.ParserException;

import junit.framework.TestSuite;

/**
 * @author jcamelon
 */
public class DOMLocationTests extends AST2TestBase {

	public DOMLocationTests() {
	}

	public DOMLocationTests(String name) {
		setName(name);
	}

	public static TestSuite suite() {
		return suite(DOMLocationTests.class);
	}

	public void testBaseCase() throws ParserException {
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse("int x;", p); //$NON-NLS-1$
			IASTDeclaration declaration = tu.getDeclarations()[0];
			IASTNodeLocation[] nodeLocations = declaration.getNodeLocations();
			assertNotNull(nodeLocations);
			assertEquals(nodeLocations.length, 1);
			assertTrue(nodeLocations[0] instanceof IASTFileLocation);
			IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
			assertEquals(fileLocation.getFileName(), TEST_CODE);
			assertEquals(fileLocation.getNodeOffset(), 0);
			assertEquals(fileLocation.getNodeLength(), 6);
			IASTNodeLocation[] tuLocations = tu.getNodeLocations();
			assertEquals(tuLocations.length, nodeLocations.length);
			assertEquals(fileLocation.getFileName(), ((IASTFileLocation) tuLocations[0]).getFileName());
			assertEquals(fileLocation.getNodeOffset(), tuLocations[0].getNodeOffset());
			assertEquals(fileLocation.getNodeLength(), tuLocations[0].getNodeLength());
		}
	}

	public void testSimpleDeclaration() throws ParserException {
		String code = "int xLen5, * yLength8, zLength16( int );"; //$NON-NLS-1$
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			IASTDeclaration[] declarations = tu.getDeclarations();
			assertEquals(declarations.length, 1);
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) declarations[0];
			IASTNodeLocation[] nodeLocations = declaration.getNodeLocations();
			assertNotNull(nodeLocations);
			assertEquals(nodeLocations.length, 1);
			assertTrue(nodeLocations[0] instanceof IASTFileLocation);
			IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
			assertEquals(fileLocation.getFileName(), TEST_CODE);
			assertEquals(fileLocation.getNodeOffset(), 0);
			assertEquals(fileLocation.getNodeLength(), code.indexOf(";") + 1); //$NON-NLS-1$
			IASTDeclarator[] declarators = declaration.getDeclarators();
			assertEquals(declarators.length, 3);
			for (int i = 0; i < 3; ++i) {
				IASTDeclarator declarator = declarators[i];
				switch (i) {
				case 0:
					assertSoleLocation(declarator, code.indexOf("xLen5"), "xLen5".length()); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case 1:
					assertSoleLocation(declarator, code.indexOf("* yLength8"), "* yLength8".length()); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case 2:
					assertSoleLocation(declarator, code.indexOf("zLength16( int )"), "zLength16( int )".length()); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				}
			}

		}
	}

	public void testSimpleObjectStyleMacroDefinition() throws Exception {
		String code = "/* hi */\n#define FOOT 0x01\n\n"; //$NON-NLS-1$
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			IASTDeclaration[] declarations = tu.getDeclarations();
			assertEquals(declarations.length, 0);
			IASTPreprocessorMacroDefinition[] macros = tu.getMacroDefinitions();
			assertNotNull(macros);
			assertEquals(macros.length, 1);
			assertSoleLocation(macros[0], code.indexOf("#"), code.indexOf("0x01") + 4 - code.indexOf("#")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			assertTrue(macros[0] instanceof IASTPreprocessorObjectStyleMacroDefinition);
			assertEquals(macros[0].getName().toString(), "FOOT"); //$NON-NLS-1$
			assertEquals(macros[0].getExpansion(), "0x01"); //$NON-NLS-1$
		}
	}

	public void testSimpleFunctionStyleMacroDefinition() throws Exception {
		String code = "#define FOOBAH( WOOBAH ) JOHN##WOOBAH\n\n"; //$NON-NLS-1$
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			IASTDeclaration[] declarations = tu.getDeclarations();
			assertEquals(declarations.length, 0);
			IASTPreprocessorMacroDefinition[] macros = tu.getMacroDefinitions();
			assertNotNull(macros);
			assertEquals(macros.length, 1);
			assertTrue(macros[0] instanceof IASTPreprocessorFunctionStyleMacroDefinition);
			assertSoleLocation(macros[0], code.indexOf("#define"), //$NON-NLS-1$
					code.indexOf("##WOOBAH") + 8 - code.indexOf("#define")); //$NON-NLS-1$ //$NON-NLS-2$s
			assertEquals(macros[0].getName().toString(), "FOOBAH"); //$NON-NLS-1$
			assertEquals(macros[0].getExpansion(), "JOHN##WOOBAH"); //$NON-NLS-1$
			IASTFunctionStyleMacroParameter[] parms = ((IASTPreprocessorFunctionStyleMacroDefinition) macros[0])
					.getParameters();
			assertNotNull(parms);
			assertEquals(parms.length, 1);
			assertEquals(parms[0].getParameter(), "WOOBAH"); //$NON-NLS-1$
		}

	}

	private void assertSoleLocation(IASTNode n, int offset, int length) {
		assertEquals(length, ((ASTNode) n).getLength());
		IASTNodeLocation[] locations = n.getNodeLocations();
		assertEquals(1, locations.length);
		IASTNodeLocation nodeLocation = locations[0];
		assertEquals(offset, nodeLocation.getNodeOffset());
		assertEquals(length, nodeLocation.getNodeLength());
	}

	private void assertFileLocation(IASTNode n, int offset, int length) {
		IASTNodeLocation location = n.getFileLocation();
		assertEquals(offset, location.getNodeOffset());
		assertEquals(length, location.getNodeLength());
	}

	public void testBug83664() throws Exception {
		String code = "int foo(x) int x; {\n 	return x;\n   }\n"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.C, true);
		IASTDeclaration[] declarations = tu.getDeclarations();
		assertEquals(declarations.length, 1);
		IASTFunctionDefinition definition = (IASTFunctionDefinition) declarations[0];
		IASTFunctionDeclarator declarator = definition.getDeclarator();
		assertSoleLocation(declarator, code.indexOf("foo"), code.indexOf("int x;") + 6 - code.indexOf("foo")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
		assertEquals(body.getStatements().length, 1);
		IASTReturnStatement returnStatement = (IASTReturnStatement) body.getStatements()[0];
		IASTIdExpression expression = (IASTIdExpression) returnStatement.getReturnValue();
		assertSoleLocation(expression, code.indexOf("return ") + "return ".length(), 1); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testBug84343() throws Exception {
		String code = "class A {}; int f() {\nA * b = 0;\nreturn b;}"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[1];
		IASTDeclarationStatement ds = (IASTDeclarationStatement) ((IASTCompoundStatement) f.getBody())
				.getStatements()[0];
		IASTSimpleDeclaration b = (IASTSimpleDeclaration) ds.getDeclaration();
		ICPPASTNamedTypeSpecifier namedTypeSpec = (ICPPASTNamedTypeSpecifier) b.getDeclSpecifier();
		assertSoleLocation(namedTypeSpec, code.indexOf("\nA") + 1, 1); //$NON-NLS-1$
	}

	public void testBug84366() throws Exception {
		String code = "enum hue { red, blue, green };"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		IASTSimpleDeclaration d = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier) d.getDeclSpecifier();
		IASTEnumerationSpecifier.IASTEnumerator enumerator = enumeration.getEnumerators()[0];
		assertSoleLocation(enumerator, code.indexOf("red"), "red".length()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testBug84375() throws Exception {
		String code = "class D { public: int x; };\nclass C : public virtual D {};"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		IASTSimpleDeclaration d2 = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		ICPPASTCompositeTypeSpecifier classSpec = (ICPPASTCompositeTypeSpecifier) d2.getDeclSpecifier();
		ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier[] bases = classSpec.getBaseSpecifiers();
		assertSoleLocation(bases[0], code.indexOf("public virtual D"), "public virtual D".length()); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public void testBug84357() throws Exception {
		String code = "class X {	int a;\n};\nint X::  * pmi = &X::a;"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		IASTSimpleDeclaration pmi = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		IASTDeclarator d = pmi.getDeclarators()[0];
		IASTPointerOperator p = d.getPointerOperators()[0];
		assertSoleLocation(p, code.indexOf("X::  *"), "X::  *".length()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testBug84367() throws Exception {
		String code = "void foo(   int   );"; //$NON-NLS-1$
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			IASTSimpleDeclaration definition = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTStandardFunctionDeclarator declarator = (IASTStandardFunctionDeclarator) definition.getDeclarators()[0];
			IASTParameterDeclaration parameter = declarator.getParameters()[0];
			assertSoleLocation(parameter, code.indexOf("int"), 3); //$NON-NLS-1$
		}
	}

	public void testElaboratedTypeSpecifier() throws ParserException {
		String code = "/* blah */ struct A anA; /* blah */"; //$NON-NLS-1$
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTElaboratedTypeSpecifier elabType = (IASTElaboratedTypeSpecifier) declaration.getDeclSpecifier();
			assertSoleLocation(elabType, code.indexOf("struct"), "struct A".length()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testBug83852() throws Exception {
		String code = "/* blah */ typedef short jc;  int x = 4;  jc myJc = (jc)x; "; //$NON-NLS-1$
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			IASTDeclaration[] declarations = tu.getDeclarations();
			assertEquals(3, declarations.length);
			for (int i = 0; i < 3; ++i) {
				IASTSimpleDeclaration decl = (IASTSimpleDeclaration) declarations[i];
				int start = 0, length = 0;
				switch (i) {
				case 0:
					start = code.indexOf("typedef"); //$NON-NLS-1$
					length = "typedef short jc;".length(); //$NON-NLS-1$
					break;
				case 1:
					start = code.indexOf("int x = 4;"); //$NON-NLS-1$
					length = "int x = 4;".length(); //$NON-NLS-1$
					break;
				case 2:
					start = code.indexOf("jc myJc = (jc)x;"); //$NON-NLS-1$
					length = "jc myJc = (jc)x;".length(); //$NON-NLS-1$
					break;
				}
				assertSoleLocation(decl, start, length);
			}
			IASTEqualsInitializer initializer = (IASTEqualsInitializer) ((IASTSimpleDeclaration) declarations[2])
					.getDeclarators()[0].getInitializer();
			IASTCastExpression castExpression = (IASTCastExpression) initializer.getInitializerClause();
			IASTTypeId typeId = castExpression.getTypeId();
			assertSoleLocation(typeId, code.indexOf("(jc)") + 1, "jc".length()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testBug83853() throws ParserException {
		String code = "int f() {return (1?0:1);	}"; //$NON-NLS-1$
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			IASTFunctionDefinition definition = (IASTFunctionDefinition) tu.getDeclarations()[0];
			IASTCompoundStatement statement = (IASTCompoundStatement) definition.getBody();
			IASTReturnStatement returnStatement = (IASTReturnStatement) statement.getStatements()[0];
			IASTUnaryExpression unaryExpression = (IASTUnaryExpression) returnStatement.getReturnValue();
			assertEquals(unaryExpression.getOperator(), IASTUnaryExpression.op_bracketedPrimary);
			IASTConditionalExpression conditional = (IASTConditionalExpression) unaryExpression.getOperand();
			assertSoleLocation(conditional, code.indexOf("1?0:1"), "1?0:1".length()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testBug84374() throws Exception {
		String code = "class P1 { public: int x; };\nclass P2 { public: int x; };\nclass B : public P1, public P2 {};\nvoid main() {\nB * b = new B();\n}"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		IASTFunctionDefinition main = (IASTFunctionDefinition) tu.getDeclarations()[3];
		IASTCompoundStatement statement = (IASTCompoundStatement) main.getBody();
		IASTDeclarationStatement decl = (IASTDeclarationStatement) statement.getStatements()[0];
		IASTSimpleDeclaration b = (IASTSimpleDeclaration) decl.getDeclaration();
		IASTEqualsInitializer initializerExpression = (IASTEqualsInitializer) b.getDeclarators()[0].getInitializer();
		assertSoleLocation(initializerExpression.getInitializerClause(), code.indexOf("new B()"), "new B()".length()); //$NON-NLS-1$ //$NON-NLS-2$
		ICPPASTNewExpression newExpression = (ICPPASTNewExpression) initializerExpression.getInitializerClause();
		assertSoleLocation(newExpression, code.indexOf("new B()"), "new B()".length()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testBug83737() throws Exception {
		String code = "void f() {  if( a == 0 ) g( a ); else if( a < 0 ) g( a >> 1 ); else if( a > 0 ) g( *(&a + 2) ); }"; //$NON-NLS-1$
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			IASTFunctionDefinition definition = (IASTFunctionDefinition) tu.getDeclarations()[0];
			IASTCompoundStatement statement = (IASTCompoundStatement) definition.getBody();
			IASTIfStatement first_if = (IASTIfStatement) statement.getStatements()[0];
			IASTIfStatement second_if = (IASTIfStatement) first_if.getElseClause();
			IASTIfStatement third_if = (IASTIfStatement) second_if.getElseClause();
			assertNull(third_if.getElseClause());
			int first_if_start = code.indexOf("if( a == 0 )"); //$NON-NLS-1$
			int total_if_length = "if( a == 0 ) g( a ); else if( a < 0 ) g( a >> 1 ); else if( a > 0 ) g( *(&a + 2) );" //$NON-NLS-1$
					.length();
			int total_if_end = first_if_start + total_if_length;
			int second_if_start = code.indexOf("if( a < 0 )"); //$NON-NLS-1$
			int third_if_start = code.indexOf("if( a > 0 )"); //$NON-NLS-1$
			assertSoleLocation(first_if, first_if_start, total_if_length);
			assertSoleLocation(second_if, second_if_start, total_if_end - second_if_start);
			assertSoleLocation(third_if, third_if_start, total_if_end - third_if_start);
		}
	}

	public void testBug84467() throws Exception {
		String code = "class D { };\n D d1;\n const D d2;\n void foo() {\n typeid(d1) == typeid(d2);\n }"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		IASTBinaryExpression bexp = (IASTBinaryExpression) ((IASTExpressionStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) tu
				.getDeclarations()[3]).getBody()).getStatements()[0]).getExpression();
		IASTUnaryExpression exp = (IASTUnaryExpression) ((IASTBinaryExpression) ((IASTExpressionStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) tu
				.getDeclarations()[3]).getBody()).getStatements()[0]).getExpression()).getOperand1();

		assertSoleLocation(bexp, code.indexOf("typeid(d1) == typeid(d2)"), "typeid(d1) == typeid(d2)".length()); //$NON-NLS-1$ //$NON-NLS-2$
		assertSoleLocation(exp, code.indexOf("typeid(d1)"), "typeid(d1)".length()); //$NON-NLS-1$ //$NON-NLS-2$
		exp = (IASTUnaryExpression) ((IASTBinaryExpression) ((IASTExpressionStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) tu
				.getDeclarations()[3]).getBody()).getStatements()[0]).getExpression()).getOperand2();
		assertSoleLocation(exp, code.indexOf("typeid(d2)"), "typeid(d2)".length()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testBug84576() throws Exception {
		String code = "namespace A {\n extern \"C\" int g();\n }"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTLinkageSpecification spec = (ICPPASTLinkageSpecification) ((ICPPASTNamespaceDefinition) tu
				.getDeclarations()[0]).getDeclarations()[0];
		assertSoleLocation(spec, code.indexOf("extern \"C\""), "extern \"C\" int g();".length()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSimplePreprocessorStatements() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#ifndef _APPLE_H_\n"); //$NON-NLS-1$
		buffer.append("#define _APPLE_H_\n"); //$NON-NLS-1$
		buffer.append("#undef _APPLE_H_\n"); //$NON-NLS-1$
		buffer.append("#endif\n"); //$NON-NLS-1$
		String code = buffer.toString();
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			assertEquals(tu.getDeclarations().length, 0);
			IASTPreprocessorStatement[] statements = tu.getAllPreprocessorStatements();
			assertEquals(statements.length, 4);
			IASTPreprocessorIfndefStatement ifndef = (IASTPreprocessorIfndefStatement) statements[0];
			assertTrue(ifndef.taken());
			assertSoleLocation(ifndef, code.indexOf("#ifndef _APPLE_H_"), "#ifndef _APPLE_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$
			IASTPreprocessorObjectStyleMacroDefinition definition = (IASTPreprocessorObjectStyleMacroDefinition) statements[1];
			assertSoleLocation(definition, code.indexOf("#define _APPLE_H_"), "#define _APPLE_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$
			IASTPreprocessorUndefStatement undef = (IASTPreprocessorUndefStatement) statements[2];
			assertSoleLocation(undef, code.indexOf("#undef _APPLE_H_"), "#undef _APPLE_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$
			IASTPreprocessorEndifStatement endif = (IASTPreprocessorEndifStatement) statements[3];
			assertSoleLocation(endif, code.indexOf("#endif"), "#endif".length()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testBug162180() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#include <notfound.h>\n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		String code = buffer.toString();
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p, false, false);
			IASTDeclaration[] decls = tu.getDeclarations();
			assertEquals(decls.length, 1);
			IASTPreprocessorStatement[] statements = tu.getAllPreprocessorStatements();
			assertEquals(statements.length, 1);
			IASTProblem[] problems = tu.getPreprocessorProblems();
			assertEquals(problems.length, 1);
			assertSoleLocation(decls[0], code, "int x;");
		}
	}

	private void assertSoleLocation(IASTNode node, String code, String snip) {
		assertSoleLocation(node, code.indexOf(snip), snip.length());
	}

	private void assertFileLocation(IASTNode node, String code, String snip) {
		assertFileLocation(node, code.indexOf(snip), snip.length());
	}

	public void testBug162180_0() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#include <notfound.h>\n"); //$NON-NLS-1$
		buffer.append("#include <notfound1.h> \r\n"); //$NON-NLS-1$
		buffer.append("#include <notfound2.h>  // more stuff \n"); //$NON-NLS-1$
		buffer.append("int x;\n"); //$NON-NLS-1$
		String code = buffer.toString();
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p, false, false);
			IASTDeclaration[] decls = tu.getDeclarations();
			IASTPreprocessorStatement[] statements = tu.getAllPreprocessorStatements();
			IASTProblem[] problems = tu.getPreprocessorProblems();
			assertEquals(1, decls.length);
			assertEquals(3, statements.length);
			assertEquals(3, problems.length);
			String snip = "<notfound.h>";
			assertSoleLocation(statements[0], code, "#include <notfound.h>");
			assertSoleLocation(statements[1], code, "#include <notfound1.h>");
			assertSoleLocation(statements[2], code, "#include <notfound2.h>");
			assertSoleLocation(decls[0], code, "int x;");
		}
	}

	public void test162180_1() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#define xxx(!) int a\n"); // [0-20]
		buffer.append("int x;\n"); // [21-27]
		buffer.append("int x\\i;\n"); // [28-36]
		buffer.append("int x2;\n"); // [37-44]
		String code = buffer.toString();
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p, false, false);
			IASTDeclaration[] decls = tu.getDeclarations();
			IASTPreprocessorStatement[] statements = tu.getAllPreprocessorStatements();
			IASTProblem[] problems = tu.getPreprocessorProblems();
			assertEquals(3, decls.length);
			assertEquals(0, statements.length);
			assertEquals(2, problems.length);
			assertSoleLocation(problems[0], code, "xxx(!");
			assertSoleLocation(decls[0], code, "int x;");
			assertSoleLocation(problems[1], code, "\\");
			assertFileLocation(decls[1], code, "int x\\i;");
			assertSoleLocation(decls[2], code, "int x2;");
		}
	}

	public void test162180_2() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#define ! x\n");
		buffer.append("int x;\n");
		String code = buffer.toString();
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p, false, false);
			IASTDeclaration[] decls = tu.getDeclarations();
			IASTPreprocessorStatement[] statements = tu.getAllPreprocessorStatements();
			IASTProblem[] problems = tu.getPreprocessorProblems();
			assertEquals(1, decls.length);
			assertEquals(0, statements.length);
			assertEquals(1, problems.length);
			assertSoleLocation(problems[0], code, "!");
			assertSoleLocation(decls[0], code, "int x;");
		}
	}

	public void test162180_3() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#define nix(x) x\n");
		buffer.append("nix(y,z);");
		buffer.append("int x;\n");
		String code = buffer.toString();
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p, false, false);
			IASTDeclaration[] decls = tu.getDeclarations();
			IASTPreprocessorStatement[] statements = tu.getAllPreprocessorStatements();
			IASTProblem[] problems = tu.getPreprocessorProblems();
			assertEquals(2, decls.length);
			assertEquals(1, statements.length);
			assertEquals(1, problems.length);
			assertSoleLocation(problems[0], code, "nix(y,");
			assertSoleLocation(decls[1], code, "int x;");
		}
	}

	public void test162180_4() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#include \"\"\n");
		buffer.append("#else\n");
		buffer.append("int x;\n");
		String code = buffer.toString();
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p, false, false);
			IASTDeclaration[] decls = tu.getDeclarations();
			IASTProblem[] problems = tu.getPreprocessorProblems();
			assertEquals(1, decls.length);
			assertEquals(2, problems.length);
			assertSoleLocation(problems[0], code, "#include \"\"");
			assertSoleLocation(problems[1], code, "#else");
			assertSoleLocation(decls[0], code, "int x;");
		}
	}

	public void testBug85820() throws Exception {
		String code = "int *p = (int []){2, 4};"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.C);
		IASTSimpleDeclaration sd = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTDeclarator d = sd.getDeclarators()[0];
		assertSoleLocation(d, code.indexOf("*p = (int []){2, 4}"), "*p = (int []){2, 4}".length()); //$NON-NLS-1$//$NON-NLS-2$
	}

	public void testBug86323() throws Exception {
		String code = "void f() { int i=0;	for (; i<10; i++) {	} }"; //$NON-NLS-1$
		for (ParserLanguage p : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(code, p);
			IASTForStatement for_stmt = (IASTForStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) tu
					.getDeclarations()[0]).getBody()).getStatements()[1];
			assertTrue(for_stmt.getInitializerStatement() instanceof IASTNullStatement);
		}
	}

	public void testBug86698_1() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("struct C;\n"); //$NON-NLS-1$
		buffer.append("void no_opt(C*);\n"); //$NON-NLS-1$
		buffer.append("struct C {\n"); //$NON-NLS-1$
		buffer.append("int c;\n"); //$NON-NLS-1$
		buffer.append("C() : c(0) { no_opt(this); }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		final ICPPASTCompositeTypeSpecifier ct = getCompositeType(tu, 2);
		final ICPPASTFunctionDefinition fdef = getDeclaration(ct, 1);
		ICPPASTFunctionDeclarator funC = (ICPPASTFunctionDeclarator) fdef.getDeclarator();
		assertSoleLocation(funC, buffer.toString().indexOf("C()"), "C()".length()); //$NON-NLS-1$//$NON-NLS-2$
		ICPPASTConstructorChainInitializer memInit = fdef.getMemberInitializers()[0];
		assertSoleLocation(memInit, buffer.toString().indexOf("c(0)"), "c(0)".length()); //$NON-NLS-1$//$NON-NLS-2$
	}

	public void testBug86698_2() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("class C {\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("double d;\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("C(int, double);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("C::C(int ii, double id)\n"); //$NON-NLS-1$
		buffer.append("try\n"); //$NON-NLS-1$
		buffer.append(": i(f(ii)), d(id)\n"); //$NON-NLS-1$
		buffer.append("{\n }\n"); //$NON-NLS-1$
		buffer.append("catch (...)\n"); //$NON-NLS-1$
		buffer.append("{\n }\n"); //$NON-NLS-1$

		final String code = buffer.toString();
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		final IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu.getDeclarations()[2];
		assertInstance(fdef, ICPPASTFunctionWithTryBlock.class);
		assertSoleLocation(fdef.getDeclarator(), code.indexOf("C::C(int ii, double id)"), //$NON-NLS-1$
				"C::C(int ii, double id)".length()); //$NON-NLS-1$
		ICPPASTFunctionWithTryBlock tryblock = ((ICPPASTFunctionWithTryBlock) fdef);
		assertSoleLocation(tryblock.getCatchHandlers()[0], code.indexOf("catch"), "catch (...)\n{\n }".length());
	}

	public void testBug157009_1() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#ifndef A\r\n#error X\r\n#else\r\n#error Y\r\n#endif");
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP, false, false);

		IASTProblem[] problems = tu.getPreprocessorProblems();
		assertEquals(1, problems.length);
		assertSoleLocation(problems[0], buffer.indexOf("X"), "X".length());
	}

	public void testBug157009_2() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#ifndef A\n#error X\n#else\n#error Y\n#endif");
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP, false, false);

		IASTProblem[] problems = tu.getPreprocessorProblems();
		assertEquals(1, problems.length);
		assertSoleLocation(problems[0], buffer.indexOf("X"), "X".length());
	}

	public void testBug171520() throws Exception {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171520
		StringBuilder buffer = new StringBuilder();
		buffer.append("int i = sizeof(int);");
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP, false, false);
		IASTDeclaration[] decls = tu.getDeclarations();
		assertEquals(1, decls.length);
		assertSoleLocation(decls[0], 0, buffer.length());
		assertTrue(decls[0] instanceof IASTSimpleDeclaration);
		IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) decls[0];
		IASTDeclarator[] declarators = simpleDecl.getDeclarators();
		assertEquals(1, declarators.length);
		IASTInitializer initializer = declarators[0].getInitializer();
		assertTrue(initializer instanceof IASTEqualsInitializer);
		IASTInitializerClause expr = ((IASTEqualsInitializer) initializer).getInitializerClause();
		assertTrue(expr instanceof IASTTypeIdExpression);
		assertSoleLocation(expr, buffer.indexOf("sizeof"), "sizeof(int)".length());
	}

	public void testBug120607() throws Exception {
		// C/C++ Indexer rejects valid pre-processor directive
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120607
		StringBuilder buffer = new StringBuilder();
		buffer.append("#import \"include_once.h\"\n");
		buffer.append("#warning \"deprecated include\"\n");
		buffer.append("#line 5\n");
		buffer.append("# 5 \"foo.h\"\n");
		buffer.append("#ident \"version 1.0\"\n");
		buffer.append("#assert thisIsTrue(value)\n");
		buffer.append("#unassert thisIsTrue(value)\n");
		buffer.append("#invalid");
		String code = buffer.toString();
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP, true, false);

		IASTProblem[] problems = tu.getPreprocessorProblems();
		assertEquals(3, problems.length);
		assertEquals(IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, problems[0].getID());
		assertEquals(IProblem.PREPROCESSOR_POUND_WARNING, problems[1].getID());
		assertEquals(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, problems[2].getID());
		assertSoleLocation(problems[0], code, "#import \"include_once.h\"");
		assertSoleLocation(problems[1], code, "\"deprecated include\"");
		assertSoleLocation(problems[2], code, "#invalid");
	}

	public void testBug527396_1() throws Exception {
		String code = "void foo() noexcept {}"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		ICPPASTDeclarator declarator = (ICPPASTDeclarator) definition.getDeclarator();
		String rawDeclarator = "foo() noexcept"; //$NON-NLS-1$
		assertSoleLocation(declarator, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testBug527396_2() throws Exception {
		String code = "void foo() noexcept(false) {}"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		ICPPASTDeclarator declarator = (ICPPASTDeclarator) definition.getDeclarator();
		String rawDeclarator = "foo() noexcept(false)"; //$NON-NLS-1$
		assertSoleLocation(declarator, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testBug527396_3() throws Exception {
		String code = "void foo() {}"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		ICPPASTDeclarator declarator = (ICPPASTDeclarator) definition.getDeclarator();
		String rawDeclarator = "foo()"; //$NON-NLS-1$
		assertSoleLocation(declarator, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testBug527396_4() throws Exception {
		String code = "void foo() noexcept;"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		IASTSimpleDeclaration definition = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTDeclarator declarator = (ICPPASTDeclarator) definition.getDeclarators()[0];
		String rawDeclarator = "foo() noexcept"; //$NON-NLS-1$
		assertSoleLocation(declarator, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testBug527396_5() throws Exception {
		String code = "void foo() noexcept(false);"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		IASTSimpleDeclaration definition = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTDeclarator declarator = (ICPPASTDeclarator) definition.getDeclarators()[0];
		String rawDeclarator = "foo() noexcept(false)"; //$NON-NLS-1$
		assertSoleLocation(declarator, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testBug527396_6() throws Exception {
		String code = "void foo();"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		IASTSimpleDeclaration definition = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTDeclarator declarator = (ICPPASTDeclarator) definition.getDeclarators()[0];
		String rawDeclarator = "foo()"; //$NON-NLS-1$
		assertSoleLocation(declarator, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testSwitchInitStatement_1() throws Exception {
		String code = "void foo() { switch (int i = 1; i) {} }"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
		IASTSwitchStatement statement = (IASTSwitchStatement) body.getStatements()[0];
		String rawDeclarator = "switch (int i = 1; i) {}"; //$NON-NLS-1$
		assertSoleLocation(statement, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testSwitchInitStatement_2() throws Exception {
		String code = "void foo() { char c = 'a'; switch (; c) {} }"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
		IASTSwitchStatement statement = (IASTSwitchStatement) body.getStatements()[1];
		String rawDeclarator = "switch (; c) {}"; //$NON-NLS-1$
		assertSoleLocation(statement, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testIfInitStatement_1() throws Exception {
		String code = "void foo() { if (int i = 1; i == 1) {} }"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
		IASTIfStatement statement = (IASTIfStatement) body.getStatements()[0];
		String rawDeclarator = "if (int i = 1; i == 1) {}"; //$NON-NLS-1$
		assertSoleLocation(statement, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testIfInitStatement_2() throws Exception {
		String code = "void foo() { if (; bool b = true) {} }"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
		IASTIfStatement statement = (IASTIfStatement) body.getStatements()[0];
		String rawDeclarator = "if (; bool b = true) {}"; //$NON-NLS-1$
		assertSoleLocation(statement, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testConstexprIf_1() throws Exception {
		String code = "void foo() { if constexpr (true) {} }"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
		IASTIfStatement statement = (IASTIfStatement) body.getStatements()[0];
		String rawDeclarator = "if constexpr (true) {}"; //$NON-NLS-1$
		assertSoleLocation(statement, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testConstexprIf_2() throws Exception {
		String code = "void foo() { if constexpr (constexpr int i = 1; i == 1) {} }"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
		IASTIfStatement statement = (IASTIfStatement) body.getStatements()[0];
		String rawDeclarator = "if constexpr (constexpr int i = 1; i == 1) {}"; //$NON-NLS-1$
		assertSoleLocation(statement, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	public void testConstexprIf_3() throws Exception {
		String code = "void foo() { if constexpr (; constexpr bool b = true) {} }"; //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
		IASTIfStatement statement = (IASTIfStatement) body.getStatements()[0];
		String rawDeclarator = "if constexpr (; constexpr bool b = true) {}"; //$NON-NLS-1$
		assertSoleLocation(statement, code.indexOf(rawDeclarator), rawDeclarator.length());
	}

	// int main(void){
	// 	#define one 1
	//	int integer = one;
	//	return integer;
	// }
	public void testRawSignature_Bug117029() throws Exception {
		String content = getContents(1)[0].toString();
		IASTTranslationUnit tu = parse(content, ParserLanguage.CPP);
		IASTFunctionDefinition decl = (IASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement compound = (IASTCompoundStatement) decl.getBody();
		assertEquals("int integer = one;", compound.getStatements()[0].getRawSignature());
		assertEquals("return integer;", compound.getStatements()[1].getRawSignature());
	}

	public void testTemplateIdNameLocation_Bug211444() throws Exception {
		IASTTranslationUnit tu = parse("Foo::template test<T> bar;", ParserLanguage.CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPASTQualifiedName qn = (ICPPASTQualifiedName) col.getName(0);
		IASTName lastName = qn.getLastName();
		assertTrue(lastName instanceof ICPPASTTemplateId);
		ICPPASTTemplateId templateId = (ICPPASTTemplateId) lastName;
		IASTName templateIdName = templateId.getTemplateName();

		assertEquals("test", templateIdName.getRawSignature()); //$NON-NLS-1$
	}

	// struct Base {
	//   virtual void func1();
	//   virtual void func2();
	//   virtual void func3();
	//   virtual void func4();
	// };
	// struct Sub : Base {
	//   void func1() final;
	//   void func2() override;
	//   void func3() final override;
	//   void func4() override final;
	// };
	public void testFunctionDeclaratorLocationContainsVirtualSpecifiers_Bug518628() throws Exception {
		String testCode = getAboveComment();
		BindingAssertionHelper assertionHelper = getAssertionHelper(ParserLanguage.CPP);
		String[] funcDeclaratorSignatures = new String[] { "func1() final", "func2() override",
				"func3() final override", "func4() override final" };
		Arrays.stream(funcDeclaratorSignatures).forEach(signature -> {
			IASTNode func1Declarator = assertionHelper.assertNode(signature, ICPPASTFunctionDeclarator.class);
			assertFileLocation(func1Declarator, testCode, signature);

		});
	}
}
