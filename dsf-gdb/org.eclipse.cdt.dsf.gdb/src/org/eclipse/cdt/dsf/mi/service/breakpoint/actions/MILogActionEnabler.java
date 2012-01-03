/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.breakpoint.actions;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.breakpointactions.ILogActionEnabler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * @since 3.0
 */
public class MILogActionEnabler implements ILogActionEnabler {

    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fServiceTracker;
    private final IBreakpointsTargetDMContext fContext;

    public MILogActionEnabler(DsfExecutor executor, DsfServicesTracker serviceTracker, IDMContext context) {
        fExecutor = executor;
        fServiceTracker = serviceTracker;
        fContext = DMContexts.getAncestorOfType(context, IBreakpointsTargetDMContext.class);
    }

	@Override
    public String evaluateExpression(final String expression) throws Exception {
        // Use a Query to synchronize the call
        Query<String> query = new Query<String>() {
            @Override
            protected void execute(final DataRequestMonitor<String> drm) {
                final IExpressions expressionService = fServiceTracker.getService(IExpressions.class);
                if (expressionService != null) {
                    final IExpressionDMContext expressionDMC = expressionService.createExpression(fContext, expression);
                    String formatId = IFormattedValues.NATURAL_FORMAT;
                    FormattedValueDMContext valueDmc = expressionService.getFormattedValueContext(expressionDMC, formatId);
                    expressionService.getFormattedExpressionValue(
                            valueDmc, 
                            new DataRequestMonitor<FormattedValueDMData>(fExecutor, drm) {
                                @Override
                                protected void handleCompleted() {
                                    String result = expression + ": evaluation failed."; //$NON-NLS-1$
                                    if (isSuccess()) {
                                        result = getData().getFormattedValue();
                                    }
                                    drm.setData(result);
                                    drm.done();
                                }
                            }
                    );
                }
            }
        };
        fExecutor.execute(query);

        try {
            // The happy case
            return query.get();
        } catch (InterruptedException e) {
            return "Error evaluating \"" + expression + "\" (InterruptedException)."; //$NON-NLS-1$ //$NON-NLS-2$
        } catch (ExecutionException e) {
            return "Error evaluating \"" + expression + "\" (ExecutionException)."; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
