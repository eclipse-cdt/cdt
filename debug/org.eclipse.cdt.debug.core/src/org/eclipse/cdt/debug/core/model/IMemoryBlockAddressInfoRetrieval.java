/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import java.math.BigInteger;

import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.model.IMemoryBlock;

/**
 * An implementation that offers the possibility to request information related to addresses for a given memory block
 * and within a specific context. It also offers the possibility to register listeners, listeners that can receive
 * notifications of changes/updates to the address information
 * 
 * @since 8.0
 */
public interface IMemoryBlockAddressInfoRetrieval {

	enum EventType {
		STOPPED, RESUMED, VALUE_CHANGED
	}

	/**
	 * Information item to a memory address or range
	 */
	public interface IMemoryBlockAddressInfoItem {
		/**
		 * @return The unique id for this item
		 */
		String getId();

		/**
		 * @return The type of information
		 */
		String getInfoType();

		/**
		 * @return The label or name for this entry
		 */
		String getLabel();

		/**
		 * Update the label or name for this entry
		 */
		void setLabel(String label);

		/**
		 * @return The memory address this information is associated to
		 */
		BigInteger getAddress();

		/**
		 * @param address Set / Update this item's address
		 */
		void setAddress(BigInteger address);

		/**
		 * @return The range of addressable units this information applies to
		 */
		BigInteger getRangeInAddressableUnits();

		void setRangeInAddressableUnits(BigInteger length);

		/**
		 * @return A preferred color to mark this entry
		 */
		int getRegionRGBColor();

		void setRegionRGBColor(int color);
	}

	/**
	 * An async request for information items, triggering the callback via the method done()
	 */
	public interface IGetMemoryBlockAddressInfoReq extends IRequest {
		/**
		 * @return The different types of items available
		 */
		String[] getAddressInfoItemTypes();

		/**
		 * @return The subset of items of the given type
		 */
		IMemoryBlockAddressInfoItem[] getAddressInfoItems(String type);

		/**
		 * @return The full set of items i.e. including all types
		 */
		IMemoryBlockAddressInfoItem[] getAddressInfoItems();

		/**
		 * Sets the address information items of the given type
		 */
		void setAddressInfoItems(String type, IMemoryBlockAddressInfoItem[] items);
	}

	/**
	 * Interface which defines the access to the listeners of address information updates
	 */
	public interface IAddressInfoUpdateListener {
		void handleAddressInfoUpdate(EventType type, Object update);
	}

	/**
	 * @param request
	 *            An asynchronous request of Address information providing the callback method "done"
	 */
	void getMemoryBlockAddressInfo(Object context, IMemoryBlock memoryBlock, IGetMemoryBlockAddressInfoReq request);

	/**
	 * Register the a listener so it can receive notifications of changes to address information items
	 * 
	 * @param listener
	 */
	void registerAddressInfoUpdateListener(IAddressInfoUpdateListener listener);

	/**
	 * Removes a listener so it no longer receive notifications
	 * 
	 * @param listener
	 */
	void removeAddressInfoUpdateListener(IAddressInfoUpdateListener listener);

}
