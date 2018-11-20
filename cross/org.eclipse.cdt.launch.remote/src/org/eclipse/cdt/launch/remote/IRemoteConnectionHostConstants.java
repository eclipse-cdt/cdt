/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Johann Draschwandtner (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/
package org.eclipse.cdt.launch.remote;

/**
 * Constants used for Remote CDT connection properties.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a> team.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since org.eclipse.rse.remotecdt 2.1
 */
public interface IRemoteConnectionHostConstants {

	public static final String PI_REMOTE_CDT = "org.eclipse.cdt.launch.remote.attr"; //$NON-NLS-1$

	public static final String REMOTE_WS_ROOT = "remoteWsRoot"; //$NON-NLS-1$
	public static final String DEFAULT_SKIP_DOWNLOAD = "defaultSkipDownload"; //$NON-NLS-1$
}
