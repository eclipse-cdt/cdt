/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.filters;
//import org.eclipse.rse.core.*;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;



/**
 * A class capturing the attributes commonly needed by dialogs that
 * work with filter pools.
 */
public class SystemFilterPoolDialogInputs extends SystemFilterDialogInputs
{


    public ISystemFilterPoolManagerProvider poolManagerProvider = null;
	public ISystemFilterPoolManager[] poolManagers = null;
	public ISystemFilterPoolReferenceManager refManager = null;
	public int mgrSelection = 0;
	public String poolNamePrompt;	
	public String poolNameTip;	
	public String poolMgrNamePrompt;
	public String poolMgrNameTip;	
	
    public SystemSimpleContentElement filterPoolTreeRoot;
}