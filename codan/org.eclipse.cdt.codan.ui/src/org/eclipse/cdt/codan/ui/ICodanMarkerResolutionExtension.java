/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Felix Morgner <fmorgner@hsr.ch> - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui;

import org.eclipse.core.resources.IMarker;

/**
 * @since 3.3
 */
public interface ICodanMarkerResolutionExtension extends ICodanMarkerResolution {
	/**
	 * This method will be called before the fix is suggested to the user. It
	 * enables customization based on the problem attached to the marker.
	 *
	 * @param marker
	 */
	public void prepareFor(IMarker marker);
}
