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
import org.eclipse.search.ui.IGroupByKeyComputer;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GroupByKeyComputer implements IGroupByKeyComputer {

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.IGroupByKeyComputer#computeGroupByKey(org.eclipse.core.resources.IMarker)
	 */
	 
	public Object computeGroupByKey(IMarker marker) {
		if( marker == null ){
			return null;
		}
		
	//	ICElement element = getCElement( marker );
	//	if( element != null && element.exists() ){
	//		return _lastHandle;
	//	}
		
		return null;
	}
	/*
	private String getJavaElementHandleId(IMarker marker) {
		try {
			return (String)marker.getAttribute(ICSearchUIConstants.ATT_JE_HANDLE_ID);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, CSearchMessages.getString("Search.Error.markerAttributeAccess.title"), CSearchMessages.getString("Search.Error.markerAttributeAccess.message")); //$NON-NLS-2$ //$NON-NLS-1$
			return null;
		}
	}
	
	private ICElement getCElement( IMarker marker ){
		String handle = getCElementHandleId( marker );
		if( handle == null ){
			_lastHandle = null;
			_lastElement = null;
			return null;
		}
		if( !handle.equals( _lastHandle ) ){
			_lastElement = SearchUtil
		}
		return _lastElement;
	}*/
	
	private String 	  _lastHandle = null;
	private ICElement _lastElement = null;

}
