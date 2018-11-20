/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.swt.widgets.Composite;

/**
 * Bug #183341 : Single property page which does not
 * require separate cPropertyTab to display data.
 *
 */
public abstract class AbstractSinglePage extends AbstractPage {

	/**
	 * Implement this method to create your own widgets
	 */
	@Override
	public abstract void createWidgets(Composite c);

	/**
	 * Implement this method to perform apply:
	 * copy all data affected by this page
	 * from src resource description to dst
	 * @param src
	 * @param dst
	 */
	protected abstract void performApply(ICResourceDescription src, ICResourceDescription dst);

	/**
	 * Rewrite this method to handle configuration change
	 * Do not forget to call super.cfgChanged(_cfgd);
	 */
	@Override
	protected void cfgChanged(ICConfigurationDescription _cfgd) {
		super.cfgChanged(_cfgd);
		//	if (displayedConfig) {
		// update widgets according to getResDesc() values
		//	}
	}

	/**
	 * Usually, this method needs not to be rewritten
	 */
	@Override
	public boolean performCancel() {
		//	if (! noContentOnPage && displayedConfig) {
		// do nothing in most cases
		//	}
		return true;
	}

	/**
	 * Rewrite this method to restore default
	 * values in current ResourceDescription
	 */
	@Override
	public void performDefaults() {
		//	if (! noContentOnPage && displayedConfig) {
		// do something with getResDesc() fields
		//	}
	}

	/**
	 * Usually, this method needs not to be rewritten
	 */
	@Override
	public boolean performOk() {
		if (!noContentOnPage && displayedConfig) {
			// do nothing in most cases
		}
		return super.performOk();
	}

	/**
	 *
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			cfgChanged(getResDesc().getConfiguration());
		}
	}

	/**
	 * No need to rewrite
	 */
	@Override
	protected boolean isSingle() {
		return true;
	}

	/**
	 * Call to "foreach" does not really matter, since we have not tabs
	 * But we intercept this call to perform other operations (apply).
	 */
	@Override
	protected void forEach(int m, Object data) {
		if (m == ICPropertyTab.APPLY)
			performApply(getResDesc(), (ICResourceDescription) data);
	}

}
