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
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfoProvider;

/**
 * @author aniefer
 */

public class ParserSymbolTableTemplateTests extends TestCase {

	public ParserSymbolTable table = null;
	public TypeInfoProvider provider = null;
	
	public ParserSymbolTableTemplateTests( String arg )
	{
		super( arg );
	}
	
	public ParserSymbolTable newTable(){
		return newTable( ParserLanguage.CPP );
	}
	
	public ParserSymbolTable newTable( ParserLanguage language ){
		table = new ParserSymbolTable( language, ParserMode.COMPLETE_PARSE );
		provider = TypeInfoProvider.getProvider( table );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
		ISymbol param = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( param );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( A );
		A.addParent( param );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		ISymbol i = table.newSymbol( "i", ITypeInfo.t_int ); //$NON-NLS-1$
		B.addSymbol( i );
		table.getCompilationUnit().addSymbol( B );
		
		ITypeInfo type = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, B );
		ArrayList args = new ArrayList();
		args.add( type );
		
		IContainerSymbol instance = (IContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		assertEquals( instance.getInstantiatedSymbol(), A );
		
		ISymbol a = table.newSymbol( "a", ITypeInfo.t_type ); //$NON-NLS-1$
		a.setTypeSymbol( instance );
		
		table.getCompilationUnit().addSymbol( a );
		
		ISymbol look = table.getCompilationUnit().lookup( "a" ); //$NON-NLS-1$
		
		assertEquals( look, a );
		
		ISymbol symbol = a.getTypeSymbol();
		assertEquals( symbol, instance );

		look = instance.lookup( "i" ); //$NON-NLS-1$
		assertEquals( look, i );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
			
		ISymbol param = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( param );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( A );
		
		ISymbol t = table.newSymbol( "t", ITypeInfo.t_type ); //$NON-NLS-1$
		ISymbol look = template.lookup( "T" ); //$NON-NLS-1$
		assertEquals( look, param );
		t.setTypeSymbol( param );
		A.addSymbol( t );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( B );
		
		ITypeInfo type = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0 , null );
		ArrayList args = new ArrayList();
		args.add( type );
		
