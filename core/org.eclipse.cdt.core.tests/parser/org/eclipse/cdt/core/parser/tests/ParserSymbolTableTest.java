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

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.StandardSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.TemplateInstance;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.Mark;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo.OperatorExpression;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp;



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
		return newTable( ParserLanguage.CPP );
	}
	
	public ParserSymbolTable newTable( ParserLanguage language ){
		table = new ParserSymbolTable( language );
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
		
		ISymbol x = table.newSymbol( "x", TypeInfo.t_int );
		table.getCompilationUnit().addSymbol( x );
		
		ISymbol look = table.getCompilationUnit().lookup( "x" );
		
		assertEquals( x, look );
	}
	
	public void testLookupNonExistant() throws Exception{
		newTable();
		
		ISymbol look = table.getCompilationUnit().lookup("boo");
		assertEquals( look, null );
	}
	
	public void testSimpleSetGetObject() throws Exception{
		newTable();
		
		IContainerSymbol x = table.newContainerSymbol( "x", TypeInfo.t_namespace );
		
		ISymbolASTExtension extension = new StandardSymbolExtension(x,null);  
		
		x.setASTExtension( extension );
				
		table.getCompilationUnit().addSymbol( x );
		
		ISymbol look = table.getCompilationUnit().lookup( "x" );
		
		assertEquals( look.getASTExtension(), extension );
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
		firstClass.setType( TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( firstClass );

		ISymbol look = firstClass.lookup( "x" );
		assertEquals( look, firstX );
		
		ISymbol secondX = table.newSymbol("x");
		firstClass.addSymbol( secondX );
		
		look = firstClass.lookup( "x" );
		assertEquals( look, secondX );
		
		look = table.getCompilationUnit().lookup( "x" );
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
		decl.setType( TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( decl );
		
		ISymbol look = decl.lookup( "x" );
		
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
		parent.setType( TypeInfo.t_class );

		IDerivableContainerSymbol class1 = table.newDerivableContainerSymbol("class");
		class1.setType( TypeInfo.t_class );
		class1.addParent( parent );
		
		ISymbol decl = table.newSymbol( "x", TypeInfo.t_int );
		parent.addSymbol( decl );
		
		table.getCompilationUnit().addSymbol( parent );
		table.getCompilationUnit().addSymbol( class1 );
		
		ISymbol look = class1.lookup( "x" );
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
		
		IDerivableContainerSymbol class1 = (IDerivableContainerSymbol) table.getCompilationUnit().lookup( "class" );
		class1.addParent( parent2 );
		
		ISymbol decl = table.newSymbol( "x", TypeInfo.t_int );
		parent2.addSymbol( decl );
				
		try{
			class1.lookup( "x" );
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
			a.lookup("foo");
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
		a.addParent( c, true, ASTAccessVisibility.PUBLIC, 3, null );
		
		IDerivableContainerSymbol b    = table.newDerivableContainerSymbol("B");
		b.addParent( c, true, ASTAccessVisibility.PUBLIC, 6, null );
		
		decl.addParent( a );
		decl.addParent( b );
		
		IContainerSymbol compUnit = table.getCompilationUnit();
		compUnit.addSymbol( c );
		
		ISymbol x = table.newSymbol( "x", TypeInfo.t_int );
		c.addSymbol( x );
		
		compUnit.addSymbol( decl );
		compUnit.addSymbol( a );
		compUnit.addSymbol( b );
		
		ISymbol look = decl.lookup( "x" ); 
		
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
		
		IDerivableContainerSymbol cls = (IDerivableContainerSymbol) compUnit.lookup("class");
		IDerivableContainerSymbol c   = (IDerivableContainerSymbol) compUnit.lookup("C");
		IDerivableContainerSymbol d   = table.newDerivableContainerSymbol("D");
		
		d.addParent( c );
		cls.addParent( d );
		
		compUnit.addSymbol( d );
		
		try{
			cls.lookup( "x" );
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
		
		IContainerSymbol enum = table.newContainerSymbol( "enum", TypeInfo.t_enumeration );
		
		ISymbol enumerator = table.newSymbol( "enumerator", TypeInfo.t_enumerator );
		
		ISymbol stat = table.newSymbol( "static", TypeInfo.t_int );
		stat.getTypeInfo().setBit( true, TypeInfo.isStatic );
		
		ISymbol x = table.newSymbol( "x", TypeInfo.t_int );
		
		d.addSymbol( enum );
		d.addSymbol( stat );
		d.addSymbol( x );
		
		enum.addSymbol( enumerator );
		
		a.addParent( b );
		a.addParent( c );
		b.addParent( d );
		c.addParent( d );
		
		try{
			a.lookup( "enumerator" );
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			a.lookup( "static" );
			assertTrue( true );	
		}
		catch ( ParserSymbolTableException e){
			assertTrue( false );
		}
		
		try{
			a.lookup( "x" );
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
		cls.setType( TypeInfo.t_class );
		
		IDerivableContainerSymbol struct = table.newDerivableContainerSymbol("struct");
		struct.setType( TypeInfo.t_struct );
		
		IContainerSymbol union = table.newContainerSymbol("union");
		union.setType( TypeInfo.t_union );
		
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
		
		table.getCompilationUnit().addSymbol( a );
		table.getCompilationUnit().addSymbol( b );
		
		ISymbol look = a.elaboratedLookup( TypeInfo.t_class, "class" );
		assertEquals( look, cls );
		look = a.elaboratedLookup( TypeInfo.t_struct, "struct" );
		assertEquals( look, struct );
		look = a.elaboratedLookup( TypeInfo.t_union, "union" );
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
		ISymbol look = compUnit.lookup("A");
		assertEquals( look, A );
		ISymbol a = table.newSymbol("a");
		a.setTypeSymbol( look );
		compUnit.addSymbol( a );
		
		//later "a.member"
		look = compUnit.lookup("a");
		assertEquals( look, a );
		IContainerSymbol type = (IContainerSymbol) look.getTypeSymbol();
		assertEquals( type, A );
		
		look = type.lookup("member");
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
		struct.setType( TypeInfo.t_struct );
		compUnit.addSymbol( struct );
		
		IParameterizedSymbol function = table.newParameterizedSymbol( "stat" );
		function.setType( TypeInfo.t_function );
		compUnit.addSymbol( function );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		f.setType( TypeInfo.t_function );
		compUnit.addSymbol( f );
				
		ISymbol look = f.elaboratedLookup( TypeInfo.t_struct, "stat" );
		assertEquals( look, struct );
		
		look = f.lookup( "stat" );
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
		nsA.setType( TypeInfo.t_namespace );
		table.getCompilationUnit().addSymbol( nsA );
		
		ISymbol nsA_i = table.newSymbol("i");
		nsA.addSymbol( nsA_i );
		
		IContainerSymbol nsB = table.newContainerSymbol("B");
		nsB.setType( TypeInfo.t_namespace );
		nsA.addSymbol( nsB );
		
		IContainerSymbol nsC = table.newContainerSymbol("C");
		nsC.setType( TypeInfo.t_namespace );
		nsB.addSymbol( nsC );
		
		ISymbol nsC_i = table.newSymbol("i");
		nsC.addSymbol( nsC_i );
		
		ISymbol look = nsB.lookup("C");
		assertEquals( look, nsC );
		nsB.addUsingDirective( nsC );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol("f");
		f1.setType( TypeInfo.t_function );
		
		nsB.addSymbol( f1 );
		
		look = f1.lookup( "i" );
		assertEquals( look, nsC_i ); //C::i visible and hides A::i
		
		IContainerSymbol nsD = table.newContainerSymbol("D");
		nsD.setType( TypeInfo.t_namespace );
		nsA.addSymbol( nsD );
		
		look = nsD.lookup("B");
		assertEquals( look, nsB );
		nsD.addUsingDirective( nsB );
		
		look = nsD.lookup("C");
		assertEquals( look, nsC );
		nsD.addUsingDirective( nsC );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f2" );
		f2.setType( TypeInfo.t_function );
		nsD.addSymbol( f2 );
		
		try
		{
			look = f2.lookup( "i" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			//ambiguous B::C::i and A::i
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol("f3");
		f3.setType( TypeInfo.t_function );
		nsA.addSymbol( f3 );
		
		look = f3.lookup("i");
		assertEquals( look, nsA_i );  //uses A::i
		
		IParameterizedSymbol f4 = table.newParameterizedSymbol("f4");
		f4.setType( TypeInfo.t_function );
		table.getCompilationUnit().addSymbol( f4 );
		
		look = f4.lookup("i");
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
		nsM.setType( TypeInfo.t_namespace );
		
		compUnit.addSymbol( nsM );
		
		ISymbol nsM_i = table.newSymbol("i");
		nsM.addSymbol( nsM_i );
				
		IContainerSymbol nsN = table.newContainerSymbol( "N" );
		nsN.setType( TypeInfo.t_namespace );
		
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
			look = f.lookup( "i" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e )
		{
			//ambiguous, both M::i and N::i are visible.
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		look = f.lookupNestedNameSpecifier("N");
		look = ((IContainerSymbol) look).qualifiedLookup("i"); //ok
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
		nsA.setType( TypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
		
		ISymbol a = table.newSymbol("a");
		nsA.addSymbol( a );
				
		IContainerSymbol nsB = table.newContainerSymbol("B");
		nsB.setType( TypeInfo.t_namespace );
		compUnit.addSymbol( nsB );
		nsB.addUsingDirective( nsA );
		
		IContainerSymbol nsC = table.newContainerSymbol("C");
		nsC.setType( TypeInfo.t_namespace );
		compUnit.addSymbol( nsC );
		nsC.addUsingDirective( nsA );
		
		IContainerSymbol nsBC = table.newContainerSymbol("BC");
		nsBC.setType( TypeInfo.t_namespace );
		compUnit.addSymbol( nsBC );
		nsBC.addUsingDirective( nsB );
		nsBC.addUsingDirective( nsC );		
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		f.setType(TypeInfo.t_function);
		compUnit.addSymbol( f );
		
		ISymbol look = f.lookupNestedNameSpecifier("BC");
		assertEquals( look, nsBC );
		look = ((IContainerSymbol)look).qualifiedLookup("a");
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
		nsB.setType( TypeInfo.t_namespace );
		compUnit.addSymbol( nsB );
		
		ISymbol b = table.newSymbol("b");
		nsB.addSymbol( b );
		
		IContainerSymbol nsA = table.newContainerSymbol( "A" );
		nsA.setType( TypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
		
		nsA.addUsingDirective( nsB );
		
		ISymbol a = table.newSymbol("a");
		nsA.addSymbol( a );
		
		nsB.addUsingDirective( nsA );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		compUnit.addSymbol(f);
		
		IContainerSymbol lookA = f.lookupNestedNameSpecifier("A");
		ISymbol look = lookA.qualifiedLookup("a");
		assertEquals( look, a );
		
		look = lookA.qualifiedLookup("b");
		assertEquals( look, b );
		
		IContainerSymbol lookB = f.lookupNestedNameSpecifier("B");
		look = lookB.qualifiedLookup("a");
		assertEquals( look, a );
		
		look = lookB.qualifiedLookup("b");
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
		nsA.setType( TypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
			
		IContainerSymbol nsB = table.newContainerSymbol( "B" );
		nsB.setType( TypeInfo.t_namespace );
		compUnit.addSymbol( nsB );
		nsB.addUsingDirective( nsA );
		
		nsA.addUsingDirective( nsB );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		compUnit.addSymbol(f);
		f.addUsingDirective(nsA);
		f.addUsingDirective(nsB);
		
		ISymbol look = f.lookup("i");
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
		nsA.setType( TypeInfo.t_namespace );
		
		compUnit.addSymbol( nsA );
		
		IContainerSymbol structX = table.newContainerSymbol("x");
		structX.setType( TypeInfo.t_struct );
		nsA.addSymbol( structX );
		
		ISymbol intX = table.newSymbol("x");
		intX.setType( TypeInfo.t_int );
		nsA.addSymbol( intX );
		
		ISymbol intY = table.newSymbol("y");
		intY.setType( TypeInfo.t_int );
		nsA.addSymbol( intY );

		IContainerSymbol nsB = table.newContainerSymbol("B");
		nsB.setType( TypeInfo.t_namespace );
		
		compUnit.addSymbol( nsB );
		IContainerSymbol structY = table.newContainerSymbol("y");
		structY.setType( TypeInfo.t_struct );
		nsB.addSymbol( structY );
		
		IContainerSymbol nsC = table.newContainerSymbol("C");
		nsC.setType( TypeInfo.t_namespace);
		compUnit.addSymbol( nsC );
		
		ISymbol look = nsC.lookup("A");
		assertEquals( look, nsA );
		nsC.addUsingDirective( nsA );
		
		look = nsC.lookup("B");
		assertEquals( look, nsB );
		nsC.addUsingDirective( nsB );
		
		//lookup C::x
		look = nsC.lookupNestedNameSpecifier("C");
		assertEquals( look, nsC );
		look = ((IContainerSymbol)look).qualifiedLookup( "x" );
		assertEquals( look, intX );
		
		//lookup C::y
		look = nsC.lookupNestedNameSpecifier("C");
		assertEquals( look, nsC );

		try{
			look = ((IContainerSymbol)look).qualifiedLookup( "y" );
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
		nsA.setType( TypeInfo.t_namespace );
		compUnit.addSymbol( nsA );
	
		IContainerSymbol nsB = table.newContainerSymbol( "B" );
		nsB.setType( TypeInfo.t_namespace );
		nsA.addSymbol( nsB );
	
		IParameterizedSymbol f1 = table.newParameterizedSymbol("f1");
		f1.setType( TypeInfo.t_function );
		nsB.addSymbol( f1 );
	
		nsA.addUsingDirective( nsB );
	
		IContainerSymbol lookA = compUnit.lookupNestedNameSpecifier( "A" );
		assertEquals( nsA, lookA );
	
		ISymbol look = lookA.lookupMemberForDefinition( "f1" );
		assertEquals( look, null );
	
		//but notice if you wanted to do A::f1 as a function call, it is ok
		look = lookA.qualifiedLookup( "f1" );
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
		B.setType( TypeInfo.t_struct );
		compUnit.addSymbol( B );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		f.setType( TypeInfo.t_function );
		B.addSymbol( f );
	
		IContainerSymbol E = table.newContainerSymbol( "E" );
		E.setType( TypeInfo.t_enumeration );
		B.addSymbol( E );
		
		ISymbol e = table.newSymbol( "e" );
		e.setType( TypeInfo.t_enumerator );
		E.addSymbol( e );
		
		/**
		 * TBD: Anonymous unions are not yet implemented
		 */
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol( "C" );
		C.setType( TypeInfo.t_class );
		compUnit.addSymbol( C );
		
		IParameterizedSymbol g = table.newParameterizedSymbol( "g" );
		g.setType( TypeInfo.t_function );
		C.addSymbol( g );
		
		IDerivableContainerSymbol D = table.newDerivableContainerSymbol( "D" );
		D.setType( TypeInfo.t_struct );
		ISymbol look = compUnit.lookup( "B" );
		assertEquals( look, B );
		D.addParent( B );
		
		compUnit.addSymbol( D );
		
		IContainerSymbol lookB = D.lookupNestedNameSpecifier("B");
		assertEquals( lookB, B );

		D.addUsingDeclaration( "f", lookB );
		D.addUsingDeclaration( "e", lookB );
		  
		//TBD anonymous union
		//D.addUsingDeclaration( "x", lookB );
		
		look = D.lookupNestedNameSpecifier("C");
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
		
		IContainerSymbol A = table.newContainerSymbol( "A", TypeInfo.t_namespace );
		compUnit.addSymbol( A );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f1.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		f1.addParameter( TypeInfo.t_int, 0, null, false );
		A.addSymbol( f1 );
		
		ISymbol look = compUnit.lookupNestedNameSpecifier("A");
		assertEquals( look, A );
		
		IParameterizedSymbol usingF = (IParameterizedSymbol) compUnit.addUsingDeclaration( "f", A );
		
		look = compUnit.lookup("A");
		assertEquals( look, A );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol("f");
		f2.setType( TypeInfo.t_function );
		f2.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		f2.addParameter( TypeInfo.t_char, 0, null, false );
		
		A.addSymbol( f2 );
		
		IParameterizedSymbol foo = table.newParameterizedSymbol("foo");
		foo.setType( TypeInfo.t_function );
		compUnit.addSymbol( foo );

		LinkedList paramList = new LinkedList();
		TypeInfo param = new TypeInfo( TypeInfo.t_char, 0, null );
		paramList.add( param );
		
		look = foo.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, usingF );
		assertTrue( usingF.hasSameParameters( f1 ) );
		
		IParameterizedSymbol bar = table.newParameterizedSymbol( "bar" );
		bar.setType( TypeInfo.t_function );
		bar.addParameter( TypeInfo.t_char, 0, null, false );
		compUnit.addSymbol( bar );
		
		look = bar.lookupNestedNameSpecifier( "A" );
		assertEquals( look, A );
		bar.addUsingDeclaration( "f", A );
		
		look = bar.unqualifiedFunctionLookup( "f", paramList );
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
		
		IDerivableContainerSymbol cls = table.newDerivableContainerSymbol( "class", TypeInfo.t_class );
		
		IParameterizedSymbol fn = table.newParameterizedSymbol("function");
		fn.setType( TypeInfo.t_function );
		fn.getTypeInfo().addPtrOperator( new PtrOp( PtrOp.t_undef, true, false ) );
		//fn.setCVQualifier( ParserSymbolTable.TypeInfo.cvConst );
		
		table.getCompilationUnit().addSymbol( cls );
		cls.addSymbol( fn );
		
		ISymbol look = fn.lookup("this");
		assertTrue( look != null );
		
		assertEquals( look.getType(), TypeInfo.t_type );
		assertEquals( look.getTypeSymbol(), cls );
		assertEquals( ((PtrOp)look.getPtrOperators().iterator().next()).getType(), TypeInfo.PtrOp.t_pointer );
		assertTrue( ((PtrOp)look.getPtrOperators().iterator().next()).isConst() );
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
		cls.setType( TypeInfo.t_class );
		
		IContainerSymbol enumeration = table.newContainerSymbol("enumeration");
		enumeration.setType( TypeInfo.t_enumeration );
		
		table.getCompilationUnit().addSymbol( cls );
		cls.addSymbol( enumeration );
		
		ISymbol enumerator = table.newSymbol( "enumerator" );
		enumerator.setType( TypeInfo.t_enumerator );
		enumeration.addSymbol( enumerator );
		
		ISymbol look = cls.lookup( "enumerator" );
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
		NS.setType( TypeInfo.t_namespace );
		
		compUnit.addSymbol( NS );
		
		IDerivableContainerSymbol T = table.newDerivableContainerSymbol("T");
		T.setType( TypeInfo.t_class );
		
		NS.addSymbol( T );
		
		IParameterizedSymbol f = table.newParameterizedSymbol("f");
		f.setType( TypeInfo.t_function );
		f.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		
		ISymbol look = NS.lookup( "T" );
		assertEquals( look, T );				
		f.addParameter( look, null, false );
		
		NS.addSymbol( f );	
				
		look = compUnit.lookupNestedNameSpecifier( "NS" );
		assertEquals( look, NS );
		look = NS.qualifiedLookup( "T" );
		assertEquals( look, T );
		
		ISymbol param = table.newSymbol("parm");
		param.setType( TypeInfo.t_type );
		param.setTypeSymbol( look );
		compUnit.addSymbol( param );
		
		IParameterizedSymbol main = table.newParameterizedSymbol("main");
		main.setType( TypeInfo.t_function );
		main.setReturnType( table.newSymbol( "", TypeInfo.t_int ) );
		compUnit.addSymbol( main );

		LinkedList paramList = new LinkedList();
		look = main.lookup( "parm" );
		assertEquals( look, param );
		TypeInfo p = new TypeInfo( TypeInfo.t_type, 0, look );
		paramList.add( p );
		
		look = main.unqualifiedFunctionLookup( "f", paramList );
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
		NS1.setType( TypeInfo.t_namespace );
		 
		compUnit.addSymbol( NS1 );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f1.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		f1.addParameter( TypeInfo.t_void, 0, new PtrOp( PtrOp.t_pointer ), false );
		NS1.addSymbol( f1 );
		
		IContainerSymbol NS2 = table.newContainerSymbol( "NS2" );
		NS2.setType( TypeInfo.t_namespace );
		
		compUnit.addSymbol( NS2 );
		
		ISymbol look = NS2.lookup( "NS1" );
		assertEquals( look, NS1 );
		NS2.addUsingDirective( NS1 );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" );
		B.setType( TypeInfo.t_class );
		NS2.addSymbol( B );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( TypeInfo.t_function );
		f2.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		f2.addParameter( TypeInfo.t_void, 0, new PtrOp( PtrOp.t_pointer ), false );
		NS2.addSymbol( f2 );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" );
		A.setType( TypeInfo.t_class );
		look = compUnit.lookupNestedNameSpecifier( "NS2" );
		assertEquals( look, NS2 );
		
		look = NS2.qualifiedLookup( "B" );
		assertEquals( look, B );
		A.addParent( B );
		
		compUnit.addSymbol( A );
		
		look = compUnit.lookup( "A" );
		assertEquals( look, A );
		ISymbol a = table.newSymbol( "a" );
		a.setType( TypeInfo.t_type );
		a.setTypeSymbol( look );
		compUnit.addSymbol( a );
		
		LinkedList paramList = new LinkedList();
		look = compUnit.lookup( "a" );
		assertEquals( look, a );
		TypeInfo param = new TypeInfo( look.getType(), 0, look, null, false );
		//new PtrOp( PtrOp.t_reference )
		param.addOperatorExpression( OperatorExpression.addressof );
		paramList.add( param );
		
		look = compUnit.unqualifiedFunctionLookup( "f", paramList );
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
		C.setType( TypeInfo.t_class );
		compUnit.addSymbol(C);
				
		IParameterizedSymbol f1 = table.newParameterizedSymbol("foo");
		f1.setType( TypeInfo.t_function );
		f1.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		f1.addParameter( TypeInfo.t_int, 0, null, false );
		C.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol("foo");
		f2.setType( TypeInfo.t_function );
		f2.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		f2.addParameter( TypeInfo.t_int, 0, null, false );
		f2.addParameter( TypeInfo.t_char, 0, null, false );
		C.addSymbol( f2 );
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol("foo");
		f3.setType( TypeInfo.t_function );
		f3.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		f3.addParameter( TypeInfo.t_int, 0, null, false );
		f3.addParameter( TypeInfo.t_char, 0, null, false );
		f3.addParameter( C, new PtrOp( PtrOp.t_pointer ), false );
		C.addSymbol( f3 );
		
		ISymbol look = compUnit.lookup("C");
		assertEquals( look, C );
		
		ISymbol c = table.newSymbol("c");
		c.setType( TypeInfo.t_type );
		c.setTypeSymbol( look );
		c.addPtrOperator( new PtrOp( PtrOp.t_pointer, false, false ) );
		compUnit.addSymbol( c );
		
		look = compUnit.lookup( "c" );
		assertEquals( look, c );
		assertEquals( look.getTypeSymbol(), C );
		
		LinkedList paramList = new LinkedList();
															  
		TypeInfo p1 = new TypeInfo( TypeInfo.t_int, 0, null );
		TypeInfo p2 = new TypeInfo( TypeInfo.t_char, 0, null );
		TypeInfo p3 = new TypeInfo( TypeInfo.t_type, 0, c );
		
		paramList.add( p1 );
		look = C.memberFunctionLookup( "foo", paramList );
		assertEquals( look, f1 );
		
		paramList.add( p2 );
		look = C.memberFunctionLookup( "foo", paramList );
		assertEquals( look, f2 );
				
		paramList.add( p3 );
		look = C.memberFunctionLookup( "foo", paramList );
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
		f1.setType( TypeInfo.t_function );
		f1.addParameter( TypeInfo.t_int, 0, null, false );
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol("f");
		f2.setType( TypeInfo.t_function );
		f2.addParameter( TypeInfo.t_char, 0, null, true );
		compUnit.addSymbol( f2 );
		
		LinkedList paramList = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_int, 0, null );
		paramList.add( p1 );
		
		ISymbol look = compUnit.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		paramList.clear();
		TypeInfo p2 = new TypeInfo( TypeInfo.t_char, 0, null );
		paramList.add( p2 );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		paramList.clear();
		TypeInfo p3 = new TypeInfo( TypeInfo.t_bool, 0, null );
		paramList.add( p3 );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		look = compUnit.unqualifiedFunctionLookup( "f", null );
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
		A.setType( TypeInfo.t_class );
		compUnit.addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" );
		B.setType( TypeInfo.t_class );
		B.addParent( A );
		compUnit.addSymbol( B );
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol( "C" );
		C.setType( TypeInfo.t_class );
		C.addParent( B );
		compUnit.addSymbol( C );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" );
		f1.setType( TypeInfo.t_function );
		f1.addParameter( A, new PtrOp( PtrOp.t_pointer ), false );
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( TypeInfo.t_function );
		f2.addParameter( B, new PtrOp( PtrOp.t_pointer ), false );
		compUnit.addSymbol( f2 );
		
		ISymbol a = table.newSymbol( "a" );
		a.setType( TypeInfo.t_type );
		a.setTypeSymbol( A );
		a.addPtrOperator( new PtrOp( PtrOp.t_pointer, false, false ) );
		
		ISymbol c = table.newSymbol( "c" );
		c.setType( TypeInfo.t_type );
		c.setTypeSymbol( C );
		c.addPtrOperator( new PtrOp( PtrOp.t_pointer, false, false ) );
		
		LinkedList paramList = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_type, 0, a );
		paramList.add( p1 );
		ISymbol look = compUnit.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		paramList.clear();
		TypeInfo p2 = new TypeInfo( TypeInfo.t_type, 0, c );
		paramList.add( p2 );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
	}
	
	/**
	 * 
	 * @throws Exception
	 *
	 * class A {};
	 * typedef B A *;
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
		A.setType( TypeInfo.t_class );
		compUnit.addSymbol( A );
		
		ISymbol B = table.newSymbol( "B" );
		B.setType( TypeInfo.t_type );
		B.setTypeSymbol( A );
		B.getTypeInfo().setBit( true, TypeInfo.isTypedef );
		B.addPtrOperator( new PtrOp( PtrOp.t_pointer, false, false ) );
		compUnit.addSymbol( B );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" );
		f1.setType( TypeInfo.t_function );
		f1.addParameter( A, new PtrOp( PtrOp.t_pointer ), false );
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( TypeInfo.t_function );
		f2.addParameter( A, null, false );
		compUnit.addSymbol( f2 );

		ISymbol a = table.newSymbol( "a" );
		a.setType( TypeInfo.t_type );
		a.setTypeSymbol( A );
		compUnit.addSymbol( a );
				
		ISymbol b = table.newSymbol( "b" );
		b.setType( TypeInfo.t_type );
		b.setTypeSymbol( B );
		compUnit.addSymbol( b );
		
		ISymbol array = table.newSymbol( "array" );
		array.setType( TypeInfo.t_type );
		array.setTypeSymbol( A );
		array.addPtrOperator( new PtrOp( PtrOp.t_array, false, false ) );
				
		LinkedList paramList = new LinkedList();
		TypeInfo p = new TypeInfo( TypeInfo.t_type, 0, a );
		paramList.add( p );
		
		ISymbol look = compUnit.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		p.addOperatorExpression( OperatorExpression.addressof );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		p.setTypeSymbol( b );
		p.getOperatorExpressions().clear();
		look = compUnit.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f1 );
		
		p.addOperatorExpression( OperatorExpression.indirection );
		look = compUnit.unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f2 );
		
		p.setTypeSymbol( array );
		p.getOperatorExpressions().clear();
		look = compUnit.unqualifiedFunctionLookup( "f", paramList );
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
		A.setType( TypeInfo.t_class );
		compUnit.addSymbol( A );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B" );
		B.setType( TypeInfo.t_class );
		compUnit.addSymbol( B );
		
		IParameterizedSymbol constructor = table.newParameterizedSymbol("B");
		constructor.setType( TypeInfo.t_constructor );
		constructor.addParameter( A, null, false );
		B.addConstructor( constructor );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f" );
		f.setType( TypeInfo.t_function );
		f.addParameter( B, null, false );
		compUnit.addSymbol( f );
		
		ISymbol a = table.newSymbol( "a" );
		a.setType( TypeInfo.t_type );
		a.setTypeSymbol( A );
		compUnit.addSymbol( a );
		
		LinkedList paramList = new LinkedList();
		TypeInfo p = new TypeInfo( TypeInfo.t_type, 0, a );
		paramList.add( p );
		
		ISymbol look = compUnit.unqualifiedFunctionLookup( "f", paramList );
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
		f1.setType( TypeInfo.t_function );
		f1.addParameter( TypeInfo.t_int, 0, new PtrOp( PtrOp.t_pointer, true, false ), false );
		f1.addParameter( TypeInfo.t_int, TypeInfo.isShort, null, false );
		
		compUnit.addSymbol( f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( TypeInfo.t_function );
		f2.addParameter( TypeInfo.t_int, 0, new PtrOp( PtrOp.t_pointer ), false );
		f2.addParameter( TypeInfo.t_int, 0, null, false );
		compUnit.addSymbol( f2 );
		
		ISymbol i = table.newSymbol( "i" );
		i.setType( TypeInfo.t_int );
		compUnit.addSymbol( i );
		
		ISymbol s = table.newSymbol( "s" );
		s.setType( TypeInfo.t_int );
		s.getTypeInfo().setBit( true, TypeInfo.isShort );
		compUnit.addSymbol( s );
		
		IParameterizedSymbol main = table.newParameterizedSymbol( "main" );
		main.setType( TypeInfo.t_function );
		compUnit.addSymbol( main );
		
		LinkedList params = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_type, 0, i );
		p1.addOperatorExpression( OperatorExpression.addressof );
		TypeInfo p2 = new TypeInfo( TypeInfo.t_type, 0, s );
		params.add( p1 );
		params.add( p2 );
		
		ISymbol look = null;
		
		try{
			look = main.unqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
		
		params.clear();
		TypeInfo p3 = new TypeInfo( TypeInfo.t_int, TypeInfo.isLong, null );
		params.add( p1 );
		params.add( p3 );
		look = main.unqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );
		
		params.clear();
		TypeInfo p4 = new TypeInfo( TypeInfo.t_char, 0, null );
		params.add( p1 );
		params.add( p4 );
		look = main.unqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );
		
		params.clear();
		p1.addPtrOperator( new PtrOp( PtrOp.t_undef, true, false ) );
		//((PtrOp)p1.getPtrOperators().iterator().next()).setConst( true );
		params.add( p1 );
		params.add( p3 );
		look = main.unqualifiedFunctionLookup( "f", params );
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
		B.setType( TypeInfo.t_class );
		
		compUnit.addSymbol( B );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A" );
		A.setType( TypeInfo.t_class );
		compUnit.addSymbol( A );
		
		IParameterizedSymbol constructA = table.newParameterizedSymbol( "A" );
		constructA.setType( TypeInfo.t_constructor );
		constructA.addParameter( B, new PtrOp( PtrOp.t_reference ), false );
		A.addConstructor( constructA );
		
		IParameterizedSymbol operator = table.newParameterizedSymbol( "operator A" );
		operator.setType( TypeInfo.t_function );
		B.addSymbol( operator );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f" );
		f1.setType( TypeInfo.t_function );
		f1.addParameter( A, null, false );
		compUnit.addSymbol( f1 );
		
		ISymbol b = table.newSymbol( "b" );
		b.setType( TypeInfo.t_type );
		b.setTypeSymbol( B );
		
		LinkedList params = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_type, 0, b );
		params.add( p1 );
		
		ISymbol look = null;
		
		try{
			look = compUnit.unqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		IDerivableContainerSymbol C = table.newDerivableContainerSymbol("C");
		C.setType( TypeInfo.t_class );
		compUnit.addSymbol( C );
		
		IParameterizedSymbol constructC = table.newParameterizedSymbol("C");
		constructC.setType( TypeInfo.t_constructor );
		constructC.addParameter( B, new PtrOp( PtrOp.t_reference ), false );
		C.addConstructor( constructC );

		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f" );
		f2.setType( TypeInfo.t_function );
		f2.addParameter(  C, null, false );
		compUnit.addSymbol( f2 );
		
		try{
			look = compUnit.unqualifiedFunctionLookup( "f", params );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous ); 
		}
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f" );
		f3.setType( TypeInfo.t_function );
		f3.addParameter(  B, null, false );
		compUnit.addSymbol( f3 );
		
		look = compUnit.unqualifiedFunctionLookup( "f", params );
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
		
		ISymbol look = A.lookup("f");
		assertEquals( look, f );
		
		assertTrue( table.rollBack( mark ) );
		
		look = A.lookup("f");
		assertEquals( look, null );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol("B");
		B.setType( TypeInfo.t_class );
		
		mark = table.setMark();
		table.getCompilationUnit().addSymbol( B );
		Mark mark2 = table.setMark();
		A.addParent( B );
		Mark mark3 = table.setMark();
		
		IParameterizedSymbol C = table.newParameterizedSymbol("C");
		C.addParameter( TypeInfo.t_class, 0, null, false );
		
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
		
		IParameterizedSymbol template = table.newParameterizedSymbol( "A", TypeInfo.t_template );
		ISymbol param = table.newSymbol( "T", TypeInfo.t_undef );
		template.addParameter( param );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		template.addSymbol( A );
		A.addParent( param );
		
		table.getCompilationUnit().addSymbol( template );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		ISymbol i = table.newSymbol( "i", TypeInfo.t_int );
		B.addSymbol( i );
		
		TypeInfo type = new TypeInfo( TypeInfo.t_class, 0, B );
		LinkedList args = new LinkedList();
		args.add( type );
		
		TemplateInstance instance = table.getCompilationUnit().templateLookup( "A", args );
		assertEquals( instance.getInstantiatedSymbol(), A );
		
		ISymbol a = table.newSymbol( "a", TypeInfo.t_type );
		a.setTypeSymbol( instance );
		
		table.getCompilationUnit().addSymbol( a );
		
		ISymbol look = table.getCompilationUnit().lookup( "a" );
		
		assertEquals( look, a );
		
		ISymbol symbol = a.getTypeSymbol();
		assertEquals( symbol, instance );

		look = ((IContainerSymbol)instance.getInstantiatedSymbol()).lookup( "i" );
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
		
		IParameterizedSymbol template = table.newParameterizedSymbol( "A", TypeInfo.t_template );
		ISymbol param = table.newSymbol( "T", TypeInfo.t_undef );
		template.addParameter( param );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		ISymbol t = table.newSymbol( "t", TypeInfo.t_type );
		
		ISymbol look = template.lookup( "T" );
		assertEquals( look, param );
		
		t.setTypeSymbol( param );
		
		template.addSymbol( A );
		A.addSymbol( t );
		table.getCompilationUnit().addSymbol( template );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		
		TypeInfo type = new TypeInfo( TypeInfo.t_int, 0 , null );
		LinkedList args = new LinkedList();
		args.add( type );
		
		look = table.getCompilationUnit().templateLookup( "A", args );
		assertTrue( look instanceof TemplateInstance );
		
		B.addParent( look );
		table.getCompilationUnit().addSymbol( B );
		
		ISymbol b = table.newSymbol( "b", TypeInfo.t_type );
		b.setTypeSymbol( B );
		table.getCompilationUnit().addSymbol( b );
		
		look = table.getCompilationUnit().lookup( "b" );
		assertEquals( look, b );
		
		look = ((IDerivableContainerSymbol) b.getTypeSymbol()).lookup( "t" );
		assertTrue( look instanceof TemplateInstance );
		
		TemplateInstance instance = (TemplateInstance) look;
		assertEquals( instance.getInstantiatedSymbol(), t );
		assertTrue( instance.isType( TypeInfo.t_int ) );
		
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
		
		IParameterizedSymbol template = table.newParameterizedSymbol( "X", TypeInfo.t_template );
		
		ISymbol paramT = table.newSymbol( "T", TypeInfo.t_undef );
		template.addParameter( paramT );
		
		ISymbol look = template.lookup( "T" );
		assertEquals( look, paramT );
		ISymbol paramU = table.newSymbol( "U", TypeInfo.t_undef );
		paramU.getTypeInfo().setDefault( new TypeInfo( TypeInfo.t_type, 0, look ) );
		template.addParameter( paramU );
		
		IDerivableContainerSymbol X = table.newDerivableContainerSymbol( "X", TypeInfo.t_class );
		template.addSymbol( X );
		
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
			
		table.getCompilationUnit().addSymbol( template );
		
		TypeInfo type = new TypeInfo( TypeInfo.t_char, 0, null );
		LinkedList args = new LinkedList();
		args.add( type );
		look = table.getCompilationUnit().templateLookup( "X", args );
		assertTrue( look instanceof TemplateInstance );
				
		TemplateInstance instance = (TemplateInstance) look;
		look = ((IDerivableContainerSymbol) instance.getInstantiatedSymbol()).lookup( "t" );
		
		assertTrue( look instanceof TemplateInstance );
		assertTrue( ((TemplateInstance) look).isType( TypeInfo.t_char ) );
		
		look = ((IDerivableContainerSymbol) instance.getInstantiatedSymbol()).lookup( "u" );
		assertTrue( look instanceof TemplateInstance );
		assertTrue( ((TemplateInstance) look).isType( TypeInfo.t_char ) );
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
		
		IParameterizedSymbol template = table.newParameterizedSymbol( "A", TypeInfo.t_template );
		ISymbol paramT = table.newSymbol( "T", TypeInfo.t_undef );
		template.addParameter( paramT );
		
		IDerivableContainerSymbol A = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		template.addSymbol( A );
		
		ISymbol t = table.newSymbol( "t", TypeInfo.t_type );
		t.setTypeSymbol( paramT );
		A.addSymbol( t );
		
		table.getCompilationUnit().addSymbol( template );
		
		IDerivableContainerSymbol B = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( B );
		
		IParameterizedSymbol temp = (IParameterizedSymbol) table.getCompilationUnit().lookup( "A" );
		assertEquals( temp, template );
		
		LinkedList args = new LinkedList();
		TypeInfo arg = new TypeInfo( TypeInfo.t_type, 0, B );
		args.add( arg );
				
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f1.addParameter( TypeInfo.t_char, 0, null, false );
		table.getCompilationUnit().addSymbol( f1 );
				
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f2.addParameter( temp.instantiate( args ), null, false );
		table.getCompilationUnit().addSymbol( f2 );
		
		IParameterizedSymbol f3 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f3.addParameter( TypeInfo.t_int, 0, null, false );
		table.getCompilationUnit().addSymbol( f3 );
		
		ISymbol a = table.newSymbol( "a", TypeInfo.t_type );
		a.setTypeSymbol( temp.instantiate( args ) );
		table.getCompilationUnit().addSymbol( a );

		LinkedList params = new LinkedList();
		params.add( new TypeInfo( TypeInfo.t_type, 0, a ) );
				
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", params );
		assertEquals( look, f2 );		
			
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
	//TODO
	public void incompletetestTemplateSpecialization() throws Exception{
		newTable();
		
		IDerivableContainerSymbol cls1 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol cls2 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol cls3 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol cls4 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol cls5 = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		
		IParameterizedSymbol template1 = table.newParameterizedSymbol( "A", TypeInfo.t_template );
		ISymbol T1p1 = table.newSymbol( "T1", TypeInfo.t_undef );
		ISymbol T1p2 = table.newSymbol( "T2", TypeInfo.t_undef );
		ISymbol T1p3 = table.newSymbol( "I", TypeInfo.t_int );
		template1.addParameter( T1p1 );
		template1.addParameter( T1p2 );
		template1.addParameter( T1p3 );
		template1.addSymbol( cls1 );
		table.getCompilationUnit().addSymbol( template1 );
		
		IParameterizedSymbol template2 = table.newParameterizedSymbol( "A", TypeInfo.t_template );
		ISymbol T2p1 = table.newSymbol( "T", TypeInfo.t_undef );
		ISymbol T2p2 = table.newSymbol( "I", TypeInfo.t_int );
		template2.addParameter( T2p1 );
		template2.addParameter( T2p2 );
		ISymbol T2a1 = table.newSymbol( "T", TypeInfo.t_undef );
		ISymbol T2a2 = table.newSymbol( "T", TypeInfo.t_undef );
		T2a2.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		ISymbol T2a3 = table.newSymbol( "I", TypeInfo.t_int );
		template2.addArgument( T2a1 );
		template2.addArgument( T2a2 );
		template2.addArgument( T2a3 );
		template2.addSymbol( cls2 );
		template1.addSpecialization( template2 );
		
		IParameterizedSymbol template3 = table.newParameterizedSymbol( "A", TypeInfo.t_template );
		ISymbol T3p1 = table.newSymbol( "T1", TypeInfo.t_undef );
		ISymbol T3p2 = table.newSymbol( "T2", TypeInfo.t_undef );
		ISymbol T3p3 = table.newSymbol( "I", TypeInfo.t_int );
		template3.addParameter( T3p1 );
		template3.addParameter( T3p2 );
		template3.addParameter( T3p3 );
		ISymbol T3a1 = table.newSymbol( "T1", TypeInfo.t_undef );
		T3a1.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		ISymbol T3a2 = table.newSymbol( "T2", TypeInfo.t_undef );
		ISymbol T3a3 = table.newSymbol( "I", TypeInfo.t_int );
		template3.addArgument( T3a1 );
		template3.addArgument( T3a2 );
		template3.addArgument( T3a3 );
		template3.addSymbol( cls3 );
		template1.addSpecialization( template3 );
				
		IParameterizedSymbol template4 = table.newParameterizedSymbol( "A", TypeInfo.t_template );
		ISymbol T4p1 = table.newSymbol( "T", TypeInfo.t_undef );
		template4.addParameter( T4p1 );
		
		ISymbol T4a1 = table.newSymbol( "", TypeInfo.t_int );
		ISymbol T4a2 = table.newSymbol( "T", TypeInfo.t_undef );
		T4a2.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		ISymbol T4a3 = table.newSymbol( "", TypeInfo.t_int );
		T4a3.getTypeInfo().setDefault( new Integer(5) );
		template4.addArgument( T4a1 );
		template4.addArgument( T4a2 );
		template4.addArgument( T4a3 );
		template4.addSymbol( cls4 );
		template1.addSpecialization( template4 );
				
		IParameterizedSymbol template5 = table.newParameterizedSymbol( "A", TypeInfo.t_template );
		ISymbol T5p1 = table.newSymbol( "T1", TypeInfo.t_undef );
		ISymbol T5p2 = table.newSymbol( "T2", TypeInfo.t_undef );
		ISymbol T5p3 = table.newSymbol( "I", TypeInfo.t_int );
		template5.addParameter( T5p1 );
		template5.addParameter( T5p2 );
		template5.addParameter( T5p3 );
		ISymbol T5a1 = table.newSymbol( "T1", TypeInfo.t_undef );
		ISymbol T5a2 = table.newSymbol( "T2", TypeInfo.t_undef );
		T5a1.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		ISymbol T5a3 = table.newSymbol( "I", TypeInfo.t_int );
		template5.addArgument( T5a1 );
		template5.addArgument( T5a2 );
		template5.addArgument( T5a3 );
		template5.addSymbol( cls5 );
		template1.addSpecialization( template5 );
		
		IParameterizedSymbol a = (IParameterizedSymbol) table.getCompilationUnit().lookup( "A" );
		
		LinkedList args = new LinkedList();
		
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, new Integer(1) ) );
		
		TemplateInstance a1 = a.instantiate( args );
		assertEquals( a1.getInstantiatedSymbol(), cls1 );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, new Integer(1) ) );
		
		TemplateInstance a2 = a.instantiate( args );
		assertEquals( a2.getInstantiatedSymbol(), cls2 );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_char, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, new Integer(5) ) );
		TemplateInstance a3 = a.instantiate( args );
		assertEquals( a3.getInstantiatedSymbol(), cls4 );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		args.add( new TypeInfo( TypeInfo.t_char, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, new Integer(1) ) );
		TemplateInstance a4 = a.instantiate( args );
		assertEquals( a4.getInstantiatedSymbol(), cls5 );
		
		args.clear();
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, new PtrOp( PtrOp.t_pointer ), false ) );
		args.add( new TypeInfo( TypeInfo.t_int, 0, null, null, new Integer(2) ) );
		
		try{
			a.instantiate( args );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}
	
	/**
	 * class A;
	 *
	 * A * a;
	 *
	 * class A {};
	 *
	 * @throws Exception
	 */
	public void testForwardClassDeclaration() throws Exception{
		newTable();
		
		ISymbol forwardSymbol = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		forwardSymbol.setIsForwardDeclaration( true );
		
		table.getCompilationUnit().addSymbol( forwardSymbol );
		
		/*...*/
		
		ISymbol lookup = table.getCompilationUnit().lookup( "A" );
		ISymbol otherLookup = table.getCompilationUnit().elaboratedLookup( TypeInfo.t_class, "A" );
		
		assertEquals( lookup, otherLookup );
		assertEquals( lookup, forwardSymbol );
		
		ISymbol a = table.newSymbol( "a", TypeInfo.t_type );
		a.setTypeSymbol( forwardSymbol );
		a.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		
		table.getCompilationUnit().addSymbol( a );
		
		/*...*/
		
		lookup = table.getCompilationUnit().lookup( "A" );
		IDerivableContainerSymbol classA = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		assertTrue( lookup.isForwardDeclaration() );
		lookup.setTypeSymbol( classA ); 
		
		table.getCompilationUnit().addSymbol( classA );
		
		lookup = table.getCompilationUnit().lookup( "a" );
		assertEquals( lookup, a );
		assertEquals( a.getTypeSymbol(), classA );
		
		lookup = table.getCompilationUnit().lookup( "A" );
		assertEquals( lookup, classA );
		
		lookup = table.getCompilationUnit().elaboratedLookup( TypeInfo.t_class, "A" );
		assertEquals( lookup, classA );
	}
	
	/**
	 * class A;
	 * 
	 * class B {
	 *    static void f( A * );
	 *    static void f( int );
	 * };
	 * 
	 * A* a1;
	 * 
	 * class A {};
	 * 
	 * void B::f( A * ) {}
	 * void B::f( int ) {}
	 * 
	 * A* a2;
	 * 
	 * B::f( a1 );
	 * B::f( a2 );
	 * 
	 * @throws Exception
	 */
	public void testForwardDeclarationUsedAsFunctionParam() throws Exception{
		newTable();
		
		ISymbol forwardSymbol = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		forwardSymbol.setIsForwardDeclaration( true );
		table.getCompilationUnit().addSymbol( forwardSymbol );
		
		/*...*/
	
		IDerivableContainerSymbol classB = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
			
		IParameterizedSymbol fn1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		ISymbol lookup = table.getCompilationUnit().lookup( "A" );
		assertEquals( lookup, forwardSymbol );
		fn1.addParameter( lookup, new PtrOp( PtrOp.t_pointer ), false );
		fn1.getTypeInfo().setBit( true, TypeInfo.isStatic );
		classB.addSymbol( fn1 );
		
		IParameterizedSymbol fn2 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		fn2.addParameter( TypeInfo.t_int, 0, null, false );
		fn2.getTypeInfo().setBit( true, TypeInfo.isStatic );
		classB.addSymbol( fn2 );
		
		table.getCompilationUnit().addSymbol( classB );
		
		/*...*/
		
		ISymbol a1 = table.newSymbol( "a1", TypeInfo.t_type );
		lookup = table.getCompilationUnit().lookup( "A" );
		assertEquals( lookup, forwardSymbol );
		a1.setTypeSymbol( lookup );
		a1.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		
		table.getCompilationUnit().addSymbol( a1 );
		
		/*...*/
		
		lookup = table.getCompilationUnit().lookup( "A" );
		IDerivableContainerSymbol classA = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		assertTrue( lookup.isForwardDeclaration() );
		lookup.setTypeSymbol( classA ); 
		table.getCompilationUnit().addSymbol( classA );
		
		/*..*/
		ISymbol a2 = table.newSymbol( "a2", TypeInfo.t_type );
		lookup = table.getCompilationUnit().lookup( "A" );
		assertEquals( lookup, classA );
		a2.setTypeSymbol( lookup );
		a2.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		
		table.getCompilationUnit().addSymbol( a2 );
		
		/*..*/
		
		LinkedList paramList = new LinkedList();
		TypeInfo p1 = new TypeInfo( TypeInfo.t_type, 0, a1 );
		paramList.add( p1 );
		ISymbol look = classB.memberFunctionLookup( "f", paramList );
		assertEquals( look, fn1 );
		
		paramList.clear();
		p1 = new TypeInfo( TypeInfo.t_type, 0, a2 );
		paramList.add( p1 );
		look = classB.memberFunctionLookup( "f", paramList );
		assertEquals( look, fn1 );
	}
	
	public void testConstructors() throws Exception{
		newTable();
		
		IDerivableContainerSymbol classA = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		
		IParameterizedSymbol constructor1 = table.newParameterizedSymbol( "A", TypeInfo.t_constructor );
		constructor1.addParameter( classA, new PtrOp( PtrOp.t_reference ), false );
		
		IParameterizedSymbol constructor2 = table.newParameterizedSymbol( "A", TypeInfo.t_constructor );
		constructor2.addParameter( TypeInfo.t_int, 0, null, false );
		
		IParameterizedSymbol constructor3 = table.newParameterizedSymbol( "A", TypeInfo.t_constructor );
		constructor3.addParameter( TypeInfo.t_char, 0, null, false );
		
		classA.addConstructor( constructor1 );
		classA.addConstructor( constructor2 );
		classA.addConstructor( constructor3 );
		
		assertEquals( classA.getConstructors().size(), 3 );
		
		IParameterizedSymbol cloned = (IParameterizedSymbol) constructor2.clone();
		try{
			classA.addConstructor( cloned );
		} catch ( ParserSymbolTableException e ) {
			assertEquals( e.reason, ParserSymbolTableException.r_InvalidOverload );
		}
		
		LinkedList paramList = new LinkedList();
		paramList.add( new TypeInfo( TypeInfo.t_int, 0, null ) );
		
		ISymbol lookup = classA.lookupConstructor( paramList );
		
		assertEquals( lookup, constructor2 );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * namespace A
	 * { 
	 *    int x;
	 * }
	 * namespace B = A;
	 * 
	 * ++B::x;
	 */
	public void testNamespaceAlias() throws Exception{
		newTable();
		
		IContainerSymbol NSA = table.newContainerSymbol( "A", TypeInfo.t_namespace );
		table.getCompilationUnit().addSymbol( NSA );
		
		ISymbol x = table.newSymbol( "x", TypeInfo.t_int );
		NSA.addSymbol( x );
		
		IContainerSymbol NSB = table.newContainerSymbol( "B", TypeInfo.t_namespace );
		NSB.setTypeSymbol( NSA );  //alias B to A
		
		table.getCompilationUnit().addSymbol( NSB );
		
		ISymbol lookup = table.getCompilationUnit().lookup( "B" );
		assertEquals( lookup, NSB );
		
		lookup = NSB.lookup( "x" );
		assertEquals( lookup, x );
	}
	
	/**
	 * 
	 * @throws Exception
	 * namespace A 
	 * {
	 *   void f( );
	 * }
	 * namespace B = A;
	 * 
	 * B::f();
	 * 
	 * using namespace B;
	 * f();
	 */
	public void testUsingNamespaceAlias() throws Exception{
		newTable();
		
		IContainerSymbol NSA = table.newContainerSymbol( "A", TypeInfo.t_namespace );
		table.getCompilationUnit().addSymbol( NSA );
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		
		NSA.addSymbol( f );
		
		IContainerSymbol NSB = table.newContainerSymbol( "B", TypeInfo.t_namespace );
		NSB.setTypeSymbol( NSA );
		table.getCompilationUnit().addSymbol( NSB );
		
		//look for function that has no parameters
		LinkedList paramList = new LinkedList();
		ISymbol look = NSB.qualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f );
		
		table.getCompilationUnit().addUsingDirective( NSB );
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", paramList );
		assertEquals( look, f );
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * The general rule is that when you set a TypeInfo's type to be t_type, you 
	 * should set the type symbol to be something.  This is to test that the function
	 * resolution can handle a bad typeInfo that has a null symbol without throwing a NPE
	 */
	public void testBadParameterInfo() throws Exception{
		newTable();
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f.setReturnType( table.newSymbol( "", TypeInfo.t_void ) );
		
		IDerivableContainerSymbol a = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( a );
		
		f.addParameter( a, null, false );
		
		table.getCompilationUnit().addSymbol( f );
		
		LinkedList paramList = new LinkedList ();
		
		TypeInfo param = new TypeInfo( TypeInfo.t_type, 0, null );
		
		paramList.add( param );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", paramList );
		
		assertEquals( look, null );
		
		ISymbol intermediate = table.newSymbol( "", TypeInfo.t_type );
		
		param.setTypeSymbol( intermediate );
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", paramList );
		
		assertEquals( look, null );
	}
	
	/**
	 * class A {
	 *    A ( C ) {};
	 * } a;
	 * class B : public A {} b;
	 * class C {
	 *    C ( A ) {};
	 * } c;
	 * 
	 * isTrue ? &a : &b;	//expect type = 2nd operand ( A )
	 * isTrue ? &a : &c;	//expect null, neither converts
	 * isTrue ? a : c;		//expect exception, both convert 
	 * 
	 * @throws Exception
	 */
	public void testGetConditionalOperand_bug43106() throws Exception{
		newTable();
		
		IDerivableContainerSymbol clsA = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol clsB = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		
		clsB.addParent( clsA );
		
		table.getCompilationUnit().addSymbol( clsA );
		table.getCompilationUnit().addSymbol( clsB );
		
		ISymbol a = table.newSymbol( "a", TypeInfo.t_type );
		a.setTypeSymbol( clsA );
		
		ISymbol b = table.newSymbol( "b", TypeInfo.t_type );
		b.setTypeSymbol( clsB );
		
		table.getCompilationUnit().addSymbol( a );
		table.getCompilationUnit().addSymbol( b );
		
		TypeInfo secondOp = new TypeInfo( TypeInfo.t_type, 0, a );
		secondOp.addOperatorExpression( OperatorExpression.addressof );
		TypeInfo thirdOp = new TypeInfo( TypeInfo.t_type, 0, b );
		thirdOp.addOperatorExpression( OperatorExpression.addressof );
		
		TypeInfo returned = ParserSymbolTable.getConditionalOperand( secondOp, thirdOp );
		assertEquals( returned, secondOp );
		
		IDerivableContainerSymbol clsC = table.newDerivableContainerSymbol( "C", TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( clsC );
		ISymbol c = table.newSymbol( "c", TypeInfo.t_type );
		c.setTypeSymbol( clsC );
		table.getCompilationUnit().addSymbol( c );
		
		TypeInfo anotherOp = new TypeInfo( TypeInfo.t_type, 0, c );
		anotherOp.addOperatorExpression( OperatorExpression.addressof );
		
		returned = ParserSymbolTable.getConditionalOperand( secondOp, anotherOp );
		assertEquals( returned, null );
		
		IParameterizedSymbol constructorA = table.newParameterizedSymbol( "A", TypeInfo.t_constructor );
		constructorA.addParameter( clsC, null, false );
		clsA.addConstructor( constructorA );
		
		IParameterizedSymbol constructorC = table.newParameterizedSymbol( "C", TypeInfo.t_constructor );
		constructorC.addParameter( clsA, null, false );
		clsC.addConstructor( constructorC );
		
		secondOp.getOperatorExpressions().clear();
		anotherOp.getOperatorExpressions().clear();
		try{
			
			returned = ParserSymbolTable.getConditionalOperand( secondOp, anotherOp );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			//good
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 * class A {};
	 * class B : public A {} b;
	 * class C : private A {} c;
	 * int f ( A & );
	 * 
	 * int i = f ( b );  //calls f( A & );
	 * 
	 * int f ( B & );
	 * i = f( b );   	//now calls f( B& );
	 * 
	 * i = f( c );	//exception, A is not an accessible base class
	 */
	public void testDerivedReference() throws Exception{
		newTable();
		
		IDerivableContainerSymbol clsA = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		IDerivableContainerSymbol clsB = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		IDerivableContainerSymbol clsC = table.newDerivableContainerSymbol( "C", TypeInfo.t_class );
		
		clsB.addParent( clsA );
		clsC.addParent( clsA, false, ASTAccessVisibility.PRIVATE, 0, null );
		
		ISymbol b = table.newSymbol("b", TypeInfo.t_type );
		b.setTypeSymbol( clsB );
		
		ISymbol c = table.newSymbol("c", TypeInfo.t_type );
		c.setTypeSymbol( clsC );
		
		table.getCompilationUnit().addSymbol( clsA );
		table.getCompilationUnit().addSymbol( clsB );
		table.getCompilationUnit().addSymbol( clsC );
		table.getCompilationUnit().addSymbol( b );
		table.getCompilationUnit().addSymbol( c );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f1.addParameter( clsA, new PtrOp( PtrOp.t_reference ), false );
		table.getCompilationUnit().addSymbol( f1 );
		
		LinkedList parameters = new LinkedList();
		TypeInfo param = new TypeInfo( TypeInfo.t_type, 0, b );
		parameters.add( param );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters );
		assertEquals( look, f1 );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f2.addParameter( clsB, new PtrOp( PtrOp.t_reference ), false );
		table.getCompilationUnit().addSymbol( f2 );
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters );
		assertEquals( look, f2 );
		
		parameters.clear();
		param = new TypeInfo( TypeInfo.t_type, 0, c );
		parameters.add( param );
		try{
			look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			//good
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * class A {
	 * private : 
	 *    A ( const A & ) {}
	 * } a;
	 * 
	 * class B : public A {
	 * } b;
	 * 
	 * 1 > 2 ? a : b;	//fails, b can't be converted to a without the A( const A & ) copy constructor
	 * -----------------------
	 * class A {
	 *    A ( const A & ) {}
	 * } a;
	 * class B : public A {} b;
	 * 
	 * 1 > 2 ? a : b;  //succeeds, b can be converted to a using copy constructor
	 * 
	 */
	public void testAddCopyConstructor() throws Exception {
		newTable();
		
		IDerivableContainerSymbol clsA = table.newDerivableContainerSymbol( "A", TypeInfo.t_class );
		table.getCompilationUnit().addSymbol( clsA );
		
		ISymbol a = table.newSymbol( "a", TypeInfo.t_type );
		a.setTypeSymbol( clsA );
		table.getCompilationUnit().addSymbol( a );
		
		IDerivableContainerSymbol clsB = table.newDerivableContainerSymbol( "B", TypeInfo.t_class );
		clsB.addParent( clsA );
		table.getCompilationUnit().addSymbol( clsB );
		
		ISymbol b = table.newSymbol( "b", TypeInfo.t_type );
		b.setTypeSymbol( clsB );
		table.getCompilationUnit().addSymbol( b );
		
		TypeInfo secondOp = new TypeInfo( TypeInfo.t_type, 0, a, null, false );
		TypeInfo thirdOp = new TypeInfo( TypeInfo.t_type, 0, b, null, false );
		
		TypeInfo returned = ParserSymbolTable.getConditionalOperand( secondOp, thirdOp );
		assertEquals( returned, null );
		
		clsA.addCopyConstructor();
		clsB.addCopyConstructor();
		
		returned = ParserSymbolTable.getConditionalOperand( secondOp, thirdOp );
		assertEquals( returned, secondOp );	
	}
	
	public void testbug43834() throws Exception{
		newTable();
		
		IParameterizedSymbol f = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		table.getCompilationUnit().addSymbol( f );
		
		LinkedList parameters = new LinkedList();
		TypeInfo param = new TypeInfo( TypeInfo.t_void, 0, null );
		parameters.add( param );
		
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters );
		assertEquals( look, f );
		
		f.addParameter( TypeInfo.t_void, 0, null, false );
		
		parameters.clear();
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "f", parameters );
		assertEquals( look, f );
	}
	
	/**
	 * 
	 * namespace A{
	 *    void f();
	 * }
	 * namespace B{
	 *    int f;
	 * }
	 * namespace C{
	 *    using namespace A;
	 *    using namespace B;
	 *    using f;		//ambiguous, int f or void f()?
	 * }
	 */
	public void testBug43503_AmbiguousUsing() throws Exception{
		newTable();
		IContainerSymbol NSA = table.newContainerSymbol( "A", TypeInfo.t_namespace );
		table.getCompilationUnit().addSymbol( NSA );
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		NSA.addSymbol( f1 );
		
		IContainerSymbol NSB = table.newContainerSymbol( "B", TypeInfo.t_namespace );
		table.getCompilationUnit().addSymbol( NSB );
		
		ISymbol f2 = table.newSymbol( "f", TypeInfo.t_int );
		NSB.addSymbol( f2 );
		
		IContainerSymbol NSC = table.newContainerSymbol( "C", TypeInfo.t_namespace );
		table.getCompilationUnit().addSymbol( NSC );
		NSC.addUsingDirective( NSA );
		NSC.addUsingDirective( NSB );
		
		try{
			NSC.addUsingDeclaration( "f" );
			assertTrue( false );
		} catch ( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_Ambiguous );
		}
	}
	
	/**
	 * void f( void );
	 * void f( int );
	 * 
	 * void * pF = &f;  //lookup without function parameters, should be ambiguous
	 * @throws Exception
	 */
	public void testBug43503_UnableToResolveFunction() throws Exception{
		newTable();
		
		IParameterizedSymbol f1 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		
		IParameterizedSymbol f2 = table.newParameterizedSymbol( "f", TypeInfo.t_function );
		f2.addParameter( TypeInfo.t_int, 0, null, false );
		
		table.getCompilationUnit().addSymbol( f1 );
		table.getCompilationUnit().addSymbol( f2 );
		
		try{
			table.getCompilationUnit().lookup( "f" );
			assertTrue( false );
		} catch( ParserSymbolTableException e ){
			assertEquals( e.reason, ParserSymbolTableException.r_UnableToResolveFunction );
		}
		
	}
	
	/**
	 * int initialize();
	 * int initialize(){
	 *    return 3;
	 * }
	 * 
	 * int i = initialize();
	 * 
	 * @throws Exception
	 */
	public void testBug44510() throws Exception{
		newTable();
		
		IParameterizedSymbol init1 = table.newParameterizedSymbol( "initialize", TypeInfo.t_function );
		
		table.getCompilationUnit().addSymbol( init1 );
		
		IParameterizedSymbol init2 = table.newParameterizedSymbol( "initialize", TypeInfo.t_function );
		
		ISymbol look = table.getCompilationUnit().unqualifiedFunctionLookup( "initialize", new LinkedList() );
		assertEquals( look, init1 );
		
		init1.getTypeInfo().setIsForwardDeclaration( true );
		init1.setTypeSymbol( init2 );
		
		table.getCompilationUnit().addSymbol( init2 );
		
		look = table.getCompilationUnit().unqualifiedFunctionLookup( "initialize", new LinkedList() );
		
		assertEquals( look, init2 ); 
	}
}

