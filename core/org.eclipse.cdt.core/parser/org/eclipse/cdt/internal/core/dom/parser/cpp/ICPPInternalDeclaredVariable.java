/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

public interface ICPPInternalDeclaredVariable extends ICPPInternalVariable {
	/**
	 * Informs the variable that all its declarations and definitions have already been added. 
	 */
	public void allDeclarationsDefinitionsAdded();
}
