package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

import org.eclipse.cdt.core.model.ICModelMarker;


public class CMarkerAnnotation extends MarkerAnnotation implements IProblemAnnotation {		
	
	private boolean fIsProblemMarker;
	private IDocument fDocument;
	private int error_start = -1;
	private int error_length = 0;
	private IDebugModelPresentation fPresentation;

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
							start = text.indexOf(var, pos);
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
			}
			else if (MarkerUtilities.isMarkerType(marker, IBreakpoint.LINE_BREAKPOINT_MARKER)) {
				if (fPresentation == null) 
					fPresentation= DebugUITools.newDebugModelPresentation();
				
				setLayer(4);
				setImage(fPresentation.getImage(marker));
			
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
							System.out.println("offset " + position);
							String text = fDocument.get(position, fDocument.getLineLength(line));
							System.out.println("text:" + text);
						} catch (BadLocationException e) {}
					}
				}
			} else {
				if(marker.isSubtypeOf(ICModelMarker.C_MODEL_PROBLEM_MARKER)) { //|| getMarker().isSubtypeOf(IMarker.SEVERITY_WARNING)) {
					fIsProblemMarker= true;
				}
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
			return getMarker().getAttribute(IMarker.MESSAGE, "");
		return "";
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
		String [] s = {"problem", "here"};
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
}