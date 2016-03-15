/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.wizards.IPathEntryContainerPage;

/**
 * @deprecated as of CDT 4.0. This class was used for property pages
 * for 3.X style projects.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IContainerDescriptor {
	public IPathEntryContainerPage createPage() throws CoreException;
	public String getName();
	public Image getImage();
	public boolean canEdit(IPathEntry entry);
}
