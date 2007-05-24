/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.dstore.internal.extra;

import org.eclipse.dstore.extra.DomainEvent;
import org.eclipse.dstore.extra.IDomainListener;
import org.eclipse.dstore.extra.IDomainNotifier;

public class DomainNotifier implements IDomainNotifier 
{


    public DomainNotifier()
      {
      }
  
    public void enable(boolean on)
    { 
    }

    public boolean isEnabled()
    {
        return false;
    }

  public void addDomainListener(IDomainListener listener)
      {
      }


  public void fireDomainChanged(DomainEvent event)
      {
      }	

  public boolean hasDomainListener(IDomainListener listener)
      {
	  return false;
      }

  public void removeDomainListener(IDomainListener listener)
      {
      }
}
