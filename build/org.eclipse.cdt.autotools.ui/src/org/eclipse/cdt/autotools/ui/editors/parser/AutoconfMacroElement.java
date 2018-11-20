/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Ed Swartz (NOKIA Inc) - support standalone parser
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.parser;

/**
 * A call to a macro.
 * <p>
 * Macro element now stores arguments as AutoconfMacroElement or AutoconfMacroArgument children
 *
 */
public class AutoconfMacroElement extends AutoconfElement {

	public AutoconfMacroElement(String name) {
		super(name);
	}

	@Override
	public String getVar() {
		if (children.size() > 0)
			return getParameter(0);
		else
			return null;
	}

	public int getParameterCount() {
		return children.size();
	}

	public String getParameter(int num) {
		return children.get(num).getName();
	}

	/**
	 * Check the most recently added child and make sure it is valid.
	 * Children of his class should overwrite this and perform proper
	 * validation.
	 * @param verions Autoconf to be used to validate this macro.
	 * @throws InvalidMacroException
	 */
	public void validate(String version) throws InvalidMacroException {
	}

}
