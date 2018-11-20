/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

/**
 * A common interface for CPPVariable and CPPVariableTemplate.
 */
public interface ICPPInternalDeclaredVariable extends ICPPInternalVariable {
	/**
	 * Informs the variable that all its declarations and definitions have already been added.
	 */
	public void allDeclarationsDefinitionsAdded();
}
