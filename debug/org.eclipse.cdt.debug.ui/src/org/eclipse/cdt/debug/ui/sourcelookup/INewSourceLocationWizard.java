/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
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