		look = table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A );
		
		B.addParent( look );
		
		ISymbol b = table.newSymbol( "b", ITypeInfo.t_type ); //$NON-NLS-1$
		b.setTypeSymbol( B );
		table.getCompilationUnit().addSymbol( b );
		
		look = table.getCompilationUnit().lookup( "b" ); //$NON-NLS-1$
		assertEquals( look, b );
		
		look = ((IDerivableContainerSymbol) b.getTypeSymbol()).lookup( "t" ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), t );
		assertTrue( look.isType( ITypeInfo.t_int ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" ); //$NON-NLS-1$
		
		ISymbol paramT = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( paramT );
		
		ISymbol look = template.lookup( "T" ); //$NON-NLS-1$
		assertEquals( look, paramT );
		ISymbol paramU = table.newSymbol( "U" ); //$NON-NLS-1$
		paramU.setTypeInfo( TypeInfoProvider.newTypeInfo( ITypeInfo.t_templateParameter, 0, null, null, 
		        										  TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, look ) ) );
		template.addTemplateParameter( paramU );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( X );
		
		look = X.lookup( "T" ); //$NON-NLS-1$
		assertEquals( look, paramT );
		ISymbol t = table.newSymbol( "t", ITypeInfo.t_type ); //$NON-NLS-1$
		t.setTypeSymbol( look );
		X.addSymbol( t );
		
		look = X.lookup( "U" ); //$NON-NLS-1$
		assertEquals( look, paramU );
		ISymbol u = table.newSymbol( "u", ITypeInfo.t_type ); //$NON-NLS-1$
		u.setTypeSymbol( look );
		X.addSymbol( u );
		
		ITypeInfo type = TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null );
		ArrayList args = new ArrayList();
		args.add( type );
		IDerivableContainerSymbol lookX = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "X", args ); //$NON-NLS-1$
		assertTrue( lookX.isTemplateInstance() );
		assertEquals( lookX.getInstantiatedSymbol(), X );
				
		
		look = lookX.lookup( "t" ); //$NON-NLS-1$
		
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), t );
		assertTrue( look.isType( ITypeInfo.t_char ) );
		
		look = lookX.lookup( "u" ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), u );
		assertTrue( look.isType( ITypeInfo.t_char ) );	
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
		ISymbol paramT = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( paramT );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( A );
		
		ISymbol t = table.newSymbol( "t", ITypeInfo.t_type ); //$NON-NLS-1$
		t.setTypeSymbol( paramT );
		A.addSymbol( t );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( B );
		
		ArrayList args = new ArrayList();
		ITypeInfo arg = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, B );
		args.add( arg );
		
		IDerivableContainerSymbol lookA = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		assertTrue( lookA.isTemplateInstance() );
		assertEquals( lookA.getInstantiatedSymbol(), A );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.addParameter( ITypeInfo.t_char, 0, null, false );
		table.getCompilationUnit().addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.addParameter( lookA, 0, null, false );
		table.getCompilationUnit().addSymbol( f2 );
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f3.addParameter( ITypeInfo.t_int, 0, null, false );
		table.getCompilationUnit().addSymbol( f3 );

		args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, B ) );
		IDerivableContainerSymbol lookA2 = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		assertEquals( lookA2, lookA );
		
		
		ISymbol a = table.newSymbol( "a", ITypeInfo.t_type ); //$NON-NLS-1$
		a.setTypeSymbol( lookA );
		table.getCompilationUnit().addSymbol( a );

		ArrayList params = new ArrayList();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a ) );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertEquals( look, f2 );		
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IDerivableContainerSymbol T = table.newDerivableContainerSymbol( "T", ITypeInfo.t_class ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( T );
		
		ISymbol i = table.newSymbol( "i", ITypeInfo.t_int ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( i );
		
		ITemplateSymbol template = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
				
		ISymbol paramT = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( paramT );

		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		ISymbol parami = table.newSymbol( "i", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		parami.getTypeInfo().setTemplateParameterType( ITypeInfo.t_type );
		
		ISymbol look = factory.lookup( "T" ); //$NON-NLS-1$

		assertEquals( look, paramT );
		parami.setTypeSymbol( look );
		template.addTemplateParameter( parami );

		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		ISymbol fParam = table.newSymbol( "t", ITypeInfo.t_type ); //$NON-NLS-1$
		fParam.setTypeSymbol( paramT );
		f.addParameter( fParam );
		
		factory.addSymbol( f );
		
		look = f.lookup( "T" ); //$NON-NLS-1$
		assertEquals( look, paramT );
		
		look = f.lookup( "i" ); //$NON-NLS-1$
		assertEquals( look, parami );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol templateR = table.newTemplateSymbol( "R" ); //$NON-NLS-1$
		
		ISymbol paramA = table.newSymbol( "a", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		paramA.getTypeInfo().setTemplateParameterType( ITypeInfo.t_int );
		paramA.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		templateR.addTemplateParameter( paramA );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateR );
		
		IDerivableContainerSymbol R = table.newDerivableContainerSymbol( "R", ITypeInfo.t_struct ); //$NON-NLS-1$
		factory.addSymbol( R );
				
		ITemplateSymbol templateS = table.newTemplateSymbol( "S" ); //$NON-NLS-1$
				
		ISymbol paramB = table.newSymbol( "b", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		paramB.getTypeInfo().setTemplateParameterType( ITypeInfo.t_int );
		paramB.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_array ) );
		templateS.addTemplateParameter( paramB );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateS ); 
		
		IDerivableContainerSymbol S = table.newDerivableContainerSymbol( "S", ITypeInfo.t_struct ); //$NON-NLS-1$
		factory.addSymbol( S );
				
		ISymbol look = table.getCompilationUnit().lookup( "S" ); //$NON-NLS-1$
		assertEquals( look, templateS );
		
		Iterator iter = templateS.getParameterList().iterator();
		ISymbol param = (ISymbol) iter.next();
		assertFalse( iter.hasNext() );
		iter = param.getTypeInfo().getPtrOperators().iterator();
		ITypeInfo.PtrOp ptr = (ITypeInfo.PtrOp) iter.next();
		assertFalse( iter.hasNext() );
		assertEquals( ptr.getType(), ITypeInfo.PtrOp.t_pointer );
		
		ISymbol p = table.newSymbol( "p", ITypeInfo.t_int ); //$NON-NLS-1$
		p.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		table.getCompilationUnit().addSymbol( p );
		
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, p ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "R", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), R );
		
		look = table.getCompilationUnit().lookupTemplateId( "S", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), S );
		
		ISymbol v = table.newSymbol( "v", ITypeInfo.t_int ); //$NON-NLS-1$
		v.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_array ) );
		table.getCompilationUnit().addSymbol( v );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, v ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "R", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), R );

		look = table.getCompilationUnit().lookupTemplateId( "S", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), S );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "String" ); //$NON-NLS-1$
		
		ISymbol param = table.newSymbol( "T" ); //$NON-NLS-1$
		param.setTypeInfo( TypeInfoProvider.newTypeInfo( ITypeInfo.t_templateParameter, 0, null, null, 
		        										 TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null ) ));
		template.addTemplateParameter( param );

		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol string = table.newDerivableContainerSymbol( "String", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( string );
		
		List args = new ArrayList();
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "String", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), string );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" ); //$NON-NLS-1$
		template.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		factory.addSymbol( table.newDerivableContainerSymbol( "X", ITypeInfo.t_class ) ); //$NON-NLS-1$
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( f );
		
		IDerivableContainerSymbol S = table.newDerivableContainerSymbol( "S", ITypeInfo.t_struct ); //$NON-NLS-1$
		f.addSymbol( S );
		
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, S ) );
		try{
			f.lookupTemplateId( "X", args ); //$NON-NLS-1$
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, S, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		try{
			f.lookupTemplateId( "X", args ); //$NON-NLS-1$
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" ); //$NON-NLS-1$
		template.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ISymbol param2 = table.newSymbol( "p", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		param2.getTypeInfo().setTemplateParameterType( ITypeInfo.t_char );
		param2.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		template.addTemplateParameter( param2 );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( X );
		
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		provider.beginTypeConstruction();
		provider.setType( ITypeInfo.t_char );
		provider.setDefaultObj( "Studebaker" ); //$NON-NLS-1$
		ITypeInfo info = provider.completeConstruction();
		info.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		args.add( info ); //$NON-NLS-1$
		
		try{
			table.getCompilationUnit().lookupTemplateId( "X", args ); //$NON-NLS-1$
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		ISymbol p = table.newSymbol( "p", ITypeInfo.t_char ); //$NON-NLS-1$
		p.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_array ) );
		table.getCompilationUnit().addSymbol( p );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, p ) );
		
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "X", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), X );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" ); //$NON-NLS-1$
		
		ISymbol param = table.newSymbol( "p", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		param.getTypeInfo().setTemplateParameterType( ITypeInfo.t_int );
		param.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		template.addTemplateParameter( param );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( X );
		
		IDerivableContainerSymbol S = table.newDerivableContainerSymbol( "S", ITypeInfo.t_struct ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( S );
		
		ISymbol m = table.newSymbol( "m", ITypeInfo.t_int ); //$NON-NLS-1$
		S.addSymbol( m );
		ISymbol s = table.newSymbol( "s", ITypeInfo.t_int ); //$NON-NLS-1$
		s.getTypeInfo().setBit( true, ITypeInfo.isStatic );
		S.addSymbol( s );
		ISymbol t = table.newSymbol( "t", ITypeInfo.t_int ); //$NON-NLS-1$
		t.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		S.addSymbol( t );
				
		List args = new ArrayList();
		ITypeInfo arg =  TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, m );
		arg.applyOperatorExpression( ITypeInfo.OperatorExpression.addressof );
		args.add( arg );
		
		try
		{
			table.getCompilationUnit().lookupTemplateId( "X", args ); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, t ) );

		try
		{
			table.getCompilationUnit().lookupTemplateId( "X", args ); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		args.clear();
		arg =  TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, s );
		arg.applyOperatorExpression( ITypeInfo.OperatorExpression.addressof );
		args.add( arg );
		
		assertNotNull( table.getCompilationUnit().lookupTemplateId( "X", args ) ); //$NON-NLS-1$
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "B" ); //$NON-NLS-1$
		
		ISymbol I = table.newSymbol( "I", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		I.getTypeInfo().setTemplateParameterType( ITypeInfo.t_int );
		I.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference, true, false ) );
		template.addTemplateParameter( I );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_struct ); //$NON-NLS-1$
		factory.addSymbol( B );
		
		List args = new ArrayList( );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, null, "1" ) ); //$NON-NLS-1$
		
		try{
			table.getCompilationUnit().lookupTemplateId( "B", args ); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateArgument );
		}
		
		ISymbol c = table.newSymbol( "c", ITypeInfo.t_int ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( c );
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, c ) );
		
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "B", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), B );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol templateA = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( templateA );
		
		templateA.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		IDerivableContainerSymbol A1 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		templateA.addSymbol( A1 );
		
		ISymbol x1 = table.newSymbol( "x", ITypeInfo.t_int ); //$NON-NLS-1$
		A1.addSymbol( x1 );
		
		ISpecializedSymbol specialization = table.newSpecializedSymbol( "A" ); //$NON-NLS-1$
		templateA.addSpecialization( specialization );
		
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		specialization.addTemplateParameter( T );
		specialization.addArgument( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		
		IDerivableContainerSymbol A2 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		specialization.addSymbol( A2 );
		
		ISymbol x2 = table.newSymbol( "x", ITypeInfo.t_int ); //$NON-NLS-1$
		x2.getTypeInfo().setBit( true, ITypeInfo.isLong );
		A2.addSymbol( x2 );
		
		ITemplateSymbol templateC = table.newTemplateSymbol( "C" ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( templateC );
		
		ITemplateSymbol templateV = table.newTemplateSymbol( "V" ); //$NON-NLS-1$
		templateV.setTypeInfo( TypeInfoProvider.newTypeInfo( ITypeInfo.t_templateParameter ) );
		templateV.getTypeInfo().setTemplateParameterType( ITypeInfo.t_template );
		ISymbol U = table.newSymbol( "U", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		templateV.addTemplateParameter( U );
		
		templateC.addTemplateParameter( templateV );
		
		IDerivableContainerSymbol C	= table.newDerivableContainerSymbol( "C", ITypeInfo.t_class ); //$NON-NLS-1$
		templateC.addSymbol( C );
		
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		
		ISymbol look = templateC.lookupTemplateId( "V", args ); //$NON-NLS-1$
		assertTrue( look != null );
		assertTrue( look instanceof IDeferredTemplateInstance );
		
		ISymbol y = table.newSymbol( "y", ITypeInfo.t_type ); //$NON-NLS-1$
		y.setTypeSymbol( look );
		
		C.addSymbol( y );

		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		
		look = templateC.lookupTemplateId( "V", args ); //$NON-NLS-1$
		assertTrue( look != null );
		assertTrue( look instanceof IDeferredTemplateInstance );
		
		ISymbol z = table.newSymbol( "z", ITypeInfo.t_type ); //$NON-NLS-1$
		z.setTypeSymbol( look );
		C.addSymbol( z );
		
		look = table.getCompilationUnit().lookup( "A" ); //$NON-NLS-1$
		assertEquals( look, templateA );
		
		args.clear();
		args.add ( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, look ) );
		look = table.getCompilationUnit().lookupTemplateId( "C", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), C );
		
		IDerivableContainerSymbol lookC = (IDerivableContainerSymbol)look;
		look = lookC.lookup( "y" ); //$NON-NLS-1$
		
		assertEquals( look.getType(), ITypeInfo.t_type );
		ISymbol symbol = look.getTypeSymbol();
		assertTrue( symbol instanceof IContainerSymbol );
		assertTrue( symbol.isTemplateInstance() );
		assertEquals( symbol.getInstantiatedSymbol(), A1 );
		
		look = ((IContainerSymbol) symbol).lookup( "x" ); //$NON-NLS-1$
		
		assertEquals( look.getType(), ITypeInfo.t_int );
		assertEquals( look.getTypeInfo().checkBit( ITypeInfo.isLong ), false );
		
		look = lookC.lookup( "z" ); //$NON-NLS-1$
		assertEquals( look.getType(), ITypeInfo.t_type );
		symbol = look.getTypeSymbol();
		assertTrue( symbol instanceof IContainerSymbol );
		assertTrue( symbol.isTemplateInstance() );
		assertEquals( symbol.getInstantiatedSymbol(), A2 );
		look = ((IContainerSymbol)symbol).lookup( "x" ); //$NON-NLS-1$
		
		assertEquals( look.getType(), ITypeInfo.t_int );
		assertEquals( look.getTypeInfo().checkBit( ITypeInfo.isLong ), true );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		ISymbol primaryT1 = table.newSymbol( "T1", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol primaryT2 = table.newSymbol( "T2", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( primaryT1 );		
		template.addTemplateParameter( primaryT2 );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		
		factory.addSymbol( A );	
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f1", ITypeInfo.t_function ); //$NON-NLS-1$
		
		ISymbol look = A.lookup( "T1" ); //$NON-NLS-1$
		assertEquals( look, primaryT1 );
		
		f1.setIsForwardDeclaration( true );
		f1.setReturnType( look );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f2", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.setIsForwardDeclaration( true );
		
		A.addSymbol( f1 );
		A.addSymbol( f2 );
		
		ITemplateSymbol temp = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		ISymbol U = table.newSymbol( "U", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol V = table.newSymbol( "V", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		temp.addTemplateParameter( U );
		temp.addTemplateParameter( V );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		ISymbol returnType = factory.lookup( "U" ); //$NON-NLS-1$
		assertEquals( returnType, U );
		
		List args  = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, U ) ); 
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, V ) );
		
		look = factory.lookupTemplateIdForDefinition( "A", args ); //$NON-NLS-1$
		assertEquals( look, A );
		factory.pushTemplateId( look, args );
		
		IParameterizedSymbol lookF = factory.lookupMethodForDefinition( "f1", new ArrayList() ); //$NON-NLS-1$
		assertEquals( lookF, f1 );
		assertTrue( lookF.isForwardDeclaration() );
		
		IParameterizedSymbol defnd = table.newParameterizedSymbol( "f1", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.setForwardSymbol( defnd );
		defnd.setReturnType( returnType );
		factory.addSymbol( defnd );
		
		//Test that the adding was all good by doing a lookup
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null ) );
		
		IDerivableContainerSymbol lookA = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		assertTrue( lookA.isTemplateInstance() );
		assertEquals( lookA.getInstantiatedSymbol(), A );
		
		List params = new ArrayList();
		look = lookA.qualifiedFunctionLookup( "f1", params ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), defnd );
		assertTrue( ((IParameterizedSymbol)look).getReturnType().isType( ITypeInfo.t_int ) );
		
		params.clear();
		args.clear();
		
		temp = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		ISymbol X = table.newSymbol( "X", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol Y = table.newSymbol( "Y", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		temp.addTemplateParameter( X );
		temp.addTemplateParameter( Y );

		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, Y ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, X ) ); 

		try{
			look = factory.lookupTemplateIdForDefinition( "A", args ); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplate );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
		ISymbol primaryT = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( primaryT );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_struct ); //$NON-NLS-1$
		factory.addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		B.setIsForwardDeclaration( true );
		A.addSymbol( B );
		
		ISymbol U = table.newSymbol( "U", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ITemplateSymbol temp = table.newTemplateSymbol( "" ); //$NON-NLS-1$
		temp.addTemplateParameter( U );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
				
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, U ) );
		
		IContainerSymbol lookA = factory.lookupTemplateIdForDefinition( "A", args ); //$NON-NLS-1$
		assertEquals( lookA, A );
		factory.pushTemplateId( lookA, args );
		
		ISymbol look = lookA.lookupMemberForDefinition( "B" ); //$NON-NLS-1$
		assertEquals( look, B );
		
		IDerivableContainerSymbol newB = table.newDerivableContainerSymbol( "B", ITypeInfo.t_class ); //$NON-NLS-1$
		look.setForwardSymbol( newB );
		
		factory.addSymbol( newB );
		
		ISymbol i = table.newSymbol( "i", ITypeInfo.t_type ); //$NON-NLS-1$
		look = newB.lookup( "U" ); //$NON-NLS-1$
		assertEquals( look, U );
		i.setTypeSymbol( U );
		newB.addSymbol( i );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A );
		
		assertTrue( look instanceof IDerivableContainerSymbol );
		lookA = (IDerivableContainerSymbol) look;
		look = lookA.qualifiedLookup( "B" ); //$NON-NLS-1$
		
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), newB );
		
		look = ((IDerivableContainerSymbol) look).lookup( "i" ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), i );
		assertEquals( look.getType(), ITypeInfo.t_int );	
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" ); //$NON-NLS-1$
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( X );
		
		ISymbol look = X.lookup( "T" ); //$NON-NLS-1$
		assertEquals( look, T );
		
		ISymbol s = table.newSymbol( "s", ITypeInfo.t_type ); //$NON-NLS-1$
		s.setTypeSymbol( look );
		s.getTypeInfo().setBit( true, ITypeInfo.isStatic );
		s.setIsForwardDeclaration( true );
		X.addSymbol( s );
		
		ITemplateSymbol temp = table.newTemplateSymbol( "" ); //$NON-NLS-1$
		ISymbol paramU = table.newSymbol( "U", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		temp.addTemplateParameter( paramU );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, paramU ) );
		
		look = factory.lookupTemplateIdForDefinition( "X", args ); //$NON-NLS-1$
		assertEquals( look, X );
		factory.pushTemplateId( X, args );
		
		look = ((IContainerSymbol)look).lookupMemberForDefinition("s" ); //$NON-NLS-1$
		assertEquals( look, s );
		assertTrue( look.isForwardDeclaration() );
		
		ISymbol newS = table.newSymbol( "s", ITypeInfo.t_type ); //$NON-NLS-1$
		newS.setTypeSymbol( paramU );
		
		look.setForwardSymbol( newS );
		
		factory.addSymbol( newS );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_float, 0, null ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "X", args ); //$NON-NLS-1$
		
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), X );
		
		look = ((IContainerSymbol)look).qualifiedLookup( "s" ); //$NON-NLS-1$
		
		assertTrue( look.isType( ITypeInfo.t_float ) );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), newS );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "string" ); //$NON-NLS-1$
		template1.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template1 );
		
		IDerivableContainerSymbol string = table.newDerivableContainerSymbol( "string", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( string );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "compare" ); //$NON-NLS-1$
		ISymbol T2 = table.newSymbol( "T2", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template2.addTemplateParameter( T2 );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( string );
		factory.pushTemplate( template2 );
				
		IParameterizedSymbol compare = table.newParameterizedSymbol( "compare", ITypeInfo.t_function ); //$NON-NLS-1$
		compare.setIsForwardDeclaration( true );
		compare.addParameter( T2, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference, true, false ), false );
		ISymbol returnType = table.newSymbol( "", ITypeInfo.t_type ); //$NON-NLS-1$
		returnType.setTypeSymbol( T2 );
		compare.setReturnType( returnType );
		factory.addSymbol( compare );
		
		ITemplateSymbol temp = table.newTemplateSymbol( "" ); //$NON-NLS-1$
		ISymbol U = table.newSymbol( "U", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		temp.addTemplateParameter( U );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		ITemplateSymbol temp2 = table.newTemplateSymbol( "" ); //$NON-NLS-1$
		ISymbol V = table.newSymbol( "V", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		temp2.addTemplateParameter( V );
		
		factory.pushTemplate( temp2 );

		ISymbol lookV = factory.lookup( "V" ); //$NON-NLS-1$
		assertEquals( lookV, V );
		
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, U ) );
		
		ISymbol look = factory.lookupTemplateIdForDefinition( "string", args ); //$NON-NLS-1$
		assertEquals( look, string );
		factory.pushTemplateId( look, args );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, lookV,  new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference, true, false ), false ) );

		look = ((IContainerSymbol)look).lookupMethodForDefinition( "compare", args ); //$NON-NLS-1$
		assertEquals( look, compare );
		
		IParameterizedSymbol compareDef = table.newParameterizedSymbol( "compare", ITypeInfo.t_function ); //$NON-NLS-1$
		compareDef.addParameter( lookV, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference, true, false ), false );
		ISymbol defReturn = table.newSymbol( "", ITypeInfo.t_type ); //$NON-NLS-1$
		defReturn.setTypeSymbol( lookV );
		compareDef.setReturnType( defReturn );
		compare.setForwardSymbol( compareDef );
		factory.addSymbol( compareDef );
		
		look = compareDef.lookup( "U" ); //$NON-NLS-1$
		assertEquals( look, U );
		
		ISymbol u = table.newSymbol( "u", ITypeInfo.t_type ); //$NON-NLS-1$
		u.setTypeSymbol( look );
		
		compareDef.addSymbol( u );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "string", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), string );
		
		look = ((IDerivableContainerSymbol)look).lookupTemplateId( "compare", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertTrue( look.getInstantiatedSymbol().isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol().getInstantiatedSymbol(), compareDef );
		
		assertTrue( ((IParameterizedSymbol)look).getReturnType().isType( ITypeInfo.t_int ) );
		
		look = ((IContainerSymbol)look).lookup( "u" ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertTrue( look.getInstantiatedSymbol().isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol().getInstantiatedSymbol(), u );
		assertTrue( look.isType( ITypeInfo.t_int ) );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
		template.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_struct ); //$NON-NLS-1$
		factory.addSymbol( A );
		
		ITemplateSymbol memberTemplate = table.newTemplateSymbol( "g" ); //$NON-NLS-1$
		ISymbol C = table.newSymbol( "C", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		memberTemplate.addTemplateParameter( C );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( A );
		factory.pushTemplate( memberTemplate );
		
		IParameterizedSymbol g = table.newParameterizedSymbol( "g", ITypeInfo.t_function ); //$NON-NLS-1$
		g.addParameter( C, 0, null, false );
		g.getTypeInfo().setBit( true, ITypeInfo.isVirtual );
		
		try{
			factory.addSymbol( memberTemplate );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplate );
		}
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f.getTypeInfo().setBit( true, ITypeInfo.isVirtual );
		
		A.addSymbol( f );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IContainerSymbol N = table.newContainerSymbol( "N", ITypeInfo.t_namespace ); //$NON-NLS-1$
		
		table.getCompilationUnit().addSymbol( N );
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
		
		template.addTemplateParameter( table.newSymbol( "T1", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		template.addTemplateParameter( table.newSymbol( "T2", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		IDerivableContainerSymbol A1 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		
		template.addSymbol( A1 );
		
		N.addSymbol( template );
		
		table.getCompilationUnit().addUsingDeclaration( "A", N ); //$NON-NLS-1$
		
		ISpecializedSymbol spec = table.newSpecializedSymbol( "A" ); //$NON-NLS-1$
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		spec.addTemplateParameter( T );
		spec.addArgument( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T ) );
		spec.addArgument( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		
		IDerivableContainerSymbol A2 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		spec.addSymbol( A2 );
		template.addSpecialization( spec );
		
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "A", args );  //$NON-NLS-1$
		assertTrue( look != null );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A2 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		IDerivableContainerSymbol cls1 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		IDerivableContainerSymbol cls2 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		IDerivableContainerSymbol cls3 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		IDerivableContainerSymbol cls4 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		IDerivableContainerSymbol cls5 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
		ISymbol T1p1 = table.newSymbol( "T1", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol T1p2 = table.newSymbol( "T2", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol T1p3 = table.newSymbol( "I", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		T1p3.getTypeInfo().setTemplateParameterType( ITypeInfo.t_int );
		
		template1.addTemplateParameter( T1p1 );
		template1.addTemplateParameter( T1p2 );
		template1.addTemplateParameter( T1p3 );
		template1.addSymbol( cls1 );
		table.getCompilationUnit().addSymbol( template1 );
		
		ISpecializedSymbol template2 = table.newSpecializedSymbol( "A" ); //$NON-NLS-1$
		ISymbol T2p1 = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol T2p2 = table.newSymbol( "I", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		T2p2.getTypeInfo().setTemplateParameterType( ITypeInfo.t_int );
		
		template2.addTemplateParameter( T2p1 );
		template2.addTemplateParameter( T2p2 );
		
		ITypeInfo T2a1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T2p1 );
		ITypeInfo T2a2 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T2p1, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		ITypeInfo T2a3 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T2p2 );
		
		template2.addArgument( T2a1 );
		template2.addArgument( T2a2 );
		template2.addArgument( T2a3 );
		template2.addSymbol( cls2 );
		template1.addSpecialization( template2 );
		
		ISpecializedSymbol template3 = table.newSpecializedSymbol( "A" ); //$NON-NLS-1$
		ISymbol T3p1 = table.newSymbol( "T1", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol T3p2 = table.newSymbol( "T2", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol T3p3 = table.newSymbol( "I", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		T3p3.getTypeInfo().setTemplateParameterType( ITypeInfo.t_int );
		
		template3.addTemplateParameter( T3p1 );
		template3.addTemplateParameter( T3p2 );
		template3.addTemplateParameter( T3p3 );
		
		ITypeInfo T3a1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T3p1, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		ITypeInfo T3a2 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T3p2 );
		ITypeInfo T3a3 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T3p3 );
		
		template3.addArgument( T3a1 );
		template3.addArgument( T3a2 );
		template3.addArgument( T3a3 );
		template3.addSymbol( cls3 );
		template1.addSpecialization( template3 );
		
		ISpecializedSymbol template4 = table.newSpecializedSymbol( "A" ); //$NON-NLS-1$
		ISymbol T4p1 = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template4.addTemplateParameter( T4p1 );
		
		ITypeInfo T4a1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null );
		ITypeInfo T4a2 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T4p1, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		ITypeInfo T4a3 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, null, "5" ); //$NON-NLS-1$
		
		template4.addArgument( T4a1 );
		template4.addArgument( T4a2 );
		template4.addArgument( T4a3 );
		template4.addSymbol( cls4 );
		template1.addSpecialization( template4 );
		
		ISpecializedSymbol template5 = table.newSpecializedSymbol( "A" ); //$NON-NLS-1$
		ISymbol T5p1 = table.newSymbol( "T1", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol T5p2 = table.newSymbol( "T2", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		ISymbol T5p3 = table.newSymbol( "I", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		T5p3.getTypeInfo().setTemplateParameterType( ITypeInfo.t_int );
		
		template5.addTemplateParameter( T5p1 );
		template5.addTemplateParameter( T5p2 );
		template5.addTemplateParameter( T5p3 );
		
		ITypeInfo T5a1 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T5p1 );
		ITypeInfo T5a2 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T5p2, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		ITypeInfo T5a3 = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T5p3 );
		
		template5.addArgument( T5a1 );
		template5.addArgument( T5a2 );
		template5.addArgument( T5a3 );
		template5.addSymbol( cls5 );
		template1.addSpecialization( template5 );
		
		ITemplateSymbol a = (ITemplateSymbol) table.getCompilationUnit().lookup( "A" ); //$NON-NLS-1$
		assertEquals( a, template1 );
		
		ArrayList args = new ArrayList();
		
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, null, "1" ) ); //$NON-NLS-1$
		
		IContainerSymbol a1 = (IContainerSymbol) a.instantiate( args );
		assertTrue( a1.isTemplateInstance() );
		assertEquals( a1.getInstantiatedSymbol(), cls1 );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, null, "1" ) ); //$NON-NLS-1$
		
		IContainerSymbol a2 = (IContainerSymbol) a.instantiate( args );
		assertTrue( a2.isTemplateInstance() );
		assertEquals( a2.getInstantiatedSymbol(), cls2 );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, null, "5" ) ); //$NON-NLS-1$
		IContainerSymbol a3 = (IContainerSymbol) a.instantiate( args );
		assertTrue( a3.isTemplateInstance() );
		assertEquals( a3.getInstantiatedSymbol(), cls4 );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, null, "1" ) ); //$NON-NLS-1$
		IContainerSymbol a4 = (IContainerSymbol) a.instantiate( args );
		assertTrue( a4.isTemplateInstance() );
		assertEquals( a4.getInstantiatedSymbol(), cls5 );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, null, "2" ) ); //$NON-NLS-1$
		
		try{
			a.instantiate( args );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
		template1.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template1 );
		
		ISymbol T = template1.lookup( "T" ); //$NON-NLS-1$
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.addParameter( T, 0, null, false );
		factory.addSymbol( f1 );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
		template2.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template2 );
		
		T = template2.lookup( "T" ); //$NON-NLS-1$
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.addParameter( T, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		factory.addSymbol( f2 );
		
		ITemplateSymbol template3 = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
		template3.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template3 );
		
		T = template3.lookup( "T" ); //$NON-NLS-1$
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f3.addParameter( T, ITypeInfo.isConst, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, false ), false );
		factory.addSymbol( f3 );
		
		ISymbol p = table.newSymbol( "p", ITypeInfo.t_int ); //$NON-NLS-1$
		p.getTypeInfo().setBit( true, ITypeInfo.isConst );
		p.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, false ) );
		table.getCompilationUnit().addSymbol( p );
		
		List params = new ArrayList();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, p ) );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f3 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "g" ); //$NON-NLS-1$
		template1.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template1 );
		
		ISymbol T = template1.lookup( "T" ); //$NON-NLS-1$
		IParameterizedSymbol g1 = table.newParameterizedSymbol( "g", ITypeInfo.t_function ); //$NON-NLS-1$
		g1.addParameter( T, 0, null, false );
		factory.addSymbol( g1 );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "g" ); //$NON-NLS-1$
		template2.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template2 );
		
		T = template2.lookup( "T" ); //$NON-NLS-1$
		IParameterizedSymbol g2 = table.newParameterizedSymbol( "g", ITypeInfo.t_function ); //$NON-NLS-1$
		g2.addParameter( T, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ), false );
		factory.addSymbol( g2 );
		
		ISymbol x = table.newSymbol( "x", ITypeInfo.t_float ); //$NON-NLS-1$
		List params = new ArrayList();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, x ) );
		try{
			table.getCompilationUnit().unqualifiedFunctionLookup( "g", params ); //$NON-NLS-1$
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol templateA = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
		templateA.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateA );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_struct ); //$NON-NLS-1$
		factory.addSymbol( A );
				
		ITemplateSymbol template1 = table.newTemplateSymbol( "h" ); //$NON-NLS-1$
		template1.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template1 );
		
		ISymbol T = template1.lookup( "T" ); //$NON-NLS-1$
		IParameterizedSymbol h1 = table.newParameterizedSymbol( "h", ITypeInfo.t_function ); //$NON-NLS-1$
		h1.addParameter( T, ITypeInfo.isConst, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference, false, false ),false );
		factory.addSymbol( h1 );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "h" ); //$NON-NLS-1$
		template2.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template2 );
		
		T = template2.lookup( "T" ); //$NON-NLS-1$
		
		IParameterizedSymbol h2 = table.newParameterizedSymbol( "h", ITypeInfo.t_function ); //$NON-NLS-1$
		List argList = new ArrayList();
		argList.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T ) );
		h2.addParameter( templateA.instantiate( argList ), 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ), false );
		factory.addSymbol( h2 );
		
		ISymbol z = table.newSymbol( "z", ITypeInfo.t_type ); //$NON-NLS-1$
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A );
		z.setTypeSymbol( look );
		
		List params = new ArrayList();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, z ) );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "h", params ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), h2 );
		
		ISymbol z2 = table.newSymbol( "z2", ITypeInfo.t_type ); //$NON-NLS-1$
		look = table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), A );
		z2.setTypeSymbol( look );
		z2.getTypeInfo().setBit( true, ITypeInfo.isConst );
		
		params.clear();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, z2 ) );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "h", params ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), h1 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "X" ); //$NON-NLS-1$
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( X );
		
		ISymbol look = X.lookup( "X" ); //$NON-NLS-1$
		
		assertTrue( look != null );
		assertTrue( look instanceof IDeferredTemplateInstance );
		IDeferredTemplateInstance deferred = (IDeferredTemplateInstance) look;
		assertEquals( deferred.getTemplate(), template );
		
		Iterator iter = deferred.getArguments().iterator();
		ITypeInfo type = (ITypeInfo) iter.next();
		assertTrue( type.isType( ITypeInfo.t_type ) );
		assertEquals( type.getTypeSymbol(), T );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "Y" ); //$NON-NLS-1$
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( T );
		IDerivableContainerSymbol Y1 = table.newDerivableContainerSymbol( "Y", ITypeInfo.t_class ); //$NON-NLS-1$
		template.addSymbol( Y1 );
		
		table.getCompilationUnit().addSymbol( template );
		
		ISpecializedSymbol spec = table.newSpecializedSymbol( "Y" ); //$NON-NLS-1$
		spec.addArgument( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		
		template.addSpecialization( spec );
		
		IDerivableContainerSymbol Y2 = table.newDerivableContainerSymbol( "Y", ITypeInfo.t_class ); //$NON-NLS-1$
		spec.addSymbol( Y2 );
		
		ISymbol look = Y2.lookup( "Y" ); //$NON-NLS-1$
		assertTrue( look != null );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), Y2 );	
		
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "Y" ); //$NON-NLS-1$
		template.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ISymbol i = table.newSymbol( "i", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		i.getTypeInfo().setTemplateParameterType( ITypeInfo.t_int );
		template.addTemplateParameter( i );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol Y = table.newDerivableContainerSymbol( "Y", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( Y );
		
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_int ); //$NON-NLS-1$
		try{
			Y.addSymbol( T );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_RedeclaredTemplateParam );
		}
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		Y.addSymbol( f );
		
		try{
			f.addSymbol( table.newSymbol( "T", ITypeInfo.t_char ) ); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_RedeclaredTemplateParam );
		}
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "X" ); //$NON-NLS-1$
		try{
			template2.addTemplateParameter( table.newSymbol( "X", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_BadTemplateParameter );
		}
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_struct ); //$NON-NLS-1$
		factory.addSymbol( A );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f.addParameter( T, 0, null, false );
		
		A.addSymbol( f );
		
		ITemplateSymbol temp = table.newTemplateSymbol( "" ); //$NON-NLS-1$
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
		
		ArrayList args = new ArrayList();
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		ITemplateSymbol templateSpec = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		factory.pushTemplate( templateSpec );
		
		IDerivableContainerSymbol ASpec = table.newDerivableContainerSymbol( "A", ITypeInfo.t_struct ); //$NON-NLS-1$
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		factory.addTemplateId( ASpec, args );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.addParameter( ITypeInfo.t_int, 0, null, false );
		f2.setIsForwardDeclaration( true );
		ASpec.addSymbol( f2 );

		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f3.addParameter( ITypeInfo.t_int, 0, null, false );
		
		IDerivableContainerSymbol look = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), ASpec );
		
		ISymbol flook = look.lookupMethodForDefinition( "f", args ); //$NON-NLS-1$
		assertTrue( flook.isTemplateInstance() );
		assertEquals( flook.getInstantiatedSymbol(), f2 );
		flook.setForwardSymbol( f3 );
		
		look.addSymbol( f3 );
		
		look = (IDerivableContainerSymbol) table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
		flook = look.qualifiedFunctionLookup( "f", args ); //$NON-NLS-1$
		
		assertEquals( flook, f3 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol templateArray = table.newTemplateSymbol( "Array" ); //$NON-NLS-1$
		templateArray.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( templateArray );
		
		IDerivableContainerSymbol array = table.newDerivableContainerSymbol( "Array", ITypeInfo.t_class ); //$NON-NLS-1$
		factory.addSymbol( array );
		
		ITemplateSymbol templateSort = table.newTemplateSymbol( "sort" ); //$NON-NLS-1$
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		templateSort.addTemplateParameter( T );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( templateSort );
		
		IParameterizedSymbol sort = table.newParameterizedSymbol( "sort", ITypeInfo.t_function ); //$NON-NLS-1$
		
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T ) );

		ISymbol arrayLook = factory.lookupTemplateId( "Array", args ); //$NON-NLS-1$
		sort.addParameter( arrayLook, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ), false );
		
		factory.addSymbol( sort );

		ITemplateSymbol temp = table.newTemplateSymbol( "" ); //$NON-NLS-1$
		factory = table.newTemplateFactory();
		
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( temp );
				
		IParameterizedSymbol newSort = table.newParameterizedSymbol( "sort", ITypeInfo.t_function ); //$NON-NLS-1$
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		arrayLook = table.getCompilationUnit().lookupTemplateId( "Array", args ); //$NON-NLS-1$
		assertTrue( arrayLook.isTemplateInstance() );
		assertEquals( arrayLook.getInstantiatedSymbol(), array );
		newSort.addParameter( arrayLook, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ), false );
		
		factory.addSymbol( newSort );
		
		ISymbol a = table.newSymbol( "a", ITypeInfo.t_type ); //$NON-NLS-1$
		a.setTypeSymbol( arrayLook );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a ) );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "sort", args ); //$NON-NLS-1$
		
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), newSort );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template1 = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
		ISymbol T1 = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template1.addTemplateParameter( T1 );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template1 );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f1.addParameter( T1, 0, null, false );
		
		factory.addSymbol( f1 );
		
		ITemplateSymbol template2 = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
		ISymbol T2 = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template2.addTemplateParameter( T2 );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template2 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f2.addParameter( T2, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		
		factory.addSymbol( f2 );
		
		factory = table.newTemplateFactory();
		ITemplateSymbol template = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
				
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f3.addParameter( ITypeInfo.t_int, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		
		List args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false ) );
		factory.addTemplateId( f3, args );
		
		args = new ArrayList();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		
		template = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IParameterizedSymbol f4 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f4.addParameter( ITypeInfo.t_int, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ), false );
		factory.addTemplateId( f4, args );

		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null ) );

		template = table.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME );
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() );
		factory.pushTemplate( template );
		
		IParameterizedSymbol f5 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f5.addParameter( ITypeInfo.t_char, 0, null, false );
		factory.addSymbol( f5 );

		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null ) );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f5 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		
		ISymbol lookT = template.lookup( "T" ); //$NON-NLS-1$
		assertEquals( lookT, T );
		
		ISymbol paramX = table.newSymbol( "x", ITypeInfo.t_type ); //$NON-NLS-1$
		paramX.setTypeSymbol( lookT );
		
		ISymbol paramY = table.newSymbol( "y", ITypeInfo.t_type ); //$NON-NLS-1$
		paramY.setTypeSymbol( lookT );
		
		f.addParameter( paramX );
		f.addParameter( paramY );
		
		factory.addSymbol( f );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", ITypeInfo.t_struct ); //$NON-NLS-1$
		table.getCompilationUnit().addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_struct ); //$NON-NLS-1$
		B.addParent( A );
		table.getCompilationUnit().addSymbol( B );
		
		ISymbol a = table.newSymbol( "a", ITypeInfo.t_type ); //$NON-NLS-1$
		a.setTypeSymbol( A );
		
		ISymbol b = table.newSymbol( "b", ITypeInfo.t_type ); //$NON-NLS-1$
		b.setTypeSymbol( B );
		
		table.getCompilationUnit().addSymbol( a );
		table.getCompilationUnit().addSymbol( b );
		
		List argList = new ArrayList();
		ITypeInfo aParam =  TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, a );
		ITypeInfo bParam =  TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, b );
		
		argList.add( aParam );
		argList.add( bParam );
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList ); //$NON-NLS-1$
		assertEquals( look, null );
		
		argList.clear();
		argList.add( bParam );
		argList.add( aParam );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList ); //$NON-NLS-1$
		assertEquals( look, null );
		
		argList.clear();
		argList.add( aParam );
		argList.add( aParam );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f );
		
		argList.clear();
		argList.add( bParam );
		argList.add( bParam );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance());
		assertEquals( look.getInstantiatedSymbol(), f );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
		
		template.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		template.addTemplateParameter( table.newSymbol( "U", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IParameterizedSymbol paramFunction = table.newParameterizedSymbol( "", ITypeInfo.t_function ); //$NON-NLS-1$
		paramFunction.setIsTemplateMember( true );
		
		ISymbol T = template.lookup( "T" ); //$NON-NLS-1$
		ISymbol U = template.lookup( "U" ); //$NON-NLS-1$
		
		paramFunction.setReturnType( T );
		paramFunction.addParameter( T, 0, null, false );
		paramFunction.addParameter( U, 0, null, false );
		paramFunction.addParameter( U, 0, null, false );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f.addParameter( paramFunction, 0, null, false );
		
		factory.addSymbol( f );
		
		IParameterizedSymbol g1 = table.newParameterizedSymbol( "g1", ITypeInfo.t_function ); //$NON-NLS-1$
		g1.setReturnType( table.newSymbol( "", ITypeInfo.t_int ) ); //$NON-NLS-1$
		g1.addParameter( ITypeInfo.t_int, 0, null, false );
		g1.addParameter( ITypeInfo.t_float, 0, null, false );
		g1.addParameter( ITypeInfo.t_float, 0, null, false );
		
		table.getCompilationUnit().addSymbol( g1 );
		
		IParameterizedSymbol g2 = table.newParameterizedSymbol( "g2", ITypeInfo.t_function ); //$NON-NLS-1$
		g2.setReturnType( table.newSymbol( "", ITypeInfo.t_char ) ); //$NON-NLS-1$
		g2.addParameter( ITypeInfo.t_int, 0, null, false );
		g2.addParameter( ITypeInfo.t_float, 0, null, false );
		g2.addParameter( ITypeInfo.t_float, 0, null, false );
		
		table.getCompilationUnit().addSymbol( g2);
		
		IParameterizedSymbol g3 = table.newParameterizedSymbol( "g3", ITypeInfo.t_function ); //$NON-NLS-1$
		g3.setReturnType( table.newSymbol( "", ITypeInfo.t_int ) ); //$NON-NLS-1$
		g3.addParameter( ITypeInfo.t_int, 0, null, false );
		g3.addParameter( ITypeInfo.t_char, 0, null, false );
		g3.addParameter( ITypeInfo.t_float, 0, null, false );
		
		table.getCompilationUnit().addSymbol( g3);
		
		ITypeInfo arg = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, g1 );
		List argList = new ArrayList();
		argList.add( arg );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f );
		
		arg.setTypeSymbol( g2 );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList ); //$NON-NLS-1$
		assertEquals( look, null );
		
		arg.setTypeSymbol( g3 );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", argList ); //$NON-NLS-1$
		assertEquals( look, null );	
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol template = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
		ISymbol T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		template.addTemplateParameter( T );
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( template );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		f.addParameter( T, 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, true, false ), false );
		factory.addSymbol( f );
		
		ISymbol p = table.newSymbol( "p", ITypeInfo.t_int ); //$NON-NLS-1$
		p.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
		
		List params = new ArrayList();
		params.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, p ) );
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params ); //$NON-NLS-1$
		
		assertTrue( look != null );
		assertTrue( look.isTemplateInstance() );
		
		assertEquals( look.getInstantiatedSymbol(), f );
		
		IParameterizedSymbol fn = (IParameterizedSymbol) look;
		Iterator iter = fn.getParameterList().iterator();
		ISymbol param = (ISymbol) iter.next();
		assertTrue( param.isType( ITypeInfo.t_int ) );
		
		assertFalse( iter.hasNext() );
		
		iter = param.getTypeInfo().getPtrOperators().iterator();
		ITypeInfo.PtrOp op = (ITypeInfo.PtrOp) iter.next();
		assertTrue( op.isConst() );
		assertEquals( op.getType(), ITypeInfo.PtrOp.t_pointer );
		assertFalse( iter.hasNext() );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
		
		ITemplateSymbol templateB = table.newTemplateSymbol( "B" ); //$NON-NLS-1$
		templateB.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$
		
		ITemplateFactory factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateB );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", ITypeInfo.t_struct ); //$NON-NLS-1$
		factory.addSymbol( B );
		
		ITemplateSymbol templateD = table.newTemplateSymbol( "D" ); //$NON-NLS-1$
		templateD.addTemplateParameter( table.newSymbol( "T", ITypeInfo.t_templateParameter ) ); //$NON-NLS-1$

		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templateD );
		
		IDerivableContainerSymbol D = table.newDerivableContainerSymbol( "D", ITypeInfo.t_struct ); //$NON-NLS-1$
		factory.addSymbol( D );
		
		ISymbol T = templateD.lookup( "T" ); //$NON-NLS-1$
		List args = new ArrayList ();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T ) );
		ISymbol look = table.getCompilationUnit().lookupTemplateId( "B", args ); //$NON-NLS-1$
		assertTrue( look instanceof IDeferredTemplateInstance );
		assertEquals( ((IDeferredTemplateInstance)look).getTemplate(), templateB );
		
		D.addParent( look );
		
		IDerivableContainerSymbol D2 = table.newDerivableContainerSymbol( "D2", ITypeInfo.t_struct ); //$NON-NLS-1$

		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		look = table.getCompilationUnit().lookupTemplateId( "B", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), B );
		
		D2.addParent( look );
		
		table.getCompilationUnit().addSymbol( D2 );
		
		ITemplateSymbol templatef = table.newTemplateSymbol( "f" ); //$NON-NLS-1$
		T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
		templatef.addTemplateParameter( T );
		
		factory = table.newTemplateFactory();
		factory.setContainingSymbol( table.getCompilationUnit() ); 
		factory.pushTemplate( templatef );
		
		IParameterizedSymbol f  = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, T ) );
		
		look = table.getCompilationUnit().lookupTemplateId( "B", args ); //$NON-NLS-1$
		assertTrue( look instanceof IDeferredTemplateInstance );
		assertEquals( ((IDeferredTemplateInstance)look).getTemplate(), templateB );
		
		ISymbol param = table.newSymbol( "", ITypeInfo.t_type ); //$NON-NLS-1$
		param.setTypeSymbol( look );
		param.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference ) );
		f.addParameter( param );
		
		factory.addSymbol( f );
		
		ISymbol d = table.newSymbol( "d", ITypeInfo.t_type ); //$NON-NLS-1$
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
		look = table.getCompilationUnit().lookupTemplateId( "D", args ); //$NON-NLS-1$
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), D );
		
		d.setTypeSymbol( look );
		table.getCompilationUnit().addSymbol( d );
		
		ISymbol d2 = table.newSymbol( "d2", ITypeInfo.t_type ); //$NON-NLS-1$
		d2.setTypeSymbol( D2 );
		table.getCompilationUnit().addSymbol( d2 );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, d ) );
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", args ); //$NON-NLS-1$
		assertTrue( look != null );
		assertTrue( look.isTemplateInstance() );
		assertEquals( look.getInstantiatedSymbol(), f );
		
		args.clear();
		args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, d2 ) );
		ISymbol look2 = table.getCompilationUnit().unqualifiedFunctionLookup( "f", args ); //$NON-NLS-1$
		assertTrue( look2 != null );
		assertTrue( look2.isTemplateInstance() );
		assertEquals( look2.getInstantiatedSymbol(), f );
		
		//both are the template function instantiated with int, should be the same instance.
		assertEquals( look, look2 );
		assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
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
	  	ITemplateSymbol template = table.newTemplateSymbol( "A" ); //$NON-NLS-1$
	  	ISymbol T1 = table.newSymbol( "T1", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
	  	ISymbol T2 = table.newSymbol( "T2", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
	  	template.addTemplateParameter( T1 );
	  	template.addTemplateParameter( T2 );
	  	
	  	ITemplateFactory factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( template );
	  	
	  	IDerivableContainerSymbol A1 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
	  	factory.addSymbol( A1 );
	  	
	  	IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
	  	f1.setIsForwardDeclaration( true );
	  	A1.addSymbol( f1 );
	  	
	  	//template < class T > class A < T, T >    { void f(); };
	  	ITemplateSymbol spec1 = table.newTemplateSymbol(""); //$NON-NLS-1$
	  	ISymbol spec1_T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
	  	spec1.addTemplateParameter( spec1_T );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( spec1 );
	  	
	  	List args = new ArrayList();
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, spec1_T ) );
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, spec1_T ) );
	  	
	  	IDerivableContainerSymbol A2 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
	  	factory.addTemplateId( A2, args );
	  	
	  	IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
	  	f2.setIsForwardDeclaration( true );
	  	A2.addSymbol( f2 );

	  	//template < class T > class A < char, T > { void f(); };
	  	ITemplateSymbol spec2 = table.newTemplateSymbol(""); //$NON-NLS-1$
	  	ISymbol spec2_T = table.newSymbol( "T", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
	  	spec2.addTemplateParameter( spec2_T );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( spec2 );
	  	
	  	args.clear();
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null ) );
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, spec2_T ) );
	  	
	  	IDerivableContainerSymbol A3 = table.newDerivableContainerSymbol( "A", ITypeInfo.t_class ); //$NON-NLS-1$
	  	factory.addTemplateId( A3, args );
	  	
	  	IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
	  	f3.setIsForwardDeclaration( true );
	  	A3.addSymbol( f3 );
	  	
	  	//template < class U, class V > void A<U, V>::f(){  int c; }
	  	ITemplateSymbol templateDef = table.newTemplateSymbol(""); //$NON-NLS-1$
	  	ISymbol U = table.newSymbol( "U", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
	  	ISymbol V = table.newSymbol( "V", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
	  	templateDef.addTemplateParameter( U );
	  	templateDef.addTemplateParameter( V );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( spec2 );
	  	
	  	args.clear();
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, U ) );
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, V ) );
	  	
	  	ISymbol symbol = factory.lookupTemplateId( "A",  args ); //$NON-NLS-1$
	  	assertEquals( ((IDeferredTemplateInstance)symbol).getTemplate(), template );
		factory.pushTemplateId( symbol, args );
	  	
	  	ISymbol look = factory.lookupMethodForDefinition( "f", new ArrayList() ); //$NON-NLS-1$
	  	assertEquals( look, f1 );
	  	IParameterizedSymbol f1Def = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
	  	f1.setForwardSymbol( f1Def );
	  	factory.addSymbol( f1Def );
	  	
	  	ISymbol c1 = table.newSymbol( "c", ITypeInfo.t_int ); //$NON-NLS-1$
	  	f1Def.addSymbol( c1 );
	  	
	  	//template < class W > void A < W, W >::f(){  char c; }
	  	ITemplateSymbol specDef1 = table.newTemplateSymbol(""); //$NON-NLS-1$
	  	ISymbol W = table.newSymbol( "W", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
	  	specDef1.addTemplateParameter( W );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( specDef1 );
	  	
	  	args = new ArrayList();
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, W ) );
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, W ) );
	  	
	  	symbol = factory.lookupTemplateId( "A",  args ); //$NON-NLS-1$
		factory.pushTemplateId( symbol, args );
	  	
	  	look = factory.lookupMethodForDefinition( "f", new ArrayList() ); //$NON-NLS-1$
	  	assertEquals( look, f2 );
	  	IParameterizedSymbol f2Def = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
	  	f2.setForwardSymbol( f2Def );
	  	factory.addSymbol( f2Def );
	  	
	  	ISymbol c2 = table.newSymbol( "c", ITypeInfo.t_char ); //$NON-NLS-1$
	  	f2Def.addSymbol( c2 );
	  	
	  	//template < class X > void < char, X >::f(){ float c; }
	  	ITemplateSymbol specDef2 = table.newTemplateSymbol(""); //$NON-NLS-1$
	  	ISymbol X = table.newSymbol( "X", ITypeInfo.t_templateParameter ); //$NON-NLS-1$
	  	specDef2.addTemplateParameter( X );
	  	
	  	factory = table.newTemplateFactory();
	  	factory.setContainingSymbol( table.getCompilationUnit() );
	  	factory.pushTemplate( specDef1 );
	  	
	  	args = new ArrayList();
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null ) );
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, X ) );
	  	
	  	symbol = factory.lookupTemplateId( "A",  args ); //$NON-NLS-1$
		factory.pushTemplateId( symbol, args );
		
		look = factory.lookupMethodForDefinition( "f", new ArrayList() ); //$NON-NLS-1$
	  	assertEquals( look, f3 );
	  	IParameterizedSymbol f3Def = table.newParameterizedSymbol( "f", ITypeInfo.t_function ); //$NON-NLS-1$
	  	f3.setForwardSymbol( f3Def );
	  	factory.addSymbol( f3Def );
	  	
	  	ISymbol c3 = table.newSymbol( "c", ITypeInfo.t_float ); //$NON-NLS-1$
	  	f3Def.addSymbol( c3 );
	  	
	  	//A< int, char > a1;
	  	args = new ArrayList();
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null ) );
	  
	  	look = table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), A1 );
	  	
	  	look = ((IContainerSymbol)look).qualifiedFunctionLookup( "f", new ArrayList() ); //$NON-NLS-1$
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), f1Def );
	  	
	  	look = ((IContainerSymbol)look).qualifiedLookup( "c" ); //$NON-NLS-1$
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), c1 );
	  	assertTrue( look.isType( ITypeInfo.t_int ) );
	
	  	//A< int, int > a2;
	  	args.clear();
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
	  	
	  	look = table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), A2 );
	  	
	  	look = ((IContainerSymbol)look).qualifiedFunctionLookup( "f", new ArrayList() ); //$NON-NLS-1$
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), f2Def );
	  	
	  	look = ((IContainerSymbol)look).qualifiedLookup( "c" ); //$NON-NLS-1$
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), c2 );
	  	assertTrue( look.isType( ITypeInfo.t_char ) );
	  	
	  	//A< char, int > a3;
	  	args.clear();
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_char, 0, null ) );
	  	args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_int, 0, null ) );
	  	
	  	look = table.getCompilationUnit().lookupTemplateId( "A", args ); //$NON-NLS-1$
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), A3 );
	  	
	  	look = ((IContainerSymbol)look).qualifiedFunctionLookup( "f", new ArrayList() ); //$NON-NLS-1$
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), f3Def );
	  	
	  	look = ((IContainerSymbol)look).qualifiedLookup( "c" ); //$NON-NLS-1$
	  	assertTrue( look.isTemplateInstance() );
	  	assertEquals( look.getInstantiatedSymbol(), c3 );
	  	assertTrue( look.isType( ITypeInfo.t_float ) );
	  	assertEquals( table.getTypeInfoProvider().numAllocated(), 0 );
	  }
}