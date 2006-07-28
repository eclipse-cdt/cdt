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
package org.eclipse.dd.dsf.concurrent;

/**
 * A DSF-instrumented alternative to the Runnable interface.    
 * <p>
 * While it is perfectly fine for clients to call the Riverbed executor with
 * an object only implementing the Runnable interface, the RbRunnable is a 
 * place holder for future tracing enhancments for Riverbed.  
 */
abstract public class DsfRunnable implements Runnable {
}
