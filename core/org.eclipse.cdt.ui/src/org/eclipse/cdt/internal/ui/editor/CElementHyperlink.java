/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * C element hyperlink.
 *
 * @since 3.0
 */
public class CElementHyperlink implements IHyperlink {

	private final IRegion fRegion;
	private final IAction fOpenAction;

	/**
	 * Creates a new C element hyperlink.
	 */
	public CElementHyperlink(IRegion region, IAction openAction) {
		Assert.isNotNull(openAction);
		Assert.isNotNull(region);

		fRegion = region;
		fOpenAction = openAction;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return fRegion;
	}

	@Override
	public void open() {
		fOpenAction.run();
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return Action.removeMnemonics(fOpenAction.getText());
	}
}
