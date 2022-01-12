/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.sourcelookup;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;

/**
 * PDA source lookup director.
 */
public class PDASourceLookupDirector extends AbstractSourceLookupDirector {
	@Override
	public void initializeParticipants() {
		// No need to add participants here, the source display adapter will
		// add the participant with the correct session ID.
	}
}
