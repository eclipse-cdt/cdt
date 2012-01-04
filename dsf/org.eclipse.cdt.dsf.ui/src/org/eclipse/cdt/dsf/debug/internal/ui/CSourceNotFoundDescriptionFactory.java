/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui;

import java.util.HashMap;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.internal.core.sourcelookup.ICSourceNotFoundDescription;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.MessagesForLaunchVM;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdapterFactory;

import com.ibm.icu.text.MessageFormat;

/**
 * This factory provides an instance of ICSourceNotFoundDescription that
 * can generate a description of a IFrameDMContext.
 *
 */
public class CSourceNotFoundDescriptionFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(ICSourceNotFoundDescription.class) &&
				adaptableObject instanceof IFrameDMContext)
		{
			final IFrameDMContext frameDMC = (IFrameDMContext) adaptableObject;
			return new ICSourceNotFoundDescription() {

				@Override
				public String getDescription() {
		               Query<IStack.IFrameDMData> query = new Query<IStack.IFrameDMData>() {
		                    @Override
		                    protected void execute(DataRequestMonitor<IStack.IFrameDMData> rm) {
		                        DsfServicesTracker tracker = 
		                            new DsfServicesTracker(DsfUIPlugin.getBundleContext(), frameDMC.getSessionId());
		                        
								IStack stack = tracker.getService(IStack.class);
								if (stack != null) {
									stack.getFrameData(frameDMC, rm);
		                        } else {
		                            rm.setData(null);
		                            rm.done();
		                        }
		                        tracker.dispose();
		                    }
		                };	                
		                DsfSession session = DsfSession.getSession(frameDMC.getSessionId());
		                if (session != null && session.getExecutor() != null)
		                {
			                session.getExecutor().execute(query);
			                try {
			                	IFrameDMData dmData = query.get();
								return getFrameDescription(dmData);
							} catch (Exception e) {
								return frameDMC.toString();
							}
		                }
		                return frameDMC.toString();
				}};
		}
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ICSourceNotFoundDescription.class };
	}
	
	/** Creates a brief description of stack frame data.
	 * Based on code in StackFrameVMNode.
	 * @param frame
	 * @return the frame description
	 */
	private static String getFrameDescription(IStack.IFrameDMData frame)
	{
		String formatString = ""; //$NON-NLS-1$
		String[] propertyNames = null;
		HashMap<String, Object> properties = new HashMap<String, Object>();
		fillFrameDataProperties(properties, frame);

		Integer line = (Integer)properties.get(ILaunchVMConstants.PROP_FRAME_LINE);
		String file = (String)properties.get(ILaunchVMConstants.PROP_FRAME_FILE);
		String function = (String)properties.get(ILaunchVMConstants.PROP_FRAME_FUNCTION);
		String module = (String)properties.get(ILaunchVMConstants.PROP_FRAME_MODULE);
		if (line != null && line >= 0 && file != null && file.length() > 0)
		{
			if (function != null && function.contains(")")) //$NON-NLS-1$
				formatString = MessagesForLaunchVM.StackFramesVMNode_No_columns__text_format;
			else
				formatString = MessagesForLaunchVM.StackFramesVMNode_No_columns__add_parens__text_format;
			propertyNames = new String[] { 
                    ILaunchVMConstants.PROP_FRAME_ADDRESS, 
                    ILaunchVMConstants.PROP_FRAME_FUNCTION, 
                    ILaunchVMConstants.PROP_FRAME_FILE, 
                    ILaunchVMConstants.PROP_FRAME_LINE, 
                    ILaunchVMConstants.PROP_FRAME_COLUMN, 
                    ILaunchVMConstants.PROP_FRAME_MODULE};
		}
		else if (function != null && function.length() > 0 && module != null && module.length() > 0)
		{
			if (function.contains(")")) //$NON-NLS-1$
				formatString = MessagesForLaunchVM.StackFramesVMNode_No_columns__No_line__text_format;
			else
				formatString = MessagesForLaunchVM.StackFramesVMNode_No_columns__add_parens__text_format;
			propertyNames = new String[] { 
                    ILaunchVMConstants.PROP_FRAME_ADDRESS, 
                    ILaunchVMConstants.PROP_FRAME_FUNCTION, 
                    ILaunchVMConstants.PROP_FRAME_MODULE};
		}
		else if (module != null && module.length() > 0)
		{
			formatString = MessagesForLaunchVM.StackFramesVMNode_No_columns__No_function__text_format;
			propertyNames = new String[] { 
                    ILaunchVMConstants.PROP_FRAME_ADDRESS, 
                    ILaunchVMConstants.PROP_FRAME_MODULE};
		}
		else if (function != null && function.length() > 0)
		{
			if (function.contains(")")) //$NON-NLS-1$
				formatString = MessagesForLaunchVM.StackFramesVMNode_No_columns__No_module__text_format;
			else
				formatString = MessagesForLaunchVM.StackFramesVMNode_No_columns__No_module__add_parens__text_format;
			propertyNames = new String[] { 
                    ILaunchVMConstants.PROP_FRAME_ADDRESS, 
                    ILaunchVMConstants.PROP_FRAME_FUNCTION};
		}
		else
		{
			formatString = MessagesForLaunchVM.StackFramesVMNode_No_columns__Address_only__text_format;
			propertyNames = new String[] { 
					ILaunchVMConstants.PROP_FRAME_ADDRESS};
		}

        Object[] propertyValues = new Object[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            propertyValues[i] = properties.get(propertyNames[i]); 
        }

		return new MessageFormat(formatString).format(propertyValues, new StringBuffer(), null).toString();
	}
	
	private static void fillFrameDataProperties(java.util.Map<String,Object> properties, IFrameDMData data) {
        IAddress address = data.getAddress();
        if (address != null) {
        	properties.put(ILaunchVMConstants.PROP_FRAME_ADDRESS, "0x" + address.toString(16)); //$NON-NLS-1$
        }
        properties.put(ILaunchVMConstants.PROP_FRAME_FILE, data.getFile());
        properties.put(ILaunchVMConstants.PROP_FRAME_FUNCTION, data.getFunction());
        properties.put(ILaunchVMConstants.PROP_FRAME_LINE, data.getLine());
        properties.put(ILaunchVMConstants.PROP_FRAME_COLUMN, data.getColumn());
        properties.put(ILaunchVMConstants.PROP_FRAME_MODULE, data.getModule());        
    }

}
