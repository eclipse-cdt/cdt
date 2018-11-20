/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * ITemplatesListProvider
 *
 * @since 4.0
 */
public interface ITemplatesListProvider extends ITreeContentProvider {

	Template[] getTemplates();

	String getDescription(Object object);

	boolean showTemplatesInTreeView();

}
