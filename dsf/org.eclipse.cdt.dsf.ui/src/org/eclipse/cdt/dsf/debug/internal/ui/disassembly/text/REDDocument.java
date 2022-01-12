/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ITextStore;

/**
 * Standard Document implementation with REDTextStore (splice texts)
 * as text storage.
 */
public class REDDocument extends AbstractDocument {

	public REDDocument() {
		setTextStore(new REDTextStore());
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}

	/**
	 * Free text store (delete scratchfiles).
	 */
	public void dispose() {
		ITextStore store = getStore();
		if (store instanceof REDTextStore) {
			((REDTextStore) store).dispose();
			setTextStore(new StringTextStore());
			getTracker().set(""); //$NON-NLS-1$
		}
	}

}
