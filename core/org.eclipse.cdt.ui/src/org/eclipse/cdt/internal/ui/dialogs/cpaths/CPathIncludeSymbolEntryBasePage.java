/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Abstract DialogPage for C/C++ Project Paths page for 3.X projects.
 */
public abstract class CPathIncludeSymbolEntryBasePage extends CPathBasePage {

	public CPathIncludeSymbolEntryBasePage(String title) {
		super(title);
	}

	public CPathIncludeSymbolEntryBasePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public abstract void init(ICElement cElement, List<CPElement> cPaths);

	public abstract List<CPElement> getCPaths();
}
