/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.internal.testsubsystem;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemConfiguration;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystemNode;
import org.eclipse.rse.ui.view.AbstractSystemRemoteAdapterFactory;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Adapter factory for subsystem adapters.
 */
public class TestSubSystemAdapterFactory extends AbstractSystemRemoteAdapterFactory
	implements IAdapterFactory {

	private TestSubSystemAdapter subSystemAdapter = new TestSubSystemAdapter();
	private TestSubSystemNodeAdapter subSystemNodeAdapter = new TestSubSystemNodeAdapter();
	private TestSubSystemConfigurationAdapter SubSystemConfigAdapter = new TestSubSystemConfigurationAdapter();

	/**
	 * Constructor.
	 */
	public TestSubSystemAdapterFactory() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemRemoteAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		Object adapter = null;
		if (adaptableObject instanceof ITestSubSystem) {
			adapter = subSystemAdapter;
		}
		if (adaptableObject instanceof ITestSubSystemNode) {
			adapter = subSystemNodeAdapter;
		}
		if (adaptableObject instanceof ITestSubSystemConfiguration) {
			adapter = SubSystemConfigAdapter;
		}
		
		if (adapter != null && adapter instanceof ISystemViewElementAdapter && adapterType == IPropertySource.class) {
			((ISystemViewElementAdapter)adapter).setPropertySourceInput(adaptableObject);
		}
		
		return adapter;
	}

}
