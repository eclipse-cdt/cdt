/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.text.selection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.tests.BaseTestCase;

import org.eclipse.cdt.internal.core.pdom.PDOM;

public class ResolveBindingTests extends BaseTestCase  {

	private ICProject fCProject;
	private PDOM fPdom;

	public ResolveBindingTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite("ResolveBindingTests");
		suite.addTestSuite(ResolveBindingTests.class);
		suite.addTest(getFailingTests());
		return suite;
	}

	private static Test getFailingTests() {
		TestSuite suite= new TestSuite("Failing Tests");
        suite.addTest(getFailingTest("_testNamespaceVarBinding2", 156519));
        return suite;
	}
	
	private static Test getFailingTest(String name, int bug) {
		BaseTestCase failingTest= new ResolveBindingTests(name);
		failingTest.setExpectFailure(bug);
		return failingTest;
	}

	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCProject("ResolveBindingTests", "bin");
		CCorePlugin.getPDOMManager().setIndexerId(fCProject, "org.eclipse.cdt.core.fastIndexer");
		fPdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(fCProject);
		fPdom.clear();
	}
		
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			fCProject.getProject().delete(IProject.FORCE | IProject.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
		super.tearDown();
	}

	private IASTName getSelectedName(IASTTranslationUnit astTU, int offset, int len) {
		IASTName[] names= astTU.getLanguage().getSelectedNames(astTU, offset, len);
		assertEquals(1, names.length);
		return names[0];
	}

	private void checkBinding(IASTName name, Class clazz) {
		IBinding binding;
		binding= name.resolveBinding();
		assertNotNull("Cannot resolve binding", binding);
		if (binding instanceof IProblemBinding) {
			IProblemBinding problem= (IProblemBinding) binding;
			fail("Cannot resolve binding: " + problem.getMessage());
		}
		assertTrue(clazz.isInstance(binding));
	}
	
    // {namespace-var-test}
	//	namespace ns {
	//		int var;
	//		void func();
	//	};
	//
	//	void ns::func() {
	//		++var; // r1
	//      ++ns::var; // r2
	//	}
	
	public void testNamespaceVarBinding() throws Exception {
		String content = readTaggedComment("namespace-var-test");
		IFile file= createFile(fCProject.getProject(), "nsvar.cpp", content);
		waitForIndexer(fPdom, file, 2000);
		
		IASTTranslationUnit astTU= createPDOMBasedAST(fCProject, file);
		IASTName name= getSelectedName(astTU, content.indexOf("var"), 3);
		IBinding binding= name.resolveBinding();
		assertTrue(binding instanceof IVariable);

		name= getSelectedName(astTU, content.indexOf("var; // r1"), 3);
		checkBinding(name, IVariable.class);

		name= getSelectedName(astTU, content.indexOf("var; // r2"), 3);
		checkBinding(name, IVariable.class);
	}

	public void _testNamespaceVarBinding2() throws Exception {
		String content = readTaggedComment("namespace-var-test");
		IFile file= createFile(fCProject.getProject(), "nsvar.cpp", content);
		waitForIndexer(fPdom, file, 2000);
		
		IASTTranslationUnit astTU= createPDOMBasedAST(fCProject, file);

		IASTName name= getSelectedName(astTU, content.indexOf("var; // r1"), 3);
		IBinding binding= name.resolveBinding();
		checkBinding(name, IVariable.class);

		name= getSelectedName(astTU, content.indexOf("var; // r2"), 3);
		checkBinding(name, IVariable.class);
	}

}
