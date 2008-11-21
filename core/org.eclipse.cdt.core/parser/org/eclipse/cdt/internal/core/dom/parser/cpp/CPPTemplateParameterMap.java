/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * Maps template parameters to values.
 */
public class CPPTemplateParameterMap implements ICPPTemplateParameterMap {
	public static final CPPTemplateParameterMap EMPTY = new CPPTemplateParameterMap(0);
	
	private final ObjectMap fMap;

	/**
	 * Constructs an empty parameter map.
	 */
	public CPPTemplateParameterMap(int initialSize) {
		fMap= new ObjectMap(initialSize);
	}

	/**
	 * Returns whether the map contains the given parameter
	 */
	public boolean contains(ICPPTemplateParameter templateParameter) {
		return fMap.containsKey(templateParameter.getParameterID());
	}

	/**
	 * Adds the mapping to the map.
	 */
	public void put(ICPPTemplateParameter param, ICPPTemplateArgument value) {
		fMap.put(param.getParameterID(), value);
	}

	/**
	 * Adds the mapping to the map.
	 */
	public void put(int parameterID, ICPPTemplateArgument value) {
		fMap.put(parameterID, value);
	}

	/**
	 * Returns the value for the given parameter.
	 */
	public ICPPTemplateArgument getArgument(ICPPTemplateParameter param) {
		return (ICPPTemplateArgument) fMap.get(param.getParameterID());
	}

	/**
	 * Returns the value for the template parameter with the given id.
	 * @see ICPPTemplateParameter#getParameterID()
	 */
	public ICPPTemplateArgument getArgument(int paramID) {
		return (ICPPTemplateArgument) fMap.get(paramID);
	}

	/**
	 * Puts all mappings from the supplied map into this map.
	 */
	public void putAll(ICPPTemplateParameterMap map) {
		
		if (map instanceof CPPTemplateParameterMap) {
			final ObjectMap omap= ((CPPTemplateParameterMap) map).fMap;
			for (int i = 0; i < omap.size(); i++) {
				fMap.put(omap.keyAt(i), omap.getAt(i));
			}
		} else {
			assert false;
		}
	}

	public ICPPTemplateArgument[] values() {
		ICPPTemplateArgument[] result= new ICPPTemplateArgument[fMap.size()];
		for (int i = 0; i < result.length; i++) {
			result[i]= (ICPPTemplateArgument) fMap.getAt(i);
		}
		return result;
	}

	/**
	 * Returns the array of template parameter positions, for which a mapping exists.
	 */
	public Integer[] getAllParameterPositions() {
		return fMap.keyArray(Integer.class);
	}
	
	/**
	 * For debugging purposes, only.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{"); //$NON-NLS-1$
		for (int i = 0; i < fMap.size(); i++) {
			Integer key = (Integer) fMap.keyAt(i);
			if (key != null) {
				if (sb.length() > 1) {
					sb.append(", "); //$NON-NLS-1$
				}
				ICPPTemplateArgument value = (ICPPTemplateArgument) fMap.getAt(i);
				sb.append('#');
				sb.append(key >> 16);
				sb.append(',');
				sb.append(key & 0xffff);
				sb.append(": "); //$NON-NLS-1$
				sb.append(ASTTypeUtil.getArgumentString(value, true));
			}
		}
		sb.append("}"); //$NON-NLS-1$
		return sb.toString();
	}
}
