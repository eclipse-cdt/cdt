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
 * A variation of the Eclipse-supplied ComboBoxPropertyDescriptor for
 * displaying properties are a list. This list will be different for each
 * selected object, hence we need the ability to change that list as each
 * object is selected.
 */
public class SystemComboBoxPropertyDescriptor
       extends PropertyDescriptor 
{
    private SystemComboBoxCellEditor editor;
	    
	/**
	 * The list of possible values to display in the combo box
	 */
	protected String[] values;
    /**
     * Creates an property descriptor with the given id, display name, and list
     * of value labels to display in the combo box cell editor.
     * 
     * @param id the id of the property
     * @param displayName the name to display for the property
     * @param valuesArray the list of possible values to display in the combo box
     */
    public SystemComboBoxPropertyDescriptor(Object id, String displayName, String[] valuesArray) 
    {
    	super(id, displayName);
    	values = valuesArray;
    }
    /**
     * Creates an property descriptor with the given id, display name, but no list.
     * You must call setValues.
     * 
     * @param id the id of the property
     * @param displayName the name to display for the property
     */
    public SystemComboBoxPropertyDescriptor(Object id, String displayName) 
    {
    	super(id, displayName);
    }    
    /**
     * The <code>ComboBoxPropertyDescriptor</code> implementation of this 
     * <code>IPropertyDescriptor</code> method creates and returns a new
     * <code>ComboBoxCellEditor</code>.
     * <p>
     * The editor is configured with the current validator if there is one.
     * </p>
     */
    public CellEditor createPropertyEditor(Composite parent) 
    {
    	//editor = new SystemComboBoxCellEditor(parent, false); // last parm = readonly
    	editor = new SystemComboBoxCellEditor(parent);
    	editor.setItems(values);
	    if (getValidator() != null)
		    editor.setValidator(getValidator());
	    return editor;
    }
    /**
     * Set the values to display in the list.
     */
    public void setValues(String[] values)
    {
    	this.values = values;
    	if (editor != null)
    	  editor.setItems(values);
    }
    
    
}