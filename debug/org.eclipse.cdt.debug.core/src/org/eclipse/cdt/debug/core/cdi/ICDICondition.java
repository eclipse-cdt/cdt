/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

/**
 * 
 * Represents a break condition.
 * 
 * @since Jul 9, 2002
 */
public interface ICDICondition {
	/**
	 * Returns the condition expression.
	 * 
	 * @return the condition expression
	 */
	String getExpression();
	
	/**
	 * Returns the ignore count of this condition.
	 * 
	 * @return the ignore count of this condition
	 */
	int getIgnoreCount();
	
}
