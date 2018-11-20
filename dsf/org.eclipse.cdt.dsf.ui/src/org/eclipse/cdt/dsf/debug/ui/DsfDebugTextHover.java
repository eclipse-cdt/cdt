/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui;

/**
 * A default hover provided by DSF.  Any hover provided by another
 * debugger integration using DSF will automatically override this one
 * based on the <code>BestMatchHover</code> class.
 *
 * @since 2.1
 */
public class DsfDebugTextHover extends AbstractDsfDebugTextHover {

	/*
	 * This Hover should work for any model using DSF, so we don't
	 * use the getModelId() method.
	 */
	@Override
	protected String getModelId() {
		return null;
	}

	/*
	 * Override to not use the getModelId() method, since this hover should
	 * be valid for any modelId using DSF.
	 */
	@Override
	protected boolean canEvaluate() {
		if (getFrame() != null) {
			return true;
		}
		return false;
	}
}
