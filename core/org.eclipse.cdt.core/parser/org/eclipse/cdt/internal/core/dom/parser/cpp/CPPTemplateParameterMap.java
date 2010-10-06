/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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

	public CPPTemplateParameterMap(CPPTemplateParameterMap other) {
		fMap= (ObjectMap) other.fMap.clone();
	}

	/**
	 * Returns whether the map contains the given parameter
	 */
	public boolean contains(ICPPTemplateParameter templateParameter) {
		return fMap.containsKey(templateParameter.getParameterID());
	}

	/**
	 * Adds the mapping.
	 */
	public void put(ICPPTemplateParameter param, ICPPTemplateArgument value) {
		fMap.put(param.getParameterID(), value);
	}
	
	/**
	 * Adds the mapping.
	 */
	public void put(int parameterID, ICPPTemplateArgument value) {
		fMap.put(parameterID, value);
	}

	/**
	 * Adds the mapping.
	 */
	public void put(ICPPTemplateParameter param, ICPPTemplateArgument[] packExpansion) {
		fMap.put(param.getParameterID(), packExpansion);
	}
	
	/**
	 * Adds the mapping.
	 */
	public void put(int parameterID, ICPPTemplateArgument[] packExpansion) {
		fMap.put(parameterID, packExpansion);
	}

	/**
	 * Returns the value for the given parameter.
	 */
	public ICPPTemplateArgument getArgument(ICPPTemplateParameter param) {
		if (param == null)
			return null;
		return getArgument(param.getParameterID());
	}

	/**
	 * Returns the value for the template parameter with the given id.
	 * @see ICPPTemplateParameter#getParameterID()
	 */
	public ICPPTemplateArgument getArgument(int paramID) {
		final Object object = fMap.get(paramID);
		if (object instanceof ICPPTemplateArgument) {
			return (ICPPTemplateArgument) object;
		}
		return null;
	}

	/**
	 * Returns the values for the given template parameter pack.
	 */
	public ICPPTemplateArgument[] getPackExpansion(ICPPTemplateParameter tpar) {
		return getPackExpansion(tpar.getParameterID());
	}

	/**
	 * Returns the values for the template parameter pack with the given id.
	 * @see ICPPTemplateParameter#getParameterID()
	 */
	public ICPPTemplateArgument[] getPackExpansion(int paramID) {
		final Object object = fMap.get(paramID);
		if (object instanceof ICPPTemplateArgument[]) {
			return (ICPPTemplateArgument[]) object;
		}
		return null;
	}

	public ICPPTemplateArgument getArgument(ICPPTemplateParameter tpar, int packOffset) {
		return getArgument(tpar.getParameterID(), packOffset);
	}

	/**
	 * Returns the argument at the given position
	 */
	public ICPPTemplateArgument getArgument(int paramID, int packOffset) {
		final Object object = fMap.get(paramID);
		if (object instanceof ICPPTemplateArgument)
			return (ICPPTemplateArgument) object;
		if (object instanceof ICPPTemplateArgument[]) {
			ICPPTemplateArgument[] args = (ICPPTemplateArgument[]) object;
			if (packOffset < args.length && packOffset >= 0)
				return args[packOffset];
		}
		return null;
	}

	/**
	 * Returns the argument at the given position
	 */
	public boolean putPackElement(Integer paramID, int packOffset, ICPPTemplateArgument arg, int packSize) {
		ICPPTemplateArgument[] args;
		final Object object = fMap.get(paramID);
		if (object instanceof ICPPTemplateArgument[]) {
			args = (ICPPTemplateArgument[]) object;
			if (packSize != args.length) 
				return false;
		} else if (object == null) {
			args= new ICPPTemplateArgument[packSize];
			fMap.put(paramID, args);
		} else {
			return false;
		}
		args[packOffset]= arg;
		return true;
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

	public boolean mergeToExplicit(CPPTemplateParameterMap deducedMap) {
		Integer[] keys= deducedMap.getAllParameterPositions();
		for (Integer key : keys) {
			Object explicit= fMap.get(key);
			Object deduced= deducedMap.fMap.get(key);
			if (explicit == null) {
				if (deduced instanceof ICPPTemplateArgument[]) {
					for (ICPPTemplateArgument arg : (ICPPTemplateArgument[]) deduced) {
						if (arg == null)
							return false;
					}
				}
				fMap.put(key, deduced);
			} else if (explicit instanceof ICPPTemplateArgument[] && deduced instanceof ICPPTemplateArgument[]) {
				ICPPTemplateArgument[] explicitPack= (ICPPTemplateArgument[]) explicit;
				ICPPTemplateArgument[] deducedPack= (ICPPTemplateArgument[]) deduced;
				if (deducedPack.length < explicitPack.length)
					return false;
				System.arraycopy(explicitPack, 0, deducedPack, 0, explicitPack.length);
				for (ICPPTemplateArgument arg : deducedPack) {
					if (arg == null)
						return false;
				}
				fMap.put(key, deducedPack);
			} else {
				return false;
			}
		}
		return true;
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
				
				final Object obj = fMap.getAt(i);
				if (obj instanceof ICPPTemplateArgument) {
					appendArg(sb, key, (ICPPTemplateArgument) obj);
				} else if (obj instanceof ICPPTemplateArgument[]) {
					for (ICPPTemplateArgument arg : (ICPPTemplateArgument[]) obj) {
						appendArg(sb, key, arg);
					}
				}
			}
		}
		sb.append("}"); //$NON-NLS-1$
		return sb.toString();
	}

	private void appendArg(StringBuilder sb, Integer key, ICPPTemplateArgument value) {
		sb.append('#');
		sb.append(key >> 16);
		sb.append(',');
		sb.append(key & 0xffff);
		sb.append(": "); //$NON-NLS-1$
		sb.append(ASTTypeUtil.getArgumentString(value, true));
	}
}
