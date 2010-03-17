/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * Represents an asynchronous OOB record from gdb that notifies the client of
 * state changes on the target (stopped, started, etc).
 * 
 * <p>
 * All such records are prefixed by *.
 * 
 * @see MIAsyncRecord
 */
public class MIExecAsyncOutput extends MIAsyncRecord {
}
