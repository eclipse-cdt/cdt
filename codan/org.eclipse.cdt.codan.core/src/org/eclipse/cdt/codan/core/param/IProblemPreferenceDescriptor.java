/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Problem parameter usually key=value settings that allows to alter checker
 * behaviour for given problem. For example if checker finds violation of naming
 * conventions for function, parameter would be the pattern of allowed names.
 * ProblemParameterInfo represent parameter meta-info for the ui. If more that
 * one parameter required ParameterInfo should describe hash or array of
 * parameters. This is only needed for auto-generated ui for parameter editing.
 * For complex case custom ui control should be used. Extend
 * AbstractProblemParamterInfo class
 * to implement this interface.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemPreferenceDescriptor extends Cloneable {
	public enum PreferenceType {
		TYPE_STRING("string"), //$NON-NLS-1$
		TYPE_INTEGER("integer"), //$NON-NLS-1$
		TYPE_BOOLEAN("boolean"), //$NON-NLS-1$
		TYPE_FILE("file"), //$NON-NLS-1$
		TYPE_LIST("list"), //$NON-NLS-1$
		TYPE_MAP("map"); //$NON-NLS-1$
		private String literal;

		private PreferenceType(String literal) {
			this.literal = literal;
		}

		public static PreferenceType valueOfLiteral(String name) {
			PreferenceType[] values = values();
			for (int i = 0; i < values.length; i++) {
				PreferenceType e = values[i];
				if (e.literal.equals(name))
					return e;
			}
			return null;
		}

		@Override
		public String toString() {
			return literal;
		}

		/**
		 * @param value
		 * @return parameter type corresponding to the value java type
		 */
		public static PreferenceType typeOf(Object value) {
			if (value instanceof Boolean)
				return TYPE_BOOLEAN;
			if (value instanceof String)
				return TYPE_STRING;
			if (value instanceof Integer)
				return TYPE_INTEGER;
			if (value instanceof File)
				return TYPE_FILE;
			if (value instanceof List)
				return TYPE_LIST;
			if (value instanceof Map)
				return TYPE_MAP;
			throw new IllegalArgumentException(
					"Cannot determine type for " + value.getClass()); //$NON-NLS-1$
		}
	}

	String getKey();

	/**
	 * type of the parameter, supports boolean, integer, string, file, list and
	 * hash. If list is the value - it is an array - subparameter can be
	 * accessed by number, if hash is the value - it is a hash - subparameter
	 * can be accesses by name
	 * 
	 * @return string value of the type
	 */
	PreferenceType getType();

	/**
	 * Additional info on how it is represented in the ui, for example boolean
	 * can be represented as checkbox, drop-down and so on, Values TBD
	 * 
	 * @return ui info or null if not set
	 */
	String getUiInfo();

	/**
	 * User visible label for the parameter control in UI
	 * 
	 * @return the label
	 */
	String getLabel();

	/**
	 * Detailed explanation of parameter
	 * 
	 * @return the toolTip text
	 */
	String getToolTip();

	/**
	 * Available if type is composite. Returns value of subdescriptor with the
	 * name of key. For the "list" type key is the number (index).
	 * 
	 * @param key
	 *            - name of the subdescriptor.
	 * @return
	 */
	IProblemPreference getChildDescriptor(String key);

	/**
	 * Available if type is list or map. Returns array of children.
	 * Of size 0 for basic types, keys for map, and arrays of 0 element for
	 * array type
	 * (since all elements are the same).
	 * 
	 * @return
	 */
	IProblemPreference[] getChildDescriptors();

	void addChildDescriptor(IProblemPreference info);

	Object clone();

	IProblemPreference getParent();

	public void setParent(IProblemPreference parent);

	String getQualifiedKey();
}
