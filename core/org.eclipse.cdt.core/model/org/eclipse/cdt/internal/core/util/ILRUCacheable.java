package org.eclipse.cdt.internal.core.util;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

/**
 * Types implementing this interface can occupy a variable amount of space
 * in an LRUCache.  Cached items that do not implement this interface are
 * considered to occupy one unit of space.
 *
 * @see LRUCache
 * 
 * This interface is similar to the JDT ILRUCacheable interface.
 */
public interface ILRUCacheable {
	/**
	 * Returns the space the receiver consumes in an LRU Cache.  The default space
	 * value is 1.
	 *
	 * @return int Amount of cache space taken by the receiver
	 */
	public int getCacheFootprint();

}
