package org.eclipse.cdt.core.parser.tests;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;
import org.eclipse.cdt.internal.core.newparser.Declaration;
import org.eclipse.cdt.internal.core.newparser.ParserSymbolTable;
import org.eclipse.cdt.internal.core.newparser.ParserSymbolTableException;

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
		
		Declaration decl = new Declaration( "x" );
		
		table.addDeclaration( decl );
		
		Declaration compUnit = table.getCompilationUnit();
		assertEquals( compUnit, table.peek() );
		
		Map declarations = compUnit.getContainedDeclarations();
		assertEquals( 1, declarations.size() );
		
		Iterator iter = declarations.values().iterator();
		Declaration contained = (Declaration) iter.next();
		
		assertEquals( false, iter.hasNext() );
		assertEquals( decl, contained );
		assertEquals( contained.getName(), "x" );
	}

	/**
	 * testSimpleLookup
	 * Add a declaration to the table, then look it up.
	 * @throws Exception
	 */
	public void testSimpleLookup() throws Exception{
		newTable(); //new symbol table
		
		Declaration decl = new Declaration( "x" );
		
		table.addDeclaration( decl );
		
		Declaration look = table.Lookup( "x" );
		
		assertEquals( decl, look );
	}
	
	public void testLookupNonExistant() throws Exception{
		newTable();
		
		Declaration look = table.Lookup( "boo" );
		assertEquals( look, null );
	}
	
	/**
	 * testSimplePushPop
	 * test pushing and popping
	 * @throws Exception
	 */
	public void testSimplePushPop() throws Exception{
		newTable();
		
		Declaration pushing = new Declaration( "class" );
		assertEquals( pushing.getContainingScope(), null );
		
		table.push( pushing );
		assertEquals( pushing, table.peek() );
		assertEquals( pushing.getContainingScope(), table.getCompilationUnit() );
		
		Declaration popped = table.pop();
		assertEquals( pushing, popped );
		assertEquals( table.peek(), table.getCompilationUnit() );
	}
	
	/**
	 * testHide
	 * test that a declaration in a scope hides declarations in containing
	 * scopes
	 * @throws Exception
	 */
	public void testHide() throws Exception{
		newTable();
		
		Declaration firstX = new Declaration( "x" );
		table.addDeclaration( firstX );
		
		Declaration firstClass = new Declaration( "class" );
		table.addDeclaration( firstClass );
		table.push( firstClass );
		
		Declaration look = table.Lookup( "x" );
		assertEquals( look, firstX );
		
		Declaration secondX = new Declaration( "x" );
		table.addDeclaration( secondX );
		
		look = table.Lookup( "x" );
		assertEquals( look, secondX );
		
		table.pop();
		
		look = table.Lookup( "x" );
		assertEquals( look, firstX );
	}
	
	/**
	 * testContainingScopeLookup
	 * test lookup of something declared in the containing scope
	 * @throws Exception
	 */
	public void testContainingScopeLookup() throws Exception{
		newTable();
		
		Declaration x = new Declaration("x");
		Declaration cls = new Declaration("class");
		
		table.addDeclaration( x );
		table.addDeclaration( cls );
		table.push( cls );
		
		Declaration look = table.Lookup( "x" );
		
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
		
		Declaration class1 = new Declaration( "class" );
		Declaration parent = new Declaration( "parent" );
		Declaration decl   = new Declaration( "x" );
		
		table.addDeclaration( parent );
		table.push( parent );
		table.addDeclaration( decl );
		table.pop();
		
		class1.addParent( parent );
		table.addDeclaration( class1 );
		table.push( class1 );
		
		Declaration look = table.Lookup( "x" );
		assertEquals( look, decl );
		
		table.pop();
		assertEquals( table.peek(), table.getCompilationUnit() );
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
		
		Declaration parent2 = new Declaration( "parent2" );
		
		table.addDeclaration( parent2 );
		
		Declaration class1 = table.Lookup( "class" );
		class1.addParent( parent2 );
		
		Declaration decl = new Declaration("x");
		table.push( parent2 );
		table.addDeclaration( decl );
		table.pop();
		
		table.push( class1 );
		try{
			table.Lookup( "x" );
			assertTrue( false );
		}
		catch ( ParserSymbolTableException e ){
			assertTrue( true );
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
		
		Declaration decl = new Declaration("class");
		Declaration c    = new Declaration("C");
		
		Declaration a    = new Declaration("A");
		a.addParent( c, true );
		
		Declaration b    = new Declaration("B");
		b.addParent( c, true );
		
		decl.addParent( a );
		decl.addParent( b );
		
		table.addDeclaration( c );
		table.push( c );
		Declaration x = new Declaration( "x" );
		table.addDeclaration( x );
		table.pop();
		
		table.addDeclaration( decl );
		table.addDeclaration( a );
		table.addDeclaration( b );
		
		table.push(decl);
		
		Declaration look = table.Lookup( "x" ); 
		
		assertEquals( look, x );
		
		table.pop();
		
		assertEquals( table.peek(), table.getCompilationUnit() );
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
		
		Declaration cls = table.Lookup("class");
		Declaration c   = table.Lookup("C");
		Declaration d   = new Declaration("D");
		
		d.addParent( c );
		
		cls.addParent( d );
		
		table.push( cls );
		
		try{
			table.Lookup( "x" );
			assertTrue( false );
		}
		catch( ParserSymbolTableException e){
			assertTrue( true );
		}
	}
}