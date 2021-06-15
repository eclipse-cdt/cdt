/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.viewsupport.DecoratingCOutlineLabelProvider;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

public class DividerLine extends WorkbenchAdapter implements IAdaptable {

	private ISourceReference element;

	public DividerLine(ISourceReference element) {
		this.element = element;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return adapter.cast(this);
		}
		if (adapter == ISourceReference.class) {
			return adapter.cast(element);
		}
		return null;
	}

	/**
	 * When owner draw is not enabled ( General > Appearance > Use mixed fonts and colors for labels.)
	 * the custom draw in {@link DecoratingCOutlineLabelProvider} is not used and therefore the
	 * entry uses the getLabel value to fill the entry.
	 * @see DecoratingCOutlineLabelProvider
	 */
	@Override
	public String getLabel(Object object) {
		return "-".repeat(20); //$NON-NLS-1$
	}
}
