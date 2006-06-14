/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jun 12, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchUtil {

	public static int LRU_WORKINGSET_LIST_SIZE= 3;
	private static LRUWorkingSets workingSetsCache;
	
	/**
	 * 
	 */
	public CSearchUtil() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param sets
	 */
	public static void updateLRUWorkingSets(IWorkingSet[] workingSets) {
		if (workingSets == null || workingSets.length < 1)
			return;
		
		CSearchUtil.getLRUWorkingSets().add(workingSets);
	}

	public static LRUWorkingSets getLRUWorkingSets() {
		if (CSearchUtil.workingSetsCache == null) {
			CSearchUtil.workingSetsCache = new LRUWorkingSets(CSearchUtil.LRU_WORKINGSET_LIST_SIZE);
		}
		return CSearchUtil.workingSetsCache;
	}
	
	/**
	 * @param object
	 * @param shell
	 */
	public static void warnIfBinaryConstant( ICElement element, Shell shell) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * @param workingSets
	 * @return
	 */
	public static String toString(IWorkingSet[] workingSets) {
		if( workingSets != null & workingSets.length > 0 ){
			String string = new String();
			for( int i = 0; i < workingSets.length; i++ ){
				if( i > 0 )
					string += ", ";  //$NON-NLS-1$
				string += workingSets[i].getName();
			}
			
			return string;
		}
		
		return null;
	}


	/**
	 * @param marker
	 * @return
	 */
	public static ICElement getCElement(IMarker marker) {
		// TODO Auto-generated method stub
		return null;
	}
}
