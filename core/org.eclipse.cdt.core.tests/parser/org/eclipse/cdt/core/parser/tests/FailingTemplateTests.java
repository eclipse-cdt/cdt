/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;

/**
 * @author aniefer
 */
public class FailingTemplateTests extends TestCase {

	public ParserSymbolTable table = null;
	
	public FailingTemplateTests( String arg )
	{
		super( arg );
	}
	
	public ParserSymbolTable newTable(){
		return newTable( ParserLanguage.CPP );
	}
	
	public ParserSymbolTable newTable( ParserLanguage language ){
		table = new ParserSymbolTable( language, ParserMode.COMPLETE_PARSE );
		return table;
	}
	
	/**
	 *	 These tests represent code snippets from the ANSI C++ spec
	 */
	
	
	/**
	 * A specialization of a member function template does not override a virtual
	 * function from a base class.
	 * 
	 * class B {
	 *    virtual void f( int );
	 * };
	 * 
	 * class D : public B{
	 *    template < class T > void f( T );
	 * };
	 * 
	 * template <> void D::f< int > ( int ) {}  //does not override B::f( int );
	 * 
	 * void main(){
	 *    D d;
	 *    d.f( 1 );  //calls B::f( int )
	 *    d.f<>( 1 ); //calls D::f<int>( int );
	 * }
	 * @throws Exception
	 */
	public void test_14_5_2__4_VirtualBaseClassFunctions() throws Exception{
		//bug 51483  TBD
	}
	
	/**
	 * template < class T = int > struct A {
	 *    static int x;
	 * };
	 * 
	 * template <> struct A< double > {}; //specialize T == double
	 * template <> struct A<> {};         //specialize T == int
	 * 
	 * template <> int A< char >::x = 0;
	 * template <> int A< float >::x = 0.5;
	 * 
	 * @throws Exception
	 */
	public void test_14_7__3_ExplicitSpecialization() throws Exception{
		//bug 51485
	}
	
	/**
	 * Each class template specialization instantiated from a template has its own
	 * copy  of any static members
	 * 
	 * template < class T > class X {
	 *    static T s;
	 * }
	 * 
	 * template < class T > T X<T>::s = 0;
	 * 
	 * X<int> a;		//a.s has type int
	 * X<char *> b;		//b.s has type char *
	 * @throws Exception
	 */
	public void test_14_7__6_ExplicitSpecializationStaticMembers() throws Exception{
		//bug 51485
	}
	
	/**
	 * template<class T> void f( void (*) (T, int) );
	 * template<class T> void foo( T, int );
	 * 
	 * void g( int, int );
	 * void g( char, int );
	 * 
	 * void h( int, int, int );
	 * void h( char, int );
	 * 
	 * int m(){
	 *    f( &g );	 //error, ambiguous
	 *    f( &h );   //ok, h(char, int) is a unique match
	 *    f( &foo ); //error, foo is a template
	 * }
	 * 
	 * @throws Exception
	 */
	public void test_14_8_2_4__16_ArgumentDeduction() throws Exception{
		//This test will require resolving the address of an overloaded function
		//without arguments.  bug 45764
		
//		newTable();
//		
//		ITemplateSymbol templateF = table.newTemplateSymbol( "f" );
//		
//		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
//		templateF.addParameter( T );
//		
//		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
//		
//		IParameterizedSymbol fParam = table.newParameterizedSymbol( "", TypeInfo.t_function );
//		fParam.setIsTemplateMember( true );
//		fParam.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
//		fParam.addParameter( T, null, false );
//		fParam.addParameter( TypeInfo.t_int, 0, null, false );
//		fParam.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
//		
//		f.addParameter( fParam );
//		
//		templateF.addSymbol( f );
//		table.getCompilationUnit().addSymbol( templateF );
//		
//		ITemplateSymbol templateFoo = table.newTemplateSymbol( "foo" );
//		T = table.newSymbol( "T", TypeInfo.t_templateParameter );
//		templateFoo.addParameter( T );
//		
//		IParameterizedSymbol foo = table.newParameterizedSymbol( "foo", TypeInfo.t_function );
//		foo.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
//		foo.addParameter( T, null, false );
//		foo.addParameter( TypeInfo.t_int, 0, null, false );
//		
//		templateFoo.addSymbol( foo );
//		table.getCompilationUnit().addSymbol( templateFoo );
//		
//		IParameterizedSymbol g1 = table.newParameterizedSymbol( "g", TypeInfo.t_function );
//		g1.addParameter( TypeInfo.t_int, 0, null, false );
//		g1.addParameter( TypeInfo.t_int, 0, null, false );
//		table.getCompilationUnit().addSymbol( g1 );
//		
//		IParameterizedSymbol g2 = table.newParameterizedSymbol( "g", TypeInfo.t_function );
//		g2.addParameter( TypeInfo.t_char, 0, null, false );
//		g2.addParameter( TypeInfo.t_int, 0, null, false );
//		table.getCompilationUnit().addSymbol( g2 );
//		
//		IParameterizedSymbol h1 = table.newParameterizedSymbol( "h", TypeInfo.t_function );
//		h1.addParameter( TypeInfo.t_int, 0, null, false );
//		h1.addParameter( TypeInfo.t_int, 0, null, false );
//		h1.addParameter( TypeInfo.t_int, 0, null, false );
//		table.getCompilationUnit().addSymbol( h1 );
//		
//		IParameterizedSymbol h2 = table.newParameterizedSymbol( "h", TypeInfo.t_function );
//		h2.addParameter( TypeInfo.t_char, 0, null, false );
//		h2.addParameter( TypeInfo.t_int, 0, null, false );
//		table.getCompilationUnit().addSymbol( h2 );
//		
//
//		
//		List args = new LinkedList();
//
//		
//		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", args );
//		assertTrue( look != null );
//		assertTrue( look.isTemplateInstance() );
//		assertEquals( look.getInstantiatedSymbol(), f );
//		
//		look = table.getCompilationUnit().lookup( "foo" );
//		assertTrue( look != null );
//		args.clear();
//		TypeInfo arg = new TypeInfo( TypeInfo.t_type, 0, look );
//		arg.addOperatorExpression( TypeInfo.OperatorExpression.addressof );
//		args.add( arg );
//		
//		try{
//			look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", args );
//			assertTrue( false );	
//		}catch ( ParserSymbolTableException e ){
//			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateParameter );
//		}
		
	}
}
