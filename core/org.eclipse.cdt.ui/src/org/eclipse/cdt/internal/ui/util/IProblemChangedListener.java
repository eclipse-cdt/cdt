package org.eclipse.cdt.internal.ui.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Set;

/**
 * Can be added to a ProblemMarkerManager to get notified about error
 * marker changes. Used to update error ticks.
 */
public interface IProblemChangedListener {
	
	/**
	 * @param changedElements  A set of type <code>IPath</code> that
	 * describe the resources that had an error marker change.
	 */
	void problemsChanged(Set changedElements);
}

