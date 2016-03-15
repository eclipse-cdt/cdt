/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.model.IWorkingCopy;

/**
 * @deprecated use {@link org.eclipse.cdt.core.model.IWorkingCopyProvider}, instead.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IWorkingCopyProvider {
	public IWorkingCopy[] getWorkingCopies();
}
