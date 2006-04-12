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

package org.eclipse.rse.internal.subsystems.shells.subsystems;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.subsystems.shells.core.subsystems.ICandidateCommand;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/** 
 * class represents a candidate command to run
 */ 
public class CandidateCommand implements IAdaptable, ICandidateCommand
{

	protected String _name;
	protected String _type;
	protected String _description;
	protected String _path;
	protected ImageDescriptor _imageDescriptor; 

	/**
	 * Constructor
	 * 
	 * @param type indicates the type of the candidate command
	 * @param name indicates the name of the candidate command
	 * @param description describes the candidate command
	 * @param path indicates the path of the candidate command if one exists
	 */
	public CandidateCommand(String type, String name, String description, String path)
	{
		_name = name;
		_type = type;
		_description = description;
		_path = path;
	}

	/**
	 * Gets the name of the candidate command
	 * @return the name of the candidate command
	 */
	public String getName()
	{
		return _name;	
	}

	/**
	 * Gets the type of the candidate command. 
	 * @return the type of the command
	 */
	public String getType()
	{
		return _type;
	}
	
	/**
	 * Gets the path of a candidate command.
	 * @return the path to the command, if one exists
	 */
	public String getPath()
	{
		return _path;
	}

	/**
	 * Gets the description for a candidate command.
	 * @return the description of the command
	 */
	public String getDescription()
	{
		return _description;
	}
	
	/**
	 * Gets the image descriptor to display for a candidate command
	 * @return the image descriptor for the command
	 */
	public ImageDescriptor getImageDescriptor()
	{
	    if (_imageDescriptor == null)
	    {
	        _imageDescriptor = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
	        //_imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemConstants.ICON_SYSTEM_RUN_ID);	    
	    }
	    return _imageDescriptor;
	}
	
	
	public Object getAdapter(Class adapterType)
	{
			return Platform.getAdapterManager().getAdapter(this, adapterType);
	}
}