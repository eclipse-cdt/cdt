package org.eclipse.cdt.debug.core;

/**
 *
 * Enter type comment.
 * 
 * @since: Sep 30, 2002
 */
public interface ICDebugTargetType
{
	public static final int TARGET_TYPE_UNKNOWN = 0;
	public static final int TARGET_TYPE_LOCAL_RUN = 1;
	public static final int TARGET_TYPE_LOCAL_ATTACH = 2;
	public static final int TARGET_TYPE_LOCAL_CORE_DUMP = 3;
	
	/**
	 * Returns the type of this target.
	 * 
	 * @return the type of this target
	 */
	int getTargetType();
}
