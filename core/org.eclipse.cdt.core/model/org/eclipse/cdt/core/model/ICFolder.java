package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFolder;

/**
 * A C Folder Resource.
 */
public interface ICFolder extends IParent, ICResource, ICElement {

	public IFolder getFolder();
}
