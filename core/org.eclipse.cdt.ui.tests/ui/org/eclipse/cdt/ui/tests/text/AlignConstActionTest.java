/*******************************************************************************
 * Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.ListResourceBundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.actions.AlignConstAction;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test for the const alignment action.
 */
public class AlignConstActionTest extends TestCase {
	private static final String PROJECT = "AlignConstTests";

	private static final class EmptyBundle extends ListResourceBundle {
		@Override
		protected Object[][] getContents() {
			return new Object[0][];
		}
	}

	protected static class AlignConstTestSetup extends TestSetup {
		private ICProject fCProject;

		public AlignConstTestSetup(Test test) {
			super(test);
		}

		@Override
		protected void setUp() throws Exception {
			super.setUp();
			fCProject = EditorTestHelper.createCProject(PROJECT, "resources/constalign");
		}

		@Override
		protected void tearDown() throws Exception {
			if (fCProject != null)
				CProjectHelper.delete(fCProject);
			super.tearDown();
		}
	}

	public static Test suite() {
		return new AlignConstTestSetup(new TestSuite(AlignConstActionTest.class));
	}

	private CEditor fEditor;
	private SourceViewer fSourceViewer;
	private IDocument fDocument;

	@Override
	protected void setUp() throws Exception {
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
		prefs.putBoolean(CCorePreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, getName().startsWith("testRight"));
		String filename = createFileName("Before");
		fEditor = (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(filename), true);
		fSourceViewer = EditorTestHelper.getSourceViewer(fEditor);
		fDocument = fSourceViewer.getDocument();
	}

	@Override
	protected void tearDown() throws Exception {
		EditorTestHelper.closeEditor(fEditor);
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
		prefs.putBoolean(CCorePreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, false);
	}

	private void assertAlignConstResult() throws Exception {
		String afterFile = createFileName("After");
		String expected = ResourceTestHelper.read(afterFile).toString();

		new AlignConstAction(new EmptyBundle(), "prefix", fEditor).run();

		assertEquals(expected, fDocument.get());
	}

	private String createFileName(String qualifier) {
		String name = getName();
		name = name.substring(4, 5).toLowerCase() + name.substring(5);
		return "/" + PROJECT + "/src/" + name + "/" + qualifier + ".cpp";
	}

	private void selectAll() {
		fSourceViewer.setSelectedRange(0, fDocument.getLength());
	}

	public void testRightUnchanged() throws Exception {
		selectAll();
		assertAlignConstResult();
	}

	public void testRightChanged() throws Exception {
		selectAll();
		assertAlignConstResult();
	}

	public void testLeftUnchanged() throws Exception {
		selectAll();
		assertAlignConstResult();
	}

	public void testLeftChanged() throws Exception {
		selectAll();
		assertAlignConstResult();
	}
}