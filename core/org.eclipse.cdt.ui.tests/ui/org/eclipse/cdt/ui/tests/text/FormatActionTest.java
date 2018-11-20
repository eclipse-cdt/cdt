/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.ListResourceBundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the Formatter.
 */
public class FormatActionTest extends TestCase {
	private static final String PROJECT = "FormatTests";

	private static final class EmptyBundle extends ListResourceBundle {
		@Override
		protected Object[][] getContents() {
			return new Object[0][];
		}
	}

	protected static class FormatTestSetup extends TestSetup {

		private ICProject fCProject;

		public FormatTestSetup(Test test) {
			super(test);
		}

		@Override
		protected void setUp() throws Exception {
			super.setUp();

			fCProject = EditorTestHelper.createCProject(PROJECT, "resources/formatter");
			fCProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.TAB);
		}

		@Override
		protected void tearDown() throws Exception {
			if (fCProject != null)
				CProjectHelper.delete(fCProject);

			super.tearDown();
		}
	}

	private static final Class<?> THIS = FormatActionTest.class;

	public static Test suite() {
		return new FormatTestSetup(new TestSuite(THIS));
	}

	private CEditor fEditor;
	private SourceViewer fSourceViewer;
	private IDocument fDocument;

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		String filename = createFileName("Before");
		fEditor = (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(filename), true);
		fSourceViewer = EditorTestHelper.getSourceViewer(fEditor);
		fDocument = fSourceViewer.getDocument();
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		EditorTestHelper.closeEditor(fEditor);
	}

	private void assertFormatResult() throws Exception {
		String afterFile = createFileName("After");
		String expected = ResourceTestHelper.read(afterFile).toString();

		IAction formatAction = fEditor.getAction("Format");
		assertNotNull("No format action", formatAction);
		formatAction.run();

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

	public void testTemplates() throws Exception {
		selectAll();
		assertFormatResult();
	}

	public void testPreview() throws Exception {
		selectAll();
		assertFormatResult();
	}

	public void testSample() throws Exception {
		selectAll();
		assertFormatResult();
	}

	public void testComplex() throws Exception {
		selectAll();
		assertFormatResult();
	}

	public void testBugs() throws Exception {
		selectAll();
		assertFormatResult();
	}
}
