package org.eclipse.cdt.core.model.tests;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Stack;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.cdt.testplugin.util.ExpectedStrings;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;



/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the core C model's TranslationUnit
 * class. There is nothing exotic here, mostly just sanity type tests
 *
 */
public class TranslationUnitTests extends TestCase {
    IWorkspace workspace;
    IWorkspaceRoot root;
    ICProject testProject;
    IFile cfile, exefile, libfile, archfile, objfile;
    Path cpath, exepath, libpath, archpath, objpath;
    NullProgressMonitor monitor;
    
    /* This is a list of elements in the test .c file. It will be used 
     * in a number of places in the tests
     */
    String[] expectedStringList= {"stdio.h", "unistd.h", "func2p", 
        "globalvar", "myenum", "mystruct", "mystruct_t", "myunion", "mytype", 
        "func1", "func2", "main", "func3", "KRFunction"};
    int[]  expectedLines={ 12,14,17,20,23,28,32,35,42,47,53,58,65,70};
    /* This is a list of that the types of the above list of elements is 
     * expected to be.
     */
    int[] expectedTypes= { ICElement.C_INCLUDE, ICElement.C_INCLUDE, 
        ICElement.C_FUNCTION_DECLARATION, ICElement.C_VARIABLE, 
        ICElement.C_ENUMERATION, ICElement.C_STRUCT, ICElement.C_TYPEDEF, 
        ICElement.C_UNION, ICElement.C_TYPEDEF, ICElement.C_FUNCTION,
        ICElement.C_FUNCTION, ICElement.C_FUNCTION,ICElement.C_FUNCTION,
        ICElement.C_FUNCTION};
    

