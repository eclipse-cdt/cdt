/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;


import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class ErrorTickAdornmentProvider implements IAdornmentProvider {
	
	private static final int ERRORTICK_WARNING= CElementImageProvider.OVERLAY_WARNING;
	private static final int ERRORTICK_ERROR= CElementImageProvider.OVERLAY_ERROR;	
	
	/*
	 * @see IAdornmentProvider#computeAdornmentFlags(Object, int)
	 */
	public int computeAdornmentFlags(Object obj) {
		try {
			if (obj instanceof ICElement) {
				ICElement element= (ICElement) obj;
				
				int type= element.getElementType();
				switch (type) {
					case ICElement.C_PROJECT:
					case ICElement.C_CCONTAINER:
						return getErrorTicksFromMarkers(element.getResource(), IResource.DEPTH_INFINITE, null);
					case ICElement.C_UNIT:
						return getErrorTicksFromMarkers(element.getResource(), IResource.DEPTH_ONE, null);
					case ICElement.C_FUNCTION:
					case ICElement.C_CLASS:
					case ICElement.C_UNION:
					case ICElement.C_STRUCT:
					case ICElement.C_VARIABLE:
					case ICElement.C_METHOD:
						ITranslationUnit tu= ((ISourceReference)element).getTranslationUnit();
						if (tu != null && tu.exists()) {
							// I assume that only source elements in compilation unit can have markers
							ISourceRange range= ((ISourceReference)element).getSourceRange();
							return getErrorTicksFromMarkers(tu.getResource(), IResource.DEPTH_ONE, range);
						}
					default:
				}
			} else if (obj instanceof IResource) {
				return getErrorTicksFromMarkers((IResource) obj, IResource.DEPTH_INFINITE, null);
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		return 0;
	}
	
	/*
	 * @see IAdornmentProvider#dispose()
	 */
	public void dispose() {
	}	
	
	private int getErrorTicksFromMarkers(IResource res, int depth, ISourceRange range) throws CoreException {
		// Trying to call findMarkers() on non existing resources will throw an exception
		// findMarkers() --> CoreException - if this method fails.
		// Reasons include: 
		//  This resource does not exist. 
		//  This resource is a project that is not open. 
		if (res == null || !res.isAccessible()) { // for elements in archives
			return 0;
		}
		int info= 0;
		
		IMarker[] markers= res.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, depth);
		if (markers != null) {
			for (int i= 0; i < markers.length && (info != ERRORTICK_ERROR); i++) {
				IMarker curr= markers[i];
				if (range == null || isMarkerInRange(curr, range)) {
					int priority= curr.getAttribute(IMarker.SEVERITY, -1);
					if (priority == IMarker.SEVERITY_WARNING) {
						info= ERRORTICK_WARNING;
					} else if (priority == IMarker.SEVERITY_ERROR) {
						info= ERRORTICK_ERROR;
					}
				}
			}			
		}
		return info;
	}
	
	private boolean isMarkerInRange(IMarker marker, ISourceRange range) throws CoreException {
		if (marker.isSubtypeOf(IMarker.TEXT)) {
			int pos= marker.getAttribute(IMarker.CHAR_START, -1);
			int offset= range.getStartPos();
			if(pos == -1) {
				int line= MarkerUtilities.getLineNumber(marker);
				if (line >= 0) {
					return (line >= range.getStartLine() && line <= range.getEndLine());
				}
				
			}
			return (offset <= pos && offset + range.getLength() > pos);
		}
		return false;
	}
	
			
	private IMarker isAnnotationInRange(IAnnotationModel model, Annotation annot, ISourceRange range) throws CoreException {
			if (annot instanceof MarkerAnnotation) {
				IMarker marker= ((MarkerAnnotation)annot).getMarker();
				if (marker.exists() && marker.isSubtypeOf(ICModelMarker.C_MODEL_PROBLEM_MARKER)) {
					Position pos= model.getPosition(annot);
					if (pos.overlapsWith(range.getStartPos(), range.getLength())) {
						return marker;
					}
				}
			}
				
		return null;
	}
}


