package org.eclipse.cdt.debug.mi.core.output;

/**
 *
 */
public class MIOOBRecord  {

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

	public MIResult[] getResults() {
		return null;
	}
}
