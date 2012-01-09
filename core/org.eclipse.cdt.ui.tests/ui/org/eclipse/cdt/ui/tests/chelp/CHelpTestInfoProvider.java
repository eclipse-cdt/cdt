/**********************************************************************
 * Copyright (c) 2004, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - Initial API and implementation
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
	
	/**
	 * Flag indicating whether this help provider should provide help info.
	 * Should be set to <code>true</code> during tests only.
	 */
	static boolean fgEnabled= false;

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
	@Override
	public void initialize() {
		Assert.assertFalse("initialize is called several times",fIsInitialized);
		fIsInitialized = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getCHelpBooks()
	 */
	@Override
	public ICHelpBook[] getCHelpBooks() {
		if (!fgEnabled) {
			return new ICHelpBook[0];
		}
		Assert.assertTrue("getCHelpBooks is called before completion contributor gets initialized",fIsInitialized);
		return fCHelpBooks;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getFunctionInfo(org.eclipse.cdt.ui.text.ICHelpInvocationContext, org.eclipse.cdt.ui.ICHelpBook[], java.lang.String)
	 */
	@Override
	public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context,
			ICHelpBook[] helpBooks, String name) {
		if (!fgEnabled) {
			return null;
		}
		Assert.assertTrue("getFunctionInfo is called before completion contributor gets initialized",fIsInitialized);
		return CHelpProviderTester.getDefault().generateFunctionInfo(helpBooks,name,fProviderID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getMatchingFunctions(org.eclipse.cdt.ui.text.ICHelpInvocationContext, org.eclipse.cdt.ui.ICHelpBook[], java.lang.String)
	 */
	@Override
	public IFunctionSummary[] getMatchingFunctions(
			ICHelpInvocationContext context, ICHelpBook[] helpBooks,
			String prefix) {
		if (!fgEnabled) {
			return new IFunctionSummary[0];
		}
		Assert.assertTrue("getMatchingFunctions is called before completion contributor gets initialized",fIsInitialized);
        return null; // TODO returning null until someone puts in a preference to control it.
        //return CHelpProviderTester.getDefault().generateMatchingFunctions(helpBooks,prefix,fProviderID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getHelpResources(org.eclipse.cdt.ui.text.ICHelpInvocationContext, org.eclipse.cdt.ui.ICHelpBook[], java.lang.String)
	 */
	@Override
	public ICHelpResourceDescriptor[] getHelpResources(
			ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name) {
		if (!fgEnabled) {
			return new ICHelpResourceDescriptor[0];
		}
		Assert.assertTrue("getHelpResources is called before completion contributor gets initialized",fIsInitialized);
		return CHelpProviderTester.getDefault().generateHelpResources(helpBooks,name,fProviderID);
	}
}
