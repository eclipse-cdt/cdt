/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core.model;


/**
 * Provides common support for local RSE model objects
 * Extenders inherit property set support
 * @author dmcknigh
 *
 */
public abstract class RSEModelObject extends PropertySetContainer implements IRSEModelObject
{
	protected boolean _isDirty = true;
	protected boolean _wasRestored = false;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.persistance.IRSEPersistableContainer#isDirty()
	 */
	public final boolean isDirty()
	{
		return _isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.persistance.IRSEPersistableContainer#setDirty(boolean)
	 */
	public void setDirty(boolean flag)
	{
		_isDirty = flag;
	}
	
	


	public final boolean wasRestored() 
	{
		return _wasRestored;
	}

	public final void setWasRestored(boolean flag) 
	{
		_wasRestored = flag;
	}
	
	public String getDescription()
	{
		return RSEModelResources.RESID_MODELOBJECTS_MODELOBJECT_DESCRIPTION;
	}
	
	/**
	 * Does a null-aware string comparison. Two strings that are
	 * <code>null</code> will compare equal. Otherwise the result is 
	 * the same as s1.equals(s2), if s1 is not null.
	 * @param s1 The first string to compare
	 * @param s2 the second string
	 * @return true if the strings are equal or both null.
	 */
	protected boolean compareStrings(String s1, String s2) {
		boolean result = false;
		if (s1 == null) {
			result = s1 == null;
		} else {
			result = s1.equals(s2);
		}
		return result;
	}
}