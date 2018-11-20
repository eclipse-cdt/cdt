/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.Assert;

/**
 * Mock Language Settings Provider that keeps count how many times it has been registered.
 */
public class MockListenerRegisterer extends LanguageSettingsSerializableProvider
		implements ILanguageSettingsEditableProvider, ICListenerAgent {
	private static MockListenerManager mockListenerManager = new MockListenerManager();

	private static class MockListenerManager {
		private class ListenerCount {
			private MockListenerRegisterer listener;
			private int count;

			public ListenerCount(MockListenerRegisterer l, int cnt) {
				listener = l;
				count = cnt;
			}
		}

		private List<ListenerCount> register = new ArrayList<>();

		public void registerListener(MockListenerRegisterer listener) {
			for (ListenerCount lc : register) {
				if (lc.listener == listener) {
					lc.count++;
					return;
				}
			}

			register.add(new ListenerCount(listener, 1));
		}

		public void unregisterListener(MockListenerRegisterer listener) {
			for (ListenerCount lc : register) {
				if (lc.listener == listener) {
					lc.count--;
					Assert.isTrue(lc.count >= 0);
					return;
				}
			}

			// attempt to unregister non-registered listener
			Assert.isTrue(false);
		}

		/**
		 * Note that that count includes all listeners with that id.
		 */
		public int getCount(String id) {
			int count = 0;

			for (ListenerCount lc : register) {
				if (lc.listener.getId().equals(id)) {
					count = count + lc.count;
				}
			}

			return count;
		}
	}

	public MockListenerRegisterer() {
		super();
	}

	public MockListenerRegisterer(String id, String name) {
		super(id, name);
	}

	@Override
	public void registerListener(ICConfigurationDescription cfgDescription) {
		mockListenerManager.registerListener(this);
	}

	@Override
	public void unregisterListener() {
		mockListenerManager.unregisterListener(this);
	}

	@Override
	public MockListenerRegisterer cloneShallow() throws CloneNotSupportedException {
		return (MockListenerRegisterer) super.cloneShallow();
	}

	@Override
	public MockListenerRegisterer clone() throws CloneNotSupportedException {
		return (MockListenerRegisterer) super.clone();
	}

	public static int getCount(String id) {
		return mockListenerManager.getCount(id);
	}
}