package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.jface.text.source.IAnnotationModel;


/**
 * Filters problems based on their types.
 */
public class ProblemAnnotationIterator implements Iterator {
			
	private Iterator fIterator;
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
	public boolean hasNext() {
		return fNext != null;
	}

	/*
	 * @see Iterator#next()
	 */
	public Object next() {
		try {
			return fNext;
		} finally {
			skip();
		}
	}

	/*
	 * @see Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}


