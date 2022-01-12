/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Nokia - Added support for AbsoluteSourceContainer( 159833 )
 * Wind River Systems - Adapted for use with DSF
*******************************************************************************/
package org.eclipse.cdt.dsf.debug.sourcelookup;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * DSF source lookup director.
 *
 * When a launch (or the global) source lookup containers are being edited it is
 * an instance of CSourceLookupDirector that is created. However, when using DSF
 * launch, the subclass DsfSourceLookupDirector is actually instantiated because
 * connection to the DsfSession is needed.
 *
 * @since 1.0
 */
public class DsfSourceLookupDirector extends CSourceLookupDirector {

	private final DsfSession fSession;

	public DsfSourceLookupDirector(DsfSession session) {
		fSession = session;
	}

	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] { new DsfSourceLookupParticipant(fSession) });
	}

}
