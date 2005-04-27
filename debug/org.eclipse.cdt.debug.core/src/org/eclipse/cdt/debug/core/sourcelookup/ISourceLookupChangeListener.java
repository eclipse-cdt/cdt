/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.core.sourcelookup; 

import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

 /**
 * A source lookup change listener is notified of changes in the source lookup path.
 */
public interface ISourceLookupChangeListener {

	/**
	 * Notification that the source lookup containers have changed.
	 */
	public void sourceContainersChanged( ISourceLookupDirector director );
}
