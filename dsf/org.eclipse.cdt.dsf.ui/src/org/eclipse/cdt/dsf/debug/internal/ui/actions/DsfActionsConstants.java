package org.eclipse.cdt.dsf.debug.internal.ui.actions;

/**
 * Constants used by the DSF UI action adapters
 */
class DsfActionsConstants {
	/**
	 * The timeout in ms which action adapters will wait before disabling 
	 * the action itself, in order to avoid blocking the UI thread while
	 * waiting for the DSF thread to service a blocking query.
	 */
	static final int ACTION_ADAPTERS_TIMEOUT_MS = 500;

}
