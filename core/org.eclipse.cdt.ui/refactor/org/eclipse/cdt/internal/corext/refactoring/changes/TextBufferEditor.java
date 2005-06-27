/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.changes;

import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.corext.textmanipulation.TextBuffer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditProcessor;
import org.eclipse.text.edits.UndoEdit;

public class TextBufferEditor extends TextEditProcessor {

	private TextBuffer fBuffer;
	private TextEditProcessor fUndoProcessor;
		
	/**
	 * Creates a new <code>TextBufferEditor</code> for the given 
	 * <code>TextBuffer</code>.
	 * 
	 * @param the text buffer this editor is working on.
	 */
	public TextBufferEditor(TextBuffer buffer) {
		super(buffer.getDocument(), new MultiTextEdit(0, buffer.getDocument().getLength()),
				TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);
		fBuffer= buffer;
	}
	
	/**
	 * Returns the text buffer this editor is working on.
	 * 
	 * @return the text buffer this editor is working on
	 */
	public TextBuffer getTextBuffer() {
		return fBuffer;
	}
	
	/**
	 * Adds an <code>Edit</code> to this edit processor. Adding an edit
	 * to an edit processor transfers ownership of the edit to the 
	 * processor. So after an edit has been added to a processor the 
	 * creator of the edit <b>must</b> not continue modifying the edit.
	 * 
	 * @param edit the edit to add
	 * @exception MalformedTreeException if the text edit can not be 
	 *  added to this edit processor.
	 * 
	 * @see TextEdit#addChild(TextEdit)
	 */
	public void add(TextEdit edit) throws MalformedTreeException {
		getRoot().addChild(edit);
	}
		
	/**
	 * Adds an undo memento to this edit processor. Adding an undo memento
	 * transfers ownership of the memento to the processor. So after a memento 
	 * has been added the creator of that memento <b>must</b> not continue
	 * modifying it.
	 * 
	 * @param undo the undo memento to add
	 * @exception EditException if the undo memento can not be added
	 * 	to this processor
	 */
	public void add(UndoEdit undo) {
		Assert.isTrue(!getRoot().hasChildren());
		fUndoProcessor= new TextEditProcessor(getDocument(), undo, TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.text.edits.TextEditProcessor#canPerformEdits()
	 */
	public boolean canPerformEdits() {
		if (fUndoProcessor != null)
			return fUndoProcessor.canPerformEdits();
		return super.canPerformEdits();
	}
	
	/**
	 * Executes the text edits added to this text buffer editor and clears all added
	 * text edits.
	 * 
	 * @param pm a progress monitor to report progress or <code>null</code> if
	 * 	no progress is desired.
	 * @return an object representing the undo of the executed <code>TextEdit</code>s
	 * @exception CoreException if the edits cannot be executed
	 */
	public UndoEdit performEdits(IProgressMonitor pm) throws CoreException {
		try {
			if (fUndoProcessor != null) {
				return fUndoProcessor.performEdits();
			} else {
				return super.performEdits();
			}
		} catch (BadLocationException e) {
			String message= (e != null ? e.getMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.getPluginId(),
				IStatus.ERROR, message, e));
		} catch (MalformedTreeException e) {
			String message= (e != null ? e.getMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.getPluginId(),
				IStatus.ERROR, message, e));
		}
	}	
}

