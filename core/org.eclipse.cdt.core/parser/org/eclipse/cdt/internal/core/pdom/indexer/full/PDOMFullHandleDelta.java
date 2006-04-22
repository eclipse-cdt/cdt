/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFullHandleDelta extends PDOMFullIndexerJob {

	private final ICElementDelta delta;
	
	public PDOMFullHandleDelta(PDOM pdom, ICElementDelta delta) {
		super(pdom);
		this.delta = delta;
	}

	protected IStatus run(IProgressMonitor monitor) {
//		try {
			long start = System.currentTimeMillis();
			
			return Status.OK_STATUS;
//		} catch (CoreException e) {
//			return e.getStatus();
//		}
	}

}
