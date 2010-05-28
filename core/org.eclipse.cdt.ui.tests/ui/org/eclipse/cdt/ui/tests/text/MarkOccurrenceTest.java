/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import java.util.Iterator;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AnnotationPreference;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.DisplayHelper;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.cdt.internal.ui.viewsupport.ISelectionListenerWithAST;
import org.eclipse.cdt.internal.ui.viewsupport.SelectionListenerWithASTManager;


/**
 * Tests the C/C++ Editor's occurrence marking feature.
 * 
 * @since 5.0
 */
public class MarkOccurrenceTest extends BaseUITestCase {
	
	private static final String PROJECT = "MarkOccurrenceTest";

	private static final String OCCURRENCE_ANNOTATION= "org.eclipse.cdt.ui.occurrences";
	private static final RGB fgHighlightRGB= getHighlightRGB();
	
	private CEditor fEditor;
	private IDocument fDocument;
	private FindReplaceDocumentAdapter fFindReplaceDocumentAdapter;
	private int fOccurrences;
	private IAnnotationModel fAnnotationModel;
	private ISelectionListenerWithAST fSelWASTListener;
	private IRegion fMatch;
	private StyledText fTextWidget;

	private MarkOccurrenceTestSetup fProjectSetup;

	protected static class MarkOccurrenceTestSetup extends TestSetup {
		private ICProject fCProject;
		
		public MarkOccurrenceTestSetup(Test test) {
			super(test);
		}
		@Override
		protected void setUp() throws Exception {
			super.setUp();
			fCProject= EditorTestHelper.createCProject(PROJECT, "resources/ceditor", false, true);
		}
		@Override
		protected void tearDown() throws Exception {
			if (fCProject != null)
				CProjectHelper.delete(fCProject);
			
			super.tearDown();
		}
	}
	
	public static Test setUpTest(Test someTest) {
		return new MarkOccurrenceTestSetup(someTest);
	}
	
	public static Test suite() {
		return setUpTest(new TestSuite(MarkOccurrenceTest.class));
	}
	
	@Override
	protected void setUp() throws Exception {
		if (!ResourcesPlugin.getWorkspace().getRoot().exists(new Path(PROJECT))) {
			fProjectSetup= new MarkOccurrenceTestSetup(this);
			fProjectSetup.setUp();
		}
		assertNotNull(fgHighlightRGB);
		final IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_MARK_OCCURRENCES, true);
	    // TLETODO temporary fix for bug 314635
		store.setValue(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX 
				            + SemanticHighlightings.OVERLOADED_OPERATOR 
				            + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX, true);
		fEditor= openCEditor(new Path("/" + PROJECT + "/src/occurrences.cpp"));
		assertNotNull(fEditor);
		fTextWidget= fEditor.getViewer().getTextWidget();
		assertNotNull(fTextWidget);
		EditorTestHelper.joinReconciler((SourceViewer)fEditor.getViewer(), 10, 200, 20);
		fDocument= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		assertNotNull(fDocument);
		fFindReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		fAnnotationModel= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
	
