/*******************************************************************************
 * Copyright (c) 2015 Patrick Hofer
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Hofer - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests;

import java.io.File;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.PartInitException;

/**
 * Base class for tests. If you want to use outside of this plugin, you need
 * to override {@link #getPlugin()} method and maybe {@link #getSourcePrefix()}
 * method to get source directory for the tests,
 * default is "src". To make it read comment from java class, you need to
 * include this source directory (with test java files) into the build bundle.
 */
public class AnnotationTestCase extends UITestCaseWithProject {
	private IAnnotationModel fAnnotationModel;
	private Object fAnnotationModelLockObject;
	protected Annotation[] annotations;

	private CEditor fEditor;

	protected String testedAnnotationId = Annotation.TYPE_UNKNOWN;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public boolean checkAnnotationType(Annotation a) {
		return true;
	}

	public Annotation checkAnnotationLine(int i) {
		return checkAnnotationLine(currentFile, i);
	}

	public void checkAnnotationLines(Object... args) {
		for (Object i : args) {
			checkAnnotationLine((Integer) i);
		}
	}

	public Annotation checkAnnotationLine(int i, String annotationId) {
		return checkAnnotationLine(currentFile, i, annotationId);
	}

	public Annotation checkAnnotationLine(File file, int expectedLine) {
		return checkAnnotationLine(file, expectedLine, null);
	}

	public Annotation checkAnnotationLine(File file, int expectedLine, String annotationId) {
		assertTrue(annotations != null);
		assertTrue("No annotations found but should", annotations.length > 0); //$NON-NLS-1$
		int line = 0;
		Annotation a = null;
		for (Annotation annotation : annotations) {
			line = getLine(annotation);
			if (line == expectedLine && (annotationId == null || annotationId.equals(annotation.getType()))) {
				a = annotation;
				break;
			}
		}
		assertNotNull(a);
		if (a != null) {
			assertEquals(expectedLine, line);
			assertTrue(checkAnnotationType(a));
		}
		return a;
	}

	private int getLine(Annotation annotation) {
		int line = 0;
		IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		Position position = fAnnotationModel.getPosition(annotation);
		try {
			line = document.getLineOfOffset(position.getOffset()) + 1;
		} catch (BadLocationException e) {
			fail(e.getMessage());
		}
		return line;
	}

	public void checkNoAnnotations() {
		if (annotations == null || annotations.length == 0) {
			// all good
		} else {
			Annotation m = annotations[0];
			fail("Found " + annotations.length + " annotation but should not.");
		}
	}

	public void runOnProject() {
		try {
			indexFiles();
		} catch (CoreException | InterruptedException e) {
			fail(e.getMessage());
		}
		runInEditor();
	}

	public void loadCodeAndRun(String code) {
		loadCode(code);
		runInEditor();
	}

	public void loadCodeAndRunCpp(String code) {
		loadCode(code, true);
		runInEditor();
	}

	protected void runInEditor() {
		try {
			annotations = null;
			fEditor = openCEditor(currentIFile);
			assertNotNull(fEditor);
			EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(fEditor), 100, 1000000, 1000);

			fAnnotationModel = fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
			fAnnotationModelLockObject = getLockObject(fAnnotationModel);
			synchronized (fAnnotationModelLockObject) {
				for (Iterator<Annotation> iter = fAnnotationModel.getAnnotationIterator(); iter.hasNext();) {
					Annotation anotation = iter.next();
					if (anotation != null && (testedAnnotationId.equals(anotation.getType()))) {
						annotations = ArrayUtil.append(Annotation.class, annotations, anotation);
					}
				}
			}
			annotations = ArrayUtil.trim(Annotation.class, annotations);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	private CEditor openCEditor(IFile file) {
		assertNotNull(file);
		assertTrue(file.exists());
		try {
			return (CEditor) EditorTestHelper.openInEditor(file, true);
		} catch (PartInitException e) {
			fail();
			return null;
		}
	}
}
