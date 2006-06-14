/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Additions to the <code>ICElement</code> hierarchy provided by
 * contributed languages.
 * 
 * Contributed elements are required to be adaptable to an
 * <code>ImageDescriptor</code>.
 * 
 * @author Jeff Overbey
 * @see ICElement
 * @see IAdaptable
 */
public interface IContributedCElement extends ICElement {
}
