/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.internal.core.dom.NullPDOMProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 *
 */
public class PDOM {

    private static IPDOMProvider pdomProvider;
    
    private static synchronized void initPDOMProvider() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(IPDOMProvider.ID);
		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions.length > 0) {
			// For now just take the first one
			IConfigurationElement[] elements= extensions[0].getConfigurationElements();
			if (elements.length > 0) {
				// For now just take the first provider
				try {
					pdomProvider = (IPDOMProvider)elements[0].createExecutableExtension("class"); //$NON-NLS-1$
					return;
				} catch (CoreException e) {
				}
			}
		}
		
		// Couldn't find one
		pdomProvider = new NullPDOMProvider();
    }
    
    private static IPDOMProvider getPDOMProvider() {
    	if (pdomProvider == null) {
    		initPDOMProvider();
   		}

    	return pdomProvider;
    }

    /**
     * Get the PDOM for the given project.
     * 
     * @param project
     * @return the PDOM for the project
     */
	public static IPDOM getPDOM(IProject project) {
		return getPDOMProvider().getPDOM(project);
	}

	/**
	 * Startup the PDOM. This mainly sets us up to handle model
	 * change events.
	 */
	public static void startup() {
		IElementChangedListener listener = getPDOMProvider().getElementChangedListener();
		if (listener != null) {
			CoreModel.getDefault().addElementChangedListener(listener);
		}
	}

}
