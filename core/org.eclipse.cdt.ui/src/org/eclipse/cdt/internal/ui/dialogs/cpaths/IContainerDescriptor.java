/*
 * Created on Apr 27, 2004
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.wizards.ICPathContainerPage;
import org.eclipse.core.runtime.CoreException;

public interface IContainerDescriptor {

	public abstract ICPathContainerPage createPage() throws CoreException;
	public abstract String getName();
	public abstract boolean canEdit(IPathEntry entry);
}
