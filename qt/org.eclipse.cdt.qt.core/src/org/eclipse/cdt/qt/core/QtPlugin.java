/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.core;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.qt.core.index.QMakeProjectInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class QtPlugin extends Plugin {

    public static final String ID = "org.eclipse.cdt.qt.core";
    public static final String SIGNAL_SLOT_TAGGER_ID = ID + ".signalslot.tagger";

    public static final int SignalSlot_Mask_signal = 1;
    public static final int SignalSlot_Mask_slot = 2;

	public static final String QMAKE_ENV_PROVIDER_EXT_POINT_NAME = "qmakeEnvProvider"; //$NON-NLS-1$
	public static final String QMAKE_ENV_PROVIDER_ID = ID + "." + QMAKE_ENV_PROVIDER_EXT_POINT_NAME; //$NON-NLS-1$

    public static final String QT_SYNTAX_ERR_ID = "org.eclipse.cdt.qt.core.qtproblem"; //$NON-NLS-1$

    /**
     * Instances of QtIndex are cached within the session properties of the project from
     * which they are created.  This name is used to store the property.
     */
    public static final QualifiedName QTINDEX_PROP_NAME = new QualifiedName(ID, "qtindex");

    private static QtPlugin instance;

    public static QtPlugin getDefault() {
    	return instance;
    }

    public QtPlugin() {
    	instance = this;
    }

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		QMakeProjectInfo.start();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		QMakeProjectInfo.stop();
		super.stop(context);
	}

    public static CoreException coreException(String msg) {
    	return new CoreException(new Status(IStatus.INFO, ID, msg));
    }

	public static IStatus info(String msg) {
		return new Status(IStatus.INFO, ID, msg);
	}

	public static IStatus error(String msg) {
		return error(msg, null);
	}

	public static IStatus error(String msg, Throwable e) {
		return new Status(IStatus.ERROR, ID, msg, e);
	}

	public static void log(String e) {
		log(IStatus.INFO, e, null);
	}

	public static void log(Throwable e) {
		String msg= e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}

	public static void log(String message, Throwable e) {
		Throwable nestedException;
		if (e instanceof CModelException
				&& (nestedException = ((CModelException)e).getException()) != null) {
			e = nestedException;
		}
		log(IStatus.ERROR, message, e);
	}

	public static void log(int code, String msg, Throwable e) {
		getDefault().getLog().log(new Status(code, ID, msg, e));
	}
}
