/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.SortLinesAction;

/**
 * Tests for the SortLinesAction.
 *
 * @since 5.2
 */
public class SortLinesTest extends BaseUITestCase {
	private static final String PROJECT = "SortLinesTest";
	private static final String FILE = "test.cpp";

	private static final class EmptyBundle extends ListResourceBundle {
		@Override
		protected Object[][] getContents() {
			return new Object[0][];
		}
	}

	protected static class SortLinesTestSetup extends TestSetup {
		private ICProject fCProject;

		public SortLinesTestSetup(Test test) {
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

	private static final Class<?> THIS= SortLinesTest.class;
	public static Test suite() {
		return new SortLinesTestSetup(new TestSuite(THIS));
	}

	private CEditor fEditor;
	private SourceViewer fSourceViewer;
	private IDocument fDocument;
	private SortLinesTestSetup fProjectSetup;

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		if (!ResourcesPlugin.getWorkspace().getRoot().exists(new Path(PROJECT))) {
			fProjectSetup= new SortLinesTestSetup(this);
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

	private void sortLines() throws Exception {
		new SortLinesAction(fEditor).run();
	}

	/**
	 * Selects part of the document.
	 *
	 * @param startLine First line of the selection. Zero based.
	 * @param startPosition Start position of the selection in startLine. Zero based.
	 * @param endLine Last line of the selection. Zero based.
	 * @param endPosition Position after the end of the selection in endLine. Zero based.
	 */
	private void select(int startLine, int startPosition, int endLine, int endPosition)
			throws BadLocationException {
		int offset = fDocument.getLineOffset(startLine) + startPosition;
		fSourceViewer.setSelectedRange(offset, fDocument.getLineOffset(endLine) + endPosition - offset);
	}

	/**
	 * Selects the whole document.
	 */
	private void selectAll() {
		fSourceViewer.setSelectedRange(0, fDocument.getLength());
	}

	//	// e.h
	//	#include "e.h"
	//	#include "bbb.h"
	//	#include "dd.h"
	//	/*
	//	 * ccccc.h
	//	 */
	//	#include "ccccc.h"
	//	#include "aaaa.h"

	//	#include "aaaa.h"
	//	#include "bbb.h"
	//	/*
	//	 * ccccc.h
	//	 */
	//	#include "ccccc.h"
	//	#include "dd.h"
	//	// e.h
	//	#include "e.h"
	public void testSortLinesMixed() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		String after= contents[1].toString();
		fDocument.set(before);
		selectAll();
		sortLines();
		assertEquals(after, fDocument.get());
	}

	//	/*
	//	 * Ganymede
	//	 * Europa
	//	 * Callisto
	//	 */

	//	/*
	//	 * Europa
	//	 * Ganymede
	//	 * Callisto
	//	 */
	public void testSortLinesCommentsOnly() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		String after= contents[1].toString();
		fDocument.set(before);
		select(1, 0, 3, 0);
		sortLines();
		assertEquals(after, fDocument.get());
	}
}
