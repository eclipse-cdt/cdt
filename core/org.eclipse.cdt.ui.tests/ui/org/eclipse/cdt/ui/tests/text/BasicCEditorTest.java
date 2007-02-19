/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * Basic CEditor tests.
 * 
 * @since 4.0
 */
public class BasicCEditorTest extends BaseUITestCase {

	private static CEditor fEditor;
	private static SourceViewer fSourceViewer;
	private ICProject fCProject;
	private IProject fNonCProject;
	private StyledText fTextWidget;
	private Accessor fAccessor;
	private IDocument fDocument;

	public static Test suite() {
		return new TestSuite(BasicCEditorTest.class); 
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown () throws Exception {
		EditorTestHelper.closeEditor(fEditor);
		if (fCProject != null)
			CProjectHelper.delete(fCProject);
		if (fNonCProject != null) {
			ResourceHelper.delete(fNonCProject);
		}
		super.tearDown();
	}

	private void setUpEditor(String file) throws PartInitException {
		IEditorPart editor= EditorTestHelper.openInEditor(ResourceTestHelper.findFile(file), true);
		assertNotNull(editor);
		assertTrue(editor instanceof CEditor);
		fEditor= (CEditor) editor;
		fTextWidget= fEditor.getViewer().getTextWidget();
		assertNotNull(fTextWidget);
		fAccessor= new Accessor(fTextWidget, StyledText.class);
		fDocument= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		assertNotNull(fDocument);
	}

	private void setUpEditor(File file) throws PartInitException, CModelException {
		IEditorPart editor= EditorUtility.openInEditor(new ExternalTranslationUnit(fCProject, Path.fromOSString(file.toString()), CCorePlugin.CONTENT_TYPE_CXXSOURCE));
		assertNotNull(editor);
		assertTrue(editor instanceof CEditor);
		fEditor= (CEditor) editor;
		fTextWidget= fEditor.getViewer().getTextWidget();
		assertNotNull(fTextWidget);
		fAccessor= new Accessor(fTextWidget, StyledText.class);
		fDocument= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		assertNotNull(fDocument);
	}

	public void testEditInNonCProject() throws Exception {
		final String file= "/ceditor/src/main.cpp";
		fNonCProject= EditorTestHelper.createNonCProject("ceditor", "resources/ceditor", false);
		setUpEditor(file);
		fSourceViewer= EditorTestHelper.getSourceViewer(fEditor);
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 100));
		String content= fDocument.get();
		setCaret(0);
		String newtext= "/* "+getName()+" */";
		type(newtext);
		type('\n');
		String newContent= fDocument.get();
		assertEquals("Edit failed", newtext, newContent.substring(0, newtext.length()));
		// save
		fEditor.doSave(new NullProgressMonitor());
		// close and reopen
		EditorTestHelper.closeEditor(fEditor);
		setUpEditor(file);
		content= fDocument.get();
		assertEquals("Save failed", newContent, content);
	}

	public void testEditExternalTranslationUnit() throws Exception {
		final String file= "/ceditor/src/main.cpp";
		fCProject= EditorTestHelper.createCProject("ceditor", "resources/ceditor", false);
		IFile mainFile= ResourceTestHelper.findFile(file);
		assertNotNull(mainFile);
		File tmpFile= File.createTempFile("tmp", ".cpp");
		tmpFile.deleteOnExit();
		FileTool.copy(mainFile.getLocation().toFile(), tmpFile);
		setUpEditor(tmpFile);
		fSourceViewer= EditorTestHelper.getSourceViewer(fEditor);
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 100));
		String content= fDocument.get();
		setCaret(0);
		String newtext= "/* "+getName()+" */";
		type(newtext);
		type('\n');
		String newContent= fDocument.get();
		assertEquals("Edit failed", newtext, newContent.substring(0, newtext.length()));
		// save
		fEditor.doSave(new NullProgressMonitor());
		// close and reopen
		EditorTestHelper.closeEditor(fEditor);
		setUpEditor(tmpFile);
		fSourceViewer= EditorTestHelper.getSourceViewer(fEditor);
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 100));
		content= fDocument.get();
		assertEquals("Save failed", newContent, content);
		// check reconciler
		ITranslationUnit tUnit= (ITranslationUnit)fEditor.getInputCElement();
		ICElement[] children= tUnit.getChildren();
		assertEquals(2, children.length);
		setCaret(content.length());
		type('\n');
		type("void func() {}\n");
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 100));
		children= tUnit.getChildren();
		assertEquals(3, children.length);
		tmpFile.delete();
	}

	/**
	 * Type characters into the styled text.
	 * 
	 * @param characters the characters to type
	 */
	private void type(CharSequence characters) {
		for (int i= 0; i < characters.length(); i++)
			type(characters.charAt(i), 0, 0);
	}

	/**
	 * Type a character into the styled text.
	 * 
	 * @param character the character to type
	 */
	private void type(char character) {
		type(character, 0, 0);
	}
	
	/**
	 * Type a character into the styled text.
	 * 
	 * @param character the character to type
	 * @param keyCode the key code
	 * @param stateMask the state mask
	 */
	private void type(char character, int keyCode, int stateMask) {
		Event event= new Event();
		event.character= character;
		event.keyCode= keyCode;
		event.stateMask= stateMask;
		fAccessor.invoke("handleKeyDown", new Object[] {event});
		
		new DisplayHelper() {
			protected boolean condition() {
				return false;
			}
		}.waitForCondition(EditorTestHelper.getActiveDisplay(), 50);
	}

	private int getCaret() {
		return ((ITextSelection) fEditor.getSelectionProvider().getSelection()).getOffset();
	}

	private void setCaret(int offset) {
		fEditor.getSelectionProvider().setSelection(new TextSelection(offset, 0));
		int newOffset= ((ITextSelection)fEditor.getSelectionProvider().getSelection()).getOffset();
		assertEquals(offset, newOffset);
	}
}
