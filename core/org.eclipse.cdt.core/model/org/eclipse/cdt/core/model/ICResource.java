/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.core.model;

import org.eclipse.core.resources.IResource;

public interface ICResource extends IParent, ICElement {
	/**
	 * Returns the resource that corresponds directly to this element,
	 * <p>
	 * For example, the corresponding resource for an <code>ATranslationUnit</code>
	 * is its underlying <code>IFile</code>.
	 *
	 * @return the corresponding resource.
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	public IResource getResource() throws CModelException;
}
