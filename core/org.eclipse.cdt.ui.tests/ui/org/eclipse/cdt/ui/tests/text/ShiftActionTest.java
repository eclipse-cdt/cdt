/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.ListResourceBundle;

import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.texteditor.ShiftAction;

import junit.framework.Test;

/**
 * Test the Shift left/right actions.
 *
 * @since 5.0
 */
public class ShiftActionTest extends BaseUITestCase {
	private static final String PROJECT = "ShiftTests";
	private static final String FILE = "shiftTest.c";

	private static final class EmptyBundle extends ListResourceBundle {
		@Override
		protected Object[][] getContents() {
			return new Object[0][];
		}
	}

	public static Test suite() {
		return suite(ShiftActionTest.class);
	}

	private CEditor fEditor;
	private SourceViewer fSourceViewer;
	private IDocument fDocument;
	private ICProject fCProject;

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		fCProject = CProjectHelper.createCProject(PROJECT, null);
		fCProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
		fCProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, String.valueOf(8));
		fCProject.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, String.valueOf(4));
		IFile file = EditorTestHelper.createFile(fCProject.getProject(), FILE, "", new NullProgressMonitor());
		fEditor = (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(PROJECT + '/' + FILE), true);
		fSourceViewer = EditorTestHelper.getSourceViewer(fEditor);
		fDocument = fSourceViewer.getDocument();
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		EditorTestHelper.closeAllEditors();
		CProjectHelper.delete(fCProject);
		super.tearDown();
	}

	private void shiftLeft() throws Exception {
		new ShiftAction(new EmptyBundle(), "prefix", fEditor, ITextOperationTarget.SHIFT_LEFT).run();
	}

	private void shiftRight() throws Exception {
		new ShiftAction(new EmptyBundle(), "prefix", fEditor, ITextOperationTarget.SHIFT_RIGHT).run();
	}

	private void selectAll() {
		fSourceViewer.setSelectedRange(0, fDocument.getLength());
	}

	//void f() {
	//    for(;;) {
	//}

	//    void f() {
	//	for(;;) {
	//    }
	public void testShiftRight() throws Exception {
		CharSequence[] contents = getContentsForTest(2);
		String before = contents[0].toString();
		String after = contents[1].toString();
		fDocument.set(before);
		selectAll();
		shiftRight();
		assertEquals(after, fDocument.get());
	}

	//    void f() {
	//	for(;;) {
	//    }

	//void f() {
	//    for(;;) {
	//}
	public void testShiftLeft() throws Exception {
		CharSequence[] contents = getContentsForTest(2);
		String before = contents[0].toString();
		String after = contents[1].toString();
		fDocument.set(before);
		selectAll();
		shiftLeft();
		assertEquals(after, fDocument.get());
	}

}
