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

package org.eclipse.rse.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class InheritControlLayout extends Layout 
{
    private Point iExtent; // the cached size

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean changed) 
    {
	   Control [] children = composite.getChildren();
	   if (changed || (iExtent == null) )
		//iExtent = children[0].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		iExtent = children[0].computeSize(wHint, hHint, true);		
	   return new Point(iExtent.x, iExtent.y);
    }
    protected void layout(Composite composite, boolean changed) 
    {
	   Control [] children = composite.getChildren();
	   if (changed || (iExtent == null) ) 
		 iExtent = children[0].computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	   children[0].setBounds(0, 0, iExtent.x, iExtent.y);
	}
}