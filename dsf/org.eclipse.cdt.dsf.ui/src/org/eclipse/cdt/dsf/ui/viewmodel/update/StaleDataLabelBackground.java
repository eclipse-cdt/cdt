/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelBackground;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.RGB;

/**
 * Stale data backgroun color label attribute to use with the
 * PropertyBasedLabelProvider.  The background color should only be
 * used when the view is in no-columns mode.
 *
 * @since 2.0
 */
public class StaleDataLabelBackground extends LabelBackground {

	public StaleDataLabelBackground() {
		super(null);
		setPropertyNames(
				new String[] { ICachingVMProvider.PROP_CACHE_ENTRY_DIRTY, ICachingVMProvider.PROP_UPDATE_POLICY_ID });
	}

	@Override
	public RGB getBackground() {
		return JFaceResources.getColorRegistry().getRGB(IDsfDebugUIConstants.PREF_COLOR_STALE_DATA_BACKGROUND);
	}

	@Override
	public boolean isEnabled(IStatus status, java.util.Map<String, Object> properties) {
		return Boolean.TRUE.equals(properties.get(ICachingVMProvider.PROP_CACHE_ENTRY_DIRTY))
				&& !AutomaticUpdatePolicy.AUTOMATIC_UPDATE_POLICY_ID
						.equals(properties.get(ICachingVMProvider.PROP_UPDATE_POLICY_ID));
	}
}
