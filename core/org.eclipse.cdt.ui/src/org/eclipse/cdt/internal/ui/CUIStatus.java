package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Convenience class for error exceptions thrown inside JavaUI plugin.
 */
public class CUIStatus extends Status {

	public CUIStatus(int code, String message, Throwable throwable) {
		super(IStatus.ERROR, CPlugin.getPluginId(), code, message, throwable);
	}

}


