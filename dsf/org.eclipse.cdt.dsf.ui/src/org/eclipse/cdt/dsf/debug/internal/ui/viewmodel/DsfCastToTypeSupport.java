/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.model.ICastToArray;
import org.eclipse.cdt.debug.core.model.ICastToType;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.CastInfo;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.ICastedExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.MessagesForVariablesVM;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

/**
 * This provides {@link ICastToType} and {@link ICastToArray} support on
 * expression nodes.
 */
public class DsfCastToTypeSupport  {
	private final DsfServicesTracker serviceTracker;
	private final AbstractDMVMProvider dmvmProvider;
	private final SyncVariableDataAccess fSyncVariableDataAccess;
	 
    /** expression memento to casting context (TODO: persist these; bug 228301)*/
    private Map<String, CastInfo> fCastedExpressionStorage = new HashMap<String, CastInfo>();

    public class CastImplementation extends PlatformObject implements ICastToArray  {
		private final IExpressionDMContext exprDMC;
		private String memento;

		public CastImplementation(IExpressionDMContext exprDMC) {
			this.exprDMC = exprDMC;
			this.memento = createCastedExpressionMemento(exprDMC);
    	}
		
	    public class TestExpressions2Query extends Query<Boolean> {

	        public TestExpressions2Query() {
	            super();
	        }

	        @Override
	        protected void execute(final DataRequestMonitor<Boolean> rm) {
	            /*
	             * We're in another dispatch, so we must guard against executor
	             * shutdown again.
	             */
	            final DsfSession session = DsfSession.getSession(
	            		dmvmProvider.getSession().getId());
	            if (session == null) {
	                rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Debug session already shut down.", null)); //$NON-NLS-1$
	                rm.done();
	                return;
	            }

                DsfServicesTracker tracker = new DsfServicesTracker(
                		DsfUIPlugin.getBundleContext(), dmvmProvider.getSession().getId());
                IExpressions2 expressions2 = tracker.getService(IExpressions2.class);
                rm.setData(expressions2 != null);
                rm.done();
                tracker.dispose();
	        }
	    }

		private boolean isValid() {
	        TestExpressions2Query query = new TestExpressions2Query();
	        dmvmProvider.getSession().getExecutor().execute(query);

			try {
				/*
				 * Return value is irrelevant, any error would come through with an
				 * exception.
				 */
				return query.get();
			} catch (InterruptedException e) {
				assert false;
				return false;
			} catch (ExecutionException e) {
				return false;
			}
		}
		
		private void throwIfNotValid() throws DebugException {
        	if (!isValid())
        		throw new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, 
        				MessagesForVariablesVM.VariableVMNode_CannotCastVariable, null)); 
		}
		
        /*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToType#canCast()
		 */
	    @Override
        public boolean canCast() {
        	return isValid();
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToType#getCurrentType()
		 */
	    @Override
        public String getCurrentType() {
        	// get expected casted type first, if possible (if there's an error in the type,
        	// the expression might not evaluate successfully)
        	CastInfo castDMC = fCastedExpressionStorage.get(memento);
        	if (castDMC != null && castDMC.getTypeString() != null)
        		return castDMC.getTypeString();
        	
        	// else, get the actual type
        	IExpressionDMData data = fSyncVariableDataAccess.readVariable(exprDMC);
        	if (data != null)
        		return data.getTypeName();
        	
			return ""; //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToType#cast(java.lang.String)
		 */
	    @Override
        public void cast(String type) throws DebugException {
        	throwIfNotValid();
        	
        	CastInfo currentContext = fCastedExpressionStorage.get(memento);
        	
        	updateCastInformation(type, 
        			currentContext != null ? currentContext.getArrayStartIndex() : 0, 
        			currentContext != null ? currentContext.getArrayCount() : 0);
        	
		}

        /*
         * (non-Javadoc)
         * @see org.eclipse.cdt.debug.core.model.ICastToType#restoreOriginal()
         */
	    @Override
		public void restoreOriginal() throws DebugException {
			throwIfNotValid();
        	fCastedExpressionStorage.remove(memento);
        	fireExpressionChangedEvent(exprDMC);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToType#isCasted()
		 */
	    @Override
		public boolean isCasted() {
			if (isValid())
				return fCastedExpressionStorage.containsKey(memento);
			else
				return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToArray#canCastToArray()
		 */
	    @Override
		public boolean canCastToArray() {
			return isValid();
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToArray#castToArray(int, int)
		 */
	    @Override
		public void castToArray(int startIndex, int length)
				throws DebugException {
			throwIfNotValid();
        	
			CastInfo currentContext = fCastedExpressionStorage.get(memento);
			
        	updateCastInformation(currentContext != null ? currentContext.getTypeString() : null, 
        			startIndex,
        			length);
		}

		private void updateCastInformation(
				String type, int arrayStartIndex, 
				int arrayCount) {
			final CastInfo info = new CastInfo(type, arrayStartIndex, arrayCount);
			fCastedExpressionStorage.put(memento, info);
		    fireExpressionChangedEvent(exprDMC);
		}

		private class ExpressionChangedEvent extends AbstractDMEvent<IExpressionDMContext> implements IExpressionChangedDMEvent {
			public ExpressionChangedEvent(IExpressionDMContext context) {
				super(context);
			}
		}
		
		private void fireExpressionChangedEvent(IExpressionDMContext exprDMC) {
			ExpressionChangedEvent event = new ExpressionChangedEvent(exprDMC);
			dmvmProvider.handleEvent(event);
		}
    }
    
	public DsfCastToTypeSupport(DsfSession session, AbstractDMVMProvider dmvmProvider, SyncVariableDataAccess fSyncVariableDataAccess) {
		this.dmvmProvider = dmvmProvider;
		this.fSyncVariableDataAccess = fSyncVariableDataAccess;
		this.serviceTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.ICastSupportTarget#createCastedExpressionMemento(org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext, java.lang.String)
	 */
	public String createCastedExpressionMemento(IExpressionDMContext exprDMC) {
		// go to the original variable first
		if (exprDMC instanceof ICastedExpressionDMContext) {
			IExpressionDMContext origExpr = DMContexts.getAncestorOfType(exprDMC.getParents()[0], IExpressionDMContext.class);
			if (origExpr == null) {
				assert false;
			} else {
				exprDMC = origExpr;
			}
		}
		
		// TODO: the memento doesn't really strictly define the expression's context;
		// we should fetch module name, function name, etc. to be more useful (but do that asynchronously)
		String expression = exprDMC.getExpression();
		String memento = exprDMC.getSessionId() + "." + expression; //$NON-NLS-1$ 
		return memento;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.ICastSupportTarget#replaceWihCastedExpression(org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext)
	 */
	public IExpressionDMContext replaceWithCastedExpression(
			IExpressionDMContext exprDMC) {
		IExpressions2 expression2Service = serviceTracker.getService(IExpressions2.class);
		if (expression2Service == null)
			return exprDMC;
		
		if (!fCastedExpressionStorage.isEmpty()) {
			String memento = createCastedExpressionMemento(exprDMC);
			CastInfo castInfo = fCastedExpressionStorage.get(memento);
			if (castInfo != null) {
				return expression2Service.createCastedExpression(exprDMC, castInfo);
			}
		}
		return exprDMC;
	}

	/**
	 * Get the ICastToArray (and ICastToType) implementation for the expression.
	 * This does not necessarily return a unique object for each call. 
	 * @param exprDMC
	 * @return {@link ICastToArray}
	 */
	public ICastToArray getCastImpl(IExpressionDMContext exprDMC) {
		return new CastImplementation(exprDMC);
	}
}