    /**
     * Constructor for TranslationUnitTests
     * @param name
     */
    public TranslationUnitTests(String name) {
        super(name);
    }
    
    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     * 
     * Example code test the packages in the project 
     *  "com.qnx.tools.ide.cdt.core"
     */
    protected void setUp() throws CoreException,FileNotFoundException {
        /***
         * The rest of the tests assume that they have a working workspace
         * and workspace root object to use to create projects/files in, 
         * so we need to get them setup first.
         */
        IWorkspaceDescription desc;
        String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile();
        workspace= ResourcesPlugin.getWorkspace();
        root= workspace.getRoot();
        monitor = new NullProgressMonitor();
        if (workspace==null) 
            fail("Workspace was not setup");
        if (root==null)
            fail("Workspace root was not setup");
            
        desc=workspace.getDescription();
	    desc.setAutoBuilding(false);
	    workspace.setDescription(desc);

        /***
         * Setup the various files, paths and projects that are needed by the
         * tests
         */
            
        testProject=CProjectHelper.createCProject("filetest", "none");
        if (testProject==null)
            fail("Unable to create project");

        cfile = testProject.getProject().getFile("exetest.c");
        if (!cfile.exists()) {
            cfile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/cfiles/TranslationUnits.c"),false, monitor);
        
        }
        cpath=new Path(workspace.getRoot().getLocation()+"/filetest/main.c");

        objfile = testProject.getProject().getFile("exetest.o");
        if (!objfile.exists()) {
            objfile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/x86/o.g/main.o"),false, monitor);
        
        }
        objpath=new Path(workspace.getRoot().getLocation()+"/filetest/main.o");
        
        exefile = testProject.getProject().getFile("test_g");
        if (!exefile.exists()) {
            exefile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/x86/o.g/exe_g"),false, monitor);
        
        }
        exepath=new Path(workspace.getRoot().getLocation()+"/filetest/exe_g");
        
        archfile = testProject.getProject().getFile("libtestlib_g.a");
        if (!archfile.exists()) {
            archfile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/testlib/x86/a.g/libtestlib_g.a"),false, monitor);
        
        }
        libpath=new Path(workspace.getRoot().getLocation()+"/filetest/libtestlib_g.so");
        
        libfile = testProject.getProject().getFile("libtestlib_g.so");
        if (!libfile.exists()) {
            libfile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/testlib/x86/so.g/libtestlib_g.so"),false, monitor);
        
        }
        archpath=new Path(workspace.getRoot().getLocation()+"/filetest/libtestlib_g.a");


    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() throws CoreException {
       // release resources here and clean-up
       testProject.getProject().delete(true,true,monitor);
    }
    
    public static TestSuite suite() {
		TestSuite suite= new TestSuite(TranslationUnitTests.class.getName());
		suite.addTest(new TranslationUnitTests("testIsTranslationUnit"));
		suite.addTest(new TranslationUnitTests("testGetChildren"));
		suite.addTest(new TranslationUnitTests("testGetElement"));
		suite.addTest(new TranslationUnitTests("testBug23478A"));
		suite.addTest(new TranslationUnitTests("testBug23478B"));
        suite.addTest(new TranslationUnitTests("testKRFunctionDeclarations"));
		// TODO: suite.addTest(new TranslationUnitTests("testGetElementAtLine"));
		return suite;
    }
    
    public static void main (String[] args){
        junit.textui.TestRunner.run(suite());
    }




    /***
     *  Simple sanity test to make sure TranslationUnit.isTranslationUnit returns true
     *  
     */
   public void testIsTranslationUnit() throws CoreException,FileNotFoundException {
        ITranslationUnit myTranslationUnit;
    
        myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
        assertTrue("A TranslationUnit", myTranslationUnit != null);

    }

    /***
     * Simple sanity tests to make sure TranslationUnit.getChildren seems to 
     * basicly work 
     */
    public void testGetChildren() {
        ITranslationUnit myTranslationUnit;
        ICElement[] elements;
        int x;

        ExpectedStrings expectedString=new ExpectedStrings(expectedStringList);

        myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");

        
        if (myTranslationUnit.hasChildren()) {
            elements=myTranslationUnit.getChildren();
            for (x=0;x<elements.length;x++) {
                expectedString.foundString(elements[x].getElementName());
            }
        }
        assertTrue("PR:23603 " +expectedString.getMissingString(),expectedString.gotAll());
        assertTrue(expectedString.getExtraString(),!expectedString.gotExtra());
    
    }
    
    /***
     * Simple sanity tests for the getElement() call
     */
    public void testGetElement() throws CModelException {
        ITranslationUnit myTranslationUnit;
        ICElement myElement;
        Stack missing=new Stack();
        int x;
        myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
        
        for (x=0;x<expectedStringList.length;x++) {
            myElement=myTranslationUnit.getElement(expectedStringList[x]);
            if (myElement==null)
                missing.push(expectedStringList[x]);
            else {
                assertTrue("Expected:" + expectedStringList[x] + " Got:" + myElement.getElementName(),
                    expectedStringList[x].equals(myElement.getElementName()));
            }
            
        }
        if (!missing.empty()) {
            String output=new String("PR:23603 Could not get elements: ");
            while (!missing.empty())
                output+=missing.pop() + " ";
            assertTrue(output, false);
        }

    }
    
	/***
	 * Simple sanity tests for the getInclude call
	 */
	public void testBug23478A() {
		IInclude myInclude;
		int x;
		String includes[]={"stdio.h", "unistd.h"};
		ITranslationUnit myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
                
		for (x=0; x < includes.length; x++) {
			myInclude=myTranslationUnit.getInclude(includes[x]);
			if (myInclude==null)
				fail("Unable to get include: " + includes[x]);
			else {
				// Failed test: Include.getIncludeName() always returns "";
				// assertTrue
				assertTrue("PR:23478 Expected:"+ new String("") +" Got:"+ myInclude.getIncludeName(), includes[x].equals(myInclude.getIncludeName()));
			}
		}
        

	}
	/***
	 * Simple sanity tests for the getIncludes call
	 */
	public void testBug23478B() throws CModelException {
		IInclude myIncludes[];
		String includes[]={"stdio.h", "unistd.h"};
		ExpectedStrings myExp= new ExpectedStrings(includes);
		int x;
		ITranslationUnit myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
                
		myIncludes=myTranslationUnit.getIncludes();
		for (x=0; x < myIncludes.length; x++) {
			myExp.foundString(myIncludes[x].getIncludeName());
		}
		// Failed test: Include.getIncludeName() always returns "";
		// assertTrue
		assertTrue(myExp.getMissingString(), myExp.gotAll());
		assertTrue(myExp.getExtraString(), !myExp.gotExtra());
	}
    
    
    
    /***
     * Simple sanity tests for the getElementAtLine() call
     */
    public void testGetElementAtLine() throws CoreException {
        ITranslationUnit myTranslationUnit;
        ICElement myElement;
        Stack missing=new Stack();
        int x;
        myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
        
        for (x=0;x<expectedStringList.length;x++) {
            myElement=myTranslationUnit.getElementAtLine(expectedLines[x]);
            if (myElement==null)
                missing.push(expectedStringList[x]);
            else {
                if (expectedStringList[x].equals("mystruct_t")) {
                    assertTrue("PR:23603 expected:" + expectedStringList[x] + " Got:" + myElement.getElementName(),
                        expectedStringList[x].equals(myElement.getElementName()));
                } else {
                    assertTrue("Expected:" + expectedStringList[x] + " Got:" + myElement.getElementName(),
                        expectedStringList[x].equals(myElement.getElementName()));
                }
    
            }
            
        }
        if (!missing.empty()) {
            String output=new String("PR: 23603 Could not get elements: ");
            while (!missing.empty())
                output+=missing.pop() + " ";
            assertTrue(output, false);
        }

    }
    /***
     * Simple sanity tests for the getInclude call
     */
/* Reintroduce this test when Bug# 23478 is fixed
    public void testGetInclude() {
        IInclude myInclude;
        int x;
        String includes[]={"stdio.h", "unistd.h"};
        ITranslationUnit myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
                
        for (x=0;x<includes.length;x++) {
            myInclude=myTranslationUnit.getInclude(includes[x]);
            if (myInclude==null)
                fail("Unable to get include: " + includes[x]);
            else
                assertTrue("PR:23478 Expected:"+includes[x] +" Got:"+ myInclude.getIncludeName(), includes[x].equals(myInclude.getIncludeName()));
        }
        

    }
*/
    /***
     * Simple sanity tests for the getIncludes call
     */
/* Reintroduce this test when Bug# 23478 is fixed
    public void testGetIncludes() throws CModelException {
        IInclude myIncludes[];
        String includes[]={"stdio.h", "unistd.h"};
        ExpectedStrings myExp= new ExpectedStrings(includes);
        int x;
        ITranslationUnit myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
        fail("PR:23478 Unable to test because we can't get the name of an include file"); 
                
        myIncludes=myTranslationUnit.getIncludes();
        for (x=0;x<myIncludes.length;x++) {
            myExp.foundString(myIncludes[x].getIncludeName());
        }
        assertTrue(myExp.getMissingString(), myExp.gotAll());
        assertTrue(myExp.getExtraString(), !myExp.gotExtra());
    }
*/

    /***
     * Simple sanity test for old K&R-style C function declaration
     */
    public void testKRFunctionDeclarations() throws CModelException {
        ITranslationUnit myTranslationUnit = CProjectHelper.findTranslationUnit(testProject,"exetest.c");
        
        assertTrue(myTranslationUnit.getElement("KRFunction") instanceof IFunction);            
        IFunction myKRFunction = (IFunction)myTranslationUnit.getElement("KRFunction"); 
        assertEquals(myKRFunction.getSignature(), "KRFunction(const char*, int(*)(float), parm3)");
        assertEquals(myKRFunction.getReturnType(), "bool");
    }      
}
