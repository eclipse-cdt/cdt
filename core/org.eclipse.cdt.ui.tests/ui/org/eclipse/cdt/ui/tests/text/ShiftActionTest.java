/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.ListResourceBundle;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.texteditor.ShiftAction;

import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.internal.ui.editor.CEditor;

/**
 * Test the Shift left/right actions.
 * 
 * @since 5.0
 */
public class ShiftActionTest extends BaseUITestCase {
	private static final String PROJECT= "ShiftTests";
	private static final String FILE = "shiftTest.c";

	private static final class EmptyBundle extends ListResourceBundle {
		@Override
		protected Object[][] getContents() {
			return new Object[0][];
		}
	}

	protected static class ShiftTestSetup extends TestSetup {

		private ICProject fCProject;

		public ShiftTestSetup(Test test) {
			super(test);
		}
		
		@Override
		protected void setUp() throws Exception {
			super.setUp();
			
			fCProject= CProjectHelper.createCProject(PROJECT, null);
			fCProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
			fCProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, String.valueOf(8));
			fCProject.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, String.valueOf(4));
			IFile file= EditorTestHelper.createFile(fCProject.getProject(), FILE, "", new NullProgressMonitor());
		}

		@Override
		protected void tearDown () throws Exception {
			EditorTestHelper.closeAllEditors();
			if (fCProject != null) {
				CProjectHelper.delete(fCProject);
			}
			super.tearDown();
		}
	}
	
	private static final Class<?> THIS= ShiftActionTest.class;
	public static Test suite() {
		return new ShiftTestSetup(new TestSuite(THIS));
	}

	private CEditor fEditor;
	private SourceViewer fSourceViewer;
	private IDocument fDocument;
	private ShiftTestSetup fProjectSetup;

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		if (!ResourcesPlugin.getWorkspace().getRoot().exists(new Path(PROJECT))) {
			fProjectSetup= new ShiftTestSetup(this);
			fProjectSetup.setUp();
		}
		fEditor= (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(PROJECT + '/' + FILE), true);
		fSourceViewer= EditorTestHelper.getSourceViewer(fEditor);
		fDocument= fSourceViewer.getDocument();
		super.setUp();
	}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (fProjectSetup != null) {
			fProjectSetup.tearDown();
		}
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
		CharSequence[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		String after= contents[1].toString();
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
		CharSequence[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		String after= contents[1].toString();
		fDocument.set(before);
		selectAll();
		shiftLeft();
		assertEquals(after, fDocument.get());
	}

}
