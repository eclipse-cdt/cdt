package org.eclipse.cdt.debug.mi.core.output;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MIOOBRecord extends MIOutput {

	public final int ASYNC_STOPPED = 0;

	/**
	 * @see org.eclipse.cdt.debug.mi.core.MIOutput#interpret()
	 */
	public boolean interpret() {
		return false;
	}

	public int getAsyncType() {
		return ASYNC_STOPPED;
	}

	public MIResult getResult() {
		return null;
	}
}
