/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.jface.wizard.IWizard;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 25, 2002
 */
public interface INewSourceLocationWizard extends IWizard
{
	String getDescription();
	ICSourceLocation getSourceLocation();
}
