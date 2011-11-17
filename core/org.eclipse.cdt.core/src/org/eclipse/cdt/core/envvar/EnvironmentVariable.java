/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    James Blackburn (Broadcom Corp.)
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.core.envvar;

import org.eclipse.cdt.internal.core.SafeStringInterner;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;



/**
 * A trivial implementation of {@link IEnvironmentVariable}
 *
 * @since 3.0
 */
public class EnvironmentVariable implements IEnvironmentVariable, Cloneable {
	protected String fName;
	protected String fValue;
	protected String fDelimiter;
	protected int fOperation;

	public EnvironmentVariable(String name, String value, int op, String delimiter) {
		fName = SafeStringInterner.safeIntern(name);
		fOperation = op;
		fValue = SafeStringInterner.safeIntern(value);
		if (delimiter == null)
			fDelimiter = EnvironmentVariableManager.getDefault().getDefaultDelimiter();
		else
			fDelimiter = delimiter;
	}

	protected EnvironmentVariable() {
		fDelimiter = EnvironmentVariableManager.getDefault().getDefaultDelimiter();
	}

	public EnvironmentVariable(String name){
		this(name,null,ENVVAR_REPLACE,null);
	}

	public EnvironmentVariable(String name, String value){
		this(name,value,ENVVAR_REPLACE,null);
	}

	public EnvironmentVariable(String name, String value, String delimiter){
		this(name,value,ENVVAR_REPLACE,delimiter);
	}

	public EnvironmentVariable(IEnvironmentVariable var){
		this(var.getName(),var.getValue(),var.getOperation(),var.getDelimiter());
	}

	@Override
	public String getName(){
		return fName;
	}

	@Override
	public String getValue(){
		return fValue;
	}

	@Override
	public int getOperation(){
		return fOperation;
	}

	@Override
	public String getDelimiter(){
		return fDelimiter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fDelimiter == null) ? 0 : fDelimiter.hashCode());
		result = prime * result + ((fName == null) ? 0 : fName.hashCode());
		result = prime * result + fOperation;
		result = prime * result + ((fValue == null) ? 0 : fValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof IEnvironmentVariable))
			return super.equals(obj);
		IEnvironmentVariable other = (IEnvironmentVariable)obj;
		if (!equals(fName, other.getName()))
			return false;
		if (!equals(fValue, other.getValue()))
			return false;
		if (!equals(fDelimiter, other.getDelimiter()))
			return false;
		if (fOperation != other.getOperation())
			return false;
		return true;
	}

	// Helper method to check equality of two objects
	private boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2)
			return true;
		else if (obj1 == null)
			return false;
		else
			return obj1.equals(obj2);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (fName != null)
			sb.append(fName);
		if (fValue != null)
			sb.append("=").append(fValue); //$NON-NLS-1$
		sb.append(" ").append(fDelimiter); //$NON-NLS-1$
		switch (fOperation) {
			case ENVVAR_REPLACE:
				sb.append(" [REPL]"); //$NON-NLS-1$
				break;
			case ENVVAR_REMOVE:
				sb.append(" [REM]"); //$NON-NLS-1$
				break;
			case ENVVAR_PREPEND:
				sb.append(" [PREP]"); //$NON-NLS-1$
				break;
			case ENVVAR_APPEND:
				sb.append(" [APP]"); //$NON-NLS-1$
				break;
			default:
				sb.append(" [NONE]"); //$NON-NLS-1$
				break;
		}
		return sb.toString();
	}

	@Override
	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
}
