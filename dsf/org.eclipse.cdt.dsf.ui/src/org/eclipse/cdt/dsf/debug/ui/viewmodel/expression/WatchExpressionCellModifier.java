/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format in editing (Bug 343021)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionManagerVMNode.NewExpressionVMC;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.IElementFormatProvider;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 */
@ThreadSafeAndProhibitedFromDsfExecutor("")
public class WatchExpressionCellModifier implements ICellModifier {

    /**
     * Constructor for the modifier requires a valid DSF session in order to 
     * initialize the service tracker.  
     * @param session DSF session this modifier will use.
     */
    public WatchExpressionCellModifier() {
    }

    @Override
	public boolean canModify(Object element, String property) {
        return IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(property) && 
               (getWatchExpression(element) != null  || element instanceof NewExpressionVMC); 
    }

    @Override
	public Object getValue(Object element, String property) {
        if (!IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(property)) return ""; //$NON-NLS-1$

        IWatchExpression expression = getWatchExpression(element);
        
        if (expression != null) {
            return expression.getExpressionText();
        }
        return ""; //$NON-NLS-1$
    }

    @Override
	public void modify(Object element, String property, Object value) {
        if (!IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(property)) return;
        if (!(value instanceof String)) return;
        
        String origStrValue = (String) value;
        String strValue = origStrValue.trim();
        IWatchExpression expression = getWatchExpression(element);
        IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager(); 
        if (expression != null) {
            if (strValue.length() != 0) {
                expression.setExpressionText(origStrValue);
            } else {
                // (bug 233111) If user entered a blank string, remove the expression.
                expressionManager.removeExpression(expression);
            }
        } else if (element instanceof NewExpressionVMC && strValue.length() != 0) {
            IWatchExpression watchExpression = expressionManager.newWatchExpression(origStrValue); 
            expressionManager.addExpression(watchExpression);            
        }
    }

    /**
     * Use query to get element format for a vm context from a given provider for given presentation context.
     * One use of this method is in cell modifier's getValue() and modify().
     * @param provider given provider
     * @param presCtx given presentation context
     * @param ctx vm context
     * @return element format, null if not available
     */
	protected String queryElementFormat(final IElementFormatProvider provider, final IPresentationContext presCtx, final IVMContext ctx) {
		DsfSession session = null;
		if (ctx instanceof IDMVMContext) {
			IDMContext dmctx = ((IDMVMContext) ctx).getDMContext();
			if (dmctx != null)
				session = DsfSession.getSession(dmctx.getSessionId());
		}
		if (session == null) {
			return null;
		}
		Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				// Since cell modifier does not provide the fully qualified
				// tree path of the element starting from root, this tree path
				// is just the leaf; this is somewhat different than how
				// FormatValueRetriever pass in a fully qualified tree path to
				// the IElementFormatProvider. It is believed that IVMContext
				// can be used to get its parents when needed. 
				TreePath treePath = new TreePath(new Object[] {ctx});
				Object viewerInput = null;
				IWorkbenchPart part = presCtx.getPart();
				if (part instanceof IDebugView) {
					Viewer viewer = ((IDebugView) part).getViewer();
					if (viewer != null) {
						viewerInput = viewer.getInput();
					}
				}
				provider.getActiveFormat(presCtx, ctx.getVMNode(), viewerInput, treePath,
						new DataRequestMonitor<String>(ImmediateExecutor.getInstance(), rm) {
					@Override
					protected void handleSuccess() {
						rm.setData(this.getData());
						super.handleSuccess();
					}
				});
			}
		};
		session.getExecutor().execute(query);
		try {
			return query.get(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// no op
		} catch (ExecutionException e) {
			// no op
		} catch (TimeoutException e) {
			// no op
		}
		return null;
	}

	/**
	 * Use query to get format from a given vm context. This method calls queryElementFormat
	 * if the vm provider associated with the vm context supports individual element format.
	 * If the vm provider does not support individual element format or queryElementFormat
	 * returns null, this method returns the preferred format of the view.
     * One use of this method is in cell modifier's getValue() and modify().
	 * @param ctx the given vm context
	 * @return the format
	 */
	protected String queryFormat(IVMContext ctx) {
		String formatId = null;
        IVMProvider vmprovider = ctx.getVMNode().getVMProvider();
        IPresentationContext presCtx = vmprovider.getPresentationContext();
        if (vmprovider instanceof IElementFormatProvider) {
        	formatId = queryElementFormat((IElementFormatProvider) vmprovider, presCtx, ctx);
        }
        if (formatId == null) {
            formatId = FormattedValueVMUtil.getPreferredFormat(presCtx);
        }
		return formatId;
	}

	private IWatchExpression getWatchExpression(Object element) {
        if (element instanceof IAdaptable) {
            return (IWatchExpression)((IAdaptable)element).getAdapter(IWatchExpression.class);
        }
        return null;
    }

}
