/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Qnx Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ITemplate;

public class Template implements ITemplate {
	protected static final String[] fgEmptyList = {};
	protected String[] fTemplateParameterTypes;
	protected String[] fTemplateArgs;
	protected String fName;

	public Template(String name) {
		fName = name;
		fTemplateParameterTypes = fgEmptyList;
		fTemplateArgs = fgEmptyList;
	}

	/**
	 * Returns the parameterTypes.
	 * @see org.eclipse.cdt.core.model.ITemplate#getTemplateParameterTypes()
	 * @return String[]
	 */
	@Override
	public String[] getTemplateParameterTypes() {
		return fTemplateParameterTypes;
	}

	@Override
	public String[] getTemplateArguments() {
		return fTemplateArgs;
	}

	/**
	 * Sets the parameter types and template arguments.
	 */
	public void setTemplateInfo(String[] templateParameterTypes, String[] args) {
		if (templateParameterTypes != null)
			fTemplateParameterTypes = templateParameterTypes;
		if (args != null) {
			fTemplateArgs = args;
		}
	}

	@Override
	public int getNumberOfTemplateParameters() {
		return fTemplateParameterTypes == null ? 0 : fTemplateParameterTypes.length;
	}

	/**
	 * @see org.eclipse.cdt.core.model.ITemplate#getTemplateSignature()
	 */
	@Override
	public String getTemplateSignature() {
		StringBuilder sig = new StringBuilder(fName);
		if (getNumberOfTemplateParameters() > 0) {
			sig.append("<"); //$NON-NLS-1$
			String[] paramTypes = getTemplateParameterTypes();
			int i = 0;
			sig.append(paramTypes[i++]);
			while (i < paramTypes.length) {
				sig.append(", "); //$NON-NLS-1$
				sig.append(paramTypes[i++]);
			}
			sig.append(">"); //$NON-NLS-1$
		} else {
			sig.append("<>"); //$NON-NLS-1$
		}
		return sig.toString();
	}
}
