/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
/**
 * A variation of the Eclipse-supplied TextPropertyDescriptor for
 * displaying text string properties that are inheritable.
 */
public class SystemInheritableTextPropertyDescriptor
       extends PropertyDescriptor 
{
    private SystemInheritableTextCellEditor editor;        
	private String toggleButtonToolTipText, entryFieldToolTipText;
	private boolean editable = true;
    
    /**
     * Creates a property descriptor with the given id, display name
     * 
     * @param id the id of the property
     * @param displayName the name to display for the property
     */
    public SystemInheritableTextPropertyDescriptor(Object id, String displayName) 
    {
    	super(id, displayName);
    }
	/**
	 * Call this with false in special circumstances to user's disable ability to edit this value.
	 * Default is true
	 * @see #getEditable()
	 */
	public void setEditable(boolean allow)
	{
		editable = allow;
	}
	/**
	 * Query the allow-editing value. Default is true.
	 */
	public boolean getEditable()
	{
		return editable;
	}
    
    /**
     * Return an instance of SystemInheritableTextCellEditor, unless
     * our editable property is false, in which case we return null;
     */
    public CellEditor createPropertyEditor(Composite parent) 
    {
    	if (!editable)
    	  return null;
    	editor = new SystemInheritableTextCellEditor(parent);    	
	    if (getValidator() != null)
		  editor.setValidator(getValidator());
		if (toggleButtonToolTipText != null)
		  editor.setToggleButtonToolTipText(toggleButtonToolTipText);
		if (entryFieldToolTipText != null)
		  editor.setEntryFieldToolTipText(entryFieldToolTipText);
	    return editor;
    }
    
    
	/**
	 * Gets the toggleButtonToolTipText
	 * @return Returns a String
	 */
	public String getToggleButtonToolTipText() 
	{
		return toggleButtonToolTipText;
	}
	/**
	 * Sets the toggleButtonToolTipText
	 * @param toggleButtonToolTipText The toggleButtonToolTipText to set
	 */
	public void setToggleButtonToolTipText(String toggleButtonToolTipText)
	{
		this.toggleButtonToolTipText = toggleButtonToolTipText;
	}

	/**
	 * Gets the entryFieldToolTipText
	 * @return Returns a String
	 */
	public String getEntryFieldToolTipText() 
	{
		return entryFieldToolTipText;
	}
	/**
	 * Sets the entryFieldToolTipText
	 * @param entryFieldToolTipText The entryFieldToolTipText to set
	 */
	public void setEntryFieldToolTipText(String entryFieldToolTipText)
	{
		this.entryFieldToolTipText = entryFieldToolTipText;
	}
    
}