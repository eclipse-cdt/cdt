package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICCompletionContributor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;

/**
 * Manages external plugins that contribute completion and function
 * info through the CCompletionContributor extension point
 */

public class CCompletionContributorManager {

	static private List fCompletionContributors;
	static boolean fContributorsLoaded = false;
	public static final String CONTRIBUTION_EXTENSION = "CCompletionContributor"; //$NON-NLS-1$
	static private CCompletionContributorManager fInstance;

	private CCompletionContributorManager() {
		// Initialize and scan the extension points
	}

	public static CCompletionContributorManager getDefault() {
		if (fInstance == null) {
			fInstance = new CCompletionContributorManager();
		}
		return fInstance;
	}

	public IFunctionSummary getFunctionInfo(String name) {
		if (!fContributorsLoaded)
			loadExtensions();

		for (int i = 0; i < fCompletionContributors.size(); i++) {
			ICCompletionContributor c = (ICCompletionContributor) fCompletionContributors.get(i);
			IFunctionSummary f = c.getFunctionInfo(name);

			if (f != null)
				return f;
		}
		return null;
	}

	public IFunctionSummary[] getMatchingFunctions(String frag) {
		if (!fContributorsLoaded)
			loadExtensions();
		IFunctionSummary[] fs = null;

		for (int i = 0; i < fCompletionContributors.size(); i++) {
			ICCompletionContributor c = (ICCompletionContributor) fCompletionContributors.get(i);
			IFunctionSummary[] f = c.getMatchingFunctions(frag);
			if (f != null) {
				if (fs != null) {
					int length = f.length + fs.length;
					IFunctionSummary[] ft = new IFunctionSummary[length];
					int j;
					for (j = 0; j < fs.length; j++)
						ft[j] = fs[j];
					for (j = 0; j < f.length; j++)
						ft[j + fs.length] = f[j];
					fs = ft;
				} else {
					fs = f;
				}
			}

			//if(f != null) 
			//return f;
		}

		return fs;
	}

	private void loadExtensions() {
		fContributorsLoaded = true;
		fCompletionContributors = new ArrayList(2);

		// populate list
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID, "CCompletionContributor"); //$NON-NLS-1$
		if (extensionPoint != null) {
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getName().equals("provider")) { //$NON-NLS-1$
					try {
						final ICCompletionContributor c;
						// Instantiate the class
						c = (ICCompletionContributor) elements[i].createExecutableExtension("class"); //$NON-NLS-1$
						ISafeRunnable runnable = new ISafeRunnable() {
							public void run() throws Exception {
								// Initialize
								c.initialize();
								// Add to contributor list
								fCompletionContributors.add(c);
							}
							public void handleException(Throwable exception) {
							}
						};
						Platform.run(runnable);
					} catch (CoreException e) {
					}
				}
			}
		}
	}
}