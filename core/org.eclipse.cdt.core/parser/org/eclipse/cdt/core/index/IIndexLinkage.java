/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/ 
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ILinkage;

/**
 * Represents the linkage of a name in the index.
 * 
 * @since 4.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIndexLinkage extends ILinkage {
	/**
	 * Empty IIndexLinkage array constant
	 * @since 4.0.1
	 */
	IIndexLinkage[] EMPTY_INDEX_LINKAGE_ARRAY= {};
}
