package org.eclipse.cdt.debug.ui;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.IReverseTraceMethod;

/**
 * @since 7.6
 */
public enum ReverseTraceMethod implements IReverseTraceMethod {INVALID, STOP_TRACE, FULL_TRACE, BRANCH_TRACE, PROCESSOR_TRACE, GDB_TRACE};
