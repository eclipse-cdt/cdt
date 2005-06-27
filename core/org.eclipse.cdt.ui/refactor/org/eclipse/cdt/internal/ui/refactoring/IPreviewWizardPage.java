/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.cdt.internal.corext.refactoring.base.IChange;

public interface IPreviewWizardPage extends IWizardPage {

	/** The page's name */
	public static final String PAGE_NAME= "PreviewPage"; //$NON-NLS-1$
	
	/**
	 * Sets that change for which the page is supposed to display a preview.
	 * 
	 * @param change the new change.
	 */
	public void setChange(IChange change);	
}

