package org.eclipse.cdt.core.model.tests;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.cdt.testplugin.*;
import org.eclipse.cdt.core.model.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;


/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the core C model. Nothing 
 * exotic, but should be a small sanity set of tests.
 *
 */
public class CModelTests extends TestCase {
    IWorkspace workspace;
    IWorkspaceRoot root;
    IProject project_c, project_cc;
    NullProgressMonitor monitor;
    String pluginRoot;

    /**
     * Constructor for CModelTests.
     * @param name
     */
    public CModelTests(String name) {
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
    protected void setUp() throws CoreException {
        /***
         * The test of the tests assume that they have a working workspace
         * and workspace root object to use to create projects/files in, 
         * so we need to get them setup first.
         */
        IWorkspaceDescription desc;
        workspace= ResourcesPlugin.getWorkspace();
        root= workspace.getRoot();
        monitor = new NullProgressMonitor();
        if (workspace==null) 
            fail("Workspace was not setup");
        if (root==null)
            fail("Workspace root was not setup");
        pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile();
		desc=workspace.getDescription();
		desc.setAutoBuilding(false);
		workspace.setDescription(desc);

    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() {
       // release resources here and clean-up
    }
    
    public static TestSuite suite() {
        return new TestSuite(CModelTests.class);
    }
    
    public static void main (String[] args){
        junit.textui.TestRunner.run(suite());
    }

    /***
     * Very simple sanity tests to make sure the get*Nature* functions
     * are returning sane values
     */
   public void testGetNatureId(){
        assertTrue("GetCNatureId returns correct value", CoreModel.getDefault().getCNatureId().equals(CoreModel.C_NATURE_ID));
        assertTrue("GetCNatureName returns correct value", CoreModel.getDefault().getCNatureName().equals(CoreModel.C_NATURE_NAME));

        assertTrue("GetCCNatureId returns correct value", CoreModel.getDefault().getCCNatureId().equals(CoreModel.CC_NATURE_ID));
        assertTrue("GetCCNatureName returns correct value", CoreModel.getDefault().getCCNatureName().equals(CoreModel.CC_NATURE_NAME));

        /***
         * The following tests are here to make sure the names/ids of the 
         * natures do not change over time. Changing them would likely break
         * backwards compatibility, so if it happens, we should know about it.
         */
        assertTrue("C_NATURE_NAME is constant", CoreModel.C_NATURE_NAME.equals("cnature"));
        assertTrue("C_NATURE_ID is constant", CoreModel.C_NATURE_ID.equals("org.eclipse.cdt.core.cnature"));
        assertTrue("CC_NATURE_NAME is constant", CoreModel.CC_NATURE_NAME.equals("ccnature"));
        assertTrue("CC_NATURE_ID is constant", CoreModel.CC_NATURE_ID.equals("org.eclipse.cdt.core.ccnature"));


    }

    /***
     * The follow are a simple set of tests to make usre the HasC/CCNature calls
     * seem to be sane.
     * 
     * Assumes that the CProjectHelper.createCProject properly creates a C 
     * project with a C nature, but does not add the CC nature.
     * It also assums that the AddCCNature call works 
     * 
     * @see CProjectHelper#createCProject
     * @see CoreModel#addCCNature
     */
    public void testHasNature() throws CoreException {
        ICProject testProject;
        testProject=CProjectHelper.createCProject("naturetest", "none");
        if (testProject==null)
            fail("Unable to create project");
        assertTrue("hasCNature works", CoreModel.hasCNature(testProject.getProject()));
        assertTrue("hasCCNature works without ccnature", !(CoreModel.hasCCNature(testProject.getProject())));
        CoreModel.addCCNature(testProject.getProject(), monitor);
        assertTrue("hasCCNature works", (CoreModel.hasCCNature(testProject.getProject())));
        
        CoreModel.removeCCNature(testProject.getProject(), monitor);
        CoreModel.removeCNature(testProject.getProject(), monitor);                
        assertTrue("hasCNature works without cnature", !CoreModel.hasCNature(testProject.getProject()));
        assertTrue("hasCCNature works without ccnature or cnature", !(CoreModel.hasCCNature(testProject.getProject())));

    }    

    /***
     * Simple tests to make sure the models file identification methods seem
     * to work as expected.
     */
    public void testFileType() throws CoreException,FileNotFoundException {
        ICProject testProject;
        testProject=CProjectHelper.createCProject("filetest", "none");
        if (testProject==null)
            fail("Unable to create project");

        IFile file = testProject.getProject().getFile("exetest_g");
        if (!file.exists()) {
            file.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/x86/o.g/exe_g"),false, monitor);
        
        }
        /***
         * file should be a binary, executable, not shared or archive
         */
        assertTrue("isBinary", CoreModel.isBinary(file));
        assertTrue("isExecutable", CoreModel.isExecutable(file));
        assertTrue("isSharedLib", !CoreModel.isSharedLib(file));
        assertTrue("isArchive", !CoreModel.isArchive(file));
        assertTrue("isObject", !CoreModel.isObject(file));
        assertTrue("isTranslationUnit", !CoreModel.isTranslationUnit(file));
        
        
        file = testProject.getProject().getFile("exetest.c");
        if (!file.exists()) {
            file.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/main.c"),false, monitor);
        
        }
        /***
         * file should be a translation unit
         */
        assertTrue("isBinary", !CoreModel.isBinary(file));
        assertTrue("isExecutable", !CoreModel.isExecutable(file));
        assertTrue("isSharedLib", !CoreModel.isSharedLib(file));
        assertTrue("isArchive", !CoreModel.isArchive(file));
        assertTrue("isObject", !CoreModel.isObject(file));
        assertTrue("isTranslationUnit", CoreModel.isTranslationUnit(file));
        
        file = testProject.getProject().getFile("exetest.o");
        if (!file.exists()) {
            file.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/x86/o.g/main.o"),false, monitor);
        
        }
        /***
         * file should be a object file unit
         */
        assertTrue("isBinary", CoreModel.isBinary(file));
        assertTrue("isExecutable", !CoreModel.isExecutable(file));
        assertTrue("isSharedLib", !CoreModel.isSharedLib(file));
        assertTrue("isArchive", !CoreModel.isArchive(file));
        assertTrue("isObject", CoreModel.isObject(file));
        assertTrue("isTranslationUnit", !CoreModel.isTranslationUnit(file));

        file = testProject.getProject().getFile("liblibtest_g.so");
        if (!file.exists()) {
            file.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/testlib/x86/so.g/libtestlib_g.so"),false, monitor);
        
        }
        /***
         * file should be a sharedlib/binary file
         */
        assertTrue("isBinary", CoreModel.isBinary(file));
        assertTrue("isExecutable", !CoreModel.isExecutable(file));
        assertTrue("isSharedLib", CoreModel.isSharedLib(file));
        assertTrue("isArchive", !CoreModel.isArchive(file));
        assertTrue("isObject", !CoreModel.isObject(file));
        assertTrue("isTranslationUnit", !CoreModel.isTranslationUnit(file));

        file = testProject.getProject().getFile("liblibtest_g.a");
        if (!file.exists()) {
            file.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/testlib/x86/a.g/libtestlib_g.a"),false, monitor);
        
        } else {
            fail("Does not exist?");
        }
        /***
         * file should be a archive file
         */
        assertTrue("isArchive", CoreModel.isArchive(file));
        assertTrue("isBinary:", !CoreModel.isBinary(file));
        assertTrue("isExecutable", !CoreModel.isExecutable(file));
        assertTrue("isSharedLib", !CoreModel.isSharedLib(file));
        assertTrue("isArchive", CoreModel.isArchive(file));
        assertTrue("isObject", !CoreModel.isObject(file));
        assertTrue("isTranslationUnit", !CoreModel.isTranslationUnit(file));


        testProject.getProject().delete(true,true,monitor);
    }    

    /****
     * Some simple tests for isValidTranslationUnitName
     */
    public void testIsValidTranslationUnitName() throws CoreException {
        assertTrue("Invalid C file", !CoreModel.isValidTranslationUnitName("notcfile"));        
        assertTrue("Invalid C file", !CoreModel.isValidTranslationUnitName("not.c.file"));        
        assertTrue("Invalid C file", !CoreModel.isValidTranslationUnitName("not.ca"));        
        assertTrue("Valid C file", CoreModel.isValidTranslationUnitName("areal.c"));        
    }
}
