package org.eclipse.cdt.core.model.tests;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.cdt.testplugin.*;
import org.eclipse.cdt.testplugin.util.*;
import org.eclipse.cdt.core.model.*;
import org.eclipse.core.internal.runtime.Log;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.internal.core.model.*;


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
        "func1", "func2", "main", "func3"};
    int[]  expectedLines={ 12,14,17,20,23,28,32,35,42,47,53,58,65};
    /* This is a list of that the types of the above list of elements is 
     * expected to be.
     */
    int[] expectedTypes= { ICElement.C_INCLUDE, ICElement.C_INCLUDE, 
        ICElement.C_FUNCTION_DECLARATION, ICElement.C_VARIABLE, 
        ICElement.C_ENUMERATION, ICElement.C_STRUCT, ICElement.C_TYPEDEF, 
        ICElement.C_UNION, ICElement.C_TYPEDEF, ICElement.C_FUNCTION,
        ICElement.C_FUNCTION, ICElement.C_FUNCTION,ICElement.C_FUNCTION};
    

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
        return new TestSuite(TranslationUnitTests.class);
    }
    
    public static void main (String[] args){
        junit.textui.TestRunner.run(suite());
    }


    /***
     * This is a simple test to make sure we can not create an TranslationUnit with
     * a non-TranslationUnit Ifile/IPath
     */
    public void testTranslationUnit() throws CoreException {
        TranslationUnit myTranslationUnit;
        boolean caught;

        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, cfile);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("Created an TranslationUnit with a C file", !caught);
        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, cpath);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("Created an TranslationUnit with a C file", !caught);

        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, objfile);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("PR:13037 Created an TranslationUnit with a .o file", caught);
        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, objpath);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("Created an TranslationUnit with a .o file", caught);

        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, exefile);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("Created an TranslationUnit with a exe file", caught);
        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, exepath);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("Created an TranslationUnit with a exe file", caught);

        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, libfile);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("Created an TranslationUnit with a .so file", caught);
        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, libpath);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("Created an TranslationUnit with a .so file", caught);

        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, archfile);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("Created an TranslationUnit with a .a file", caught);
        myTranslationUnit=null;
        caught=false;
        try {
            myTranslationUnit=new TranslationUnit(testProject, archpath);
        } catch  (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue("Created an TranslationUnit with a .a file", caught);
            

    }


    /***
     *  Simple sanity test to make sure TranslationUnit.isTranslationUnit returns true
     *  
     */
   public void testIsTranslationUnit() throws CoreException,FileNotFoundException {
        TranslationUnit myTranslationUnit;
    
        myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
        assertTrue("A TranslationUnit", myTranslationUnit.isTranslationUnit());

    }

    /***
     * Simple sanity tests to make sure TranslationUnit.getChildren seems to 
     * basicly work 
     */
    public void testGetChildern() {
        TranslationUnit myTranslationUnit;
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
        assertTrue("PR:13062 " +expectedString.getMissingString(),expectedString.gotAll());
        assertTrue(expectedString.getExtraString(),!expectedString.gotExtra());
    
    }
    
    /***
     * Simple sanity tests for the getElement() call
     */
    public void testGetElement() {
        TranslationUnit myTranslationUnit;
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
            String output=new String("PR: 13062 Could not get elements: ");
            while (!missing.empty())
                output+=missing.pop() + " ";
            assertTrue(output, false);
        }

    }
    /***
     * Simple sanity tests for the getElementAtLine() call
     */
    public void testGetElementAtLine() throws CoreException {
        TranslationUnit myTranslationUnit;
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
                    assertTrue("PR: 13062 expected:" + expectedStringList[x] + " Got:" + myElement.getElementName(),
                        expectedStringList[x].equals(myElement.getElementName()));
                } else {
                    assertTrue("Expected:" + expectedStringList[x] + " Got:" + myElement.getElementName(),
                        expectedStringList[x].equals(myElement.getElementName()));
                }
    
            }
            
        }
        if (!missing.empty()) {
            String output=new String("PR: 13062 Could not get elements: ");
            while (!missing.empty())
                output+=missing.pop() + " ";
            assertTrue(output, false);
        }

    }
    /***
     * Simple sanity tests for the getInclude call
     */
    public void testGetInclude() {
        Include myInclude;
        int x;
        String includes[]={"stdio.h", "unistd.h"};
        TranslationUnit myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
                
        for (x=0;x<includes.length;x++) {
            myInclude=(Include)myTranslationUnit.getInclude(includes[x]);
            if (myInclude==null)
                fail("Unable to get include: " + includes[x]);
            else
                assertTrue("PR:BZ23478 Expected:"+includes[x] +" Got:"+ myInclude.getIncludeName(), includes[x].equals(myInclude.getIncludeName()));
        }
        

    }
    /***
     * Simple sanity tests for the getIncludes call
     */
    public void testGetIncludes() throws CModelException {
        IInclude myIncludes[];
        String includes[]={"stdio.h", "unistd.h"};
        ExpectedStrings myExp= new ExpectedStrings(includes);
        int x;
        TranslationUnit myTranslationUnit=CProjectHelper.findTranslationUnit(testProject,"exetest.c");
        fail("Unable to test because we can't get the name of an include file (PR: BZ23478"); 
                
        myIncludes=myTranslationUnit.getIncludes();
        for (x=0;x<myIncludes.length;x++) {
            myExp.foundString(myIncludes[x].getIncludeName());
        }
        assertTrue(myExp.getMissingString(), myExp.gotAll());
        assertTrue(myExp.getExtraString(), !myExp.gotExtra());
        

    }
    
    
    
    
}
