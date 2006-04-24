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

package org.eclipse.cdt.internal.core.pdom.indexer.nulli;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMNullReindex extends Job {

	private final PDOM pdom;
	
	public PDOMNullReindex(PDOM pdom) {
		super("Null Indexer: " + pdom.getProject().getElementName());
		this.pdom = pdom;
		setRule(CCorePlugin.getPDOMManager().getIndexerSchedulingRule());
	}

	protected IStatus run(IProgressMonitor monitor) {
		try { 
			pdom.acquireWriteLock();
			pdom.clear();
			pdom.fireChange();
		} catch (CoreException e) {
			return e.getStatus();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		} finally {
			pdom.releaseWriteLock();
		}
		return Status.OK_STATUS;
	}

}
