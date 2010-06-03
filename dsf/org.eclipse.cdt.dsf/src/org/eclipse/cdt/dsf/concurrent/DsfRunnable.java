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


/**
 * A DSF-instrumented alternative to the Runnable interface.    
 * <p>
 * While it is perfectly fine for clients to call the DSF executor with
 * an object only implementing the Runnable interface, the DsfRunnable 
 * contains fields and methods that used for debugging and tracing when 
 * tracing is enabled.
 */
abstract public class DsfRunnable extends DsfExecutable implements Runnable {
}
