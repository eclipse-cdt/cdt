/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.cdt.core.model.ICElement;

/**
 * Abstract DialogPage for C/C++ Project Paths page for 3.X projects.
 * 
 * @deprecated as of CDT 4.0. This page was used to set preferences/properties
 * for 3.X style projects.
 */
@Deprecated
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
