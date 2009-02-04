/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

/**
 * Represents a field declared in a type.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMacro extends ICElement, ISourceManipulation, ISourceReference {
	/**
	 * Returns the Identifier List.
	 * @return String
	 */
	String getIdentifierList();
	/**
	 * Returns the Token Sequence.
	 * @return String
	 */
	String getTokenSequence();
}
