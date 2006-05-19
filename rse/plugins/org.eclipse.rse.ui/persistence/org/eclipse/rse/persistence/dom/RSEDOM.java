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

package org.eclipse.rse.persistence.dom;

import org.eclipse.rse.model.ISystemProfile;

/**
 * This class is the root node of an RSE DOM.  Each
 * RSEDOM represents the properties of a profile to persist.
 */
public class RSEDOM extends RSEDOMNode
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean _saveScheduled = false;
	private transient ISystemProfile _profile;
	
	public RSEDOM(ISystemProfile profile)
	{
		super(null, TYPE_PROFILE, profile.getName());
		_profile = profile;
	}
	
	public RSEDOM(String profileName)
	{
		super(null, TYPE_PROFILE, profileName);
		_profile = null;
	}
	
	public ISystemProfile getProfile()
	{
		return _profile;
	}
	
	/**
	 * Indicate that this DOM needs to be saved
	 */
	public void markForSave()
	{
		if (!restoring && !_needsSave)
		{
			System.out.println("RSEDOM "+getName() + " needs saving");
			_needsSave = true;
		}
	}
	
	/**
	 * Indicate that this DOM has been saved
	 *
	 */
	public void markUpdated()
	{
		if (_needsSave)
		{
			System.out.println("RSEDOM "+getName() + " is up to date");

			_needsSave = false;
			_saveScheduled = false;
			super.markUpdated();
		}
	}
	
	/**
	 * Returns whether this DOM is scheduled to be saved
	 * @return
	 */
	public boolean saveScheduled()
	{
		return _saveScheduled;
	}
	
	/**
	 * Indicate that this DOM is scheduled to be saved
	 */
	public void markSaveScheduled()
	{
		if (!_saveScheduled)
		{
			_saveScheduled = true;
		}
	}
	
	
	/**
	 * Has the DOM changed since last update?
	 */
	public boolean needsSave()
	{
		return _needsSave;
	}

	public void print(RSEDOMNode node, String indent)
	{
		String type = node.getType();
		String name = node.getName();
		RSEDOMNodeAttribute[] attributes = node.getAttributes();
		RSEDOMNode[] children = node.getChildren();
		
		System.out.println(indent + "RSEDOMNode " + type);
		System.out.println(indent + "{");
		String sindent = indent + "  ";

		System.out.println(sindent + "name=" + name);
		for (int i = 0; i < attributes.length; i++)
		{
			RSEDOMNodeAttribute attribute = attributes[i];
			String key = attribute.getKey();
			String value = attribute.getValue();
			System.out.println(sindent + key + "=" + value);
		}
		
		String cindent = sindent + "    ";
		for (int c = 0; c < children.length; c++)
		{
			RSEDOMNode child = children[c];
			print(child, cindent);
		}
		System.out.println(indent + "}");
	}

}