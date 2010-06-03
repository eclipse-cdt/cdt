/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that given package, class, method, can be accessed on
 * any thread, except on the dispatch thread of given DsfExecutor. 
 * <br> This restriction is desirable if it is expected that the implementation 
 * behavior is to block the calling thread and execute a transaction using an 
 * executor.  In this situation, if the call is made on the executor's dispatch
 * thread, the execution would dead-lock.
 * <br> 
 * If declared on package or type, a field or method could still be declared 
 * with an annotation indicating that it's thread-safe.
 * <p>
 * Note: the runtime retention policy is there to allow automated testing 
 * and validation code.
 * 
 * @param value The value indicates the method to use to obtain the executor.  
 * It should be null if it cannot be determined from the given object. 
 * 
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Inherited
@Documented
public @interface ThreadSafeAndProhibitedFromDsfExecutor {
    String value();
}
