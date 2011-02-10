package org.eclipse.cdt.codan.core.model;

/**
 * CheckerLaunchMode - how checker can be run
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same.
 * </p>
 * 
 * @since 2.0
 */
public enum CheckerLaunchMode {
	/**
	 * use parent settings
	 */
	USE_PARENT,
	/**
	 * checker run when full build is running
	 */
	RUN_ON_FULL_BUILD,
	/**
	 * checker run when incremental build is running
	 */
	RUN_ON_INC_BUILD,
	/**
	 * checker run in editor as you type
	 */
	RUN_AS_YOU_TYPE,
}