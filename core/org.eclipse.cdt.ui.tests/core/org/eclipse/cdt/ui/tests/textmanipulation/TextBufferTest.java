/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

package org.eclipse.cdt.ui.tests.textmanipulation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.corext.textmanipulation.MoveTextEdit;
import org.eclipse.cdt.internal.corext.textmanipulation.SimpleTextEdit;
import org.eclipse.cdt.internal.corext.textmanipulation.SwapTextEdit;
import org.eclipse.cdt.internal.corext.textmanipulation.TextBuffer;
import org.eclipse.cdt.internal.corext.textmanipulation.TextBufferEditor;
import org.eclipse.cdt.internal.corext.textmanipulation.TextRange;
import org.eclipse.cdt.internal.corext.textmanipulation.UndoMemento;
import org.eclipse.cdt.testplugin.TestPluginLauncher; 



public class TextBufferTest extends TestCase {

	private static final Class THIS= TextBufferTest.class;
	
	private TextBuffer fBuffer;
	private TextBufferEditor fEditor;
	
	public TextBufferTest(String name) {
		super(name);
	}
	
	public static void main(String[] args) {
		TestPluginLauncher.run(TestPluginLauncher.getLocationFromProperties(), THIS, args);
	}

	public static Test suite() {
		TestSuite result= new TestSuite(THIS);
		if (false) {	// For hot code replace when debugging test cases
			result.addTestSuite(THIS);
			result.addTestSuite(THIS);
			result.addTestSuite(THIS);
			result.addTestSuite(THIS);
			result.addTestSuite(THIS);
			result.addTestSuite(THIS);
		}
		return result;
	}
	
	protected void setUp() throws Exception {
		fBuffer= TextBuffer.create("0123456789");
		fEditor= new TextBufferEditor(fBuffer);
	}
	
	protected void tearDown() throws Exception {
		fEditor= null;
	}
	
	public void testOverlap1() throws Exception {
		// [ [ ] ]
		fEditor.add(SimpleTextEdit.createReplace(0, 2, "01"));
		fEditor.add(SimpleTextEdit.createReplace(1, 2, "12"));
		assertTrue(!fEditor.canPerformEdits());
	}	
	
	public void testOverlap2() throws Exception {
		// [[ ] ]
		fEditor.add(SimpleTextEdit.createReplace(0, 2, "01"));
		fEditor.add(SimpleTextEdit.createReplace(0, 1, "0"));
		assertTrue(!fEditor.canPerformEdits());
	}	
	
	public void testOverlap3() throws Exception {
		// [ [ ]]
		fEditor.add(SimpleTextEdit.createReplace(0, 2, "01"));
		fEditor.add(SimpleTextEdit.createReplace(1, 1, "1"));
		assertTrue(!fEditor.canPerformEdits());
	}	
	
	public void testOverlap4() throws Exception {
		// [ [ ] ]
		fEditor.add(SimpleTextEdit.createReplace(0, 3, "012"));
		fEditor.add(SimpleTextEdit.createReplace(1, 1, "1"));
		assertTrue(!fEditor.canPerformEdits());
	}
	
	public void testOverlap5() throws Exception {
		// [ []  ]
		fEditor.add(SimpleTextEdit.createReplace(0, 3, "012"));
		fEditor.add(SimpleTextEdit.createInsert(1, "xx"));
		assertTrue(!fEditor.canPerformEdits());
	}
	
	public void testOverlap6() throws Exception {
		// [  [] ]
		fEditor.add(SimpleTextEdit.createReplace(0, 3, "012"));
		fEditor.add(SimpleTextEdit.createInsert(2, "xx"));
		assertTrue(!fEditor.canPerformEdits());
	}
	
	public void testOverlap7() throws Exception {
		boolean catched= false;
		try {
			new MoveTextEdit(2,5,3);
		} catch (Exception e) {
			catched= true;
		}
		assertTrue(catched);
	}
	
	public void testOverlap8() throws Exception {
		boolean catched= false;
		try {
			new MoveTextEdit(2,5,6);
		} catch (Exception e) {
			catched= true;
		}
		assertTrue(catched);
	}
	
