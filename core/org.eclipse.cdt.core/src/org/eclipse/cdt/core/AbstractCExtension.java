/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core;

import org.eclipse.cdt.internal.core.InternalCExtension;
import org.eclipse.core.resources.IProject;

public abstract class AbstractCExtension extends InternalCExtension implements ICExtension {

	/**
	 * Returns the project for which this extrension is defined.
	 *	
	 * @return the project
	 */
	public final IProject getProject() {
		return super.getProject();
	}
	
	public final ICExtensionReference getExtensionReference() {
		return super.getExtensionReference();
	}
}
