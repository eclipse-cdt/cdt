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
import java.util.ArrayList;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;


/**
 * @author vhirsl
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CModelElementsFailedTests extends TestCase {
	private ICProject fCProject;
	private IFile headerFile;
	private NullProgressMonitor monitor;
		
	public static Test suite() {
		TestSuite suite= new TestSuite();
		suite.addTest(new CModelElementsFailedTests("testBug36379"));
		return suite;
	}		
		
	public CModelElementsFailedTests(String name) {
		super(name);
	}
		
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();
		String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile();
	
		fCProject= CProjectHelper.createCProject("TestProject1", "bin");
		headerFile = fCProject.getProject().getFile("CModelElementsTest.h");
		if (!headerFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(pluginRoot+ "model/org/eclipse/cdt/core/model/tests/resources/cfiles/CModelElementsTestStart.h"); 
				headerFile.create(fileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if (!fCProject.getProject().hasNature(CCProjectNature.CC_NATURE_ID)) {
			addNatureToProject(fCProject.getProject(), CCProjectNature.CC_NATURE_ID, null);
		}

		CCorePlugin.getDefault().setUseNewParser(true);
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
	
	protected void tearDown() throws Exception {
		CProjectHelper.delete(fCProject);
	}	
			
	public void testBug36379() {
		TranslationUnit tu = new TranslationUnit(fCProject, headerFile);
		// parse the translation unit to get the elements tree		
		Map newElement = tu.parse(true); // require line numbers
		
		// tu ---> namespace: MyPackage
		ArrayList tuPackages = tu.getChildrenOfType(ICElement.C_NAMESPACE);
		INamespace namespace = (INamespace) tuPackages.get(0);
		assertEquals(namespace.getElementName(), new String("MyPackage"));

		// MyPackage ---> class: Hello
		ArrayList nsClasses = namespace.getChildrenOfType(ICElement.C_CLASS);		
		IStructure classHello = (IStructure) nsClasses.get(0);
		assertEquals(classHello.getElementName(), new String("Hello"));

		// Bug 36379: parser does not provide line number information for nested definitions
		assertEquals(0, ((CElement)classHello).getStartLine());
		assertEquals(0, ((CElement)classHello).getEndLine());
	}

}
