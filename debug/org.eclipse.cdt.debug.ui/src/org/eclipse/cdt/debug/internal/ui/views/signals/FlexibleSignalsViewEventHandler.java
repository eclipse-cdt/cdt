/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - flexible hierarchy Signals view (bug 338908)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;

/**
 */
public class FlexibleSignalsViewEventHandler extends DebugEventHandler {

	private Object fTarget;

	/**
	 * Constructor for SignalsViewEventHandler.
	 */
	public FlexibleSignalsViewEventHandler(AbstractModelProxy proxy, Object target) {
		super(proxy);
		fTarget = target;
	}

	@Override
	protected boolean handlesEvent(DebugEvent event) {
		int kind = event.getKind();
		if (kind == DebugEvent.CREATE || kind == DebugEvent.TERMINATE || kind == DebugEvent.CHANGE
				|| kind == DebugEvent.SUSPEND)
			return true;
		return false;
	}

	@Override
	protected void handleChange(DebugEvent event) {
		if (event.getSource() instanceof ICSignal)
			fireDelta((ICSignal) event.getSource(), IModelDelta.STATE);
	}

	@Override
	protected void handleCreate(DebugEvent event) {
		Object source = event.getSource();
		if (source instanceof IDebugTarget) {
			refreshRoot(event);
		} else if (source instanceof ICSignal) {
			if (accept((ICSignal) source)) {
				ICSignal signal = (ICSignal) source;
				fireDelta(signal, IModelDelta.ADDED);
			}
		}
	}

	@Override
	protected void handleTerminate(DebugEvent event) {
		Object source = event.getSource();
		if (source instanceof IDebugTarget) {
			refreshRoot(event);
		} else if (source instanceof ICSignal) {
			fireDelta((ICSignal) source, IModelDelta.REMOVED);
		}
	}

	private void fireDelta(ICSignal signal, int flags) {
		ModelDelta root = new ModelDelta(fTarget, IModelDelta.NO_CHANGE);
		root.addNode(signal, flags);
		fireDelta(root);
	}

	@Override
	public synchronized void dispose() {
		super.dispose();
		fTarget = null;
	}

	private boolean accept(ICSignal signal) {
		return fTarget.equals(signal.getDebugTarget());
	}
}
