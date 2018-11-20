/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lidia Popescu - [536255] initial API and implementation. Extension point for open call hierarchy view
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.jface.viewers.IOpenListener;

/**
 * The Call Hierarchy Extension Content Provider Interface
 * @since 6.4
 * */
public interface ICHEContentProvider {

	Object[] asyncComputeExtendedRoot(Object parentElement);

	IOpenListener getCCallHierarchyOpenListener();
}
