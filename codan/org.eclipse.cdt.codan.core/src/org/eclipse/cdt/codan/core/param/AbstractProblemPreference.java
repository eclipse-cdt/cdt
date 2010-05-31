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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * Default implementation of problem preference. It keeps preference metadata
 * together with preference value. Some implementations may separate them.
 * 
 */
public abstract class AbstractProblemPreference implements IProblemPreference {
	/**
	 * default key for a preference
	 */
	public static final String PARAM = "params"; //$NON-NLS-1$
	private String key = PARAM;
	private String label = ""; //$NON-NLS-1$
	private String toolTip = null;
	private String uiInfo;
	private IProblemPreference parent;

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

	/**
	 * Set preference key for itself
	 * 
	 * @param key
	 */
	public void setKey(String key) {
		if (key == null)
			throw new NullPointerException("key"); //$NON-NLS-1$
		if (isValidIdentifier(key))
			this.key = key;
		else
			throw new IllegalArgumentException(
					"Key must have java identifier syntax or number, i.e no dots and other funky stuff: " + key); //$NON-NLS-1$
	}

	protected boolean isValidIdentifier(String id) {
		if (id == null)
			return false;
		int n = id.length();
		if (n == 0)
			return false;
		if (id.equals("#")) //$NON-NLS-1$
			return true;
		for (int i = 0; i < n; i++)
			if (!Character.isJavaIdentifierPart(id.charAt(i)))
				return false;
		return true;
	}

	/**
	 * Sets a label for UI control
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		if (label == null)
			throw new NullPointerException("Label cannot be null"); //$NON-NLS-1$
		this.label = label;
	}

	/**
	 * Sets tooltip for ui control. Not supported now.
	 * 
	 * @param tooltip
	 */
	public void setToolTip(String tooltip) {
		this.toolTip = tooltip;
	}

	/**
	 * Sets uiinfo for ui control. Not supported now.
	 * 
	 * @param uiinfo
	 */
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
	 *        the parent to set
	 */
	public void setParent(IProblemPreference parent) {
		this.parent = parent;
	}

	public String getQualifiedKey() {
		if (parent == null)
			return getKey();
		return parent.getQualifiedKey() + "." + getKey(); //$NON-NLS-1$
	}

	/**
	 * @param tokenizer
	 * @throws IOException
	 */
	public abstract void importValue(StreamTokenizer tokenizer)
			throws IOException;

	public void importValue(String str) {
		StreamTokenizer tokenizer = getImportTokenizer(str);
		try {
			importValue(tokenizer);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(str, e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected String escape(String x) {
		x = x.replaceAll("[\"\\\\]", "\\\\$0"); //$NON-NLS-1$//$NON-NLS-2$
		return "\"" + x + "\""; //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * @param str
	 * @return
	 */
	protected String unescape(String str) {
		StreamTokenizer tokenizer = getImportTokenizer(str);
		try {
			tokenizer.nextToken();
		} catch (IOException e) {
			return null;
		}
		String sval = tokenizer.sval;
		return sval;
	}
}
