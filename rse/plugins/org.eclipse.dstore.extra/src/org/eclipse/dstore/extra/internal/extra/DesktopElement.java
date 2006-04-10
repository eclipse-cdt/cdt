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

package org.eclipse.dstore.extra.internal.extra;

import org.eclipse.jface.resource.*;

import java.util.*;


public class DesktopElement implements org.eclipse.ui.model.IWorkbenchAdapter
{

  
  private IDataElement _element;
  
  public DesktopElement (IDataElement e)
  {
    _element = e;    
  }

  public IDataElement toElement(Object object)
      {
        IDataElement element = null;
        if (object instanceof IDataElement)
        {
          element = (IDataElement)object;
        }        
        else
        {
          element = _element;
        }
        return element;
      }
  
  public Object[] getChildren(Object o) 
  {
    IDataElement element = toElement(o);
    
   
    List objs = element.getAssociated("contents");
    return objs.toArray();
  }

  public ImageDescriptor getImageDescriptor(Object object) 
  {
      return null;
  }

  public String getLabel(Object o) 
  {
    return (String)_element.getElementProperty("value");
  }

  public Object getParent(Object o) 
  {
      return null;
  }
  
  public static boolean matches(Class aClass)
  {
    return (aClass == org.eclipse.ui.model.IWorkbenchAdapter.class);    
  }

    public static Object getPlatformAdapter(Object obj, Class aClass)
    {
	return org.eclipse.core.runtime.Platform.getAdapterManager().getAdapter(obj, aClass);
    }  
}