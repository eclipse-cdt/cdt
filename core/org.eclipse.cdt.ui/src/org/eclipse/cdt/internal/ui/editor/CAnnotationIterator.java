/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Filters problems based on their types.
 */
public class CAnnotationIterator implements Iterator<Annotation> {
	private Iterator<Annotation> fIterator;
	private Annotation fNext;
	private boolean fReturnAllAnnotations;
	
	/**
	 * Returns a new CAnnotationIterator.
	 * @param parent the parent iterator to iterate over annotations
	 * @param returnAllAnnotations whether to return all annotations or just problem annotations
	 */
	public CAnnotationIterator(Iterator<Annotation> parent, boolean returnAllAnnotations) {
		fReturnAllAnnotations= returnAllAnnotations;
		fIterator= parent;
		skip();
	}

	private void skip() {
		while (fIterator.hasNext()) {
			Annotation next= fIterator.next();
			
			if (next.isMarkedDeleted())
				continue;

			if (fReturnAllAnnotations || next instanceof ICAnnotation || isProblemMarkerAnnotation(next)) {
				fNext= next;
				return;
			}
		}
		fNext= null;
	}

	private static boolean isProblemMarkerAnnotation(Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation))
			return false;
		try {
			return(((MarkerAnnotation)annotation).getMarker().isSubtypeOf(IMarker.PROBLEM));
		} catch (CoreException e) {
			return false;
		}
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
	public Annotation next() {
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
