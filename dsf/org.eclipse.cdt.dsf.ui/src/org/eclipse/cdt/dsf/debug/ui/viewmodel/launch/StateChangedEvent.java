/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - initial API and implementation
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * A generic UI state changed event.
 *
 * @since 2.2
 */
public class StateChangedEvent extends AbstractDMEvent<IDMContext> {

	public StateChangedEvent(IDMContext context) {
		super(context);
	}
}
