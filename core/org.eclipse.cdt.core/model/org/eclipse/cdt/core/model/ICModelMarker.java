package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.CCorePlugin;


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
	 * detected by the C ompilers.
	 */
	public static final String C_MODEL_PROBLEM_MARKER = CCorePlugin.PLUGIN_ID + ".problem"; //$NON-NLS-1$
	
	/**
	 * C model extension to the marker problem markers which may hold a hint on
	 * the variable name that caused the error. Used by the ui to highlight the variable
	 * itself if it can be found.
	 */
	public static final String C_MODEL_MARKER_VARIABLE = "problem.variable"; //$NON-NLS-1$
}


