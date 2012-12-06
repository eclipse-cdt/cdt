/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

/**
 * Maps function parameter positions to values.
 */
public class CPPFunctionParameterMap {
	public static final CPPFunctionParameterMap EMPTY = new CPPFunctionParameterMap(0);
	
	private ObjectMap fMap;

	/**
	 * Constructs an empty parameter map.
	 */
	public CPPFunctionParameterMap(int initialSize) {
		fMap= new ObjectMap(initialSize);
	}

	public CPPFunctionParameterMap(CPPFunctionParameterMap other) {
		fMap= (ObjectMap) other.fMap.clone();
	}

	/**
	 * Returns whether the map contains the given parameter
	 */
	public boolean contains(int parameterPosition) {
		return fMap.containsKey(parameterPosition);
	}

	/**
	 * Adds the mapping.
	 */
	public void put(int parameterPosition, ICPPEvaluation value) {
		fMap.put(parameterPosition, value);
	}
	
	/**
	 * Adds the mapping.
	 */
	public void put(int parameterPosition, ICPPEvaluation[] packExpansion) {
		fMap.put(parameterPosition, packExpansion);
	}
	
	/**
	 * Returns the value for the given parameter.
	 */
	public ICPPEvaluation getArgument(int parameterPosition) {
		final Object object = fMap.get(parameterPosition);
		if (object instanceof ICPPEvaluation) {
			return (ICPPEvaluation) object;
		}
		return null;
	}

	/**
	 * Returns the values for the given function parameter pack.
	 */
	public ICPPEvaluation[] getPackExpansion(int parameterPosition) {
		final Object object = fMap.get(parameterPosition);
		if (object instanceof ICPPEvaluation[]) {
			return (ICPPEvaluation[]) object;
		}
		return null;
	}

	/**
	 * Returns the argument at the given position
	 */
	public ICPPEvaluation getArgument(int parameterPosition, int packOffset) {
		final Object object = fMap.get(parameterPosition);
		if (object instanceof ICPPEvaluation)
			return (ICPPEvaluation) object;
		if (object instanceof ICPPEvaluation[]) {
			ICPPEvaluation[] args = (ICPPEvaluation[]) object;
			if (packOffset < args.length && packOffset >= 0)
				return args[packOffset];
		}
		return null;
	}

	/**
	 * Puts all mappings from the supplied map into this map.
	 */
	public void putAll(CPPFunctionParameterMap map) {
		final ObjectMap otherMap= map.fMap;
		for (int i = 0; i < otherMap.size(); i++) {
			fMap.put(otherMap.keyAt(i), otherMap.getAt(i));
		}
	}
}
