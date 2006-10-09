/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
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
    public IAdaptable createElement(IMemento memento) {
        // Get the file name.
        String fileName = memento.getString(TAG_PATH);
        if (fileName == null) {
			return null;
		}

        fCElement = CoreModel.getDefault().create(new Path(fileName));
        if (fCElement != null && fCElement.getResource() != null) {
        	IResource resource= fCElement.getResource();
        	if (!resource.isAccessible()) {
        		return resource;
        	}
        }
        return fCElement;
    }

    /**
     * @see IPersistableElement
     */
    public String getFactoryId() {
        return FACTORY_ID;
    }

    /**
     * @see IPersistableElement
     */
    public void saveState(IMemento memento) {
    	if (fCElement.getResource() != null) {
	        memento.putString(TAG_PATH, fCElement.getResource().getFullPath().toString());
    	}
    }
}
