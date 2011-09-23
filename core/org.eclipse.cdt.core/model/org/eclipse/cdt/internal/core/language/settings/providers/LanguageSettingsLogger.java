package org.eclipse.cdt.internal.core.language.settings.providers;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

@Deprecated
public class LanguageSettingsLogger {

	/**
	 * 
	 */
	private static final boolean ENABLED = false;

	// AG FIXME
	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logInfo(String msg) {
		if (ENABLED) {
			Exception e = new Exception(msg);
			IStatus status = new Status(IStatus.INFO, CCorePlugin.PLUGIN_ID, msg, e);
			CCorePlugin.log(status);
		}
	}

	// AG FIXME
	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logWarning(String msg) {
		if (ENABLED) {
			Exception e = new Exception(msg);
			IStatus status = new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, e);
			CCorePlugin.log(status);
		}
	}

	// AG FIXME
	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logError(String msg) {
		if (ENABLED) {
			Exception e = new Exception(msg);
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg, e);
			CCorePlugin.log(status);
		}
	}
	
	// AG FIXME
	/**
	 * @param rc
	 * @param who - pass "this" (calling class instance) here
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logScannerInfoProvider(IResource rc, Object who) {
		if (ENABLED) {
			String msg = "rc="+rc+" <-- "+who.getClass().getSimpleName();
			if (rc instanceof IFile) {
				LanguageSettingsLogger.logInfo(msg);
			} else if (rc instanceof IProject) {
				LanguageSettingsLogger.logWarning(msg);
			} else {
				LanguageSettingsLogger.logError(msg);
			}
		}
	}
}