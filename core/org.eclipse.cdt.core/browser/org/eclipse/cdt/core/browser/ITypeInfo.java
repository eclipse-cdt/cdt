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
package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * Type information.
 */
public interface ITypeInfo {

	/**
	 * Gets the CElement type.
	 */
	public int getType();

	/**
	 * Gets the type name.
	 */
	public String getName();

	/**
	 * Gets the enclosing type names.
	 */
	public String[] getEnclosingNames();

	/**
	 * Gets the resource where type is located.
	 */
	public IResource getResource();
	
	/**
	 * Gets the relative path where type is located.
	 */
	public IPath getPath();

	/**
	 * Gets the absolute path where type is located.
	 */
	public IPath getLocation();

	/**
	 * Gets the start offset of type position.
	 */
	public int getStartOffset();
	
	/**
	 * Gets the end offset of type position.
	 */
	public int getEndOffset();

	/**
	 * Returns true if type is enclosed in the given scope
	 */
	public boolean isEnclosed(ICSearchScope scope);

	/**
	 * Gets the filename where this type is located.
	 */
	public String getFileName();
	
	/**
	 * Gets the fully qualified type container name: Includes enclosing type names, but
	 * not filename. Identifiers are separated by colons.
	 */
	public String getParentName();
	
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

	/** Gets the include path for this type.
	 * 
	 * @param cProject the C Project to use as a reference.
	 * @return The path to this type, relative to the longest
	 * matching include path in the given project, or
	 * <code>null</code> if not found.
	 */
	public IPath resolveIncludePath(ICProject cProject);
}
