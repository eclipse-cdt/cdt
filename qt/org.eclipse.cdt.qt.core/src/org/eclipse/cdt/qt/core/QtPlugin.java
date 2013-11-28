package org.eclipse.cdt.qt.core;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.qt.core.index.QMakeProjectInfo;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class QtPlugin extends Plugin {

    public static final String ID = "org.eclipse.cdt.qt.core";
    public static final String SIGNAL_SLOT_TAGGER_ID = ID + ".signalslot.tagger";

    public static final int SignalSlot_Mask_signal = 1;
    public static final int SignalSlot_Mask_slot = 2;

	public static final String QMAKE_ENV_PROVIDER_EXT_POINT_NAME = "qmakeEnvProvider"; //$NON-NLS-1$
	public static final String QMAKE_ENV_PROVIDER_ID = ID + "." + QMAKE_ENV_PROVIDER_EXT_POINT_NAME; //$NON-NLS-1$

	private static QtPlugin INSTANCE;
    private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	static QtPlugin getDefault() {
		return INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext bundleContext) throws Exception {
		INSTANCE = this;
		QtPlugin.context = bundleContext;
		QMakeProjectInfo.start();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext bundleContext) throws Exception {
		QMakeProjectInfo.stop();
		QtPlugin.context = null;
		INSTANCE = null;
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
		if (e instanceof CModelException && (nestedException = ((CModelException)e).getException()) != null) {
			e = nestedException;
		}
		log(IStatus.ERROR, message, e);
	}

	public static void log(int code, String msg, Throwable e) {
		getDefault().getLog().log(new Status(code, ID, msg, e));
	}

}
