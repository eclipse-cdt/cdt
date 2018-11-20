/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.core.index.IIndexManager;

public class UpdateUnresolvedIncludesAction extends AbstractUpdateIndexAction {
	@Override
	protected int getUpdateOptions() {
		return IIndexManager.UPDATE_UNRESOLVED_INCLUDES;
	}
}
