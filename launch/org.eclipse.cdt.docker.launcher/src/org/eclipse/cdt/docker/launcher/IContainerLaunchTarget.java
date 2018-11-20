/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.cdt.docker.launcher;

/**
 * @since 1.2
 * @author jjohnstn
 *
 */
public interface IContainerLaunchTarget {

	// Container attributes
	public static final String ATTR_CONNECTION_URI = "connection_uri"; //$NON-NLS-1$
	public static final String ATTR_IMAGE_ID = "image_id"; //$NON-NLS-1$

}
