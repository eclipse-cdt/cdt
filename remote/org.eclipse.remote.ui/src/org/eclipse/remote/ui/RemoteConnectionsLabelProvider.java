/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A label provider to show the test and base image for remote connections.
 * It calls out to the connection type services to get the text and images for
 * the types of the connections.
 * 
 * @since 2.0
 */
public class RemoteConnectionsLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof IRemoteConnection) {
			IRemoteConnectionType type = ((IRemoteConnection) element).getConnectionType();
			IRemoteUIConnectionService uiService = type.getService(IRemoteUIConnectionService.class);
			if (uiService != null) {
				return uiService.getLabelProvider().getText(element);
			} else {
				return ((IRemoteConnection) element).getName();
			}
		} else {
			return super.getText(element);
		}
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IRemoteConnection) {
			IRemoteConnection connection = (IRemoteConnection) element; 
			IRemoteConnectionType type = connection.getConnectionType();
			IRemoteUIConnectionService uiService = type.getService(IRemoteUIConnectionService.class);
			if (uiService != null) {
				final Image baseImage = uiService.getLabelProvider().getImage(element);
				if (connection.isOpen()) {
					return baseImage;
				} else {
					String closedId = "closed." + type.getId(); //$NON-NLS-1$
					Image closedImage = RemoteUIPlugin.getDefault().getImageRegistry().get(closedId);
					if (closedImage == null) {
						final Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
						ImageDescriptor desc = new CompositeImageDescriptor() {
							@Override
							protected Point getSize() {
								Rectangle bounds = baseImage.getBounds();
								return new Point(bounds.width, bounds.height);
							}
							
							@Override
							protected void drawCompositeImage(int width, int height) {
								drawImage(baseImage.getImageData(), 0, 0);
								int y = baseImage.getBounds().height - errorImage.getBounds().height;
								drawImage(errorImage.getImageData(), 0, y);
							}
						};
						closedImage = desc.createImage();
						RemoteUIPlugin.getDefault().getImageRegistry().put(closedId, closedImage);
					}
					return closedImage;
				}
			}
		}
		return super.getImage(element);
	}

}
