/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.includes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.util.OneSourceMultipleHeadersTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

import org.eclipse.cdt.internal.ui.refactoring.includes.BindingClassifier;
import org.eclipse.cdt.internal.ui.refactoring.includes.InclusionContext;

public class BindingClassifierTest extends OneSourceMultipleHeadersTestCase {
	private IIndex fIndex;
	private InclusionContext fContext;
	private BindingClassifier fBindingClassifier;

	public BindingClassifierTest() {
		super(new TestSourceReader(CTestPlugin.getDefault().getBundle(), "ui", BindingClassifierTest.class), true);
	}

	public static TestSuite suite() {
		return suite(BindingClassifierTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IASTTranslationUnit ast = getAst();
		fIndex = CCorePlugin.getIndexManager().getIndex(getCProject(),
				IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT);
		fIndex.acquireReadLock();
		ITranslationUnit tu = ast.getOriginatingTranslationUnit();
		fContext = new InclusionContext(tu, fIndex);
		fBindingClassifier = new BindingClassifier(fContext, ast);
	}

	@Override
	protected void tearDown() throws Exception {
		fIndex.releaseReadLock();
		super.tearDown();
	}

	private void assertDefined(String... names) {
		assertExpectedBindings(names, fBindingClassifier.getBindingsToDefine(), "defined");
	}

	private void assertDeclared(String... names) {
		assertExpectedBindings(names, fBindingClassifier.getBindingsToDeclare(), "declared");
	}

	private void assertExpectedBindings(String[] expectedNames, Set<IBinding> bindings, String verb) {
		Set<String> remaining = new HashSet<String>(Arrays.asList(expectedNames));
		for (IBinding binding : bindings) {
			String name = binding.getName();
			if (!remaining.remove(name)) {
				fail("Binding \"" + name + "\" should not be " + verb);
			}
		}
		if (!remaining.isEmpty())
			fail("Binding \"" + remaining.iterator().next() + "\" is not " + verb);
	}

	//	class A;
	//	typedef A* td1;
	//	typedef td1* td2;
	//	td2 f();

	//	A* a = *f();
	public void testTypedef_1() throws Exception {
		assertDefined("f");
		assertDeclared("A");
	}

	//	class A;
	//	typedef A* td1;
	//	typedef td1* td2;
	//	td2 f();

	//	td1 a = *f();
	public void testTypedef_2() throws Exception {
		assertDefined("f", "td1");
	}

	//	class A { int x; };
	//	typedef A* td;
	//	td f();

	//	int a = f()->x;
	public void testClassMember() throws Exception {
		assertDefined("f", "A");
	}
}
