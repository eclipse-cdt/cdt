/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.services.clientserver;

public interface IServiceConstants 
{
    public static final String TOKEN_SEPARATOR = "|"; //$NON-NLS-1$
    
	// Unexpected Error
	public static final String UNEXPECTED_ERROR = "unexpectedError"; //$NON-NLS-1$

    // Failure strings
    public static final String FAILED_WITH_EXIST = "failed with exist"; //$NON-NLS-1$
    public static final String FAILED_WITH_DOES_NOT_EXIST = "failed with does not exist"; //$NON-NLS-1$
    public static final String FAILED_WITH_EXCEPTION = "failed with exception"; //$NON-NLS-1$
    public static final String FAILED_WITH_SECURITY = "failed with security"; //$NON-NLS-1$
    public static final String FAILED_TO_DELETE_DIR = "failed to delete directory"; //$NON-NLS-1$

    // Status strings for communication
    public static final String SUCCESS = "success"; //$NON-NLS-1$
    public static final String FAILED = "failed"; //$NON-NLS-1$
    public static final String VERSION_1 = "version_1"; //$NON-NLS-1$
}
