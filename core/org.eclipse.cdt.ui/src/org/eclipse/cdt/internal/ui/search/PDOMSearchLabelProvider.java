/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchLabelProvider extends LabelProvider {

	public Image getImage(Object element) {
		ImageDescriptor imageDescriptor = null;
		if (element instanceof IProject) {
			imageDescriptor = CPluginImages.DESC_OBJS_SEARCHHIERPROJECT;
		} else if (element instanceof IFolder) {
			imageDescriptor = CPluginImages.DESC_OBJS_SEARCHHIERFODLER;
		} else if (element instanceof IFile) {
			imageDescriptor = CPluginImages.DESC_OBJS_TUNIT;
		} else if (element instanceof PDOMName) {
			imageDescriptor = CPluginImages.DESC_OBJS_VARIABLE;
		} else if (element instanceof String) {
			// external path, likely a header file
			imageDescriptor = CPluginImages.DESC_OBJS_TUNIT_HEADER;
		}
		
		if (imageDescriptor != null) {
			return CUIPlugin.getImageDescriptorRegistry().get( imageDescriptor );
		} else
			return super.getImage(element);
	}

	public String getText(Object element) {
		if (element instanceof IResource) {
			return ((IResource)element).getName();
		} else if (element instanceof PDOMName) {
			PDOMName name = (PDOMName)element;
			return new String(name.toCharArray())
				+ " (" + name.getFileName() + ")";
		} else {
			return super.getText(element);
		}
		
	}

}
