/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.cdtvariables;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.cdtvariables.CdtVariable;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;

/**
 * This class represents the Build Macro that could be loaded
 * and stored in XML
 *
 * @since 3.0
 *
 */
public class StorableCdtVariable extends CdtVariable {
	public static final String STRING_MACRO_ELEMENT_NAME = "stringMacro"; //$NON-NLS-1$
	public static final String STRINGLIST_MACRO_ELEMENT_NAME = "stringListMacro"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String TYPE = "type"; //$NON-NLS-1$

	public static final String VALUE_ELEMENT_NAME = "value"; //$NON-NLS-1$
	public static final String VALUE_ELEMENT_VALUE = "name"; //$NON-NLS-1$

	public static final String TYPE_TEXT = "VALUE_TEXT"; //$NON-NLS-1$
	public static final String TYPE_TEXT_LIST = "VALUE_TEXT_LIST"; //$NON-NLS-1$
	public static final String TYPE_PATH_FILE = "VALUE_PATH_FILE"; //$NON-NLS-1$
	public static final String TYPE_PATH_FILE_LIST = "VALUE_PATH_FILE_LIST"; //$NON-NLS-1$
	public static final String TYPE_PATH_DIR = "VALUE_PATH_DIR"; //$NON-NLS-1$
	public static final String TYPE_PATH_DIR_LIST = "VALUE_PATH_DIR_LIST"; //$NON-NLS-1$
	public static final String TYPE_PATH_ANY = "VALUE_PATH_ANY"; //$NON-NLS-1$
	public static final String TYPE_PATH_ANY_LIST = "VALUE_PATH_ANY_LIST"; //$NON-NLS-1$

	public StorableCdtVariable(String name, int type, String value) {
		super(name, type, value);
	}

	public StorableCdtVariable(String name, int type, String value[]) {
		super(name, type, value);
	}

	public StorableCdtVariable(ICStorageElement element) {
		load(element);
	}

	private void load(ICStorageElement element) {
		fName = element.getAttribute(NAME);

		fType = typeStringToInt(element.getAttribute(TYPE));

		if (!CdtVariableResolver.isStringListVariable(fType))
			fStringValue = element.getAttribute(VALUE);
		else {
			ICStorageElement nodeList[] = element.getChildren();
			List<String> values = new ArrayList<>();
			for (int i = 0; i < nodeList.length; ++i) {
				ICStorageElement node = nodeList[i];
				if (node.getName().equals(VALUE_ELEMENT_NAME)) {
					values.add(node.getAttribute(VALUE_ELEMENT_VALUE));
				}
			}
			fStringListValue = values.toArray(new String[values.size()]);
		}
	}

	private int typeStringToInt(String typeString) {
		int type;

		if (TYPE_TEXT_LIST.equals(typeString))
			type = VALUE_TEXT_LIST;
		else if (TYPE_PATH_FILE.equals(typeString))
			type = VALUE_PATH_FILE;
		else if (TYPE_PATH_FILE_LIST.equals(typeString))
			type = VALUE_PATH_FILE_LIST;
		else if (TYPE_PATH_DIR.equals(typeString))
			type = VALUE_PATH_DIR;
		else if (TYPE_PATH_DIR_LIST.equals(typeString))
			type = VALUE_PATH_DIR_LIST;
		else if (TYPE_PATH_ANY.equals(typeString))
			type = VALUE_PATH_ANY;
		else if (TYPE_PATH_ANY_LIST.equals(typeString))
			type = VALUE_PATH_ANY_LIST;
		else
			type = VALUE_TEXT;

		return type;
	}

	private String typeIntToString(int type) {
		String stringType;

		switch (type) {
		case VALUE_TEXT_LIST:
			stringType = TYPE_TEXT_LIST;
			break;
		case VALUE_PATH_FILE:
			stringType = TYPE_PATH_FILE;
			break;
		case VALUE_PATH_FILE_LIST:
			stringType = TYPE_PATH_FILE_LIST;
			break;
		case VALUE_PATH_DIR:
			stringType = TYPE_PATH_DIR;
			break;
		case VALUE_PATH_DIR_LIST:
			stringType = TYPE_PATH_DIR_LIST;
			break;
		case VALUE_PATH_ANY:
			stringType = TYPE_PATH_ANY;
			break;
		case VALUE_PATH_ANY_LIST:
			stringType = TYPE_PATH_ANY_LIST;
			break;
		case VALUE_TEXT:
		default:
			stringType = TYPE_TEXT;
			break;
		}

		return stringType;
	}

	public void serialize(ICStorageElement element) {
		if (fName != null)
			element.setAttribute(NAME, fName);

		element.setAttribute(TYPE, typeIntToString(fType));

		if (!CdtVariableResolver.isStringListVariable(fType)) {
			if (fStringValue != null)
				element.setAttribute(VALUE, fStringValue);
		} else {
			if (fStringListValue != null && fStringListValue.length > 0) {
				for (int i = 0; i < fStringListValue.length; i++) {
					ICStorageElement valEl = element.createChild(VALUE_ELEMENT_NAME);
					if (fStringListValue[i] != null)
						valEl.setAttribute(VALUE_ELEMENT_VALUE, fStringListValue[i]);
				}
			}
		}

	}
}
