/*******************************************************************************
 * Copyright (c) 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.internal.ui.actions.AbstractUpdateIndexAction;
import org.eclipse.cdt.internal.ui.actions.AbstractUpdateIndexHandler;

/**
 * Handler for {@link org.eclipse.cdt.internal.ui.search.actions.FindUnresolvedIncludesProjectAction}
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class FindUnresolvedIncludesHandler extends AbstractUpdateIndexHandler {

	private final FindUnresolvedIncludesProjectAction findUnresolvedIncludesAction = new FindUnresolvedIncludesProjectAction();
	
	@Override
	public AbstractUpdateIndexAction getAction() {
		return findUnresolvedIncludesAction;
	}
}
