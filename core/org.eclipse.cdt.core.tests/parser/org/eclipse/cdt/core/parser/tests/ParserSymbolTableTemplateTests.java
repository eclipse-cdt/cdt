/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IDeferredTemplateInstance;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISpecializedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp;

/**
 * @author aniefer
 */

public class ParserSymbolTableTemplateTests extends TestCase {

	public ParserSymbolTable table = null;
	
	public ParserSymbolTableTemplateTests( String arg )
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
	 * 
	 * @throws Exception
	 *
	 * template < class T > class A : public T {};
	 *
	 * class B 
	 * {
	 *    int i;
	 * }
	 *
	 * A<B> a;
	 * a.i;  //finds B::i;
	 */
	public void testTemplateParameterAsParent() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" );
		ISymbol param = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( param );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		factory.addSymbol( A );
		A.addParent( param );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		ISymbol i = table.newSymbol( "i", TypeInfo.t_int );
		B.addSymbol( i );
		table.getCompilationUnit().addSymbol( B );
		
		TypeInfo type = new TypeInfo( TypeInfo.t_type, 0, B );
		ArrayList args = new ArrayList();
		args.add( type );
		
		IContainerSymbol instance = (IContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args );
		assertEquals( instance.getInstantiatedSymbol(), A );
		
		ISymbol a = table.newSymbol( "a", TypeInfo.t_type );
		a.setTypeSymbol( instance );
		
		table.getCompilationUnit().addSymbol( a );
		
		ISymbol look = table.getCompilationUnit().lookup( "a" );
		
		assertEquals( look, a );
		
		ISymbol symbol = a.getTypeSymbol();
		assertEquals( symbol, instance );

		look = instance.lookup( "i" );
		assertEquals( look, i );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * template < class T > class A { T t; }
	 * class B : public A< int > { }
	 * 
	 * B b;
	 * b.t;	//finds A::t, will be type int
	 */
	public void testTemplateInstanceAsParent() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" );
			
		ISymbol param = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( param );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		factory.addSymbol( A );
		
		ISymbol t = table.newSymbol( "t", TypeInfo.t_type );
		ISymbol look = template.lookup( "T" );
		assertEquals( look, param );
		t.setTypeSymbol( param );
		A.addSymbol( t );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( B );
		
		TypeInfo type = new TypeInfo( TypeInfo.t_int, 0 , null );
		ArrayList args = new ArrayList();
		args.add( type );
		
		look = table.getCompilationUnit().lookupTemplateId( "A", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A );
		
		B.addParent( look );
		
		ISymbol b = table.newSymbol( "b", TypeInfo.t_type );
		b.setTypeSymbol( B );
		table.getCompilationUnit().addSymbol( b );
		
		look = table.getCompilationUnit().lookup( "b" );
		assertEquals( look, b );
		
