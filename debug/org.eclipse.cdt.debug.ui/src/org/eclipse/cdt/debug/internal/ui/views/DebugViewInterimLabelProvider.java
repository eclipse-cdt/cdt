/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views; 

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * A label provider that provide interim text labels. When queried for text, the label provider
 * returns a default string ("<pending...>"). When queried for images, the label provider
 * queries a debug model presentation. This label provider is intended to be passed to
 * a <code>DebugViewDecoratingLabelProvider</code>.
 */
public class DebugViewInterimLabelProvider implements ILabelProvider, IColorProvider, IFontProvider {

	/**
	 * The text label returned by this label provider (value: "...").
	 */
	public final static String PENDING_LABEL= "..."; //$NON-NLS-1$
	
	/**
	 * The debug model presentation used for computing images.
	 */
	protected IDebugModelPresentation presentation;

	/**
	 * Creates a new interim label provider with the given model presentation.
	 * 
	 * @param presentation the model presentation to use for computing images
	 */
	public DebugViewInterimLabelProvider(IDebugModelPresentation presentation) {
		this.presentation= presentation;
	}
	
	/**
	 * Returns the debug model presentation used by this label provider
	 * to compute images.
	 * 
	 * @return this label provider's model presentation
	 */
	public IDebugModelPresentation getPresentation() {
		return presentation;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return presentation.getImage(element);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return DebugViewInterimLabelProvider.PENDING_LABEL;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		presentation.addListener(listener);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		presentation.dispose();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return presentation.isLabelProperty(element, property);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		presentation.removeListener(listener);
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {
        if (presentation instanceof IColorProvider) {
            IColorProvider colorProvider = (IColorProvider) presentation;
            return colorProvider.getForeground(element);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public Color getBackground(Object element) {
        if (presentation instanceof IColorProvider) {
            IColorProvider colorProvider = (IColorProvider) presentation;
            return colorProvider.getBackground(element);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element) {
        if (presentation instanceof IFontProvider) {
            IFontProvider fontProvider = (IFontProvider) presentation;
            return fontProvider.getFont(element);
        }
        return null;
    }
}
