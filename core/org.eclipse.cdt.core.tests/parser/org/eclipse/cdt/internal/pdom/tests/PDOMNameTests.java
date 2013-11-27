package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

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

public class PDOMNameTests extends BaseTestCase {
	private ICProject cproject;

	public static Test suite() {
		return suite(PDOMNameTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cproject= CProjectHelper.createCCProject("PDOMNameTest"+System.currentTimeMillis(), "bin", IPDOMManager.ID_FAST_INDEXER);
		waitForIndexer(cproject);
	}

	@Override
	protected void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
		super.tearDown();
	}

	public void testExternalReferences() throws Exception {
		IProject project = cproject.getProject();
		TestSourceReader.createFile(project, "file.cpp", "extern void func_cpp() { func_cpp(); }");
		TestSourceReader.createFile(project, "file.c", "extern void func_c() { func_c(); }");

		IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		waitForIndexer(cproject);

		PDOM pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		pdom.acquireWriteLock();
		try {
			IIndexBinding[] bindings = pdom.findBindings(new char[][]{"func_cpp".toCharArray()}, IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof PDOMBinding);

			PDOMBinding binding_cpp = (PDOMBinding) bindings[0];
			PDOMName name_cpp = binding_cpp.getFirstReference();
			assertNotNull(name_cpp);
			assertSame(binding_cpp.getLinkage(), name_cpp.getLinkage());

			bindings = pdom.findBindings(new char[][]{"func_c".toCharArray()}, IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof PDOMBinding);

			PDOMBinding binding_c = (PDOMBinding) bindings[0];
			PDOMName name_c = binding_c.getFirstReference();
			assertNotNull(name_c);
			assertSame(binding_c.getLinkage(), name_c.getLinkage());

			// Check that the external references list is currently empty.
			IPDOMIterator<PDOMName> extNames = binding_cpp.getExternalReferences();
			assertNotNull(extNames);
			assertFalse(extNames.hasNext());

			// Make sure the C++ binding and the C name are in different linkages, then add the name
			// as an external reference of the binding.
			assertNotSame(binding_cpp.getLinkage(), name_c.getLinkage());
			binding_cpp.addReference(name_c);

			// Make sure there is an external reference, then retrieve it.  Then make sure there
			// aren't anymore external references.
			extNames = binding_cpp.getExternalReferences();
			assertNotNull(extNames);
			assertTrue(extNames.hasNext());
			PDOMName extRef = extNames.next();
			assertNotNull(extRef);
			assertFalse(extNames.hasNext());

			// Check that the external reference is the same as the C name that was added, that the
			// external reference does not have the same linkage as the binding, and that it does
			// have the same linkage as the initial name.
			assertSame(name_c.getLinkage(), extRef.getLinkage());
			assertEquals(name_c.getRecord(), extRef.getRecord());
			assertNotSame(binding_cpp.getLinkage(), extRef.getLinkage());
			assertSame(binding_c.getLinkage(), extRef.getLinkage());
		} finally {
			pdom.releaseWriteLock();
		}
	}
}
