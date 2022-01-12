/*******************************************************************************
 * Copyright (c) 2010, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Adapter factory adapting an {@link ICStackFrame} to an {@link ICEditorTextHover}.
 *
 * @since 7.0
 */
public class DebugTextHoverAdapterFactory implements IAdapterFactory {

	private static final Class<?>[] TYPES = { ICEditorTextHover.class };
	private static final Object fDebugTextHover = new DebugTextHover();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof ICStackFrame) {
			return (T) fDebugTextHover;
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return TYPES;
	}

}
