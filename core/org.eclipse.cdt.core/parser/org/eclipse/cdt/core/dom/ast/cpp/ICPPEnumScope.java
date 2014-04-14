/*******************************************************************************
 * Copyright (c) 2014 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Interface for enumeration scopes.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 5.7
 */
public interface ICPPEnumScope extends ICPPScope {
	/**
	 * Returns the binding for the enumeration this scope is associated with.
	 */
	ICPPEnumeration getEnumerationType();
}