		fMatch= null;
		fSelWASTListener= new ISelectionListenerWithAST() {
			public void selectionChanged(IEditorPart part, ITextSelection selection, IASTTranslationUnit astRoot) {
				if (fMatch != null && selection != null && selection.getOffset() == fMatch.getOffset() && selection.getLength() == fMatch.getLength()) {
					countOccurrences();
				}
			}
	
			private synchronized void countOccurrences() {
				fOccurrences= 0;
				Iterator<Annotation> iter= fAnnotationModel.getAnnotationIterator();
				while (iter.hasNext()) {
					Annotation annotation= iter.next();
					if (OCCURRENCE_ANNOTATION.equals(annotation.getType()))
						fOccurrences++;
				}
			}
		};
		SelectionListenerWithASTManager.getDefault().addListener(fEditor, fSelWASTListener);
	}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		final IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setToDefault(PreferenceConstants.EDITOR_MARK_OCCURRENCES);
	    // TLETODO temporary fix for bug 314635
		store.setToDefault(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX 
				            + SemanticHighlightings.OVERLOADED_OPERATOR 
				            + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX);
		SelectionListenerWithASTManager.getDefault().removeListener(fEditor, fSelWASTListener);
		EditorTestHelper.closeAllEditors();
		if (fProjectSetup != null) {
			fProjectSetup.tearDown();
		}
	}
	
	private CEditor openCEditor(IPath path) {
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		assertTrue(file != null && file.exists());
		try {
			return (CEditor) EditorTestHelper.openInEditor(file, true);
		} catch (PartInitException e) {
			fail();
			return null;
		}
	}
	
	public void testMarkTypeOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "ClassContainer", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(3);
		assertOccurrencesInWidget();
	}

	public void testMarkTypeOccurrences2() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "Base1", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(5);
		assertOccurrencesInWidget();
	}

	public void testMarkTypeOccurrences3() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "Base2", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(3);
		assertOccurrencesInWidget();
	}

	public void testMarkTypedefOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "size_t", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(4);
		assertOccurrencesInWidget();
	}

	public void testMarkClassTemplateOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "TemplateClass", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(3);
		assertOccurrencesInWidget();
	}

	public void testMarkTemplateParameterOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "T1", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(3);
		assertOccurrencesInWidget();
	}

	public void testMarkTemplateIdOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "ConstantTemplate", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(4);
		assertOccurrencesInWidget();
	}

	public void testMarkOccurrencesAfterEditorReuse() {
		IPreferenceStore store= PlatformUI.getWorkbench().getPreferenceStore();
		store.setValue("REUSE_OPEN_EDITORS_BOOLEAN", true);
		
		int reuseOpenEditors= store.getInt("REUSE_OPEN_EDITORS");
		store.setValue("REUSE_OPEN_EDITORS", 1);
		
		SelectionListenerWithASTManager.getDefault().removeListener(fEditor, fSelWASTListener);
		fEditor= openCEditor(new Path("/" + PROJECT + "/src/main.cpp"));
		EditorTestHelper.joinReconciler((SourceViewer)fEditor.getViewer(), 10, 200, 20);
		SelectionListenerWithASTManager.getDefault().addListener(fEditor, fSelWASTListener);
		fDocument= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		assertNotNull(fDocument);
		fFindReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		fAnnotationModel= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
		
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "main", true, true, false, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);
		fMatch= new Region(fMatch.getOffset(), 4);
		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(1);
		assertOccurrencesInWidget();
		
		store.setValue("REUSE_OPEN_EDITORS_BOOLEAN", false);
		store.setValue("REUSE_OPEN_EDITORS", reuseOpenEditors);
	}
	
	public void testMarkMethodOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "pubMethod", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}
	
	public void testMarkMethodOccurrences2() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "getNumber", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}
	
	public void testMarkFieldOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "pubField", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}
	
	public void testMarkFieldOccurrences2() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "tArg1", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}
	
	public void testMarkConstructorOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "Base1(", true, true, false, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fMatch= new Region(fMatch.getOffset(), fMatch.getLength() - 1);
		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}

	public void testMarkDestructorOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "~Base1", true, true, false, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fMatch= new Region(fMatch.getOffset() + 1, fMatch.getLength() - 1);
		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}
	
	public void testMarkLocalOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "localVar", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}
	
	public void testMarkMacroOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "INT", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(5);
		assertOccurrencesInWidget();
	}

	public void testMarkEmptyMacroOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "EMPTY_MACRO", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(3);
		assertOccurrencesInWidget();
	}

	public void testMarkEnumeratorOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "ONE", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}

	public void testMarkNamespaceOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "ns", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(3);
		assertOccurrencesInWidget();
	}

	public void testMarkNamespaceVariableOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "namespaceVar", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(4);
		assertOccurrencesInWidget();
	}

	public void testMarkLabelOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "label", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}

	public void testMarkOperatorOccurrences() {
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "operator+", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(2);
		assertOccurrencesInWidget();
	}
	
	
	public void testNoOccurrencesIfDisabled() {
		CUIPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.EDITOR_MARK_OCCURRENCES, false);
		fOccurrences= Integer.MAX_VALUE;
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, "Base1", true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);

		fEditor.selectAndReveal(fMatch.getOffset(), fMatch.getLength());
		
		assertOccurrences(0);
		assertOccurrencesInWidget();
	}
	
	private void assertOccurrencesInWidget() {
		EditorTestHelper.runEventQueue(500);

		Iterator<Annotation> iter= fAnnotationModel.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation= iter.next();
			if (OCCURRENCE_ANNOTATION.equals(annotation.getType()))
				assertOccurrenceInWidget(fAnnotationModel.getPosition(annotation));
		}
	}

	private void assertOccurrenceInWidget(Position position) {
		StyleRange[] styleRanges= fTextWidget.getStyleRanges(position.offset, position.length);
		for (int i= 0; i < styleRanges.length; i++) {
			if (styleRanges[i].background != null) {
				RGB rgb= styleRanges[i].background.getRGB();
				if (fgHighlightRGB.equals(rgb))
					return;
			}
		}
		fail();
	}
	
	/**
	 * Returns the occurrence annotation color.
	 * 
	 * @return the occurrence annotation color
	 */
	private static RGB getHighlightRGB() {
		AnnotationPreference annotationPref= EditorsPlugin.getDefault().getAnnotationPreferenceLookup().getAnnotationPreference(OCCURRENCE_ANNOTATION);
		IPreferenceStore store= EditorsUI.getPreferenceStore();
		if (store != null)
			return PreferenceConverter.getColor(store, annotationPref.getColorPreferenceKey());
		
		return null;
	}

	private void assertOccurrences(final int expected) {
		DisplayHelper helper= new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fOccurrences == expected;
			}
		};
		if (!helper.waitForCondition(EditorTestHelper.getActiveDisplay(), 10000)) {
			assertEquals(expected, fOccurrences);
		}
	}
}
