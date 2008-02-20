/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.sourcelookup;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;

/**
 * PDA source lookup director.
 */
public class PDASourceLookupDirector extends AbstractSourceLookupDirector {
	public void initializeParticipants() {
	    // No need to add participants here, the surce display adapter will
	    // add the participant with the correct session ID.
	}
}
