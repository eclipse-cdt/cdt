/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;

/**
 * Standard label provider for IStorage objects.
 * Use this class when you want to present IStorage objects in a viewer.
 */
public class StorageLabelProvider extends LabelProvider {

	private IEditorRegistry fEditorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
	private Map<String, Image> fJarImageMap = new HashMap<>(10);
	private Image fDefaultImage;

	/* (non-Javadoc)
	 * @see ILabelProvider#getImage
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof IStorage)
			return getImageForJarEntry((IStorage) element);

		return super.getImage(element);
	}

	/* (non-Javadoc)
	 * @see ILabelProvider#getText
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IStorage)
			return ((IStorage) element).getName();

		return super.getText(element);
	}

	/* (non-Javadoc)
	 *
	 * @see IBaseLabelProvider#dispose
	 */
	@Override
	public void dispose() {
		if (fJarImageMap != null) {
			Iterator<Image> each = fJarImageMap.values().iterator();
			while (each.hasNext()) {
				Image image = each.next();
				image.dispose();
			}
			fJarImageMap = null;
		}
		if (fDefaultImage != null)
			fDefaultImage.dispose();
		fDefaultImage = null;
	}

	/*
	 * Gets and caches an image for a JarEntryFile.
	 * The image for a JarEntryFile is retrieved from the EditorRegistry.
	 */
	private Image getImageForJarEntry(IStorage element) {
		if (fJarImageMap == null)
			return getDefaultImage();

		if (element == null || element.getName() == null)
			return getDefaultImage();

		// Try to find icon for full name
		String name = element.getName();
		Image image = fJarImageMap.get(name);
		if (image != null)
			return image;
		IFileEditorMapping[] mappings = fEditorRegistry.getFileEditorMappings();
		int i = 0;
		while (i < mappings.length) {
			if (mappings[i].getLabel().equals(name))
				break;
			i++;
		}
		String key = name;
		if (i == mappings.length) {
			// Try to find icon for extension
			IPath path = element.getFullPath();
			if (path == null)
				return getDefaultImage();
			key = path.getFileExtension();
			if (key == null)
				return getDefaultImage();
			image = fJarImageMap.get(key);
			if (image != null)
				return image;
		}

		// Get the image from the editor registry
		ImageDescriptor desc = fEditorRegistry.getImageDescriptor(name);
		image = desc.createImage();

		fJarImageMap.put(key, image);

		return image;
	}

	private Image getDefaultImage() {
		if (fDefaultImage == null)
			fDefaultImage = fEditorRegistry.getImageDescriptor((String) null).createImage();
		return fDefaultImage;
	}
}
