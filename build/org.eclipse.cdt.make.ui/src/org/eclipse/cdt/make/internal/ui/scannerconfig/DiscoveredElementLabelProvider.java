/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.ui.scannerconfig;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.cpaths.CPathEntryMessages;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Label provider for DiscoveredElement-s. DiscoveredElement can be active or removed.
 * 
 * @author vhirsl
 */
public class DiscoveredElementLabelProvider extends LabelProvider implements IColorProvider {
	private final Color inDirect = new Color(Display.getDefault(), new RGB(170, 170, 170));

	private ImageDescriptor fIncludeIcon, fMacroIcon, fContainerImage;
	private ImageDescriptor fRemovedIncludeIcon, fRemovedMacroIcon;
	private ImageDescriptorRegistry fRegistry;

	private final String DISABLED_LABEL = MakeUIPlugin.
			getResourceString("ManageScannerConfigDialogCommon.discoveredGroup.annotation.disabled");//$NON-NLS-1$ 
		
	public DiscoveredElementLabelProvider() {
		fRegistry = CUIPlugin.getImageDescriptorRegistry();
		fIncludeIcon = CPluginImages.DESC_OBJS_INCLUDES_FOLDER;
		fMacroIcon = CPluginImages.DESC_OBJS_MACRO;
		fContainerImage = CPluginImages.DESC_OBJS_LIBRARY;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof DiscoveredElement) {
			DiscoveredElement elem = (DiscoveredElement) element;
			Image image = composeImage(elem);
			if (image != null) {
				return image;
			}
		}
		return super.getImage(element);
	}
	/**
	 * @param elem
	 * @return
	 */
	private Image composeImage(DiscoveredElement elem) {
		Image image = null;
		switch (elem.getEntryKind()) {
			case DiscoveredElement.PATHS_GROUP:
				image = CPluginImages.get(CPluginImages.IMG_OBJS_INCLUDES_CONTAINER);
				break;
			case DiscoveredElement.CONTAINER:
				image = fRegistry.get(fContainerImage);
				break;
			case DiscoveredElement.INCLUDE_PATH:
				image = fRegistry.get(fIncludeIcon);
				break;
			case DiscoveredElement.SYMBOLS_GROUP:
			case DiscoveredElement.SYMBOL_DEFINITION:
				image = fRegistry.get(fMacroIcon);
			    break;
		}
		if (image != null && elem.isRemoved()) {
			image = new DiscoveredElementImageDescriptor(image, true).createImage();
		}
		return image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof DiscoveredElement) {
			DiscoveredElement elem = (DiscoveredElement) element;
			switch (elem.getEntryKind()) {
				case DiscoveredElement.PATHS_GROUP:
					return CPathEntryMessages.getString("CPElementLabelProvider.Includes"); //$NON-NLS-1$
				case DiscoveredElement.SYMBOLS_GROUP:
					return CPathEntryMessages.getString("CPElementLabelProvider.PreprocessorSymbols"); //$NON-NLS-1$
				case DiscoveredElement.CONTAINER:
				case DiscoveredElement.INCLUDE_PATH:
				case DiscoveredElement.SYMBOL_DEFINITION:
					return elem.getEntry() + (elem.isRemoved() ? addAnnotation(DISABLED_LABEL) : "");	//$NON-NLS-1$
			}
		}
		return super.getText(element);
	}

	/**
	 * @param annotation
	 * @return
	 */
	private String addAnnotation(String annotation) {
		return " (" + annotation + ")";	//$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof DiscoveredElement) {
			DiscoveredElement elem = (DiscoveredElement) element;
			switch (elem.getEntryKind()) {
				case DiscoveredElement.INCLUDE_PATH:
				case DiscoveredElement.SYMBOL_DEFINITION:
					if (elem.isRemoved()) {
						return inDirect;
					}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return null;
	}

	/**
	 * ComositeImageDescriptor adds 'removed' image overlay to the DiscoveredElement
	 * 
	 * @author vhirsl
	 */
	private class DiscoveredElementImageDescriptor extends CompositeImageDescriptor {
		private Image fBaseImage;
		private boolean showRemoved;
		private Point fSize;

		public DiscoveredElementImageDescriptor(Image baseImage, boolean removed) {
			fBaseImage = baseImage;
			showRemoved = removed;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
		 */
		protected void drawCompositeImage(int width, int height) {
			ImageData bg = fBaseImage.getImageData();
			if (bg == null) {
				bg = DEFAULT_IMAGE_DATA;
			}
			drawImage(bg, 0, 0);
			drawOverlays();
		}

		/**
		 * Add any overlays to the image as specified in the flags.
		 */
		protected void drawOverlays() {
			ImageData data = null;
			if (showRemoved) {
				data = CPluginImages.DESC_OVR_ERROR.getImageData();
				drawImage(data, 0, 0);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
		 */
		protected Point getSize() {
			if (fSize == null) {
				ImageData data = fBaseImage.getImageData();
				fSize = new Point(data.width, data.height);
			}
			return fSize;
		}

	}
}
