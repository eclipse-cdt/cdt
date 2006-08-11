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

package org.eclipse.rse.references;

import org.eclipse.rse.core.references.IRSEBasePersistableReferencedObject;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;

/**
 * Referencing objects are shadows of real objects. Typically, shadows are created
 * to enable a GUI which does not allow the same real object to appear multiple times.
 * In these cases, a unique shadow object is created for each unique instance of the
 * real object.
 * <p>
 * The parent interface ISystemReferencingObject captures the simple set of methods 
 * such a shadow must implement.
 * <p>
 * This interface specializes that for the case of references that must be persisted.
 * Typically, we build the references in memory at runtime just to satisfy the GUI.
 * However, occassionally we build the list of references for a more permanent reason,
 * such as when we let a user choose a subset from a master list.
 * <p>
 * When we persist such a reference, we can't persist the memory reference to the master
 * object. Instead, we persist the unique name or key of that object, and upon restoring
 * from disk we then resolve that into a runtime reference to a real memory object.
 * <p>
 * This interface captures the methods to set and query that name or key.
 */
public interface ISystemBasePersistableReferencingObject
	   extends IRSEBaseReferencingObject 
{
	/**
	 * Set the object to which we reference. This is an overload of the parent
	 * interface method of the same name. This one takes an object of which we
	 * can query its unique name for the purpose of saving that to disk.
	 */
	public void setReferencedObject(IRSEBasePersistableReferencedObject obj);	
    /**
     * Query the unique name or key of the object we are referencing.
     */
    public String getReferencedObjectName();
}