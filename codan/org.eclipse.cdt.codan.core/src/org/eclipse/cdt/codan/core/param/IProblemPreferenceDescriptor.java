/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
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
 * 
 * IProblemPreferenceDescriptor represent preference's meta-info for the ui. If
 * more than one parameter is required it can be map or list of sub-preferences.
 * This is only needed for auto-generated ui for parameter
 * editing. For more complex cases custom ui control should be used. Extend
 * {@link AbstractProblemPreference} class
 * to implement this interface.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemPreferenceDescriptor extends Cloneable {
	/**
	 * Type of the user preference
	 */
	public enum PreferenceType {
		/**
		 * String type, represented by string input field by default
		 */
		TYPE_STRING("string"), //$NON-NLS-1$
		/**
		 * Integer type, represented by integer input field by default
		 */
		TYPE_INTEGER("integer"), //$NON-NLS-1$
		/**
		 * Boolean type, represented by checkbox (boolean input field)
		 */
		TYPE_BOOLEAN("boolean"), //$NON-NLS-1$
		/**
		 * File type, represented by file picker input field
		 */
		TYPE_FILE("file"), //$NON-NLS-1$
		/**
		 * List type, represented by list (table) control
		 */
		TYPE_LIST("list"), //$NON-NLS-1$
		/**
		 * Map type, represented by composite of children fields
		 */
		TYPE_MAP("map"), //$NON-NLS-1$
		/**
		 * Custom type, represented by string input field by default
		 */
		TYPE_CUSTOM("custom"); //$NON-NLS-1$
		private String literal;

		private PreferenceType(String literal) {
			this.literal = literal;
		}

		/**
		 * @param name - name of the type literal (i.e. comes from name() or
		 *        toString())
		 * @return type represented by this name
		 */
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
			return TYPE_CUSTOM;
		}
	}

	/**
	 * Key of the preference. Key must be java-like identified or number. Cannot
	 * contain dots. Cannot be null.
	 * 
	 * @return key
	 */
	String getKey();

	/**
	 * type of the parameter, supports boolean, integer, string, file, list and
	 * map. For list type child preference can be
	 * accessed by number (index), if map is the type child preference can be
	 * accessed by a key (string)
	 * 
	 * @return type of the preference
	 */
	PreferenceType getType();

	/**
	 * Additional info on how it is represented in the ui, for example boolean
	 * can be represented as checkbox, drop-down and so on, Values TBD.
	 * Not supported at the moment.
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
	 * Detailed explanation of parameter. Not supported at the moment.
	 * 
	 * @return the toolTip text
	 */
	String getToolTip();

	/**
	 * default clone implementation
	 * 
	 * @return clone of the object
	 */
	Object clone();

	/**
	 * @return parent preference
	 */
	IProblemPreference getParent();

	/**
	 * Combined key of values from parents plus itself separated by dot
	 * 
	 * @return qualified key
	 */
	String getQualifiedKey();
}
