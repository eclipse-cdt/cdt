/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.p2.internal.repo.artifact;

import org.eclipse.osgi.util.NLS;

/**
 * @author DSchaefe
 *
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.p2.internal.repo.artifact.messages"; //$NON-NLS-1$

	public static String io_failedRead;
	public static String io_incompatibleVersion;
	public static String io_parseError;
	
	static {
		// initialize resource bundles
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Do not instantiate
	}

}
