/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.resources;

import java.util.EventObject;

import org.eclipse.core.runtime.IPath;

/**
 * Describes a change in path variable.
 */
public class PathEntryVariableChangeEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	/** Event type constant (value = 1) that denotes a value change . */
	public final static int VARIABLE_CHANGED = 1;

	/** Event type constant (value = 2) that denotes a variable creation. */
	public final static int VARIABLE_CREATED = 2;

	/** Event type constant (value = 3) that denotes a variable deletion. */
	public final static int VARIABLE_DELETED = 3;


	/**
	 * The name of the changed variable.
	 */
	private String variableName;

	/**
	 * The value of the changed variable (may be null). 
	 */
	private IPath value;

	/** The event type. */
	private int type;

	/**
	 * Constructor for this class.
	 */
	public PathEntryVariableChangeEvent(IPathEntryVariableManager source, String variableName, IPath value, int type) {
		super(source);
		if (type < VARIABLE_CHANGED || type > VARIABLE_DELETED)
			throw new IllegalArgumentException("Invalid event type: " + type); //$NON-NLS-1$
		this.variableName = variableName;
		this.value = value;
		this.type = type;
	}

	public IPath getValue() {
		return value;
	}

	public String getVariableName() {
		return variableName;
	}

	public int getType() {
		return type;
	}

	/**
	 * Return a string representation of this object.
	 */
	public String toString() {
		String[] typeStrings = {"VARIABLE_CHANGED", "VARIABLE_CREATED", "VARIABLE_DELETED"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append("[variable = "); //$NON-NLS-1$
		sb.append(variableName);
		sb.append(", type = "); //$NON-NLS-1$
		sb.append(typeStrings[type - 1]);
		if (type != VARIABLE_DELETED) {
			sb.append(", value = "); //$NON-NLS-1$
			sb.append(value);
		}
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

}
