/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.debug.core.ICBreakpointManager;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * A C/C++ specific extension of IDebugTarget.
 * 
 * @since: Dec 2, 2002
 */
public interface ICDebugTarget extends IDebugTarget,
									   ICDebugTargetType,
									   ICExpressionEvaluator,
									   IDebuggerProcessSupport,
									   IExecFileInfo,
									   IRestart,
									   IRunToLine,
									   IRunToAddress,
									   IState,
									   ISwitchToThread,
									   ICBreakpointManager
{
}
