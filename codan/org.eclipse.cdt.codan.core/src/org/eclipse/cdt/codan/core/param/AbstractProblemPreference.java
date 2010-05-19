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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * Default implementation of problem preference. It keeps preference metadata
 * together with preference itself.
 * 
 */
public abstract class AbstractProblemPreference implements IProblemPreference {
	public static final String PARAM = "param"; //$NON-NLS-1$
	protected String key;
	protected String label;
	protected String toolTip = null;
	protected PreferenceType type;
	protected String uiInfo;
	private IProblemPreference parent;

	public PreferenceType getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public String getToolTip() {
		return toolTip;
	}

	public String getKey() {
		return key;
	}

	public String getUiInfo() {
		return uiInfo;
	}

	public void setKey(String key) {
		if (isValidIdentifier(key))
			this.key = key;
		else
			throw new IllegalArgumentException(
					"Key must have java identifier syntax or number, i.e no dots and other funky stuff"); //$NON-NLS-1$
	}

	protected boolean isValidIdentifier(String id) {
		if (id == null)
			return false;
		int n = id.length();
		if (n == 0)
			return false;
		for (int i = 0; i < n; i++)
			if (!Character.isJavaIdentifierPart(id.charAt(i)))
				return false;
		return true;
	}

	public void setLabel(String label) {
		if (label == null)
			throw new NullPointerException("Label cannot be null"); //$NON-NLS-1$
		this.label = label;
	}

	public void setToolTip(String tooltip) {
		this.toolTip = tooltip;
	}

	public void setType(PreferenceType type) {
		if (type == null)
			throw new NullPointerException("Type cannot be null"); //$NON-NLS-1$
		this.type = type;
	}

	public void setUiInfo(String uiinfo) {
		this.uiInfo = uiinfo;
	}

	public Object getValue() {
		throw new UnsupportedOperationException();
	}

	public void setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * @param str
	 * @return
	 */
	protected StreamTokenizer getImportTokenizer(String str) {
		ByteArrayInputStream st = new ByteArrayInputStream(str.getBytes());
		StreamTokenizer tokenizer = new StreamTokenizer(new InputStreamReader(
				st));
		tokenizer.resetSyntax();
		tokenizer.quoteChar('"');
		tokenizer.wordChars('_', '_');
		tokenizer.wordChars('-', '-');
		tokenizer.wordChars('.', '.');
		tokenizer.wordChars('0', '9');
		tokenizer.wordChars('a', 'z');
		tokenizer.wordChars('A', 'Z');
		tokenizer.wordChars(128 + 32, 255);
		tokenizer.whitespaceChars(0, ' ');
		tokenizer.commentChar('/');
		return tokenizer;
	}

	public IProblemPreference getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(IProblemPreference parent) {
		this.parent = parent;
	}

	public String getQualifiedKey() {
		if (parent == null)
			return getKey();
		return parent.getQualifiedKey() + "." + getKey(); //$NON-NLS-1$
	}
}
