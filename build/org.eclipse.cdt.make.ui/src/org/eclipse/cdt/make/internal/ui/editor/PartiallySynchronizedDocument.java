/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ISynchronizable;

/**
 * PartiallySynchronizedDocument
 * Document that can also be used by a background reconciler.
 */
public class PartiallySynchronizedDocument extends Document implements ISynchronizable {
    
    private final Object fInternalLockObject= new Object();
    private Object fLockObject;
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentExtension#startSequentialRewrite(boolean)
	 */
	synchronized public void startSequentialRewrite(boolean normalized) {
		super.startSequentialRewrite(normalized);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentExtension#stopSequentialRewrite()
	 */
	synchronized public void stopSequentialRewrite() {
		super.stopSequentialRewrite();
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#get()
	 */
	synchronized public String get() {
		return super.get();
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#get(int, int)
	 */
	synchronized public String get(int offset, int length) throws BadLocationException {
		return super.get(offset, length);
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getChar(int)
	 */
	synchronized public char getChar(int offset) throws BadLocationException {
		return super.getChar(offset);
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#replace(int, int, java.lang.String)
	 */
	synchronized public void replace(int offset, int length, String text) throws BadLocationException {
		super.replace(offset, length, text);
		//TODO to be used for incremental parsing...not for 3.0
//		if (length == 0 && text != null) {
//			// Insert
//		} else if (text == null || text.length() == 0) {
//			// Remove
//		} else {
//			fAntModel.setReplaceHasOccurred();
//		}
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#set(java.lang.String)
	 */
	synchronized public void set(String text) {
		super.set(text);
	}
	
	/*
     * @see org.eclipse.jface.text.ISynchronizable#setLockObject(java.lang.Object)
     */
    public void setLockObject(Object lockObject) {
        fLockObject= lockObject;
    }

    /*
     * @see org.eclipse.jface.text.ISynchronizable#getLockObject()
     */
    public Object getLockObject() {
        return fLockObject == null ? fInternalLockObject : fLockObject;
    }
}
