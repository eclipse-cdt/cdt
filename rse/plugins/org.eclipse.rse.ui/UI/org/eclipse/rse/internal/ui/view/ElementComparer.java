/********************************************************************************
* Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
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
* Kevin Doyle (IBM) - [195537] Move ElementComparer From SystemView to Separate File
 * Martin Oberhuber (Wind River) - [215820] Move SystemRegistry implementation to Core
********************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.core.model.SystemRegistry;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;

public class ElementComparer implements IElementComparer 
{

	public boolean equals(Object a, Object b) 
    {
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		if (registry instanceof SystemRegistry)
		{
			return ((SystemRegistry) registry).isSameObjectByAbsoluteName(a, null, b, null);
		}
		return false;
    }
    
    public int hashCode(Object element) 
    {
        ISystemViewElementAdapter ident=null;
        if(element instanceof IAdaptable) {
            ident = (ISystemViewElementAdapter)
                ((IAdaptable)element).getAdapter(ISystemViewElementAdapter.class);
            if(ident!=null) {
                String absName = ident.getAbsoluteName(element);
                if(absName!=null) return absName.hashCode();
                //Since one adapter is typically used for many elements in RSE,
                //performance would be better if the original Element's hashCode
                //were used rather than the adapter's hashCode. The problem with
                //this is, that if the remote object changes, it cannot be 
                //identified any more.
                //Note that even if the SAME object is modified during refresh
                //(so object a==b), the hashCode of the object can change 
                //over time if properties are modified. Therefore, play it
                //safe and return the adapter's hashCode which won't ever change.
                return ident.hashCode();
            }
        }		          
        if (element != null) { // adding check because I hit a null exception here once at startup
      	  return element.hashCode();
        } else {
      	  //System.out.println("null element");
      	  return 0;
        }
    }
    
}
 
