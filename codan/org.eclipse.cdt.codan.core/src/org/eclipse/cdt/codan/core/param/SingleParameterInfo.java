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

/**
 * ParameterInfo representing a single checker parameter
 * 
 */
public class SingleParameterInfo extends AbstractProblemParameterInfo {
	protected String key = PARAM;
	protected String label;
	protected String toolTip = null;
	protected ParameterType type = ParameterType.TYPE_STRING;
	protected String uiInfo;

	/**
	 * Generate an info with given key and label
	 * 
	 * @param key
	 *            - property id (use in actual property hash of a checker)
	 * @param label
	 *            - label to be shown to user
	 * @param type
	 *            - parameter type
	 * @return
	 */
	public SingleParameterInfo(String key, String label, ParameterType type) {
		if (key == null)
			throw new NullPointerException("key"); //$NON-NLS-1$
		if (type == null)
			throw new NullPointerException("type"); //$NON-NLS-1$
		setKey(key);
		setLabel(label);
		setType(type);
	}

	/**
	 * Generate an info with given key and label
	 * 
	 * @param key
	 *            - property id (use in actual property hash of a checker)
	 * @param label
	 *            - label to be shown to user
	 * @return
	 */
	public SingleParameterInfo(String key, String label) {
		setKey(key);
		setLabel(label);
	}

	/**
	 * Generate an info with given label, default key PARAM would be as a key
	 * 
	 * @param label
	 *            - label to be shown to user
	 * @return
	 */
	public SingleParameterInfo(String label) {
		setLabel(label);
	}

	@Override
	public ParameterType getType() {
		return type;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getToolTip() {
		return toolTip;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getUiInfo() {
		return uiInfo;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setToolTip(String tooltip) {
		this.toolTip = tooltip;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	public void setUiInfo(String uiinfo) {
		this.uiInfo = uiinfo;
	}
}
