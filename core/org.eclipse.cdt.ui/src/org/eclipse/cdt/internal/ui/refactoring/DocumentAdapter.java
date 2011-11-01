/*******************************************************************************
 *  Copyright (c) 2002, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      Rational Software - Initial API and implementation
 *      Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.model.BufferChangedEvent;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.IBufferChangedListener;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.IBufferFactory;

/**
 * Adapts <code>IDocument</code> to <code>IBuffer</code>. Uses the
 * same algorithm as the text widget to determine the buffer's line delimiter. 
 * All text inserted into the buffer is converted to this line delimiter.
 * This class is <code>public</code> for test purposes only.
 * 
 * This class is similar to the JDT DocumentAdapter class.
 */
public class DocumentAdapter implements IBuffer, IAdaptable, IDocumentListener {
	/**
	 *  Executes a document set content call in the ui thread.
	 */
	protected class DocumentSetCommand implements Runnable {
		private String fContents;
		
		@Override
		public void run() {
			fDocument.set(fContents);
		}
	
		public void set(String contents) {
			fContents= contents;
			Display.getDefault().syncExec(this);
		}
	}

	/**
	 * Executes a document replace call in the ui thread.
	 */
	protected class DocumentReplaceCommand implements Runnable {
		private int fOffset;
		private int fLength;
		private String fText;
		
		@Override
		public void run() {
			try {
				fDocument.replace(fOffset, fLength, fText);
			} catch (BadLocationException x) {
				// ignore
			}
		}
		
		public void replace(int offset, int length, String text) {
			fOffset= offset;
			fLength= length;
			fText= text;
			Display.getDefault().syncExec(this);
		}
	}

	private static final boolean DEBUG_LINE_DELIMITERS= true;
	public static final IBuffer NULL_BUFFER = new IBuffer() {
		@Override
		public void addBufferChangedListener(IBufferChangedListener listener) {}
		@Override
		public void append(char[] text) {}
		@Override
		public void append(String text) {}
		@Override
		public void close() {}
		@Override
		public char getChar(int position) { return 0; }
		@Override
		public char[] getCharacters() { return new char[0]; }
		@Override
		public String getContents() { return ""; } //$NON-NLS-1$
		@Override
		public int getLength() { return 0; }
		@Override
		public IOpenable getOwner() {return null;}
		@Override
		public String getText(int offset, int length) { return ""; } //$NON-NLS-1$
		@Override
		public IResource getUnderlyingResource() { return null; }
		@Override
		public boolean hasUnsavedChanges() { return false; }
		@Override
		public boolean isClosed() { return false; }
		@Override
		public boolean isReadOnly() { return true; }
		@Override
		public void removeBufferChangedListener(IBufferChangedListener listener) {}
		@Override
		public void replace(int position, int length, char[] text) {}
		@Override
		public void replace(int position, int length, String text) {}
		@Override
		public void save(IProgressMonitor progress, boolean force) throws CModelException {}
		@Override
		public void setContents(char[] contents) {}
		@Override
		public void setContents(String contents) {}
	};

    public static IBufferFactory FACTORY= new IBufferFactory() {
        @Override
		public IBuffer createBuffer(IOpenable owner) {
        	if (owner instanceof IWorkingCopy) {
        		IWorkingCopy wc= (IWorkingCopy) owner;
        		ITranslationUnit tu= wc.getOriginalElement();
        		if (tu != null) {
        			IResource r= tu.getResource();
        			if (r instanceof IFile) {
        				return new DocumentAdapter(wc, (IFile) r);
        			}
        		}
        	}
        	assert false;
        	return DocumentAdapter.NULL_BUFFER;
        }
    };

	private ITranslationUnit fTranslationUnit;
	private IWorkingCopy fOwner;
	private IFile fFile;
	private ITextFileBuffer fTextFileBuffer;
	IDocument fDocument;

	private DocumentSetCommand fSetCmd= new DocumentSetCommand();
	private DocumentReplaceCommand fReplaceCmd= new DocumentReplaceCommand();

