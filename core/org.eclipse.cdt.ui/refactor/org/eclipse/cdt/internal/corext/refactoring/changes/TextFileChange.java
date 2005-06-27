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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.core.model.CModelException;

import org.eclipse.text.edits.UndoEdit;
import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.corext.refactoring.Checks;
import org.eclipse.cdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.cdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.cdt.internal.corext.textmanipulation.TextBuffer;

public class TextFileChange extends TextChange  {

	protected static class UndoTextFileChange extends UndoTextChange {
		private IFile fFile;
		private TextBuffer fAcquiredTextBuffer;
		private int fAcquireCounter;
		public UndoTextFileChange(String name, IFile file, int changeKind, UndoEdit undo) {
			super(name, changeKind, undo);
			fFile= file;
		}
		public Object getModifiedLanguageElement(){
			return fFile;
		}
		protected TextBuffer acquireTextBuffer() throws CoreException {
			TextBuffer result= TextBuffer.acquire(fFile);
			if (fAcquiredTextBuffer == null || result == fAcquiredTextBuffer) {
				fAcquiredTextBuffer= result;
				fAcquireCounter++;
			}
			return result;
		}
		protected void releaseTextBuffer(TextBuffer textBuffer) {
			TextBuffer.release(textBuffer);
			if (textBuffer == fAcquiredTextBuffer) {
				if (--fAcquireCounter == 0)
					fAcquiredTextBuffer= null;
			}
		}
		protected TextBuffer createTextBuffer() throws CoreException {
			return TextBuffer.create(fFile);
		}
		protected IChange createReverseChange(UndoEdit undo, int changeKind) {
			return new UndoTextFileChange(getName(), fFile, changeKind, undo);
		}
		public RefactoringStatus aboutToPerform(ChangeContext context, IProgressMonitor pm) {
			RefactoringStatus result= Checks.validateModifiesFiles(new IFile[] {fFile});
			if (result.hasFatalError())
				return result;
			context.checkUnsavedFile(result, fFile);
			return result;
		}
		public void perform(ChangeContext context, IProgressMonitor pm) throws CModelException, ChangeAbortException {
			if (!isActive()) {
				super.perform(context, pm);
				return;
			}
			try{
				acquireTextBuffer();
				pm.beginTask("", 10); //$NON-NLS-1$
				super.perform(context, new SubProgressMonitor(pm, 8));
				TextBuffer.aboutToChange(fAcquiredTextBuffer);
				TextBuffer.save(fAcquiredTextBuffer, new SubProgressMonitor(pm, 2));
			} catch (Exception e) {
				handleException(context, e);
			} finally {
				pm.done();
			}
		}
		public void performed() {
			// During acquiring of text buffer an exception has occured. In this case
			// the pointer is <code>null</code>
			if (fAcquiredTextBuffer != null) {
				try {
					TextBuffer.changed(fAcquiredTextBuffer);
				} catch (CoreException e) {
					Assert.isTrue(false, "Should not happen since the buffer is acquired through a text buffer manager");	 //$NON-NLS-1$
				} finally {
					releaseTextBuffer(fAcquiredTextBuffer);
				}
			}
			super.performed();
		}		
	}

	private IFile fFile;
	private TextBuffer fAcquiredTextBuffer;
	private int fAcquireCounter;
	private boolean fSave= true;

	/**
	 * Creates a new <code>TextFileChange</code> for the given file.
	 * 
	 * @param name the change's name mainly used to render the change in the UI
	 * @param file the file this text change operates on
	 */
	public TextFileChange(String name, IFile file) {
		super(name);
		fFile= file;
		Assert.isNotNull(fFile);
	}
	
	/**
	 * Sets the save state. If set to <code>true</code> the change will save the
	 * content of the file back to disk.
	 * 
	 * @param save whether or not the changes should be saved to disk
	 */
	public void setSave(boolean save) {
		fSave= save;
	}
	
	/**
	 * Returns the <code>IFile</code> this change is working on.
	 * 
	 * @return the file this change is working on
	 */
	public IFile getFile() {
		return fFile;
	}
		
	/* non java-doc
	 * Method declared in TextChange
	 */
	protected TextBuffer acquireTextBuffer() throws CoreException {
		TextBuffer result= TextBuffer.acquire(fFile);
		if (fAcquiredTextBuffer == null || result == fAcquiredTextBuffer) {
			fAcquiredTextBuffer= result;
			fAcquireCounter++;
		}
		return result;
	}
	
	/* non java-doc
	 * Method declared in TextChange
	 */
	protected void releaseTextBuffer(TextBuffer textBuffer) {
		TextBuffer.release(textBuffer);
		if (textBuffer == fAcquiredTextBuffer) {
			if (--fAcquireCounter == 0)
				fAcquiredTextBuffer= null;
		}
	}

	/* non java-doc
	 * Method declared in TextChange
	 */
	protected TextBuffer createTextBuffer() throws CoreException {
		return TextBuffer.create(fFile);
	}
	
	/* non java-doc
	 * Method declared in TextChange
	 */
	protected IChange createReverseChange(UndoEdit undo, int changeKind) {
		return new UndoTextFileChange(getName(), fFile, changeKind, undo);
	}
	
	/* non java-doc
	 * Method declared in IChange.
	 */
	public Object getModifiedLanguageElement(){
		return fFile;
	}
	
	/* non java-doc
	 * Method declared in TextChange
	 */
	public RefactoringStatus aboutToPerform(ChangeContext context, IProgressMonitor pm) {
		if (fSave) {
			return Checks.validateModifiesFiles(new IFile[] {fFile});
		}
		return new RefactoringStatus();
	}
	
	/* non java-doc
	 * Method declared in TextChange
	 */
	public void perform(ChangeContext context, IProgressMonitor pm) throws CModelException, ChangeAbortException {
		if (pm == null)
			pm= new NullProgressMonitor();
		if (!isActive()) {
			super.perform(context, pm);
			return;
		}
		try{
			acquireTextBuffer();
			pm.beginTask("", 10); //$NON-NLS-1$
			super.perform(context, new SubProgressMonitor(pm, 8));
			if (fSave) {
				TextBuffer.aboutToChange(fAcquiredTextBuffer);
				TextBuffer.save(fAcquiredTextBuffer, new SubProgressMonitor(pm, 2));
			}
		} catch (Exception e) {
			handleException(context, e);
		} finally {
			pm.done();
		}
	}
	
	/* non java-doc
	 * Method declared in TextChange
	 */
	public void performed() {
		// During acquiring of text buffer an exception has occured. In this case
		// the pointer is <code>null</code>
		if (fAcquiredTextBuffer != null) {
			try {
				if (fSave)
					TextBuffer.changed(fAcquiredTextBuffer);
			} catch (CoreException e) {
				Assert.isTrue(false, "Should not happen since the buffer is acquired through a text buffer manager");	 //$NON-NLS-1$
			} finally {
				releaseTextBuffer(fAcquiredTextBuffer);
			}
		}
		super.performed();
	}		
}

