/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.ILocationProvider;

/**
 * An EditorInput for an external (non-workspace) file.
 */
public class ExternalEditorInput implements ITranslationUnitEditorInput, IPersistableElement {

	private IStorage externalFile;
	private IResource markerResource;
	private ITranslationUnit unit;
	private IPath location;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof IStorageEditorInput))
			return false;
		IStorageEditorInput other = (IStorageEditorInput) obj;
		try {
			return externalFile.equals(other.getStorage());
		} catch (CoreException exc) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return externalFile.hashCode();
	}

	@Override
	public boolean exists() {
		// External file can not be deleted
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (ILocationProvider.class.equals(adapter)) {
			return (T) this;
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		return registry.getImageDescriptor(externalFile.getFullPath().getFileExtension());
	}

	@Override
	public String getName() {
		return externalFile.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return this;
	}

	@Override
	public IStorage getStorage() {
		return externalFile;
	}

	@Override
	public String getToolTipText() {
		return externalFile.getFullPath().toString();
	}

	@Override
	public ITranslationUnit getTranslationUnit() {
		return unit;
	}

	@Override
	public IPath getPath(Object element) {
		return location;
	}

	public ExternalEditorInput(ITranslationUnit unit, IStorage exFile) {
		this(exFile, exFile.getFullPath());
		this.unit = unit;
		markerResource = unit.getCProject().getProject();
	}

	public ExternalEditorInput(IStorage exFile) {
		this(exFile, exFile.getFullPath());
	}

	public ExternalEditorInput(IStorage exFile, IPath location) {
		externalFile = exFile;
		this.location = location;
	}

	/**
	 * This constructor accepts the storage for the editor
	 * and a reference to a resource which holds the markers for the external file.
	 */
	public ExternalEditorInput(IStorage exFile, IResource markerResource) {
		this(exFile, exFile.getFullPath());
		this.markerResource = markerResource;
	}

	/**
	 * Return the resource where markers for this external editor input are stored
	 */
	public IResource getMarkerResource() {
		return markerResource;
	}

	@Override
	public String getFactoryId() {
		return ExternalEditorInputFactory.ID;
	}

	@Override
	public void saveState(IMemento memento) {
		ExternalEditorInputFactory.saveState(memento, this);
	}

}
