/*******************************************************************************
 * Copyright (c) 2008, 2009 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		super(""); // //$NON-NLS-N$
	}
	public AutoconfMacroArgumentElement(String name) {
		super(name);
	}
	public String getVar() {
		return super.getVar();
	}
}
