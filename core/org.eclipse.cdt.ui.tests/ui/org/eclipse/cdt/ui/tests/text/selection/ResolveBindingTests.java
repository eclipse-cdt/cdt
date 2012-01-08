/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.text.selection;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

public class ResolveBindingTests extends BaseUITestCase  {

	private static final int WAIT_FOR_INDEXER = 8000;
	private ICProject fCProject;
	private IIndex fIndex;

	public ResolveBindingTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return suite(ResolveBindingTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject("ResolveBindingTests", "bin", IPDOMManager.ID_FAST_INDEXER);
		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
	}
		
	@Override
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			fCProject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
		super.tearDown();
	}

	private IASTName getSelectedName(IASTTranslationUnit astTU, int offset, int len) {
		// get the language from the language manager
		ILanguage language = null;
		try {
			IProject project = fCProject.getProject();
			ICConfigurationDescription configuration = CoreModel.getDefault().getProjectDescription(project, false).getActiveConfiguration();
			language = LanguageManager.getInstance().getLanguageForFile(astTU.getFilePath(), project, configuration);
		} catch (CoreException e) {
			fail("Unexpected exception while getting language for file.");
		}
		
		assertNotNull("No language for file " + astTU.getFilePath().toString(), language);
		
		IASTName name= astTU.getNodeSelector(null).findName(offset, len);
		assertNotNull(name);
		return name;
	}

	private void checkBinding(IASTName name, Class<?> clazz) {
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
		waitForIndexer(fIndex, file, WAIT_FOR_INDEXER);
		
		IIndex index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit astTU= createIndexBasedAST(index, fCProject, file);
			IASTName name= getSelectedName(astTU, content.indexOf("var"), 3);
			IBinding binding= name.resolveBinding();
			assertTrue(binding instanceof IVariable);

			name= getSelectedName(astTU, content.indexOf("var; // r1"), 3);
			checkBinding(name, IVariable.class);

			name= getSelectedName(astTU, content.indexOf("var; // r2"), 3);
			checkBinding(name, IVariable.class);
		}
		finally {
			index.releaseReadLock();
		}			
	}

	public void testNamespaceVarBinding_156519() throws Exception {
		String content = readTaggedComment("namespace-var-test");
		IFile file= createFile(fCProject.getProject(), "nsvar.cpp", content);
		waitForIndexer(fIndex, file, WAIT_FOR_INDEXER);
		
		IIndex index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit astTU= createIndexBasedAST(index, fCProject, file);

			IASTName name= getSelectedName(astTU, content.indexOf("var; // r1"), 3);
			IBinding binding= name.resolveBinding();
			checkBinding(name, IVariable.class);

			name= getSelectedName(astTU, content.indexOf("var; // r2"), 3);
			checkBinding(name, IVariable.class);
		}
		finally {
			index.releaseReadLock();
		}
	}
	
	// {testMethods.h}
	// class MyClass {
	// public:
	//    void method();
	// };
	
	// {testMethods.cpp}
	// #include "testMethods.h"
	// void MyClass::method() {
	//    method(); // r1
	// }
	//
	// void func() {
    //	   MyClass m, *n;
    //	   m.method(); // r2
    //	   n->method(); // r3
    // }
	public void testMethodBinding_158735() throws Exception {
		String content = readTaggedComment("testMethods.h");
		IFile hfile= createFile(fCProject.getProject(), "testMethods.h", content);
		content = readTaggedComment("testMethods.cpp");
		IFile cppfile= createFile(fCProject.getProject(), "testMethods.cpp", content);
		waitForIndexer(fIndex, hfile, WAIT_FOR_INDEXER);
		
		IIndex index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit astTU= createIndexBasedAST(index, fCProject, cppfile);

			IASTName name= getSelectedName(astTU, content.indexOf("method"), 6);
			IBinding binding= name.resolveBinding();
			checkBinding(name, ICPPMethod.class);

			name= getSelectedName(astTU, content.indexOf("method(); // r1"), 6);
			checkBinding(name, ICPPMethod.class);

			name= getSelectedName(astTU, content.indexOf("method(); // r2"), 6);
			checkBinding(name, ICPPMethod.class);

			name= getSelectedName(astTU, content.indexOf("method(); // r3"), 6);
			checkBinding(name, ICPPMethod.class);
		}
		finally {
			index.releaseReadLock();
		}
	}

}
