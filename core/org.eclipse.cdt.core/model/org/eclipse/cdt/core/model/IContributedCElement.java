/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
