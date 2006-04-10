/********************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and International Business Machines Corporation. All rights reserved.
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

import java.util.List;

public class DomainEvent 
{


  public static final int UNKNOWN= 0; 
  public static final int INSERT= 1;
  public static final int REMOVE= 2;
  public static final int STRUCTURE_CHANGE= 3;
  public static final int NON_STRUCTURE_CHANGE= 4;
  public static final int FILE_CHANGE=5;
  

  public static final int FIRST_CUSTOM_CHANGE= 10;
  public static final int LAST_CUSTOM_CHANGE= 255;
  
  public static final int MASK= 0xFF;
  public static final int REVEAL= 0x100;
  public static final int SELECT= 0x200;
  
  public static final int INSERT_REVEAL= INSERT | REVEAL;
  public static final int INSERT_REVEAL_SELECT= INSERT_REVEAL | SELECT;
  
  private IDataElement _parent;
  private int    _type;
  
  public DomainEvent(int type, IDataElement parent, Object property) 
      {
	_type = type;	
        _parent = parent;
      }

  public DomainEvent(int type, IDataElement parent, Object property, IDataElement child) 
      {
	_type = type;	
        _parent = parent;
      }


  public DomainEvent(IDomainNotifier source, int type, IDataElement parent, Object property) 
      {
	_type = type;	
        _parent = parent;
      }


  public boolean equals(Object event)
  {
      return (((DomainEvent)event).getParent() == getParent());    
  }

    public String getId()
    {
	return _parent.getId();
    }
  
  public String getName()
  {
    return _parent.getName();
    
  }
  
  public int getType()
  {
    return _type;    
  }
  
  public IDataElement getParent()
      {
        return _parent;
      }

  public List getChildren()
      {
        return _parent.getNestedData();
      } 

  public int getChildrenCount()
      {
        return _parent.getNestedSize();
      }
}