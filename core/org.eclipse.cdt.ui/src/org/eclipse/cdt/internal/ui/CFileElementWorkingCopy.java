package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.IBufferChangedListener;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.WorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.internal.ui.editor.DocumentAdapter;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class CFileElementWorkingCopy extends WorkingCopy {

	IDocumentProvider fProvider;
	IEditorInput input;
	IBuffer buffer;

	/**
	 * Internal IBuffer implementation very simple, must cases will use DocumentAdapter.
	 * 
	 */
	class Buffer implements IBuffer {

		CFileElementWorkingCopy owner;

		public Buffer(CFileElementWorkingCopy o) {
			owner = o;
		}
		/* (non-Javadoc)
		* @see org.eclipse.cdt.core.model.IBuffer#addBufferChangedListener(org.eclipse.cdt.core.model.IBufferChangedListener)
		*/
		public void addBufferChangedListener(IBufferChangedListener listener) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#append(char[])
		 */
		public void append(char[] text) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#append(java.lang.String)
		 */
		public void append(String text) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#close()
		 */
		public void close() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#getChar(int)
		 */
		public char getChar(int position) {
			IDocument doc = fProvider.getDocument(input);
			if (doc != null) {
				try {
					return doc.getChar(position);
				} catch (BadLocationException e) {
				}
			}
			return 0;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#getCharacters()
		 */
		public char[] getCharacters() {
			return getContents().toCharArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#getContents()
		 */
		public String getContents() {
			IDocument doc = fProvider.getDocument(input);
			if (doc != null) {
				return doc.get();
			}
			return new String();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#getLength()
		 */
		public int getLength() {
			IDocument doc = fProvider.getDocument(input);
			if (doc != null) {
				return doc.getLength();
			}
			return 0;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#getOwner()
		 */
		public IOpenable getOwner() {
			return owner;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#getText(int, int)
		 */
		public String getText(int offset, int length) {
			IDocument doc = fProvider.getDocument(input);
			if (doc != null) {
				try {
					return doc.get(offset, length);
				} catch (BadLocationException e) {
				}
			}
			return new String();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#getUnderlyingResource()
		 */
		public IResource getUnderlyingResource() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#hasUnsavedChanges()
		 */
		public boolean hasUnsavedChanges() {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#isClosed()
		 */
		public boolean isClosed() {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#isReadOnly()
		 */
		public boolean isReadOnly() {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#removeBufferChangedListener(org.eclipse.cdt.core.model.IBufferChangedListener)
		 */
		public void removeBufferChangedListener(IBufferChangedListener listener) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#replace(int, int, char[])
		 */
		public void replace(int position, int length, char[] text) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#replace(int, int, java.lang.String)
		 */
		public void replace(int position, int length, String text) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#save(org.eclipse.core.runtime.IProgressMonitor, boolean)
		 */
		public void save(IProgressMonitor progress, boolean force) throws CModelException {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#setContents(char[])
		 */
		public void setContents(char[] contents) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.model.IBuffer#setContents(java.lang.String)
		 */
		public void setContents(String contents) {
		}

	}

	/**
	 * Creates a working copy of this element
	 */
	public CFileElementWorkingCopy(IStorageEditorInput StoreInput, IDocumentProvider provider) throws CoreException {
		super(null, new Path(StoreInput.getName()), null);
		input = StoreInput;
		fProvider = provider;
		IStorage storage = StoreInput.getStorage();
		super.setLocation(storage.getFullPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IOpenable#getBuffer()
	 */
	public IBuffer getBuffer() throws CModelException {
		if (buffer == null) {
			if (fProvider instanceof CDocumentProvider) {
				buffer = new DocumentAdapter(this, fProvider.getDocument(input), new DefaultLineTracker(), (CDocumentProvider)fProvider, input);
			} else {
				buffer = new Buffer(this);
			}
		}
		return buffer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.IWorkingCopy#getOriginalElement()
	 */
	public ITranslationUnit getOriginalElement() {
		return this;
	}

}
