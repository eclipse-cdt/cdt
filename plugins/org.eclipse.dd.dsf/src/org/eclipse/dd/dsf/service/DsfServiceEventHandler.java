/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for service event handler methods.  The name of the event 
 * handler method is irrelevant, only the annotation is checked.
 * <p>
 * Each service event handler method should have one or two parameters:
 * <li>
 * <br> First argument is required and it should be the event object, with 
 * type with the event class desired.
 * <br> Second argument is optional, and it has to be of type Dictionary<String,String>.
 * If this parameter is declared, the handler will be passed the properties 
 * dictionary of the service that submitted the event.  
 * </li>
 * <p>
 * It is expected that service event classes are hierarchical.  So that if a
 * handler is registered for a super-class of another event, this handler
 * will be called every time one of the sub-class events is invoked.  
 * If a listener declares a handler for an event AND a superclass of that event,
 * both handlers will be invoked when the event is dispatched. 
 * 
 * <br>TODO: Handling of second argument is not yet implemented.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DsfServiceEventHandler {

}
