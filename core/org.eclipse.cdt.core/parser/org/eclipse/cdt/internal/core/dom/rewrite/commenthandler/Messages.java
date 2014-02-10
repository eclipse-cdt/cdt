/*******************************************************************************
 * Copyright (c) 2014 Institute for Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lukas Felber - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.messages"; //$NON-NLS-1$
	public static String NO_COMMENTS_IN_AST_WARNING;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
