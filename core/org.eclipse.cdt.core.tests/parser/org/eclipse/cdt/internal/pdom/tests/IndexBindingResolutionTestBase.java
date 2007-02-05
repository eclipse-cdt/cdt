/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.osgi.framework.Bundle;

/**
 * When the PDOM is used to avoid parsing work (i.e. an AST is obtained which
 * is backed by the PDOM), it must be possible to resolve which binding a name
 * in the AST is referring to. If the binding is not defined in the AST fragment
 * then it is assumed to have come from a file which is already indexed.
 * 
 * This class is for testing the process by which bindings are looked up in
 * the PDOM purely from AST information (i.e. without a real binding from the DOM)
 */
public abstract class IndexBindingResolutionTestBase extends PDOMTestBase {

	IIndex index;
	ICProject cproject;
	IPath header, references;
	StringBuffer[] testData;
	IASTTranslationUnit ast;

	
	protected void setUp() throws Exception {
		Bundle b = CTestPlugin.getDefault().getBundle();
		testData = TestSourceReader.getContentsForTest(b, "parser", getClass(), getName(), 2);
		
		IFile file = TestSourceReader.createFile(cproject.getProject(), header, testData[0].toString());
		CCoreInternals.getPDOMManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
		
		IFile cppfile= TestSourceReader.createFile(cproject.getProject(), references, testData[1].toString());
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
		
		index= CCorePlugin.getIndexManager().getIndex(cproject);
		index.acquireReadLock();
		ast = TestSourceReader.createIndexBasedAST(index, cproject, cppfile);
	}
	
	protected void tearDown() throws Exception {
		if (index != null) {
			index.releaseReadLock();
		}
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
		super.tearDown();
	}
		
	protected IBinding getBindingFromASTName(String section, int len) {
		IASTName[] names= ast.getLanguage().getSelectedNames(ast, testData[1].indexOf(section), len);
		assertEquals("<>1 name found for \""+section+"\"", 1, names.length);
		IBinding binding = names[0].resolveBinding();
		assertNotNull("No binding for "+names[0].getRawSignature(), binding);
		assertFalse("Binding is a ProblemBinding for name "+names[0].getRawSignature(), IProblemBinding.class.isAssignableFrom(names[0].resolveBinding().getClass()));
		return names[0].resolveBinding();
	}

	protected IBinding getProblemFromASTName(String section, int len) {
		IASTName[] names= ast.getLanguage().getSelectedNames(ast, testData[1].indexOf(section), len);
		assertEquals("<>1 name found for \""+section+"\"", 1, names.length);
		IBinding binding = names[0].resolveBinding();
		assertNotNull("No binding for "+names[0].getRawSignature(), binding);
		assertTrue("Binding is not a ProblemBinding for name "+names[0].getRawSignature(), IProblemBinding.class.isAssignableFrom(names[0].resolveBinding().getClass()));
		return names[0].resolveBinding();
	}

	protected void assertQNEquals(String expectedQn, IBinding b12) throws DOMException {
		assertTrue(b12 instanceof ICPPBinding);
		assertEquals(expectedQn, CPPVisitor.renderQualifiedName(((ICPPBinding)b12).getQualifiedName()));
	}
	
	protected IType getVariableType(IBinding binding) throws DOMException {
		assertTrue(binding instanceof IVariable);
		return ((IVariable)binding).getType();
	}
	
	protected IType getPtrType(IBinding binding) throws DOMException {
		// assert binding is a variable
		IVariable v = (IVariable) binding;
		IPointerType ptr = (IPointerType) v.getType();
		return ptr.getType();
	}
	
	protected void assertParamType(int index, Class type, IType function) throws DOMException {
		// assert function is IFunctionType
		IFunctionType ft = (IFunctionType) function;
		assertTrue(type.isInstance((ft.getParameterTypes()[index])));
	}
	
	protected void assertCompositeTypeParam(int index, int compositeTypeKey, IType function, String qn) throws DOMException {
		// assert function is IFunctionType
		IFunctionType ft = (IFunctionType) function;
		assertTrue(ICPPClassType.class.isInstance((ft.getParameterTypes()[index])));
		assertEquals(compositeTypeKey, ((ICPPClassType)ft.getParameterTypes()[index]).getKey());
		assertEquals(qn, CPPVisitor.renderQualifiedName(((ICPPClassType)ft.getParameterTypes()[index]).getQualifiedName()));
	}
	
	protected String readTaggedComment(final String tag) throws IOException {
    	return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), "parser", getClass(), tag);
    }
}
