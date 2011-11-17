/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.internal.core.util.ICancelable;
import org.eclipse.cdt.internal.core.util.ICanceler;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * A progress monitor accepting a <code>ICancelable</code> object to receive the cancel request.
 *
 * @since 5.0
 */
public class ProgressMonitorAndCanceler extends NullProgressMonitor implements ICanceler {

	private ICancelable fCancelable;

	@Override
	public void setCancelable(ICancelable cancelable) {
		fCancelable= cancelable;
		checkCanceled();
	}

	@Override
	public void setCanceled(boolean canceled) {
		super.setCanceled(canceled);
		checkCanceled();
	}

	private void checkCanceled() {
		if (fCancelable != null && isCanceled()) {
			fCancelable.cancel();
			fCancelable= null;
		}
	}

}
