/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.internal;

import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Abstract base class that represents information
 * associated with a declared extension point.
 * <p>
 * Derived classes are expected to implement their
 * own getter functions to return data from the
 * associated IConfigurationElement in a reasonable
 * format.
 */
public abstract class ACExtensionPoint {

	public final static String FIELD_ID = "id"; //$NON-NLS-1$
	public final static String FIELD_NAME = "name"; //$NON-NLS-1$
	public final static String FIELD_TYPE = "name"; //$NON-NLS-1$
	public final static String FIELD_NATURES = "natures"; //$NON-NLS-1$
	public final static String FIELD_CLASS = "class"; //$NON-NLS-1$

	/**
	 * Configuration element associated with this class.
	 * CONSIDER: is it expensive to hold on to this?
	 */
	private IConfigurationElement fElement;

	/**
	 * Constructor.
	 * 
	 * @param element configuration element for the build configuration provider.
	 */
	public ACExtensionPoint(IConfigurationElement element) {
		fElement = element;
	}

	/**
	 * Returns the configuration element for the build configuration provider.
	 * 
	 * @return configuration element
	 */
	protected IConfigurationElement getConfigurationElement() {
		return fElement;
	}

	/**
	 * Breaks up a token-delimited string into individual tokens.
	 * 
	 * @param data string to tokenize.
	 * @param sep delimiter character(s).
	 * @return array of tokens extracted from the string.
	 */
	protected String[] parseField(String data, String sep) {
		Vector res = new Vector();
		StringTokenizer st = new StringTokenizer(data, sep);
		while (st.hasMoreElements()) {
			res.add(st.nextElement());
		}
		return (String[]) res.toArray(new String[res.size()]);
	}

	/**
	 * Returns the value of the named field from the configuration element.
	 * If the named field is not present or has no value, returns an empty
	 * string.
	 * 
	 * @param fieldName name of field.
	 * @return value of named field, or "".
	 */
	protected String getField(String fieldName) {
		return getField(fieldName, ""); //$NON-NLS-1$
	}

	/**
	 * Returns the value of the named field from the configuration element.
	 * If the named field is not present or has no value, returns the
	 * specified default value.
	 * 
	 * @param fieldName name of field.
	 * @param defaultValue default value if field not present.
	 * @return value of named field, or default.
	 */
	protected String getField(String fieldName, String defaultValue) {
		String val = getConfigurationElement().getAttribute(fieldName);
		return val != null ? val : defaultValue;
	}

	/**
	 * Returns an instance of of an implementing class.  This
	 * method uses the value of the FIELD_CLASS attribute in
	 * the configuration element to create the class.
	 * 
	 * @return instance of provider class.
	 */
	protected Object getClassInstance() throws CoreException {
		return getClassInstance(FIELD_CLASS);
	}

	/**
	 * Returns an instance of of an implementing class.
	 * 
	 * @param fieldName name of field.
	 * @return instance of provider class.
	 */
	protected Object getClassInstance(String fieldName) throws CoreException {
		return getConfigurationElement().createExecutableExtension(fieldName);
	}

}
