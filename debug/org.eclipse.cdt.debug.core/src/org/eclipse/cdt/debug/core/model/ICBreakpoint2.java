/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * Extension that allows client to force breakpoint message to refresh.
 *
 * @since 7.2
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICBreakpoint2 extends ICBreakpoint {
	/**
	 * Refresh the marker message for the breakpoint.
	 * <p>
	 * Many of breakpoint settings are backed by marker attributes and it is
	 * sometimes more convenient to modify those attributes in the marker
	 * directly rather than through accessor methods of the breakpoint.  This
	 * method allows the client to force the breakpoint to refresh its
	 * {@link org.eclipse.core.resources.IMarker#MESSAGE} attribute to reflect
	 * its current attribute values.
	 * </p>
	 *
	 * @throws CoreException if unable to access the property
	 *  on this breakpoint's underlying marker
	 */
	public void refreshMessage() throws CoreException;

	/**
	 * Returns the marker type of the given CDT Breakpoint.
	 * @return marker type ID
	 */
	public String getMarkerType();

	/**
	 * Obtain the combined message from all installed extensions on the
	 * breakpoint. See {@link ICBreakpointExtension#getExtensionMessage}
	 *
	 * @return extension message, or empty-string ({@code ""}) for no message
	 * @since 8.2
	 */
	default public String getExtensionMessage() {
		return ""; //$NON-NLS-1$
	}
}
