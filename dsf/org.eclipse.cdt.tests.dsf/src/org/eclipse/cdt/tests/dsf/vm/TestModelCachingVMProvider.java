/*******************************************************************************
 * Copyright (c) 2010, 2015 Wind River Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.BreakpointHitUpdatePolicy;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.DebugManualUpdatePolicy;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.AutomaticUpdatePolicy;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @since 2.2
 */
public class TestModelCachingVMProvider extends AbstractDMVMProvider {

	public static final String COLUMN_ID = "COLUMN_ID";
	public static final String COLUMN_FORMATTED_VALUE = "COLUMN_FORMATTED_VALUE";
	public static final String COLUMN_DUMMY_VALUE = "COLUMN_DUMMY_VALUE";
	private static final String[] COLUMNS = new String[] { COLUMN_ID, COLUMN_FORMATTED_VALUE, COLUMN_DUMMY_VALUE };

	private IPropertyChangeListener fPresentationContextListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			handleEvent(event);
		}
	};

	private static IColumnPresentation COLUMN_PRESENTATION = new IColumnPresentation() {
		@Override
		public void init(IPresentationContext context) {
		}

		@Override
		public void dispose() {
		};

		@Override
		public String[] getAvailableColumns() {
			return COLUMNS;
		};

		@Override
		public String getHeader(String id) {
			return id;
		};

		@Override
		public String getId() {
			return "ID";
		}

		@Override
		public ImageDescriptor getImageDescriptor(String id) {
			return null;
		}

		@Override
		public String[] getInitialColumns() {
			return COLUMNS;
		}

		@Override
		public boolean isOptional() {
			return false;
		}
	};

	public TestModelCachingVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
		super(adapter, context, session);

		setRootNode(new TestModelDMVMNode(this, session));
		addChildNodes(getRootVMNode(), new IVMNode[] { getRootVMNode() });

		context.addPropertyChangeListener(fPresentationContextListener);
	}

	@Override
	protected IVMUpdatePolicy[] createUpdateModes() {
		return new IVMUpdatePolicy[] { new AutomaticUpdatePolicy(),
				new DebugManualUpdatePolicy(new String[] { TestModelDMVMNode.PROP_PREFIX_DUMMY }),
				new BreakpointHitUpdatePolicy() };
	}

	public TestModelDMVMNode getNode() {
		return (TestModelDMVMNode) getRootVMNode();
	}

	@Override
	public void dispose() {
		getPresentationContext().removePropertyChangeListener(fPresentationContextListener);
		super.dispose();
	}

	public void postEvent(Object event) {
		super.handleEvent(event);
	}

	public TestElementVMContext getElementVMContext(IPresentationContext context, TestElement element) {
		return ((TestModelDMVMNode) getRootVMNode()).createVMContext(element);
	}

	@Override
	public String getColumnPresentationId(IPresentationContext context, Object element) {
		return COLUMN_PRESENTATION.getId();
	}

	@Override
	public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
		return COLUMN_PRESENTATION;
	}
}
