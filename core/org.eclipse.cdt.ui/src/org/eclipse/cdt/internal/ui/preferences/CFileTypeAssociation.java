/**********************************************************************
 * Copyright (c) 2005 QNX Software System and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software System - Initial implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.runtime.content.IContentType;

public class CFileTypeAssociation {

	public CFileTypeAssociation(String spec, int type, IContentType contentType) {
		super();
		fSpec = spec;
		fType = type;
		fContentType = contentType;
	}

	private String fSpec;
	private int fType;
	private IContentType fContentType;

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((fContentType == null) ? 0 : fContentType.getId().hashCode());
		result = PRIME * result + ((fSpec == null) ? 0 : fSpec.hashCode());
		result = PRIME * result + fType;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CFileTypeAssociation other = (CFileTypeAssociation) obj;
		if (!fContentType.getId().equals(other.fContentType.getId())) {
			return false;
		}
		if (!fSpec.equals(other.fSpec)) {
			return false;
		}
		if (fType != other.fType) {
			return false;
		}
		return true;
	}

	public boolean equalsIgnoreCaseOfSpec(CFileTypeAssociation other) {
		if (!fContentType.getId().equals(other.fContentType.getId())) {
			return false;
		}
		if (!fSpec.equalsIgnoreCase(other.fSpec)) {
			return false;
		}
		if (fType != other.fType) {
			return false;
		}
		return true;
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

	public boolean isFileSpec() {
		return (fType & IContentType.FILE_NAME_SPEC) != 0;
	}

	public boolean isExtSpec() {
		return (fType & IContentType.FILE_EXTENSION_SPEC) != 0;
	}

	public boolean isPredefined() {
		return (fType & IContentType.IGNORE_USER_DEFINED) != 0;
	}

	public boolean isUserDefined() {
		return (fType & IContentType.IGNORE_PRE_DEFINED) != 0;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return fContentType.getName();
	}

	/**
	 * Returns {@link IContentType#FILE_NAME_SPEC} or {@link IContentType#FILE_EXTENSION_SPEC}.
	 */
	public int getFileSpecType() {
		return fType & (IContentType.FILE_NAME_SPEC | IContentType.FILE_EXTENSION_SPEC);
	}

}
