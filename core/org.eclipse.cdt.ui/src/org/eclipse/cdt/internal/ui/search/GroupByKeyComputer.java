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

import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
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
		
		IMatch match = null;
		
		try {
			match = (IMatch) marker.getAttribute(CSearchResultCollector.IMATCH);
		} catch (CoreException e) {
		}
		
		return match.getParentName();
	}
}
