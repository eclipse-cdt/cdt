/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.qt.core.IQMLAnalyzer;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends Plugin {

	public static final String ID = "org.eclipse.cdt.qt.core"; //$NON-NLS-1$
	public static final String SIGNAL_SLOT_TAGGER_ID = ID + ".signalslot.tagger"; //$NON-NLS-1$

	public static final int SignalSlot_Mask_signal = 1;
	public static final int SignalSlot_Mask_slot = 2;

	public static final String QMAKE_ENV_PROVIDER_EXT_POINT_NAME = "qmakeEnvProvider"; //$NON-NLS-1$
	public static final String QMAKE_ENV_PROVIDER_ID = ID + "." + QMAKE_ENV_PROVIDER_EXT_POINT_NAME; //$NON-NLS-1$

	public static final String QT_SYNTAX_ERR_ID = "org.eclipse.cdt.qt.core.qtproblem"; //$NON-NLS-1$

	/**
	 * Instances of QtIndex are cached within the session properties of the
	 * project from which they are created. This name is used to store the
	 * property.
	 */
	public static final QualifiedName QTINDEX_PROP_NAME = new QualifiedName(ID, "qtindex"); //$NON-NLS-1$

	private static Activator instance;

	public static Activator getDefault() {
		return instance;
	}

	public Activator() {
		instance = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		context.registerService(IQtInstallManager.class, new QtInstallManager(), null);

		QMLAnalyzer qmlAnalyzer = new QMLAnalyzer();
		context.registerService(IQMLAnalyzer.class, qmlAnalyzer, null);
		Job startupJob = new Job("Load QML Analyzer") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					qmlAnalyzer.load();
				} catch (NoSuchMethodException | ScriptException | IOException e) {
					log(e);
				}
				return Status.OK_STATUS;
			}
		};
		startupJob.setSystem(true);
		startupJob.schedule();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// QMakeProjectInfoManager.stop();

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
		String msg = e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}

	public static void log(String message, Throwable e) {
		Throwable nestedException;
		if (e instanceof CModelException && (nestedException = ((CModelException) e).getException()) != null) {
			e = nestedException;
		}
		log(IStatus.ERROR, message, e);
	}

	public static void log(int code, String msg, Throwable e) {
		getDefault().getLog().log(new Status(code, ID, msg, e));
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = instance.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
