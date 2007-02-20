/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.core.runtime.CoreException;

public class ExceptionFactory {
	public static WriteAccessException createIsReadOnlyException(){
		return new WriteAccessException();
	}

	public static CoreException createCoreException(String message){
		return new CoreException(new DescriptionStatus(message));
	}

	public static CoreException createCoreException(Throwable e){
		return new CoreException(new DescriptionStatus(e));
	}

	public static CoreException createCoreException(String message, Exception exception){
		return new CoreException(new DescriptionStatus(message, exception));
	}

}
