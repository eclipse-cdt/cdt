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

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
/**
 * Dummy action representing a separator in menus.
 */
public class SystemSeparatorAction extends SystemBaseAction 
{
	private boolean realAction;
	
	/**
	 * Constructor for SystemSeparatorAction when you intend to subclass
	 */
	public SystemSeparatorAction(Shell parent) 
	{
		super("_separator_",(ImageDescriptor)null,parent);
		realAction = true;
	}
	/**
	 * Constructor for SystemSeparatorAction when you just want the separator
	 */
	public SystemSeparatorAction() 
	{
		super("_separator_",(ImageDescriptor)null,null);
		realAction = false;
	}

    public Separator getSeparator()
    {
    	return new Separator();
    }
    
    /**
     * Return true if this is both a separator and a real action, false if this is only
     *  a separator
     */
    public boolean isRealAction()
    {
    	return realAction;
    }
}