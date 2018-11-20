/*******************************************************************************
 * Copyright (c) 2009, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.Collection;
import java.util.ListResourceBundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.ui.editor.AddIncludeAction;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.refactoring.includes.IElementSelector;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the AddIncludeOnSelectionAction.
 */
public class AddIncludeTest extends BaseTestCase {
	private static final String PROJECT = "AddIncludeTests";

	private static final class EmptyBundle extends ListResourceBundle {
		@Override
		protected Object[][] getContents() {
			return new Object[0][];
		}
	}

	protected static class AddIncludeTestSetup extends TestSetup {
		private ICProject fCProject;

		public AddIncludeTestSetup(Test test) {
			super(test);
		}

		@Override
		protected void setUp() throws Exception {
			super.setUp();
			fCProject = EditorTestHelper.createCProject(PROJECT, "resources/addInclude");
			CCorePlugin.getIndexManager().setIndexerId(fCProject, IPDOMManager.ID_FAST_INDEXER);
			waitForIndexer(fCProject);
		}

		@Override
		protected void tearDown() throws Exception {
			if (fCProject != null)
				CProjectHelper.delete(fCProject);
			super.tearDown();
		}
	}

	public static Test suite() {
		return new AddIncludeTestSetup(new TestSuite(AddIncludeTest.class));
	}

	private CEditor fEditor;
	private SourceViewer fSourceViewer;
	private IDocument fDocument;

	@Override
	protected void setUp() throws Exception {
		String filename = createFileName("");
		fEditor = (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(filename), true);
		fSourceViewer = EditorTestHelper.getSourceViewer(fEditor);
		fDocument = fSourceViewer.getDocument();
		IWorkingCopy tu = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		tu.makeConsistent(new NullProgressMonitor(), true);
	}

	@Override
	protected void tearDown() throws Exception {
		EditorTestHelper.closeEditor(fEditor);
	}

	private void assertAddIncludeResult() throws Exception {
		AddIncludeAction action = new AddIncludeAction(fEditor);
		action.setAmbiguityResolver(new IElementSelector() {
			@Override
			public <T> T selectElement(Collection<T> elements) {
				switch (elements.size()) {
				case 0:
					return null;
				case 1:
					return elements.iterator().next();
				}
				throw new RuntimeException("Ambiguous input: " + elements); //$NON-NLS-1$
			}
		});
		action.run();

		String file = createFileName(".expected");
		String expected = ResourceTestHelper.read(file).toString();
		assertEquals(expected.replace("\r\n", "\n"), fDocument.get().replace("\r\n", "\n"));
	}

	private String createFileName(String suffix) {
		String name = getName();
		name = name.substring(4); // Strip "test" prefix.
		return "/" + PROJECT + "/src/" + name + ".cpp" + suffix;
	}

	private void select(String name) {
		final int offset = fDocument.get().indexOf(name);
		assertTrue(offset >= 0);
		fSourceViewer.setSelectedRange(offset, name.length());
	}

	public void testOverloadedFunction() throws Exception {
		select("func");
		assertAddIncludeResult();
	}

	public void testResolvedName() throws Exception {
		select("A");
		assertAddIncludeResult();
	}

	public void testUnresolvedName() throws Exception {
		select("B");
		assertAddIncludeResult();
	}

	public void testVariableType() throws Exception {
		select("a_");
		assertAddIncludeResult();
	}

	public void testMacro() throws Exception {
		select("ONE");
		assertAddIncludeResult();
	}

	public void testInsertionPoint_301780() throws Exception {
		select("XXX");
		assertAddIncludeResult();
	}

	public void testInsertionPoint_435340() throws Exception {
		select("A435340");
		assertAddIncludeResult();
	}

	public void testTemplate_306670() throws Exception {
		select("func306670");
		assertAddIncludeResult();
	}

	public void testEnumerator_307738() throws Exception {
		select("ENUM_VALUE");
		assertAddIncludeResult();
	}
}
