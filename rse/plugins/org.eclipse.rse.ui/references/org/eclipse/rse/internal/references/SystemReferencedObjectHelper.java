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

package org.eclipse.rse.internal.references;
import java.util.Vector;

import org.eclipse.rse.core.references.IRSEBaseReferencedObject;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;


/**
 * This is a class that implements all the methods in the IRSEReferencedObject.
 * It makes implementing this interface trivial.
 * The easiest use of this class is to subclass it, but since that is not
 * always possible, it is not abstract and hence can be leveraged via containment.
 */
public class SystemReferencedObjectHelper implements IRSEBaseReferencedObject 
{
	private Vector referencingObjects = new Vector();
	/**
	 * Constructor for SystemReferencedObjectHelper
	 */
	public SystemReferencedObjectHelper() 
	{
		super();
	}

	/**
	 * @see IRSEBaseReferencedObject#addReference(IRSEBaseReferencingObject)
	 */
	public int addReference(IRSEBaseReferencingObject ref) 
	{
		referencingObjects.addElement(ref);
		return referencingObjects.size();
	}

	/**
	 * @see IRSEBaseReferencedObject#removeReference(IRSEBaseReferencingObject)
	 */
	public int removeReference(IRSEBaseReferencingObject ref) 
	{
		int before = referencingObjects.size();
		referencingObjects.removeElement(ref);
		int after = referencingObjects.size();
		assertThis((after == (before - 1)), "removeReference failed for "+ref);
		return referencingObjects.size();
	}

	/**
	 * @see IRSEBaseReferencedObject#getReferenceCount()
	 */
	public int getReferenceCount() 
	{
		return referencingObjects.size();
	}

	/**
	 * Clear the list of referenced objects.
	 */
	public void removeAllReferences()
	{
		referencingObjects.removeAllElements();
	}

	/**
	 * @see IRSEBaseReferencedObject#getReferencingObjects()
	 */
	public IRSEBaseReferencingObject[] getReferencingObjects() 
	{
		IRSEBaseReferencingObject[] references = new IRSEBaseReferencingObject[referencingObjects.size()];
		for (int idx=0; idx<referencingObjects.size(); idx++)
		   references[idx] = (IRSEBaseReferencingObject)referencingObjects.elementAt(idx);
		return references;
	}

    /**
     * Little assertion method for debugging purposes
     */
    protected void assertThis(boolean assertion, String msg)
    {
    	if (!assertion)
    	  System.out.println("ASSERTION FAILED IN SystemReferencedObject: " + msg);
    }
    
}