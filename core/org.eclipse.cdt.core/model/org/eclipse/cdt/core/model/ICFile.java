package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;

/**
 * A C File Resource.
 */
public interface ICFile extends IParent, ICResource, ICElement {

	public boolean isBinary();

	public boolean isArchive();

	public boolean isTranslationUnit();

	public IFile getFile();
}
