/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jun 18, 2003
 */
package org.eclipse.cdt.ui;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchResultCollector;
import org.eclipse.cdt.internal.ui.search.CSearchResultPage;
import org.eclipse.cdt.internal.ui.search.NewSearchResultCollector;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchResultLabelProvider extends LabelProvider {
 
	public static final int SHOW_NAME_ONLY		   = 0; 
	public static final int SHOW_ELEMENT_CONTAINER = 1;
	public static final int SHOW_CONTAINER_ELEMENT = 2;
	public static final int SHOW_PATH			   = 3;//default
	
	public static final String POTENTIAL_MATCH = CSearchMessages.getString("CSearchResultLabelProvider.potentialMatch"); //$NON-NLS-1$

	public CSearchResultLabelProvider(){
		_sortOrder = SHOW_PATH;
	}
	
	/**
	 * @param page
	 */
	public CSearchResultLabelProvider(CSearchResultPage page) {
		
		// TODO Auto-generated constructor stub
	}

	public Image getImage( Object element ) {
		IMatch match = null;
		int elementType = -1;
		int visibility = -1;
		if( element instanceof ISearchResultViewEntry ){
			ISearchResultViewEntry viewEntry = (ISearchResultViewEntry)element;
			IMarker marker = viewEntry.getSelectedMarker();
			try {
				match = (IMatch) marker.getAttribute( CSearchResultCollector.IMATCH );
				if( match == null )
					return null;
				elementType = match.getElementType();
				visibility = match.getVisibility();
			} catch (CoreException e) {
				return null;
			}
		} else if ( element instanceof IMatch ){
			match = (IMatch) element;
			if( match == null )
				return null;
			elementType = match.getElementType();
			visibility = match.getVisibility();
		} else if (element instanceof ICElement){
			elementType = ((ICElement) element).getElementType();
		} else if (element instanceof String){
			String eleString = (String) element;
			int elIndex = eleString.indexOf(NewSearchResultCollector.ELEMENTTYPE);
			int vizIndex = eleString.indexOf(NewSearchResultCollector.VISIBILITY);
			
			String elType = eleString.substring(elIndex+NewSearchResultCollector.ELEMENTTYPE_LENGTH,vizIndex);
			String elViz = eleString.substring(vizIndex+NewSearchResultCollector.VISIBILITY_LENGTH,eleString.length());
			
			elementType = new Integer(elType).intValue();
			visibility = new Integer(elViz).intValue();
		}
		
		
			
		ImageDescriptor imageDescriptor = null;
		
		switch( elementType ){
			case ICElement.C_PROJECT:		imageDescriptor = CPluginImages.DESC_OBJS_SEARCHHIERPROJECT;  break;
			case ICElement.C_CCONTAINER:    imageDescriptor = CPluginImages.DESC_OBJS_SEARCHHIERFODLER; 	break;
			case ICElement.C_CLASS:			imageDescriptor = CPluginImages.DESC_OBJS_CLASS;		break;
			case ICElement.C_STRUCT:		imageDescriptor = CPluginImages.DESC_OBJS_STRUCT;		break;
			case ICElement.C_UNION:			imageDescriptor = CPluginImages.DESC_OBJS_UNION;		break;
			case ICElement.C_NAMESPACE:		imageDescriptor = CPluginImages.DESC_OBJS_NAMESPACE;	break;
			case ICElement.C_ENUMERATION:	imageDescriptor = CPluginImages.DESC_OBJS_ENUMERATION;	break;
			case ICElement.C_MACRO:			imageDescriptor = CPluginImages.DESC_OBJS_MACRO;		break;
			case ICElement.C_FUNCTION:		imageDescriptor = CPluginImages.DESC_OBJS_FUNCTION;		break;
			case ICElement.C_VARIABLE:		imageDescriptor = CPluginImages.DESC_OBJS_VARIABLE;		break;
			case ICElement.C_ENUMERATOR:	imageDescriptor = CPluginImages.DESC_OBJS_ENUMERATOR;	break;
			case ICElement.C_TYPEDEF:		imageDescriptor = CPluginImages.DESC_OBJS_TYPEDEF;		break;
			case ICElement.C_UNIT:			imageDescriptor = CPluginImages.DESC_OBJS_TUNIT;		break;
			case ICElement.C_FIELD:		
			{
				switch( visibility ){
					case ICElement.CPP_PUBLIC:	imageDescriptor = CPluginImages.DESC_OBJS_PUBLIC_FIELD;		break;
					case ICElement.CPP_PRIVATE:	imageDescriptor = CPluginImages.DESC_OBJS_PRIVATE_FIELD;	break;
					default:					imageDescriptor = CPluginImages.DESC_OBJS_PROTECTED_FIELD;	break;
				}
				break;
			}
			case ICElement.C_METHOD:
			{
				switch( visibility ){
					case ICElement.CPP_PUBLIC:	imageDescriptor = CPluginImages.DESC_OBJS_PUBLIC_METHOD;	break;
					case ICElement.CPP_PRIVATE:	imageDescriptor = CPluginImages.DESC_OBJS_PRIVATE_METHOD;	break;
					default:					imageDescriptor = CPluginImages.DESC_OBJS_PROTECTED_METHOD;	break;
				}
				break;
			}
		}
		
		int flags = 0;
		
		if (match != null){
			if( match.isStatic()   ) flags |= CElementImageDescriptor.STATIC;
			if( match.isConst()    ) flags |= CElementImageDescriptor.CONSTANT;
			if( match.isVolatile() ) flags |= CElementImageDescriptor.VOLATILE;
		}
		
		imageDescriptor = new CElementImageDescriptor( imageDescriptor, flags, SMALL_SIZE );

		Image image = CUIPlugin.getImageDescriptorRegistry().get( imageDescriptor );
				
		return image;
	}
	
	public String getText( Object element ) {
		IMatch match = null;
		
		if( element instanceof ISearchResultViewEntry ){
			ISearchResultViewEntry viewEntry = (ISearchResultViewEntry) element;
		
			IMarker marker = viewEntry.getSelectedMarker();
		
			try {
				match = (IMatch) marker.getAttribute(CSearchResultCollector.IMATCH);
			} catch (CoreException e) {
				return ""; //$NON-NLS-1$
			}
		} else if( element instanceof IMatch ){
			match = (IMatch) element;
		} else if ( element instanceof ICElement){
			return  getElementText((ICElement) element);
		} else if (element instanceof String){
			String elString = (String) element;
			
			int parentIndex = elString.indexOf(NewSearchResultCollector.PARENT);
			int nameIndex = elString.indexOf(NewSearchResultCollector.NAME);
			int locationIndex = elString.indexOf(NewSearchResultCollector.LOCATION);
			int elementIndex = elString.indexOf(NewSearchResultCollector.ELEMENTTYPE);
			
			String elParent = elString.substring(parentIndex+NewSearchResultCollector.PARENT_LENGTH,nameIndex);
			String elName = elString.substring(nameIndex+NewSearchResultCollector.NAME_LENGTH,locationIndex);
			String elPath = elString.substring(locationIndex+NewSearchResultCollector.LOCATION_LENGTH, elementIndex);
			
			return getCSearchSortElementText(elParent, elName, elPath);
		}
		
		if( match == null )
			return ""; //$NON-NLS-1$
		
		IResource resource = match.getResource();
		
		String result = ""; //$NON-NLS-1$
		String path = "";  //$NON-NLS-1$
		if (resource != null){
			if (resource.isLinked()){
				path = match.getLocation().toOSString();
			}
			else{
				path = resource.getFullPath().toOSString();
			}
		}
		
		switch( getOrder() ){
			case SHOW_NAME_ONLY:
				result = match.getName();
			case SHOW_ELEMENT_CONTAINER:
				if( !match.getParentName().equals("") ) //$NON-NLS-1$
					result = match.getName() + " - " + match.getParentName() + " ( " + path + " )"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				else
					result = match.getName() + " ( " + path + " )"; //$NON-NLS-1$ //$NON-NLS-2$
						
				break;
			case SHOW_PATH:
				result = path + " - " + match.getParentName()+ "::" + match.getName(); //$NON-NLS-1$ //$NON-NLS-2$
				break;				
			case SHOW_CONTAINER_ELEMENT:
				result = match.getParentName() + "::" + match.getName() + " ( " + path + " )"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
		}
		
		return result;
	}
	
	/**
	 * @param element
	 * @return
	 */
	private String getCSearchSortElementText(String parentName, String name, String path) {
		String result = ""; //$NON-NLS-1$
		
		switch( getOrder() ){
			case SHOW_NAME_ONLY:
				result = name;
			case SHOW_ELEMENT_CONTAINER:
				if( !parentName.equals("") ) //$NON-NLS-1$
					result = name + " - " + parentName + " ( " + path + " )"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				else
					result = name+ " ( " + path + " )"; //$NON-NLS-1$ //$NON-NLS-2$
						
				break;
			case SHOW_PATH:
				result = path + " - " + parentName + "::" + name; //$NON-NLS-1$ //$NON-NLS-2$
				break;				
			case SHOW_CONTAINER_ELEMENT:
				result = parentName + "::" + name + " ( " + path + " )"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
		}
	
		return result;
	}

	private String getElementText(ICElement element){
		
		String result=""; //$NON-NLS-1$
		String path=""; //$NON-NLS-1$
		ICElement parent=element.getParent();
		
	
		result = element.getElementName() + " ( " + element.getPath() + " )"; //$NON-NLS-1$ //$NON-NLS-2$
		
		
				
		return result;
	}
	
	public int getOrder(){
		return _sortOrder;
	}
	public void setOrder(int orderFlag) {
		_sortOrder = orderFlag;
	}
	
	private int _sortOrder;
	private int _textFlags;
	private int _imageFlags;

	private static final Point SMALL_SIZE= new Point(16, 16);

	
}
