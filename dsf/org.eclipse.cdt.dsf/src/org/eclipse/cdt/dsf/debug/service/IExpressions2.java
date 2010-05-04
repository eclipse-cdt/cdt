/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;

/**
 * This interface extends the expressions service with support for casting to type or
 * array.  
 * @since 2.1
 */
public interface IExpressions2 extends IExpressions {

	/**
	 * This class specifies how an expression should be
	 * typecast to another type and/or displayed as an array.
	 */
	public static class CastInfo {
		private final String typeString;
		private final int arrayCount;
		private final int arrayStart;

		/**
		 * Create an instance of casting information
		 * @param typeString if not <code>null</code>, the C/C++ type to which to cast the expression (e.g. "char**")
		 * @param arrayStart if arrayCount > 0, the start index for viewing contents of the expression as an array
		 * @param arrayCount if > 0, indicates to show [arrayStart ... arrayStart+arrayCount) as child expressions
		 */
		public CastInfo(String typeString, int arrayStart, int arrayCount) {
			this.typeString = typeString;
			this.arrayStart = arrayStart;
			this.arrayCount = arrayCount;
		}
		
		/**
		 * Create an instance of casting information for casting to type (only)
		 * @param typeString must be non-<code>null</code>; the C/C++ type to which to cast the expression (e.g. "char**")
		 */
		public CastInfo(String typeString) {
			if (typeString == null)
				throw new IllegalArgumentException();
			this.typeString = typeString;
			this.arrayStart = this.arrayCount = 0;
		}
		
		
		/**
		 * Create an instance of casting information for showing as an array (only)
		 * @param arrayStart the start index for viewing contents of the expression as an array
		 * @param arrayCount must be > 0; indicates to show [arrayStart ... arrayStart+arrayCount) as child expressions
		*/
		public CastInfo(int arrayStart, int arrayCount) {
			if (arrayCount <= 0)
				throw new IllegalArgumentException();
			this.typeString = null;
			this.arrayStart = arrayStart;
			this.arrayCount = arrayCount;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + arrayCount;
			result = prime * result + arrayStart;
			result = prime * result
					+ ((typeString == null) ? 0 : typeString.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CastInfo other = (CastInfo) obj;
			if (arrayCount != other.arrayCount)
				return false;
			if (arrayStart != other.arrayStart)
				return false;
			if (typeString == null) {
				if (other.typeString != null)
					return false;
			} else if (!typeString.equals(other.typeString))
				return false;
			return true;
		}

		/**
		 * Get the user-friendly type name.  This may be a post-processed string
		 * but should be semantically equivalent to the type used to create the context.
		 * @return type string, or <code>null</code> if no type casting performed
		 */
		public String getTypeString() {
			return typeString;
		}
		
		/**
		 * Get the start index for viewing children as an array.  (Only effective if #getCount() > 0)
		 * @return the index of the first element of the array. 0 means that 
		 * the original element is the first member of the array. This may be negative, too.
		 */
		public int getArrayStartIndex(){
			return arrayStart;
		}
		
		/**
		 * Get the number of elements to show when viewing children as an array.
		 * @return the array size, or <= 0 if not viewing as an array
		 */
		public int getArrayCount(){
			return arrayCount;
		}
	}
	
	/**
	 * This context identifies a casted expression.  Its parent is the original
	 * {@link IExpressionDMContext}.
	 */
	public interface ICastedExpressionDMContext extends IExpressionDMContext {
		CastInfo getCastInfo();
		
	}
	
	/**
	 * Create a variant of the expression which is casted with the given casting info.
	 * <p>
	 * If {@link ICastInfo#getTypeString()} is not <code>null</code>, such an expression should 
	 * report the casted type via {@link IExpressionDMData} and generate subexpressions accordingly.
	 * <p>
	 * Note that typically, a cast of a primitive type (int, double, etc.) to another
	 * primitive type is interpreted as "*(other_type*)&(expression)", not merely
	 * as casting the rvalue of "expression" to another type (which usually only 
	 * truncates or extends the value without much benefit). 
	 * <p>
	 * If {@link ICastInfo#getArrayCount()} is greater than <code>0</code>, the expression should
	 * yield that number of elements as subexpressions, as array elements, starting with index
	 * {@link ICastInfo#getArrayStartIndex()}.  (This does not affect the address of the 
	 * expression itself, only which children are returned.)  
	 * <p>
	 * The expected semantics of an array cast ranging from J to K are to take a
	 * pointer-valued expression whose base type is size N, evaluate its value
	 * to A, and yield array elements of the pointer base type at locations
	 * <code>A + N*J</code>, <code>A + N*(J+1)</code>, ...,
	 * <code>A + N*(J+K-1)</code>.  But the address of the expression is <b>not</b> modified 
	 * when an array cast is applied.  
	 * <p>An implementation may provide its own semantics for viewing other data as arrays, if so desired.
	 * @param context an existing expression
	 * @param castInfo the casting information
	 * @return a casted expression data model context object that must be passed to the appropriate
     *          data retrieval routine to obtain the value of the expression.  The object must
     *          report the casted type (if any) via {@link #getExpressionData(IExpressionDMContext, DataRequestMonitor)}
     *          and report alternate children according to the array casting context via 
     *          {@link #getSubExpressionCount(IExpressionDMContext, DataRequestMonitor)}
     *          and {@link #getSubExpressions}.
	 */
	ICastedExpressionDMContext createCastedExpression(IExpressionDMContext context, 
			CastInfo castInfo);


}
