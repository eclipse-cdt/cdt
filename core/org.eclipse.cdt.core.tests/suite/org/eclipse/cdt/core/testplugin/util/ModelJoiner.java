/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin.util;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.core.runtime.CoreException;

/**
 * Some test steps need synchronizing against a CModel event. This class
 * is a very basic means of doing that.
 */
public class ModelJoiner implements IElementChangedListener {
	private final boolean[] changed = new boolean[1];

	public ModelJoiner() {
		CoreModel.getDefault().addElementChangedListener(this);
	}

	public void clear() {
		synchronized (changed) {
			changed[0] = false;
			changed.notifyAll();
		}
	}

	public void join() throws CoreException {
		try {
			synchronized (changed) {
				while (!changed[0]) {
					changed.wait();
				}
			}
		} catch (InterruptedException e) {
			throw new CoreException(CCorePlugin.createStatus("Interrupted", e));
		}
	}

	public void dispose() {
		CoreModel.getDefault().removeElementChangedListener(this);
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;

		synchronized (changed) {
			changed[0] = true;
			changed.notifyAll();
		}
	}
}