package org.eclipse.cdt.internal.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IStatus;

public interface IStatusChangeListener {
	
	/**
	 * Called to annonce that the given status has changed
	 */
	void statusChanged(IStatus status);
}
