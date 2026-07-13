/*******************************************************************************
 * Copyright (c) 2026 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - initial implementation (#1455)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.editor;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.internal.ui.editor.CSourceStickyLinesProcessor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CSourceStickyLinesProcessorTest extends BaseTestCase5 {

	private static final String RESOURCES_FOLDER = "resources/stickyLines"; //$NON-NLS-1$
	private static final String PROJECT_NAME = "CSourceStickyLinesProcessorTest"; //$NON-NLS-1$
	private static final String SOURCE_PATH = "src/stickyLines.cpp"; //$NON-NLS-1$
	private final CDocumentProvider fDocProvider;

	private ICProject fCProject;

	public CSourceStickyLinesProcessorTest() {
		super();
		fDocProvider = CUIPlugin.getDefault().getDocumentProvider();
	}

	@BeforeEach
	public void setup() throws Exception {
		fCProject = EditorTestHelper.createCProject(PROJECT_NAME, RESOURCES_FOLDER);
	}

	@AfterEach
	protected void teardown() throws Exception {
		if (null != fCProject) {
			CProjectHelper.delete(fCProject);
		}
	}

	@Test
	public void testStickyLinesProcessor() throws CoreException {
		final IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).getFile(SOURCE_PATH);
		final FileEditorInput input = new FileEditorInput(sourceFile);
		fDocProvider.connect(input);
		final IDocument doc = fDocProvider.getDocument(input);
		final ITranslationUnit tu = fDocProvider.getWorkingCopy(input);
		tu.open(null);
		final IStatus status = CUIPlugin.getDefault().getASTProvider().runOnAST(tu, ASTProvider.WAIT_IF_OPEN, null,
				(lang, ast) -> {
					final CSourceStickyLinesProcessor processor = new CSourceStickyLinesProcessor(doc, ast);
					try {
						testStickyLinesCalculation(processor);
					} catch (BadLocationException e) {
						return Status.error(e.getMessage(), e);
					}
					return Status.OK_STATUS;
				});
		tu.close();
		fDocProvider.disconnect(input);
		assertTrue(status.isOK(), status.getMessage());
	}

	// NOTE: CSourceStickyLinesProcessor uses zero-based IDocument line numbers for input and output
	// Subtract one from source file line numbers in stickyLines.cpp to obtain IDocument line numbers

	private List<Integer> doc(List<Integer> sourceLines) {
		return sourceLines.stream().map(i -> i - 1).toList();
	}

	private int doc(int sourceLine) {
		return sourceLine - 1;
	}

	private void testStickyLinesCalculation(CSourceStickyLinesProcessor processor) throws BadLocationException {
		assertIterableEquals(doc(List.of(8, 9, 10)), processor.calculateStickyLines(doc(11)), "TEST1 - line 11");
		assertIterableEquals(doc(List.of(17, 18)), processor.calculateStickyLines(doc(19)), "TEST2 - line 19");
		assertIterableEquals(doc(List.of(17, 18, 20)), processor.calculateStickyLines(doc(21)), "TEST2 - line 21");
		assertIterableEquals(doc(List.of(26, 27)), processor.calculateStickyLines(doc(30)), "TEST3 - line 30");
		assertIterableEquals(doc(List.of(26, 27, 31, 32)), processor.calculateStickyLines(doc(33)), "TEST3 - line 33");
	}

}
