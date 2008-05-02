/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM) - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 *******************************************************************************/

package org.eclipse.dstore.core.model;

/**
 * DE is a container of <code>DataElement</code> constants.  These constants
 * are used to identify <code>DataElement</code> attributes.
 * 
 * <li>
 * Attributes beginning with "P_" indicate <I>property</I> attribute identifiers.
 * </li>
 * <li>
 * Attributes beginning with "T_" indicate <code>DataElement</code> <I>type</I> attributes.
 * </li>
 * <li>
 * Attributes beginning with "A_" indicate <code>DataElement</code> indexs into <I>attributes</I>.
 * </li>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DE 	     
{


    /*
     * The nested data (children) property identifier of a <code>DataElement</code>.
     */
    public static final String P_CHILDREN = "children"; //$NON-NLS-1$

    /*
     * The image property identifier of a <code>DataElement</code>.  This is the same
     * as the value property identifier
     */
    public static final String P_LABEL = "label"; //$NON-NLS-1$

    /*
     * The notifier property identifier of a <code>DataElement</code>.
     */
    public static final String P_NOTIFIER = "notifier"; //$NON-NLS-1$

    /*
     * The <code>DataStore</code> property identifier of a <code>DataElement</code>.
     */
    public static final String P_DATASTORE       = "dataStore"; //$NON-NLS-1$

    /*
     * The source name property identifier of a <code>DataElement</code>.  This is the
     * name of a source location if one exists.
     */
    public static final String P_SOURCE_NAME     = "source"; //$NON-NLS-1$

    /*
     * The source file property identifier of a <code>DataElement</code>.
     */
    public static final String P_SOURCE          = "sourcefile"; //$NON-NLS-1$

    /*
     * The source location property identifier of a <code>DataElement</code>.
     */
    public static final String P_SOURCE_LOCATION = "sourcelocation"; //$NON-NLS-1$

    public static final String P_SOURCE_LOCATION_COLUMN = "sourcelocationcolumn"; //$NON-NLS-1$

    /*
     * The nested data (children) property identifier of a <code>DataElement</code>.  Same as <code>P_CHILDREN</code>.
     */
    public static final String P_NESTED          = "nested"; //$NON-NLS-1$

    /*
     * The buffer property identifier of a <code>DataElement</code>.  
     */
    public static final String P_BUFFER          = "buffer"; //$NON-NLS-1$

    /*
     * The type property identifier of a <code>DataElement</code>.  
     */
    public static final String P_TYPE            = "type"; //$NON-NLS-1$

    /*
     * The id property identifier of a <code>DataElement</code>.  
     */
    public static final String P_ID              = "id"; //$NON-NLS-1$

    /*
     * The name property identifier of a <code>DataElement</code>.  
     */
    public static final String P_NAME            = "name"; //$NON-NLS-1$

    /*
     * The value property identifier of a <code>DataElement</code>.  
     */
    public static final String P_VALUE           = "value"; //$NON-NLS-1$

    /*
     * The <I>is reference?</I> property identifier of a <code>DataElement</code>. Deprecated. Use P_REF_TYPE.  
     */
    public static final String P_ISREF           = "isRef"; //$NON-NLS-1$
    
    /*
     * The <I>is reference?</I> property identifier of a <code>DataElement</code>.  
     */
    public static final String P_REF_TYPE           = "refType"; //$NON-NLS-1$

    /*
     * The visibility property identifier of a <code>DataElement</code>.  
     */
    public static final String P_DEPTH           = "depth"; //$NON-NLS-1$

    /*
     * The attributes property identifier of a <code>DataElement</code>.  
     */
    public static final String P_ATTRIBUTES      = "attribute"; //$NON-NLS-1$

    /*
     * The file property identifier of a <code>DataElement</code>.  
     */
    public static final String P_FILE            = "file"; //$NON-NLS-1$

    /*
     * The file property identifier of a <code>DataElement</code>.  
     */
    public static final String P_DESCRIPTOR      = "descriptor"; //$NON-NLS-1$

    /*
     * Reference type.  
     */
    public static final String T_REFERENCE          = "reference"; //$NON-NLS-1$

    /*
     * Command type.  
     */
    public static final String T_COMMAND            = "command"; //$NON-NLS-1$

    /*
     * UI Command Descriptor type.  
     */
    public static final String T_UI_COMMAND_DESCRIPTOR  = "ui_commanddescriptor"; //$NON-NLS-1$

    /*
     * Object Descriptor type.  
     */
    public static final String T_OBJECT_DESCRIPTOR  = "objectdescriptor"; //$NON-NLS-1$

    /*
     * Command Descriptor type.  
     */
    public static final String T_COMMAND_DESCRIPTOR = "commanddescriptor"; //$NON-NLS-1$

    /*
     * Relation Descriptor type.  
     */
    public static final String T_RELATION_DESCRIPTOR = "relationdescriptor"; //$NON-NLS-1$

    /*
     * Abstract Object Descriptor type.  
     */
    public static final String T_ABSTRACT_OBJECT_DESCRIPTOR = "abstractobjectdescriptor"; //$NON-NLS-1$

    /*
     * Abstract Command Descriptor type.  
     */
    public static final String T_ABSTRACT_COMMAND_DESCRIPTOR = "abstractcommanddescriptor"; //$NON-NLS-1$

    /*
     * Abstract Relation Descriptor type.  
     */
    public static final String T_ABSTRACT_RELATION_DESCRIPTOR = "abstractrelationdescriptor"; //$NON-NLS-1$


    /*
     * Type attribute index.  
     */
    public static final int    A_TYPE       = 0;

    /*
     * ID attribute index.  
     */
    public static final int    A_ID         = 1;

    /*
     * Name attribute index.  
     */
    public static final int    A_NAME       = 2;

    /*
     * Value attribute index.  
     */
    public static final int    A_VALUE      = 3;

    /*
     * Source attribute index.  
     */
    public static final int    A_SOURCE     = 4;
    
    /*
     * Source location attribute index.   
     */
    public static final int    A_SOURCE_LOCATION     = 5;

    /*
     * IsRef attribute index. Deprecated. Use A_REF_TYPE. 
     */
    public static final int    A_ISREF      = 6;
    
    /*
     * RefType attribute index.  
     */
    public static final int    A_REF_TYPE   = 6;

    /*
     * Visibility attribute index.  
     */
    public static final int    A_DEPTH      = 7;

    /*
     * Size attribute index.  
     */
    public static final int    A_SIZE       = 8;
    
    public static final  String ENCODING_UTF_8 = "UTF-8"; //$NON-NLS-1$
}
