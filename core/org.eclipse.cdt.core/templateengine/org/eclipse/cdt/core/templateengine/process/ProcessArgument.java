/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.w3c.dom.Element;

/**
 * ProcessArgument class responsible for constructing process Arguments  by taking info from Template.
 */
public class ProcessArgument {

	static final String ELEM_NAME = "name"; //$NON-NLS-1$
	private static final String ELEM_VALUE = "value"; //$NON-NLS-1$
	private static final String ELEM_ELEMENT = "element"; //$NON-NLS-1$
	private static final String ELEM_SIMPLE = "simple"; //$NON-NLS-1$
	private static final String ELEM_SIMPLE_ARRAY = "simple-array"; //$NON-NLS-1$
	private static final String ELEM_COMPLEX = "complex"; //$NON-NLS-1$
	private static final String ELEM_COMPLEX_ARRAY = "complex-array"; //$NON-NLS-1$

	private String name;
	private byte type;

	private String simpleValue;
	private String[] simpleValueArray;
	private ProcessArgument[] complexValue;
	private ProcessArgument[][] complexValueArray;

	private String resolvedSimpleValue;
	private String[] resolvedSimpleValueArray;

	private TemplateCore template;

	private Set<String> macros;
	private boolean resolved;
	private ProcessParameter externalParam;

	/**
	 * constructor
	 * @param template
	 * @param elem
	 */
	public ProcessArgument(TemplateCore template, Element elem) {
		this.template = template;
		this.name = elem.getAttribute(ELEM_NAME);
		String elemName = elem.getNodeName();
		if (elemName.equals(ELEM_SIMPLE)) {
			type = ProcessParameter.SIMPLE;
			simpleValue = elem.getAttribute(ELEM_VALUE);
			collectMacros(simpleValue);
		} else if (elemName.equals(ELEM_SIMPLE_ARRAY)) {
			type = ProcessParameter.SIMPLE_ARRAY;
			List<Element> valueElements = TemplateEngine.getChildrenOfElementByTag(elem, ELEM_ELEMENT);
			simpleValueArray = new String[valueElements.size()];
			for (int i = 0, l = valueElements.size(); i < l; i++) {
				simpleValueArray[i] = (valueElements.get(i)).getAttribute(ELEM_VALUE);
				collectMacros(simpleValueArray[i]);
			}
		} else if (elemName.equals(ELEM_COMPLEX)) {
			type = ProcessParameter.COMPLEX;
			List<Element> children = TemplateEngine.getChildrenOfElement(elem);
			complexValue = new ProcessArgument[children.size()];
			for (int i = 0, l = children.size(); i < l; i++) {
				complexValue[i] = new ProcessArgument(template, children.get(i));
				Set<String> subMacros = complexValue[i].getMacros();
				if (macros == null) {
					macros = new HashSet<>();
				}
				if (subMacros != null) {
					macros.addAll(subMacros);
				}
			}
		} else if (elemName.equals(ELEM_COMPLEX_ARRAY)) {
			type = ProcessParameter.COMPLEX_ARRAY;
			List<Element> valueElements = TemplateEngine.getChildrenOfElementByTag(elem, ELEM_ELEMENT);
			complexValueArray = new ProcessArgument[valueElements.size()][];

			for (int i = 0, l = valueElements.size(); i < l; i++) {
				List<Element> children = TemplateEngine.getChildrenOfElement(valueElements.get(i));
				complexValueArray[i] = new ProcessArgument[children.size()];
				for (int j = 0, l2 = children.size(); j < l2; j++) {
					complexValueArray[i][j] = new ProcessArgument(template, children.get(j));
					Set<String> subMacros = complexValueArray[i][j].getMacros();
					if (subMacros != null) {
						if (macros == null) {
							macros = new HashSet<>();
						}
						macros.addAll(subMacros);
					}
				}
			}
		}
	}

	/**
	 * Creates an <i>external</i> argument. This is not read from the template descriptor.
	 * @param param The ProcessParameter whose replacement this argument is in the Process call
	 */
	public ProcessArgument(TemplateCore template, ProcessParameter param) {
		this.template = template;
		name = param.getName();
		type = param.getType();
		macros = new HashSet<>();
		macros.add(name);
		simpleValue = ProcessHelper.getReplaceMarker(name);
		this.externalParam = param;
	}

