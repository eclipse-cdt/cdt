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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.SearchUI;
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

	public Image getImage( Object element ) {
		return null;	
	}
	
	public String getText( Object element ) {
		_lastMarker = null;
		
		ICElement cElement = getCElement( element );
		
		boolean isPotentialMatch = _lastMarker != null && _lastMarker.getAttribute( SearchUI.POTENTIAL_MATCH, false );
		
		if( cElement == null ){
			if( _lastMarker != null ){
				if( isPotentialMatch )
					return super.getText( _lastMarker.getResource() ) + POTENTIAL_MATCH;
				else 
					return super.getText( _lastMarker.getResource() );
			} else {
				return "";
			}
		}
		
		String text = "";
		if( isPotentialMatch ){
			text = CElementLabels.getTextLabel( element, _textFlags ) + POTENTIAL_MATCH;
		} else {
			text = CElementLabels.getTextLabel( element, _textFlags );
		}
		//if( cElement instanceof )
		
		return element == null ? "" : element.toString();//$NON-NLS-1$	
	}
	
	public void setOrder(int orderFlag) {
		int flags = DEFAULT_TEXTFLAGS | CElementLabels.P_COMPRESSED;
		
		switch( orderFlag ){
			case SHOW_ELEMENT_CONTAINER:
				flags |= CElementLabels.ALL_POST_QUALIFIED | CElementLabels.M_PARAMETER_TYPES;
				break;
			case SHOW_PATH:
				flags |= CElementLabels.PREPEND_ROOT_PATH;
				/*fall through to SHOW_CONTAINER_ELEMENT*/
			case SHOW_CONTAINER_ELEMENT:
				flags |= CElementLabels.ALL_FULLY_QUALIFIED | CElementLabels.M_PARAMETER_TYPES;
				break;
		}
			
		_textFlags = flags;
	}
	
	protected IMarker getMarker( Object o ){
		if( !( o instanceof ISearchResultViewEntry ) ){
			return null;
		}
		
		return ( (ISearchResultViewEntry)o ).getSelectedMarker();
	}
	
	private ICElement getCElement( Object o ){
		if( o instanceof ICElement )
			return (ICElement) o;

		IMarker marker = getMarker( o );
		if( marker == null )
			return null;
			
		return getCElement( marker, (ISearchResultViewEntry) o );
	}
	
	private ICElement getCElement( IMarker marker, ISearchResultViewEntry entry ) {
		if( _lastMarker != marker ){
			boolean canUseGroupByKey = false;
			
			if( canUseGroupByKey && entry.getGroupByKey() instanceof ICElement ){
				_lastCElement = (ICElement) entry.getGroupByKey();
			} else {
				_lastCElement = CSearchUtil.getCElement( marker );
			}
			
			_lastMarker = marker;
		}
		return _lastCElement;
	}
	
	private IMarker _lastMarker;
	private ICElement _lastCElement;
	
	private int _textFlags;
	private int _imageFlags;
	
}
