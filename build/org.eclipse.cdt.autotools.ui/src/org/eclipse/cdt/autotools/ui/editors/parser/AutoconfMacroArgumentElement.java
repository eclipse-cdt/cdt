/*******************************************************************************
 * Copyright (c) 2008, 2015 Nokia Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ed Swartz (Nokia) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors.parser;

/**
 * This is a macro argument node.  It may also hold
 * other AutoconfMacroElements.  The source range includes any quotes around an argument
 * but the #getName() has them stripped.
 * @author eswartz
 *
 */
public class AutoconfMacroArgumentElement extends AutoconfElement {

	public AutoconfMacroArgumentElement() {
		super(""); //
	}

	public AutoconfMacroArgumentElement(String name) {
		super(name);
	}
}
