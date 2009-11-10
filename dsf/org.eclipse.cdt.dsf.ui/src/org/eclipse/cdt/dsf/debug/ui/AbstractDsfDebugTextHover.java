/**
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

package org.eclipse.cdt.dsf.debug.ui;

import org.eclipse.cdt.debug.ui.editors.AbstractDebugTextHover;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugModelProvider;

/**
 * An implementation of AbstractDebugTextHover using DSF services.
 * 
 * @since 2.1
 */
abstract public class AbstractDsfDebugTextHover extends AbstractDebugTextHover {

    /**
     * Returns the debug model ID of that this debug text hover is to be used for.
     */
    abstract protected String getModelId();
    
	private static class GetExpressionValueQuery extends Query<FormattedValueDMData> {
    	private final IFrameDMContext frame;
    	private final String expression;
		private DsfServicesTracker dsfServicesTracker;

        public GetExpressionValueQuery(IFrameDMContext frame, String expression, DsfServicesTracker dsfServicesTracker) {
            this.frame = frame;
			this.expression = expression;
			this.dsfServicesTracker = dsfServicesTracker;
        }

        @Override
        protected void execute(final DataRequestMonitor<FormattedValueDMData> rm) {
            DsfSession session = DsfSession.getSession(frame.getSessionId());
			IExpressions expressions = dsfServicesTracker.getService(IExpressions.class);
    		IExpressionDMContext expressionDMC = expressions.createExpression(frame, expression);
    		FormattedValueDMContext formattedValueContext = expressions.getFormattedValueContext(expressionDMC, IFormattedValues.NATURAL_FORMAT);
        	expressions.getFormattedExpressionValue(formattedValueContext,
        			new DataRequestMonitor<FormattedValueDMData>(session.getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    rm.setData(getData());
                    rm.done();
                }
                
                @Override
        		protected void handleFailure() {
        			rm.done();
        		}
            });
        }
    }

	protected IFrameDMContext getFrame() {
	    IAdaptable adaptable = getSelectionAdaptable();
	    if (adaptable != null) {
	        return (IFrameDMContext) adaptable.getAdapter(IFrameDMContext.class);
		}
		return null;
	}

	@Override
	protected boolean canEvaluate() {
	    if (getFrame() == null) {
	        return false;
	    }
	    
		IAdaptable adaptable = getSelectionAdaptable();
		if (adaptable != null) {
		    IDebugModelProvider modelProvider = (IDebugModelProvider)adaptable.getAdapter(IDebugModelProvider.class);
		    if (modelProvider != null) {
		        String[] models = modelProvider.getModelIdentifiers();
		        String myModel = getModelId();
		        for (int i = 0; i < models.length; i++) {
		            if (models[i].equals(myModel)) {
		                return true;
		            }
		        }
		    }
		}
		return false;
	}

	@Override
	protected String evaluateExpression(String expression) {
		IFrameDMContext frame = getFrame();
		String sessionId = frame.getSessionId();
		DsfServicesTracker dsfServicesTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), sessionId);
		try {
			GetExpressionValueQuery query = new GetExpressionValueQuery(frame, expression, dsfServicesTracker);
			DsfSession session = DsfSession.getSession(sessionId);
	        session.getExecutor().execute(query);
	        try {
	        	FormattedValueDMData data = query.get();
	        	if (data != null)
	        		return data.getFormattedValue();
	        } catch (Exception e) {
	        }
		} finally {
			dsfServicesTracker.dispose();
		}
        return null;
	}

}
