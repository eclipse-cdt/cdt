/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMTypeReference implements ITypeReference {
	
	private final PDOMName name;
	private final IPath path;
	private ITranslationUnit tu;
	
	public PDOMTypeReference(PDOMName name) {
		this.name = name;
		this.path = new Path(name.getFileLocation().getFileName());
		
		ICElement element = CoreModel.getDefault().create(path);
		if (element instanceof ITranslationUnit)
			tu = (ITranslationUnit)element;
	}

	public ICElement[] getCElements() {
		throw new PDOMNotImplementedError();
	}

	public int getLength() {
		return name.getFileLocation().getNodeLength();
	}

	public IPath getLocation() {
		return path;
	}

	public int getOffset() {
		return name.getFileLocation().getNodeOffset();
	}

	public IPath getPath() {
		return path;
	}

	public IProject getProject() {
		return tu != null ? tu.getUnderlyingResource().getProject() : null;
	}

	public IPath getRelativeIncludePath(IProject project) {
		throw new PDOMNotImplementedError();
	}

	public IPath getRelativePath(IPath relativeToPath) {
		throw new PDOMNotImplementedError();
	}

	public IResource getResource() {
		throw new PDOMNotImplementedError();
	}

	public ITranslationUnit getTranslationUnit() {
		return tu;
	}

	public IWorkingCopy getWorkingCopy() {
		throw new PDOMNotImplementedError();
	}

	public boolean isLineNumber() {
		return name.getFileLocation().getNodeLength() == -1;
	}

}
