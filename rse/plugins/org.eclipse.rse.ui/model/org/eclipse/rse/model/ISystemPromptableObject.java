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

package org.eclipse.rse.model;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

/**
 * This interface captures special-case objects in the SystemView that are only there to
 *  prompt the user to create something new. Eg "New Connection..." which when selected
 *  launches the new connection wizard. 
 * <p>
 * These promptables can either run when expanded, or they can show child promptable
 * objects
 * <p>
 * Related adapter is org.eclipse.rse.ui.view.SystemViewPromptableAdapter
 */
public interface ISystemPromptableObject extends IAdaptable
{

    /**
     * Get the parent object (within tree view)
     */
    public Object getParent();
    /**
     * Set the parent object so that we can respond to getParent requests
     */
    public void setParent(Object parent);
	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * Calls getImage on the subsystem's owning factory.
	 */
	public ImageDescriptor getImageDescriptor();
	/**
	 * Return the label for this object
	 */
	public String getText();
	/**
	 * Return the type label for this object
	 */
	public String getType();
	
	/**
	 * Run this prompt. This should return an appropriate ISystemMessageObject to show
	 *  as the child, reflecting if it ran successfully, was cancelled or failed.
	 */
	public Object[] run(Shell shell);	
	
	/**
	 * Return the child promptable objects.
	 * If this returns null, then SystemViewPromptableAdapter will subsequently
	 * call {@link #run(Shell)}.
	 */
	public ISystemPromptableObject[] getChildren();
	/**
	 * Return true if this is an expandable prompt
	 */
	public boolean hasChildren();
}