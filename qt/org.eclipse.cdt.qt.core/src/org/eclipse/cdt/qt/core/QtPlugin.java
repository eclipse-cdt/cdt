package org.eclipse.cdt.qt.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class QtPlugin implements BundleActivator {

    public static final String ID = "org.eclipse.cdt.qt.core";
    public static final String SIGNAL_SLOT_TAGGER_ID = ID + ".signalslot.tagger";

    public static final int SignalSlot_Mask_signal = 1;
    public static final int SignalSlot_Mask_slot = 2;

    private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext bundleContext) throws Exception {
		QtPlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext bundleContext) throws Exception {
		QtPlugin.context = null;
	}

}
