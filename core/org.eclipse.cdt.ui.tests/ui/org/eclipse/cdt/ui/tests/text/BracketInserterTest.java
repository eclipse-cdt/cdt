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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PartInitException;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.Accessor;
import org.eclipse.cdt.ui.testplugin.DisplayHelper;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;

import org.eclipse.cdt.internal.ui.editor.CEditor;


/**
 * Tests the automatic bracket insertion feature of the CEditor. Also tests
 * linked mode along the way.
 */
public class BracketInserterTest extends TestCase {

	private static final String SRC= "src";
	private static final String SEP= "/";
	private static final String TU_NAME= "smartedit.cpp";
	private static final String TU_CONTENTS= 
		"#include \n" +
		"class Application {\n" + 
		"    char* string;\n" + 
		"    int integer;\n" + 
		"\n" + 
		"public:\n" +
		"    static void main(int argc, char** args);\n" + 
		"protected:\n" +
		"    void foo(char** args);\n" +
		"};\n" +
		"\n" +
		"void Application::main(int argc, char** args) {\n" +
		"    \n" + 
		"}\n" +
		"void Application::foo(char** args) {\n" +
		"    char[] t= args[0];" + 
		"}\n";
	
	// document offsets 
	private static final int INCLUDE_OFFSET= 9;
	private static final int BODY_OFFSET= 212;
	private static final int ARGS_OFFSET= 184;
	private static final int BRACKETS_OFFSET= 262;
	
	public static Test suite() {
		TestSuite suite= new TestSuite(BracketInserterTest.class);
		return suite;
	}
	
	private CEditor fEditor;
	private StyledText fTextWidget;
	private IDocument fDocument;
	private Accessor fAccessor;
	private ICProject fProject;

