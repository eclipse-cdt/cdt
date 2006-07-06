/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ITranslationUnit;


public class CMarkerAnnotation extends MarkerAnnotation implements IProblemAnnotation, ICAnnotation {		

	public static final String C_MARKER_TYPE_PREFIX= "org.eclipse.cdt"; //$NON-NLS-1$
	public static final String ERROR_ANNOTATION_TYPE= "org.eclipse.cdt.ui.error"; //$NON-NLS-1$
	public static final String WARNING_ANNOTATION_TYPE= "org.eclipse.cdt.ui.warning"; //$NON-NLS-1$
	public static final String INFO_ANNOTATION_TYPE= "org.eclipse.cdt.ui.info"; //$NON-NLS-1$
	public static final String TASK_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.task"; //$NON-NLS-1$

	private boolean fIsProblemMarker;

	private ICAnnotation fOverlay;

	public CMarkerAnnotation(IMarker marker, IDocument document) {
		super(marker);
		fIsProblemMarker = MarkerUtilities.isMarkerType(getMarker(), ICModelMarker.C_MODEL_PROBLEM_MARKER);
	}

	/**
	 * @see IProblemAnnotation#getMessage()
	 */
	public String getMessage() {
		if (fIsProblemMarker)
			return getMarker().getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see IProblemAnnotation#isError()
	 */
	public boolean isError() {
		if (fIsProblemMarker) {
			int markerSeverity= getMarker().getAttribute(IMarker.SEVERITY, -1);
			return (markerSeverity == IMarker.SEVERITY_ERROR);
		}
		return false;
	}

	/**
	 * @see IProblemAnnotation#isWarning()
	 */
	public boolean isWarning() {
		if (fIsProblemMarker) {
			int markerSeverity= getMarker().getAttribute(IMarker.SEVERITY, -1);
			return (markerSeverity == IMarker.SEVERITY_WARNING);
		}
		return false;
	}
	
	/**
	 * @see IProblemAnnotation#isTemporaryProblem()
	 */
	public boolean isTemporaryProblem() {
		return false;
	}
	
	/**
	 * @see IProblemAnnotation#getArguments()
	 */
	public String[] getArguments() {
		String [] s = {"problem", "here"}; //$NON-NLS-1$ //$NON-NLS-2$
		//if (fIsProblemMarker)
		//	return Util.getProblemArgumentsFromMarker(getMarker().getAttribute(CCorePlugin.C_PROBLEMMARKER));
		return s;
		//returnm null;
	}

	/**
	 * @see IProblemAnnotation#getId()
	 */
	public int getId() {
		if (fIsProblemMarker)
			return getMarker().getAttribute(ICModelMarker.C_MODEL_PROBLEM_MARKER, -1);
		return 0;
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.editor.IProblemAnnotation#isProblem()
	 */
	public boolean isProblem() {
		return fIsProblemMarker;
	}
	
	/**
	 * Overlays this annotation with the given cAnnotation.
	 * 
	 * @param cAnnotation annotation that is overlaid by this annotation
	 */
	public void setOverlay(ICAnnotation cAnnotation) {
		if (fOverlay != null)
			fOverlay.removeOverlaid(this);
			
		fOverlay= cAnnotation;
		if (!isMarkedDeleted())
			markDeleted(fOverlay != null);
		
		if (fOverlay != null)
			fOverlay.addOverlaid(this);
	}

	/*
	 * @see ICAnnotation#hasOverlay()
	 */
	public boolean hasOverlay() {
		return fOverlay != null;
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.editor.ICAnnotation#getOverlay()
	 */
	public ICAnnotation getOverlay() {
		return fOverlay;
	}
	
	/*
	 * @see ICAnnotation#addOverlaid(ICAnnotation)
	 */
	public void addOverlaid(ICAnnotation annotation) {
		// not supported
	}

	/*
	 * @see ICAnnotation#removeOverlaid(ICAnnotation)
	 */
	public void removeOverlaid(ICAnnotation annotation) {
		// not supported
	}
	
	/*
	 * @see ICAnnotation#getOverlaidIterator()
	 */
	public Iterator getOverlaidIterator() {
		// not supported
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getCompilationUnit()
	 */
	public ITranslationUnit getTranslationUnit() {
		ICElement element= CoreModel.getDefault().create(getMarker().getResource());
		if (element instanceof ITranslationUnit) {
			return (ITranslationUnit)element;
		}
		return null;
	}
}