/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 18, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.internal.ui.CElementImageProvider;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.swt.graphics.Image;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchResultLabelProvider extends LabelProvider {

	public static final int SHOW_ELEMENT_CONTAINER = 1; //default
	public static final int SHOW_CONTAINER_ELEMENT = 2;
	public static final int SHOW_PATH			   = 3;
	
	public final static int DEFAULT_TEXTFLAGS = CElementLabels.ROOT_VARIABLE 	 | 
												CElementLabels.M_PARAMETER_TYPES |  
												CElementLabels.M_APP_RETURNTYPE  |
												CElementLabels.REFERENCED_ROOT_POST_QUALIFIED;
	
	public static final String POTENTIAL_MATCH = CSearchMessages.getString("CSearchResultLabelProvider.potentialMatch"); //$NON-NLS-1$

	public CSearchResultLabelProvider(){
		_sortOrder = SHOW_ELEMENT_CONTAINER;
		//_imageProvider = new CElementImageProvider();
		//_labelProvider = new CElementLabelProvider();	
	}
	
	public Image getImage( Object element ) {
		if( !( element instanceof ISearchResultViewEntry ) ){
			return null;
		}
		
		ISearchResultViewEntry viewEntry = (ISearchResultViewEntry)element;
		IMarker marker = viewEntry.getSelectedMarker();
		Match match = null;
		try {
			match = (Match) marker.getAttribute( CSearchResultCollector.IMATCH );
		} catch (CoreException e) {
			return null;
		}
	
		ImageDescriptor imageDescriptor = match.imageDesc;
			
		Image image = CUIPlugin.getImageDescriptorRegistry().get( imageDescriptor );
				
		return image;
	}
	
	public String getText( Object element ) {
		if( ! (element instanceof ISearchResultViewEntry ) ){
			return null;
		}
		
		ISearchResultViewEntry viewEntry = (ISearchResultViewEntry) element;
		
		IMarker marker = viewEntry.getSelectedMarker();
		
		Match match = null;
		
		try {
			match = (Match) marker.getAttribute(CSearchResultCollector.IMATCH);
		} catch (CoreException e) {
			return null;
		}
		
		IResource resource = marker.getResource();
		
		String result = null;
		String path = (resource != null ) ? resource.getFullPath().toString() : "";
		
		switch( getOrder() ){
			case SHOW_ELEMENT_CONTAINER:
				result = match.name + " - " + match.parent + " ( " + path + " )";
				break;
			case SHOW_PATH:
				result = path + " - " + match.parent + "::" + match.name;
				break;				
			case SHOW_CONTAINER_ELEMENT:
				result = match.parent + "::" + match.name + " ( " + path + " )";
				break;
		}
		
		return result;
	}
	
	public int getOrder(){
		return _sortOrder;
	}
	public void setOrder(int orderFlag) {
		_sortOrder = orderFlag;
	}
	
	protected IMarker getMarker( Object o ){
		if( !( o instanceof ISearchResultViewEntry ) ){
			return null;
		}
		
		return ( (ISearchResultViewEntry)o ).getSelectedMarker();
	}
	
	
	private CElementImageProvider _imageProvider;
	private CElementLabelProvider _labelProvider;
	
	private int _sortOrder;
	private int _textFlags;
	private int _imageFlags;
	
}