	private Set<String> fLegalLineDelimiters;

	private List<IBufferChangedListener> fBufferListeners= new ArrayList<IBufferChangedListener>(3);
	private IStatus fStatus;

	
	public DocumentAdapter(IWorkingCopy owner, IFile file) {
		fOwner= owner;
		fFile= file;
		fTranslationUnit= owner.getOriginalElement();
		if (fTranslationUnit != null) {
			addBufferChangedListener(fTranslationUnit);
		}
		initialize();			
	}

	private void initialize() {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath location= fFile.getFullPath();
		try {
			manager.connect(location, LocationKind.IFILE, new NullProgressMonitor());
			fTextFileBuffer= manager.getTextFileBuffer(location, LocationKind.IFILE);
			fDocument= fTextFileBuffer.getDocument();
		} catch (CoreException x) {
			fStatus= x.getStatus();
			fDocument= manager.createEmptyDocument(location, LocationKind.IFILE);
		}
		fDocument.addPrenotifiedDocumentListener(this);
	}

	/**
	 * Returns the status of this document adapter.
	 */
	public IStatus getStatus() {
		if (fStatus != null)
			return fStatus;
		if (fTextFileBuffer != null)
			return fTextFileBuffer.getStatus();
		return null;
	}
	
	/**
	 * Returns the adapted document.
	 * 
	 * @return the adapted document
	 */
	public IDocument getDocument() {
		return fDocument;
	}

	/*
	 * @see IBuffer#addBufferChangedListener(IBufferChangedListener)
	 */
	@Override
	public void addBufferChangedListener(IBufferChangedListener listener) {
		Assert.isNotNull(listener);
		if (!fBufferListeners.contains(listener))
			fBufferListeners.add(listener);
	}
	
