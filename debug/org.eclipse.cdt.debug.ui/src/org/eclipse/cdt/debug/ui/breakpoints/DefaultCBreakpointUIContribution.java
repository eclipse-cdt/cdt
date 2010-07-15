/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpoints;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.preferences.ReadOnlyFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

class DefaultCBreakpointUIContribution implements ICBreakpointsUIContribution {
	private String attLabel;
	private String attId;
	private String fieldEditorClassName;
	private String markerType;
	private String modelId;
	private String attType;
	private Map<String, String> valueLabels = new LinkedHashMap<String, String>();
	private Map<String, String> conditions = new HashMap<String, String>();

	public String getId() {
		return attId;
	}

	public String getLabel() {
		return attLabel;
	}

	public String getDebugModelId() {
		return modelId;
	}

	static private Class[] fieldSignature = new Class[] { String.class, String.class,
			Composite.class };

	public FieldEditor getFieldEditor(String name, String labelText, Composite parent) {
		String className = fieldEditorClassName;
		if (fieldEditorClassName == null) {
			className = ReadOnlyFieldEditor.class.getName();
		}
		try {
			Class cclass = Class.forName(className);
			Constructor constructor = cclass.getConstructor(fieldSignature);
			FieldEditor editor = (FieldEditor) constructor.newInstance(name, labelText, parent);
			if (editor instanceof ICBreakpointsUIContributionUser) {
				((ICBreakpointsUIContributionUser)editor).setContribution(this);
			}
			return editor;
		} catch (Exception e) {
			// cannot happened, would have happened when loading extension
			CDebugUIPlugin.log(e);
			return null;
		}
	}

	public String getLabelForValue(String value) {
		if (valueLabels.containsKey(value))
			return valueLabels.get(value);
		return value;
	}

	public String getMarkerType() {
		return markerType;
	}

	public String[] getPossibleValues() {
		Set<String> set = valueLabels.keySet();
		return set.toArray(new String[set.size()]);
	}

	public String getType() {
		return attType;
	}

	public boolean isApplicable(Map properties) {
		for (Object key : properties.keySet()) {
			String value = conditions.get(key);
			if (value != null) {
				String realValue = (String) properties.get(key);
				if (!value.equals(realValue)) {
					return false;
				}
			}
		}
		return true;
	}

	public void setLabel(String attLabel) {
		this.attLabel = attLabel;
	}

	public void setId(String attId) {
		this.attId = attId;
	}

	public void setControlClass(String controlClass) {
		this.fieldEditorClassName = controlClass;
	}

	public void setMarkerType(String markerId) {
		this.markerType = markerId;
	}

	public void setDebugModelId(String modelId) {
		this.modelId = modelId;
	}

	public void setType(String attType) {
		this.attType = attType;
	}

	public void addValue(String value, String valueLabel) {
		valueLabels.put(value, valueLabel);
	};

	public void addContionEquals(String property, String value) {
		conditions.put(property, value);
	}

	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub

	}

	public Map<String, String> getConditions() {
		return conditions;
	}

	public void addContionsAll(Map<String, String> conditions2) {
		conditions.putAll(conditions2);
	}

	@Override
	public String toString() {
		return attId + " " + attLabel; //$NON-NLS-1$
	}

	public String getFieldEditorClassName() {
		return fieldEditorClassName;
	}
}