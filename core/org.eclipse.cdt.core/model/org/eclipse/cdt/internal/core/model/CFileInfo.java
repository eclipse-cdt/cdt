package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class CFileInfo extends CResourceInfo {

	/**
	 * Constructs a new C Model Info 
	 */
	protected CFileInfo(CElement element) {
		super(element);
	}

	protected boolean hasChildren() {
		return false;
	}

	public boolean isBinary() {
		return false;
	}

	public boolean isArchive() {
		return false;
	}

	public boolean isTranslationUnit() {
		return (this instanceof TranslationUnitInfo);
	}
}
