/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Aug 23, 2004
 */
package org.eclipse.cdt.core.parser.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

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
}
