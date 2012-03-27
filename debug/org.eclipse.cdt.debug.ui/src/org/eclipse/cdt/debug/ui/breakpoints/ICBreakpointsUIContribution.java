/*******************************************************************************
 * Copyright (c) 2008, 2012 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.breakpoints;

import java.util.Map;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;


/**
 * Interface representing a contributed UI element for breakpoint properties 
 * dialogs.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICBreakpointsUIContribution {

    /**
     * Main element name of breakpoint labels extension element.
     * @since 7.2
     */
    public static final String BREAKPOINT_LABELS = "breakpointLabels"; //$NON-NLS-1$
    
    /**
     * Main element name of breakpoint editors extension element.
     * @since 7.2
     */
    public static final String BREAKPOINT_EDITORS = "breakpointEditors"; //$NON-NLS-1$

	/**
	 * Attribute id
	 * @return
	 */
	public String getId();
	
	/**
	 * Extenralizable label for this attribute id
	 * @return
	 */
	public String getLabel();
	
	/**
	 * Creates FieldEditor for given attribute or null if not needed
	 * @param name - property name, must be the same as breakpoint attribute
	 * @param labelText - text usually in front of field
	 * @param parent - parent composite
	 * @return ready to use FieldEditor
	 */
	public FieldEditor getFieldEditor(String name, String labelText, Composite parent);
	
	/**
	 * Returns the element name under which this attribute was added.  The value should either be
	 * "breakpointLabels" or "breakpointEditors".
	 * @return Main element name.
	 * @since 7.2
	 */
	public String getMainElement();
	
	/**
	 * Get raw field editor class name
	 * @return class name
	 */
	public String getFieldEditorClassName();
	/**
	 * Return list of possible values that attributes can take, of null of no restrictions
	 * @return
	 */
	public String[] getPossibleValues();
	
	/**
	 * Get label for given attribute value, externalizable string
	 * @param value
	 * @return
	 */
	public String getLabelForValue(String value);
	
	/**
	 * Get type of the attribute
	 * @return
	 */
	public String getType();
	
	/**
	 * Get marker type for which this attribute is created
	 * @return
	 */
    public String getMarkerType();
    
    /**
     * Get debug model id
     * @return
     */
    public String getDebugModelId();
    
    /**
     * Return true if setting for an attribute applicable for setting of other attributes provided by the map
     * @param map - contains pairs of attribute=value for other breakpoint attributes
     * @return
     */
    public boolean isApplicable(Map<String, Object> map);
}
