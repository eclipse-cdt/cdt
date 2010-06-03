/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Warren Paul (Nokia) - Bug 218266
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.net.URI;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * ExternalTranslationUnit
 */
public class ExternalTranslationUnit extends TranslationUnit {

	public ExternalTranslationUnit(ICElement parent, java.io.File file, String contentTypeID) {
		this(parent, URIUtil.toURI(file.getAbsolutePath()), contentTypeID);
	}

	public ExternalTranslationUnit(ICElement parent, IPath location, String contentTypeID) {
		super(parent, URIUtil.toURI(location), contentTypeID);
	}

	public ExternalTranslationUnit(ICElement parent, URI uri, String contentTypeID) {
		super(parent, uri, contentTypeID);
	}

	/**
	 * A file included from a different project can still belong to a (non-CDT) project
	 */
	public void setResource(IFile file) {
		resource= file;
	}
}