		look = ((IDerivableContainerSymbol) b.getTypeSymbol()).lookup( "t" );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), t );
		assertTrue( look.isType( TypeInfo.t_int ) );
	}
	
	/**
	 * The scope of a template-parameter extends from its point of declaration 
	 * until the end of its template.  In particular, a template parameter can be used
	 * in the declaration of a subsequent template-parameter and its default arguments.
	 * @throws Exception
	 * 
	 * template< class T, class U = T > class X 
	 * { 
	 *    T t; 
	 *    U u; 
	 * };
	 * 
	 * X< char > x;
	 * x.t;
	 * x.u;
	 */
	public void testTemplateParameterDefaults() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" );
		
		ISymbol paramT = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( paramT );
		
		ISymbol look = template.lookup( "T" );
		assertEquals( look, paramT );
		ISymbol paramU = table.newSymbol( "U", TypeInfo.t_templateParameter );
		paramU.getTypeInfo().setDefault( new TypeInfo( TypeInfo.t_type, 0, look ) );
		template.addTemplateParameter( paramU );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", TypeInfo.t_class );
		factory.addSymbol( X );
		
		look = X.lookup( "T" );
		assertEquals( look, paramT );
		ISymbol t = table.newSymbol( "t", TypeInfo.t_type );
		t.setTypeSymbol( look );
		X.addSymbol( t );
		
		look = X.lookup( "U" );
		assertEquals( look, paramU );
		ISymbol u = table.newSymbol( "u", TypeInfo.t_type );
		u.setTypeSymbol( look );
		X.addSymbol( u );
		
		TypeInfo type = new TypeInfo( TypeInfo.t_char, 0, null );
		ArrayList args = new ArrayList();
		args.add( type );
		IDerivableContainerSymbol lookX = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "X", args );
		assertTrue( lookX.isTemplateInstance() );
		assertEquals( lookX.getInstantiatedSymbol(), X );
				
		
		look = lookX.lookup( "t" );
		
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), t );
		assertTrue( look.isType( TypeInfo.t_char ) );
		
		look = lookX.lookup( "u" );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), u );
		assertTrue( look.isType( TypeInfo.t_char ) );	
	}
	
	/**
	 * 
	 * @throws Exception
	 * template  < class T > class A {
	 *    T t;
	 * };
	 * class B {};
	 * void f( char c ) {}
	 * void f( A<B> b ) { ... }
	 * void f( int i ) {}
	 * 
	 * A<B> a;
	 * f( a );	//calls f( A<B> )
	 * 
	 */
	public void testTemplateParameterAsFunctionArgument() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" );
		ISymbol paramT = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( paramT );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		factory.addSymbol( A );
		
		ISymbol t = table.newSymbol( "t", TypeInfo.t_type );
		t.setTypeSymbol( paramT );
		A.addSymbol( t );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( B );
		
		ArrayList args = new ArrayList();
		TypeInfo arg = new TypeInfo( TypeInfo.t_type, 0, B );
		args.add( arg );
		
		IDerivableContainerSymbol lookA = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args );
		assertTrue( lookA.isTemplateInstance() );
		assertEquals( lookA.getInstantiatedSymbol(), A );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f1.addParameter( TypeInfo.t_char, 0, null, false );
		table.getCompilationUnit().addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f2.addParameter( lookA, 0, null, false );
		table.getCompilationUnit().addSymbol( f2 );
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f3.addParameter( TypeInfo.t_int, 0, null, false );
		table.getCompilationUnit().addSymbol( f3 );

		args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_type, 0, B ) );
		IDerivableContainerSymbol lookA2 = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args );
		assertEquals( lookA2, lookA );
		
		
		ISymbol a = table.newSymbol( "a", TypeInfo.t_type );
		a.setTypeSymbol( lookA );
		table.getCompilationUnit().addSymbol( a );

		ArrayList params = new ArrayList();
		params.add( new TypeInfo( TypeInfo.t_type, 0, a ) );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );		
	}

	
	/**
	 * class T { };
	 * int i;
	 * 
	 * template< class T, T i > void  f( T t )
	 * {
	 *    T t1 = i;   //template parameters T & i 
	 * }
	 */
	public void test_14_1__3_ParameterLookup() throws Exception{
		newTable();
		
		IDerivableContainerSymbol T = table.newDerivableContainerSymbol( "T", TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( T );
		
		ISymbol i = table.newSymbol( "i", TypeInfo.t_int );
		table.getCompilationUnit().addSymbol( i );
		
		ITemplateSymbol template = table.newTemplateSymbol( "f" );
				
		ISymbol paramT = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( paramT );

		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		ISymbol parami = table.newSymbol( "i", TypeInfo.t_templateParameter );
		parami.getTypeInfo().setTemplateParameterType( TypeInfo.t_type );
		
		ISymbol look = factory.lookup( "T" );

		assertEquals( look, paramT );
		parami.setTypeSymbol( look );
		template.addTemplateParameter( parami );

		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		ISymbol fParam = table.newSymbol( "t", TypeInfo.t_type );
		fParam.setTypeSymbol( paramT );
		f.addParameter( fParam );
		
		factory.addSymbol( f );
		
		look = f.lookup( "T" );
		assertEquals( look, paramT );
		
		look = f.lookup( "i" );
		assertEquals( look, parami );
	}
	
	/**
	 * A non-type template parameter of type "array of T" or "function returning T" is adjusted to
	 * be of type "pointer to T" or "pointer to function returning T" respectively
	 * 
	 * template < int *a > struct R {};
	 * template < int b[5] > struct S {};
	 * 
	 * int *p;
	 * R<p> w;  //ok
	 * S<p> x;  //ok due to parameter adjustment
	 * int v[5];
	 * R<v> y;  //ok due to implicit argument conversion
	 * S<v> z;  //ok due to adjustment and conversion 
	 * @throws Exception
	 */
	public void test_14_1__8_ParameterAdjustment() throws Exception{
		newTable();
		
		ITemplateSymbol templateR = table.newTemplateSymbol( "R" );
		
		ISymbol paramA = table.newSymbol( "a", TypeInfo.t_templateParameter );
		paramA.getTypeInfo().setTemplateParameterType( TypeInfo.t_int );
		paramA.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		templateR.addTemplateParameter( paramA );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateR );
		
		IDerivableContainerSymbol R = table.newDerivableContainerSymbol( "R", TypeInfo.t_struct );
		factory.addSymbol( R );
				
		ITemplateSymbol templateS = table.newTemplateSymbol( "S" );
				
		ISymbol paramB = table.newSymbol( "b", TypeInfo.t_templateParameter );
		paramB.getTypeInfo().setTemplateParameterType( TypeInfo.t_int );
		paramB.addPtrOperator( new PtrOp( PtrOp.t_array ) );
		templateS.addTemplateParameter( paramB );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateS ); 
		
		IDerivableContainerSymbol S = table.newDerivableContainerSymbol( "S", TypeInfo.t_struct );
		factory.addSymbol( S );
				
		ISymbol look = table.getCompilationUnit().lookup( "S" );
		assertEquals( look, templateS );
		
		Iterator iter = templateS.getParameterList().iterator();
		ISymbol param = (ISymbol) iter.next();
		assertFalse( iter.hasNext() );
		iter = param.getTypeInfo().getPtrOperators().iterator();
		PtrOp ptr = (PtrOp) iter.next();
		assertFalse( iter.hasNext() );
		assertEquals( ptr.getType(), PtrOp.t_pointer );
		
		ISymbol p = table.newSymbol( "p", TypeInfo.t_int );
		p.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		table.getCompilationUnit().addSymbol( p );
		
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_type, 0, p ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "R", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), R );
		
		look = table.getCompilationUnit().lookupTemplateId( "S", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), S );
		
		ISymbol v = table.newSymbol( "v", TypeInfo.t_int );
		v.addPtrOperator( new PtrOp( PtrOp.t_array ) );
		table.getCompilationUnit().addSymbol( v );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_type, 0, v ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "R", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), R );

		look = table.getCompilationUnit().lookupTemplateId( "S", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), S );
	}

	/**
	 * When default template-arguments are used, a template-argument list can be empty.  In that 
	 * case, the empty <> brackets shall still be used as the template-argument list
	 * 
	 * template< class T = char > class String;
	 * String <> * p;	//ok, T = char
	 * 
	 * @throws Exception
	 */
	public void test_14_3__4_ParameterDefaults() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "String" );
		
		ISymbol param = table.newSymbol( "T", TypeInfo.t_templateParameter );
		param.getTypeInfo().setDefault( new TypeInfo( TypeInfo.t_char, 0, null ) );
		template.addTemplateParameter( param );

		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol string = table.newDerivableContainerSymbol( "String", TypeInfo.t_class );
		factory.addSymbol( string );
		
		List args = new ArrayList();
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "String", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), string );
	}	
	
	/**
	 * A local type, a type with no linkage, an unnamed type or a type compounded from 
	 * any of these type shall not be used as a template-argument for a template-parameter
	 * 
	 * template <class T> class X { };
	 * void f(){
	 *    struct S { };
	 * 
	 *    X<S> x;   //error
	 *    X<S*> y; //error
	 * }
	 * @throws Exception
	 */
	public void test_14_3_1__2_TypeArgumentRestrictions() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" );
		template.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		factory.addSymbol( table.newDerivableContainerSymbol( "X", TypeInfo.t_class ) );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		table.getCompilationUnit().addSymbol( f );
		
		IDerivableContainerSymbol S = table.newDerivableContainerSymbol( "S", TypeInfo.t_struct );
		f.addSymbol( S );
		
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_type, 0, S ) );
		try{
			f.lookupTemplateId( "X", args );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_type, 0, S, new PtrOp( PtrOp.t_pointer ), false ) );
		try{
			f.lookupTemplateId( "X", args );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
	}
	
	/**
	 * A String literal is not an acceptable template-argument for a non-type, non-template parameter 
	 * because a string literal is an object with internal linkage
	 * 
	 * template< class T, char* p> class X {};
	 * 
	 * X< int, "Studebaker" > x1; //error
	 * 
	 * char p [] = "Vivisectionist";
	 * X< int, p >  x2; 		//ok
	 * 
	 * @throws Exception
	 */
	public void test_14_3_2__2_NonTypeArgumentRestrictions() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" );
		template.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ISymbol param2 = table.newSymbol( "p", TypeInfo.t_templateParameter );
		param2.getTypeInfo().setTemplateParameterType( TypeInfo.t_char );
		param2.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		template.addTemplateParameter( param2 );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", TypeInfo.t_class );
		factory.addSymbol( X );
		
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_char, 0, null, new PtrOp( PtrOp.t_pointer ), "Studebaker" ) );
		
		try{
			table.getCompilationUnit().lookupTemplateId( "X", args );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		ISymbol p = table.newSymbol( "p", TypeInfo.t_char );
		p.addPtrOperator( new PtrOp( PtrOp.t_array ) );
		table.getCompilationUnit().addSymbol( p );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_type, 0, p ) );
		
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "X", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), X );
	}
	
	/**
	 * names or addresses of non-static class members are not acceptable non-type template arguments
	 * 
	 * template < int * p > class X {};
	 * 
	 * struct S 
	 * { 
	 *    int m; 
	 *    static int s;
	 *    int * t; 
	 * } s;
	 * 
	 * X<&s.m> x1;   //error, address of non-static member
	 * X<s.t> x2;    //error, name of non-static member
	 * X<&S::s> x3;  //ok, address of static member
	 * 
	 * @throws Exception
	 */
	public void test_14_3_2__3_NonTypeArgumentRestrictions() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" );
		
		ISymbol param = table.newSymbol( "p", TypeInfo.t_templateParameter );
		param.getTypeInfo().setTemplateParameterType( TypeInfo.t_int );
		param.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		template.addTemplateParameter( param );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", TypeInfo.t_class );
		factory.addSymbol( X );
		
		IDerivableContainerSymbol S = table.newDerivableContainerSymbol( "S", TypeInfo.t_struct );
		table.getCompilationUnit().addSymbol( S );
		
		ISymbol m = table.newSymbol( "m", TypeInfo.t_int );
		S.addSymbol( m );
		ISymbol s = table.newSymbol( "s", TypeInfo.t_int );
		s.getTypeInfo().setBit( true, TypeInfo.isStatic );
		S.addSymbol( s );
		ISymbol t = table.newSymbol( "t", TypeInfo.t_int );
		t.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		S.addSymbol( t );
				
		List args = new ArrayList();
		TypeInfo arg =  new TypeInfo( TypeInfo.t_type, 0, m );
		arg.addOperatorExpression( TypeInfo.OperatorExpression.addressof );
		args.add( arg );
		
		try
		{
			table.getCompilationUnit().lookupTemplateId( "X", args );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_type, 0, t ) );

		try
		{
			table.getCompilationUnit().lookupTemplateId( "X", args );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		args.clear();
		arg =  new TypeInfo( TypeInfo.t_type, 0, s );
		arg.addOperatorExpression( TypeInfo.OperatorExpression.addressof );
		args.add( arg );
		
		assertNotNull( table.getCompilationUnit().lookupTemplateId( "X", args ) );
	}
	
	/**
	 * Tempories, unnamed lvalues, and named lvalues that do not have external linkage are
	 * not acceptable template-arguments when the corresponding template-parameter has 
	 * reference type
	 * 
	 * template< const int & I > struct B {};
	 * 
	 * B<1> b1;   	//error, temporary would be required
	 * int c = 1;
	 * B<c> b2;		//ok
	 * @throws Exception
	 */
	public void test_14_3_2__4_NonTypeArgumentRestrictions() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "B" );
		
		ISymbol I = table.newSymbol( "I", TypeInfo.t_templateParameter );
		I.getTypeInfo().setTemplateParameterType( TypeInfo.t_int );
		I.addPtrOperator( new PtrOp( PtrOp.t_reference, true, false ) );
		template.addTemplateParameter( I );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_struct );
		factory.addSymbol( B );
		
		List args = new ArrayList( );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, "1" ) );
		
		try{
			table.getCompilationUnit().lookupTemplateId( "B", args );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		ISymbol c = table.newSymbol( "c", TypeInfo.t_int );
		table.getCompilationUnit().addSymbol( c );
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_type, 0, c ) );
		
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "B", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), B );
	}

	/**
	 * template< class T > class A {
	 *    int x;
	 * };
	 * 
	 * template < class T > class A < T * > {
	 *    long x;
	 * };
	 * 
	 * template< template< class U > class V > class C{
	 *    V< int > y;
	 *    V< int * > z;
	 * }
	 * 
	 * C< A > c;	//V<int> uses primary template, so C.y.x is type int
	 *              //V<int*> uses partial specialization, so C.z.x is type long
	 * 
	 * @throws Exception
	 */
	  public void test_14_3_3__2_PartialSpecialization_TemplateTemplateParameter() throws Exception{
	  	//TODO
		newTable();
		
		ITemplateSymbol templateA = table.newTemplateSymbol( "A" );
		table.getCompilationUnit().addSymbol( templateA );
		
		templateA.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		IDerivableContainerSymbol A1 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		templateA.addSymbol( A1 );
		
		ISymbol x1 = table.newSymbol( "x", TypeInfo.t_int );
		A1.addSymbol( x1 );
		
		ISpecializedSymbol specialization = table.newSpecializedSymbol( "A" );
		templateA.addSpecialization( specialization );
		
		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		specialization.addTemplateParameter( T );
		specialization.addArgument( new TypeInfo( TypeInfo.t_type, 0, T, new PtrOp( PtrOp.t_pointer ), false ) );
		
		IDerivableContainerSymbol A2 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		specialization.addSymbol( A2 );
		
		ISymbol x2 = table.newSymbol( "x", TypeInfo.t_int );
		x2.getTypeInfo().setBit( true, TypeInfo.isLong );
		A2.addSymbol( x2 );
		
		ITemplateSymbol templateC = table.newTemplateSymbol( "C" );
		table.getCompilationUnit().addSymbol( templateC );
		
		ITemplateSymbol templateV = table.newTemplateSymbol( "V" );
		templateV.setType( TypeInfo.t_templateParameter );
		templateV.getTypeInfo().setTemplateParameterType( TypeInfo.t_template );
		ISymbol U = table.newSymbol( "U", TypeInfo.t_templateParameter );
		templateV.addTemplateParameter( U );
		
		templateC.addTemplateParameter( templateV );
		
		IDerivableContainerSymbol C	= table.newDerivableContainerSymbol( "C", TypeInfo.t_class );
		templateC.addSymbol( C );
		
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		
		ISymbol look = templateC.lookupTemplateId( "V", args );
		assertTrue( look != null );
		assertTrue( look instanceof IDeferredTemplateInstance );
		
		ISymbol y = table.newSymbol( "y", TypeInfo.t_type );
		y.setTypeSymbol( look );
		
		C.addSymbol( y );

		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		
		look = templateC.lookupTemplateId( "V", args );
		assertTrue( look != null );
		assertTrue( look instanceof IDeferredTemplateInstance );
		
		ISymbol z = table.newSymbol( "z", TypeInfo.t_type );
		z.setTypeSymbol( look );
		C.addSymbol( z );
		
		look = table.getCompilationUnit().lookup( "A" );
		assertEquals( look, templateA );
		
		args.clear();
		args.add ( new TypeInfo( TypeInfo.t_type, 0, look ) );
		look = table.getCompilationUnit().lookupTemplateId( "C", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), C );
		
		IDerivableContainerSymbol lookC = (IDerivableContainerSymbol)look;
		look = lookC.lookup( "y" );
		
		assertEquals( look.getType(), TypeInfo.t_type );
		ISymbol symbol = look.getTypeSymbol();
		assertTrue( symbol instanceof IContainerSymbol );
		assertTrue( symbol.isTemplateInstance() );
		assertEquals( symbol.getInstantiatedSymbol(), A1 );
		
		look = ((IContainerSymbol) symbol).lookup( "x" );
		
		assertEquals( look.getType(), TypeInfo.t_int );
		assertEquals( look.getTypeInfo().checkBit( TypeInfo.isLong ), false );
		
		look = lookC.lookup( "z" );
		assertEquals( look.getType(), TypeInfo.t_type );
		symbol = look.getTypeSymbol();
		assertTrue( symbol instanceof IContainerSymbol );
		assertTrue( symbol.isTemplateInstance() );
		assertEquals( symbol.getInstantiatedSymbol(), A2 );
		look = ((IContainerSymbol)symbol).lookup( "x" );
		
		assertEquals( look.getType(), TypeInfo.t_int );
		assertEquals( look.getTypeInfo().checkBit( TypeInfo.isLong ), true );
	}
	
	/**
	 * template< class T1, class T2 > struct A {
	 *    T1 f1();
	 *    void f2();
	 * };
	 * 
	 * template<class U, class V> U A< U, V >::f1() {} //ok 
	 * 
	 * template<class X, class Y> void A< Y, X >::f2() {}  //error
	 * 
	 * 
	 * @throws Exception
	 */
	 public void test_14_5_1__3_MemberFunctions() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		ISymbol primaryT1 = table.newSymbol( "T1", TypeInfo.t_templateParameter );
		ISymbol primaryT2 = table.newSymbol( "T2", TypeInfo.t_templateParameter );
		template.addTemplateParameter( primaryT1 );		
		template.addTemplateParameter( primaryT2 );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		
		factory.addSymbol( A );	
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f1", TypeInfo.t_function );
		
		ISymbol look = A.lookup( "T1" );
		assertEquals( look, primaryT1 );
		
		f1.setIsForwardDeclaration( true );
		f1.setReturnType( look );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f2", TypeInfo.t_function );
		f2.setIsForwardDeclaration( true );
		
		A.addSymbol( f1 );
		A.addSymbol( f2 );
		
		ITemplateSymbol temp = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		ISymbol U = table.newSymbol( "U", TypeInfo.t_templateParameter );
		ISymbol V = table.newSymbol( "V", TypeInfo.t_templateParameter );
		temp.addTemplateParameter( U );
		temp.addTemplateParameter( V );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		ISymbol returnType = factory.lookup( "U" );
		assertEquals( returnType, U );
		
		List args  = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_type, 0, U ) ); 
		args.add( new TypeInfo( TypeInfo.t_type, 0, V ) );
		
		look = factory.lookupTemplateIdForDefinition( "A", args );
		assertEquals( look, A );
		factory.pushTemplateId( look, args );
		
		IParameterizedSymbol lookF = factory.lookupMethodForDefinition( "f1", new ArrayList() );
		assertEquals( lookF, f1 );
		assertTrue( lookF.isForwardDeclaration() );
		
		IParameterizedSymbol defnd = table.newParameterizedSymbol( "f1", TypeInfo.t_function );
		f1.setTypeSymbol( defnd );
		defnd.setReturnType( returnType );
		factory.addSymbol( defnd );
		
		//Test that the adding was all good by doing a lookup
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_char, 0, null ) );
		
		IDerivableContainerSymbol lookA = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args );
		assertTrue( lookA.isTemplateInstance() );
		assertEquals( lookA.getInstantiatedSymbol(), A );
		
		List params = new ArrayList();
		look = lookA.qualifiedFunctionLookup( "f1", params );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), defnd );
		assertTrue( ((IParameterizedSymbol)look).getReturnType().isType( TypeInfo.t_int ) );
		
		params.clear();
		args.clear();
		
		temp = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		ISymbol X = table.newSymbol( "X", TypeInfo.t_templateParameter );
		ISymbol Y = table.newSymbol( "Y", TypeInfo.t_templateParameter );
		temp.addTemplateParameter( X );
		temp.addTemplateParameter( Y );

		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		args.add( new TypeInfo( TypeInfo.t_type, 0, Y ) );
		args.add( new TypeInfo( TypeInfo.t_type, 0, X ) ); 

		try{
			look = factory.lookupTemplateIdForDefinition( "A", args );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplate );
		}
	}
	
	/**
	 * template<class T> struct A{
	 *    class B;
	 * };
	 * template< class U > class A<U>::B { U i; };
	 * A<int>::B b;
	 * 
	 * @throws Exception
	 */
	  public void test_14_5_1_2_MemberClass() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" );
		ISymbol primaryT = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( primaryT );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_struct );
		factory.addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		B.setIsForwardDeclaration( true );
		A.addSymbol( B );
		
		ISymbol U = table.newSymbol( "U", TypeInfo.t_templateParameter );
		ITemplateSymbol temp = table.newTemplateSymbol( "" );
		temp.addTemplateParameter( U );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
				
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_type, 0, U ) );
		
		IContainerSymbol lookA = factory.lookupTemplateIdForDefinition( "A", args );
		assertEquals( lookA, A );
		factory.pushTemplateId( lookA, args );
		
		ISymbol look = lookA.lookupMemberForDefinition( "B" );
		assertEquals( look, B );
		
		IDerivableContainerSymbol newB = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		look.setTypeSymbol( newB );
		
		factory.addSymbol( newB );
		
		ISymbol i = table.newSymbol( "i", TypeInfo.t_type );
		look = newB.lookup( "U" );
		assertEquals( look, U );
		i.setTypeSymbol( U );
		newB.addSymbol( i );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "A", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A );
		
		assertTrue( look instanceof IDerivableContainerSymbol );
		lookA = (IDerivableContainerSymbol) look;
		look = lookA.qualifiedLookup( "B" );
		
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), newB );
		
		look = ((IDerivableContainerSymbol) look).lookup( "i" );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), i );
		assertEquals( look.getType(), TypeInfo.t_int );		
	}
	
	/**
	 * template< class T> class X{
	 *    static T s;
	 * };
	 * 
	 * template<class U> U X<U>::s = 0;
	 * 
	 * @throws Exception
	 */
	  public void test_14_5_1_3_StaticDataMember() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" );
		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", TypeInfo.t_class );
		factory.addSymbol( X );
		
		ISymbol look = X.lookup( "T" );
		assertEquals( look, T );
		
		ISymbol s = table.newSymbol( "s", TypeInfo.t_type );
		s.setTypeSymbol( look );
		s.getTypeInfo().setBit( true, TypeInfo.isStatic );
		s.setIsForwardDeclaration( true );
		X.addSymbol( s );
		
		ITemplateSymbol temp = table.newTemplateSymbol( "" );
		ISymbol paramU = table.newSymbol( "U", TypeInfo.t_templateParameter );
		temp.addTemplateParameter( paramU );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_type, 0, paramU ) );
		
		look = factory.lookupTemplateIdForDefinition( "X", args );
		assertEquals( look, X );
		factory.pushTemplateId( X, args );
		
		look = ((IContainerSymbol)look).lookupMemberForDefinition("s" );
		assertEquals( look, s );
		assertTrue( look.isForwardDeclaration() );
		
		ISymbol newS = table.newSymbol( "s", TypeInfo.t_type );
		newS.setTypeSymbol( paramU );
		
		look.setTypeSymbol( newS );
		
		factory.addSymbol( newS );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_float, 0, null ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "X", args );
		
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), X );
		
		look = ((IContainerSymbol)look).qualifiedLookup( "s" );
		
		assertTrue( look.isType( TypeInfo.t_float ) );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), newS );
		
	}
	  
	/**
	 * template< class T > class string{
	 *    template< class T2 > T2 compare( const T2 & );
	 * };
	 * 
	 * template< class U > template< class V > V string<U>::compare( const V & ) {
	 *     U u;
	 * }
	 * @throws Exception
	 */
	public void test_14_5_2__1_MemberTemplates() throws Exception{
		newTable();
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "string" );
		template1.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template1 );
		
		IDerivableContainerSymbol string = table.newDerivableContainerSymbol( "string", TypeInfo.t_class );
		factory.addSymbol( string );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "compare" );
		ISymbol T2 = table.newSymbol( "T2", TypeInfo.t_templateParameter );
		template2.addTemplateParameter( T2 );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( string );
		factory.pushTemplate( template2 );
				
		IParameterizedSymbol compare = table.newParameterizedSymbol( "compare", TypeInfo.t_function );
		compare.setIsForwardDeclaration( true );
		compare.addParameter( T2, 0, new PtrOp( PtrOp.t_reference, true, false ), false );
		ISymbol returnType = table.newSymbol( "", TypeInfo.t_type );
		returnType.setTypeSymbol( T2 );
		compare.setReturnType( returnType );
		factory.addSymbol( compare );
		
		ITemplateSymbol temp = table.newTemplateSymbol( "" );
		ISymbol U = table.newSymbol( "U", TypeInfo.t_templateParameter );
		temp.addTemplateParameter( U );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		ITemplateSymbol temp2 = table.newTemplateSymbol( "" );
		ISymbol V = table.newSymbol( "V", TypeInfo.t_templateParameter );
		temp2.addTemplateParameter( V );
		
		factory.pushTemplate( temp2 );

		ISymbol lookV = factory.lookup( "V" );
		assertEquals( lookV, V );
		
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_type, 0, U ) );
		
		ISymbol look = factory.lookupTemplateIdForDefinition( "string", args );
		assertEquals( look, string );
		factory.pushTemplateId( look, args );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_type, 0, lookV,  new PtrOp( PtrOp.t_reference, true, false ), false ) );

		look = ((IContainerSymbol)look).lookupMethodForDefinition( "compare", args );
		assertEquals( look, compare );
		
		IParameterizedSymbol compareDef = table.newParameterizedSymbol( "compare", TypeInfo.t_function );
		compareDef.addParameter( lookV, 0, new PtrOp( PtrOp.t_reference, true, false ), false );
		ISymbol defReturn = table.newSymbol( "", TypeInfo.t_type );
		defReturn.setTypeSymbol( lookV );
		compareDef.setReturnType( defReturn );
		compare.setTypeSymbol( compareDef );
		factory.addSymbol( compareDef );
		
		look = compareDef.lookup( "U" );
		assertEquals( look, U );
		
		ISymbol u = table.newSymbol( "u", TypeInfo.t_type );
		u.setTypeSymbol( look );
		
		compareDef.addSymbol( u );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "string", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), string );
		
		look = ((IDerivableContainerSymbol)look).lookupTemplateId( "compare", args );
		assertTrue( look.isTemplateInstance() );
		assertTrue( look.getInstantiatedSymbol().isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol().getInstantiatedSymbol(), compareDef );
		
		assertTrue( ((IParameterizedSymbol)look).getReturnType().isType( TypeInfo.t_int ) );
		
		look = ((IContainerSymbol)look).lookup( "u" );
		assertTrue( look.isTemplateInstance() );
		assertTrue( look.getInstantiatedSymbol().isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol().getInstantiatedSymbol(), u );
		assertTrue( look.isType( TypeInfo.t_int ) );
	}
	
	/**
	 *  A member function template shall not be virtual
	 * 
	 * template< class T > struct A {
	 *    template < class C > virtual void g( C ); //error
	 *    virtual void f();  //ok
	 * };
	 * @throws Exception
	 */
	public void test_14_5_2__3_VirtualMemberFunctionTemplate() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" );
		template.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_struct );
		factory.addSymbol( A );
		
		ITemplateSymbol memberTemplate = table.newTemplateSymbol( "g" );
		ISymbol C = table.newSymbol( "C", TypeInfo.t_templateParameter );
		memberTemplate.addTemplateParameter( C );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( A );
		factory.pushTemplate( memberTemplate );
		
		IParameterizedSymbol g = table.newParameterizedSymbol( "g", TypeInfo.t_function );
		g.addParameter( C, 0, null, false );
		g.getTypeInfo().setBit( true, TypeInfo.isVirtual );
		
		try{
			factory.addSymbol( memberTemplate );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplate );
		}
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f.getTypeInfo().setBit( true, TypeInfo.isVirtual );
		
		A.addSymbol( f );
	}
	
	/**
	 * Partial specialization declarations are not found by name lookup,  Rather, when the primary 
	 * template name is used, any previously declared partial template specializations of the 
	 * primary template are also considered.
	 * One consequence is that a using-declaration which refers to a class template does not restrict
	 * the set of partial specializations which may be found through the using-declaration.
	 * 
	 * namespace N{
	 *    template< class T1, class T2 > class A {};
	 * }
	 * 
	 * using N::A;
	 * 
	 * namespace N{
	 *    template< class T> class A < T, T * > {};
	 * }
	 * 
	 * A< int, int * > a;  //uses partial specialization
	 * 
	 * @throws Exception
	 */
	  public void test_14_5_4__7_PartialSpecializationLookup() throws Exception{
	  	//TODO
		newTable();
		
		IContainerSymbol N = table.newContainerSymbol( "N", TypeInfo.t_namespace );
		
		table.getCompilationUnit().addSymbol( N );
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" );
		
		template.addTemplateParameter( table.newSymbol( "T1", TypeInfo.t_templateParameter ) );
		template.addTemplateParameter( table.newSymbol( "T2", TypeInfo.t_templateParameter ) );
		
		IDerivableContainerSymbol A1 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		
		template.addSymbol( A1 );
		
		N.addSymbol( template );
		
		table.getCompilationUnit().addUsingDeclaration( "A", N );
		
		ISpecializedSymbol spec = table.newSpecializedSymbol( "A" );
		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		spec.addTemplateParameter( T );
		spec.addArgument( new TypeInfo( TypeInfo.t_type, 0, T ) );
		spec.addArgument( new TypeInfo( TypeInfo.t_type, 0, T, new PtrOp( PtrOp.t_pointer ), false ) );
		
		IDerivableContainerSymbol A2 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		spec.addSymbol( A2 );
		template.addSpecialization( spec );
		
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "A", args ); 
		assertTrue( look != null );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A2 );
	}
	
	/**
	 * 
	 * template < class T1, class T2, int I > class A                {}  //#1
	 * template < class T, int I >            class A < T, T*, I >   {}  //#2
	 * template < class T1, class T2, int I > class A < T1*, T2, I > {}  //#3
	 * template < class T >                   class A < int, T*, 5 > {}  //#4
	 * template < class T1, class T2, int I > class A < T1, T2*, I > {}  //#5
	 * 
	 * A <int, int, 1>   a1;		//uses #1
	 * A <int, int*, 1>  a2;		//uses #2, T is int, I is 1
	 * A <int, char*, 5> a3;		//uses #4, T is char
	 * A <int, char*, 1> a4;		//uses #5, T is int, T2 is char, I is1
	 * A <int*, int*, 2> a5;		//ambiguous, matches #3 & #5.
	 * 
	 * @throws Exception   
	 */
	  public void test_14_5_4_1__2_MatchingTemplateSpecializations() throws Exception{
	  	//TODO
		newTable();
		
		IDerivableContainerSymbol cls1 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol cls2 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol cls3 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol cls4 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol cls5 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "A" );
		ISymbol T1p1 = table.newSymbol( "T1", TypeInfo.t_templateParameter );
		ISymbol T1p2 = table.newSymbol( "T2", TypeInfo.t_templateParameter );
		ISymbol T1p3 = table.newSymbol( "I", TypeInfo.t_templateParameter );
		T1p3.getTypeInfo().setTemplateParameterType( TypeInfo.t_int );
		
		template1.addTemplateParameter( T1p1 );
		template1.addTemplateParameter( T1p2 );
		template1.addTemplateParameter( T1p3 );
		template1.addSymbol( cls1 );
		table.getCompilationUnit().addSymbol( template1 );
		
		ISpecializedSymbol template2 = table.newSpecializedSymbol( "A" );
		ISymbol T2p1 = table.newSymbol( "T", TypeInfo.t_templateParameter );
		ISymbol T2p2 = table.newSymbol( "I", TypeInfo.t_templateParameter );
		T2p2.getTypeInfo().setTemplateParameterType( TypeInfo.t_int );
		
		template2.addTemplateParameter( T2p1 );
		template2.addTemplateParameter( T2p2 );
		
		TypeInfo T2a1 = new TypeInfo( TypeInfo.t_type, 0, T2p1 );
		TypeInfo T2a2 = new TypeInfo( TypeInfo.t_type, 0, T2p1, new PtrOp( PtrOp.t_pointer ), false );
		TypeInfo T2a3 = new TypeInfo( TypeInfo.t_type, 0, T2p2 );
		
		template2.addArgument( T2a1 );
		template2.addArgument( T2a2 );
		template2.addArgument( T2a3 );
		template2.addSymbol( cls2 );
		template1.addSpecialization( template2 );
		
		ISpecializedSymbol template3 = table.newSpecializedSymbol( "A" );
		ISymbol T3p1 = table.newSymbol( "T1", TypeInfo.t_templateParameter );
		ISymbol T3p2 = table.newSymbol( "T2", TypeInfo.t_templateParameter );
		ISymbol T3p3 = table.newSymbol( "I", TypeInfo.t_templateParameter );
		T3p3.getTypeInfo().setTemplateParameterType( TypeInfo.t_int );
		
		template3.addTemplateParameter( T3p1 );
		template3.addTemplateParameter( T3p2 );
		template3.addTemplateParameter( T3p3 );
		
		TypeInfo T3a1 = new TypeInfo( TypeInfo.t_type, 0, T3p1, new PtrOp( PtrOp.t_pointer ), false );
		TypeInfo T3a2 = new TypeInfo( TypeInfo.t_type, 0, T3p2 );
		TypeInfo T3a3 = new TypeInfo( TypeInfo.t_type, 0, T3p3 );
		
		template3.addArgument( T3a1 );
		template3.addArgument( T3a2 );
		template3.addArgument( T3a3 );
		template3.addSymbol( cls3 );
		template1.addSpecialization( template3 );
		
		ISpecializedSymbol template4 = table.newSpecializedSymbol( "A" );
		ISymbol T4p1 = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template4.addTemplateParameter( T4p1 );
		
		TypeInfo T4a1 = new TypeInfo( TypeInfo.t_int, 0, null );
		TypeInfo T4a2 = new TypeInfo( TypeInfo.t_type, 0, T4p1, new PtrOp( PtrOp.t_pointer ), false );
		TypeInfo T4a3 = new TypeInfo( TypeInfo.t_int, 0, null, null, "5" );
		//T4a3.setDefault( new Integer(5) );
		
		template4.addArgument( T4a1 );
		template4.addArgument( T4a2 );
		template4.addArgument( T4a3 );
		template4.addSymbol( cls4 );
		template1.addSpecialization( template4 );
		
		ISpecializedSymbol template5 = table.newSpecializedSymbol( "A" );
		ISymbol T5p1 = table.newSymbol( "T1", TypeInfo.t_templateParameter );
		ISymbol T5p2 = table.newSymbol( "T2", TypeInfo.t_templateParameter );
		ISymbol T5p3 = table.newSymbol( "I", TypeInfo.t_templateParameter );
		T5p3.getTypeInfo().setTemplateParameterType( TypeInfo.t_int );
		
		template5.addTemplateParameter( T5p1 );
		template5.addTemplateParameter( T5p2 );
		template5.addTemplateParameter( T5p3 );
		
		TypeInfo T5a1 = new TypeInfo( TypeInfo.t_type, 0, T5p1 );
		TypeInfo T5a2 = new TypeInfo( TypeInfo.t_type, 0, T5p2, new PtrOp( PtrOp.t_pointer ), false );
		TypeInfo T5a3 = new TypeInfo( TypeInfo.t_type, 0, T5p3 );
		
		template5.addArgument( T5a1 );
		template5.addArgument( T5a2 );
		template5.addArgument( T5a3 );
		template5.addSymbol( cls5 );
		template1.addSpecialization( template5 );
		
		ITemplateSymbol a = (ITemplateSymbol) table.getCompilationUnit().lookup( "A" );
		assertEquals( a, template1 );
		
		ArrayList args = new ArrayList();
		
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, new Integer(1) ) );
		
		IContainerSymbol a1 = (IContainerSymbol) a.instantiate( args );
		assertTrue( a1.isTemplateInstance() );
		assertEquals( a1.getInstantiatedSymbol(), cls1 );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, "1" ) );
		
		IContainerSymbol a2 = (IContainerSymbol) a.instantiate( args );
		assertTrue( a2.isTemplateInstance() );
		assertEquals( a2.getInstantiatedSymbol(), cls2 );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_char, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, "5" ) );
		IContainerSymbol a3 = (IContainerSymbol) a.instantiate( args );
		assertTrue( a3.isTemplateInstance() );
		assertEquals( a3.getInstantiatedSymbol(), cls4 );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_char, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, "1" ) );
		IContainerSymbol a4 = (IContainerSymbol) a.instantiate( args );
		assertTrue( a4.isTemplateInstance() );
		assertEquals( a4.getInstantiatedSymbol(), cls5 );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, "2" ) );
		
		try{
			a.instantiate( args );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}

	/**
	 * template< class T > void f( T );			//#1
	 * template< class T > void f( T* );		//#2
	 * template< class T > void f( const T* );	//#3
	 * 
	 * const int *p;
	 * f( p );	//calls f( const T * ) , 3 is more specialized than 1 or 2
	 *
	 * @throws Exception 
	 * 
	 */
	  public void test_14_5_5_2__5_OrderingFunctionTemplates_1() throws Exception{
		newTable();
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "f" );
		template1.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template1 );
		
		ISymbol T = template1.lookup( "T" );
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f1.addParameter( T, 0, null, false );
		factory.addSymbol( f1 );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "f" );
		template2.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template2 );
		
		T = template2.lookup( "T" );
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f2.addParameter( T, 0, new PtrOp( PtrOp.t_pointer ), false );
		factory.addSymbol( f2 );
		
		ITemplateSymbol template3 = table.newTemplateSymbol( "f" );
		template3.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template3 );
		
		T = template3.lookup( "T" );
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f3.addParameter( T, TypeInfo.isConst, new PtrOp( PtrOp.t_pointer, false, false ), false );
		factory.addSymbol( f3 );
		
		ISymbol p = table.newSymbol( "p", TypeInfo.t_int );
		p.getTypeInfo().setBit( true, TypeInfo.isConst );
		p.addPtrOperator( new PtrOp( PtrOp.t_pointer, false, false ) );
		table.getCompilationUnit().addSymbol( p );
		
		List params = new ArrayList();
		params.add( new TypeInfo( TypeInfo.t_type, 0, p ) );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f3 );
	}
	
	/**
	 * template< class T > void g( T );			//#1
	 * template< class T > void g( T& );		//#2

	 * float x;
	 * g( x );  //ambiguous 1 or 2
	 * 
	 * @throws Exception
	 */
	  public void test_14_5_5_2__5_OrderingFunctionTemplates_2() throws Exception{
		newTable();
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "g" );
		template1.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template1 );
		
		ISymbol T = template1.lookup( "T" );
		IParameterizedSymbol g1 = table.newParameterizedSymbol( "g", TypeInfo.t_function );
		g1.addParameter( T, 0, null, false );
		factory.addSymbol( g1 );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "g" );
		template2.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template2 );
		
		T = template2.lookup( "T" );
		IParameterizedSymbol g2 = table.newParameterizedSymbol( "g", TypeInfo.t_function );
		g2.addParameter( T, 0, new PtrOp( PtrOp.t_reference ), false );
		factory.addSymbol( g2 );
		
		ISymbol x = table.newSymbol( "x", TypeInfo.t_float );
		List params = new ArrayList();
		params.add( new TypeInfo( TypeInfo.t_type, 0, x ) );
		try{
			table.getCompilationUnit().unqualifiedFunctionLookup( "g", params );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}

	/**
	 * template< class T > struct A {  };
	 * 
	 * template< class T > void h( const T & );	//#1
	 * template< class T > void h( A<T>& );		//#2
	 * 
	 * A<int> z;
	 * h( z );  //calls 2
	 * 
	 * const A<int> z2;
	 * h( z2 ); //calls 1 because 2 is not callable.
	 * @throws Exception
	 */
	  public void test_14_5_5_2__5_OrderingFunctionTemplates_3() throws Exception{
		newTable();
		
		ITemplateSymbol templateA = table.newTemplateSymbol( "A" );
		templateA.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateA );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_struct );
		factory.addSymbol( A );
				
		ITemplateSymbol template1 = table.newTemplateSymbol( "h" );
		template1.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template1 );
		
		ISymbol T = template1.lookup( "T" );
		IParameterizedSymbol h1 = table.newParameterizedSymbol( "h", TypeInfo.t_function );
		h1.addParameter( T, TypeInfo.isConst, new PtrOp( PtrOp.t_reference, false, false ),false );
		factory.addSymbol( h1 );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "h" );
		template2.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template2 );
		
		T = template2.lookup( "T" );
		
		IParameterizedSymbol h2 = table.newParameterizedSymbol( "h", TypeInfo.t_function );
		List argList = new ArrayList();
		argList.add( new TypeInfo( TypeInfo.t_type, 0, T ) );
		h2.addParameter( templateA.instantiate( argList ), 0, new PtrOp( PtrOp.t_reference ), false );
		factory.addSymbol( h2 );
		
		ISymbol z = table.newSymbol( "z", TypeInfo.t_type );
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "A", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A );
		z.setTypeSymbol( look );
		
		List params = new ArrayList();
		params.add( new TypeInfo( TypeInfo.t_type, 0, z ) );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "h", params );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), h2 );
		
		ISymbol z2 = table.newSymbol( "z2", TypeInfo.t_type );
		look = table.getCompilationUnit().lookupTemplateId( "A", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A );
		z2.setTypeSymbol( look );
		z2.getTypeInfo().setBit( true, TypeInfo.isConst );
		
		params.clear();
		params.add( new TypeInfo( TypeInfo.t_type, 0, z2 ) );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "h", params );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), h1 );
	}
	
	/**
	 * Within the scope of a class template, when the name of the template is neither qualified
	 * nor followed by <, it is equivalent to the name of the template followed by the template-parameters
	 * enclosed in <>.
	 * 
	 * template < class T > class X {
	 *    X* p;			//meaning X< T > 
	 * };
	 * 
	 * @throws Exception
	 */
	  public void test_14_6_1__1_TemplateName() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" );
		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", TypeInfo.t_class );
		factory.addSymbol( X );
		
		ISymbol look = X.lookup( "X" );
		
		assertTrue( look != null );
		assertTrue( look instanceof IDeferredTemplateInstance );
		IDeferredTemplateInstance deferred = (IDeferredTemplateInstance) look;
		assertEquals( deferred.getTemplate(), template );
		
		Iterator iter = deferred.getArguments().iterator();
		TypeInfo type = (TypeInfo) iter.next();
		assertTrue( type.isType( TypeInfo.t_type ) );
		assertEquals( type.getTypeSymbol(), T );
	}
	
	/**
	 * Within the scope of a class template specialization or partial specialization, when the name of the
	 * template is neither qualified nor followed by <, it is equivalent to the name of the template 
	 * followed by the template-arguments enclosed in <>
	 * 
	 * template< class T > class Y;
	 * 
	 * template<> class Y< int > {
	 *   Y* p;   //meaning Y<int>
	 * } 
	 * @throws Exception
	 */
	  public void test_14_6_1__2_SpecializationName() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "Y" );
		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( T );
		IDerivableContainerSymbol Y1 = table.newDerivableContainerSymbol( "Y", TypeInfo.t_class );
		template.addSymbol( Y1 );
		
		table.getCompilationUnit().addSymbol( template );
		
		ISpecializedSymbol spec = table.newSpecializedSymbol( "Y" );
		spec.addArgument( new TypeInfo( TypeInfo.t_int, 0, null ) );
		
		template.addSpecialization( spec );
		
		IDerivableContainerSymbol Y2 = table.newDerivableContainerSymbol( "Y", TypeInfo.t_class );
		spec.addSymbol( Y2 );
		
		ISymbol look = Y2.lookup( "Y" );
		assertTrue( look != null );
		assertTrue( look instanceof IDeferredTemplateInstance );
		IDeferredTemplateInstance deferred = (IDeferredTemplateInstance) look;
		assertEquals( deferred.getTemplate(), spec );	
		
		Iterator iter = deferred.getArguments().iterator();
		TypeInfo type = (TypeInfo) iter.next();
		assertTrue( type.isType( TypeInfo.t_int ) );
	}

	/**
	 * A template-parameter shall not be redeclared within its scope.  A template-parameter shall
	 * not have the same name as the template name.
	 * 
	 * template< class T, int i > class Y {
	 *    int T;		//error
	 *    void f(){
	 *       char T;	//error
	 *    }
	 * };
	 * 
	 * template <class X> class X {};  //error
	 * 
	 * @throws Exception
	 */
	public void test_14_6_1__4_ParameterRedeclaration() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "Y" );
		template.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ISymbol i = table.newSymbol( "i", TypeInfo.t_templateParameter );
		i.getTypeInfo().setTemplateParameterType( TypeInfo.t_int );
		template.addTemplateParameter( i );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol Y = table.newDerivableContainerSymbol( "Y", TypeInfo.t_class );
		factory.addSymbol( Y );
		
		ISymbol T = table.newSymbol( "T", TypeInfo.t_int );
		try{
			Y.addSymbol( T );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_RedeclaredTemplateParam );
		}
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		Y.addSymbol( f );
		
		try{
			f.addSymbol( table.newSymbol( "T", TypeInfo.t_char ) );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_RedeclaredTemplateParam );
		}
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "X" );
		try{
			template2.addTemplateParameter( table.newSymbol( "X", TypeInfo.t_templateParameter ) );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateParameter );
		}
	}
		
		
	  /**
	   * A member of an explicitly specialized class shall be explicitly defined in the same
	   * manner as members of normal classes
	   * 
	   * template< class T > struct A {
	   *     void f( T ) {}
	   * };
	   * 
	   * template <> struct A< int >{
	   *    void f( int );
	   * }
	   * 
	   * void A< int >::f( int ){ }
	   * 
	   * @throws Exception
	   */
	public void test_14_7_3__5_ExplicitSpecialization() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" );
		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_struct );
		factory.addSymbol( A );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f.addParameter( T, 0, null, false );
		
		A.addSymbol( f );
		
		ITemplateSymbol temp = table.newTemplateSymbol( "" );
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		ArrayList args = new ArrayList();
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		ITemplateSymbol templateSpec = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		factory.pushTemplate( templateSpec );
		
		IDerivableContainerSymbol ASpec = table.newDerivableContainerSymbol( "A", TypeInfo.t_struct );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		factory.addTemplateId( ASpec, args );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f2.addParameter( TypeInfo.t_int, 0, null, false );
		f2.setIsForwardDeclaration( true );
		ASpec.addSymbol( f2 );

		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f3.addParameter( TypeInfo.t_int, 0, null, false );
		
		IDerivableContainerSymbol look = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), ASpec );
		
		ISymbol flook = look.lookupMethodForDefinition( "f", args );
		assertTrue( flook.isTemplateInstance() );
		assertEquals( flook.getInstantiatedSymbol(), f2 );
		flook.setTypeSymbol( f3 );
		
		look.addSymbol( f3 );
		
		look = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args );
		flook = look.qualifiedFunctionLookup( "f", args );
		
		assertEquals( flook, f3 );
	}
	
	/**
	 * template < class T > class Array { };
	 * template < class T > void sort( Array< T > & );
	 * 
	 * template<> void sort( Array< int > & ){}  //T deduced as int
	 * 
	 * @throws Exception
	 */
	public void test_14_7_3__11_ExplicitSpecializationArgumentDeduction() throws Exception{
		newTable();
		
		ITemplateSymbol templateArray = table.newTemplateSymbol( "Array" );
		templateArray.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( templateArray );
		
		IDerivableContainerSymbol array = table.newDerivableContainerSymbol( "Array", TypeInfo.t_class );
		factory.addSymbol( array );
		
		ITemplateSymbol templateSort = table.newTemplateSymbol( "sort" );
		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		templateSort.addTemplateParameter( T );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( templateSort );
		
		IParameterizedSymbol sort = table.newParameterizedSymbol( "sort", TypeInfo.t_function );
		
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_type, 0, T ) );

		ISymbol arrayLook = factory.lookupTemplateId( "Array", args );
		sort.addParameter( arrayLook, 0, new PtrOp( PtrOp.t_reference ), false );
		
		factory.addSymbol( sort );

		ITemplateSymbol temp = table.newTemplateSymbol( "" );
		factory = table.newTemplateFactory();
		
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
				
		IParameterizedSymbol newSort = table.newParameterizedSymbol( "sort", TypeInfo.t_function );
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		arrayLook = table.getCompilationUnit().lookupTemplateId( "Array", args );
		assertTrue( arrayLook.isTemplateInstance() );
		assertEquals( arrayLook.getInstantiatedSymbol(), array );
		newSort.addParameter( arrayLook, 0, new PtrOp( PtrOp.t_reference ), false );
		
		factory.addSymbol( newSort );
		
		ISymbol a = table.newSymbol( "a", TypeInfo.t_type );
		a.setTypeSymbol( arrayLook );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_type, 0, a ) );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "sort", args );
		
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), newSort );
	}
	
	/**
	 * It is possible for a specialization with a given function signature to be instantiated from more 
	 * than one function-template.  In such cases, explicit specification of the template arguments must be used
	 * to uniquely identify the function template specialization being specialized
	 * 
	 * template< class T > void f( T );
	 * template< class T > void f( T * );
	 * 
	 * template <> void f<int *>( int * );
	 * template <> void f< int >( int * );
	 * template <> void f( char );        
	 * 
	 * @throws Exception
	 */
	public void test_14_7_3__12_ExplicitSpecializationOverloadedFunction() throws Exception{
		newTable();
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "f" );
		ISymbol T1 = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template1.addTemplateParameter( T1 );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template1 );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f1.addParameter( T1, 0, null, false );
		
		factory.addSymbol( f1 );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "f" );
		ISymbol T2 = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template2.addTemplateParameter( T2 );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template2 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f2.addParameter( T2, 0, new PtrOp( PtrOp.t_pointer ), false );
		
		factory.addSymbol( f2 );
		
		factory = table.newTemplateFactory();
		ITemplateSymbol template = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
				
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f3.addParameter( TypeInfo.t_int, 0, new PtrOp( PtrOp.t_pointer ), false );
		
		List args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		factory.addTemplateId( f3, args );
		
		args = new ArrayList();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		
		template = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IParameterizedSymbol f4 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f4.addParameter( TypeInfo.t_int, 0, new PtrOp( PtrOp.t_pointer ), false );
		factory.addTemplateId( f4, args );

		args.clear();
		args.add( new TypeInfo( TypeInfo.t_char, 0, null ) );

		template = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IParameterizedSymbol f5 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f5.addParameter( TypeInfo.t_char, 0, null, false );
		factory.addSymbol( f5 );

		args.clear();
		args.add( new TypeInfo( TypeInfo.t_char, 0, null ) );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f5 );
	}

	
	/**
	 * template < class T > void f( T x, T y ) { }
	 * struct A {};
	 * struct B : A {};
	 * 
	 * A a;
	 * B b;
	 * 
	 * f( a, b ); //error, T could be A or B
	 * f( b, a ); //error, T could be A or B
	 * f( a, a ); //ok, T is A
	 * f( b, b ); //ok, T is B
	 * @throws Exception
	 */
	 public void test_14_8_2_4__5_ArgumentDeduction() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "f" );
		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		
		ISymbol lookT = template.lookup( "T" );
		assertEquals( lookT, T );
		
		ISymbol paramX = table.newSymbol( "x", TypeInfo.t_type );
		paramX.setTypeSymbol( lookT );
		
		ISymbol paramY = table.newSymbol( "y", TypeInfo.t_type );
		paramY.setTypeSymbol( lookT );
		
		f.addParameter( paramX );
		f.addParameter( paramY );
		
		factory.addSymbol( f );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_struct );
		table.getCompilationUnit().addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_struct );
		B.addParent( A );
		table.getCompilationUnit().addSymbol( B );
		
		ISymbol a = table.newSymbol( "a", TypeInfo.t_type );
		a.setTypeSymbol( A );
		
		ISymbol b = table.newSymbol( "b", TypeInfo.t_type );
		b.setTypeSymbol( B );
		
		table.getCompilationUnit().addSymbol( a );
		table.getCompilationUnit().addSymbol( b );
		
		List argList = new ArrayList();
		TypeInfo aParam =  new TypeInfo( TypeInfo.t_type, 0, a );
		TypeInfo bParam =  new TypeInfo( TypeInfo.t_type, 0, b );
		
		argList.add( aParam );
		argList.add( bParam );
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList );
		assertEquals( look, null );
		
		argList.clear();
		argList.add( bParam );
		argList.add( aParam );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList );
		assertEquals( look, null );
		
		argList.clear();
		argList.add( aParam );
		argList.add( aParam );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f );
		
		argList.clear();
		argList.add( bParam );
		argList.add( bParam );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList );
		assertTrue( look.isTemplateInstance());
		assertEquals( look.getInstantiatedSymbol(), f );
	}

	/**
	 * template< class T, class U > void f( T (*)( T, U, U ) );
	 * int g1( int, float, float );
	 * char g2( int, float, float );
	 * int g3( int, char, float );
	 * 
	 * f( g1 );	//OK, T is int and U is float
	 * f( g2 ); //error, T could be char or int
	 * f( g3 ); //error, U could be char or float
	 * 
	 * @throws Exception
	 */
	  public void test_14_8_2_4__6_ArgumentDeduction() throws Exception{
		newTable();
		
		ITemplateSymbol template = table.newTemplateSymbol( "f" );
		
		template.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		template.addTemplateParameter( table.newSymbol( "U", TypeInfo.t_templateParameter ) );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IParameterizedSymbol paramFunction = table.newParameterizedSymbol( "", TypeInfo.t_function );
		paramFunction.setIsTemplateMember( true );
		
		ISymbol T = template.lookup( "T" );
		ISymbol U = template.lookup( "U" );
		
		paramFunction.setReturnType( T );
		paramFunction.addParameter( T, 0, null, false );
		paramFunction.addParameter( U, 0, null, false );
		paramFunction.addParameter( U, 0, null, false );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f.addParameter( paramFunction, 0, null, false );
		
		factory.addSymbol( f );
		
		IParameterizedSymbol g1 = table.newParameterizedSymbol( "g1", TypeInfo.t_function );
		g1.setReturnType( table.newSymbol( "", TypeInfo.t_int ) );
		g1.addParameter( TypeInfo.t_int, 0, null, false );
		g1.addParameter( TypeInfo.t_float, 0, null, false );
		g1.addParameter( TypeInfo.t_float, 0, null, false );
		
		table.getCompilationUnit().addSymbol( g1 );
		
		IParameterizedSymbol g2 = table.newParameterizedSymbol( "g2", TypeInfo.t_function );
		g2.setReturnType( table.newSymbol( "", TypeInfo.t_char ) );
		g2.addParameter( TypeInfo.t_int, 0, null, false );
		g2.addParameter( TypeInfo.t_float, 0, null, false );
		g2.addParameter( TypeInfo.t_float, 0, null, false );
		
		table.getCompilationUnit().addSymbol( g2);
		
		IParameterizedSymbol g3 = table.newParameterizedSymbol( "g3", TypeInfo.t_function );
		g3.setReturnType( table.newSymbol( "", TypeInfo.t_int ) );
		g3.addParameter( TypeInfo.t_int, 0, null, false );
		g3.addParameter( TypeInfo.t_char, 0, null, false );
		g3.addParameter( TypeInfo.t_float, 0, null, false );
		
		table.getCompilationUnit().addSymbol( g3);
		
		TypeInfo arg = new TypeInfo( TypeInfo.t_type, 0, g1 );
		List argList = new ArrayList();
		argList.add( arg );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f );
		
		arg.setTypeSymbol( g2 );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList );
		assertEquals( look, null );
		
		arg.setTypeSymbol( g3 );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList );
		assertEquals( look, null );	
	}
	/**
	 * template< class T > void f( const T * ){}
	 * 
	 * int *p;
	 * 
	 * f( p );  //f ( const int * )
	 * 
	 * @throws Exception
	 */
	  public void test_14_8_2_4__7_ArgumentDeduction() throws Exception{
		newTable(); 
		
		ITemplateSymbol template = table.newTemplateSymbol( "f" );
		ISymbol T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f.addParameter( T, 0, new PtrOp( PtrOp.t_pointer, true, false ), false );
		factory.addSymbol( f );
		
		ISymbol p = table.newSymbol( "p", TypeInfo.t_int );
		p.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		
		List params = new ArrayList();
		params.add( new TypeInfo( TypeInfo.t_type, 0, p ) );
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params );
		
		assertTrue( look != null );
		assertTrue( look.isTemplateInstance() );
		
		assertEquals( look.getInstantiatedSymbol(), f );
		
		IParameterizedSymbol fn = (IParameterizedSymbol) look;
		Iterator iter = fn.getParameterList().iterator();
		ISymbol param = (ISymbol) iter.next();
		assertTrue( param.isType( TypeInfo.t_int ) );
		
		assertFalse( iter.hasNext() );
		
		iter = param.getTypeInfo().getPtrOperators().iterator();
		PtrOp op = (PtrOp) iter.next();
		assertTrue( op.isConst() );
		assertEquals( op.getType(), PtrOp.t_pointer );
		assertFalse( iter.hasNext() );
	}
	
	/**
	 * template< class T > struct B {};
	 * 
	 * template< class T > struct D : public B< T > {};
	 * 
	 * struct D2 : public B< int > {};
	 * 
	 * template< class T > void f( B<T> & ) {}
	 * 
	 * D<int> d;
	 * D2     d2;
	 * 
	 * f( d );   //f( B<int> & )
	 * f( d2 );  //f( B<int> & )
	 * @throws Exception
	 */
	  public void test_14_8_2_4__8_ArgumentDeduction() throws Exception{
		newTable();
		
		ITemplateSymbol templateB = table.newTemplateSymbol( "B" );
		templateB.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateB );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_struct );
		factory.addSymbol( B );
		
		ITemplateSymbol templateD = table.newTemplateSymbol( "D" );
		templateD.addTemplateParameter( table.newSymbol( "T", TypeInfo.t_templateParameter ) );

		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateD );
		
		IDerivableContainerSymbol D = table.newDerivableContainerSymbol( "D", TypeInfo.t_struct );
		factory.addSymbol( D );
		
		ISymbol T = templateD.lookup( "T" );
		List args = new ArrayList ();
		args.add( new TypeInfo( TypeInfo.t_type, 0, T ) );
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "B", args );
		assertTrue( look instanceof IDeferredTemplateInstance );
		assertEquals( ((IDeferredTemplateInstance)look).getTemplate(), templateB );
		
		D.addParent( look );
		
		IDerivableContainerSymbol D2 = table.newDerivableContainerSymbol( "D2", TypeInfo.t_struct );

		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		look = table.getCompilationUnit().lookupTemplateId( "B", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), B );
		
		D2.addParent( look );
		
		table.getCompilationUnit().addSymbol( D2 );
		
		ITemplateSymbol templatef = table.newTemplateSymbol( "f" );
		T = table.newSymbol( "T", TypeInfo.t_templateParameter );
		templatef.addTemplateParameter( T );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templatef );
		
		IParameterizedSymbol f  = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_type, 0, T ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "B", args );
		assertTrue( look instanceof IDeferredTemplateInstance );
		assertEquals( ((IDeferredTemplateInstance)look).getTemplate(), templateB );
		
		ISymbol param = table.newSymbol( "", TypeInfo.t_type );
		param.setTypeSymbol( look );
		param.addPtrOperator( new PtrOp( PtrOp.t_reference ) );
		f.addParameter( param );
		
		factory.addSymbol( f );
		
		ISymbol d = table.newSymbol( "d", TypeInfo.t_type );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		look = table.getCompilationUnit().lookupTemplateId( "D", args );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), D );
		
		d.setTypeSymbol( look );
		table.getCompilationUnit().addSymbol( d );
		
		ISymbol d2 = table.newSymbol( "d2", TypeInfo.t_type );
		d2.setTypeSymbol( D2 );
		table.getCompilationUnit().addSymbol( d2 );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_type, 0, d ) );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", args );
		assertTrue( look != null );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_type, 0, d2 ) );
		ISymbol look2 = table.getCompilationUnit().unqualifiedFunctionLookup( "f", args );
		assertTrue( look2 != null );
		assertTrue( look2.isTemplateInstance() );
		assertEquals( look2.getInstantiatedSymbol(), f );
		
		//both are the template function instantiated with int, should be the same instance.
		assertEquals( look, look2 );
	}
	
	

	  
	  /**
	   * template < class T1, class T2 > class A  { void f(); }; //#1
	   * 
	   * template < class T > class A < T, T >    { void f(); }; //#2
	   * template < class T > class A < char, T > { void f(); }; //#3
       *
	   * template < class U, class V > void A<U, V>::f(){
	   *    int c;
	   * }
	   * 
	   * template < class W > void A < W, W >::f(){
	   *    char c;
	   * }
	   * 
	   * template < class X > void A< char, X >::f(){
	   *    float c;
	   * }
	   * 
	   * A< int, char > a1;  //#1
	   * a1.f();  //#1
	   * 
	   * A< int, int >  a2;  //#2
	   * a2.f();  //#2
	   *
	   * A< char, int > a3;  //#3
	   * a3.f();  //#3
	   * 
	   * @throws Exception
	   */
	  public void testPartialSpecializationDefinitions() throws Exception{
	  	newTable();
	  	
	  	//template < class T1, class T2 > class A  { void f(); };
	  	ITemplateSymbol template = table.newTemplateSymbol( "A" );
	  	ISymbol T1 = table.newSymbol( "T1", TypeInfo.t_templateParameter );
	  	ISymbol T2 = table.newSymbol( "T2", TypeInfo.t_templateParameter );
	  	template.addTemplateParameter( T1 );
	  	template.addTemplateParameter( T2 );
	  	
	  	ITemplateFactory factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( template );
	  	
	  	IDerivableContainerSymbol A1 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
	  	factory.addSymbol( A1 );
	  	
	  	IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
	  	f1.setIsForwardDeclaration( true );
	  	A1.addSymbol( f1 );
	  	
	  	//template < class T > class A < T, T >    { void f(); };
	  	ITemplateSymbol spec1 = table.newTemplateSymbol("");
	  	ISymbol spec1_T = table.newSymbol( "T", TypeInfo.t_templateParameter );
	  	spec1.addTemplateParameter( spec1_T );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( spec1 );
	  	
	  	List args = new ArrayList();
	  	args.add( new TypeInfo( TypeInfo.t_type, 0, spec1_T ) );
	  	args.add( new TypeInfo( TypeInfo.t_type, 0, spec1_T ) );
	  	
	  	IDerivableContainerSymbol A2 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
	  	factory.addTemplateId( A2, args );
	  	
	  	IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
	  	f2.setIsForwardDeclaration( true );
	  	A2.addSymbol( f2 );

	  	//template < class T > class A < char, T > { void f(); };
	  	ITemplateSymbol spec2 = table.newTemplateSymbol("");
	  	ISymbol spec2_T = table.newSymbol( "T", TypeInfo.t_templateParameter );
	  	spec2.addTemplateParameter( spec2_T );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( spec2 );
	  	
	  	args.clear();
	  	args.add( new TypeInfo( TypeInfo.t_char, 0, null ) );
	  	args.add( new TypeInfo( TypeInfo.t_type, 0, spec2_T ) );
	  	
	  	IDerivableContainerSymbol A3 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
	  	factory.addTemplateId( A3, args );
	  	
	  	IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
	  	f3.setIsForwardDeclaration( true );
	  	A3.addSymbol( f3 );
	  	
	  	//template < class U, class V > void A<U, V>::f(){  int c; }
	  	ITemplateSymbol templateDef = table.newTemplateSymbol("");
	  	ISymbol U = table.newSymbol( "U", TypeInfo.t_templateParameter );
	  	ISymbol V = table.newSymbol( "V", TypeInfo.t_templateParameter );
	  	templateDef.addTemplateParameter( U );
	  	templateDef.addTemplateParameter( V );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( spec2 );
	  	
	  	args.clear();
	  	args.add( new TypeInfo( TypeInfo.t_type, 0, U ) );
	  	args.add( new TypeInfo( TypeInfo.t_type, 0, V ) );
	  	
	  	ISymbol symbol = factory.lookupTemplateId( "A",  args );
	  	assertEquals( ((IDeferredTemplateInstance)symbol).getTemplate(), template );
		factory.pushTemplateId( symbol, args );
	  	
	  	ISymbol look = factory.lookupMethodForDefinition( "f", new ArrayList() );
	  	assertEquals( look, f1 );
	  	IParameterizedSymbol f1Def = table.newParameterizedSymbol( "f", TypeInfo.t_function );
	  	f1.setTypeSymbol( f1Def );
	  	factory.addSymbol( f1Def );
	  	
	  	ISymbol c1 = table.newSymbol( "c", TypeInfo.t_int );
	  	f1Def.addSymbol( c1 );
	  	
	  	//template < class W > void A < W, W >::f(){  char c; }
	  	ITemplateSymbol specDef1 = table.newTemplateSymbol("");
	  	ISymbol W = table.newSymbol( "W", TypeInfo.t_templateParameter );
	  	specDef1.addTemplateParameter( W );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( specDef1 );
	  	
	  	args = new ArrayList();
	  	args.add( new TypeInfo( TypeInfo.t_type, 0, W ) );
	  	args.add( new TypeInfo( TypeInfo.t_type, 0, W ) );
	  	
	  	symbol = factory.lookupTemplateId( "A",  args );
		factory.pushTemplateId( symbol, args );
	  	
	  	look = factory.lookupMethodForDefinition( "f", new ArrayList() );
	  	assertEquals( look, f2 );
	  	IParameterizedSymbol f2Def = table.newParameterizedSymbol( "f", TypeInfo.t_function );
	  	f2.setTypeSymbol( f2Def );
	  	factory.addSymbol( f2Def );
	  	
	  	ISymbol c2 = table.newSymbol( "c", TypeInfo.t_char );
	  	f2Def.addSymbol( c2 );
	  	
	  	//template < class X > void < char, X >::f(){ float c; }
	  	ITemplateSymbol specDef2 = table.newTemplateSymbol("");
	  	ISymbol X = table.newSymbol( "X", TypeInfo.t_templateParameter );
	  	specDef2.addTemplateParameter( X );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( specDef1 );
	  	
	  	args = new ArrayList();
	  	args.add( new TypeInfo( TypeInfo.t_char, 0, null ) );
	  	args.add( new TypeInfo( TypeInfo.t_type, 0, X ) );
	  	
	  	symbol = factory.lookupTemplateId( "A",  args );
		factory.pushTemplateId( symbol, args );
		
		look = factory.lookupMethodForDefinition( "f", new ArrayList() );
	  	assertEquals( look, f3 );
	  	IParameterizedSymbol f3Def = table.newParameterizedSymbol( "f", TypeInfo.t_function );
	  	f3.setTypeSymbol( f3Def );
	  	factory.addSymbol( f3Def );
	  	
	  	ISymbol c3 = table.newSymbol( "c", TypeInfo.t_float );
	  	f3Def.addSymbol( c3 );
	  	
	  	//A< int, char > a1;
	  	args = new ArrayList();
	  	args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
	  	args.add( new TypeInfo( TypeInfo.t_char, 0, null ) );
	  
	  	look = table.getCompilationUnit().lookupTemplateId( "A", args );
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), A1 );
	  	
	  	look = ((IContainerSymbol)look).qualifiedFunctionLookup( "f", new ArrayList() );
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), f1Def );
	  	
	  	look = ((IContainerSymbol)look).qualifiedLookup( "c" );
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), c1 );
	  	assertTrue( look.isType( TypeInfo.t_int ) );
	
	  	//A< int, int > a2;
	  	args.clear();
	  	args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
	  	args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
	  	
	  	look = table.getCompilationUnit().lookupTemplateId( "A", args );
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), A2 );
	  	
	  	look = ((IContainerSymbol)look).qualifiedFunctionLookup( "f", new ArrayList() );
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), f2Def );
	  	
	  	look = ((IContainerSymbol)look).qualifiedLookup( "c" );
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), c2 );
	  	assertTrue( look.isType( TypeInfo.t_char ) );
	  	
	  	//A< char, int > a3;
	  	args.clear();
	  	args.add( new TypeInfo( TypeInfo.t_char, 0, null ) );
	  	args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
	  	
	  	look = table.getCompilationUnit().lookupTemplateId( "A", args );
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), A3 );
	  	
	  	look = ((IContainerSymbol)look).qualifiedFunctionLookup( "f", new ArrayList() );
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), f3Def );
	  	
	  	look = ((IContainerSymbol)look).qualifiedLookup( "c" );
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), c3 );
	  	assertTrue( look.isType( TypeInfo.t_float ) );
	  }
}