/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.core.parser.tests;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.pst.ISymbol;
import org.eclipse.cdt.internal.core.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.pst.ParserSymbolTableException;
//import org.eclipse.cdt.internal.core.pst.ParserSymbolTable.Declaration;
import org.eclipse.cdt.internal.core.pst.ParserSymbolTable.Mark;
import org.eclipse.cdt.internal.core.pst.ParserSymbolTable.TypeInfo;

/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserSymbolTableTest extends TestCase {

	public ParserSymbolTable table = null;
	
	public ParserSymbolTableTest( String arg )
	{
		super( arg );
	}
	
	public ParserSymbolTable newTable(){
		table = new ParserSymbolTable();
		return table;
	}
	
	/**
	 * testSimpleAdd.  
	 * Add a declaration to the table and confirm it is there.
	 * 
	 * @throws Exception
	 */
	public void testSimpleAdd() throws Exception{
		newTable(); //create the symbol table
		
		ISymbol x = table.newSymbol( "x" );
		IContainerSymbol compUnit = table.getCompilationUnit();
		compUnit.addSymbol( x );
	
		Map declarations = compUnit.getContainedSymbols();
		assertEquals( 1, declarations.size() );
		
		Iterator iter = declarations.values().iterator();
		ISymbol contained = (ISymbol) iter.next();
		
		assertEquals( false, iter.hasNext() );
		assertEquals( x, contained );
		assertEquals( contained.getName(), "x" );
	}

	/**
	 * testSimpleLookup
	 * Add a declaration to the table, then look it up.
	 * @throws Exception
	 */
	public void testSimpleLookup() throws Exception{
		newTable(); //new symbol table
		
		ISymbol x = table.new Declaration( "x" );
		table.getCompilationUnit().addSymbol( x );
		
		ISymbol look = table.getCompilationUnit().Lookup( "x" );
		
		assertEquals( x, look );
	}
	
	public void testLookupNonExistant() throws Exception{
		newTable();
		
		ISymbol look = table.getCompilationUnit().Lookup("boo");
		assertEquals( look, null );
	}
	
	public void testSimpleSetGetObject() throws Exception{
		newTable();
		
		ISymbol x = table.new Declaration("x");
		
		Object obj = new Object();
		x.setCallbackExtension( obj );
				
		table.getCompilationUnit().addSymbol( x );
		
		ISymbol look = table.getCompilationUnit().Lookup( "x" );
		
		assertEquals( look.getCallbackExtension(), obj );
	}
	
	/**
	 * testHide
	 * test that a declaration in a scope hides declarations in containing
	 * scopes
	 * @throws Exception
	 */
	public void testHide() throws Exception{
		newTable();
		
		ISymbol firstX = table.newSymbol("x");
		table.getCompilationUnit().addSymbol( firstX );
		
		IDerivableContainerSymbol firstClass = table.newDerivableContainerSymbol("class");
		firstClass.setType( ParserSymbolTable.TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( firstClass );

		ISymbol look = firstClass.Lookup( "x" );
		assertEquals( look, firstX );
		
		ISymbol secondX = table.newSymbol("x");
		firstClass.addSymbol( secondX );
		
		look = firstClass.Lookup( "x" );
		assertEquals( look, secondX );
		
		look = table.getCompilationUnit().Lookup( "x" );
		assertEquals( look, firstX );
	}
	
	/**
	 * testContainingScopeLookup
	 * test lookup of something declared in the containing scope
	 * @throws Exception
	 */
	public void testContainingScopeLookup() throws Exception{
		newTable();
		
		ISymbol x = table.newSymbol("x");
		table.getCompilationUnit().addSymbol( x );

		IDerivableContainerSymbol decl = table.newDerivableContainerSymbol("class");
		decl.setType( ParserSymbolTable.TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( decl );
		
		ISymbol look = decl.Lookup( "x" );
		
		assertEquals( x, look );
	}
	
	/**
	 * testParentLookup
	 * test lookup of a variable declaration in the parent
	 *  
	 * @throws Exception
	 */
	public void testParentLookup() throws Exception{
		newTable();
		
		IDerivableContainerSymbol parent = table.newDerivableContainerSymbol("parent");
		parent.setType( ParserSymbolTable.TypeInfo.t_class );

		IDerivableContainerSymbol class1 = table.newDerivableContainerSymbol("class");
		class1.setType( ParserSymbolTable.TypeInfo.t_class );
		class1.addParent( parent );
		
		ISymbol decl = table.new Declaration("x");
		parent.addSymbol( decl );
		
		table.getCompilationUnit().addSymbol( parent );
		table.getCompilationUnit().addSymbol( class1 );
		
		ISymbol look = class1.Lookup( "x" );
		assertEquals( look, decl );
	}

	/**
	 * testAmbiguousParentLookup
	 * calls testParentLookup
	 * 
	 * tests that if a variable is declared in two parents that the lookup
	 * returns an ambiguous result.
	 * 
	 * @throws Exception
	 */
	public void testAmbiguousParentLookup() throws Exception{
		testParentLookup();
	
		IDerivableContainerSymbol parent2 = table.newDerivableContainerSymbol("parent2");
		table.getCompilationUnit().addSymbol( parent2 );
		
		IDerivableContainerSymbol class1 = (IDerivableContainerSymbol) table.getCompilationUnit().Lookup( "class" );
		class1.addParent( parent2 );
		
		ISymbol decl = table.new Declaration("x");
		parent2.addSymbol( decl );
				
		try{
			class1.Lookup( "x" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}	
	
	/**
	 * 
	 * @throws Exception
	 * test for circular inheritance 
	 */
	public void testCircularParentLookup() throws Exception{
		newTable();
		
		IDerivableContainerSymbol a = table.newDerivableContainerSymbol("a");
		table.getCompilationUnit().addSymbol( a );
		
		IDerivableContainerSymbol b = table.newDerivableContainerSymbol("b");
		b.addParent( a );
		table.getCompilationUnit().addSymbol( b );
			
		a.addParent( b );
		 
		try{
			ISymbol look = a.Lookup("foo");
			assertTrue( false );
		} catch ( ParserSymbolTableException e) {
			assertEquals( e.reason, ParserSymbolTableException.r_CircularInheritance );
		}
		
	}
	/**
	 * testVirtualParentLookup
	 * 
	 * @throws Exception
	 * tests lookup of name in virtual baseclass C
	 * 
	 * 				C
	 * 			   / \
	 *            A   B
	 *             \ /
	 *            class
	 */
	public void testVirtualParentLookup() throws Exception{
		newTable();
		
		IDerivableContainerSymbol decl = table.newDerivableContainerSymbol("class");
		IDerivableContainerSymbol c    = table.newDerivableContainerSymbol("C");
		
		IDerivableContainerSymbol a    = table.newDerivableContainerSymbol("A");
		a.addParent( c, true );
		
		IDerivableContainerSymbol b    = table.newDerivableContainerSymbol("B");
		b.addParent( c, true );
		
		decl.addParent( a );
		decl.addParent( b );
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		compUnit.addSymbol( c );
		
		ISymbol x = table.new Declaration( "x" );
		c.addSymbol( x );
		
		compUnit.addSymbol( decl );
		compUnit.addSymbol( a );
		compUnit.addSymbol( b );
		
		ISymbol look = decl.Lookup( "x" ); 
		
		assertEquals( look, x );
	}
	
	/**
	 * testAmbiguousVirtualParentLookup
	 * @throws Exception
	 * 
	 * tests lookup of name in base class C in the following hierarchy
	 *                  C   C
	 *                 / \  | 
	 *                A   B D
	 *                 \ / / 
	 *                 class
	 */
	public void testAmbiguousVirtualParentLookup() throws Exception{
		testVirtualParentLookup();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol cls = (IDerivableContainerSymbol) compUnit.Lookup("class");
		IDerivableContainerSymbol c   = (IDerivableContainerSymbol) compUnit.Lookup("C");
		IDerivableContainerSymbol d   = table.newDerivableContainerSymbol("D");
		
		d.addParent( c );
		cls.addParent( d );
		
		try{
			cls.Lookup( "x" );
			assertTrue( false );
		}
		catch( ParserSymbolTableException e){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}
	
	/**
	 * testStaticEnumParentLookup
	 * 
	 * @throws Exception
	 * 
	 *             D   D
	 *             |   |
	 *             B   C
	 *              \ /
	 *               A
	 * 
	 * Things defined in D are not ambiguous if they are static or an enum
	 */
	public void testStaticEnumParentLookup() throws Exception{
		newTable();
		
		IDerivableContainerSymbol a = table.newDerivableContainerSymbol("a" );
		IDerivableContainerSymbol b = table.newDerivableContainerSymbol( "b" );
		IDerivableContainerSymbol c = table.newDerivableContainerSymbol( "c" );
		IDerivableContainerSymbol d = table.newDerivableContainerSymbol( "d" );
	
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		compUnit.addSymbol( a );
		compUnit.addSymbol( b );
		compUnit.addSymbol( c );
		compUnit.addSymbol( d );
		
		IContainerSymbol enum = table.new Declaration("enum");
		enum.setType( ParserSymbolTable.TypeInfo.t_enumeration );
		
		ISymbol enumerator = table.new Declaration( "enumerator" );
		enumerator.setType( ParserSymbolTable.TypeInfo.t_enumerator );
		
		ISymbol stat = table.new Declaration("static");
		stat.getTypeInfo().setBit( true, ParserSymbolTable.TypeInfo.isStatic );
		
		ISymbol x = table.new Declaration("x");
		
		d.addSymbol( enum );
		d.addSymbol( stat );
		d.addSymbol( x );
		
		enum.addSymbol( enumerator );
		
		a.addParent( b );
		a.addParent( c );
		b.addParent( d );
		c.addParent( d );
		
		try{
			a.Lookup( "enumerator" );
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			a.Lookup( "static" );
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			a.Lookup( "x" );
			assertTrue( false );	
		}
		catch ( ParserSymbolTableException e){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}
	
	/**
	 * testElaboratedLookup
	 * @throws Exception
	 * test lookup of hidden names using elaborated type spec
	 */
	public void testElaboratedLookup() throws Exception{
		newTable();
		
		IDerivableContainerSymbol cls = table.newDerivableContainerSymbol( "class" );
		cls.setType( ParserSymbolTable.TypeInfo.t_class );
		
		IDerivableContainerSymbol struct = table.newDerivableContainerSymbol("struct");
		struct.setType( ParserSymbolTable.TypeInfo.t_struct );
		
		IContainerSymbol union = table.newContainerSymbol("union");
		union.setType( ParserSymbolTable.TypeInfo.t_union );
		
		IDerivableContainerSymbol hideCls = table.newDerivableContainerSymbol( "class" );
		IDerivableContainerSymbol hideStruct = table.newDerivableContainerSymbol("struct");
		IContainerSymbol hideUnion = table.newContainerSymbol("union");
		
		IDerivableContainerSymbol a = table.newDerivableContainerSymbol("a");
		IDerivableContainerSymbol b = table.newDerivableContainerSymbol("b");
		
		a.addSymbol(hideCls);
		a.addSymbol(hideStruct);
		a.addSymbol(hideUnion);
		
		a.addParent( b );
		
		b.addSymbol(cls);
		b.addSymbol(struct);
		b.addSymbol(union);
		
		ISymbol look = a.ElaboratedLookup( ParserSymbolTable.TypeInfo.t_class, "class" );
		assertEquals( look, cls );
		look = a.ElaboratedLookup( ParserSymbolTable.TypeInfo.t_struct, "struct" );
		assertEquals( look, struct );
		look = a.ElaboratedLookup( ParserSymbolTable.TypeInfo.t_union, "union" );
		assertEquals( look, union );
	}
	
	/**
	 * testDeclarationType
	 * @throws Exception
	 * test the use of ParserSymbolTable.Declaration type in the scenario
	 * 		A a;
	 * 		a.member <=...>;
	 * where A was previously declared
	 */
	public void testDeclarationType() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		//pre-condition
		IContainerSymbol A = table.newContainerSymbol("A");
		compUnit.addSymbol(A);

		ISymbol member = table.newSymbol("member");
		A.addSymbol(member);
				
		//at time of "A a;"
		ISymbol look = compUnit.Lookup("A");
		assertEquals( look, A );
		ISymbol a = table.newSymbol("a");
		a.setTypeSymbol( look );
		compUnit.addSymbol( a );
		
		//later "a.member"
		look = compUnit.Lookup("a");
		assertEquals( look, a );
		IContainerSymbol type = (IContainerSymbol) look.getTypeSymbol();
		assertEquals( type, A );
		
		look = type.Lookup("member");
		assertEquals( look, member );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * 	struct stat {
	 * 		//...
	 *  }
	 *  int stat( struct stat* );
	 *  void f() 
	 *  {
	 *  	struct stat *ps;
	 *   	stat(ps);
	 *  }
	 */
	public void testFunctionHidesClass() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol struct = table.newDerivableContainerSymbol("stat");
		struct.setType( ParserSymbolTable.TypeInfo.t_struct );
		compUnit.addSymbol( struct );
		
		IParameterizedSymbol function = table.newParameterizedSymbol( "stat" );
		function.setType( ParserSymbolTable.TypeInfo.t_function );
		compUnit.addSymbol( function );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		f.setType( ParserSymbolTable.TypeInfo.t_function );
		compUnit.addSymbol( f );
				
		ISymbol look = f.ElaboratedLookup( ParserSymbolTable.TypeInfo.t_struct, "stat" );
		assertEquals( look, struct );
		
		look = f.Lookup( "stat" );
		assertEquals( look, function );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace A {
	 *    int i;
	 *    namespace B {
	 *       namespace C{
	 *          int i;
	 *       }
	 *       using namespace A::B::C;
	 *       void f1() {
	 *          i = 5;  //OK, C::i visible and hides A::i
	 *       }
	 *    }
	 *    namespace D{
	 *       using namespace B;
	 *       using namespace C;
	 *       void f2(){
	 *          i = 5;  //ambiguous, B::C and A::i
	 *       }
	 *    }
	 *    void f3() {
	 *       i = 5;   //uses A::i
	 *    }
	 * }
	 * void f4(){
	 *    i = 5;   //no i is visible here 
	 * }
	 * 
	 */
	public void testUsingDirectives_1() throws Exception{
		newTable();
		
		IContainerSymbol nsA = table.newContainerSymbol("A");
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		table.getCompilationUnit().addSymbol( nsA );
		
		ISymbol nsA_i = table.newSymbol("i");
		nsA.addSymbol( nsA_i );
		
		IContainerSymbol nsB = table.newContainerSymbol("B");
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		nsA.addSymbol( nsB );
		
		IContainerSymbol nsC = table.newContainerSymbol("C");
		nsC.setType( ParserSymbolTable.TypeInfo.t_namespace );
		nsB.addSymbol( nsC );
		
		ISymbol nsC_i = table.newSymbol("i");
		nsC.addSymbol( nsC_i );
		
		ISymbol look = nsB.Lookup("C");
		assertEquals( look, nsC );
		nsB.addUsingDirective( nsC );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol("f");
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		
		nsB.addSymbol( f1 );
		
		look = f1.Lookup( "i" );
		assertEquals( look, nsC_i ); //C::i visible and hides A::i
		
		IContainerSymbol nsD = table.newContainerSymbol("D");
		nsD.setType( ParserSymbolTable.TypeInfo.t_namespace );
		nsA.addSymbol( nsD );
		
		look = nsD.Lookup("B");
		assertEquals( look, nsB );
		nsD.addUsingDirective( nsB );
		
		look = nsD.Lookup("C");
		assertEquals( look, nsC );
		nsD.addUsingDirective( nsC );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f2" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		nsD.addSymbol( f2 );
		
		try
		{
			look = f2.Lookup( "i" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			//ambiguous B::C::i and A::i
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol("f3");
		f3.setType( ParserSymbolTable.TypeInfo.t_function );
		nsA.addSymbol( f3 );
		
		look = f3.Lookup("i");
		assertEquals( look, nsA_i );  //uses A::i
		
		IParameterizedSymbol f4 = table.newParameterizedSymbol("f4");
		f4.setType( ParserSymbolTable.TypeInfo.t_function );
		table.getCompilationUnit().addSymbol( f4 );
		
		look = f4.Lookup("i");
		assertEquals( look, null );//neither i is visible here.
	}
	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace M {
	 *    int i;
	 * }
	 * namespace N {
	 *    int i;
	 *    using namespace M;
	 * }
	 * 
	 * void f() {
	 *    using namespace N;
	 *    i = 7;           //error, both M::i and N::i are visible
	 *    N::i = 5;        //ok, i directly declared in N, using M not
	 *                       considered (since this is a qualified lookup)
	 * }
	 * 
	 */
	public void testTransitiveUsingDirective() throws Exception
	{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsM = table.newContainerSymbol( "M" );
		nsM.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addSymbol( nsM );
		
		ISymbol nsM_i = table.newSymbol("i");
		nsM.addSymbol( nsM_i );
				
		IContainerSymbol nsN = table.newContainerSymbol( "N" );
		nsN.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addSymbol( nsN );
		
		ISymbol nsN_i = table.newSymbol("i");
		nsN.addSymbol( nsN_i );
		nsN.addUsingDirective( nsM );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		compUnit.addSymbol( f );
		
		f.addUsingDirective( nsN );
		
		ISymbol look = null;
		try
		{
			look = f.Lookup( "i" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			//ambiguous, both M::i and N::i are visible.
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		look = f.LookupNestedNameSpecifier("N");
		look = ((IContainerSymbol) look).QualifiedLookup("i"); //ok
		assertEquals( look, nsN_i );
	}
	
	/**
	 * 
	 * @throws Exception
	 * The same declaration found more than once is not an ambiguity
	 * namespace A{
	 *    int a;
	 * }
	 * namespace B{
	 *    using namespace A;
	 * }
	 * namespace C{
	 *    using namespace A;
	 * }
	 * 
	 * namespace BC{
	 *    using namespace B;
	 *    using namespace C;
	 * }
	 * 
	 * void f(){
	 *    BC::a++; //ok 
	 * }
	 */
	public void testUsing_SameDeclarationTwice() throws Exception
	{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsA = table.newContainerSymbol("A");
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
		
		ISymbol a = table.newSymbol("a");
		nsA.addSymbol( a );
				
		IContainerSymbol nsB = table.newContainerSymbol("B");
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( nsB );
		nsB.addUsingDirective( nsA );
		
		IContainerSymbol nsC = table.newContainerSymbol("C");
		nsC.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( nsC );
		nsC.addUsingDirective( nsA );
		
		IContainerSymbol nsBC = table.newContainerSymbol("BC");
		nsBC.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( nsBC );
		nsBC.addUsingDirective( nsB );
		nsBC.addUsingDirective( nsC );		
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		f.setType(ParserSymbolTable.TypeInfo.t_function);
		compUnit.addSymbol( f );
		
		ISymbol look = f.LookupNestedNameSpecifier("BC");
		assertEquals( look, nsBC );
		look = ((IContainerSymbol)look).QualifiedLookup("a");
		assertEquals( look, a );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace B {
	 *    int b;
	 * }
	 * namespace A {
	 *    using namespace B;
	 *    int a;
	 * }
	 * namespace B {
	 *    using namespace A;
	 * }
	 * 
	 * void f(){
	 *    A::a++;   //ok
	 *    A::b++;   //ok
	 *    B::a++;   //ok       
	 *    B::b++;   //ok 
	 * }
	 */
	public void testUsing_SearchedOnce() throws Exception
	{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsB = table.newContainerSymbol( "B" );
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( nsB );
		
		ISymbol b = table.newSymbol("b");
		nsB.addSymbol( b );
		
		IContainerSymbol nsA = table.newContainerSymbol( "A" );
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
		
		nsA.addUsingDirective( nsB );
		
		ISymbol a = table.newSymbol("a");
		nsA.addSymbol( a );
		
		nsB.addUsingDirective( nsA );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		compUnit.addSymbol(f);
		
		IContainerSymbol lookA = f.LookupNestedNameSpecifier("A");
		ISymbol look = lookA.QualifiedLookup("a");
		assertEquals( look, a );
		
		look = lookA.QualifiedLookup("b");
		assertEquals( look, b );
		
		IContainerSymbol lookB = f.LookupNestedNameSpecifier("B");
		look = lookB.QualifiedLookup("a");
		assertEquals( look, a );
		
		look = lookB.QualifiedLookup("b");
		assertEquals( look, b );
	}
	
	/**
	 * we pass if we don't go into an infinite loop.
	 * TBD: we need a mechanism to detect failure of this
	 * test instead of just looping forever.
	 * 
	 * @throws Exception
	 * 
	 * namespace A{
	 * }
	 * namespace B{
	 *    using namespace A;
	 * }
	 * namespace A{
	 *    using namespace B;
	 * }
	 * void f(){
	 *    using namespace A;
	 *    using namespace B;
	 *    i = 1; //not declared anywhere.
	 * }
	 */
	public void testUsing_SearchedOnce_2() throws Exception
	{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsA = table.newContainerSymbol( "A" );
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
			
		IContainerSymbol nsB = table.newContainerSymbol( "B" );
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( nsB );
		nsB.addUsingDirective( nsA );
		
		nsA.addUsingDirective( nsB );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		compUnit.addSymbol(f);
		f.addUsingDirective(nsA);
		f.addUsingDirective(nsB);
		
		ISymbol look = f.Lookup("i");
		assertEquals( look, null );
	}
	
	/**
	 * During lookup of a qualified namespace member name, if the lookup finds
	 * more than one declaration of the member, non-type names hide class or
	 * enumeration names if and only if the declarations are from the same
	 * namespace
	 * @throws Exception
	 * 
	 * namespace A {
	 *    struct x { };
	 *    int x;
	 *    int y;
	 * }
	 * namespace B {
	 *    struct y { };
	 * }
	 * 
	 * namespace C {
	 *    using namespace A;
	 *    using namespace B;
	 * 
	 *    int i = C::x;      //ok, finds A::x
	 *    int j = C::y;      //ambiguous, A::y or B::y
	 * }
	 */
	public void testNamespaceMemberHiding() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsA = table.newContainerSymbol("A");
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addSymbol( nsA );
		
		IContainerSymbol structX = table.newContainerSymbol("x");
		structX.setType( ParserSymbolTable.TypeInfo.t_struct );
		nsA.addSymbol( structX );
		
		ISymbol intX = table.newSymbol("x");
		intX.setType( ParserSymbolTable.TypeInfo.t_int );
		nsA.addSymbol( intX );
		
		ISymbol intY = table.newSymbol("y");
		intY.setType( ParserSymbolTable.TypeInfo.t_int );
		nsA.addSymbol( intY );

		IContainerSymbol nsB = table.newContainerSymbol("B");
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addSymbol( nsB );
		IContainerSymbol structY = table.newContainerSymbol("y");
		structY.setType( ParserSymbolTable.TypeInfo.t_struct );
		nsB.addSymbol( structY );
		
		IContainerSymbol nsC = table.newContainerSymbol("C");
		nsC.setType( ParserSymbolTable.TypeInfo.t_namespace);
		compUnit.addSymbol( nsC );
		
		ISymbol look = nsC.Lookup("A");
		assertEquals( look, nsA );
		nsC.addUsingDirective( nsA );
		
		look = nsC.Lookup("B");
		assertEquals( look, nsB );
		nsC.addUsingDirective( nsB );
		
		//lookup C::x
		look = nsC.LookupNestedNameSpecifier("C");
		assertEquals( look, nsC );
		look = ((IContainerSymbol)look).QualifiedLookup( "x" );
		assertEquals( look, intX );
		
		//lookup C::y
		look = nsC.LookupNestedNameSpecifier("C");
		assertEquals( look, nsC );

		try{
			look = ((IContainerSymbol)look).QualifiedLookup( "y" );
			assertTrue(false);
		} catch ( ParserSymbolTableException e ) {
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}
	
	/**
	 * In a definition for a namespace member in which the declarator-id is a
	 * qualified-id, given that the qualified-id for the namespace member has
	 * the form "nested-name-specifier unqualified-id", the unqualified-id shall
	 * name a member of the namespace designated by the nested-name-specifier.
	 * 
	 * namespace A{    
	 *    namespace B{       
	 *       void  f1(int);    
	 *    }  
	 *    using  namespace B; 
	 * }
	 * void A::f1(int) { ... } //ill-formed, f1 is not a member of A
	 */
	public void testLookupMemberForDefinition() throws Exception{
		newTable();
	
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol nsA = table.newContainerSymbol( "A" );
		nsA.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
	
		IContainerSymbol nsB = table.newContainerSymbol( "B" );
		nsB.setType( ParserSymbolTable.TypeInfo.t_namespace );
		nsA.addSymbol( nsB );
	
		IParameterizedSymbol f1 = table.newParameterizedSymbol("f1");
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		nsB.addSymbol( f1 );
	
		nsA.addUsingDirective( nsB );
	
		IContainerSymbol lookA = compUnit.LookupNestedNameSpecifier( "A" );
		assertEquals( nsA, lookA );
	
		ISymbol look = lookA.LookupMemberForDefinition( "f1" );
		assertEquals( look, null );
	
		//but notice if you wanted to do A::f1 as a function call, it is ok
		look = lookA.QualifiedLookup( "f1" );
		assertEquals( look, f1 );
	}
	
	/**
	 * testUsingDeclaration
	 * @throws Exception
	 * 7.3.3-4 A using-declaration used as a member-declaration shall refer to a
	 * member of a base-class of the class being defined, shall refer to a
	 * member of an anonymous union that is a member of a base class of the
	 * class being defined or shall refer to an enumerator for an enumeration
	 * type that is a member of a base class of the class being defined
	 *
	 * struct B {
	 *    void f( char );
	 *    enum E { e };
	 *    union { int x; };
	 * };
	 * class C {
	 *	  int g();
	 * }
	 * struct D : B {
	 *    using B::f;	//ok, B is a base class of D
	 *    using B::e;   //ok, e is an enumerator in base class B
	 *    using B::x;   //ok, x is an union member of base class B
	 *    using C::g;   //error, C isn't a base class of D
	 * }
	 */
	public void testUsingDeclaration() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol("B");
		B.setType( ParserSymbolTable.TypeInfo.t_struct );
		compUnit.addSymbol( B );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		f.setType( ParserSymbolTable.TypeInfo.t_function );
		B.addSymbol( f );
	
		IContainerSymbol E = table.newContainerSymbol( "E" );
		E.setType( ParserSymbolTable.TypeInfo.t_enumeration );
		B.addSymbol( E );
		
		ISymbol e = table.newSymbol( "e" );
		e.setType( ParserSymbolTable.TypeInfo.t_enumerator );
		E.addSymbol( e );
		
		/**
		 * TBD: Anonymous unions are not yet implemented
		 */
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol( "C" );
		C.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addSymbol( C );
		
		IParameterizedSymbol g = table.newParameterizedSymbol( "g" );
		g.setType( ParserSymbolTable.TypeInfo.t_function );
		C.addSymbol( g );
		
		IDerivableContainerSymbol D = table.newDerivableContainerSymbol( "D" );
		D.setType( ParserSymbolTable.TypeInfo.t_struct );
		ISymbol look = compUnit.Lookup( "B" );
		assertEquals( look, B );
		D.addParent( B );
		
		compUnit.addSymbol( D );
		
		IContainerSymbol lookB = D.LookupNestedNameSpecifier("B");
		assertEquals( lookB, B );

		D.addUsingDeclaration( "f", lookB );
		D.addUsingDeclaration( "e", lookB );
		  
		//TBD anonymous union
		//D.addUsingDeclaration( "x", lookB );
		
		look = D.LookupNestedNameSpecifier("C");
		assertEquals( look, C );
		
		try{
			D.addUsingDeclaration( "g", C );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException exception ){
			assertTrue( true );
		}
	}
	
	/**
	 * testUsingDeclaration_2
	 * @throws Exception
	 * 7.3.3-9 The entity declared by a using-declaration shall be known in the
	 * context using it according to its definition at the point of the using-
	 * declaration.  Definitions added to the namespace after the using-
	 * declaration are not considered when a use of the name is made.
	 * 
	 * namespace A {
	 *     void f(int);
	 * }
	 * using A::f;
	 * 
	 * namespace A {
	 * 	   void f(char);
	 * }
	 * void foo(){
	 * 	  f('a');    //calls f( int )
	 * }
	 * void bar(){
	 * 	  using A::f;
	 * 	  f('a');    //calls f( char );
	 * }	
	 */
	public void testUsingDeclaration_2() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		ParserSymbolTable.Declaration A = table.new Declaration( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_namespace );
		compUnit.addSymbol( A );
		
		ParserSymbolTable.Declaration f1 = table.new Declaration( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		A.addSymbol( f1 );
		
		ISymbol look = compUnit.LookupNestedNameSpecifier("A");
		assertEquals( look, A );
		
		IParameterizedSymbol usingF = (IParameterizedSymbol) compUnit.addUsingDeclaration( "f", A );
		
		look = compUnit.Lookup("A");
		assertEquals( look, A );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol("f");
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, "", false );
		
		A.addSymbol( f2 );
		
		IParameterizedSymbol foo = table.newParameterizedSymbol("foo");
		foo.setType( ParserSymbolTable.TypeInfo.t_function );
		compUnit.addSymbol( foo );

		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo param = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_char, null );
		paramList.add( param );
		
		look = foo.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, usingF );
		assertTrue( usingF.hasSameParameters( f1 ) );
		
		IParameterizedSymbol bar = table.newParameterizedSymbol( "bar" );
		bar.setType( ParserSymbolTable.TypeInfo.t_function );
		bar.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, null, false );
		compUnit.addSymbol( bar );
		
		look = bar.LookupNestedNameSpecifier( "A" );
		assertEquals( look, A );
		bar.addUsingDeclaration( "f", A );
		
		look = bar.UnqualifiedFunctionLookup( "f", paramList );
		assertTrue( look != null );
		assertTrue( ((IParameterizedSymbol) look).hasSameParameters( f2 ) );
	}
	
	/**
	 * testThisPointer
	 * @throws Exception
	 * In the body of a nonstatic member function... the type of this of a class
	 * X is X*.  If the member function is declared const, the type of this is
	 * const X*, if the member function is declared volatile, the type of this
	 * is volatile X*....
	 */
	public void testThisPointer() throws Exception{
		newTable();
		
		IContainerSymbol cls = table.newContainerSymbol("class");
		cls.setType( ParserSymbolTable.TypeInfo.t_class );
		
		IParameterizedSymbol fn = table.newParameterizedSymbol("function");
		fn.setType( ParserSymbolTable.TypeInfo.t_function );
		fn.setCVQualifier( ParserSymbolTable.TypeInfo.cvConst );
		
		table.getCompilationUnit().addSymbol( cls );
		cls.addSymbol( fn );
		
		ISymbol look = fn.Lookup("this");
		assertTrue( look != null );
		
		assertEquals( look.getType(), ParserSymbolTable.TypeInfo.t_type );
		assertEquals( look.getTypeSymbol(), cls );
		assertEquals( look.getPtrOperator(), "*" );
		assertEquals( look.getCVQualifier(), fn.getCVQualifier() );
		assertEquals( look.getContainingSymbol(), fn );
	}
	
	/**
	 * testEnumerator
	 * @throws Exception
	 * Following the closing brace of an enum-specifier, each enumerator has the
	 * type of its enumeration.
	 * The enum-name and each enumerator declared by an enum-specifier is
	 * declared in the scope that immediately contains the enum-specifier
	 */
	public void testEnumerator() throws Exception{
		newTable();
		
		IContainerSymbol cls = table.newContainerSymbol("class");
		cls.setType( ParserSymbolTable.TypeInfo.t_class );
		
		IContainerSymbol enumeration = table.newContainerSymbol("enumeration");
		enumeration.setType( ParserSymbolTable.TypeInfo.t_enumeration );
		
		table.getCompilationUnit().addSymbol( cls );
		cls.addSymbol( enumeration );
		
		ISymbol enumerator = table.newSymbol( "enumerator" );
		enumerator.setType( ParserSymbolTable.TypeInfo.t_enumerator );
		enumeration.addSymbol( enumerator );
		
		ISymbol look = cls.Lookup( "enumerator" );
		assertEquals( look, enumerator );
		assertEquals( look.getContainingSymbol(), cls );
		assertEquals( look.getTypeSymbol(), enumeration );
	}

	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace NS{
	 *    class T {};
	 *    void f( T );
	 * }
	 * NS::T parm;
	 * int main(){
	 *    f( parm );   //ok, calls NS::f
	 * }
	 */
	public void testArgumentDependentLookup() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol NS = table.newContainerSymbol("NS");
		NS.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addSymbol( NS );
		
		IDerivableContainerSymbol T = table.newDerivableContainerSymbol("T");
		T.setType( ParserSymbolTable.TypeInfo.t_class );
		
		NS.addSymbol( T );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		f.setType( ParserSymbolTable.TypeInfo.t_function );
		f.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		
		ISymbol look = NS.Lookup( "T" );
		assertEquals( look, T );				
		f.addParameter( look, 0, "", false );
		
		NS.addSymbol( f );	
				
		look = compUnit.LookupNestedNameSpecifier( "NS" );
		assertEquals( look, NS );
		look = NS.QualifiedLookup( "T" );
		assertEquals( look, T );
		
		ISymbol param = table.newSymbol("parm");
		param.setType( ParserSymbolTable.TypeInfo.t_type );
		param.setTypeSymbol( look );
		compUnit.addSymbol( param );
		
		IParameterizedSymbol main = table.newParameterizedSymbol("main");
		main.setType( ParserSymbolTable.TypeInfo.t_function );
		main.setReturnType( ParserSymbolTable.TypeInfo.t_int );
		compUnit.addSymbol( main );

		LinkedList paramList = new LinkedList();
		look = main.Lookup( "parm" );
		assertEquals( look, param );
		ParserSymbolTable.TypeInfo p = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, look, 0, null, false );
		paramList.add( p );
		
		look = main.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f );
	}
	
	/**
	 * testArgumentDependentLookup_2
	 * @throws Exception
	 * in the following, NS2 is an associated namespace of class B which is an
	 * associated namespace of class A, so we should find f in NS2, we should
	 * not find f in NS1 because usings are ignored for associated scopes.
	 * 
	 *
	 * namespace NS1{
	 *    void f( void * ){}; 
	 * } 
	 * namespace NS2{
	 *	  using namespace NS1;
	 * 	  class B {};
	 *	  void f( void * ){}; 
	 * }
	 * 
	 * class A : public NS2::B {};
	 *
	 * A a;
	 * f( &a );
	 *    
	 */
	public void testArgumentDependentLookup_2() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IContainerSymbol NS1 = table.newContainerSymbol( "NS1" );
		NS1.setType( ParserSymbolTable.TypeInfo.t_namespace );
		 
		compUnit.addSymbol( NS1 );
		
		ParserSymbolTable.Declaration f1 = table.new Declaration( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_void, 0, "*", false );
		NS1.addSymbol( f1 );
		
		IContainerSymbol NS2 = table.newContainerSymbol( "NS2" );
		NS2.setType( ParserSymbolTable.TypeInfo.t_namespace );
		
		compUnit.addSymbol( NS2 );
		
		ISymbol look = NS2.Lookup( "NS1" );
		assertEquals( look, NS1 );
		NS2.addUsingDirective( NS1 );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_class );
		NS2.addSymbol( B );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_void, 0, "*", false );
		NS2.addSymbol( f2 );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		look = compUnit.LookupNestedNameSpecifier( "NS2" );
		assertEquals( look, NS2 );
		
		look = NS2.QualifiedLookup( "B" );
		assertEquals( look, B );
		A.addParent( B );
		
		compUnit.addSymbol( A );
		
		look = compUnit.Lookup( "A" );
		assertEquals( look, A );
		ISymbol a = table.newSymbol( "a" );
		a.setType( ParserSymbolTable.TypeInfo.t_type );
		a.setTypeSymbol( look );
		compUnit.addSymbol( a );
		
		LinkedList paramList = new LinkedList();
		look = compUnit.Lookup( "a" );
		assertEquals( look, a );
		ParserSymbolTable.TypeInfo param = new ParserSymbolTable.TypeInfo( look.getType(), look, 0, "&", false );
		paramList.add( param );
		
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
	}
	
	/**
	 * testFunctionOverloading
	 * @throws Exception
	 * Note that this test has been contrived to not strain the resolution as
	 * that aspect is not yet complete.
	 * 
	 * class C
	 * {   
	 *    void foo( int i );
	 *    void foo( int i, char c );
	 *    void foo( int i, char c, C * ptr ); 
	 * }
	 *
	 * C * c = new C;
	 * c->foo( 1 );
	 * c->foo( 1, 'a' );
	 * c->foo( 1, 'a', c );
	 * 
	 */
	
	public void testFunctionOverloading() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol( "C" );
		C.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addSymbol(C);
				
		IParameterizedSymbol f1 = table.newParameterizedSymbol("foo");
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		C.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol("foo");
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, "", false );
		C.addSymbol( f2 );
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol("foo");
		f3.setType( ParserSymbolTable.TypeInfo.t_function );
		f3.setReturnType( ParserSymbolTable.TypeInfo.t_void );
		f3.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		f3.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, "", false );
		f3.addParameter( C, 0, "*", false );
		C.addSymbol( f3 );
		
		ISymbol look = compUnit.Lookup("C");
		assertEquals( look, C );
		
		ISymbol c = table.newSymbol("c");
		c.setType( ParserSymbolTable.TypeInfo.t_type );
		c.setTypeSymbol( look );
		c.setPtrOperator( "*" );
		compUnit.addSymbol( c );
		
		look = compUnit.Lookup( "c" );
		assertEquals( look, c );
		assertEquals( look.getTypeSymbol(), C );
		
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_int, null, 0, "", false);
		ParserSymbolTable.TypeInfo p2 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_char, null, 0, "", false);
		ParserSymbolTable.TypeInfo p3 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, c, 0, "", false);
		
		paramList.add( p1 );
		look = C.MemberFunctionLookup( "foo", paramList );
		assertEquals( look, f1 );
		
		paramList.add( p2 );
		look = C.MemberFunctionLookup( "foo", paramList );
		assertEquals( look, f2 );
				
		paramList.add( p3 );
		look = C.MemberFunctionLookup( "foo", paramList );
		assertEquals( look, f3 );
	}
	
	/**
	 * 
	 * @throws Exception
	 * test basic function resolution
	 * 
	 * void f( int i ); 
	 * void f( char c = 'c' );
	 * 
	 * f( 1 );		//calls f( int );
	 * f( 'b' ); 	//calls f( char );
	 * f(); 		//calls f( char );
	 */
	public void testFunctionResolution() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol("f");
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "", false );
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol("f");
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_char, 0, "", true );
		compUnit.addSymbol( f2 );
		
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_int, null, 0, "", false );
		paramList.add( p1 );
		
		ISymbol look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		paramList.clear();
		ParserSymbolTable.TypeInfo p2 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_char, null, 0, "", false );
		paramList.add( p2 );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		paramList.clear();
		ParserSymbolTable.TypeInfo p3 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_bool, null, 0, "", false );
		paramList.add( p3 );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		look = compUnit.UnqualifiedFunctionLookup( "f", null );
		assertEquals( look, f2 );
	}
	
	/** 
	 * 
	 * @throws Exception
	 *
	 * class A { };
	 * class B : public A {};
	 * class C : public B {};
	 * 
	 * void f ( A * );
	 * void f ( B * );
	 * 
	 * A* a = new A();
	 * C* c = new C();
	 * 
	 * f( a );		//calls f( A * );
	 * f( c );		//calls f( B * );   	      
	 */
	public void testFunctionResolution_PointersAndBaseClasses() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_class );
		B.addParent( A );
		compUnit.addSymbol( B );
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol( "C" );
		C.setType( ParserSymbolTable.TypeInfo.t_class );
		C.addParent( B );
		compUnit.addSymbol( C );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( A, 0, "*", false );
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter( B, 0, "*", false );
		compUnit.addSymbol( f2 );
		
		ISymbol a = table.newSymbol( "a" );
		a.setType( ParserSymbolTable.TypeInfo.t_type );
		a.setTypeSymbol( A );
		a.setPtrOperator( "*" );
		
		ISymbol c = table.newSymbol( "c" );
		c.setType( ParserSymbolTable.TypeInfo.t_type );
		c.setTypeSymbol( C );
		c.setPtrOperator( "*" );
		
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, a, 0, null, false );
		paramList.add( p1 );
		ISymbol look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		paramList.clear();
		ParserSymbolTable.TypeInfo p2 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, c, 0, "", false );
		paramList.add( p2 );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
	}
	
	/**
	 * 
	 * @throws Exception
	 *
	 * class A {};
	 * typedef A * B;
	 * 
	 * void f( A * );
	 * void f( A );
	 * 
	 * A a;
	 * B b;
	 * A [] array;
	 *
	 * f( a ); 		//calls f( A );
	 * f( &a );		//calls f( A * );
	 * f( b );		//calls f( A * );
	 * f( *b );		//calls f( A );
	 * f( array );  //calls f( A * );
	 */
	public void testFunctionResolution_TypedefsAndPointers() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addSymbol( A );
		
		ISymbol B = table.newSymbol( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_type );
		B.setTypeSymbol( A );
		B.setPtrOperator( "*" );
		compUnit.addSymbol( B );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( A, 0, "*", false );
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter( A, 0, null, false );
		compUnit.addSymbol( f2 );

		ISymbol a = table.newSymbol( "a" );
		a.setType( ParserSymbolTable.TypeInfo.t_type );
		a.setTypeSymbol( A );
		compUnit.addSymbol( a );
				
		ISymbol b = table.newSymbol( "b" );
		b.setType( ParserSymbolTable.TypeInfo.t_type );
		b.setTypeSymbol( B );
		compUnit.addSymbol( b );
		
		ISymbol array = table.newSymbol( "array" );
		array.setType( ParserSymbolTable.TypeInfo.t_type );
		array.setTypeSymbol( A );
		array.setPtrOperator( "[]" );
				
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, a, 0, null, false );
		paramList.add( p );
		
		ISymbol look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		p.setPtrOperator( "&" );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		p.setTypeSymbol( b );
		p.setPtrOperator( null );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		p.setPtrOperator( "*" );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		p.setTypeSymbol( array );
		p.setPtrOperator( null );
		look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
	}
	
	/**
	 * 
	 * @throws Exception
	 *
	 *  class A {};
	 *
	 *	class B
	 *	{
	 *	   B( A a ){ };
	 *	};
	 *	
	 *	void f( B b ){};
	 *	
	 *  A a;
	 *	f( a );
	 */
	public void testUserDefinedConversionSequences() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addSymbol( B );
		
		//12.1-1 "Constructors do not have names"
		IParameterizedSymbol constructor = table.newParameterizedSymbol("");
		constructor.setType( ParserSymbolTable.TypeInfo.t_function );
		constructor.addParameter( A, 0, null, false );
		B.addSymbol( constructor );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f" );
		f.setType( ParserSymbolTable.TypeInfo.t_function );
		f.addParameter( B, 0, null, false );
		compUnit.addSymbol( f );
		
		ISymbol a = table.newSymbol( "a" );
		a.setType( ParserSymbolTable.TypeInfo.t_type );
		a.setTypeSymbol( A );
		compUnit.addSymbol( a );
		
		LinkedList paramList = new LinkedList();
		ParserSymbolTable.TypeInfo p = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, a, 0, null, false );
		paramList.add( p );
		
		ISymbol look = compUnit.UnqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f );	
	}
	
	/**
	 * 
	 * @throws Exception
	 *
	 * void f( const int *, short );
	 * void f( int *, int );
	 * 
	 * int i;
	 * short s;
	 *
	 * void main() {
	 * 	  f( &i, s );		//ambiguous because &i->int* is better than &i->const int *
	 * 	  					//but s-> short is better than s->int
	 * 	  f( &i, 1L );		//calls f(int *, int) because &i->int* is better than &i->const int *
	 * 	  					//and 1L->short and 1L->int are indistinguishable
	 * 	  f( &i, 'c' );		//calls f( int*, int) because &i->int * is better than &i->const int *
	 * 	  					//and c->int is better than c->short
	 * 	  f( (const)&i, 1L ); //calls f(const int *, short ) because const &i->int* is better than &i->int *
	 * 	  					   //and 1L->short and 1L->int are indistinguishable
	 * }
	 */
	public void testOverloadRanking() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int, ParserSymbolTable.TypeInfo.cvConst, "*", false );
		f1.addParameter( ParserSymbolTable.TypeInfo.t_int | ParserSymbolTable.TypeInfo.isShort, 0, null, false );
		
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, "*", false );
		f2.addParameter( ParserSymbolTable.TypeInfo.t_int, 0, null, false );
		compUnit.addSymbol( f2 );
		
		ISymbol i = table.newSymbol( "i" );
		i.setType( ParserSymbolTable.TypeInfo.t_int );
		compUnit.addSymbol( i );
		
		ISymbol s = table.newSymbol( "s" );
		s.setType( ParserSymbolTable.TypeInfo.t_int );
		s.getTypeInfo().setBit( true, ParserSymbolTable.TypeInfo.isShort );
		compUnit.addSymbol( s );
		
		IParameterizedSymbol main = table.newParameterizedSymbol( "main" );
		main.setType( ParserSymbolTable.TypeInfo.t_function );
		compUnit.addSymbol( main );
		
		LinkedList params = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, i, 0, "&", false );
		ParserSymbolTable.TypeInfo p2 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, s, 0, null, false );
		params.add( p1 );
		params.add( p2 );
		
		ISymbol look = null;
		
		try{
			look = main.UnqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		params.clear();
		ParserSymbolTable.TypeInfo p3 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_int | ParserSymbolTable.TypeInfo.isLong, null, 0, null, false );
		params.add( p1 );
		params.add( p3 );
		look = main.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );
		
		params.clear();
		ParserSymbolTable.TypeInfo p4 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_char, null, 0, null, false );
		params.add( p1 );
		params.add( p4 );
		look = main.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );
		
		params.clear();
		p1.setCVQualifier( ParserSymbolTable.TypeInfo.cvConst );
		params.add( p1 );
		params.add( p3 );
		look = main.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f1 );
		
	}
	
	/**
	 * 
	 * @throws Exception
	 *
	 * class B;
	 * class A { A( B& ); };
	 * class B { operator A(); };
	 *  
	 * void f(A){}
	 * 
	 * B b;
	 * f( b );	//ambiguous because b->A via constructor or conversion
	 *
	 * class C { C( B& ); };
	 *  
	 * void f(C){}
	 * 
	 * f( b );	//ambiguous because b->C via constructor and b->a via constructor/conversion
	 * 
	 * void f(B){}
	 * 
	 * f( b );  //calls f(B) 
	 */
	   
	public void testUserDefinedConversionByOperator() throws Exception{
		newTable();
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" );
		B.setType( ParserSymbolTable.TypeInfo.t_class );
		
		compUnit.addSymbol( B );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" );
		A.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addSymbol( A );
		
		IParameterizedSymbol constructA = table.newParameterizedSymbol( "" );
		constructA.setType( ParserSymbolTable.TypeInfo.t_function );
		constructA.addParameter( B, 0, "&", false );
		A.addSymbol( constructA );
		
		IParameterizedSymbol operator = table.newParameterizedSymbol( "operator A" );
		operator.setType( ParserSymbolTable.TypeInfo.t_function );
		B.addSymbol( operator );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" );
		f1.setType( ParserSymbolTable.TypeInfo.t_function );
		f1.addParameter( A, 0, null, false );
		compUnit.addSymbol( f1 );
		
		ISymbol b = table.newSymbol( "b" );
		b.setType( ParserSymbolTable.TypeInfo.t_type );
		b.setTypeSymbol( B );
		
		LinkedList params = new LinkedList();
		ParserSymbolTable.TypeInfo p1 = new ParserSymbolTable.TypeInfo( ParserSymbolTable.TypeInfo.t_type, b, 0, null, false );
		params.add( p1 );
		
		ISymbol look = null;
		
		try{
			look = compUnit.UnqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol("C");
		C.setType( ParserSymbolTable.TypeInfo.t_class );
		compUnit.addSymbol( C );
		
		IParameterizedSymbol constructC = table.newParameterizedSymbol("");
		constructC.setType( ParserSymbolTable.TypeInfo.t_function );
		constructC.addParameter( B, 0, "&", false );
		C.addSymbol( constructC );

		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( ParserSymbolTable.TypeInfo.t_function );
		f2.addParameter(  C, 0, null, false );
		compUnit.addSymbol( f2 );
		
		try{
			look = compUnit.UnqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f" );
		f3.setType( ParserSymbolTable.TypeInfo.t_function );
		f3.addParameter(  B, 0, null, false );
		compUnit.addSymbol( f3 );
		
		look = compUnit.UnqualifiedFunctionLookup( "f", params );
		assertEquals( look, f3 );
	}
	
	public void testMarkRollback() throws Exception{
		newTable();
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol("A");
		A.setType( TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( A );
		
		Mark mark = table.setMark();
		
		ISymbol f = table.newSymbol("f");
		A.addSymbol( f );
		
		ISymbol look = A.Lookup("f");
		assertEquals( look, f );
		
		assertTrue( table.rollBack( mark ) );
		
		look = A.Lookup("f");
		assertEquals( look, null );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol("B");
		B.setType( TypeInfo.t_class );
		
		mark = table.setMark();
		table.getCompilationUnit().addSymbol( B );
		Mark mark2 = table.setMark();
		A.addParent( B );
		Mark mark3 = table.setMark();
		
		IParameterizedSymbol C = table.newParameterizedSymbol("C");
		C.addParameter( TypeInfo.t_class, 0, "", false );
		
		assertEquals( C.getParameterList().size(), 1 );
		table.rollBack( mark3 );
		assertEquals( C.getParameterList().size(), 0 );
		assertEquals( A.getParents().size(), 1 );
		table.rollBack( mark2 );
		assertEquals( A.getParents().size(), 0 );
		
		assertFalse( table.commit( mark2 ) );
		assertFalse( table.rollBack( mark2 ) );
		
		B.setType( TypeInfo.t_namespace );
		
		mark = table.setMark();
		A.addUsingDirective( B );
		assertEquals( A.getUsingDirectives().size(), 1 );
		table.rollBack( mark );
		assertEquals( A.getUsingDirectives().size(), 0 );
	}
}

