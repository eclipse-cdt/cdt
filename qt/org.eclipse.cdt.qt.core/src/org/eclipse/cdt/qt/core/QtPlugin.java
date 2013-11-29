package org.eclipse.cdt.qt.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

public class QtPlugin extends Plugin {

    public static final String ID = "org.eclipse.cdt.qt.core";
    public static final String SIGNAL_SLOT_TAGGER_ID = ID + ".signalslot.tagger";

    public static final int SignalSlot_Mask_signal = 1;
    public static final int SignalSlot_Mask_slot = 2;

	public static final String QMAKE_ENV_PROVIDER_EXT_POINT_NAME = "qmakeEnvProvider"; //$NON-NLS-1$
	public static final String QMAKE_ENV_PROVIDER_ID = ID + "." + QMAKE_ENV_PROVIDER_EXT_POINT_NAME; //$NON-NLS-1$

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

	public static IStatus info(String msg) {
		return new Status(IStatus.INFO, ID, msg);
	}

	public static IStatus error(String msg) {
		return error(msg, null);
	}

	public static IStatus error(String msg, Throwable e) {
		return new Status(IStatus.ERROR, ID, msg, e);
	}

	public static IStatus log(String e) {
		return log(IStatus.INFO, e, null);
	}

	public static IStatus log(Throwable e) {
		String msg = e.getMessage();
		return msg == null ? log("Error", e) : log("Error: " + msg, e);
	}

	public static IStatus log(String message, Throwable e) {
		return log(IStatus.ERROR, message, e);
	}

	public static IStatus log(int code, String msg, Throwable e) {
		IStatus status = new Status(code, ID, msg, e);
		instance.getLog().log(status);
		return status;
	}
}
