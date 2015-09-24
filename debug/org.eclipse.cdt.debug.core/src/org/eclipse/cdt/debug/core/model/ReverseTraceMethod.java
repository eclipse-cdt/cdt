/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.IReverseTraceMethod;

/**
 * @since 7.8
 */
public enum ReverseTraceMethod implements IReverseTraceMethod {INVALID, STOP_TRACE, FULL_TRACE, BRANCH_TRACE, PROCESSOR_TRACE, GDB_TRACE};
