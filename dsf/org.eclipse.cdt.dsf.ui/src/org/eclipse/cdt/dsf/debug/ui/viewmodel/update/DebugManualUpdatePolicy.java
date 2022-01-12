/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.update;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueRetriever;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICacheEntry;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTester;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTesterExtension;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicyExtension;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ManualUpdatePolicy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreePath;

/**
 * Manual update policy with extensions specific for the debugger views.  It
 * properly handles the changes in active number format values in debug view.
 * This requires clearing of cached properties related to the active format
 * preference, but not clearing the formatted value data retrieved from the
 * service.
 *
 * @since 2.1
 */
public class DebugManualUpdatePolicy extends ManualUpdatePolicy implements IVMUpdatePolicyExtension {

	private final Set<String> fActiveNumberFormatPropertiesWithPrefixes;
	private final Set<String> fElementFormatPropertiesWithPrefixes;

	/**
	 * Creates a manual update policy for debug views.
	 */
	public DebugManualUpdatePolicy() {
		this(new String[0]);
	}

	/**
	 * Creates a manual update policy for debug views for models that retrieve
	 * multiple formatted values for each view entry.  The given prefixes
	 * distinguish the formatted values properties from each other.
	 *
	 * @see FormattedValueRetriever
	 * @see FormattedValueVMUtil#getPropertyForFormatId(String, String)
	 *
	 * @param prefixes Prefixes to use when flushing the active formatted value
	 * from VM cache.
	 */
	public DebugManualUpdatePolicy(String[] prefixes) {
		if (prefixes.length == 0) {
			fActiveNumberFormatPropertiesWithPrefixes = ACTIVE_NUMBER_FORMAT_PROPERTIES;
			fElementFormatPropertiesWithPrefixes = ELEMENT_FORMAT_PROPERTIES;
		} else {
			fActiveNumberFormatPropertiesWithPrefixes = new TreeSet<>(ACTIVE_NUMBER_FORMAT_PROPERTIES);
			fElementFormatPropertiesWithPrefixes = new TreeSet<>(ELEMENT_FORMAT_PROPERTIES);
			for (String prefix : prefixes) {
				fActiveNumberFormatPropertiesWithPrefixes
						.add((prefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT).intern());
				fActiveNumberFormatPropertiesWithPrefixes
						.add((prefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE).intern());
				fElementFormatPropertiesWithPrefixes
						.add((prefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT).intern());
				fElementFormatPropertiesWithPrefixes
						.add((prefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE).intern());
			}
		}

	}

	private static final Set<String> ACTIVE_NUMBER_FORMAT_PROPERTIES = new TreeSet<>();
	static {
		ACTIVE_NUMBER_FORMAT_PROPERTIES.add(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT);
		ACTIVE_NUMBER_FORMAT_PROPERTIES.add(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE);
		ACTIVE_NUMBER_FORMAT_PROPERTIES.add(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE);
	}

	private static final Set<String> ELEMENT_FORMAT_PROPERTIES = new TreeSet<>();
	static {
		ELEMENT_FORMAT_PROPERTIES.add(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT);
		ELEMENT_FORMAT_PROPERTIES.add(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE);
	}

	/**
	 * This specialized element update tester flushes the active number format
	 * property of the elemetn under consideration.  The partial property flush
	 * is performed only if the cache entry is not yet dirty.
	 */
	private IElementUpdateTester fNumberFormatPropertyEventUpdateTester = new IElementUpdateTesterExtension() {

		@Override
		public int getUpdateFlags(Object viewerInput, TreePath path) {
			return FLUSH_PARTIAL_PROPERTIES;
		}

		@Override
		public Collection<String> getPropertiesToFlush(Object viewerInput, TreePath path, boolean isDirty) {
			return fActiveNumberFormatPropertiesWithPrefixes;
		}

		@Override
		public boolean includes(IElementUpdateTester tester) {
			// includes ElementFormatUpdateTester as well?
			return tester.equals(this);
		}

		@Override
		public String toString() {
			return "Manual (refresh = false) update tester for an event that did not originate from the data model"; //$NON-NLS-1$
		}
	};

	@Override
	public IElementUpdateTester getElementUpdateTester(Object event) {
		if (event instanceof PropertyChangeEvent && IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE
				.equals(((PropertyChangeEvent) event).getProperty())) {
			return fNumberFormatPropertyEventUpdateTester;
		}
		if (event instanceof ElementFormatEvent) {
			return new ElementFormatUpdateTester(((ElementFormatEvent) event), fElementFormatPropertiesWithPrefixes);
		}
		return super.getElementUpdateTester(event);
	}

	@Override
	public boolean canUpdateDirtyProperty(ICacheEntry entry, String property) {
		return fActiveNumberFormatPropertiesWithPrefixes.contains(property);
	}
}
