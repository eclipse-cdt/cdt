/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui;
/**
 * This is used in forms that are used within dialogs and pages, and 
 *   specifically with {@link org.eclipse.rse.ui.SystemBaseForm}.
 *   It allows the dialog or page to be called back when the form code calls
 *   setPageComplete, a method within the form class. This way the diaog or
 *   page can themselves call their own setPageComplete method.
 */
public interface ISystemPageCompleteListener
{
	/**
	 * The callback method. 
	 * This is called whenever setPageComplete is called by the form code.
	 * @see {@link SystemBaseForm#addPageCompleteListener(ISystemPageCompleteListener)} 
	 */
	public void setPageComplete(boolean complete);	
		
}