/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.model.tests;

import java.io.FileInputStream;

import junit.framework.TestCase;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author jcamelon
 *
 */
public class TranslationUnitBaseTest extends TestCase
{
    /**
     * 
     */
    public TranslationUnitBaseTest()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param name
     */
    public TranslationUnitBaseTest(String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }
    protected IWorkspace workspace;
    protected IWorkspaceRoot root;
    protected ICProject testProject;
    protected IFile objfile;
    protected IFile archfile;
    protected IFile libfile;
    protected IFile exefile;
    protected IFile cfile;
    protected Path objpath;
    protected Path archpath;
    protected Path libpath;
    protected Path exepath;
    protected Path cpath;
    protected NullProgressMonitor monitor;
    /**
         * Sets up the test fixture.
         *
         * Called before every test case method.
         * 
         * Example code test the packages in the project 
         *  "com.qnx.tools.ide.cdt.core"
         */
    protected void setUp() throws Exception
    {
        /***
         * The rest of the tests assume that they have a working workspace
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
            cfile.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/cfiles/TranslationUnits.c"))),
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
    protected void tearDown() 
    {
       // release resources here and clean-up
       try {
		testProject.getProject().delete(true,true,monitor);
	   } catch (ResourceException e) {
	   } catch (CoreException e) {
	   } 
    }
}
