/***************************************************************************************************
 * Copyright (c) 2008 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.internal.terminal.local.launch.LocalTerminalLaunchUtilities;

/**
 * The class {@link LocalTerminalLaunchLabelProvider} is an {@link ILabelProvider} for lists (or
 * tables) of {@link ILaunchConfiguration}s. It returns a configuration's name as the text label,
 * and the configuration type's regular icon as the image label.
 *
 * @author Mirko Raner
 * @version $Revision: 1.2 $
 */
public class LocalTerminalLaunchLabelProvider extends BaseLabelProvider implements ILabelProvider {

	/**
	 * Creates a new {@link LocalTerminalLaunchLabelProvider}.
	 */
	public LocalTerminalLaunchLabelProvider() {

		super();
	}

	/**
	 * Returns the image for the label of the given element.
	 *
	 * @param element the element for which the image was requested
	 * @return the image, or <code>null</code> if no image could be found
	 *
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {

		if (element instanceof ILaunchConfiguration) {

			return LocalTerminalLaunchUtilities.getImage((ILaunchConfiguration)element);
		}
		return null;
	}

	/**
	 * Returns the text for the label of the given element.
	 *
	 * @param element the element for which to provide the label text
	 * @return the text string used to label the element, or <code>null</code> if there is no text
	 * label for the given object
	 *
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {

		if (element instanceof ILaunchConfiguration) {

			return ((ILaunchConfiguration)element).getName();
		}
		return String.valueOf(element);
	}
}
