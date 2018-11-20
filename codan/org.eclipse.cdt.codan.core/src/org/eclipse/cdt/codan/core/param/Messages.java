/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import org.eclipse.osgi.util.NLS;

/**
 * Core Messages
 */
class Messages extends NLS {
	public static String FileScopeProblemPreference_Label;
	public static String SuppressionCommentProblemPreference_Label;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
