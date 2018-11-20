/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.includebrowser;

import java.util.Objects;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

public class IBFile {
	final public ITranslationUnit fTU;
	final public IIndexFileLocation fLocation;
	final public String fName;

	public IBFile(ITranslationUnit tu) {
		fTU = tu;
		fLocation = IndexLocationFactory.getIFL(tu);
		fName = tu.getElementName();
	}

	public IBFile(ICProject preferredProject, IIndexFileLocation location) throws CModelException {
		fLocation = location;
		ITranslationUnit TU = CoreModelUtil.findTranslationUnitForLocation(location, preferredProject);
		if (TU == null) //for EFS file that might not be on this filesystem
			TU = CoreModelUtil.findTranslationUnitForLocation(location.getURI(), preferredProject);
		fTU = TU;
		String name = fLocation.getURI().getPath();
		fName = name.substring(name.lastIndexOf('/') + 1);
	}

	public IBFile(String name) {
		fName = name;
		fLocation = null;
		fTU = null;
	}

	public IIndexFileLocation getLocation() {
		return fLocation;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IBFile) {
			IBFile file = (IBFile) obj;
			return (Objects.equals(fLocation, file.fLocation) && Objects.equals(fTU, file.fTU));
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fLocation) + 31 * (Objects.hashCode(fTU) + 31 * Objects.hashCode(fName));
	}

	public ITranslationUnit getTranslationUnit() {
		return fTU;
	}

	public IFile getResource() {
		if (fLocation != null) {
			String fullPath = fLocation.getFullPath();
			if (fullPath != null) {
				IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(fullPath);
				if (file instanceof IFile) {
					return (IFile) file;
				}
			}
		}
		return null;
	}

	public String getName() {
		return fName;
	}
}