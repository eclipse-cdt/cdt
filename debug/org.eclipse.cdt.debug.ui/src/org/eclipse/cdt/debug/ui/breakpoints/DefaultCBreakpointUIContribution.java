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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

class DefaultCBreakpointUIContribution implements ICBreakpointsUIContribution {
    
    private final IConfigurationElement fConfig;
    private String mainElement;
	private String attLabel;
	private String attId;
	private String fieldEditorClassName;
	private String fieldEditorFactoryClass;
	private IFieldEditorFactory fieldEditorFactory;
	private String markerType;
	private String modelId;
	private String attType;
	private Map<String, String> valueLabels = new LinkedHashMap<String, String>();
	private Map<String, String> conditions = new HashMap<String, String>();

	DefaultCBreakpointUIContribution(IConfigurationElement config) {
	    fConfig = config;
	}
	
	
	@Override
	public String getId() {
		return attId;
	}

	@Override
	public String getLabel() {
		return attLabel;
	}

	@Override
	public String getDebugModelId() {
		return modelId;
	}

	@Override
	public String getMainElement() {
	    return mainElement;
	}
	
	static private Class<?>[] fieldSignature = new Class[] { String.class, String.class,
			Composite.class };

	@Override
	public FieldEditor getFieldEditor(String name, String labelText, Composite parent) {
        if (fieldEditorFactory != null) {
            return fieldEditorFactory.createFieldEditor(name, labelText, parent);
        } else if (fieldEditorFactoryClass != null) {
            try {
                fieldEditorFactory = (IFieldEditorFactory) fConfig.createExecutableExtension("fieldEditorFactory"); //$NON-NLS-1$
            } catch (CoreException e) {
                CDebugUIPlugin.log(e);
                return null;
            } 
            return fieldEditorFactory.createFieldEditor(name, labelText, parent);
        } else if (fieldEditorClassName != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<FieldEditor> cclass = (Class<FieldEditor>)Class.forName(fieldEditorClassName);
                Constructor<FieldEditor> constructor = cclass.getConstructor(fieldSignature);
                FieldEditor editor = constructor.newInstance(name, labelText, parent);
                if (editor instanceof ICBreakpointsUIContributionUser) {
                    ((ICBreakpointsUIContributionUser)editor).setContribution(this);
                }
                return editor;
            } catch (Exception e) {
                CDebugUIPlugin.log(e);
                return null;
            }
        } else {
            return new ReadOnlyFieldEditor(name, labelText, parent);
        }
	}

	@Override
	public String getLabelForValue(String value) {
		if (valueLabels.containsKey(value))
			return valueLabels.get(value);
		return value;
	}

	@Override
	public String getMarkerType() {
		return markerType;
	}

	@Override
	public String[] getPossibleValues() {
		Set<String> set = valueLabels.keySet();
		return set.toArray(new String[set.size()]);
	}

	@Override
	public String getType() {
		return attType;
	}

	@Override
	public boolean isApplicable(Map<String, Object> properties) {
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

	public void setMainElement(String mainElement) {
	    this.mainElement = mainElement;
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
	
	public void setFieldEditorFactory(String factoryClass) {
	    fieldEditorFactoryClass = factoryClass;
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

	@Override
	public String getFieldEditorClassName() {
		return fieldEditorClassName;
	}
}