	/**
	 * Adds the macros based on the value.
	 * @param value
	 */
	private void collectMacros(String value) {
		if (value == null) {
			return;
		}
		if (macros == null) {
			macros = new HashSet<>();
		}
		macros.addAll(ProcessHelper.getReplaceKeys(value));
	}

	/**
	 * Returns Parameter name.
	 * @return   parameter name as String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the Parameter Type
	 * @return the Parmeter Type as String
	 */
	public byte getParameterType() {
		return type;
	}

	/**
	 * Returns the Simple Value.
	 * @return   String,
	 */
	public String getSimpleValue() {
		return resolved ? resolvedSimpleValue : simpleValue;
	}

	/**
	 * Returns the Simple Array Values.
	 * @return String Array.
	 */
	public String[] getSimpleArrayValue() {
		return resolved ? resolvedSimpleValueArray : simpleValueArray;
	}

	/**
	 * Returns Process Arguments
	 */
	public ProcessArgument[] getComplexValue() {
		return complexValue;
	}

	/**
	 * Returns Process Arguments
	 */
	public ProcessArgument[][] getComplexArrayValue() {
		return complexValueArray;
	}

	/**
	 * Check for parameter type.
	 * @param param
	 * @return boolean
	 */
	public boolean isOfParameterType(ProcessParameter param) {
		if (param.getType() != type || !param.getName().equals(name)) {
			return false;
		}
		switch (type) {
		case ProcessParameter.SIMPLE:
			return simpleValue != null || param.isNullable();
		case ProcessParameter.SIMPLE_ARRAY:
			return true;
		case ProcessParameter.COMPLEX:
			ProcessParameter[] params = param.getComplexChildren();
			if (params.length != complexValue.length) {
				return false;
			}
			for (int i = 0; i < complexValue.length; i++) {
				if (!complexValue[i].isOfParameterType(params[i])) {
					return false;
				}
			}
			return true;
		case ProcessParameter.COMPLEX_ARRAY:
			params = param.getComplexChildren();
			for (int i = 0; i < complexValueArray.length; i++) {
				ProcessArgument[] cValue = complexValueArray[i];
				if (params.length != cValue.length) {
					return false;
				}
				for (int j = 0; j < cValue.length; j++) {
					if (!cValue[j].isOfParameterType(params[j])) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @return true if All macros are Expandable.
	 */
	public boolean areAllMacrosExpandable() {
		switch (type) {
		case ProcessParameter.SIMPLE:
		case ProcessParameter.SIMPLE_ARRAY:
			// the code for these cases is in this form as
			// fall-through generates warning, with no way to
			// suppress pre-java 1.5
			if (type == ProcessParameter.SIMPLE && externalParam != null) {
				return externalParam.isNullable() || template.getValueStore().get(name) != null;
			}
			if (macros == null || macros.size() == 0) {
				return true;
			}
			Map<String, String> valueStore = template.getValueStore();
			for (String macro : macros) {
				if (valueStore.get(macro) == null) {
					return false;
				}
			}
			return true;
		case ProcessParameter.COMPLEX:
			for (int i = 0; i < complexValue.length; i++) {
				ProcessArgument arg = complexValue[i];
				if (!arg.areAllMacrosExpandable()) {
					return false;
				}
			}
			return true;
		case ProcessParameter.COMPLEX_ARRAY:
			for (int i = 0; i < complexValueArray.length; i++) {
				ProcessArgument[] cValue = complexValueArray[i];
				for (int j = 0; j < cValue.length; j++) {
					ProcessArgument arg = cValue[j];
					if (!arg.areAllMacrosExpandable()) {
						return false;
					}
				}
			}
			return true;
		}
		return true;
	}

	/**
	 * Returns the First Non-expandable Macro.
	 */
	public String getFirstNonExpandableMacro() {
		switch (type) {
		case ProcessParameter.SIMPLE:
		case ProcessParameter.SIMPLE_ARRAY:
			if (macros == null || macros.size() == 0) {
				return null;
			}
			Map<String, String> valueStore = template.getValueStore();
			for (String macro : macros) {
				if (valueStore.get(macro) == null) {
					return macro;
				}
			}
			return null;
		case ProcessParameter.COMPLEX:
			String macro;
			for (int i = 0; i < complexValue.length; i++) {
				ProcessArgument arg = complexValue[i];
				if ((macro = arg.getFirstNonExpandableMacro()) != null) {
					return macro;
				}
			}
			return null;
		case ProcessParameter.COMPLEX_ARRAY:
			for (int i = 0; i < complexValueArray.length; i++) {
				ProcessArgument[] cValue = complexValueArray[i];
				for (int j = 0; j < cValue.length; j++) {
					ProcessArgument arg = cValue[j];
					if ((macro = arg.getFirstNonExpandableMacro()) != null) {
						return macro;
					}
				}
			}
			return null;
		}
		return null;
	}

	/**
	 * @return the macros defined in the context of this argument
	 */
	public Set<String> getMacros() {
		return macros;
	}

	/**
	 * resolve
	 *
	 */
	public void resolve() {
		Map<String, String> valueStore = template.getValueStore();
		switch (type) {
		case ProcessParameter.SIMPLE:
			if (externalParam != null) {
				resolvedSimpleValue = template.getValueStore().get(name);
			} else {
				resolvedSimpleValue = simpleValue;
				if (macros != null && !macros.isEmpty()) {
					resolvedSimpleValue = ProcessHelper.getValueAfterExpandingMacros(resolvedSimpleValue, macros,
							valueStore);
				}
			}
			break;
		case ProcessParameter.SIMPLE_ARRAY:
			resolvedSimpleValueArray = simpleValueArray;
			if (macros != null && !macros.isEmpty()) {
				for (int i = 0; i < resolvedSimpleValueArray.length; i++) {
					resolvedSimpleValueArray[i] = ProcessHelper
							.getValueAfterExpandingMacros(resolvedSimpleValueArray[i], macros, valueStore);
				}
			}
			break;
		case ProcessParameter.COMPLEX:
			for (int i = 0; i < complexValue.length; i++) {
				ProcessArgument arg = complexValue[i];
				arg.resolve();
			}
			break;
		case ProcessParameter.COMPLEX_ARRAY:
			for (int i = 0; i < complexValueArray.length; i++) {
				ProcessArgument[] cValue = complexValueArray[i];
				for (int j = 0; j < cValue.length; j++) {
					ProcessArgument arg = cValue[j];
					arg.resolve();
				}
			}
			break;
		}
		resolved = true;
	}

	/**
	 * Checks whether the process argument has resolved.
	 * @return  boolean, true if resolved.
	 */
	public boolean isResolved() {
		return resolved;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(name);
		b.append(":"); //$NON-NLS-1$
		switch (type) {
		case ProcessParameter.SIMPLE:
			return b.append(getSimpleValue()).toString();
		case ProcessParameter.SIMPLE_ARRAY:
			b.append("{"); //$NON-NLS-1$
			String[] strings = getSimpleArrayValue();
			for (int i = 0; i < strings.length; i++) {
				b.append(strings[i]).append(", "); //$NON-NLS-1$
			}
			if (b.charAt(b.length() - 1) == ' ') {
				b.replace(b.length() - 2, b.length(), "}"); //$NON-NLS-1$
			} else {
				b.append("}"); //$NON-NLS-1$
			}
			return b.toString();
		case ProcessParameter.COMPLEX:
			b.append("{"); //$NON-NLS-1$
			ProcessArgument[] args = getComplexValue();
			for (int i = 0; i < args.length; i++) {
				ProcessArgument arg = args[i];
				b.append(arg).append(", "); //$NON-NLS-1$
			}
			if (b.charAt(b.length() - 1) == ' ') {
				b.replace(b.length() - 2, b.length(), "}"); //$NON-NLS-1$
			} else {
				b.append("}"); //$NON-NLS-1$
			}
			return b.toString();
		case ProcessParameter.COMPLEX_ARRAY:
			b.append("{"); //$NON-NLS-1$
			ProcessArgument[][] argssCA = getComplexArrayValue();
			for (int i = 0; i < argssCA.length; i++) {
				ProcessArgument[] argsCA = argssCA[i];
				b.append("{"); //$NON-NLS-1$
				for (int j = 0; j < argsCA.length; j++) {
					ProcessArgument arg = argsCA[j];
					b.append(arg).append(", "); //$NON-NLS-1$
				}
				if (b.charAt(b.length() - 1) == ' ') {
					b.replace(b.length() - 2, b.length(), "}, "); //$NON-NLS-1$
				} else {
					b.append("}, "); //$NON-NLS-1$
				}
			}
			if (b.charAt(b.length() - 1) == ' ') {
				b.replace(b.length() - 2, b.length(), "}"); //$NON-NLS-1$
			} else {
				b.append("}"); //$NON-NLS-1$
			}
			return b.toString();
		}
		return ""; //$NON-NLS-1$
	}
}
