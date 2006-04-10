/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.clientserver;

public interface IServiceConstants 
{
    public static final String TOKEN_SEPARATOR = "|";
    
	// Unexpected Error
	public static final String UNEXPECTED_ERROR = "unexpectedError";

    // Failure strings
    public static final String FAILED_WITH_EXIST = "failed with exist";
    public static final String FAILED_WITH_DOES_NOT_EXIST = "failed with does not exist";
    public static final String FAILED_WITH_EXCEPTION = "failed with exception";
    public static final String FAILED_WITH_SECURITY = "failed with security";
    public static final String FAILED_TO_DELETE_DIR = "failed to delete directory";
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";
    public static final String VERSION_1 = "version_1"; 
}