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

package org.eclipse.rse.ui.view.team;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewInputProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;


/**
 * Base label provider for System Team View part
 */
public class SystemTeamViewLabelProvider extends LabelProvider 
{

	public static final String Copyright =
		"(C) Copyright IBM Corp. 2002, 2003.  All Rights Reserved.";

	// Used to grab Workbench standard icons.
	private WorkbenchLabelProvider aWorkbenchLabelProvider = new WorkbenchLabelProvider();	
	private Viewer viewer;
	/**
	 * The cache of images that have been dispensed by this provider.
	 * Maps ImageDescriptor->Image.
	 */
	private Map imageTable = new Hashtable(40);	
	
	/**
	 * Constructor
	 */
	public SystemTeamViewLabelProvider(Viewer viewer) 
	{
		super();
		this.viewer = viewer;
	}
	/**
	 * Get the image to display
	 */
	public Image getImage(Object element) 
	{
		Image image = null;

		if (element instanceof ISystemProfile) 
		{
			ISystemProfile profile = (ISystemProfile)element;
			if (SystemPlugin.getTheSystemRegistry().getSystemProfileManager().isSystemProfileActive(profile.getName()))
			  return SystemPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_PROFILE_ACTIVE_ID);
			else
			  return SystemPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_PROFILE_ID);
		}

		// If we have a project, return the resource project images. 
		else if (element instanceof IProject) 
		{
			Image projectImage = aWorkbenchLabelProvider.getImage((IProject)element);
			return projectImage;
		}
						
		// User system view element adapter 
		ISystemViewElementAdapter adapter = getSystemViewAdapter(element);
		if (adapter != null)
		{
			//return adapter.getImage(element);
			ImageDescriptor descriptor = adapter.getImageDescriptor(element);
			if (descriptor != null)
			{
				return getImageFromImageDescriptor(descriptor);
			}
		}
			
		// use Workbench stuff.
		image = aWorkbenchLabelProvider.getImage(element);
		if (image != null) 
		{
			return image;
		}

		// all failed, use parent code.
		return super.getImage(element);
	}
	/**
	 * Turn image descriptor into image
	 */
	private Image getImageFromImageDescriptor(ImageDescriptor descriptor)
	{
		if (descriptor == null)
			return null;
		Image image = (Image) imageTable.get(descriptor);
		if (image == null) 
		{
		  image = descriptor.createImage();
		  imageTable.put(descriptor, image);
		}
		return image;				
		
	}

	/**
	 * Get the label to display
	 */
	public String getText(Object element) 
	{
		ISystemViewElementAdapter adapter = getSystemViewAdapter(element);
		if (adapter != null)
			return adapter.getText(element);
		
		// If we have a project, return the resource project images. 
		if (element instanceof IProject) 
		{
			return ((IProject)element).getName();
		}
		// use Workbench stuff. 
		String text = aWorkbenchLabelProvider.getText(element);
		if (text.length() > 0) 
			return text;

		// all failed, use parent code.
		return super.getText(element);
	}

	/**
	 * Dispose of images created here.<br>
	 */
	public void dispose() 
	{
		// The following we got from WorkbenchLabelProvider
		if (imageTable != null)
		{
		 	 Collection imageValues = imageTable.values();
		  	if (imageValues!=null)
		  	{
				Iterator images = imageValues.iterator();	    	
				if (images!=null)
			  		while (images.hasNext())
						((Image)images.next()).dispose();
				imageTable = null;	    
		  	}
		}
	}

	/**
	 * Returns the implementation of ISystemViewElement for the given
	 * object.  Returns null if the adapter is not defined or the
	 * object is not adaptable.
	 */
	protected ISystemViewElementAdapter getSystemViewAdapter(Object o) 
	{
		ISystemViewElementAdapter adapter = null;    	
		if (o == null)
		{
			SystemBasePlugin.logWarning("ERROR: null passed to getAdapter in SystemTeamViewLabelProvider");
			return null;    	  
		}
		if (!(o instanceof IAdaptable)) 
			adapter = (ISystemViewElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemViewElementAdapter.class);
		else
			adapter = (ISystemViewElementAdapter)((IAdaptable)o).getAdapter(ISystemViewElementAdapter.class);
		//if (adapter == null)
		//	SystemPlugin.logWarning("ADAPTER IS NULL FOR ELEMENT OF TYPE: " + o.getClass().getName());
		if ((adapter!=null) && (viewer != null))
		{    	
			Shell shell = null;
			if (viewer instanceof ISystemResourceChangeListener)
				shell = ((ISystemResourceChangeListener)viewer).getShell();
			else if (viewer != null)
				shell = viewer.getControl().getShell();
			if (shell != null)
				adapter.setShell(shell);
			adapter.setViewer(viewer);
			if (viewer.getInput() instanceof ISystemViewInputProvider)
			{
				ISystemViewInputProvider inputProvider = (ISystemViewInputProvider)viewer.getInput();
				adapter.setInput(inputProvider);
			}
		}
		else if (viewer == null)
			SystemBasePlugin.logWarning("VIEWER IS NULL FOR SystemTeamViewLabelProvider");    	
		return adapter;
	}
}