/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * This interface represents an inputOrder instance in the managed build system.
 * This element is only present if the user or a tool integrator needs to define
 * the specific order of input files to a tool, or needs to exclude one or more
 * input files from being used by a tool.  An inputType element can have 
 * multiple inputOrder children.
 * 
 * @since 3.0
 */
public interface IInputOrder {
	public static final String INPUT_ORDER_ELEMENT_NAME = "inputOrder";	//$NON-NLS-1$
	public static final String PATH = "path";	//$NON-NLS-1$
	public static final String ORDER = "order";	//$NON-NLS-1$
	public static final String EXCLUDED = "excluded";	//$NON-NLS-1$

	/**
	 * Returns the InputType parent of this InputOrder.
	 * 
	 * @return IInputType
	 */
	public IInputType getParent();

	/**
	 * Returns the relative or absolute path of the resource to which this element applies.
	 * The resource must be a member of the project, or the output from another tool in the 
	 * tool-chain.
	 * 
	 * @return String
	 */
	public String getPath();

	/**
	 * Sets the relative or absolute path of the resource to which this element applies.
	 * 
	 * @param path
	 */
	public void setPath(String path);
	
	/**
	 * Returns a comma-separated list of integer values that specify the order of this resource.  
	 * In most cases, only a single integer value will be specified.  A list is supported 
	 * for the case where a single input file needs to be specified multiple times on the 
	 * command line.  The order numbers begin at 1.  Not all values need to be specified.  
	 * Unordered resources will fill the first “gap”.  For example:
	 *   •	To specify the first input file, use 1.
	 *   •	To specify the last input file, without specifying an order for any other input file, use 2.
	 *   •	To specify only the first two input files and last input file, use 1, 2 & 4.
	 * 
	 * @return String
	 */
	public String getOrder();
	
	/**
	 * Sets the comma-separated list of integer values that specify the order of this resource.
	 * 
	 * @param order
	 */
	public void setOrder(String order);

	/**
	 * Returns <code>true</code> if this resource is not used as an input for the tool.
	 * 
	 * @return boolean 
	 */
	public boolean getExcluded();

	/**
	 * Sets whether this resource is not used as an input for the tool.
	 * 
	 * @param excluded
	 */
	public void setExcluded(boolean excluded);

	/**
	 * Returns <code>true</code> if this element has changes that need to 
	 * be saved in the project file, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isDirty();
	
	/**
	 * Sets the element's "dirty" (have I been modified?) flag.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);
	
}
