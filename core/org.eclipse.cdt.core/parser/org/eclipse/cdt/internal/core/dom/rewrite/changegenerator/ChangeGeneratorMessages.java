/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import org.eclipse.osgi.util.NLS;

/**
 * External strings for the change generator.
 * @since 5.0
 */
public class ChangeGeneratorMessages extends NLS {
	public static String ChangeGenerator_compositeChange;
	public static String ChangeGenerator_group;

	static {
		// Initialize resource bundle
		NLS.initializeMessages(ChangeGeneratorMessages.class.getName(), ChangeGeneratorMessages.class);
	}

	private ChangeGeneratorMessages() {
	}
}
