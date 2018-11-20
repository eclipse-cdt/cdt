/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
