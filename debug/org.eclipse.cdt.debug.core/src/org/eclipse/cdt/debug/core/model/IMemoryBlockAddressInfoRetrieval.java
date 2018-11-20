/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import java.math.BigInteger;

import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.model.IMemoryBlock;

/**
 * An interface that offers the possibility to request information related to addresses for a given memory block
 * and within a specific context. It also offers the possibility to register listeners, listeners that can receive
 * notifications of changes/updates to the address information
 *
 * @since 8.0
 */
public interface IMemoryBlockAddressInfoRetrieval {

	/**
	 * An indication of the type of change which may render the current memory address information out of date
	 */
	enum EventType {
		STOPPED, RESUMED, VALUE_CHANGED
	}

	/**
	 * Information item for a memory address or range
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
	 * An async request for information items, triggering the callback via the method done().
	 * The method done() is expected to be overridden so when the request is successful this additional API
	 * can be used to retrieve the item information collected.
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
		IMemoryBlockAddressInfoItem[] getAllAddressInfoItems();

		/**
		 * Sets the address information items of the given type
		 */
		void setAddressInfoItems(String type, IMemoryBlockAddressInfoItem[] items);
	}

	/**
	 * Call-back interface used to receive notification of changes to address information items
	 */
	public interface IAddressInfoUpdateListener {
		/**
		 * This method will be called for each registered listener, when there is a session change that may render
		 * previous memory address information out of date
		 * @param update optional General purpose update object to e.g. determine changed values
		 */
		void handleAddressInfoUpdate(EventType type, Object update);
	}

	/**
	 * Triggers an asynchronous request for Memory block address information
	 *
	 * @param context A reference to a session context
	 * @param memoryBlock The memory block to be used in conjunction with the requested Address information
	 * @param request This is the async request instance. Overriding its method "done()" allows to read and
	 * use the information items collected
	 */
	void getMemoryBlockAddressInfo(Object context, IMemoryBlock memoryBlock, IGetMemoryBlockAddressInfoReq request);

	/**
	 * Register a listener so it can receive notifications of changes to address information items
	 *
	 * @param listener
	 */
	void addAddressInfoUpdateListener(IAddressInfoUpdateListener listener);

	/**
	 * Removes a listener so it no longer receives notifications
	 *
	 * @param The listener to remove. Nothing will happen if that listener is not registered already
	 */
	void removeAddressInfoUpdateListener(IAddressInfoUpdateListener listener);

}
