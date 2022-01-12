/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import java.util.Iterator;

import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * ICAnnotation
 *
 * Interface of annotations representing markers
 * and problems.
 *
 * @see org.eclipse.core.resources.IMarker
 * @see org.eclipse.cdt.core.parser.IProblem
 */
public interface ICAnnotation {

	/**
	 * @see org.eclipse.jface.text.source.Annotation#getType()
	 */
	String getType();

	/**
	 * @see org.eclipse.jface.text.source.Annotation#isPersistent()
	 */
	boolean isPersistent();

	/**
	 * @see org.eclipse.jface.text.source.Annotation#isMarkedDeleted()
	 */
	boolean isMarkedDeleted();

	/**
	 * @see org.eclipse.jface.text.source.Annotation#getText()
	 */
	String getText();

	/**
	 * Returns whether this annotation is overlaid.
	 *
	 * @return <code>true</code> if overlaid
	 */
	boolean hasOverlay();

	/**
	 * Returns the overlay of this annotation.
	 *
	 * @return the annotation's overlay
	 */
	ICAnnotation getOverlay();

	/**
	 * Returns an iterator for iterating over the
	 * annotation which are overlaid by this annotation.
	 *
	 * @return an iterator over the overlaid annotations
	 */
	Iterator<ICAnnotation> getOverlaidIterator();

	/**
	 * Adds the given annotation to the list of
	 * annotations which are overlaid by this annotations.
	 *
	 * @param annotation	the problem annotation
	 */
	void addOverlaid(ICAnnotation annotation);

	/**
	 * Removes the given annotation from the list of
	 * annotations which are overlaid by this annotation.
	 *
	 * @param annotation	the problem annotation
	 */
	void removeOverlaid(ICAnnotation annotation);

	/**
	 * Tells whether this annotation is a problem
	 * annotation.
	 *
	 * @return <code>true</code> if it is a problem annotation
	 */
	boolean isProblem();

	/**
	 * Returns the compilation unit corresponding to the document on which the annotation is set
	 * or <code>null</code> if no corresponding co0mpilationunit exists.
	 */
	ITranslationUnit getTranslationUnit();

	String[] getArguments();

	int getId();

	/**
	 * Returns the marker type associated to this problem or <code>null<code> if no marker type
	 * can be evaluated. See also {@link org.eclipse.cdt.ui.text.IProblemLocation#getMarkerType()}.
	 *
	 * @return the type of the marker which would be associated to the problem or
	 * <code>null<code> if no marker type can be evaluated.
	 */
	String getMarkerType();
}
