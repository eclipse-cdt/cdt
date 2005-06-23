/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Aug 23, 2004
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.core.resources.IFile;

/**
 * @author aniefer
 */
public class CompleteParsePluginTest extends FileBasePluginTest {
	
    /**
     * @param name
     */
    public CompleteParsePluginTest(String name)
    {
    	super(name, CompleteParsePluginTest.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite( CompleteParsePluginTest.class );
        suite.addTest( new CompleteParsePluginTest("cleanupProject") );    //$NON-NLS-1$
	    return suite;
    }
	
    public void testBug72219() throws Exception {
        String foo = "int FOO;"; //$NON-NLS-1$
        String code = "#include \"foo.h\" \n   int bar;"; //$NON-NLS-1$
        
        importFile( "foo.h", foo ); //$NON-NLS-1$
        IFile cpp = importFile( "code.cpp", code ); //$NON-NLS-1$
        
        List calls = new ArrayList();
        Iterator i = parse( cpp, calls ).getDeclarations();
        
        assertTrue( i.next() instanceof IASTVariable );
        assertTrue( i.next() instanceof IASTVariable );
        assertFalse( i.hasNext() );
        
        i = calls.iterator();
        assertEquals( i.next(), CallbackTracker.ENTER_COMPILATION_UNIT );
        assertEquals( i.next(), CallbackTracker.ENTER_INCLUSION );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.EXIT_INCLUSION  );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.EXIT_COMPILATION_UNIT );
        assertFalse( i.hasNext() );
    }
    
    public void testBug72219_2() throws Exception {
        String foo = "int FOO;"; //$NON-NLS-1$
        String code = "int bar; \n #include \"foo.h\""; //$NON-NLS-1$
        
        importFile( "foo.h", foo ); //$NON-NLS-1$
        IFile cpp = importFile( "code.cpp", code ); //$NON-NLS-1$
        
        List calls = new ArrayList();
        Iterator i = parse( cpp, calls ).getDeclarations();
        
        assertTrue( i.next() instanceof IASTVariable );
        assertTrue( i.next() instanceof IASTVariable );
        assertFalse( i.hasNext() );
        
        i = calls.iterator();
        assertEquals( i.next(), CallbackTracker.ENTER_COMPILATION_UNIT );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.ENTER_INCLUSION );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.EXIT_INCLUSION  );
        assertEquals( i.next(), CallbackTracker.EXIT_COMPILATION_UNIT );
        assertFalse( i.hasNext() );
    }
    
    public void testBug72219_3() throws Exception {
        String defs = "#define A \n #define B \n"; //$NON-NLS-1$
        String code = "int foo; \n #include \"defs.h\" \n  int bar;"; //$NON-NLS-1$
        
        importFile( "defs.h", defs); //$NON-NLS-1$
        IFile cpp = importFile( "code.cpp", code ); //$NON-NLS-1$
        
        List calls = new ArrayList();
        Iterator i = parse( cpp, calls ).getDeclarations();
        
        assertTrue( i.next() instanceof IASTVariable );
        assertTrue( i.next() instanceof IASTVariable );
        assertFalse( i.hasNext() );
        
        i = calls.iterator();
        assertEquals( i.next(), CallbackTracker.ENTER_COMPILATION_UNIT );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.ENTER_INCLUSION );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.EXIT_INCLUSION  );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.EXIT_COMPILATION_UNIT );
        assertFalse( i.hasNext() );
    }
    
    public void test72219_4() throws Exception{
        String code = "int a; \n #define A\n  int b;\n #define B\n"; //$NON-NLS-1$
        
        IFile cpp = importFile( "code.cpp", code ); //$NON-NLS-1$
        List calls = new ArrayList();
        
        parse( cpp, calls );
        
        Iterator i = calls.iterator();
        assertEquals( i.next(), CallbackTracker.ENTER_COMPILATION_UNIT );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.EXIT_COMPILATION_UNIT );
        assertFalse( i.hasNext() );
    }
    
    public void testBug72506() throws Exception{
        String vers = "int i;\n"; //$NON-NLS-1$
        String code = "#define INCFILE(x) vers ## x\n" + //$NON-NLS-1$
        		      "#define xstr(x) str(x)\n" + //$NON-NLS-1$
        		      "#define str(x) #x\n" + //$NON-NLS-1$
        		      "#include xstr(INCFILE(2).h)\n"; //$NON-NLS-1$
        
        importFile( "vers2.h", vers ); //$NON-NLS-1$
        IFile cpp = importFile( "code.cpp", code ); //$NON-NLS-1$
        
        List calls = new ArrayList();
        
        parse( cpp, calls );
        
        Iterator i = calls.iterator();
        assertEquals( i.next(), CallbackTracker.ENTER_COMPILATION_UNIT );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.ENTER_INCLUSION );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.EXIT_INCLUSION );
        assertEquals( i.next(), CallbackTracker.EXIT_COMPILATION_UNIT );
        assertFalse( i.hasNext() );
    }
    
    public void testExpressionEvalProblems() throws Exception
    {
        String h = " #if 09 == 9    \n" + //$NON-NLS-1$   //bad octal
        		   " #endif         \n"; //$NON-NLS-1$
        
        String code = "int i1;         \n" + //$NON-NLS-1$
        		      "#include \"h.h\"\n" + //$NON-NLS-1$
        		      "int i2;         \n"; //$NON-NLS-1$
        
        importFile( "h.h", h ); //$NON-NLS-1$
        IFile cpp = importFile( "c.cpp", code ); //$NON-NLS-1$
        
        List calls = new ArrayList();
        parse( cpp, calls );
        
        Iterator i = calls.iterator();
        
        assertEquals( i.next(), CallbackTracker.ENTER_COMPILATION_UNIT );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.ENTER_INCLUSION );
        assertEquals( i.next(), CallbackTracker.ACCEPT_PROBLEM );
        assertEquals( i.next(), CallbackTracker.EXIT_INCLUSION );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.EXIT_COMPILATION_UNIT );
        assertFalse( i.hasNext() );
    }
    
	public void testBug79339() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("#ifndef _HEADER_\n"); //$NON-NLS-1$
	    writer.write("#define _HEADER_\n"); //$NON-NLS-1$
	    writer.write("#define ONE 1\n"); //$NON-NLS-1$
	    writer.write("int foo(int);\n"); //$NON-NLS-1$
	    writer.write("#endif // _HEADER_\n"); //$NON-NLS-1$
	    String header = writer.toString();
	    importFile( "header.h", header ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"header.h\"  \n"); //$NON-NLS-1$
	    writer.write( "int foo2(){\n"); //$NON-NLS-1$
	    writer.write( "   return foo(ONE);\n"); //$NON-NLS-1$
	    writer.write( "}\n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "test.cpp", source ); //$NON-NLS-1$
	    
	    int start = source.indexOf( "foo(ONE)" ); //$NON-NLS-1$
	    
	    List calls = new ArrayList();
	    IASTNode node = parse( cpp, calls, start, start + 3 ); //$NON-NLS-1$
	    assertTrue(node instanceof IASTFunction);
	    IASTFunction foo = (IASTFunction)node;
	    assertEquals(foo.getStartingLine(), 4);
	    assertEquals(foo.getNameOffset(), 52);
	    assertEquals(foo.getName(), "foo"); //$NON-NLS-1$
	    assertTrue(new String(foo.getFilename()).indexOf("header.h") > 0); //$NON-NLS-1$
	}
	
	public void testBug79810B() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("#define __WTERMSIG(status) ((status) & 0x7f)\n"); //$NON-NLS-1$
    	writer.write("#define __WIFEXITED(status) (__WTERMSIG(status) == 0)\n"); //$NON-NLS-1$
    	writer.write("#define __WAIT_INT(status) (__extension__ ({ union { int __in; int __i; } __u; \\\n"); //$NON-NLS-1$
    	writer.write("           __u.__in = (test); __u.__i; }))\n"); //$NON-NLS-1$
    	writer.write("#define WIFEXITED(status)	__WIFEXITED(__WAIT_INT(status))\n"); //$NON-NLS-1$
    	importFile( "header.h", writer.toString() ); //$NON-NLS-1$
    	
    	writer = new StringWriter();
    	writer.write( "#include \"header.h\"  \n"); //$NON-NLS-1$
    	writer.write("void foo() {\n"); //$NON-NLS-1$
    	writer.write("int test;\n"); //$NON-NLS-1$
    	writer.write("if (WIFEXITED(test)) {}\n}\n"); //$NON-NLS-1$
    	IFile cpp = importFile( "test.cpp", writer.toString() ); //$NON-NLS-1$
    	
    	List calls = new ArrayList();
        parse( cpp, calls );
        
        Iterator i = calls.iterator();
        
        assertEquals( i.next(), CallbackTracker.ENTER_COMPILATION_UNIT );
        assertEquals( i.next(), CallbackTracker.ENTER_INCLUSION );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.ACCEPT_MACRO );
        assertEquals( i.next(), CallbackTracker.EXIT_INCLUSION );
        assertEquals( i.next(), CallbackTracker.ENTER_FUNCTION );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.ENTER_CODE_BLOCK );
        assertEquals( i.next(), CallbackTracker.ENTER_CLASS_SPEC );
        assertEquals( i.next(), CallbackTracker.ACCEPT_FIELD );
        assertEquals( i.next(), CallbackTracker.ACCEPT_FIELD );
        assertEquals( i.next(), CallbackTracker.EXIT_CLASS );
        assertEquals( i.next(), CallbackTracker.ACCEPT_VARIABLE );
        assertEquals( i.next(), CallbackTracker.ACCEPT_REFERENCE );
        assertEquals( i.next(), CallbackTracker.ACCEPT_REFERENCE );
        assertEquals( i.next(), CallbackTracker.ACCEPT_REFERENCE );
        assertEquals( i.next(), CallbackTracker.ACCEPT_REFERENCE );
        assertEquals( i.next(), CallbackTracker.ACCEPT_REFERENCE );
        assertEquals( i.next(), CallbackTracker.EXIT_CODE_BLOCK );
        assertEquals( i.next(), CallbackTracker.ENTER_CODE_BLOCK );
        assertEquals( i.next(), CallbackTracker.EXIT_CODE_BLOCK );
        assertEquals( i.next(), CallbackTracker.EXIT_FUNCTION );
        assertEquals( i.next(), CallbackTracker.EXIT_COMPILATION_UNIT );
        assertFalse( i.hasNext() );
    }
}
