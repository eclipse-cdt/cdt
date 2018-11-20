/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
