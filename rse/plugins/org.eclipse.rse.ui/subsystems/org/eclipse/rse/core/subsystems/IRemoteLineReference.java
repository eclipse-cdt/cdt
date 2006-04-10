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

package org.eclipse.rse.core.subsystems;
/**
 * This interface represents an object that can be used to jump into line in source
 */
public interface IRemoteLineReference
{


	/**
	 * Gets the path to the file that this output references if it references any.  It may return null if
	 * 	no such association exists.  This may be used to jump to an editor from a view which displays 
	 * 	this
	 * 
	 * @return the path of the referenced file if there is one
	 */
	public String getAbsolutePath();
	
	/**
	 * Gets the line number within a file that this references if it references any.  By default
	 * 	it should return 0.  If no file association exists, it also returns 0.  This may be used to jump into
	 * 	a location within an editor from a view which displays remote output. 
	 * 
	 * @return the line number within a referenced file if there is one.
	 */
	public int getLine();
	
	/**
	 * Get the start offset in a line corresponding to this reference.  -1 indicates there is no offset
	 * @return the offset
	 */
	public int getCharStart();
	
	/**
	 * Get the end offset in a line corresponding to this reference.  -1 indicates there is no offset
	  * @return the offset
	 */
	public int getCharEnd();
	
	/**
	 * Get the object that contains this object. 
	 * 
	 * @return the parent object
	 */
	public Object getParent();
	
}