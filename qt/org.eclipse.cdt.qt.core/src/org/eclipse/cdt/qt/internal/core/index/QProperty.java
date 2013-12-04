/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.index;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.IQProperty;

public class QProperty extends AbstractQField implements IQProperty {

	private String type;
	private final String[] values = new String[Attribute.values().length];

	public QProperty(IQObject owner, String type, String name) {
		super(owner);
		this.type = type;
		this.name = name;
	}

	/**
	 * A regular expression for scanning the full Q_PROPERTY expansion and extracting the
	 * expansion parameter.  It provides the following capture groups:
	 * 1 - the type
	 * 2 - the name
	 * 3 - the trimmed remainder of the expansion parameter (starting with READ)
	 *
	 * This REGEX handles cases like:
	 * 	    Q_PROPERTY(T* t  READ ... )
	 * 		Q_PROPERTY(T * t READ ... )
	 * 		Q_PROPERTY(T *t  READ ... )
	 * This REGEX assumes that READ will directly follow the property name.  This is implied,
	 * although not explicitly stated in the Qt documentation.
	 *
	 * It also allows the option of having no other attribute (just type and name).  The Qt
	 * documentation forbids this, but it is used in QtSensors/
	 */
	private static final Pattern EXPANSION_REGEX = Pattern.compile("^(.+?)\\s*([a-zA-Z_][\\w]*+)(?:(?:\\s+(READ\\s+.*))|\\s*)$");

	/**
	 * A regular expression for scanning Q_PROPERTY attributes.  The regular expression is built
	 * from the values defined in IQProperty#Attribute.  It looks like:
	 * <pre>
	 * (:?READ)|(?:WRITE)|(:?RESET)|...
	 * </pre>
	 * This regular expression is used to recognize valid attributes while scanning the
	 * Q_PROPERTY macro expansion.
	 *
	 * @see QProperty#scanAttributes(String)
	 */
	private static final Pattern ATTRIBUTE_REGEX;
	static {
		StringBuilder regexBuilder = new StringBuilder();
		for(IQProperty.Attribute attr : IQProperty.Attribute.values()) {
			if (attr.ordinal() > 0)
				regexBuilder.append('|');
			regexBuilder.append("(:?");
			regexBuilder.append(attr.identifier);
			regexBuilder.append(")");
		}
		ATTRIBUTE_REGEX = Pattern.compile(regexBuilder.toString());
	}

	/**
	 * Scans the given field and extracts the strings defining the attributes of the
	 * Q_PROPERTY.  Returns false if the field is does not represent a Q_PROPERTY, does
	 * not have attribute-related information, or if the information does not match the
	 * expected format.
	 * @param field
	 * @return
	 */
	@Override
	protected boolean scanDefn(String expansionParam) {
		Matcher m = EXPANSION_REGEX.matcher(expansionParam);
		if (!m.matches())
			return false;

		this.type = m.group(1);
		this.name = m.group(2);
		return scanAttributes(m.group(3));
	}

	/**
	 * Scans the given string to extract values for all recognized attributes.  A regular expression
	 * is used to find the attributes, substrings between attributes are assigned as values.
	 * Attributes that don't expect a value (as determined by {@link IQProperty#Attribute#hasValue}),
	 * as assigned "true".
	 */
	private boolean scanAttributes(String attributes) {
		if (attributes == null)
			return true;

		int lastEnd = 0;
		IQProperty.Attribute lastAttr = null;
		for(Matcher attributeMatcher = ATTRIBUTE_REGEX.matcher(attributes); attributeMatcher.find(); lastEnd = attributeMatcher.end()) {
			// set the value of attribute found in the previous iteration to the substring between
			// the end of that attribute and the start of this one
			if (lastAttr != null) {
				String value = attributes.substring(lastEnd, attributeMatcher.start());
				values[lastAttr.ordinal()] = value.trim();
			}

			// the regex is built from the definition of the enum, so none of the strings that it
			// finds will throw an exception
			lastAttr = IQProperty.Attribute.valueOf(IQProperty.Attribute.class, attributeMatcher.group(0));

			// if this attribute doesn't have a value, then put it into the value map immediately
			// and make sure it is not used later in this scan
			if (!lastAttr.hasValue) {
				values[lastAttr.ordinal()] = Boolean.TRUE.toString();
				lastAttr = null;
			}
		}

		// the value of the last attribute in the expansion is the substring between the end of
		// the attribute identifier and the end of the string
		if (lastAttr != null) {
			String value = attributes.substring(lastEnd);
			values[lastAttr.ordinal()] = value.trim();
		}

		return true;
	}

	public void setAttribute(IQProperty.Attribute attr, String value) {
		values[attr.ordinal()] = ( value == null ? "" : value );
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue(Attribute attr) {
		return values[attr.ordinal()];
	}

	@Override
	public String getReadMethodName() {
		return Attribute.READ.valueIn(this);
	}

	@Override
	public String getWriteMethodName() {
		return Attribute.WRITE.valueIn(this);
	}

	@Override
	public String getResetMethodName() {
		return Attribute.RESET.valueIn(this);
	}

	@Override
	public String getNotifyMethodName() {
		return Attribute.NOTIFY.valueIn(this);
	}

	@Override
	public Long getRevision() {
		String revision = Attribute.REVISION.valueIn(this);
		if (revision != null)
			try {
				return Long.valueOf(revision);
			} catch(NumberFormatException e) {
				// This is a problem with the user's C++ code, there is no need to log this exception,
				// just ignore the value.
			}

		return null;
	}

	@Override
	public String getDesignable() {
		return Attribute.DESIGNABLE.valueIn(this);
	}

	@Override
	public String getScriptable() {
		return Attribute.SCRIPTABLE.valueIn(this);
	}

	@Override
	public String getStored() {
		return Attribute.STORED.valueIn(this);
	}

	@Override
	public String getUser() {
		return Attribute.USER.valueIn(this);
	}

	@Override
	public boolean isConstant() {
		return Attribute.CONSTANT.valueIn(this) != null;
	}

	@Override
	public boolean isFinal() {
		return Attribute.FINAL.valueIn(this) != null;
	}
}
