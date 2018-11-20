/*******************************************************************************
 * Copyright (c) 2008 Freescale and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.math.BigInteger;

/**
 * An extension of IAddressFactory that supports throwing an exception rather
 * than truncating the initialization value if the value is outside the range
 * supported by the factory.
 */
public interface IAddressFactory2 extends IAddressFactory {
	/**
	 * See {@link IAddressFactory#createAddress(String)}.
	 * Same contract except that the constructor will throw
	 * a NumberFormatException if the supplied initializer value
	 * is out of range (when 'truncate' is false). IAddressFactory
	 * methods implicitly truncate if the value is out of range.
	 */
	IAddress createAddress(String addr, boolean truncate);

	/**
	 * See {@link IAddressFactory#createAddress(String, int)}.
	 * Same contract except that the constructor will throw
	 * a NumberFormatException if the supplied initializer value
	 * is out of range (when 'truncate' is false). IAddressFactory
	 * methods implicitly truncate if the value is out of range.
	 */
	IAddress createAddress(String addr, int radix, boolean truncate);

	/**
	 * See {@link IAddressFactory#createAddress(BigInteger)}.
	 * Same contract except that the constructor will throw
	 * a NumberFormatException if the supplied initializer value
	 * is out of range (when 'truncate' is false). IAddressFactory
	 * methods implicitly truncate if the value is out of range.
	 */
	IAddress createAddress(BigInteger addr, boolean truncate);
}
