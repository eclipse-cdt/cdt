/*******************************************************************************
 * Copyright (c) 2013, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMIterator;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.Test;

public class PDOMNameTests extends BaseTestCase {
	private ICProject cproject;

	public static Test suite() {
		return suite(PDOMNameTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cproject = CProjectHelper.createCCProject("PDOMNameTest" + System.currentTimeMillis(), "bin",
				IPDOMManager.ID_FAST_INDEXER);
		waitForIndexer(cproject);
	}

	@Override
	protected void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
					new NullProgressMonitor());
		}
		super.tearDown();
	}

	public void testExternalReferences() throws Exception {
		IProject project = cproject.getProject();
		// Use enum because this uses a different NodeType in C++ and C.
		TestSourceReader.createFile(project, "file.cpp",
				"enum E_cpp { e_cpp }; extern E_cpp func_cpp() { func_cpp(); return e_cpp; }");
		TestSourceReader.createFile(project, "file.c",
				"enum E_c { e_c }; extern enum E_c func_c() { func_c(); return e_c; }");

		IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		waitForIndexer(cproject);

		PDOM pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		pdom.acquireWriteLock(null);
		try {
			IIndexBinding[] bindings = pdom.findBindings(new char[][] { "E_cpp".toCharArray() }, IndexFilter.ALL,
					npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof PDOMBinding);

			PDOMBinding binding_cpp = (PDOMBinding) bindings[0];
			PDOMName name_cpp = binding_cpp.getFirstReference();
			assertNotNull(name_cpp);
			assertSame(binding_cpp.getLinkage(), name_cpp.getLinkage());

			bindings = pdom.findBindings(new char[][] { "E_c".toCharArray() }, IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof PDOMBinding);

			PDOMBinding binding_c = (PDOMBinding) bindings[0];
			PDOMName name_c1 = binding_c.getFirstReference();
			assertNotNull(name_c1);
			assertSame(binding_c.getLinkage(), name_c1.getLinkage());

			// Check that the external references list is currently empty.
			assertEquals(0, countExternalReferences(binding_cpp));

			// Make sure the C++ binding and the C name are in different linkages, then add the name
			// as an external reference of the binding.  The case we're setting up is:
			//
			//     C++_Binding is referenced-by a C_Name which has a C++_Binding
			//
			// We can then test the following (see reference numbers below):
			//     1) Getting the C name as an external reference of the C++ binding
			//     2) Loading the C++ binding from the C name
			assertNotSame(binding_cpp.getLinkage(), name_c1.getLinkage());
			name_c1.setBinding(binding_cpp);
			binding_cpp.addReference(name_c1);

			// Make sure there is an external reference, then retrieve it.  Then make sure there
			// aren't anymore external references.
			IPDOMIterator<PDOMName> extNames = binding_cpp.getExternalReferences();
			assertNotNull(extNames);
			assertTrue(extNames.hasNext());
			PDOMName extRef = extNames.next();
			assertNotNull(extRef);
			assertFalse(extNames.hasNext());

			// 1) Check that the external reference is the same as the C name that was added, that the
			//    external reference does not have the same linkage as the binding, and that it does
			//    have the same linkage as the initial name.
			assertSame(name_c1.getLinkage(), extRef.getLinkage());
			assertEquals(name_c1.getRecord(), extRef.getRecord());
			assertNotSame(binding_cpp.getLinkage(), extRef.getLinkage());
			assertSame(binding_c.getLinkage(), extRef.getLinkage());

			// 2) Make sure that the C name was able to properly load the C++ binding.
			PDOMBinding extBinding = extRef.getBinding();
			assertNotNull(extBinding);
			assertEquals(binding_cpp.getRecord(), extBinding.getRecord());
			assertEquals(binding_cpp.getNodeType(), extBinding.getNodeType());
			assertTrue(binding_cpp.getClass() == extBinding.getClass());

			// Bug 426238: When names are deleted they need to be removed from the external references
			//             list.  This test puts 3 names into the list and then removes the 2nd, last,
			//             and then first.

			// Add more external references and then check removals.
			PDOMName name_c2 = name_c1.getNextInFile();
			assertNotNull(name_c2);
			PDOMName name_c3 = name_c2.getNextInFile();
			name_c2.setBinding(binding_cpp);
			binding_cpp.addReference(name_c2);
			name_c3.setBinding(binding_cpp);
			binding_cpp.addReference(name_c3);

			// Collect the 3 names from the external reference list.
			extNames = binding_cpp.getExternalReferences();
			assertNotNull(extNames);
			assertTrue(extNames.hasNext());
			PDOMName extRef_1 = extNames.next();
			assertNotNull(extRef);
			assertTrue(extNames.hasNext());
			PDOMName extRef_2 = extNames.next();
			assertTrue(extNames.hasNext());
			PDOMName extRef_3 = extNames.next();
			assertFalse(extNames.hasNext());

			// Check that the list is ordered as expected (reversed during insertion).
			assertEquals(name_c3.getRecord(), extRef_1.getRecord());
			assertEquals(name_c2.getRecord(), extRef_2.getRecord());
			assertEquals(name_c1.getRecord(), extRef_3.getRecord());

			// 3) Check deleting of middle entry.
			extRef_2.delete();
			assertEquals(2, countExternalReferences(binding_cpp));

			// 4) Make sure extref head is updated when there is a next name.
			extRef_1.delete();
			assertEquals(1, countExternalReferences(binding_cpp));
			extNames = binding_cpp.getExternalReferences();
			assertNotNull(extNames);
			assertTrue(extNames.hasNext());
			assertEquals(extRef_3.getRecord(), extNames.next().getRecord());
			assertFalse(extNames.hasNext());

			// 5) Check deleting of first entry.
			extRef_3.delete();
			assertEquals(0, countExternalReferences(binding_cpp));
		} finally {
			pdom.releaseWriteLock();
		}
	}

	private static int countExternalReferences(PDOMBinding binding) throws Exception {
		IPDOMIterator<PDOMName> extRefs = binding.getExternalReferences();
		assertNotNull(extRefs);
		int count = 0;
		for (; extRefs.hasNext(); extRefs.next())
			++count;
		return count;
	}
}
