/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 * This is a generic interface representing the builder
 * It is implemented currently by the BuildDescription builder,
 * BuildStep builder and BuildCommand builder that are used for building
 * the different parts of the build model 
 * and represent an MBS Internal Builder.
 * In the future we might also adopt the external builder invocation
 * to the same concept, e.g. the IBuildModelBuilder implementer 
 * for the external builder invocation might invoke an external builder
 * from within its build method
 * 
 * NOTE: This interface is subject to change and discuss, 
 * and is currently available in experimental mode only 
 *
 */
public interface IBuildModelBuilder {
	public static final int STATUS_OK = 0;
	public static final int STATUS_ERROR_BUILD = -1;
	public static final int STATUS_ERROR_LAUNCH = -2;
	public static final int STATUS_CANCELLED = -3;
	

	int build(OutputStream out,
			OutputStream err,
			IProgressMonitor monitor);
	
}
