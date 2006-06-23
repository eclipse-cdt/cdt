/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Extension to <code>IProblemRequestor</code>.
 * IProblemRequestorExtension
 */
public interface IProblemRequestorExtension {
	
	/**
	 * Sets the progress monitor to this problem requestor.
	 * 
	 * @param monitor the progress monitor to be used
	 */
	void setProgressMonitor(IProgressMonitor monitor);
	
	/**
	 * Sets the active state of this problem requestor.
	 * 
	 * @param isActive the state of this problem requestor
	 */
	void setIsActive(boolean isActive);
	
	/**
	 * Informs the problem requestor that a sequence of reportings is about to start. While
	 * a sequence is active, multiple peering calls of <code>beginReporting</code> and
	 * <code>endReporting</code> can appear.
	 * 
	 * @since 3.0
	 */
	void beginReportingSequence();
	
	/**
	 * Informs the problem requestor that the sequence of reportings has been finished.
	 * 
	 * @since 3.0
	 */
	void endReportingSequence();
}
