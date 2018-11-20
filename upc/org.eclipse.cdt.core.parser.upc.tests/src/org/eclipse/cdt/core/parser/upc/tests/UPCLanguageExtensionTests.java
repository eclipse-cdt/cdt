/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.upc.tests;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.upc.UPCLanguage;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTDeclSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTForallStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTTypeIdSizeofExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTUnarySizeofExpression;
import org.eclipse.cdt.core.lrparser.tests.ParseHelper;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class UPCLanguageExtensionTests extends TestCase {

	public UPCLanguageExtensionTests() {
	}

	public UPCLanguageExtensionTests(String name) {
		super(name);
	}

	protected BaseExtensibleLanguage getLanguage() {
		return UPCLanguage.getDefault();
	}

	// test problem-free parsing of UPC constructs (maily declarations)
	// test the AST is correct
	// test that binding resolution works

	private IASTTranslationUnit parseAndCheckBindings(String code) {
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckSyntaxProblems(true);
		options.setCheckPreprocessorProblems(true);
		options.setCheckBindings(true);
		return ParseHelper.parse(code, getLanguage(), options);
	}

	private IASTTranslationUnit parse(String code) {
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckSyntaxProblems(true);
		options.setCheckPreprocessorProblems(true);
		options.setCheckBindings(false);
		return ParseHelper.parse(code, getLanguage(), options);
	}

	public void testUPCSharedDeclarations1() throws Exception {
		String code = "shared int a [100+THREADS];\n" + "shared [] int b [THREADS];\n" + "shared [90] int c [10];\n"
				+ "shared [*] int d [];\n" + "relaxed int x;" + "strict int y;";

		IASTTranslationUnit tu = parseAndCheckBindings(code);
		IScope globalScope = tu.getScope();
		assertNotNull(globalScope);

		IASTDeclaration[] declarations = tu.getDeclarations();
		assertNotNull(declarations);
		assertEquals(6, declarations.length);

		// shared int a [100+THREADS];
		IASTSimpleDeclaration decl_a = (IASTSimpleDeclaration) declarations[0];
		IUPCASTSimpleDeclSpecifier declspec_a = (IUPCASTSimpleDeclSpecifier) decl_a.getDeclSpecifier();
		assertEquals(IUPCASTDeclSpecifier.rt_unspecified, declspec_a.getReferenceType());
		assertEquals(IUPCASTDeclSpecifier.sh_shared_default_block_size, declspec_a.getSharedQualifier());
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_a.getType());
		assertNull(declspec_a.getBlockSizeExpression());
		IASTDeclarator[] declarators = decl_a.getDeclarators();
		assertNotNull(declarators);
		assertEquals(1, declarators.length);
		IASTArrayDeclarator declarator_a = (IASTArrayDeclarator) declarators[0];
		IASTName name_a = declarator_a.getName();
		assertEquals("a", name_a.toString());
		IASTArrayModifier[] array_modifiers = declarator_a.getArrayModifiers();
		assertNotNull(array_modifiers);
		assertEquals(1, array_modifiers.length);
		IASTBinaryExpression expr = (IASTBinaryExpression) array_modifiers[0].getConstantExpression();
		IUPCASTKeywordExpression threads = (IUPCASTKeywordExpression) expr.getOperand2();
		assertEquals(IUPCASTKeywordExpression.kw_threads, threads.getKeywordKind());

		// shared [] int b [THREADS];
		IASTSimpleDeclaration decl_b = (IASTSimpleDeclaration) declarations[1];
		IUPCASTSimpleDeclSpecifier declspec_b = (IUPCASTSimpleDeclSpecifier) decl_b.getDeclSpecifier();
		assertEquals(IUPCASTDeclSpecifier.rt_unspecified, declspec_b.getReferenceType());
		assertEquals(IUPCASTDeclSpecifier.sh_shared_indefinite_allocation, declspec_b.getSharedQualifier());
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_b.getType());
		assertNull(declspec_b.getBlockSizeExpression());
		declarators = decl_b.getDeclarators();
		assertNotNull(declarators);
		assertEquals(1, declarators.length);
		IASTArrayDeclarator declarator_b = (IASTArrayDeclarator) declarators[0];
		IASTName name_b = declarator_b.getName();
		assertEquals("b", name_b.toString());
		array_modifiers = declarator_b.getArrayModifiers();
		assertNotNull(array_modifiers);
		assertEquals(1, array_modifiers.length);
		threads = (IUPCASTKeywordExpression) array_modifiers[0].getConstantExpression();
		assertEquals(IUPCASTKeywordExpression.kw_threads, threads.getKeywordKind());

		// shared [90] int c [10];
		IASTSimpleDeclaration decl_c = (IASTSimpleDeclaration) declarations[2];
		IUPCASTSimpleDeclSpecifier declspec_c = (IUPCASTSimpleDeclSpecifier) decl_c.getDeclSpecifier();
		assertEquals(IUPCASTDeclSpecifier.rt_unspecified, declspec_c.getReferenceType());
		assertEquals(IUPCASTDeclSpecifier.sh_shared_constant_expression, declspec_c.getSharedQualifier());
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_c.getType());
		@SuppressWarnings("unused")
		IASTLiteralExpression literalExpr = (IASTLiteralExpression) declspec_c.getBlockSizeExpression();
		declarators = decl_c.getDeclarators();
		assertNotNull(declarators);
		assertEquals(1, declarators.length);
		IASTArrayDeclarator declarator_c = (IASTArrayDeclarator) declarators[0];
		IASTName name_c = declarator_c.getName();
		assertEquals("c", name_c.toString());
		array_modifiers = declarator_c.getArrayModifiers();
		assertNotNull(array_modifiers);
		assertEquals(1, array_modifiers.length);
		assertNotNull(array_modifiers[0].getConstantExpression());

		// shared [*] int d [];
		IASTSimpleDeclaration decl_d = (IASTSimpleDeclaration) declarations[3];
		IUPCASTSimpleDeclSpecifier declspec_d = (IUPCASTSimpleDeclSpecifier) decl_d.getDeclSpecifier();
		assertEquals(IUPCASTDeclSpecifier.rt_unspecified, declspec_d.getReferenceType());
		assertEquals(IUPCASTDeclSpecifier.sh_shared_pure_allocation, declspec_d.getSharedQualifier());
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_d.getType());
		assertNull(declspec_d.getBlockSizeExpression());
		declarators = decl_d.getDeclarators();
		assertNotNull(declarators);
		assertEquals(1, declarators.length);
		IASTArrayDeclarator declarator_d = (IASTArrayDeclarator) declarators[0];
		IASTName name_d = declarator_d.getName();
		assertEquals("d", name_d.toString());
		array_modifiers = declarator_d.getArrayModifiers();
		assertNotNull(array_modifiers);
		assertEquals(1, array_modifiers.length);
		assertNull(array_modifiers[0].getConstantExpression());

		IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) declarations[4];
		IUPCASTSimpleDeclSpecifier declspec_x = (IUPCASTSimpleDeclSpecifier) decl_x.getDeclSpecifier();
		assertEquals(IUPCASTDeclSpecifier.rt_relaxed, declspec_x.getReferenceType());

		IASTSimpleDeclaration decl_y = (IASTSimpleDeclaration) declarations[5];
		IUPCASTSimpleDeclSpecifier declspec_y = (IUPCASTSimpleDeclSpecifier) decl_y.getDeclSpecifier();
		assertEquals(IUPCASTDeclSpecifier.rt_strict, declspec_y.getReferenceType());

		IVariable binding_a = (IVariable) name_a.resolveBinding();
		assertNotNull(binding_a);
		assertEquals(globalScope, binding_a.getScope());

		IVariable binding_b = (IVariable) name_a.resolveBinding();
		assertNotNull(binding_b);
		assertEquals(globalScope, binding_b.getScope());

		IVariable binding_c = (IVariable) name_a.resolveBinding();
		assertNotNull(binding_c);
		assertEquals(globalScope, binding_c.getScope());

		IVariable binding_d = (IVariable) name_a.resolveBinding();
		assertNotNull(binding_d);
		assertEquals(globalScope, binding_d.getScope());

	}

	public void testUPCSharedDeclarations2() throws Exception {
		String code = "int x = 10;" + "shared [x] int a [];\n";

		IASTTranslationUnit tu = parseAndCheckBindings(code);
		IScope globalScope = tu.getScope();
		assertNotNull(globalScope);

		IASTDeclaration[] declarations = tu.getDeclarations();
		assertNotNull(declarations);
		assertEquals(2, declarations.length);

		IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) declarations[0];

		// shared [x] int a [];
		IASTSimpleDeclaration decl_a = (IASTSimpleDeclaration) declarations[1];
		IUPCASTSimpleDeclSpecifier declspec_a = (IUPCASTSimpleDeclSpecifier) decl_a.getDeclSpecifier();
		assertEquals(IUPCASTDeclSpecifier.rt_unspecified, declspec_a.getReferenceType());
		assertEquals(IUPCASTDeclSpecifier.sh_shared_constant_expression, declspec_a.getSharedQualifier());
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_a.getType());
		IASTIdExpression expr_x = (IASTIdExpression) declspec_a.getBlockSizeExpression();

		IASTName name_x = expr_x.getName();
		IVariable binding_x = (IVariable) name_x.resolveBinding();
		assertNotNull(binding_x);
		assertEquals(decl_x.getDeclarators()[0].getName().resolveBinding(), binding_x);
	}

	public void testUPCForall1() throws Exception {
		String code = "int main() {\n" + "    int i;\n" + "    shared float *a;\n"
				+ "    upc_forall(i=0; i<N; i++; &a[i]) { \n" + "        a[i] = 99; \n" + "    } \n" + "}\n";

		IASTTranslationUnit tu = parse(code);

		IScope globalScope = tu.getScope();
		assertNotNull(globalScope);

		IASTDeclaration[] declarations = tu.getDeclarations();
		assertNotNull(declarations);
		assertEquals(1, declarations.length);

		IASTFunctionDefinition main = (IASTFunctionDefinition) declarations[0];
		IASTStatement[] body = ((IASTCompoundStatement) main.getBody()).getStatements();
		assertEquals(3, body.length);

		IASTSimpleDeclaration decl_i = (IASTSimpleDeclaration) ((IASTDeclarationStatement) body[0]).getDeclaration();
		IVariable binding_i = (IVariable) decl_i.getDeclarators()[0].getName().resolveBinding();

		IASTSimpleDeclaration decl_a = (IASTSimpleDeclaration) ((IASTDeclarationStatement) body[1]).getDeclaration();
		IVariable binding_a = (IVariable) decl_a.getDeclarators()[0].getName().resolveBinding();

		IUPCASTForallStatement forall = (IUPCASTForallStatement) body[2];

		IASTBinaryExpression expr1 = (IASTBinaryExpression) ((IASTExpressionStatement) forall.getInitializerStatement())
				.getExpression();
		IASTName name_i_1 = ((IASTIdExpression) expr1.getOperand1()).getName();

		IASTBinaryExpression expr2 = (IASTBinaryExpression) forall.getConditionExpression();
		IASTName name_i_2 = ((IASTIdExpression) expr2.getOperand1()).getName();

		IASTUnaryExpression expr3 = (IASTUnaryExpression) forall.getIterationExpression();
		IASTName name_i_3 = ((IASTIdExpression) expr3.getOperand()).getName();

		IASTUnaryExpression expr4 = (IASTUnaryExpression) forall.getAffinityExpresiion();
		IASTName name_i_4 = ((IASTIdExpression) ((IASTArraySubscriptExpression) expr4.getOperand())
				.getSubscriptExpression()).getName();
		IASTName name_a_1 = ((IASTIdExpression) ((IASTArraySubscriptExpression) expr4.getOperand())
				.getArrayExpression()).getName();

		IASTBinaryExpression expr5 = (IASTBinaryExpression) ((IASTExpressionStatement) ((IASTCompoundStatement) forall
				.getBody()).getStatements()[0]).getExpression();
		IASTName name_i_5 = ((IASTIdExpression) ((IASTArraySubscriptExpression) expr5.getOperand1())
				.getSubscriptExpression()).getName();
		IASTName name_a_2 = ((IASTIdExpression) ((IASTArraySubscriptExpression) expr5.getOperand1())
				.getArrayExpression()).getName();

		// test binding resolution
		IVariable binding_i_1 = (IVariable) name_i_1.resolveBinding();
		assertNotNull(binding_i_1);
		assertEquals(binding_i, binding_i_1);

		IVariable binding_i_2 = (IVariable) name_i_2.resolveBinding();
		assertNotNull(binding_i_2);
		assertEquals(binding_i, binding_i_2);

		IVariable binding_i_3 = (IVariable) name_i_3.resolveBinding();
		assertNotNull(binding_i_3);
		assertEquals(binding_i, binding_i_3);

		IVariable binding_i_4 = (IVariable) name_i_4.resolveBinding();
		assertNotNull(binding_i_4);
		assertEquals(binding_i, binding_i_4);

		IVariable binding_i_5 = (IVariable) name_i_5.resolveBinding();
		assertNotNull(binding_i_5);
		assertEquals(binding_i, binding_i_5);

		IVariable binding_a_1 = (IVariable) name_a_1.resolveBinding();
		assertNotNull(binding_a_1);
		assertEquals(binding_a, binding_a_1);

		IVariable binding_a_2 = (IVariable) name_a_2.resolveBinding();
		assertNotNull(binding_a_2);
		assertEquals(binding_a, binding_a_2);
	}

	/**
	 * Test a declaration inside a upc_forall definition.
	 */
	public void testUPCForall2() throws Exception {
		String code = "int main() {\n" + "    shared float *a;\n" + "    upc_forall(int i=0; i<N; i++; &a[i]) { \n"
				+ "        a[i] = 99; \n" + "    } \n" + "}\n";

		IASTTranslationUnit tu = parse(code);

		IScope globalScope = tu.getScope();
		assertNotNull(globalScope);

		IASTDeclaration[] declarations = tu.getDeclarations();
		assertNotNull(declarations);
		assertEquals(1, declarations.length);

		IASTFunctionDefinition main = (IASTFunctionDefinition) declarations[0];
		IASTStatement[] body = ((IASTCompoundStatement) main.getBody()).getStatements();
		assertEquals(2, body.length);

		IASTSimpleDeclaration decl_a = (IASTSimpleDeclaration) ((IASTDeclarationStatement) body[0]).getDeclaration();
		IVariable binding_a = (IVariable) decl_a.getDeclarators()[0].getName().resolveBinding();

		IUPCASTForallStatement forall = (IUPCASTForallStatement) body[1];

		IASTSimpleDeclaration decl_i = (IASTSimpleDeclaration) ((IASTDeclarationStatement) forall
				.getInitializerStatement()).getDeclaration();
		IVariable binding_i = (IVariable) decl_i.getDeclarators()[0].getName().resolveBinding();

		IASTBinaryExpression expr2 = (IASTBinaryExpression) forall.getConditionExpression();
		IASTName name_i_2 = ((IASTIdExpression) expr2.getOperand1()).getName();

		IASTUnaryExpression expr3 = (IASTUnaryExpression) forall.getIterationExpression();
		IASTName name_i_3 = ((IASTIdExpression) expr3.getOperand()).getName();

		IASTUnaryExpression expr4 = (IASTUnaryExpression) forall.getAffinityExpresiion();
		IASTName name_i_4 = ((IASTIdExpression) ((IASTArraySubscriptExpression) expr4.getOperand())
				.getSubscriptExpression()).getName();
		IASTName name_a_1 = ((IASTIdExpression) ((IASTArraySubscriptExpression) expr4.getOperand())
				.getArrayExpression()).getName();

		IASTBinaryExpression expr5 = (IASTBinaryExpression) ((IASTExpressionStatement) ((IASTCompoundStatement) forall
				.getBody()).getStatements()[0]).getExpression();
		IASTName name_i_5 = ((IASTIdExpression) ((IASTArraySubscriptExpression) expr5.getOperand1())
				.getSubscriptExpression()).getName();
		IASTName name_a_2 = ((IASTIdExpression) ((IASTArraySubscriptExpression) expr5.getOperand1())
				.getArrayExpression()).getName();

		// test binding resolution
		IVariable binding_i_2 = (IVariable) name_i_2.resolveBinding();
		assertNotNull(binding_i_2);
		assertEquals(binding_i, binding_i_2);

		IVariable binding_i_3 = (IVariable) name_i_3.resolveBinding();
		assertNotNull(binding_i_3);
		assertEquals(binding_i, binding_i_3);

		IVariable binding_i_4 = (IVariable) name_i_4.resolveBinding();
		assertNotNull(binding_i_4);
		assertEquals(binding_i, binding_i_4);

		IVariable binding_i_5 = (IVariable) name_i_5.resolveBinding();
		assertNotNull(binding_i_5);
		assertEquals(binding_i, binding_i_5);

		IVariable binding_a_1 = (IVariable) name_a_1.resolveBinding();
		assertNotNull(binding_a_1);
		assertEquals(binding_a, binding_a_1);

		IVariable binding_a_2 = (IVariable) name_a_2.resolveBinding();
		assertNotNull(binding_a_2);
		assertEquals(binding_a, binding_a_2);
	}

	/**
	 * Test 'continue' inside upc_forall
	 */
	public void testUPCForall3() throws Exception {
		String code = "int main() {\n" + "    upc_forall(int i=0; i<N; i++; continue) { \n" + "    } \n" + "}\n";

		IASTTranslationUnit tu = parse(code);

		IASTDeclaration[] declarations = tu.getDeclarations();
		assertNotNull(declarations);
		assertEquals(1, declarations.length);

		IASTFunctionDefinition main = (IASTFunctionDefinition) declarations[0];
		IASTStatement[] body = ((IASTCompoundStatement) main.getBody()).getStatements();
		assertEquals(1, body.length);

		IUPCASTForallStatement forall = (IUPCASTForallStatement) body[0];

		assertTrue(forall.isAffinityContinue());
		assertNull(forall.getAffinityExpresiion());
	}

	public void testUPCSynchronizationStatment() throws Exception {
		String code = "int main() {\n" + "    upc_notify 1;\n" + "    upc_notify;\n" + "    upc_wait 1;\n"
				+ "    upc_wait;\n" + "    upc_barrier 1;\n" + "    upc_barrier;\n" + "    upc_fence;\n" + "}\n";

		IASTTranslationUnit tu = parse(code);

		IASTDeclaration[] declarations = tu.getDeclarations();
		assertNotNull(declarations);
		assertEquals(1, declarations.length);

		IASTFunctionDefinition main = (IASTFunctionDefinition) declarations[0];
		IASTStatement[] body = ((IASTCompoundStatement) main.getBody()).getStatements();
		assertEquals(7, body.length);

		IUPCASTSynchronizationStatement stat;

		stat = (IUPCASTSynchronizationStatement) body[0];
		assertEquals(stat.getStatementKind(), IUPCASTSynchronizationStatement.st_upc_notify);
		assertNotNull(stat.getBarrierExpression());

		stat = (IUPCASTSynchronizationStatement) body[1];
		assertEquals(stat.getStatementKind(), IUPCASTSynchronizationStatement.st_upc_notify);
		assertNull(stat.getBarrierExpression());

		stat = (IUPCASTSynchronizationStatement) body[2];
		assertEquals(stat.getStatementKind(), IUPCASTSynchronizationStatement.st_upc_wait);
		assertNotNull(stat.getBarrierExpression());

		stat = (IUPCASTSynchronizationStatement) body[3];
		assertEquals(stat.getStatementKind(), IUPCASTSynchronizationStatement.st_upc_wait);
		assertNull(stat.getBarrierExpression());

		stat = (IUPCASTSynchronizationStatement) body[4];
		assertEquals(stat.getStatementKind(), IUPCASTSynchronizationStatement.st_upc_barrier);
		assertNotNull(stat.getBarrierExpression());

		stat = (IUPCASTSynchronizationStatement) body[5];
		assertEquals(stat.getStatementKind(), IUPCASTSynchronizationStatement.st_upc_barrier);
		assertNull(stat.getBarrierExpression());

		stat = (IUPCASTSynchronizationStatement) body[6];
		assertEquals(stat.getStatementKind(), IUPCASTSynchronizationStatement.st_upc_fence);
		assertNull(stat.getBarrierExpression());
	}

	public void testUPCSizeofExpressions() throws Exception {
		String code = "int main() {\n" + "    sizeof(int); \n" + "    sizeof x; \n" + "    upc_localsizeof(int); \n"
				+ "    upc_localsizeof x; \n" + "    upc_blocksizeof(int); \n" + "    upc_blocksizeof x; \n"
				+ "    upc_elemsizeof(int); \n" + "    upc_elemsizeof x; \n" + "}\n";

		IASTTranslationUnit tu = parse(code);

		IASTDeclaration[] declarations = tu.getDeclarations();
		assertNotNull(declarations);
		assertEquals(1, declarations.length);

		IASTFunctionDefinition main = (IASTFunctionDefinition) declarations[0];
		IASTStatement[] body = ((IASTCompoundStatement) main.getBody()).getStatements();
		assertEquals(8, body.length);

		@SuppressWarnings("unused")
		IASTTypeIdExpression cidexpr = (IASTTypeIdExpression) ((IASTExpressionStatement) body[0]).getExpression();

		IASTUnaryExpression cexpr = (IASTUnaryExpression) ((IASTExpressionStatement) body[1]).getExpression();
		assertEquals(IASTUnaryExpression.op_sizeof, cexpr.getOperator());

		IUPCASTUnarySizeofExpression expr;
		IUPCASTTypeIdSizeofExpression idexpr;

		idexpr = (IUPCASTTypeIdSizeofExpression) ((IASTExpressionStatement) body[2]).getExpression();
		assertEquals(IASTTypeIdExpression.op_sizeof, idexpr.getOperator());
		assertEquals(IUPCASTTypeIdSizeofExpression.upc_localsizeof, idexpr.getUPCSizeofOperator());

		expr = (IUPCASTUnarySizeofExpression) ((IASTExpressionStatement) body[3]).getExpression();
		assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
		assertEquals(IUPCASTUnarySizeofExpression.upc_localsizeof, expr.getUPCSizeofOperator());

		idexpr = (IUPCASTTypeIdSizeofExpression) ((IASTExpressionStatement) body[4]).getExpression();
		assertEquals(IASTTypeIdExpression.op_sizeof, idexpr.getOperator());
		assertEquals(IUPCASTTypeIdSizeofExpression.upc_blocksizeof, idexpr.getUPCSizeofOperator());

		expr = (IUPCASTUnarySizeofExpression) ((IASTExpressionStatement) body[5]).getExpression();
		assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
		assertEquals(IUPCASTUnarySizeofExpression.upc_blocksizeof, expr.getUPCSizeofOperator());

		idexpr = (IUPCASTTypeIdSizeofExpression) ((IASTExpressionStatement) body[6]).getExpression();
		assertEquals(IASTTypeIdExpression.op_sizeof, idexpr.getOperator());
		assertEquals(IUPCASTTypeIdSizeofExpression.upc_elemsizeof, idexpr.getUPCSizeofOperator());

		expr = (IUPCASTUnarySizeofExpression) ((IASTExpressionStatement) body[7]).getExpression();
		assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
		assertEquals(IUPCASTUnarySizeofExpression.upc_elemsizeof, expr.getUPCSizeofOperator());
	}

	public void testUPCSizeofExpressionsDisambiguate() throws Exception {
		String code = "int foo() {" + "	typedef int t;" + "   int x;" + "   upc_blocksizeof(t);"
				+ "   upc_blocksizeof(x);" + "}";

		IASTTranslationUnit tu = parse(code);

		IASTDeclaration[] declarations = tu.getDeclarations();
		IASTFunctionDefinition main = (IASTFunctionDefinition) declarations[0];
		IASTStatement[] body = ((IASTCompoundStatement) main.getBody()).getStatements();

		assertTrue(((IASTExpressionStatement) body[2]).getExpression() instanceof IUPCASTTypeIdSizeofExpression);
		assertTrue(((IASTExpressionStatement) body[3]).getExpression() instanceof IUPCASTUnarySizeofExpression);
	}
}