	public void testOverlap9() throws Exception {
		MoveTextEdit e1= new MoveTextEdit(3, 1, 7);
		MoveTextEdit e2= new MoveTextEdit(2, 3, 8);
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue(!fEditor.canPerformEdits());
	}
		
	public void testInsert1() throws Exception {
		// [][  ]
		SimpleTextEdit e1= SimpleTextEdit.createInsert(2, "yy");
		SimpleTextEdit e2= SimpleTextEdit.createReplace(2, 3, "3456");
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assert(e1.getTextRange(), 2, 2);
		assert(e2.getTextRange(), 4, 4);
		assertEquals("Buffer content", "01yy345656789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getTextRange(), 2, 0);
		assert(e2.getTextRange(), 2, 3);
	}
	
	public void testInsert2() throws Exception {
		// [][]
		SimpleTextEdit e1= SimpleTextEdit.createInsert(2, "yy");
		SimpleTextEdit e2= SimpleTextEdit.createInsert(2, "xx");
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assert(e1.getTextRange(), 2, 2);
		assert(e2.getTextRange(), 4, 2);
		assertEquals("Buffer content", "01yyxx23456789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getTextRange(), 2, 0);
		assert(e2.getTextRange(), 2, 0);
	}
	
	public void testInsert3() throws Exception {
		// [  ][][  ]
		SimpleTextEdit e1= SimpleTextEdit.createReplace(0, 2, "011");
		SimpleTextEdit e2= SimpleTextEdit.createInsert(2, "xx");
		SimpleTextEdit e3= SimpleTextEdit.createReplace(2, 2, "2");
		fEditor.add(e1);
		fEditor.add(e2);
		fEditor.add(e3);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assert(e1.getTextRange(), 0, 3);
		assert(e2.getTextRange(), 3, 2);
		assert(e3.getTextRange(), 5, 1);
		assertEquals("Buffer content", "011xx2456789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getTextRange(), 0, 2);
		assert(e2.getTextRange(), 2, 0);
		assert(e3.getTextRange(), 2, 2);
	}
	
	public void testInsert4() throws Exception {
		SimpleTextEdit e1= SimpleTextEdit.createInsert(0, "xx");
		fEditor.add(e1);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer length", 12, fBuffer.getLength());
		assert(e1.getTextRange(), 0, 2);
		assertEquals("Buffer content", "xx0123456789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getTextRange(), 0, 0);
	}
	
	public void testInsert5() throws Exception {
		SimpleTextEdit e1= SimpleTextEdit.createInsert(10, "xx");
		fEditor.add(e1);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer length", 12, fBuffer.getLength());
		assert(e1.getTextRange(), 10, 2);
		assertEquals("Buffer content", "0123456789xx", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getTextRange(), 10, 0);
	}
	
	public void testDelete1() throws Exception {
		SimpleTextEdit e1= SimpleTextEdit.createDelete(3, 1);
		fEditor.add(e1);
		assertTrue("Can perform edits", fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assert(e1.getTextRange(), 3, 0);
		assertEquals("Buffer content", "012456789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getTextRange(), 3, 1);
	}
	
	public void testDelete2() throws Exception {
		SimpleTextEdit e1= SimpleTextEdit.createDelete(4, 1);
		SimpleTextEdit e2= SimpleTextEdit.createDelete(3, 1);
		SimpleTextEdit e3= SimpleTextEdit.createDelete(5, 1);
		fEditor.add(e1);
		fEditor.add(e2);
		fEditor.add(e3);
		assertTrue("Can perform edits", fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assert(e1.getTextRange(), 3, 0);
		assert(e2.getTextRange(), 3, 0);
		assert(e3.getTextRange(), 3, 0);
		assertEquals("Buffer content", "0126789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getTextRange(), 4, 1);
		assert(e2.getTextRange(), 3, 1);
		assert(e3.getTextRange(), 5, 1);
	}
	
	public void testDelete3() throws Exception {
		SimpleTextEdit e1= SimpleTextEdit.createInsert(3, "x");
		SimpleTextEdit e2= SimpleTextEdit.createDelete(3, 1);
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue("Can perform edits", fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assert(e1.getTextRange(), 3, 1);
		assert(e2.getTextRange(), 4, 0);
		assertEquals("Buffer content", "012x456789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getTextRange(), 3, 0);
		assert(e2.getTextRange(), 3, 1);
	}
	
	public void testMove1() throws Exception {
		MoveTextEdit e1= new MoveTextEdit(2, 2, 5);
		fEditor.add(e1);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "0142356789", fBuffer.getContent());
		assert(e1.getTargetRange(), 3, 2);
		assert(e1.getSourceRange(), 2, 0);
		doUndo(undo);
		assert(e1.getSourceRange(), 2, 2);
		assert(e1.getTargetRange(), 5, 0);
	}
	
	public void testMove2() throws Exception {
		MoveTextEdit e1= new MoveTextEdit(5, 2, 2);
		fEditor.add(e1);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "0156234789", fBuffer.getContent());
		assert(e1.getTargetRange(), 2, 2);
		assert(e1.getSourceRange(), 7, 0);
		doUndo(undo);
		assert(e1.getSourceRange(), 5, 2);
		assert(e1.getTargetRange(), 2, 0);
	}

	public void testMove3() throws Exception {
		MoveTextEdit e1= new MoveTextEdit(2, 2, 7);
		SimpleTextEdit e2= SimpleTextEdit.createReplace(4, 1, "x");
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "01x5623789", fBuffer.getContent());
		assert(e1.getTargetRange(), 5, 2);
		assert(e1.getSourceRange(), 2, 0);
		assert(e2.getTextRange(), 2, 1);
		doUndo(undo);
		assert(e1.getSourceRange(), 2, 2);
		assert(e1.getTargetRange(), 7, 0);
		assert(e2.getTextRange(), 4, 1);
	}
	
	public void testMove4() throws Exception {
		MoveTextEdit e1= new MoveTextEdit(7, 2, 2);
		SimpleTextEdit e2= SimpleTextEdit.createReplace(5, 1, "x");
		fEditor.add(e2);
		fEditor.add(e1);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "0178234x69", fBuffer.getContent());
		assert(e1.getTargetRange(), 2, 2);
		assert(e1.getSourceRange(), 9, 0);
		assert(e2.getTextRange(), 7, 1);
		doUndo(undo);
		assert(e1.getSourceRange(), 7, 2);
		assert(e1.getTargetRange(), 2, 0);
		assert(e2.getTextRange(), 5, 1);
	}
	
	public void testMove5() throws Exception {
		// Move onto itself
		MoveTextEdit e1= new MoveTextEdit(2, 1, 3);
		SimpleTextEdit e2= SimpleTextEdit.createReplace(2,1,"x");
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assert(e1.getTargetRange(), 2, 1);
		assert(e1.getSourceRange(), 3, 0);
		assert(e2.getTextRange(), 2, 1);
		assertEquals("Buffer content", "01x3456789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getSourceRange(), 2, 1);
		assert(e1.getTargetRange(), 3, 0);
		assert(e2.getTextRange(), 2, 1);
	}
	
	public void testMove6() throws Exception {
		// Move onto itself
		MoveTextEdit e1= new MoveTextEdit(2, 1, 2);
		SimpleTextEdit e2= SimpleTextEdit.createReplace(2,1,"x");
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assert(e1.getTargetRange(), 2, 1);
		assert(e1.getSourceRange(), 3, 0);		// This gets normalized since a move from [2,1] -> 2 == [2,1] -> 3
		assert(e2.getTextRange(), 2, 1);
		assertEquals("Buffer content", "01x3456789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getSourceRange(), 2, 1);
		assert(e1.getTargetRange(), 3, 0);
		assert(e2.getTextRange(), 2, 1);
	}
	
	public void testMove7() throws Exception {
		MoveTextEdit e1= new MoveTextEdit(2, 3, 7);
		SimpleTextEdit e2= SimpleTextEdit.createReplace(3, 1, "x");
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "01562x4789", fBuffer.getContent());
		assert(e1.getTargetRange(), 4, 3);
		assert(e1.getSourceRange(), 2, 0);
		assert(e2.getTextRange(), 5, 1);
		doUndo(undo);
		assert(e1.getSourceRange(), 2, 3);
		assert(e1.getTargetRange(), 7, 0);
		assert(e2.getTextRange(), 3, 1);
	}
	
	public void testMove8() throws Exception {
		MoveTextEdit e1= new MoveTextEdit(5, 3, 1);
		SimpleTextEdit e2= SimpleTextEdit.createReplace(6, 1, "x");
		fEditor.add(e2);
		fEditor.add(e1);
		assertTrue(fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "05x7123489", fBuffer.getContent());
		assert(e1.getTargetRange(), 1, 3);
		assert(e1.getSourceRange(), 8, 0);
		assert(e2.getTextRange(), 2, 1);
		doUndo(undo);
		assert(e1.getSourceRange(), 5, 3);
		assert(e1.getTargetRange(), 1, 0);
		assert(e2.getTextRange(), 6, 1);
	}
		
	public void testMove9() throws Exception {
		MoveTextEdit e1= new MoveTextEdit(1, 1, 3);
		MoveTextEdit e2= new MoveTextEdit(1, 3, 5);
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue("Can perform edits", fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assert(e1.getTargetRange(), 3, 1);
		assert(e1.getSourceRange(), 2, 0);
		assert(e2.getTargetRange(), 2, 3);
		assert(e2.getSourceRange(), 1, 0);
		assertEquals("Buffer content", "0421356789", fBuffer.getContent());
		doUndo(undo);
		assert(e1.getSourceRange(), 1, 1);
		assert(e1.getTargetRange(), 3, 0);
		assert(e2.getSourceRange(), 1, 3);
		assert(e2.getTargetRange(), 5, 0);
	}
	
	public void testMove10() throws Exception {
		MoveTextEdit e1= new MoveTextEdit(2, 2, 8);
		MoveTextEdit e2= new MoveTextEdit(5, 2, 1);
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue("Can perform edits", fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "0561472389", fBuffer.getContent());
		doUndo(undo);		
	}
	
	public void testSwap1() throws Exception {
		SwapTextEdit e1= new SwapTextEdit(1, 1, 3, 1);
		fEditor.add(e1);
		assertTrue("Can perform edits", fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "0321456789", fBuffer.getContent());
		doUndo(undo);
	}
	
	public void testSwap2() throws Exception {
		SwapTextEdit e1= new SwapTextEdit(1, 1, 3, 1);
		SwapTextEdit e2= new SwapTextEdit(5, 1, 7, 1);
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue("Can perform edits", fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "0321476589", fBuffer.getContent());
		doUndo(undo);
	}
	
	public void testSwap3() throws Exception {
		SwapTextEdit e1= new SwapTextEdit(1, 1, 3, 1);
		SwapTextEdit e2= new SwapTextEdit(5, 1, 7, 1);
		SwapTextEdit e3= new SwapTextEdit(1, 3, 5, 3);
		fEditor.add(e1);
		fEditor.add(e2);
		fEditor.add(e3);
		assertTrue("Can perform edits", fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "0765432189", fBuffer.getContent());
		doUndo(undo);
	}

	public void testSwapAndReplace() throws Exception {
		SwapTextEdit e1= new SwapTextEdit(1, 3, 5, 3);
		SimpleTextEdit e2= SimpleTextEdit.createReplace(6, 1, "ab");
		fEditor.add(e1);
		fEditor.add(e2);
		assertTrue("Can perform edits", fEditor.canPerformEdits());
		UndoMemento undo= fEditor.performEdits(null);
		assertEquals("Buffer content", "05ab7412389", fBuffer.getContent());
		doUndo(undo);
	}
	
	private void doUndo(UndoMemento undo) throws Exception {
		fEditor.add(undo);
		fEditor.performEdits(null);
		assertBufferContent();
	}
	
	private void assert(TextRange r, int offset, int length) {
		assertEquals("Offset", offset, r.getOffset());
		assertEquals("Length", length, r.getLength());	
	}
	
	private void assertBufferContent() {
		assertEquals("Buffer content restored", "0123456789", fBuffer.getContent());
	}	
}

