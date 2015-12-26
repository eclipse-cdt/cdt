/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.sourcelookup;

import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupParticipant;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * PDA source lookup director.
 */
public class PDASourceLookupDirector extends AbstractSourceLookupDirector {
	private DsfSession fSession;

	public PDASourceLookupDirector(DsfSession session) {
		fSession = session;
	}

	@Override
	public void initializeParticipants() {
		addParticipants( new ISourceLookupParticipant[]{ new DsfSourceLookupParticipant(fSession) } );
	}
}
