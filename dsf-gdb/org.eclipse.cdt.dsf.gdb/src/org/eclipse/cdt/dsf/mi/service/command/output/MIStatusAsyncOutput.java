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
 * Represents an asynchronous OOB record from gdb that provides status on an
 * ongoing time consuming operation.
 *
 * <p>
 * All such output is prefixed by `+'.
 *
 * @see MIAsyncRecord
 */
public class MIStatusAsyncOutput extends MIAsyncRecord {

}
