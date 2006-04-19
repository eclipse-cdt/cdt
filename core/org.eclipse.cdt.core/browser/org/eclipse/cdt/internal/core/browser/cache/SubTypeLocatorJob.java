/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.IWorkingCopyProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

public class SubTypeLocatorJob extends BasicJob {

	public static final Object FAMILY = new Object();
	private ITypeInfo fLocateType;
	private ITypeCache fTypeCache;
	private IWorkingCopyProvider fWorkingCopyProvider;
	
	public SubTypeLocatorJob(ITypeInfo info, ITypeCache typeCache, IWorkingCopyProvider workingCopyProvider) {
		super(TypeCacheMessages.getString("SubTypeLocatorJob.jobName"), FAMILY); //$NON-NLS-1$
		fLocateType = info;
		fTypeCache = typeCache;
		fWorkingCopyProvider= workingCopyProvider;
	}
	
	public ITypeInfo getType() {
		return fLocateType;
	}

	protected IStatus runWithDelegatedProgress(IProgressMonitor monitor) throws InterruptedException {
		boolean success = false;
		long startTime = System.currentTimeMillis();
		trace("SubTypeLocatorJob: started"); //$NON-NLS-1$

		try {
			monitor.beginTask(TypeCacheMessages.getString("SubTypeLocatorJob.taskName"), 100); //$NON-NLS-1$
			
			if (monitor.isCanceled())
				throw new InterruptedException();
			
			TypeParser parser = new TypeParser(fTypeCache, fWorkingCopyProvider);
			success = parser.findSubTypes(fLocateType, new SubProgressMonitor(monitor, 100));

			if (monitor.isCanceled())
				throw new InterruptedException();
			
		} finally {
			long executionTime = System.currentTimeMillis() - startTime;
			if (success)
				trace("SubTypeLocatorJob: completed ("+ executionTime + " ms)"); //$NON-NLS-1$ //$NON-NLS-2$
			else
				trace("SubTypeLocatorJob: aborted ("+ executionTime + " ms)"); //$NON-NLS-1$ //$NON-NLS-2$

			monitor.done();
		}

		return Status.OK_STATUS;
	}
}
