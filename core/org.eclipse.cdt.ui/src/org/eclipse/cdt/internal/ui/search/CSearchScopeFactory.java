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
 * Created on Jun 12, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchScopeFactory {
	private static CSearchScopeFactory fgInstance;
	private static ICSearchScope EMPTY_SCOPE= SearchEngine.createCSearchScope(new Object[] {});
	
	/**
	 * 
	 */
	public CSearchScopeFactory() {
		super();
	}
	
	public static CSearchScopeFactory getInstance() {
		if (fgInstance == null)
			fgInstance = new CSearchScopeFactory();
		return fgInstance;
	}

	/**
	 * @param sets
	 * @return
	 */
	public ICSearchScope createCSearchScope(IWorkingSet[] sets) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param fStructuredSelection
	 * @return
	 */
	public ICSearchScope createCSearchScope(IStructuredSelection fStructuredSelection) {
		// TODO Auto-generated method stub
		return null;
	}

}
