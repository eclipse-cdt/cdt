package org.eclipse.lsp4e.cpp.language;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public Activator() {
		// TODO Auto-generated constructor stub

	}

	public static final String PLUGIN_ID = "org.eclipse.lsp4e.cpp.language";

	private static Activator plugin;

//	private Bundle bundle = null;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		//Instantiate Bundle
//		bundle = context.getBundle();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

//	@Override
//	public IPreferenceStore getPreferenceStore() {
//		return super.getPreferenceStore();
//	}

	public static Activator getDefault() {
		return plugin;
	}

}
