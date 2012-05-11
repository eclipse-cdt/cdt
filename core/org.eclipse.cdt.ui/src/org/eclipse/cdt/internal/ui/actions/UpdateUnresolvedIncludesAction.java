/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
