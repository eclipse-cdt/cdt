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
package org.eclipse.cdt.internal.ui.util;

import java.net.URI;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ide.FileStoreEditorInput;

/**
 * An EditorInput for an external (non-workspace) file.
 */
public final class ExternalEditorInput extends FileStoreEditorInput implements ITranslationUnitEditorInput {
	private final IPath location;
	private final IResource markerResource;
	private ITranslationUnit unit;

	/**
	 * Creates an editor input for an external translation unit.
	 *
	 * @param unit  the translation unit
	 */
	public ExternalEditorInput(ITranslationUnit unit) {
		this(unit.getLocationURI(), unit.getCProject().getProject());
		Assert.isNotNull(unit);
		this.unit = unit;
	}

	/**
	 * Creates an editor input for an external file of the local file system.
	 *
	 * @param location  the file system location
	 */
	public ExternalEditorInput(IPath location) {
		this(URIUtil.toURI(location), null);
	}

	/**
	 * Creates an editor input for an external file of the local file system.
	 *
	 * @param location  the file system location
	 * @param markerResource  the associated marker resource, may be <code>null</code>
	 */
	public ExternalEditorInput(IPath location, IResource markerResource) {
		this(URIUtil.toURI(location), markerResource);
	}

	/**
	 * Creates an editor input for a location URI.
	 *
	 * @param locationURI  the location URI
	 */
	public ExternalEditorInput(URI locationURI) {
		this(locationURI, null);
	}

	/**
	 * Creates an editor input for a location URI.
	 *
	 * @param locationURI  the location URI
	 * @param markerResource  the associated marker resource, may be <code>null</code>
	 */
	public ExternalEditorInput(URI locationURI, IResource markerResource) {
		super(getFileStore(locationURI));
		this.location = URIUtil.toPath(locationURI);
		this.markerResource = markerResource;
	}

	private static IFileStore getFileStore(URI locationURI) {
		try {
			return EFS.getStore(locationURI);
		} catch (CoreException exc) {
			CUIPlugin.log(exc);
		}
		return null;
	}

	@Override
	public ITranslationUnit getTranslationUnit() {
		return unit;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ITranslationUnit.class) && unit != null) {
			return unit;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Returns the resource where markers for this external editor input are stored
	 */
	public IResource getMarkerResource() {
		return markerResource;
	}

	@Override
	public String getFactoryId() {
		if (getPath() != null) {
			return ExternalEditorInputFactory.ID;
		}
		return super.getFactoryId();
	}

	@Override
	public void saveState(IMemento memento) {
		if (getPath() != null) {
			ExternalEditorInputFactory.saveState(memento, this);
		} else {
			super.saveState(memento);
		}
	}

	/*
	 * @see org.eclipse.ui.IPathEditorInput#getPath()
	 * Note: ExternalEditorInput must not implement IPathEditorInput!
	 */
	public IPath getPath() {
		return location;
	}
}
