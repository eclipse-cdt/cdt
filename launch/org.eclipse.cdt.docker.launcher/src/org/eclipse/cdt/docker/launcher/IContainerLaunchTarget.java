/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
