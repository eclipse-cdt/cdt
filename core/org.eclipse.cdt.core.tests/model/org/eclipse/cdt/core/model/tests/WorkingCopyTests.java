package org.eclipse.cdt.core.model.tests;
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.TestPluginLauncher;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Contains unit test cases for Working Copies. Run using JUnit Plugin Test
 * configuration launcher.
 */
public class WorkingCopyTests extends TestCase {	
	private ICProject fCProject;
	private IFile headerFile;
	private NullProgressMonitor monitor;
	
	public static void main(String[] args) {
		TestPluginLauncher.run(TestPluginLauncher.getLocationFromProperties(), WorkingCopyTests.class, args);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(WorkingCopyTests.class.getName());
		suite.addTest(new WorkingCopyTests("testWorkingCopy"));
		//suite.addTest(new WorkingCopyTests("testHashing"));
		return suite;
	}		
	
	public WorkingCopyTests(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();
	
		fCProject= CProjectHelper.createCCProject("TestProject1", "bin");
		//Path filePath = new Path(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()+ fCProject.getPath().toString()+ "/WorkingCopyTest.h");
		headerFile = fCProject.getProject().getFile("WorkingCopyTest.h");
		if (!headerFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path("resources/cfiles/WorkingCopyTestStart.h"))); 
				headerFile.create(fileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	protected void tearDown()  {
		CProjectHelper.delete(fCProject);
	}	
		
		
	public void testWorkingCopy() throws Exception {
		ITranslationUnit tu = (ITranslationUnit)CoreModel.getDefault().create(headerFile);
		// CreateWorkingCopy		
		assertNotNull (tu);
		IWorkingCopy wc = tu.getWorkingCopy();
		assertNotNull (wc);
		assertNotNull (wc.getBuffer());
		assertTrue (wc.exists());
		
		// ModifyWorkingCopy
		IBuffer wcBuf = wc.getBuffer();
		wcBuf.append("\n class Hello{ int x; };");
		if (tu.getBuffer().getContents().equals(wc.getBuffer().getContents() ) )
			fail("Buffers should NOT be equal at this point!");		
		
		// ReconcileWorkingCopy
		wc.reconcile();
		
		// CommitWorkingCopy
		wc.commit(true, monitor);
		
		if(!tu.getBuffer().getContents().equals(wc.getBuffer().getContents())) 
			fail("Buffers should be equal at this point!");
		
		// DestroyWorkingCopy
		wc.destroy();
		assertFalse(wc.exists());	
		
		Thread.sleep(1000);	
	}
}