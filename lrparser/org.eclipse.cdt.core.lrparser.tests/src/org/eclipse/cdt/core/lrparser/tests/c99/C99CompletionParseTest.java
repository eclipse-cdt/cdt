/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests.c99;

import java.util.Arrays;
import java.util.Comparator;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;


/**
 * Reuse the completion parse tests from the old parser for now.
 * 
 * This test suite is specific to C99.
 * 
 * TODO run this against C++
 */
public class C99CompletionParseTest extends TestCase {

	public C99CompletionParseTest() { }
	public C99CompletionParseTest(String name) { super(name); }
	

	protected IASTCompletionNode parse(String code, int offset) throws Exception {
		return ParseHelper.getCompletionNode(code, getLanguage(), offset);
	}


	private static class BindingsComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			IBinding b1 = (IBinding)o1;
			IBinding b2 = (IBinding)o2;
			return b1.getName().compareTo(b2.getName());
		}
	}
	
	private static BindingsComparator bindingsComparator  = new BindingsComparator();
	
	protected IBinding[] sortBindings(IBinding[] bindings) {
		Arrays.sort(bindings, bindingsComparator);
		return bindings;
	}
	
	protected IBinding[] getBindings(IASTName[] names) {
		return sortBindings(names[0].getCompletionContext().findBindings(names[0], true));
	}
	
	
	protected BaseExtensibleLanguage getLanguage() {
		return C99Language.getDefault();
	}
	
	
	// First steal tests from CompletionParseTest
	
	
	public void testCompletionStructField() throws Exception
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "int aVar; " ); //$NON-NLS-1$
		sb.append( "struct D{ " ); //$NON-NLS-1$
		sb.append( "   int aField1; " ); //$NON-NLS-1$
		sb.append( "   int aField2; " ); //$NON-NLS-1$
		sb.append( "}; " ); //$NON-NLS-1$
		sb.append( "void foo(){" ); //$NON-NLS-1$
		sb.append( "   struct D d; " ); //$NON-NLS-1$
		sb.append( "   d.a " ); //$NON-NLS-1$
		sb.append( "}\n" ); //$NON-NLS-1$
		
		String code = sb.toString();
		int index = code.indexOf( "d.a" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 3 );				
		assertNotNull( node );
		
		String prefix = node.getPrefix();
		assertNotNull( prefix );
		assertEquals( prefix, "a" ); //$NON-NLS-1$
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(2, bindings.length);
		assertEquals("aField1", ((IField)bindings[0]).getName());
		assertEquals("aField2", ((IField)bindings[1]).getName());
	}
	
	public void testCompletionStructFieldPointer() throws Exception
	{
		StringBuffer sb = new StringBuffer();
		sb.append("struct Cube {                       "); //$NON-NLS-1$
		sb.append("   int nLen;                        "); //$NON-NLS-1$
		sb.append("   int nWidth;                      "); //$NON-NLS-1$
		sb.append("   int nHeight;                     "); //$NON-NLS-1$
		sb.append("};                                  "); //$NON-NLS-1$
		sb.append("int volume( struct Cube * pCube ) { "); //$NON-NLS-1$
		sb.append("   pCube->SP                        "); //$NON-NLS-1$

		String code = sb.toString();
		IASTCompletionNode node = parse( code, code.indexOf("SP")); //$NON-NLS-1$
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(3, bindings.length);
		assertEquals("nHeight", ((IField)bindings[0]).getName());
		assertEquals("nLen", ((IField)bindings[1]).getName());
		assertEquals("nWidth", ((IField)bindings[2]).getName());
	}
	
	
	public void testCompletionParametersAsLocalVariables() throws Exception{
		StringBuffer sb = new StringBuffer();
		sb.append( "int foo( int aParameter ){" ); //$NON-NLS-1$
		sb.append( "   int aLocal;" ); //$NON-NLS-1$
		sb.append( "   if( aLocal != 0 ){" );		 //$NON-NLS-1$
		sb.append( "      int aBlockLocal;" ); //$NON-NLS-1$
		sb.append( "      a \n" ); //$NON-NLS-1$
		
		String code = sb.toString();
		int index = code.indexOf( " a " ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 2 );
		assertNotNull( node );
		
		assertEquals("a", node.getPrefix()); //$NON-NLS-1$
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(3, bindings.length);
		assertEquals("aBlockLocal", ((IVariable)bindings[0]).getName());
		assertEquals("aLocal",      ((IVariable)bindings[1]).getName());
		assertEquals("aParameter",  ((IVariable)bindings[2]).getName());
	}
	
	
	public void testCompletionTypedef() throws Exception{
		StringBuffer sb = new StringBuffer();
		sb.append( "typedef int Int; "); //$NON-NLS-1$
		sb.append( "InSP" ); //$NON-NLS-1$
		
		String code = sb.toString();
		int index = code.indexOf( "SP" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index );
		assertNotNull(node);
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		assertEquals("In", node.getPrefix());
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(1, bindings.length);
		assertEquals("Int", ((ITypedef)bindings[0]).getName());
	}
	
	public void testCompletion() throws Exception
	{
		StringBuffer sb = new StringBuffer();
		sb.append("#define GL_T 0x2001\n"); //$NON-NLS-1$
		sb.append("#define GL_TRUE 0x1\n"); //$NON-NLS-1$
		sb.append("typedef unsigned char   GLboolean;\n"); //$NON-NLS-1$
		sb.append("static GLboolean should_rotate = GL_T"); //$NON-NLS-1$
		
		String code = sb.toString();
		
		int index = code.indexOf("= GL_T"); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 6);
		assertNotNull(node);
		
		assertEquals("GL_T", node.getPrefix()); //$NON-NLS-1$
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
	}
	
	public void testCompletionInTypeDef() throws Exception{
		StringBuffer sb = new StringBuffer();
		sb.append( "struct A {  int name;  };  \n" ); //$NON-NLS-1$
		sb.append( "typedef struct A * PA;     \n" ); //$NON-NLS-1$
		sb.append( "int main() {               \n" ); //$NON-NLS-1$
		sb.append( "   PA a;                   \n" ); //$NON-NLS-1$
		sb.append( "   a->SP                   \n" ); //$NON-NLS-1$
		sb.append( "}                          \n" ); //$NON-NLS-1$
		
		String code = sb.toString();
		int index = code.indexOf("SP"); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index );
		assertNotNull( node );
		
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(1, bindings.length);
		assertEquals("name", ((IField)bindings[0]).getName());
	}
	
	
	public void _testCompletionFunctionCall() throws Exception
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "struct A {  	     \n" ); //$NON-NLS-1$ 
		sb.append( "   int f2;  		 \n" ); //$NON-NLS-1$
		sb.append( "   int f4;           \n" ); //$NON-NLS-1$
		sb.append( "};                   \n" ); //$NON-NLS-1$
		sb.append( "const A * foo(){}    \n" ); //$NON-NLS-1$
		sb.append( "void main( )         \n" ); //$NON-NLS-1$
		sb.append( "{                    \n" ); //$NON-NLS-1$
		sb.append( "   foo()->SP         \n" ); //$NON-NLS-1$
		
		String code = sb.toString();
		int index = code.indexOf( "SP" ); //$NON-NLS-1$
		
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
		StringBuffer sb = new StringBuffer();
		sb.append( "int f() {\n" ); //$NON-NLS-1$
		sb.append( "short blah;\n" ); //$NON-NLS-1$
		sb.append( "int x = sizeof(bl" ); //$NON-NLS-1$
		
		String code = sb.toString();
		int index = code.indexOf( "of(bl" ); //$NON-NLS-1$
		
		IASTCompletionNode node = parse( code, index + 5);
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		
		IBinding[] bindings = getBindings(names);
		
		assertEquals(1, bindings.length);
		assertEquals("blah", ((IVariable)bindings[0]).getName());
	}
	
	
	public void testCompletionForLoop() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append( "int f() {\n" ); //$NON-NLS-1$
		sb.append( " int biSizeImage = 5;\n" ); //$NON-NLS-1$
		sb.append( "for (int i = 0; i < bi " ); //$NON-NLS-1$
		String code = sb.toString();
		
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
		StringBuffer sb = new StringBuffer();
		sb.append(" struct Temp { char * total; };" );
		sb.append(" int f(struct Temp * t) {" );
		sb.append(" t->t[5] = t->" );
		String code = sb.toString();
		
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
		StringBuffer sb = new StringBuffer();
		sb.append( "typedef int DWORD;\n" ); //$NON-NLS-1$
		sb.append( "typedef char BYTE;\n"); //$NON-NLS-1$
		sb.append( "#define MAKEFOURCC(ch0, ch1, ch2, ch3)                  \\\n"); //$NON-NLS-1$
		sb.append( "((DWORD)(BYTE)(ch0) | ((DWORD)(BYTE)(ch1) << 8) |       \\\n"); //$NON-NLS-1$
		sb.append( "((DWORD)(BYTE)(ch2) << 16) | ((DWORD)(BYTE)(ch3) << 24 ))\n"); //$NON-NLS-1$
		sb.append( "enum e {\n"); //$NON-NLS-1$
		sb.append( "blah1 = 5,\n"); //$NON-NLS-1$
		sb.append( "blah2 = MAKEFOURCC('a', 'b', 'c', 'd'),\n"); //$NON-NLS-1$
		sb.append( "blah3\n"); //$NON-NLS-1$
		sb.append( "};\n"); //$NON-NLS-1$
		sb.append( "e mye = bl\n"); //$NON-NLS-1$
		String code = sb.toString();
		
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
		StringBuffer sb = new StringBuffer();
		sb.append( "struct packet { int a; int b; };\n" ); //$NON-NLS-1$
		sb.append( "struct packet buffer[5];\n" ); //$NON-NLS-1$
		sb.append( "int main(int argc, char **argv) {\n" ); //$NON-NLS-1$
		sb.append( " buffer[2]." ); //$NON-NLS-1$
		String code = sb.toString();
		
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
		
		assertEquals("#", node.getPrefix());
	}
	
	public void testCompletionPreprocessorMacro() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append( "#define AMACRO 99 \n");
		sb.append( "int main() { \n");
		sb.append( "	int AVAR; \n");
		sb.append( "	int x = A \n");
		String code = sb.toString();
		
		int index = code.indexOf("= A");
		
		IASTCompletionNode node = parse( code, index + 3);
		assertNotNull( node );
		
		IASTName[] names = node.getNames();
		assertEquals(1, names.length); 
		assertEquals("A", node.getPrefix());
	}
	
	
	public void testCompletionInsidePreprocessorDirective() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append( "#define MAC1 99 \n");
		sb.append( "#define MAC2 99 \n");
		sb.append( "#ifdef MA");
		String code = sb.toString();
		
		int index = code.length();
		
		IASTCompletionNode node = parse( code, index );
		assertNotNull( node );
		
		assertEquals("MA", node.getPrefix());
	}
}
