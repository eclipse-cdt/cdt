/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
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

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;

public class ExternalSearchDocumentProvider extends CStorageDocumentProvider {
	
	/** Location attribute for breakpoints. See <code>ICBreakpoint.SOURCE_HANDLE</code> */
	private static final String DEBUG_SOURCE_HANDLE = "org.eclipse.cdt.debug.core.sourceHandle"; //$NON-NLS-1$	

	public ExternalSearchDocumentProvider(){
		super();
	}

	/*
	 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#createAnnotationModel(java.lang.Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof ExternalEditorInput) {
			return createExternalSearchAnnotationModel((ExternalEditorInput)element);
		}
		if (element instanceof IStorageEditorInput) {
			IStorage storage= ((IStorageEditorInput)element).getStorage();
			if (storage.getFullPath() != null) {
				return createExternalSearchAnnotationModel(storage, null);
			}
		}
		if (element instanceof IPathEditorInput) {
			IPath path= ((IPathEditorInput)element).getPath();
			IStorage storage= new FileStorage(path);
			return createExternalSearchAnnotationModel(storage, null);
		}
		if (element instanceof IAdaptable) {
			IAdaptable adaptable= (IAdaptable) element;
			ILocationProvider provider= (ILocationProvider) adaptable.getAdapter(ILocationProvider.class);
			if (provider != null) {
				IPath path= provider.getPath(element);
				IStorage storage= new FileStorage(path);
				return createExternalSearchAnnotationModel(storage, null);
			}
		}
		return super.createAnnotationModel(element);
	}

	/**
	 * Create an annotation model for the given {@link ExternalEditorInput}.
	 * 
	 * @param externalInput
	 * @return  a new annotation model for the external editor input
	 */
	private IAnnotationModel createExternalSearchAnnotationModel(ExternalEditorInput externalInput) {
		IStorage storage = externalInput.getStorage();
		IResource markerResource = externalInput.getMarkerResource();
		return createExternalSearchAnnotationModel(storage, markerResource);
	}
	
	/**
	 * Create an annotation model for the given file and associated resource marker.
	 * 
	 * @param storage  the file in the form of a <code>IStorage</code>
	 * @param markerResource  the resource to retrieve markers from, may be <code>null</code>
	 * @return  a new annotation model for the file
	 */
	private IAnnotationModel createExternalSearchAnnotationModel(IStorage storage, IResource markerResource) {
		AnnotationModel model= null;
		if (markerResource != null){
			model = new ExternalSearchAnnotationModel(markerResource, storage);
		} else {
			// no marker resource available - search workspace root and all project resources (depth one)
			markerResource= CUIPlugin.getWorkspace().getRoot();
			model = new ExternalSearchAnnotationModel(markerResource, storage, IResource.DEPTH_ONE);
		}
		// attach annotation model for C breakpoints
		model.addAnnotationModel("debugMarkerModel", new ExternalSearchAnnotationModel(markerResource, storage, IResource.DEPTH_ZERO, DEBUG_SOURCE_HANDLE)); //$NON-NLS-1$
		return model;
	}

}
