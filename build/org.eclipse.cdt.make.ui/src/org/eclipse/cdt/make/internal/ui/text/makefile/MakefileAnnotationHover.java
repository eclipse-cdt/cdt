/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.make.internal.ui.text.makefile;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * MakefileAnnotationHover
 *  
 */
public class MakefileAnnotationHover implements IAnnotationHover {

	/**
	 *  
	 */
	public MakefileAnnotationHover() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer,
	 *      int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IDocument document = sourceViewer.getDocument();

		try {
			IRegion info = document.getLineInformation(lineNumber);
			return document.get(info.getOffset(), info.getLength());
		} catch (BadLocationException x) {
		}

		return null;

	}

}