	/*
	 * @see IBuffer#removeBufferChangedListener(IBufferChangedListener)
	 */
	@Override
	public void removeBufferChangedListener(IBufferChangedListener listener) {
		Assert.isNotNull(listener);
		fBufferListeners.remove(listener);
	}
	

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#append(char[])
	 */
	@Override
	public void append(char[] text) {
		append(new String(text));		
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#append(java.lang.String)
	 */
	@Override
	public void append(String text) {
		if (DEBUG_LINE_DELIMITERS) {
			validateLineDelimiters(text);
		}
		fReplaceCmd.replace(fDocument.getLength(), 0, text);		
	}


	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#close()
	 */
	@Override
	public void close() {
		if (isClosed())
			return;
			
		IDocument d= fDocument;
		fDocument= null;
		d.removePrenotifiedDocumentListener(this);
		
		if (fTextFileBuffer != null) {
			ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
			try {
				manager.disconnect(fTextFileBuffer.getLocation(), LocationKind.IFILE, new NullProgressMonitor());
			} catch (CoreException x) {
				// ignore
			}
			fTextFileBuffer= null;
		}
		
		fireBufferChanged(new BufferChangedEvent(this, 0, 0, null));
		fBufferListeners.clear();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#getChar(int)
	 */
	@Override
	public char getChar(int position) {
		try {
			return fDocument.getChar(position);
		} catch (BadLocationException x) {
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#getCharacters()
	 */
	@Override
	public char[] getCharacters() {
		String content= getContents();
		return content == null ? null : content.toCharArray();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#getContents()
	 */
	@Override
	public String getContents() {
		return fDocument.get();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#getLength()
	 */
	@Override
	public int getLength() {
		return fDocument.getLength();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#getOwner()
	 */
	@Override
	public IOpenable getOwner() {
		return fOwner;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#getText(int, int)
	 */
	@Override
	public String getText(int offset, int length) {
		try {
			return fDocument.get(offset, length);
		} catch (BadLocationException x) {
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#getUnderlyingResource()
	 */
	@Override
	public IResource getUnderlyingResource() {
		return fFile;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#hasUnsavedChanges()
	 */
	@Override
	public boolean hasUnsavedChanges() {
		return fTextFileBuffer != null ? fTextFileBuffer.isDirty() : false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#isClosed()
	 */
	@Override
	public boolean isClosed() {
		return fDocument == null;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		IResource resource= getUnderlyingResource();
		if (resource != null) {
			ResourceAttributes attributes = resource.getResourceAttributes();
			if (attributes != null) {
				return attributes.isReadOnly();
			}
		}
		return false;		
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#replace(int, int, char[])
	 */
	@Override
	public void replace(int position, int length, char[] text) {
		replace(position, length, new String(text));		
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#replace(int, int, java.lang.String)
	 */
	@Override
	public void replace(int position, int length, String text) {
		if (DEBUG_LINE_DELIMITERS) {
			validateLineDelimiters(text);
		}
		fReplaceCmd.replace(position, length, text);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#save(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	@Override
	public void save(IProgressMonitor progress, boolean force) throws CModelException {
		try {
			if (fTextFileBuffer != null)
				fTextFileBuffer.commit(progress, force);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#setContents(char[])
	 */
	@Override
	public void setContents(char[] contents) {
		setContents(new String(contents));		
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBuffer#setContents(java.lang.String)
	 */
	@Override
	public void setContents(String contents) {
		int oldLength= fDocument.getLength();
		
		if (contents == null) {
			if (oldLength != 0)
				fSetCmd.set(""); //$NON-NLS-1$
		} else {
			// set only if different
			if (DEBUG_LINE_DELIMITERS) {
				validateLineDelimiters(contents);
			}
			
			int newLength= contents.length();
			if (oldLength != newLength || !contents.equals(fDocument.get()))
				fSetCmd.set(contents);
		}
	}

	private void validateLineDelimiters(String contents) {
		if (fLegalLineDelimiters == null) {
			// collect all line delimiters in the document
			HashSet<String> existingDelimiters= new HashSet<String>();

			for (int i= fDocument.getNumberOfLines() - 1; i >= 0; i-- ) {
				try {
					String curr= fDocument.getLineDelimiter(i);
					if (curr != null) {
						existingDelimiters.add(curr);
					}
				} catch (BadLocationException e) {
					CUIPlugin.log(e);
				}
			}
			if (existingDelimiters.isEmpty()) {
				return; // first insertion of a line delimiter: no test
			}
			fLegalLineDelimiters= existingDelimiters;
			
		}
		
		DefaultLineTracker tracker= new DefaultLineTracker();
		tracker.set(contents);
		
		int lines= tracker.getNumberOfLines();
		if (lines <= 1)
			return;
		
		for (int i= 0; i < lines; i++) {
			try {
				String curr= tracker.getLineDelimiter(i);
				if (curr != null && !fLegalLineDelimiters.contains(curr)) {
					StringBuffer buf= new StringBuffer("New line delimiter added to new code: "); //$NON-NLS-1$
					for (int k= 0; k < curr.length(); k++) {
						buf.append(String.valueOf((int) curr.charAt(k)));
					}
					CUIPlugin.log(new Exception(buf.toString()));
				}
			} catch (BadLocationException e) {
                CUIPlugin.log(e);
			}
		}
	}

	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		// there is nothing to do here
	}

	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	@Override
	public void documentChanged(DocumentEvent event) {
		fireBufferChanged(new BufferChangedEvent(this, event.getOffset(), event.getLength(), event.getText()));
	}
	
	private void fireBufferChanged(BufferChangedEvent event) {
		if (fBufferListeners != null && fBufferListeners.size() > 0) {
			Iterator<IBufferChangedListener> e= new ArrayList<IBufferChangedListener>(fBufferListeners).iterator();
			while (e.hasNext())
				e.next().bufferChanged(event);
		}
	}

	public ITextFileBuffer getTextFileBuffer() {
		return fTextFileBuffer;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ITextFileBuffer.class)) {
			return fTextFileBuffer;
		} else if (adapter.isAssignableFrom(IDocument.class)) {
			return fDocument;
		}
		return null;
	}
}
