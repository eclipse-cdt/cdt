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

package org.eclipse.cdt.core.builder.model;

/**
 * Default implementation of the ICBuildVariable interface.
 * <p>
 * This implementation is capable of handling both static
 * resolution (where the variable part is fixed at generation
 * time) and dynamic resolution (where the variable part
 * may change over time, depending on context.)
 * <p>
 * @see ICBuildVariable
 * @see ICBuildVariableProvider
 * @see ICBuildVariableResolver
 */
public class CBuildVariable implements ICBuildVariable {

	private String fFixed;
	private String fVariable;
	private ICBuildVariableResolver fResolver;

	/**
	 * Default implementation of ICBuildVariableResolver that
	 * simply returns a previously provided string as the
	 * resolved value for the build variable.
	 */
	static private class StringResolver implements ICBuildVariableResolver {
		private String fValue;
		
		public StringResolver(String value) {
			fValue = value;
		}

		public String resolveValue(ICBuildVariable var) {
			return fValue + var.getFixed();
		}
	};

	/**
	 * Create a new build variable with the given variable
	 * and fixed elements, and a static resolver that always
	 * returns the same value for the variable portion of
	 * the variable.
	 * 
	 * @param name 	variable portion of build variable.
	 * @param fixed	fixed portion of build variable.
	 * @param resolved	resolved variable value.
	 */
	public CBuildVariable(String name, String fixed, String resolved) {
		this(name, fixed, new StringResolver(resolved));
	}

	/**
	 * Create a new build variable with the given fixed
	 * and variable values, and a dynamic resolver for
	 * the variable portion of the variable.
	 * 
	 * @param name 	variable portion of build variable.
	 * @param fixed	fixed portion of build variable.
	 * @param resolved	resolved variable value.
	 */
	public CBuildVariable(String name, String fixed, ICBuildVariableResolver resolver) {
		fVariable = name;
		fFixed = fixed;
		fResolver = resolver;
	}

	/**
	 * Create a new build variable with the given fixed
	 * and variable values, and a dynamic resolver for
	 * the variable portion of the variable.
	 * 
	 * @param name 	variable portion of build variable.
	 * @param fixed	fixed portion of build variable.
	 * @param resolved	resolved variable value.
	 */
	public CBuildVariable(String name, ICBuildVariable base) {
		fVariable = name;
		fFixed = base.getFixed();
		fResolver = base.getResolver();
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICBuildVariable#getVariable()
	 */
	public String getVariable() {
		return fVariable;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICBuildVariable#getFixed()
	 */
	public String getFixed() {
		return fFixed;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICBuildVariable#getResolver()
	 */
	public ICBuildVariableResolver getResolver() {
		return fResolver;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICBuildVariable#getValue()
	 */
	public String getValue() {
		return fResolver.resolveValue(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int result = 17;
		result = (result * 37) + fVariable.hashCode();
		result = (result * 37) + fFixed.hashCode();
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[" + fVariable + "]" + fFixed;
	}

}
