/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.search.ui.text.Match;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchMatch extends Match {

	public PDOMSearchMatch(PDOMName name, int offset, int length) {
		super(name, offset, length);
	}

	public String getFileName() {
		return ((PDOMName)getElement()).getFileName();
	}
}
