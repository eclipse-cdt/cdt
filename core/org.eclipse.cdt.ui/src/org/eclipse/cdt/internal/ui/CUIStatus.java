package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.ui.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Convenience class for error exceptions thrown inside JavaUI plugin.
 */
public class CUIStatus extends Status {

	public CUIStatus(int code, String message, Throwable throwable) {
		super(IStatus.ERROR, CUIPlugin.getPluginId(), code, message, throwable);
	}
	
	private CUIStatus(int severity, int code, String message, Throwable throwable) {
		super(severity, CUIPlugin.getPluginId(), code, message, throwable);
	}
	
	public static IStatus createError(int code, Throwable throwable) {
		String message= throwable.getMessage();
		if (message == null) {
			message= throwable.getClass().getName();
		}
		return new CUIStatus(IStatus.ERROR, code, message, throwable);
	}

	public static IStatus createError(int code, String message, Throwable throwable) {
		return new CUIStatus(IStatus.ERROR, code, message, throwable);
	}
	
	public static IStatus createWarning(int code, String message, Throwable throwable) {
		return new CUIStatus(IStatus.WARNING, code, message, throwable);
	}

	public static IStatus createInfo(int code, String message, Throwable throwable) {
		return new CUIStatus(IStatus.INFO, code, message, throwable);
	}
	
}


