/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;

/**
 * The PersistableCElementFactory is used to save and recreate an ICElement object.
 * As such, it implements the IPersistableElement interface for storage
 * and the IElementFactory interface for recreation.
 *
 * @see IMemento
 * @see IPersistableElement
 * @see IElementFactory
 */
public class PersistableCElementFactory implements IElementFactory, IPersistableElement {

    // These persistence constants are stored in XML.  Do not
    // change them.
    private static final String TAG_PATH = "path";//$NON-NLS-1$
    private static final String TAG_TYPE = "type";//$NON-NLS-1$

    private static final String FACTORY_ID = "org.eclipse.cdt.ui.PersistableCElementFactory";//$NON-NLS-1$

    // IPersistable data.
    private ICElement fCElement;

    /**
     * Create a PersistableCElementFactory.  This constructor is typically used
     * for our IElementFactory side.
     */
    public PersistableCElementFactory() {
    }

    /**
     * Create a PersistableCElementFactory.  This constructor is typically used
     * for our IPersistableElement side.
     */
    public PersistableCElementFactory(ICElement input) {
        fCElement = input;
    }

    /**
     * @see IElementFactory
     */
    @Override
	public IAdaptable createElement(IMemento memento) {
        // Get the file name.
        String fileName = memento.getString(TAG_PATH);
        if (fileName == null) {
			return null;
		}

        IPath elementPath= new Path(fileName);
        fCElement = CoreModel.getDefault().create(elementPath);
        if (fCElement != null && fCElement.getResource() != null) {
        	IResource resource= fCElement.getResource();
        	if (!resource.isAccessible()) {
        		return resource;
        	}
        }
        if (fCElement != null) {
        	return fCElement;
        }

        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        Integer elementType= memento.getInteger(TAG_TYPE);
		if (elementType == null) {
			if (elementPath.segmentCount() == 1) {
				return root.getProject(fileName);
			}
        	IFolder folder= root.getFolder(elementPath);
        	File osFile= folder.getLocation().toFile();
        	if (osFile.isDirectory()) {
        		return folder;
        	}
        	return root.getFile(elementPath);
        }
		switch (elementType.intValue()) {
		case IResource.ROOT:
			return root;
		case IResource.PROJECT:
			return root.getProject(fileName);
		case IResource.FOLDER:
			return root.getFolder(elementPath);
		case IResource.FILE:
			return root.getFile(elementPath);
		}
        return null;
    }

    /**
     * @see IPersistableElement
     */
    @Override
	public String getFactoryId() {
        return FACTORY_ID;
    }

    /**
     * @see IPersistableElement
     */
    @Override
	public void saveState(IMemento memento) {
    	if (fCElement.getResource() != null) {
	        memento.putString(TAG_PATH, fCElement.getResource().getFullPath().toString());
	        memento.putInteger(TAG_TYPE, fCElement.getResource().getType());
    	}
    }
}
