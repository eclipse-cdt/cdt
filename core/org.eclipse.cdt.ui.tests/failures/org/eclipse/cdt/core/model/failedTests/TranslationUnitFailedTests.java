package org.eclipse.cdt.core.model.failedTests;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
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
public class TranslationUnitFailedTests extends TestCase {
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
	public TranslationUnitFailedTests(String name) {
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
				assertFalse("PR:23478 Expected:"+ new String("") +" Got:"+ myInclude.getIncludeName(), includes[x].equals(myInclude.getIncludeName()));
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
		assertFalse(myExp.getMissingString(), myExp.gotAll());
		assertFalse(myExp.getExtraString(), !myExp.gotExtra());
	}
      
}
