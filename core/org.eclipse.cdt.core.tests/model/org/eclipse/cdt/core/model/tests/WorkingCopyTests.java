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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.cdt.testplugin.TestPluginLauncher;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
		String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();

		fCProject= CProjectHelper.createCProject("TestProject1", "bin");
		//Path filePath = new Path(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()+ fCProject.getPath().toString()+ "/WorkingCopyTest.h");
		headerFile = fCProject.getProject().getFile("WorkingCopyTest.h");
		if (!headerFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(pluginRoot+ "resources/cfiles/WorkingCopyTestStart.h"); 
				headerFile.create(fileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if (!fCProject.getProject().hasNature(CCProjectNature.CC_NATURE_ID)) {
			addNatureToProject(fCProject.getProject(), CCProjectNature.CC_NATURE_ID, null);
		}
	}

	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures= description.getNatureIds();
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}

	protected void tearDown()  {
		try{
		  CProjectHelper.delete(fCProject);
		} 
		catch (ResourceException e) {} 
		catch (CoreException e) {} 
	}	
		
		
	public void testWorkingCopy() throws Exception {
		ITranslationUnit tu = new TranslationUnit(fCProject, headerFile);		
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