/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.junit.jupiter.api.Test;

public class AST2UtilOldTests extends AST2TestBase {

	// Kind PRIMARY_EMPTY : void
	@Test
	public void testPrimaryEmpty() throws Exception {
		IASTTranslationUnit tu = parse("int x = f();".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f()"); //$NON-NLS-1$
	}

	// Kind PRIMARY_INTEGER_LITERAL : int
	@Test
	public void testPrimaryIntegerLiteral() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(1, 2+3);".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(1, 2 + 3)"); //$NON-NLS-1$
	}

	// Kind PRIMARY_CHAR_LITERAL : char
	@Test
	public void testPrimaryCharLiteral() throws Exception {
		IASTTranslationUnit tu = parse("int x = f('c');".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f('c')"); //$NON-NLS-1$
	}

	// Kind PRIMARY_FLOAT_LITERAL : float
	@Test
	public void testPrimaryFloatLiteral() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(1.13);".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(1.13)"); //$NON-NLS-1$
	}

	// Kind PRIMARY_STRING_LITERAL : char*
	@Test
	public void testPrimaryStringLiteral() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(\"str\");".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(\"str\")"); //$NON-NLS-1$
	}

	// Kind PRIMARY_BOOLEAN_LITERAL : bool
	@Test
	public void testPrimaryBooleanLiteral() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(true);".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(true)"); //$NON-NLS-1$
	}

	// Kind PRIMARY_THIS : type of inner most enclosing structure scope
	@Test
	public void testPrimaryThis() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(this);".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(this)"); //$NON-NLS-1$
	}

	// Kind PRIMARY_BRACKETED_EXPRESSION : LHS
	@Test
	public void testPrimaryBracketedExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(1, (2+3));".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(1, (2 + 3))"); //$NON-NLS-1$
	}

	// Kind ID_EXPRESSION : type of the ID
	@Test
	public void testIdExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(a);".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(a)"); //$NON-NLS-1$
	}

	// Kind POSTFIX_SUBSCRIPT
	@Test
	public void testPostfixSubscript() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(pa[1]);".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(pa[1])"); //$NON-NLS-1$
	}

	@Test
	public void testPostfixSubscriptA() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(pa[1][2]);".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(pa[1][2])"); //$NON-NLS-1$
	}

	// Kind POSTFIX_FUNCTIONCALL : return type of called function
	@Test
	public void testPostfixFunctioncallBug42822() throws Exception {
		IASTTranslationUnit tu = parse("int x = bar( foo( 3.0 ), foo( 5.0 ) ) ;".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"bar(foo(3.0), foo(5.0))"); //$NON-NLS-1$
	}

	// Kind POSTFIX_SIMPLETYPE_* : simple type
	@Test
	public void testPostfixSimpletypesBug42823() throws Exception {
		IASTTranslationUnit tu = parse(
				"int someInt = foo( int(3), short(4), double(3.0), float(4.0), char( 'a'), wchar_t( 'a' ), signed( 2 ), unsigned( 3 ), bool( false ), long( 3L ) );" //$NON-NLS-1$
						.toString(),
				ParserLanguage.CPP);
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(int(3), short(4), double(3.0), float(4.0), char('a'), wchar_t('a'), signed(2), unsigned(3), bool(false), long(3L))"); //$NON-NLS-1$
	}

	// Kind POSTFIX_DOT_IDEXPRESSION : type of member in the scope of the container
	@Test
	public void testPostfixDotExpression() throws Exception {
		IASTTranslationUnit tu = parse(
				"class A {int m;}; \n A  a; \n int foo(char); int foo( int ); \n int x = foo( a.m );".toString(), //$NON-NLS-1$
				ParserLanguage.CPP);
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[4]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a.m)"); //$NON-NLS-1$
	}

	// Kind POSTFIX_ARROW_IDEXPRESSION : type of member in the scope of the container
	@Test
	public void testPostfixArrowExpression() throws Exception {
		IASTTranslationUnit tu = parse(
				"class A {int m;}; \n A * a; \n int foo(char); int foo( int ); \n int x = foo( a->m );".toString(), //$NON-NLS-1$
				ParserLanguage.CPP);
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[4]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a->m)"); //$NON-NLS-1$
	}

	// Kind POSTFIX_INCREMENT : LHS
	@Test
	public void testPostfixIncrement() throws Exception {
		IASTTranslationUnit tu = parse("int y = foo( x++ );".toString(), ParserLanguage.CPP); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(x++)"); //$NON-NLS-1$
	}

	// Kind POSTFIX_DECREMENT : LHS
	@Test
	public void testPostfixDecrement() throws Exception {
		IASTTranslationUnit tu = parse("int y = foo( x-- );".toString(), ParserLanguage.CPP); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(x--)"); //$NON-NLS-1$
	}

	// Kind POSTFIX_DYNAMIC_CAST
	@Test
	public void testPostfixDynamicCast() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( dynamic_cast<B*>(a) );".toString(), ParserLanguage.CPP); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(dynamic_cast<B*>(a))"); //$NON-NLS-1$
	}

	// Kind POSTFIX_REINTERPRET_CAST
	@Test
	public void testPostfixReinterpretCast() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( reinterpret_cast<double *>(a) );".toString(), ParserLanguage.CPP); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(reinterpret_cast<double *>(a))"); //$NON-NLS-1$
	}

	// Kind POSTFIX_STATIC_CAST
	@Test
	public void testPostfixStaticCast() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( static_cast<char>(a) );".toString(), ParserLanguage.CPP); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(static_cast<char>(a))"); //$NON-NLS-1$
	}

	// Kind POSTFIX_CONST_CAST
	@Test
	public void testPostfixConstCast() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( const_cast<int *>(&a) );".toString(), ParserLanguage.CPP); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(const_cast<int *>(&a))"); //$NON-NLS-1$
	}

	// Kind POSTFIX_TYPEID_EXPRESSION : LHS
	@Test
	public void testPostfixTypeIdExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( typeid(5) );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(typeid(5))"); //$NON-NLS-1$
	}

	// Kind POSTFIX_TYPEID_EXPRESSION : type of the ID
	@Test
	public void testPostfixTypeIdExpression2() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( typeid(a) );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(typeid(a))"); //$NON-NLS-1$
	}
	// Kind POSTFIX_TYPEID_TYPEID : type of the ID

	@Test
	public void testPostfixTypeIdTypeId2() throws Exception {
		IASTTranslationUnit tu = parse("class A { }; int foo( int ); int x = foo( typeid(const A) );".toString(), //$NON-NLS-1$
				ParserLanguage.CPP);
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[2]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(typeid(const A))"); //$NON-NLS-1$
	}

	// Kind UNARY_INCREMENT : LHS
	@Test
	public void testUnaryIncrement() throws Exception {
		IASTTranslationUnit tu = parse("int y = foo( ++x );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(++x)"); //$NON-NLS-1$
	}

	// Kind UNARY_DECREMENT : LHS
	@Test
	public void testUnaryDecrement() throws Exception {
		IASTTranslationUnit tu = parse("int y = foo( --x );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(--x)"); //$NON-NLS-1$
	}

	// Kind UNARY_STAR_CASTEXPRESSION : LHS + t_pointer
	@Test
	public void testUnaryStarCastExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(*pa);".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(*pa)"); //$NON-NLS-1$
	}

	// Kind UNARY_AMPSND_CASTEXPRESSION : LHS + t_reference
	@Test
	public void testUnaryAmpersandCastExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = f(&pa);".toString(), ParserLanguage.CPP); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"f(&pa)"); //$NON-NLS-1$
	}

	// Kind UNARY_PLUS_CASTEXPRESSION  : LHS
	@Test
	public void testUnaryPlusCastExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( +5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(+5)"); //$NON-NLS-1$
	}

	// Kind UNARY_MINUS_CASTEXPRESSION : LHS
	@Test
	public void testUnaryMinusCastExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( -5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(-5)"); //$NON-NLS-1$
	}

	// Kind UNARY_NOT_CASTEXPRESSION : LHS
	@Test
	public void testUnaryNotCastExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( !b );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(!b)"); //$NON-NLS-1$
	}

	// Kind UNARY_TILDE_CASTEXPRESSION : LHS
	@Test
	public void testTildeNotCastExpression() throws Exception {
		IASTTranslationUnit tu = parse("int y = foo( ~x );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(~x)"); //$NON-NLS-1$
	}

	// Kind UNARY_SIZEOF_UNARYEXPRESSION : unsigned int
	@Test
	public void testUnarySizeofUnaryExpression() throws Exception {
		IASTTranslationUnit tu = parse("int y = foo( sizeof(5) );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(sizeof (5))"); //$NON-NLS-1$
	}

	// Kind UNARY_SIZEOF_TYPEID : unsigned int
	@Test
	public void testUnarySizeofTypeId() throws Exception {
		IASTTranslationUnit tu = parse("int x, y = foo( sizeof(x) );".toString(), ParserLanguage.CPP); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		final IASTInitializerClause expression = ((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0])
				.getDeclarators()[1].getInitializer()).getInitializerClause();
		isExpressionStringEqual(expression, "foo(sizeof (x))"); //$NON-NLS-1$
	}

	// Kind NEW_TYPEID
	@Test
	public void testNewTypeId() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( new A() );".toString(), ParserLanguage.CPP); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(new A())"); //$NON-NLS-1$
	}

	// Kind CASTEXPRESSION
	@Test
	public void testCastExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( (A*)b );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo((A*)b)"); //$NON-NLS-1$
	}

	// Kind MULTIPLICATIVE_MULTIPLY : usual arithmetic conversions
	@Test
	public void testMultiplicativeMultiply() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a * b );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a * b)"); //$NON-NLS-1$
	}

	// Kind MULTIPLICATIVE_DIVIDE : usual arithmetic conversions
	@Test
	public void testMultiplicativeDivide() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b / a );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b / a)"); //$NON-NLS-1$
	}

	// Kind MULTIPLICATIVE_MODULUS : usual arithmetic conversions
	@Test
	public void testMultiplicativeModulus() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b % a );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b % a)"); //$NON-NLS-1$
	}

	// Kind ADDITIVE_PLUS : usual arithmetic conversions
	@Test
	public void testAdditivePlus() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b + a );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b + a)"); //$NON-NLS-1$
	}

	// Kind ADDITIVE_MINUS : usual arithmetic conversions
	@Test
	public void testAdditiveMinus() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b - a );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b - a)"); //$NON-NLS-1$
	}

	// Kind SHIFT_LEFT : LHS
	@Test
	public void testShiftLeft() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a << 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a << 5)"); //$NON-NLS-1$
	}

	// Kind SHIFT_RIGHT : LHS
	@Test
	public void testShiftRight() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a >> 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a >> 5)"); //$NON-NLS-1$
	}

	// Kind RELATIONAL_LESSTHAN : bool
	@Test
	public void testRelationalLessThan() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b < 3 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b < 3)"); //$NON-NLS-1$
	}

	// Kind RELATIONAL_GREATERTHAN : bool
	@Test
	public void testRelationalGreaterThan() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b > 3 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b > 3)"); //$NON-NLS-1$
	}

	// Kind RELATIONAL_LESSTHANEQUALTO : bool
	@Test
	public void testRelationalLessThanOrEqual() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b <= 3 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b <= 3)"); //$NON-NLS-1$
	}

	// Kind RELATIONAL_GREATERTHANEQUALTO : bool
	@Test
	public void testRelationalGreaterThanOrEqual() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b >= 3 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b >= 3)"); //$NON-NLS-1$
	}

	// Kind EQUALITY_EQUALS : bool
	@Test
	public void testEqualityEquals() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b == 3 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b == 3)"); //$NON-NLS-1$
	}

	// Kind EQUALITY_NOTEQUALS : bool
	@Test
	public void testEqualityNotEquals() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( b != 3 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(b != 3)"); //$NON-NLS-1$
	}

	// Kind ANDEXPRESSION  : usual arithmetic conversions
	@Test
	public void testAndExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a & b );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a & b)"); //$NON-NLS-1$
	}

	// Kind EXCLUSIVEOREXPRESSION : usual arithmetic conversions
	@Test
	public void testExclusiveOrExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a ^ b );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a ^ b)"); //$NON-NLS-1$
	}

	// Kind INCLUSIVEOREXPRESSION : : usual arithmetic conversions
	@Test
	public void testInclusiveOrExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a | b );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a | b)"); //$NON-NLS-1$
	}

	// Kind LOGICALANDEXPRESSION : bool
	@Test
	public void testLogicalAndExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a && b );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a && b)"); //$NON-NLS-1$
	}

	// Kind LOGICALOREXPRESSION  : bool
	@Test
	public void testLogicalOrExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a || b );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a || b)"); //$NON-NLS-1$
	}

	// Kind CONDITIONALEXPRESSION : conditional Expression Conversions
	@Test
	public void testConditionalExpression() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a > 5 ? b : c );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a > 5 ? b : c)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_NORMAL : LHS
	@Test
	public void testAssignmentExpressionNormal() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a = 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a = 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_PLUS : LHS
	@Test
	public void testAssignmentExpressionPlus() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a += 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a += 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_MINUS : LHS
	@Test
	public void testAssignmentExpressionMinus() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a -= 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a -= 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_MULT : LHS
	@Test
	public void testAssignmentExpressionMulti() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a *= 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a *= 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_DIV : LHS
	@Test
	public void testAssignmentExpressionDiv() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a /= 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a /= 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_MOD : LHS
	@Test
	public void testAssignmentExpressionMod() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a %= 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a %= 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_LSHIFT : LHS
	@Test
	public void testAssignmentExpressionLShift() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a >>= 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a >>= 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_RSHIFT : LHS
	@Test
	public void testAssignmentExpressionRShift() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a <<= 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a <<= 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_AND : LHS
	@Test
	public void testAssignmentExpressionAnd() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a &= 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a &= 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_OR : LHS
	@Test
	public void testAssignmentExpressionOr() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a |= 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a |= 5)"); //$NON-NLS-1$
	}

	// Kind ASSIGNMENTEXPRESSION_XOR : LHS
	@Test
	public void testAssignmentExpressionXOr() throws Exception {
		IASTTranslationUnit tu = parse("int x = foo( a ^= 5 );".toString(), ParserLanguage.C); //$NON-NLS-1$
		IASTDeclaration[] d = tu.getDeclarations();
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"foo(a ^= 5)"); //$NON-NLS-1$
	}

}
