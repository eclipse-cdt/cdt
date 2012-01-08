/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Iterator;

import org.eclipse.jface.text.source.IAnnotationModel;


/**
 * Filters problems based on their types.
 */
public class ProblemAnnotationIterator implements Iterator<IProblemAnnotation> {
			
	private Iterator<?> fIterator;
	private IProblemAnnotation fNext;
	
	public ProblemAnnotationIterator(IAnnotationModel model) {
		fIterator= model.getAnnotationIterator();
		skip();
	}
	
	private void skip() {
		while (fIterator.hasNext()) {
			Object next= fIterator.next();
			if (next instanceof IProblemAnnotation) {
				fNext= (IProblemAnnotation) next;
				return;
			}
		}
		fNext= null;
	}
	
	/*
	 * @see Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return fNext != null;
	}

	/*
	 * @see Iterator#next()
	 */
	@Override
	public IProblemAnnotation next() {
		try {
			return fNext;
		} finally {
			skip();
		}
	}

	/*
	 * @see Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}


