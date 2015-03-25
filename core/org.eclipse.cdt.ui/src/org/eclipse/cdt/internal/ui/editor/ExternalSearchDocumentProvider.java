/*******************************************************************************
 * Copyright (c) 2002, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Norbert Ploett (Siemens AG)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.editors.text.ILocationProviderExtension;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;

public class ExternalSearchDocumentProvider extends TextFileDocumentProvider {
	
	public ExternalSearchDocumentProvider() {
		super(new CStorageDocumentProvider());
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createFileInfo(java.lang.Object)
	 */
	@Override
	protected FileInfo createFileInfo(Object element) throws CoreException {
		final FileInfo info= super.createFileInfo(element);
		if (info != null) {
			IAnnotationModel originalModel = info.fModel;
			IAnnotationModel externalSearchModel = createAnnotationModel(element);
			if (externalSearchModel != null) {
				info.fModel= externalSearchModel;
				IAnnotationModel fileBufferModel= info.fTextFileBuffer.getAnnotationModel();
				if (fileBufferModel != null) {
					((AnnotationModel) externalSearchModel).addAnnotationModel("fileBufferModel", fileBufferModel); //$NON-NLS-1$
				}
				if (originalModel != null && originalModel != fileBufferModel) {
					((AnnotationModel) externalSearchModel).addAnnotationModel("originalModel", originalModel); //$NON-NLS-1$
				}
			}
			if (info.fModel != null) {
				setUpSynchronization(info);
			}
		}
		return info;
	}


	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof ExternalEditorInput) {
			return createExternalSearchAnnotationModel((ExternalEditorInput)element);
		}
		if (element instanceof IStorageEditorInput) {
			IStorage storage= ((IStorageEditorInput)element).getStorage();
			if (storage.getFullPath() != null) {
				return createExternalSearchAnnotationModel(storage.getFullPath(), null);
			}
		}
		if (element instanceof IPathEditorInput) {
			IPath location = ((IPathEditorInput) element).getPath();
			if (location != null) {
				return createExternalSearchAnnotationModel(location, null);
			}
		}
		if (element instanceof IAdaptable) {
			IAdaptable adaptable= (IAdaptable) element;
			ILocationProvider provider = adaptable.getAdapter(ILocationProvider.class);
			if (provider != null) {
				IPath location = provider.getPath(element);
				if (location != null) {
					return createExternalSearchAnnotationModel(location, null);
				}
				if (provider instanceof ILocationProviderExtension) {
					ILocationProviderExtension extendedProvider = (ILocationProviderExtension) provider;
					URI uri = extendedProvider.getURI(element);
					location = URIUtil.toPath(uri);
					if (location != null) {
						return createExternalSearchAnnotationModel(location, null);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Create an annotation model for the given {@link ExternalEditorInput}.
	 * 
	 * @param externalInput
	 * @return  a new annotation model for the external editor input
	 */
	private IAnnotationModel createExternalSearchAnnotationModel(ExternalEditorInput externalInput) {
		IPath location = externalInput.getPath();
		if (location != null) {
			return createExternalSearchAnnotationModel(location, null);
		}
		return null;
	}
	
	/**
	 * Create an annotation model for the given file and associated resource marker.
	 * 
	 * @param location  the local file system location
	 * @param markerResource  the resource to retrieve markers from, may be <code>null</code>
	 * @return  a new annotation model for the file
	 */
	private IAnnotationModel createExternalSearchAnnotationModel(IPath location, IResource markerResource) {
		AnnotationModel model= null;
		if (markerResource != null){
			model = new ExternalSearchAnnotationModel(markerResource, location);
		} else {
			// no marker resource available - search workspace root and all project resources (depth one)
			markerResource= CUIPlugin.getWorkspace().getRoot();
			model = new ExternalSearchAnnotationModel(markerResource, location, IResource.DEPTH_ONE);
		}
		return model;
	}

}