	@Override
	protected void setUp() throws Exception {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_CLOSE_BRACKETS, true);
		setUpProject();
		setUpEditor();
	}
	
	private void setUpProject() throws CoreException {
		fProject= CProjectHelper.createCProject(getName(), "bin", IPDOMManager.ID_NO_INDEXER);
		ICContainer cContainer= CProjectHelper.addCContainer(fProject, SRC);
		IFile file= EditorTestHelper.createFile((IContainer)cContainer.getResource(), TU_NAME, TU_CONTENTS, new NullProgressMonitor());
		assertNotNull(file);
		assertTrue(file.exists());
	}

	private void setUpEditor() {
		fEditor= openCEditor(new Path(SEP + getName() + SEP + SRC + SEP + TU_NAME));
		assertNotNull(fEditor);
		fTextWidget= fEditor.getViewer().getTextWidget();
		assertNotNull(fTextWidget);
		fAccessor= new Accessor(fTextWidget, StyledText.class);
		fDocument= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		assertNotNull(fDocument);
		assertEquals(TU_CONTENTS, fDocument.get());
	}

	private CEditor openCEditor(IPath path) {
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		assertTrue(file != null && file.exists());
		try {
			return (CEditor)EditorTestHelper.openInEditor(file, true);
		} catch (PartInitException e) {
			fail();
			return null;
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		EditorTestHelper.closeEditor(fEditor);
		fEditor= null;
		if (fProject != null) {
			CProjectHelper.delete(fProject);
			fProject= null;
		}

		// reset to defaults
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setToDefault(PreferenceConstants.EDITOR_CLOSE_BRACKETS);
	}
	
	public void testInsertClosingParenthesis() throws BadLocationException, CModelException, CoreException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type('(');
		
		assertEquals("()", fDocument.get(BODY_OFFSET, 2));
		assertSingleLinkedPosition(BODY_OFFSET + 1);
	}
	
	public void testDeletingParenthesis() throws CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type('(');
		type(SWT.BS);
		
		assertEquals(TU_CONTENTS, fDocument.get());
		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}
	
	public void testMultipleParenthesisInsertion() throws BadLocationException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type("((((");
		
		assertEquals("(((())))", fDocument.get(BODY_OFFSET, 8));
		assertEquals(BODY_OFFSET + 4, getCaret());
		
		assertModel(true);
	}
	
	public void testDeletingMultipleParenthesisInertion() throws BadLocationException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type("((((");
		
		// delete two levels
		linkedType(SWT.BS, true, ILinkedModeListener.EXTERNAL_MODIFICATION);
		linkedType(SWT.BS, true, ILinkedModeListener.EXTERNAL_MODIFICATION);

		assertEquals("(())", fDocument.get(BODY_OFFSET, 4));
		assertEquals(BODY_OFFSET + 2, getCaret());
		
		// delete the second-last level
		linkedType(SWT.BS, true, ILinkedModeListener.EXTERNAL_MODIFICATION);
		assertEquals("()", fDocument.get(BODY_OFFSET, 2));
		assertEquals(BODY_OFFSET + 1, getCaret());
		
		// delete last level
		linkedType(SWT.BS, false, ILinkedModeListener.EXTERNAL_MODIFICATION);
		assertEquals(TU_CONTENTS, fDocument.get());
		assertEquals(BODY_OFFSET, getCaret());
		
		assertEquals(TU_CONTENTS, fDocument.get());
		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}
	
	public void testNoInsertInsideText() throws BadLocationException, CModelException, CoreException {
		setCaret(ARGS_OFFSET);
		type('(');
		
		assertEquals("(in", fDocument.get(ARGS_OFFSET, 3));
		assertEquals(ARGS_OFFSET + 1, getCaret());
		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}
	
	public void testInsertInsideBrackets() throws BadLocationException, CModelException, CoreException {
		setCaret(BRACKETS_OFFSET);
		type('(');
		
		assertEquals("()", fDocument.get(BRACKETS_OFFSET, 2));
		assertSingleLinkedPosition(BRACKETS_OFFSET + 1);
	}
	
	public void testPeerEntry() throws BadLocationException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type("()");
		
		assertEquals("()", fDocument.get(BODY_OFFSET, 2));
		assertEquals(BODY_OFFSET + 2, getCaret());

		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}
	
	public void testMultiplePeerEntry() throws BadLocationException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type("((((");

		linkedType(')', true, ILinkedModeListener.UPDATE_CARET);
		linkedType(')', true, ILinkedModeListener.UPDATE_CARET);
		linkedType(')', true, ILinkedModeListener.UPDATE_CARET);
		
		assertEquals("(((())))", fDocument.get(BODY_OFFSET, 8));
		assertEquals(BODY_OFFSET + 7, getCaret());
		
		LinkedPosition position= assertModel(false).findPosition(new LinkedPosition(fDocument, BODY_OFFSET + 1, 0));
		assertNotNull(position);
		assertEquals(BODY_OFFSET + 1, position.getOffset());
		assertEquals(6, position.getLength());
		
		linkedType(')', false, ILinkedModeListener.UPDATE_CARET);
		
		assertEquals("(((())))", fDocument.get(BODY_OFFSET, 8));
		assertEquals(BODY_OFFSET + 8, getCaret());
		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}
	
	public void testExitOnTab() throws BadLocationException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type("((((");
		linkedType('\t', true, ILinkedModeListener.NONE);
		
		assertEquals("(((())))", fDocument.get(BODY_OFFSET, 8));
		assertEquals(BODY_OFFSET + 5, getCaret());

		linkedType('\t', true, ILinkedModeListener.NONE);
		linkedType('\t', true, ILinkedModeListener.NONE);
		
		assertEquals("(((())))", fDocument.get(BODY_OFFSET, 8));
		assertEquals(BODY_OFFSET + 7, getCaret());

		linkedType('\t', false, ILinkedModeListener.NONE);
		
		assertEquals("(((())))", fDocument.get(BODY_OFFSET, 8));
		assertEquals(BODY_OFFSET + 8, getCaret());

		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}
	
	public void testExitOnReturn() throws BadLocationException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type("((((");
		linkedType(SWT.CR, true, ILinkedModeListener.UPDATE_CARET | ILinkedModeListener.EXIT_ALL);
		
		assertEquals("(((())))", fDocument.get(BODY_OFFSET, 8));
		assertEquals(BODY_OFFSET + 8, getCaret());

		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}
	
	public void testExitOnEsc() throws BadLocationException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type("((((");
		linkedType(SWT.ESC, true, ILinkedModeListener.EXIT_ALL);
		
		assertEquals("(((())))", fDocument.get(BODY_OFFSET, 8));
		assertEquals(BODY_OFFSET + 4, getCaret());

		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}
	
	public void testInsertClosingQuote() throws BadLocationException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type('"');
		
		assertEquals("\"\"", fDocument.get(BODY_OFFSET, 2));
		
		assertSingleLinkedPosition(BODY_OFFSET + 1);
	}
	
	// bug 270916
	public void testInsertClosingQuoteInMacroDefinition() throws BadLocationException, CModelException, CoreException {
		setCaret(BODY_OFFSET);
		type("#define MACRO ");
		int offset = getCaret();
		// enter opening quote (should be closed again)
		type('"');
		
		assertEquals("\"\"", fDocument.get(offset, 2));
		assertSingleLinkedPosition(offset + 1);

		// enter closing quote (should not add a quote, but proceed cursor)
		type('"');
		assertEquals("\"\"", fDocument.get(offset, 2));
		assertEquals(offset + 2, getCaret());
		
		// delete closing quote and enter quote again
		type(SWT.BS);
		assertEquals("\"", fDocument.get(offset, 1));
		int length = fDocument.getLength();
		type('"');

		assertEquals("\"\"", fDocument.get(offset, 2));
		assertEquals(offset + 2, getCaret());
		assertEquals(length + 1, fDocument.getLength());
	}
	
	public void testPreferences() throws BadLocationException, CModelException, CoreException {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_CLOSE_BRACKETS, false);
		
		setCaret(BODY_OFFSET);
		type('(');
		
		assertEquals("(", fDocument.get(BODY_OFFSET, 1));
		assertEquals(BODY_OFFSET + 1, getCaret());
		
		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}

	public void testAngleBracketsAsOperator() throws Exception {
		setCaret(BODY_OFFSET);
		type("test<");
		
		assertEquals("test<", fDocument.get(BODY_OFFSET, 5));
		assertFalse(">".equals(fDocument.get(BODY_OFFSET + 5, 1)));
		assertEquals(BODY_OFFSET + 5, getCaret());
		
		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}
	
	public void testAngleBrackets() throws Exception {
		setCaret(BODY_OFFSET);
		type("Test<");
		
		assertEquals("Test<>", fDocument.get(BODY_OFFSET, 6));
		assertSingleLinkedPosition(BODY_OFFSET + 5);
	}
	
	public void testAngleBracketsInInclude() throws Exception {
		setCaret(INCLUDE_OFFSET);
		type('<');
		
		assertEquals("#include <>", fDocument.get(INCLUDE_OFFSET - 9, 11));
		assertSingleLinkedPosition(INCLUDE_OFFSET + 1);
	}

	public void testInsertClosingQuoteInInclude() throws Exception {
		setCaret(INCLUDE_OFFSET);
		type('"');
		
		assertEquals("#include \"\"", fDocument.get(INCLUDE_OFFSET - 9, 11));
		assertSingleLinkedPosition(INCLUDE_OFFSET + 1);
	}

	public void testInsertClosingQuoteInIncludeAtDocumentEnd_Bug309099() throws Exception {
		int startOffset = TU_CONTENTS.length();
		setCaret(startOffset);
		type("#include ");
		type('"');
		assertEquals(startOffset + 11, fDocument.getLength());
		assertEquals("#include \"\"", fDocument.get(startOffset, 11));
		assertSingleLinkedPosition(startOffset + 10);
	}

	public void testAngleBrackets_165837() throws Exception {
		setCaret(BODY_OFFSET);
		type("cout << \n\"aaa\" ");
		type('<');
		int caretOffset= getCaret();
		assertFalse(">".equals(fDocument.get(caretOffset, 1)));
		assertFalse(LinkedModeModel.hasInstalledModel(fDocument));
	}

	/* utilities */

	private void assertSingleLinkedPosition(int offset) {
		assertEquals(offset, getCaret());
		
		LinkedPosition position= assertModel(false).findPosition(new LinkedPosition(fDocument, offset, 0));
		assertNotNull(position);
		assertEquals(offset, position.getOffset());
		assertEquals(0, position.getLength());
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
	 * Ensure there is a linked mode and type a character into the styled text.
	 * 
	 * @param character the character to type
	 * @param nested whether the linked mode is expected to be nested or not
	 * @param expectedExitFlags the expected exit flags for the current linked mode after typing the character, -1 for no exit
	 */
	private void linkedType(char character, boolean nested, int expectedExitFlags) {
		final int[] exitFlags= { -1 };
		assertModel(nested).addLinkingListener(new ILinkedModeListener() {
			@Override
			public void left(LinkedModeModel model, int flags) {
				exitFlags[0]= flags;
			}
			@Override
			public void resume(LinkedModeModel model, int flags) {
			}
			@Override
			public void suspend(LinkedModeModel model) {
			}
		});
		type(character, 0, 0);
		assertEquals(expectedExitFlags, exitFlags[0]);
	}
	
	private LinkedModeModel assertModel(boolean nested) {
		LinkedModeModel model= LinkedModeModel.getModel(fDocument, 0); // offset does not matter
		assertNotNull(model);
		assertEquals(nested, model.isNested());
		return model;
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
			@Override
			protected boolean condition() {
				return false;
			}
		}.waitForCondition(EditorTestHelper.getActiveDisplay(), 200);
		
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
