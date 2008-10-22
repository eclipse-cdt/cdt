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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * Maps template parameters to values.
 * @since 5.1
 */
public class CPPTemplateParameterMap implements ICPPTemplateParameterMap {
	public static final CPPTemplateParameterMap EMPTY = new CPPTemplateParameterMap();
	
	private ObjectMap fMap= new ObjectMap(2);

	/**
	 * Returns whether the map contains the given parameter
	 */
	public boolean contains(ICPPTemplateParameter templateParameter) {
		return fMap.containsKey(templateParameter.getParameterPosition());
	}

	/**
	 * Adds the mapping to the map.
	 */
	public void put(ICPPTemplateParameter param, ICPPTemplateArgument value) {
		fMap.put(param.getParameterPosition(), value);
	}

	/**
	 * Adds the mapping to the map.
	 */
	public void put(int parameterPos, ICPPTemplateArgument value) {
		fMap.put(parameterPos, value);
	}

	/**
	 * Returns the value for the given parameter.
	 */
	public ICPPTemplateArgument getArgument(ICPPTemplateParameter param) {
		return (ICPPTemplateArgument) fMap.get(param.getParameterPosition());
	}

	/**
	 * Returns the value for the template parameter at the given position.
	 * @see ICPPTemplateParameter#getParameterPosition()
	 */
	public ICPPTemplateArgument getArgument(int paramPosition) {
		return (ICPPTemplateArgument) fMap.get(paramPosition);
	}

	/**
	 * Puts all mappings from the supplied map into this map.
	 */
	public void putAll(CPPTemplateParameterMap map) {
		final ObjectMap omap= map.fMap;
		for (int i = 0; i < omap.size(); i++) {
			fMap.put(omap.keyAt(i), omap.getAt(i));
		}
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
