/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * ProcessParameter is responsible for construting  the Process Parameter the given configuration element.
 */
public class ProcessParameter {
	public static final byte SIMPLE = 1;
	public static final byte SIMPLE_ARRAY = 2;
	public static final byte COMPLEX = 3;
	public static final byte COMPLEX_ARRAY = 4;
	
	private static final String ELEM_NAME = "name"; //$NON-NLS-1$
	private static final String ELEM_BASE_TYPE = "baseType"; //$NON-NLS-1$
	private static final String ELEM_SIMPLE = "simple"; //$NON-NLS-1$
	private static final String ELEM_SIMPLE_ARRAY = "simpleArray"; //$NON-NLS-1$
	private static final String ELEM_COMPLEX = "complex"; //$NON-NLS-1$
	private static final String ELEM_COMPLEX_ARRAY = "complexArray"; //$NON-NLS-1$
	private static final String ELEM_EXTERNAL = "external"; //$NON-NLS-1$
	private static final String ELEM_NULLABLE = "nullable"; //$NON-NLS-1$
	
	private String name;
	private byte type;
	
	private ProcessParameter[] complexChildren;
	private boolean external;
	private boolean nullable;
	
	/**
	 * Constructor to extract the parameter info.
	 * @param element
	 */
	public ProcessParameter(IConfigurationElement element) {
		this.name = element.getAttribute(ELEM_NAME);
		String elemName = element.getName();
		if (elemName.equals(ELEM_SIMPLE)) {
			type = SIMPLE;
		} else if (elemName.equals(ELEM_SIMPLE_ARRAY)) {
			type = SIMPLE_ARRAY;
		} else if (elemName.equals(ELEM_COMPLEX)) {
			type = COMPLEX;
			IConfigurationElement[] children = element.getChildren();
			complexChildren = new ProcessParameter[children.length];
			for(int i=0; i<children.length; i++) {
				complexChildren[i] = new ProcessParameter(children[i]);
			}
		} else if (elemName.equals(ELEM_COMPLEX_ARRAY)) {
			type = COMPLEX_ARRAY;
			IConfigurationElement baseType = element.getChildren(ELEM_BASE_TYPE)[0];
			IConfigurationElement[] children = baseType.getChildren();
			complexChildren = new ProcessParameter[children.length];
			for(int i=0; i<children.length; i++) {
				complexChildren[i] = new ProcessParameter(children[i]);
			}
		} else {
			throw new IllegalArgumentException();
		}
		
		external = Boolean.valueOf(element.getAttribute(ELEM_EXTERNAL)).booleanValue(); 
		nullable = Boolean.valueOf(element.getAttribute(ELEM_NULLABLE)).booleanValue();
	}
	
	/**
	 * Return the Element name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the Element Type.
	 */
	public byte getType() {
		return type;
	}
	
	/**
	 * @return   the complexChildren
	 */
	public ProcessParameter[] getComplexChildren() {
		return complexChildren;
	}

	/**
	 * Checks whether the element in external. 
	 */
	public boolean isExternal() {
		return external;
	}

	/**
	 * @return  the nullable
	 */
	public boolean isNullable() {
		return nullable;
	}
}
