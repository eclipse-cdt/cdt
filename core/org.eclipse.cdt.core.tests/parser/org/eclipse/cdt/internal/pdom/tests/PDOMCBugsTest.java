/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;

public class PDOMCBugsTest extends BaseTestCase5 {
	ICProject cproject;
	PDOM pdom;

	@BeforeEach
	protected void beforeEach() throws Exception {
		cproject = CProjectHelper.createCProject("PDOMCBugsTest" + System.currentTimeMillis(), "bin",
				IPDOMManager.ID_NO_INDEXER);
		Bundle b = CTestPlugin.getDefault().getBundle();
		CharSequence[] testData = TestSourceReader.getContentsForTest(b, "parser", PDOMCBugsTest.this.getClass(),
				getName(), 1);

		IFile file = TestSourceReader.createFile(cproject.getProject(), new Path("header.h"), testData[0].toString());
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		waitForIndexer(cproject);

		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
	}

	@AfterEach
	protected void afterEach() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
					new NullProgressMonitor());
		}
	}

	// // check we get the right IProblemBinding objects
	// typedef A B;
	// typedef C D;
	// typedef E E;
	// typedef typeof(G) G;
	// typedef H *H;
	// typedef I *************I;
	// typedef int (*J)(J);
	@Test
	public void test192165() throws Exception {
		pdom.acquireReadLock();
		IBinding[] bindings = pdom.findBindings(Pattern.compile(".*"), false, IndexFilter.ALL, npm());
		assertEquals(7, bindings.length);
		Set bnames = new HashSet();
		for (IBinding binding : bindings) {
			final String name = binding.getName();
			bnames.add(name);
			assertTrue(binding instanceof ITypedef, "expected typedef, got " + binding);
			IType type = SemanticUtil.getUltimateType((IType) binding, false);
			if (name.equals("J")) {
				// for plain C the second J has to be interpreted as a (useless) parameter name.
				assertTrue(type instanceof IFunctionType);
				IFunctionType ft = (IFunctionType) type;
				assertEquals("int (int)", ASTTypeUtil.getType(ft));
			} else {
				assertTrue(type instanceof IProblemType);
			}
		}

		Set expected = new HashSet(Arrays.asList(new String[] { "B", "D", "E", "G", "H", "I", "J" }));
		assertEquals(expected, bnames);

		pdom.releaseReadLock();
	}
}
