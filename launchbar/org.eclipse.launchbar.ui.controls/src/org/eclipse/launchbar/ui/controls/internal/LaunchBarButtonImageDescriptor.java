/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *     Elena Laskavaia - moved to a separate class
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * This class will take two images and create descriptor that will overlay them, mainImage will be centered
 */
public class LaunchBarButtonImageDescriptor extends CompositeImageDescriptor {
	private Image bgImage;
	private Image mainImage;

	/**
	 * @param mainImage - main image, will be centered
	 * @param bgImage - background image
	 */
	public LaunchBarButtonImageDescriptor(Image mainImage, Image bgImage) {
		super();
		this.bgImage = bgImage;
		this.mainImage = mainImage;
	}

	@Override
	protected Point getSize() {
		Rectangle bounds = bgImage.getBounds();
		return new Point(bounds.width - bounds.y, bounds.height - bounds.x);
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(bgImage.getImageData(), 0, 0);
		Rectangle bgBounds = bgImage.getBounds();
		Rectangle modeBounds = mainImage.getBounds();
		int x = ((bgBounds.width - bgBounds.x) - (modeBounds.width - modeBounds.x)) / 2;
		int y = ((bgBounds.height - bgBounds.y) - (modeBounds.height - modeBounds.y)) / 2;
		drawImage(mainImage.getImageData(), x, y);
	}
}
