package org.eclipse.cdt.core.model.tests;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.ExpectedStrings;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the core C model's Archive
 * class. There is nothing exotic here, mostly just sanity type tests
 *
 */
public class ArchiveTests extends TestCase {
    IWorkspace workspace;
    IWorkspaceRoot root;
    ICProject testProject;
    IFile cfile, exefile, libfile, archfile, objfile;
    Path cpath, exepath, libpath, archpath, objpath;
    NullProgressMonitor monitor;
    

    /**
     * Constructor for ArchiveTests
     * @param name
     */
    public ArchiveTests(String name) {
        super(name);
        /***
     * The assume that they have a working workspace
     * and workspace root object to use to create projects/files in, 
     * so we need to get them setup first.
     */
        workspace= ResourcesPlugin.getWorkspace();
        root= workspace.getRoot();
        monitor = new NullProgressMonitor();
        if (workspace==null) 
            fail("Workspace was not setup");
        if (root==null)
            fail("Workspace root was not setup");

    }
    
    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     * 
     * Example code test the packages in the project 
     *  "com.qnx.tools.ide.cdt.core"
     */
    protected void setUp() throws Exception {
            
        /***
         * Setup the various files, paths and projects that are needed by the
         * tests
         */
        testProject=CProjectHelper.createCProject("filetest", "none");
        if (testProject==null)
            fail("Unable to create project");

        cfile = testProject.getProject().getFile("exetest.c");
        if (!cfile.exists()) {
            cfile.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/main.c"))),
					false, monitor);
        }
        cpath=new Path(workspace.getRoot().getLocation()+"/filetest/main.c");

        objfile = testProject.getProject().getFile("exetest.o");
        if (!objfile.exists()) {
            objfile.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/x86/o.g/main.o"))),
					false, monitor);
        }
        objpath=new Path(workspace.getRoot().getLocation()+"/filetest/main.o");
        
        exefile = testProject.getProject().getFile("test_g");
        if (!exefile.exists()) {
            exefile.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/x86/o.g/exe_g"))),
					false, monitor);
        }
        exepath=new Path(workspace.getRoot().getLocation()+"/filetest/exe_g");
        
        archfile = testProject.getProject().getFile("libtestlib_g.a");
        if (!archfile.exists()) {
            archfile.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/testlib/x86/a.g/libtestlib_g.a"))),
					false, monitor);
        }
        libpath=new Path(workspace.getRoot().getLocation()+"/filetest/libtestlib_g.so");
        
        libfile = testProject.getProject().getFile("libtestlib_g.so");
        if (!libfile.exists()) {
            libfile.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/testlib/x86/so.g/libtestlib_g.so"))),
					false, monitor);
        }
        archpath=new Path(workspace.getRoot().getLocation()+"/filetest/libtestlib_g.a");
	    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown()  {
		  CProjectHelper.delete(testProject);
    }
    
    public static TestSuite suite() {
    return new TestSuite(ArchiveTests.class);
    }
    
    public static void main (String[] args){
    junit.textui.TestRunner.run(suite());
    }


        
    public void testGetBinaries() throws CoreException,FileNotFoundException {
        IArchive myArchive;
        IBinary[] bins;
        ICElement[] elements;
        ExpectedStrings expBin, expObj[];
        String[] myStrings;
        int x;

        
        /****
         * Setup the expected strings for the binaries, and the elements within 
         * the binaries
         */
        myStrings=new String[2];
        myStrings[0]="test.o";
        myStrings[1]="test2.o";
        expBin=new ExpectedStrings(myStrings);

        expObj=new ExpectedStrings[2];
        myStrings[0]="func1";
        myStrings[1]="func2";
        expObj[0]=new ExpectedStrings(myStrings);
        myStrings[0]="test2func1";
        myStrings[1]="test2func2";
        expObj[1]=new ExpectedStrings(myStrings);

        /***
         * Grab the archive we want to test, and find all the binaries and 
         * all the elements in all the binaries and make sure we get 
         * everything we expect.
         */
        myArchive=CProjectHelper.findArchive(testProject, "libtestlib_g.a");
        if (myArchive==null)
            fail("Could not find archive");
        bins=myArchive.getBinaries();
        for (x=0;x<bins.length;x++) {
            expBin.foundString(bins[x].getElementName());
            elements=bins[x].getChildren();
            for (int i=0;i<elements.length;i++) {
                expObj[x].foundString(elements[i].getElementName());
            }
        }
        
        assertTrue(expBin.getMissingString(), expBin.gotAll());
        assertTrue(expBin.getExtraString(), !expBin.gotExtra());
        for (x=0;x<expObj.length;x++) {
            assertTrue("Binary " + expBin.expStrings[x] + " "  +expObj[x].getMissingString(), expObj[x].gotAll());
            assertTrue("Binary " + expBin.expStrings[x] + " " + expObj[x].getExtraString(), !expObj[x].gotExtra());
        }
    }
    /***
     *  Simple sanity test to make sure Archive.isArchive returns true
     *  
     */
    public void testIsArchive() throws CoreException,FileNotFoundException {
        IArchive myArchive;
        myArchive=CProjectHelper.findArchive(testProject, "libtestlib_g.a");

        assertTrue("A archive", myArchive != null);
        myArchive=null;


    }

}
