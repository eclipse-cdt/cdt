/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.browser.typeinfo;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;

/**
 * Type information.
 */
public interface ITypeInfo extends IMatch {
	
	/**
	 * Returns true if type is enclosed in the given scope
	 */
	public boolean isEnclosed(ICSearchScope scope);

	/**
	 * Returns true if the type is a low-level system type.
	 * e.g. __FILE
	 */
	public boolean isSystemType();

	/**
	 * Gets the enclosing type names.
	 */
	public String[] getEnclosingNames();

	/**
	 * Gets the filename where this type is located.
	 */
	public String getFileName();
	
	/**
	 * Gets the file path where this type is located.
	 */
	public String getFilePath();
	
	/**
	 * Gets the extension of the file where this type is located.
	 */
	public String getFileExtension();
	
	/**
	 * Gets the type qualified name: Includes enclosing type names, but
	 * not filename. Identifiers are separated by colons.
	 */
	public String getQualifiedName();

	/**
	 * Gets the fully qualified type container name: Filename or
	 * enclosing type name with filename.
	 * All identifiers are separated by colons.
	 */
	public String getQualifiedParentName();
	
	/**
	 * Gets the fully qualified type name: Includes enclosing type names and
	 * filename. All identifiers are separated by colons.
	 */
	public String getFullyQualifiedName();
	
	/**
	 * Gets the CElement which corresponds to this type.
	 */
	public ICElement getCElement();
}
