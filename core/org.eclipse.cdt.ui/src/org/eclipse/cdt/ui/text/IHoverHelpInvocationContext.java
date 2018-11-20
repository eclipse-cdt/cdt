/**********************************************************************
 * Copyright (c) 2009 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.text;

import org.eclipse.jface.text.IRegion;

/**
 * Invocation context for the CHelpProviderManager.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.1
 */
public interface IHoverHelpInvocationContext extends ICHelpInvocationContext {

	/**
	 * @return the hover region or null
	 */
	IRegion getHoverRegion();

}
