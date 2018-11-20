/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
