/**********************************************************************
Copyright (c) 2002, 2004 IBM Rational Software and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
    IBM Rational Software - Initial Contribution
**********************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.core.runtime.IPath;

public class ExternalSearchFile extends FileStorage {

	IPath referringElement;
	BasicSearchMatch searchMatch;
	/**
	 * @param path
	 */
	public ExternalSearchFile(IPath path, BasicSearchMatch searchMatch) {
		super(path);
		this.searchMatch = searchMatch;
	}
}
