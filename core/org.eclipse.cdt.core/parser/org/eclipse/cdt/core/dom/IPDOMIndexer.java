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

package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.model.IElementChangedListener;

/**
 * @author Doug Schaefer
 *
 */
public interface IPDOMIndexer extends IElementChangedListener {

	public void setPDOM(IPDOM pdom);
	
	public void reindex();
	
}
