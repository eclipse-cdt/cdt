/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.ui.dialogs.messages"; //$NON-NLS-1$
	public static String CacheSizeBlock_absoluteLimit;
	public static String CacheSizeBlock_cacheLimitGroup;
	public static String CacheSizeBlock_headerFileCache;
	public static String CacheSizeBlock_indexDatabaseCache;
	public static String CacheSizeBlock_limitRelativeToMaxHeapSize;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
