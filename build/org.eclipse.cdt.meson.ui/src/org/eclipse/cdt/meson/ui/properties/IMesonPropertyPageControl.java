/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.meson.ui.properties;

public interface IMesonPropertyPageControl {
	
	/**
	 * Get the value of the field
	 * @return field value
	 */
	String getFieldValue();
	
	/**
	 * Get the name of the field to set for meson command
	 * @return field name
	 */
	String getFieldName();
	
	/**
	 * Has the initial value changed
	 * @return
	 */
	boolean isValueChanged();
	
	/**
	 * Is this field valid?
	 * @return
	 */
	boolean isValid();
	
	/**
	 * Get the command line parameter if already configured
	 * @return String containing command-line for configured build dir
	 */
	default String getConfiguredString() {
		return "-D" + getFieldName() + "=" + getFieldValue(); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Get the command line parameter if never configured
	 * @return String containing command-line parm for unconfigured build dir
	 */
	default String getUnconfiguredString() {
		return "--" + getFieldName() + "=" + getFieldValue(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Get any error message for the control
	 * @return error message
	 */
	String getErrorMessage();
	
}
