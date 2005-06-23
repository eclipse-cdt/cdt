/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

 
import org.eclipse.cdt.core.CCorePlugin;


/**
 * Markers used by the C model.
 * <p>
 * This interface declares constants only; it is not intended to be implemented
 * or extended.
 * </p>
 */
public interface ICModelMarker {

	/**
	 * C model problem marker type (value <code>"org.eclipse.cdt.core.problem"</code>).
	 * This can be used to recognize those markers in the workspace that flag problems 
	 * detected by the C compilers.
	 */
	public static final String C_MODEL_PROBLEM_MARKER = CCorePlugin.PLUGIN_ID + ".problem"; //$NON-NLS-1$
	
	/**
	 * C model extension to the marker problem markers which may hold a hint on
	 * the variable name that caused the error. Used by the ui to highlight the variable
	 * itself if it can be found.
	 */
	public static final String C_MODEL_MARKER_VARIABLE = "problem.variable"; //$NON-NLS-1$
	
	/**
	 * C model task marker type (value <code>"org.eclipse.cdt.core.task"</code>).
	 * This can be used to recognize task markers in the workspace that correspond to tasks
	 * specified in C/C++ source comments and detected during translation (for example, 'TO-DO: ...').
	 * Tasks are identified by a task tag, which can be customized through <code>CCorePlugin</code>
	 * option <code>"org.eclipse.cdt.core.translation.taskTag"</code>.
	 */
	public static final String TASK_MARKER = CCorePlugin.PLUGIN_ID + ".task"; //$NON-NLS-1$
	
	public static final String INDEXER_MARKER = CCorePlugin.PLUGIN_ID + ".indexermarker"; //$NON-NLS-1$

	/**
	 * PatheEtnry problem marker type (value <code>"org.eclipse.cdt.core.pathentry_problem"</code>).
	 * This can be used to recognize those markers in the workspace that flag problems 
	 * detected by the C tooling during pathEntry setting.
	 */
	public static final String PATHENTRY_PROBLEM_MARKER = CCorePlugin.PLUGIN_ID + ".pathentry_problem"; //$NON-NLS-1$
	
	/** 
	 * PathEntry file format marker attribute (value <code>"PathEntryFileFormat"</code>).
	 * Used only on pathentry store problem markers.
	 * The value of this attribute is either "true" or "false".
	 * 
	 */
	 public static final String PATHENTRY_FILE_FORMAT = "pathEntryFileFormat"; //$NON-NLS-1$

}


