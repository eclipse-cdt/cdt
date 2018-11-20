/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;

/**
 * Interface for the IndexView to bridge between PDOM and PDOMProxy
 */
public interface IPDOM extends IIndexFragment {

	PDOMLinkage[] getLinkageImpls();

	void addListener(PDOM.IListener listener);

	void removeListener(PDOM.IListener indexView);
}
