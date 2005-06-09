package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.WorkingCopy;
import org.eclipse.core.runtime.CoreException;

public class CFileElementWorkingCopy extends WorkingCopy {

	ITranslationUnit unit;


	/**
	 * Creates a working copy of this element
	 */
	public CFileElementWorkingCopy(ITranslationUnit unit) throws CoreException {
		super(unit.getParent(), unit.getPath(), unit.getContentTypeId(), null);
		this.unit = unit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IOpenable#getBuffer()
	 */
	public IBuffer getBuffer() throws CModelException {
		return unit.getBuffer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.IWorkingCopy#getOriginalElement()
	 */
	public ITranslationUnit getOriginalElement() {
		return unit;
	}

}
