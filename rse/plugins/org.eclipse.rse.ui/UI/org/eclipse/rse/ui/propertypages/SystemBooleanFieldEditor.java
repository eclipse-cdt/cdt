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

package org.eclipse.rse.ui.propertypages;
import java.util.ResourceBundle;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Thin subclass so we can support setToolTipText!!
 */
public class SystemBooleanFieldEditor extends BooleanFieldEditor 
{
    private Button button;
    private String tip;
    
	/**
	 * Constructor for SystemBooleanFieldEditor
	 */
	protected SystemBooleanFieldEditor() 
	{
		super();
	}

	/**
	 * Constructor for SystemBooleanFieldEditor
	 * @param name the preference-store-key of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param style the style, either <code>DEFAULT</code> or
	 *   <code>SEPARATE_LABEL</code>
	 * @param parent the parent of the field editor's control
	 * @see #DEFAULT
	 * @see #SEPARATE_LABEL
	 */
	public SystemBooleanFieldEditor(String name, String labelText, int style, Composite parent) 
	{
		super(name, labelText, style, parent);
	}

	/**
	 * Constructor for SystemBooleanFieldEditor, using DEFAULT for the style
	 * @param name the preference-store-key of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 */
	public SystemBooleanFieldEditor(String name, String labelText, Composite parent) 
	{
		super(name, labelText, parent);
	}
	/**
	 * Constructor for SystemBooleanFieldEditor, using DEFAULT for the style, and
	 *  specifying a resource bundle and key from which the label (_LABEL and
	 *  tooltip text (_TOOLTIP are retrieved.
	 * @param name the preference-store-key of the preference this field editor works on
	 * @param rb the ResourceBundle we will query the label and tooltip from
	 * @param labelKey the resource bundle key from which we get the label (_LABEL and tooltip (_TOOLTIP
	 * @param parent the parent of the field editor's control
	 */
	public SystemBooleanFieldEditor(String name, ResourceBundle rb, String labelKey, Composite parent) 
	{
		super(name, rb.getString(labelKey+"label"), parent);
		setToolTipText(rb.getString(labelKey+"tooltip"));
	}	
	
	/**
	 * Returns the change button for this field editor.
	 * This is an override of our parent's method because this is the
	 *  only way for us to gain access to the checkbox so that we can
	 *  apply our tooltip text.
	 */
    protected Button getChangeControl(Composite parent) 
    {
    	button = super.getChangeControl(parent);
    	if (tip != null)
    	  button.setToolTipText(tip);
    	return button;	
    }
	/**
	 * Set the tooltip text
	 */
	public void setToolTipText(String tip)
	{
		if (button != null)
		  button.setToolTipText(tip);
		this.tip = tip;
	}
	/**
	 * Get the tooltip text
	 */
    public String getToolTipText()
    {
    	return tip;
    }
}