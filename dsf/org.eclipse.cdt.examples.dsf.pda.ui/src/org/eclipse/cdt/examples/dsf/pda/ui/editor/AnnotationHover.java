/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.editor;

import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Returns hover for breakpoints.
 * <p>
 * This class is identical to the corresponding in PDA debugger implemented in 
 * org.eclipse.debug.examples.ui.
 * </p>
 */
public class AnnotationHover implements IAnnotationHover {

	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
		Iterator<?> iterator = annotationModel.getAnnotationIterator();
		while (iterator.hasNext()) {
			Annotation annotation = (Annotation) iterator.next();
			Position position = annotationModel.getPosition(annotation);
			try {
				int lineOfAnnotation = sourceViewer.getDocument().getLineOfOffset(position.getOffset());
				if (lineNumber == lineOfAnnotation) {
					return annotation.getText();
				}
			} catch (BadLocationException e) {
			}
		}
		return null;
	}
}
