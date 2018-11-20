package org.eclipse.cdt.qt.core;

/**
 * Event that a Qt install change has occured.
 *
 * @since 2.1
 */
public class QtInstallEvent {

	/**
	 * Qt Install has been removed. Called before it is actually removed.
	 */
	public static int REMOVED = 1;

	private final int type;
	private final IQtInstall install;

	public QtInstallEvent(int type, IQtInstall install) {
		this.type = type;
		this.install = install;
	}

	/**
	 * Type of the event
	 *
	 * @return type of the event
	 */
	public int getType() {
		return type;
	}

	/**
	 * Qt install involved in the event
	 *
	 * @return Qt install for the event
	 */
	public IQtInstall getInstall() {
		return install;
	}

}
