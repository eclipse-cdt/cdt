/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.tests.chelp;

import junit.framework.Assert;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

/**
 *
 * this class implements ICHelpProvider and provides test information
 */
public class CHelpTestInfoProvider implements ICHelpProvider {
	private static int fNumProviders = 0;
	private static final String PROVIDER_ID_PREFIX = "TestInfoProvider_";
	
	final private String fProviderID; 
	private boolean fIsInitialized = false;
	
	private ICHelpBook fCHelpBooks[];
	
	public CHelpTestInfoProvider(){
		fProviderID = PROVIDER_ID_PREFIX + fNumProviders++;
		fCHelpBooks = CHelpProviderTester.getDefault().generateCHelpBooks(fProviderID);
	}
	
	public static int getNumProviders(){
		return fNumProviders;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#initialize()
	 */
	public void initialize() {
		Assert.assertFalse("initialize is called several times",fIsInitialized);
		fIsInitialized = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getCHelpBooks()
	 */
	public ICHelpBook[] getCHelpBooks() {
		Assert.assertTrue("getCHelpBooks is called before completion contributor gets initialized",fIsInitialized);
		return fCHelpBooks;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getFunctionInfo(org.eclipse.cdt.ui.text.ICHelpInvocationContext, org.eclipse.cdt.ui.ICHelpBook[], java.lang.String)
	 */
	public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context,
			ICHelpBook[] helpBooks, String name) {
		Assert.assertTrue("getFunctionInfo is called before completion contributor gets initialized",fIsInitialized);
		return CHelpProviderTester.getDefault().generateFunctionInfo(helpBooks,name,fProviderID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getMatchingFunctions(org.eclipse.cdt.ui.text.ICHelpInvocationContext, org.eclipse.cdt.ui.ICHelpBook[], java.lang.String)
	 */
	public IFunctionSummary[] getMatchingFunctions(
			ICHelpInvocationContext context, ICHelpBook[] helpBooks,
			String prefix) {
		Assert.assertTrue("getMatchingFunctions is called before completion contributor gets initialized",fIsInitialized);
		return CHelpProviderTester.getDefault().generateMatchingFunctions(helpBooks,prefix,fProviderID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getHelpResources(org.eclipse.cdt.ui.text.ICHelpInvocationContext, org.eclipse.cdt.ui.ICHelpBook[], java.lang.String)
	 */
	public ICHelpResourceDescriptor[] getHelpResources(
			ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name) {
		Assert.assertTrue("getHelpResources is called before completion contributor gets initialized",fIsInitialized);
		return CHelpProviderTester.getDefault().generateHelpResources(helpBooks,name,fProviderID);
	}
}
