/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;


public class CMarkerAnnotation extends MarkerAnnotation implements IProblemAnnotation, ICAnnotation {		

	public static final String C_MARKER_TYPE_PREFIX= "org.eclipse.cdt"; //$NON-NLS-1$
	public static final String ERROR_ANNOTATION_TYPE= "org.eclipse.cdt.ui.error"; //$NON-NLS-1$
	public static final String WARNING_ANNOTATION_TYPE= "org.eclipse.cdt.ui.warning"; //$NON-NLS-1$
	public static final String INFO_ANNOTATION_TYPE= "org.eclipse.cdt.ui.info"; //$NON-NLS-1$
	public static final String TASK_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.task"; //$NON-NLS-1$

	private boolean fIsProblemMarker;
	private IDocument fDocument;
	private int error_start = -1;
	private int error_length = 0;

	private ICAnnotation fOverlay;

	public CMarkerAnnotation(IMarker marker, IDocument document) {
		super(marker);
		fDocument = document;
		if (MarkerUtilities.isMarkerType(getMarker(), ICModelMarker.C_MODEL_PROBLEM_MARKER)) {
			fIsProblemMarker = true;
			try {
				String var = (String) getMarker().getAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE);
				if(var != null && fDocument != null) {
					int line = getMarker().getAttribute(IMarker.LOCATION, -1) - 1;
					if(line >= 0) {
						int position = fDocument.getLineOffset(line);
						String text = fDocument.get(position, fDocument.getLineLength(line));
						int start = 0, end, pos;
						pos = 0;
						while(start != -1) {
							start = getStart(text, var, pos);
							if(start >= 0) {
								if(Character.isJavaIdentifierStart(text.charAt(start + var.length())) == false) {
									break;
								}
							}
							pos += var.length();
						}
						//start = text.indexOf(var);
						if(start >= 0) {
							end = start + var.length();
							// We cannot just update the marker attributes as the workspace resource tree is locked
							Map map = marker.getAttributes();
							MarkerUtilities.setCharStart(map, position+start);
							MarkerUtilities.setCharEnd(map, position+end);
							error_start = position+start;
							error_length = end-start;
							
							//getMarker().setAttribute(IMarker.CHAR_START, position+start);
							//getMarker().setAttribute(IMarker.CHAR_END, position+end);
						}
					}
				}
			} catch (CoreException e) {
			} catch (BadLocationException e) {
			}
		}
	}
	/**
	 * Ensures that we return a value that is not encased in a String
	 */
	private int getStart(String text, String var, int pos){
		
		// determine if there are more than one instance of var in this text
		int count = 0;
		int currentPosition = pos;
		while(currentPosition != -1){			
			currentPosition = text.indexOf(var, currentPosition);
			if (currentPosition != -1){
				// found one!
				count++;
				currentPosition += var.length();
			}
		}
		// only one occurrence return its index
		if (count == 1){
			return text.indexOf(var, pos);
		}
		
		// otherwise we need to find the first one not inside of quotes
		int indexOfStringStart = pos;
		
		final String QUOTE = "\""; //$NON-NLS-1$
		indexOfStringStart =  text.indexOf(QUOTE, indexOfStringStart);
		int newPosition = -1;
		currentPosition = pos; // reinitialize currentPosition
		
		if (indexOfStringStart == -1) {
			// No Strings ... return the first occurrence of var
			newPosition = text.indexOf(var, currentPosition);	
		} else {
			// we have Strings
			StringTokenizer tokens = new StringTokenizer(text.substring(currentPosition), QUOTE, true);
			String nextToken = null;
			int quoteCount = 0;
			int potentialStart = -1;
			boolean found = false;
			
			while (tokens.hasMoreTokens() && !found){
				nextToken = tokens.nextToken();	
				if(QUOTE.equals(nextToken)){
					quoteCount++;					
				} else {
					if ((quoteCount % 2) == 0){
						// no open quotes .. we can check this token
						potentialStart = nextToken.indexOf(var, 0);
						if (potentialStart != -1){
							found = true;
							currentPosition += potentialStart;
							newPosition = currentPosition;
							break;
						}
					}// else ... we have an open quote and must
					 // throw away this non-quote token		
				}		
				currentPosition += nextToken.length();
			}
		}
		
		return newPosition;		
	}

	/**
	 * Initializes the annotation's icon representation and its drawing layer
	 * based upon the properties of the underlying marker.
	 */
	protected void initialize() {
		try {
			IMarker marker= getMarker();
			
			if (MarkerUtilities.isMarkerType(marker, SearchUI.SEARCH_MARKER)) {
				setLayer(2);
				setImage(SearchUI.getSearchMarkerImage());
				fIsProblemMarker= false;
				return;
			} else if (MarkerUtilities.isMarkerType(getMarker(), ICModelMarker.C_MODEL_PROBLEM_MARKER)) {
				fIsProblemMarker = true;
				String var = (String) marker.getAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE);
				if(var != null && fDocument != null) {
					int line = marker.getAttribute(IMarker.LOCATION, -1);
					if(line >= 0 && line == -1) {
						try {
							int position = fDocument.getLineOffset(line);
							System.out.println("offset " + position); //$NON-NLS-1$
							String text = fDocument.get(position, fDocument.getLineLength(line));
							System.out.println("text:" + text); //$NON-NLS-1$
						} catch (BadLocationException e) {}
					}
				}
			} else {
				if(marker.isSubtypeOf(ICModelMarker.C_MODEL_PROBLEM_MARKER)) { //|| getMarker().isSubtypeOf(IMarker.SEVERITY_WARNING)) {
					fIsProblemMarker= true;
				} else if (marker.isSubtypeOf(IMarker.TASK) || marker.isSubtypeOf(ICModelMarker.TASK_MARKER)) {
					fIsProblemMarker= false;
				} else
					fIsProblemMarker = true;
			}
			
		} catch (CoreException e) {
		}
		
		super.initialize();
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
	
	/**
	 * @see IProblemAnnotation#isProblem()
	 */
	public boolean isProblem() {
		return fIsProblemMarker;
	}
	
	public int getErrorStart() {
		return error_start;
	}
	public int getErrorLength() {
		return error_length;
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

	/*
	 * @see org.eclipse.cdt.internal.ui.editor.ICAnnotation#getImage(org.eclipse.swt.widgets.Display)
	 */
	public Image getImage(Display display) {
		return super.getImage(display);
	}

}