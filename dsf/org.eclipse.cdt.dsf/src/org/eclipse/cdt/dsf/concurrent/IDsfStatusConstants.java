/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;


/**
 * Interface that hold the codes used when reporting status using the DSF 
 * Request Monitor. 
 * <p>
 * The error codes are ordered by severity so that clients can filter error
 * reporting using these codes.  E.g. <code>getStatus().getCode() > INVALID_HANDLE</code> 
 * can be used to filter out errors resulting from expected race conditions. 
 * </p>
 * @since 1.0
 */
public interface IDsfStatusConstants {
    /** 
     * Error code indicating that the service is in a state which does not allow the 
     * request to be processed.  For example if the client requested target information
     * after target was disconnected. 
     */
    final static int INVALID_STATE = 10001;
    
    /** 
     * Error code indicating that client supplied an invalid handle to the service.
     * A handle could become invalid after an object it represents is removed from 
     * the system.
     * <p>
     * Note this code should not be used when a handle is of a wrong type.  
     * That would be an incorrect use of an interface, i.e. an {@link #INTERNAL_ERROR}. 
     * </p>
     */
    final static int INVALID_HANDLE = 10002;
    
    /**
     * Error code indicating that the client request is not supported/implemented.
     */
    final static int NOT_SUPPORTED = 10003;
    
    /**
     * Error code indicating that the request to a sub-service or an external process 
     * failed.
     */
    final static int REQUEST_FAILED = 10004;

    /**
     * Error code indicating an unexpected condition in the service, i.e. programming error.
     */
    final static int INTERNAL_ERROR = 10005;

}
