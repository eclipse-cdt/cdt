package org.eclipse.cdt.internal.core.language.settings.providers;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 *  Temporary class for logging language settings providers development.
 *
 */
@Deprecated
public class LanguageSettingsLogger {
	private static boolean isEnabled() {
		return false;
//		return true;
	}

	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logInfo(String msg) {
		if (isEnabled()) {
			Exception e = new Exception(msg);
			IStatus status = new Status(IStatus.INFO, CCorePlugin.PLUGIN_ID, msg, e);
			CCorePlugin.log(status);
		}
	}

	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logWarning(String msg) {
		if (isEnabled()) {
			Exception e = new Exception(msg);
			IStatus status = new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, e);
			CCorePlugin.log(status);
		}
	}

	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logError(String msg) {
		if (isEnabled()) {
			Exception e = new Exception(msg);
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg, e);
			CCorePlugin.log(status);
		}
	}

	/**
	 * @param rc
	 * @param who - pass "this" (calling class instance) here
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logScannerInfoProvider(IResource rc, Object who) {
		if (isEnabled()) {
			String msg = "rc="+rc+" <-- "+who.getClass().getSimpleName(); //$NON-NLS-1$ //$NON-NLS-2$
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