/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

/**
 * A container for things declared as Q_PROPERTY within a subclass of QObject.  In Qt 4.8,
 * Q_PROPERTY looks like:
 * <pre>
 * Q_PROPERTY( type name
 *             READ getFunction
 *             [WRITE setFunction]
 *             [RESET resetFunction]
 *             [NOTIFY notifySignal]
 *             [REVISION int]
 *             [DESIGNABLE bool]
 *             [SCRIPTABLE bool]
 *             [STORED bool]
 *             [USER bool]
 *             [CONSTANT]
 *             [FINAL] )
 * </pre>
 * This interface provides structured access to the elements of the Q_PROPERTY expansion.
 */
public interface IQProperty extends IQObject.IMember {

	/**
	 * The attributes that were defined in Qt 4.8.  This is the full set of attributes
	 * that will be parsed from Q_PROPERTY expansions.
	 */
	public static enum Attribute {
		// This enum is used when scanning Q_PROPERTY expansions, only attributes listed here
		// will be extracted from the expansion.  This enum should be expanded with new values
		// as needed (based on the version of Qt).  See QProperty#scanAttributes.
		READ("getFunction"), WRITE("setFunction"), RESET("resetFunction"), NOTIFY("notifySignal"), REVISION("int"),
		DESIGNABLE("bool"), SCRIPTABLE("bool"), STORED("bool"), USER("bool"), CONSTANT(null), FINAL(null);

		/**
		 * A string containing the C++ identifier for this attribute.
		 */
		public final String identifier = toString();

		/**
		 * Stores the attribute parameter name if it exists and null for attributes
		 * without parameters.  The parameter name comes from the Qt documentation
		 * and is only intended for documentation.
		 */
		public final String paramName;

		/**
		 * True when this attribute is expected to have a value and false otherwise.
		 */
		public final boolean hasValue;

		private Attribute(String paramName) {
			this.paramName = paramName;
			this.hasValue = paramName != null;
		}

		/**
		 * A convenience method to access the value of a property through this
		 * enum, e.g.,
		 * <pre>
		 * IQProperty qprop;
		 * String readFunction = IQProperty.Attribute.READ.valueId( qprop );
		 * </pre>
		 * Returns null if there is no associated value in the given property.
		 */
		public String valueIn(IQProperty qprop) {
			return qprop == null ? null : qprop.getValue(this);
		}
	}

	/**
	 * Returns the type of the property.  This is the first field in the Q_PROPERTY expansion.
	 */
	public String getType();

	/**
	 * Returns the name of the property.  This is the second field in the Q_PROPERTY expansion.
	 */
	public String getName();

	/**
	 * Return the value of the attribute associated with the given key.  E.g., in
	 * <pre>
	 * Q_PROPERTY( bool allowed READ isAllowed )
	 * <pre>
	 * The parameter Attribute.READ would return "isAllowed".
	 *
	 * Returns null if the given key is not described in the property.
	 */
	public String getValue(Attribute attr);

	/**
	 * Return the value of READ or null if READ is not described in the property.
	 */
	public String getReadMethodName();

	/**
	 * Return the value of WRITE or null if WRITE is not described in the property.
	 */
	public String getWriteMethodName();

	/**
	 * Return the value of RESET or null if RESET is not described in the property.
	 */
	public String getResetMethodName();

	/**
	 * Return the value of NOTIFY or null if NOTIFY is not described in the property.
	 */
	public String getNotifyMethodName();

	/**
	 * Return the value of REVISION or null if REVISION is not described in the property.
	 * The return type is Long in order to accommodate unsigned C++ 32-bit values.
	 */
	public Long getRevision();

	/**
	 * Return the value of DESIGNABLE or null if DESIGNABLE is not described in the property.
	 */
	public String getDesignable();

	/**
	 * Return the value of SCRIPTABLE or null if SCRIPTABLE is not described in the property.
	 */
	public String getScriptable();

	/**
	 * Return the value of STORED or null if STORED is not described in the property.
	 */
	public String getStored();

	/**
	 * Return the value of USER or null if USER is not described in the property.
	 */
	public String getUser();

	/**
	 * Return true if CONSTANT was specified in the Q_PROPERTY expansion and false otherwise.
	 */
	public boolean isConstant();

	/**
	 * Return true if FINAL was specified in the Q_PROPERTY expansion and false otherwise.
	 */
	public boolean isFinal();
}
