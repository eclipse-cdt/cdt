/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.net.URL;

import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIImages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
 
public class ToolListLabelProvider extends LabelProvider {
	private final Image IMG_TOOL = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_TOOL);
	private final Image IMG_CAT = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CAT);
	private static final String TREE_LABEL = "BuildPropertyPage.label.ToolTree";	//$NON-NLS-1$
	private static final String ERROR_UNKNOWN_ELEMENT = "BuildPropertyPage.error.Unknown_tree_element";	//$NON-NLS-1$

	private ImageDescriptor descriptor = null;
	private ResourceManager manager = null;
	
	/* (non-Javadoc)
	 * Returns the Image associated with the icon information retrieved out of OptionCategory.
	 */
	private Image getIconFromOptionCategory(IOptionCategory cat) {

		Image img = null; 
		URL url = cat.getIconPath();
		
		// Get the image from the URL. 
		if (url != null) {
			descriptor = ImageDescriptor.createFromURL(url);
			manager = JFaceResources.getResources(Display.getCurrent());
			Assert.isNotNull(manager);
			img = manager.createImageWithDefault(descriptor);			
			if (img == null) {
				// Report error by displaying a warning message
				System.err.println("Couldn't create image from URL \"" + url + "\", to display icon for Tool Options." ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return img;
	}

	public Image getImage(Object element) {
		if (!(element instanceof ToolListElement)) {
			throw unknownElement(element);
		}
		Image defaultImage = IMG_CAT;
		ToolListElement toolListElement = (ToolListElement)element;
		IOptionCategory cat = toolListElement.getOptionCategory();
	
		if (cat == null) {
			defaultImage = IMG_TOOL;
			cat = (IOptionCategory)toolListElement.getTool();
		}
		
		if (cat != null) {
			// Return a OptionCategory image for an OptionCategory reference
			Image img = getIconFromOptionCategory(cat);
			
			if (img != null) {
				return img;
			}
		}
		// Use default icon for display 
		return defaultImage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		if (!(element instanceof ToolListElement)) {
			throw unknownElement(element);
		}
		ToolListElement toolListElement = (ToolListElement)element;
		IOptionCategory cat = toolListElement.getOptionCategory();
	
		if (cat == null) {
			ITool tool = toolListElement.getTool();
			return tool.getName();
		}
		else {
			return cat.getName();
		}
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException(ManagedBuilderUIMessages.getFormattedString(ERROR_UNKNOWN_ELEMENT, element.getClass().getName()));
	}

    /**
     * Disposing any images that were allocated for it.
     * 
     * @since 3.0
     */
    public void dispose() {
        if (descriptor != null && manager != null) {
            manager.destroyImage(descriptor);
        }
    };
}
