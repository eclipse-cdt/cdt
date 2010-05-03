/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.model.ILanguage;


/**
 * Reuse the completion parse tests from the old parser for now.
 */
@SuppressWarnings("nls")
public class LRCompletionParseTest extends TestCase {

	public static TestSuite suite() {
        return new TestSuite(LRCompletionParseTest.class);
    }
	
	
	public LRCompletionParseTest() { }
	public LRCompletionParseTest(String name) { super(name); }
	

	protected IASTCompletionNode parse(String code, int offset) throws Exception {
		return ParseHelper.getCompletionNode(code, getCLanguage(), offset);
	}


	private static final Comparator<IBinding> BINDING_COMPARATOR = new Comparator<IBinding>() {
		public int compare(IBinding b1, IBinding b2) {
			return b1.getName().compareTo(b2.getName());
		}
	};
	
	
	public static IBinding[] getBindings(IASTName[] names) {
		List<IBinding> bindings = new ArrayList<IBinding>();
		
		for(IASTName name : names) {
			if(name.getTranslationUnit() == null)
				continue;
			
			for(IBinding binding : name.getCompletionContext().findBindings(name, true))
				bindings.add(binding);
		}

		Collections.sort(bindings, BINDING_COMPARATOR);
		return bindings.toArray(new IBinding[bindings.size()]);
	}
	
	
	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}
	
	
	// First steal tests from CompletionParseTest
	
	
	public void testCompletionStructField() throws Exception {
		String code =
			"int aVar; " +
			"struct D{ " +
			"   int aField1; " +
			"   int aField2; " +
			"}; " +
			"void foo(){" +
			"   struct D d; " +
			"   d.a " +
			"}\n";
		
		int index = code.indexOf( "d.a" );
		
		IASTCompletionNode node = parse( code, index + 3 );				
		assertNotNull( node );
		
		String prefix = node.getPrefix();
		assertNotNull( prefix );
		assertEquals( prefix, "a" );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(2, bindings.length);
		assertEquals("aField1", ((IField)bindings[0]).getName());
		assertEquals("aField2", ((IField)bindings[1]).getName());
	}
	
	public void testCompletionStructFieldPointer() throws Exception {
		String code =
			"struct Cube {                       " +
			"   int nLen;                        " +
			"   int nWidth;                      " +
			"   int nHeight;                     " +
			"};                                  " +
			"int volume( struct Cube * pCube ) { " +
			"   pCube->SP                        ";

		IASTCompletionNode node = parse( code, code.indexOf("SP"));
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(3, bindings.length);
		assertEquals("nHeight", ((IField)bindings[0]).getName());
		assertEquals("nLen", ((IField)bindings[1]).getName());
		assertEquals("nWidth", ((IField)bindings[2]).getName());
	}
	
	
	public void testCompletionParametersAsLocalVariables() throws Exception{
		String code =
			"int foo( int aParameter ){" +
			"   int aLocal;" +
			"   if( aLocal != 0 ){" +
			"      int aBlockLocal;" +
			"      a \n";
		
		int index = code.indexOf( " a " );
		
		IASTCompletionNode node = parse( code, index + 2 );
		assertNotNull( node );
		
		assertEquals("a", node.getPrefix()); //$NON-NLS-1$
		
		IASTName[] names = node.getNames();
		assertEquals(2, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(3, bindings.length);
		assertEquals("aBlockLocal", ((IVariable)bindings[0]).getName());
		assertEquals("aLocal",      ((IVariable)bindings[1]).getName());
		assertEquals("aParameter",  ((IVariable)bindings[2]).getName());
	}
	
	
	public void testCompletionTypedef() throws Exception {
		String code =
			"typedef int Int; " +
			"InSP";
		
		int index = code.indexOf( "SP" );
		
		IASTCompletionNode node = parse( code, index );
		assertNotNull(node);
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		assertEquals("In", node.getPrefix());
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(1, bindings.length);
		assertEquals("Int", ((ITypedef)bindings[0]).getName());
	}
	
	public void testCompletion() throws Exception {
		String code =
			"#define GL_T 0x2001\n" +
			"#define GL_TRUE 0x1\n" +
			"typedef unsigned char   GLboolean;\n" +
			"static GLboolean should_rotate = GL_T";
		
		int index = code.indexOf("= GL_T");
		
		IASTCompletionNode node = parse( code, index + 6);
		assertNotNull(node);
		
		assertEquals("GL_T", node.getPrefix()); //$NON-NLS-1$
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
	}
	
	public void testCompletionInTypeDef() throws Exception {
		String code =
			"struct A {  int name;  };  \n" +
			"typedef struct A * PA;     \n" +
			"int main() {               \n" +
			"   PA a;                   \n" +
			"   a->SP                   \n" +
			"}                          \n";
		
		int index = code.indexOf("SP"); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index );
		assertNotNull( node );
		
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(1, bindings.length);
		assertEquals("name", ((IField)bindings[0]).getName());
	}
	
	
	public void _testCompletionFunctionCall() throws Exception {
		String code =
			"struct A {  	      \n" +
			"   int f2;  		  \n" +
			"   int f4;           \n" +
			"};                   \n" +
			"const A * foo(){}    \n" +
			"void main( )         \n" +
			"{                    \n" +
			"   foo()->SP         \n";
		
		int index = code.indexOf( "SP" );
		
		IASTCompletionNode node = parse( code, index );
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(2, bindings.length);
		assertEquals("f2", ((IField)bindings[0]).getName());
		assertEquals("f4", ((IField)bindings[1]).getName());
	}
	
	
	public void _testCompletionSizeof() throws Exception {
		String code =
			"int f() {\n" +
			"short blah;\n" +
			"int x = sizeof(bl";
		
		int index = code.indexOf( "of(bl" );
		
		IASTCompletionNode node = parse( code, index + 5);
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(1, bindings.length);
		assertEquals("blah", ((IVariable)bindings[0]).getName());
	}
	
	
	public void testCompletionForLoop() throws Exception {
		String code =
			"int f() {\n" +
			" int biSizeImage = 5;\n" +
			"for (int i = 0; i < bi ";
		
		int index = code.indexOf("< bi");
		
		IASTCompletionNode node = parse( code, index + 4);
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(1, bindings.length);
		assertEquals("biSizeImage", ((IVariable)bindings[0]).getName());
	}
	
	
	
	public void testCompletionStructPointer() throws Exception {
		String code =
			" struct Temp { char * total; };" +
			" int f(struct Temp * t) {"  +
			" t->t[5] = t->";
		
		int index = code.indexOf("= t->");
		
		IASTCompletionNode node = parse( code, index + 5);
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(1, bindings.length);
		assertEquals("total", ((IVariable)bindings[0]).getName());
	}
	
	
	public void testCompletionEnum() throws Exception {
		String code =
			"typedef int DWORD;\n" +
			"typedef char BYTE;\n" +
			"#define MAKEFOURCC(ch0, ch1, ch2, ch3)                  \\\n" +
			"((DWORD)(BYTE)(ch0) | ((DWORD)(BYTE)(ch1) << 8) |       \\\n" +
			"((DWORD)(BYTE)(ch2) << 16) | ((DWORD)(BYTE)(ch3) << 24 ))\n" +
			"enum e {\n" +
			"blah1 = 5,\n" +
			"blah2 = MAKEFOURCC('a', 'b', 'c', 'd'),\n" +
			"blah3\n" +
			"};\n" +
			"e mye = bl\n";
		
		int index = code.indexOf("= bl");
		
		IASTCompletionNode node = parse( code, index + 4);
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(3, bindings.length);
		assertEquals("blah1", ((IEnumerator)bindings[0]).getName());
		assertEquals("blah2", ((IEnumerator)bindings[1]).getName());
		assertEquals("blah3", ((IEnumerator)bindings[2]).getName());
	}
	
	
	public void testCompletionStructArray() throws Exception { 
		String code =
			"struct packet { int a; int b; };\n" +
			"struct packet buffer[5];\n" +
			"int main(int argc, char **argv) {\n" +
			" buffer[2].";
		
		int index = code.indexOf("[2].");
		
		IASTCompletionNode node = parse( code, index + 4);
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(2, bindings.length);
		assertEquals("a", ((IField)bindings[0]).getName());
		assertEquals("b", ((IField)bindings[1]).getName());
	}
	
	
	public void testCompletionPreprocessorDirective() throws Exception {
		IASTCompletionNode node = parse("#", 1);
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		//assertEquals("#", node.getPrefix());
	}
	
	public void testCompletionPreprocessorMacro() throws Exception {
		String code =
			"#define AMACRO 99 \n" +
			"int main() { \n" +
			"	int AVAR; \n" +
			"	int x = A \n";
		
		int index = code.indexOf("= A");
		
		IASTCompletionNode node = parse( code, index + 3);
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		assertEquals("A", node.getPrefix());
	}
	
	
	public void testCompletionInsidePreprocessorDirective() throws Exception {
		String code =
			"#define MAC1 99 \n" +
			"#define MAC2 99 \n" +
			"#ifdef MA";
		
		int index = code.length();
		
		IASTCompletionNode node = parse( code, index );
		assertNotNull( node );
		
		assertEquals("MA", node.getPrefix());
	}
}
