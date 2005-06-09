/**********************************************************************
 * Copyright (c) 2005 QNX Software System and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software System - Initial implementation
***********************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.runtime.content.IContentType;

public class CFileTypeAssociation {

	private String fSpec;
	private int fType;
	private IContentType fContentType;

	/**
	 * @param spec
	 * @param type
	 * @param settings
	 */
	public CFileTypeAssociation(String spec, int type, IContentType contentType) {
		super();
		fSpec = spec;
		fType = type;
		fContentType = contentType;
	}

	/**
	 * @return Returns the fSettings.
	 */
	public IContentType getContentType() {
		return fContentType;
	}

	/**
	 * @return Returns the fSpec.
	 */
	public String getSpec() {
		return fSpec;
	}

	public String getPattern() {
		String pattern = getSpec();
		if (isExtSpec()) {
			return "*." + pattern; //$NON-NLS-1$
		}
		return pattern;
		
	}

	/**
	 * @return
	 */
	public boolean isFileSpec() {
		return (fType & IContentType.FILE_NAME_SPEC) != 0;
	}

	/**
	 * @return
	 */
	public boolean isExtSpec() {
		return (fType & IContentType.FILE_EXTENSION_SPEC) != 0;
	}

	/**
	 * @return
	 */
	public boolean isPredefined() {
		return (fType & IContentType.IGNORE_USER_DEFINED) != 0;
	}

	/**
	 * @return
	 */
	public boolean isUserDefined() {
		return (fType & IContentType.IGNORE_PRE_DEFINED) != 0;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return fContentType.getName();
	}

}
