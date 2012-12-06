/*****************************************************************
 * Copyright (c) 2012 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Dallaway - DSF-GDB register format persistence (bug 395909)
 *****************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.IElementFormatProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterVMProvider;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterDMC;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * GDB View Model provider for the registers view.
 */
public class GdbRegisterVMProvider extends RegisterVMProvider implements IElementFormatProvider {

	private static String REGISTER_PERSISTABLE_PROPERTY = "org.eclipse.cdt.dsf.gdb.ui.registerPersistable"; //$NON-NLS-1$
	
	public GdbRegisterVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
	}
	
	protected String getRegisterKey(IRegisterDMContext ctx) {
		if (ctx instanceof MIRegisterDMC) {
			return ((MIRegisterDMC) ctx).getName(); // use MI register name as persistence key
		}
		return null;
	}
	
	@Override
	public void getActiveFormat(IPresentationContext context, IVMNode node, Object viewerInput, TreePath elementPath,
			DataRequestMonitor<String> rm) {
		
		Object p = context.getProperty(REGISTER_PERSISTABLE_PROPERTY);
		if (p instanceof ElementFormatPersistable) {
			ElementFormatPersistable persistable = (ElementFormatPersistable) p;
			Object x = elementPath.getLastSegment();
			if (x instanceof IDMVMContext) {
				String format = null;
				IDMContext dmc = ((IDMVMContext)x).getDMContext();
				if (dmc instanceof IRegisterDMContext) {
					format = persistable.getFormat(getRegisterKey((IRegisterDMContext)dmc));
				}

				rm.done(format);
				return;
			}
		}
		
		rm.done((String)null);
	}

	@Override
	public void setActiveFormat(IPresentationContext context, IVMNode[] node, Object viewerInput, TreePath[] elementPath, String format) {
		Object p = context.getProperty(REGISTER_PERSISTABLE_PROPERTY);
		ElementFormatPersistable persistable = null;
		if (p instanceof ElementFormatPersistable) {
			persistable = (ElementFormatPersistable) p;
		} else {
			persistable = new ElementFormatPersistable(RegisterPersistableFactory.FACTORY_ID);
			context.setProperty(REGISTER_PERSISTABLE_PROPERTY, persistable);
		}
		
		boolean changed = false;
		for (int i = 0; i < elementPath.length; i++) {
			Object x = elementPath[i].getLastSegment();
			if (x instanceof IDMVMContext) {
				IDMContext dmc = ((IDMVMContext)x).getDMContext();
				if (dmc instanceof IRegisterDMContext) {
					String key = getRegisterKey((IRegisterDMContext)dmc);
					if (key != null) {
						persistable.setFormat(key, format);
						changed = true;
					}
				}
			}
		}
		if (changed) {
			refresh();
		}
	}

	@Override
	public boolean supportFormat(IVMContext context) {
		if (context instanceof IDMVMContext) {
			// Make sure we are dealing with a register node and not a register group for example
			IDMContext dmc = ((IDMVMContext)context).getDMContext();
			if (dmc instanceof IRegisterDMContext) return true;
		}
		return false;
	}
}
