/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IExpressions3;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IGDBPatternMatchingExpressions;
import org.eclipse.cdt.dsf.mi.service.IMIExpressions;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterDMC;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * @since 4.2
 */
public class GDBPatternMatchingExpressions extends AbstractDsfService implements IGDBPatternMatchingExpressions, ICachingService {
     
	protected static class GlobExpressionDMC implements IExpressionDMContext {
    	 IExpressionDMContext fExprDelegate;
    	 
    	 public GlobExpressionDMC(IExpressionDMContext exprDmc) {
    		 fExprDelegate = exprDmc;
    	 }

		@Override
		public String getExpression() {
			return fExprDelegate.getExpression();
		}

		@Override
		public String getSessionId() {
			return fExprDelegate.getSessionId();
		}
		
		@Override
		public IDMContext[] getParents() {
			return fExprDelegate.getParents();
		};
		
		@SuppressWarnings("rawtypes")
		@Override
		public Object getAdapter(Class adapterType) {
			return fExprDelegate.getAdapter(adapterType);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof GlobExpressionDMC)) return false;
			
			return ((GlobExpressionDMC)obj).fExprDelegate.equals(fExprDelegate);
		}

		@Override
		public int hashCode() {
			return fExprDelegate.hashCode();
		}
     }
     
	protected static class GlobExpressionDMData implements IExpressionDMDataExtension {
		private final String fRelativeExpression;
		private final int fNumChildren;

		public GlobExpressionDMData(String expr, int numChildren) {
			assert expr != null;
			
            fRelativeExpression = expr;
            fNumChildren = numChildren;
		}
		
		@Override
		public String getName() {
			if (hasChildren()) {
				return String.format("%s (%d matches)", fRelativeExpression, fNumChildren);
			}
			
			return String.format("%s (no match)", fRelativeExpression);
		}

		@Override
		public BasicType getBasicType() {
			return IExpressionDMData.BasicType.array;
		}

		@Override
		public String getTypeName() {
			return "Glob-pattern";
		}

		@Override
		public String getEncoding() {
			return null;
		}

		@Override
		public String getTypeId() {
			return null;
		}

		@Override
		public Map<String, Integer> getEnumerations() {
			return new HashMap<String, Integer>();
		}

		@Override
		public IRegisterDMContext getRegister() {
			return null;
		}

		@Override
		public boolean hasChildren() {
			return fNumChildren > 0;
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) return true;
			if (!(other instanceof GlobExpressionDMData)) return false;
			return fRelativeExpression.equals(((GlobExpressionDMData)other).fRelativeExpression);
		}
		
		@Override
		public int hashCode() {
			return fRelativeExpression.hashCode();
		}
		
		@Override
		public String toString() {
			return "Glob: " + fRelativeExpression; //$NON-NLS-1$
		}
	}
	
	private IMIExpressions fDelegate;
	
	public GDBPatternMatchingExpressions(DsfSession session, IExpressions delegate) {
		super(session);
		fDelegate = (IMIExpressions)delegate;
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(
				new ImmediateRequestMonitor(requestMonitor) { 
					@Override
					public void handleSuccess() {
						doInitialize(requestMonitor);
					}});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {
		// Our delegate should not be initialized yet, as we have no
		// good way to unregister it.
		assert !fDelegate.isRegistered();

		// We must first register this service to let the original
		// expression service know that it should not register itself.
		register(new String[] { IExpressions.class.getName(), 
								IExpressions2.class.getName(),
								IExpressions3.class.getName(),
								IMIExpressions.class.getName() }, 
				 new Hashtable<String, String>());
		
		// Second, we initialize the delegate so it can perform its duties
		fDelegate.initialize(requestMonitor);
	}

	@Override
	public void shutdown(final RequestMonitor requestMonitor) {
		fDelegate.shutdown(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			protected void handleSuccess() {
				unregister();
				GDBPatternMatchingExpressions.super.shutdown(requestMonitor);
			}
		});
	}

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	@Override
	public void getExpressionDataExtension(final IExpressionDMContext dmc, final DataRequestMonitor<IExpressionDMDataExtension> rm) {
		if (dmc instanceof GlobExpressionDMC) {
			getSubExpressionCount(dmc, -1, new ImmediateDataRequestMonitor<Integer>(rm) {
				@Override
				protected void handleSuccess() {
					rm.done(new GlobExpressionDMData(((GlobExpressionDMC)dmc).getExpression(), getData()));
				}
			});
    		return;
    	}
		
		fDelegate.getExpressionDataExtension(dmc, rm);
	}

	@Override
	public IExpressionDMContext createExpression(IDMContext ctx, String expression) {
		IExpressionDMContext expressionDmc = fDelegate.createExpression(ctx, expression);
		
		if (isGlobExpression(expression)) {
			return new GlobExpressionDMC(expressionDmc);
		} else {
			return expressionDmc;
		}
	}
	
	@Override
	public ICastedExpressionDMContext createCastedExpression(IExpressionDMContext context, CastInfo castInfo) {
		assert (!(context instanceof GlobExpressionDMC));
		
		return fDelegate.createCastedExpression(context, castInfo);
	}

	@Override
	public void getExpressionData(final IExpressionDMContext dmc, final DataRequestMonitor<IExpressionDMData> rm) {
		if (dmc instanceof GlobExpressionDMC) {
			getSubExpressionCount(dmc, -1, new ImmediateDataRequestMonitor<Integer>(rm) {
				@Override
				protected void handleSuccess() {
					rm.done(new GlobExpressionDMData(((GlobExpressionDMC)dmc).getExpression(), getData()));
				}
			});
    		return;
    	}
		
		fDelegate.getExpressionData(dmc, rm);
	}

	@Override
	public void getExpressionAddressData(IExpressionDMContext dmc, DataRequestMonitor<IExpressionDMAddress> rm) {
		if (dmc instanceof GlobExpressionDMC) {
    		rm.done(new IExpressionDMLocation() {
				@Override
				public IAddress getAddress() {
					return IExpressions.IExpressionDMLocation.INVALID_ADDRESS;
				}
				@Override
				public int getSize() {
					return 0;
				}
				@Override
				public String getLocation() {
					return ""; //$NON-NLS-1$
				}
    		});
    		return;
    	}
		
		fDelegate.getExpressionAddressData(dmc, rm);
	}

	@Override
	public void getSubExpressions(IExpressionDMContext exprCtx, DataRequestMonitor<IExpressionDMContext[]> rm) {
		if (exprCtx instanceof GlobExpressionDMC) {
			matchGlobExpression((GlobExpressionDMC)exprCtx, -1, -1, rm);
		} else {
			fDelegate.getSubExpressions(exprCtx, rm);
		}
	}

	@Override
	public void getSubExpressions(IExpressionDMContext exprCtx, int startIndex, int length, DataRequestMonitor<IExpressionDMContext[]> rm) {
		if (exprCtx instanceof GlobExpressionDMC) {
			matchGlobExpression((GlobExpressionDMC)exprCtx, startIndex, length, rm);
		} else {
			fDelegate.getSubExpressions(exprCtx, startIndex, length, rm);	
		}
	}

	@Override
	public void getSubExpressionCount(IExpressionDMContext dmc, DataRequestMonitor<Integer> rm) {
		if (dmc instanceof GlobExpressionDMC) {
			matchGlobExpression((GlobExpressionDMC)dmc, rm);
		} else {
			fDelegate.getSubExpressionCount(dmc, rm);
		}
	}

	@Override
	public void getSubExpressionCount(IExpressionDMContext dmc, int maxNumberOfChildren, DataRequestMonitor<Integer> rm) {
		if (dmc instanceof GlobExpressionDMC) {
			matchGlobExpression((GlobExpressionDMC)dmc, rm);
		} else {
			fDelegate.getSubExpressionCount(dmc, maxNumberOfChildren, rm);
		}
	}

	@Override
	public void getBaseExpressions(IExpressionDMContext exprContext, DataRequestMonitor<IExpressionDMContext[]> rm) {
		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
	}

	@Override
	public void canWriteExpression(IExpressionDMContext dmc, DataRequestMonitor<Boolean> rm) {
		if (dmc instanceof GlobExpressionDMC) {
    		rm.done(false);
    		return;
    	}
		
		fDelegate.canWriteExpression(dmc, rm);	
	}

	@Override
	public void writeExpression(IExpressionDMContext dmc, String expressionValue, String formatId, RequestMonitor rm) {
		assert !(dmc instanceof GlobExpressionDMC);
		fDelegate.writeExpression(dmc, expressionValue, formatId, rm);	
	}

	@Override
	public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
		fDelegate.getAvailableFormats(dmc, rm);	
	}

	@Override
	public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
		return fDelegate.getFormattedValueContext(dmc, formatId);
	}

	@Override
	public void getFormattedExpressionValue(FormattedValueDMContext dmc, DataRequestMonitor<FormattedValueDMData> rm) {
		if (DMContexts.getAncestorOfType(dmc, GlobExpressionDMC.class) != null) {
    		rm.done(new FormattedValueDMData("Glob-pattern")); //$NON-NLS-1$
    		return;
    	}
		
		fDelegate.getFormattedExpressionValue(dmc, rm);
	}

	@Override
	public void safeToAskForAllSubExpressions(IExpressionDMContext dmc,	DataRequestMonitor<Boolean> rm) {
		if (dmc instanceof GlobExpressionDMC) {
    		rm.done(true);
    		return;
    	}
		
		fDelegate.safeToAskForAllSubExpressions(dmc, rm);
	}

	@Override
	public void flushCache(IDMContext context) {
		if (fDelegate instanceof ICachingService) {
			((ICachingService)fDelegate).flushCache(context);		
		}
	}
	
	/** @since 4.1 */
	protected boolean isGlobExpression(String expr) {
		// We support the glob-pattern '*' to indicate all local variables
		if (expr.equals("*")) { //$NON-NLS-1$
			return true;
		}
		
		// We support glob-expressions for registers
		if (expr.startsWith("$")) { //$NON-NLS-1$
			// see: 'man glob'
			if (expr.indexOf('*') != -1 || expr.indexOf('?') != -1 || expr.indexOf('[') != -1) {
				return true;
			}
		}
		return false;
	}
	
	/** @since 4.1 */
	protected void matchGlobExpression(final GlobExpressionDMC globDmc, int startIndex, int length, final DataRequestMonitor<IExpressionDMContext[]> rm) {
		if (globDmc.getExpression().equals("*")) { //$NON-NLS-1$
			matchLocals(globDmc, startIndex, length, rm);
			return;
		}
		
		// Currently, we only support glob-expressions for registers, so
		// we only need to match the glob-expression with register names
		assert globDmc.getExpression().startsWith("$"); //$NON-NLS-1$
		
		final IRegisters registerService = getServicesTracker().getService(IRegisters.class);
		if (registerService == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Register service unavailable", null)); //$NON-NLS-1$
			return;
		}
		
		final int startIndex1 = (startIndex < 0) ? 0 : startIndex;
		final int length1 = (length < 0) ? Integer.MAX_VALUE : length;

		registerService.getRegisterGroups(globDmc, new ImmediateDataRequestMonitor<IRegisterGroupDMContext[]>(rm) {
			@Override
			protected void handleSuccess() {
				registerService.getRegisters(
   	   				    new CompositeDMContext(new IDMContext[] { getData()[0], globDmc } ), 
   	   		            new ImmediateDataRequestMonitor<IRegisterDMContext[]>(rm) {
   	   		            	@Override
   	   		            	protected void handleSuccess() {
   	   		            		assert getData() instanceof MIRegisterDMC[];
   	   		            		ArrayList<IExpressionDMContext> matches = new ArrayList<IExpressionDMContext>();
   	   		            		for (MIRegisterDMC register : (MIRegisterDMC[])getData()) {
   	   		            			String potentialMatch = "$"+register.getName();  //$NON-NLS-1$
   	   		            			if (globMatches(globDmc.getExpression(), potentialMatch)) {
   	   		            				matches.add(createExpression(globDmc, potentialMatch)); 
   	   		            			}
   	   		            		}

   	   		            		int numChildren = matches.size() - startIndex1;
   	   		            		numChildren = Math.min(length1, numChildren);
   	   		            		IExpressionDMContext[] matchesArray = new IExpressionDMContext[numChildren];
   	   		            		for (int i=0; i < numChildren; i++) {
   	   		            			matchesArray[i] = matches.get(startIndex1 + i);
   	   		            		}
   	   		            		rm.done(matchesArray);
   	   		            	}
   	   		            });
			}
		});
	}
	
	/** @since 4.1 */
	protected boolean globMatches(String globPattern, String content) {
	    boolean inBrackets = false;
	    char[] patternArray = globPattern.toCharArray();
	    char[] resultArray = new char[patternArray.length * 2 +2];
	    int pos = 0;

	    // Must match from the very beginning
	    resultArray[pos++] = '^';
		for (int i = 0; i < patternArray.length; i++) {
		    switch(patternArray[i]) {
		    	case '?':
		    		if (inBrackets) {
		    			resultArray[pos++] = '?';
		    		} else {
		    			resultArray[pos++] = '.';
		    		}
		    		break;
		    		
		    	case '*':
		    		if (!inBrackets) {
		    			resultArray[pos++] = '.';
		    		}
		    		resultArray[pos++] = '*';
		    		break;
		    		
		    	case '-':
		    		if (!inBrackets) {
		    			resultArray[pos++] = '\\';
		    		}
	    			resultArray[pos++] = '-';
		    		break;

		    	case '[':
		    		inBrackets = true;
		    		resultArray[pos++] = '[';

		    		if (i < patternArray.length - 1) {
		    			switch (patternArray[i+1]) {
		    			case '!':
		    			case '^':
		    				resultArray[pos++] = '^';
		    				i++;
		    				break;

		    			case ']':
		    				resultArray[pos++] = ']';
		    				i++;
		    				break;
		    			}
		    		}
		    		break;

		    	case ']':
		    		resultArray[pos++] = ']';
		    		inBrackets = false;
		    		break;

		    	case '\\':
		    		if (i == 0 && patternArray.length > 1 && patternArray[1] == '~') {
		    			resultArray[pos++] = '~';
		    			++i;
		    		} else {
		    			resultArray[pos++] = '\\';
		    			if (i < patternArray.length - 1 && "*?[]".indexOf(patternArray[i+1]) != -1) { //$NON-NLS-1$
		    				resultArray[pos++] = patternArray[++i];
		    			} else {
		    				resultArray[pos++] = '\\';
		    			}
		    		}
		    		break;

		    	default:
		    		// We must escape characters that are not digits or arrays
		    		// specifically, "^$.{}()+|<>"
		    		if (!Character.isLetterOrDigit(patternArray[i])) {
		    			resultArray[pos++] = '\\';
		    		}
		    		resultArray[pos++] = patternArray[i];
		    		break;
		    }
		}
		// Must match until the very end
	    resultArray[pos++] = '$';

		Pattern pattern = Pattern.compile(new String(resultArray, 0, pos), Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		return matcher.find();
	}

	/** @since 4.1 */
	private void matchLocals(final GlobExpressionDMC globDmc, int startIndex, int length, final DataRequestMonitor<IExpressionDMContext[]> rm) {
		
		final IStack stackService = getServicesTracker().getService(IStack.class);
		if (stackService == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Stack service unavailable", null)); //$NON-NLS-1$
			return;
		}
		
		IFrameDMContext frameCtx = DMContexts.getAncestorOfType(globDmc, IFrameDMContext.class);
		
		final int startIndex1 = (startIndex < 0) ? 0 : startIndex;
		final int length1 = (length < 0) ? Integer.MAX_VALUE : length;

		stackService.getLocals(frameCtx, new ImmediateDataRequestMonitor<IVariableDMContext[]>(rm) {
			@Override
			protected void handleSuccess() {
                IVariableDMContext[] localsDMCs = getData();
                
				int numChildren = localsDMCs.length - startIndex1;
				numChildren = Math.min(length1, numChildren);
          
                final IVariableDMData[] localsDMData = new IVariableDMData[numChildren];

                final CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm) {
                    @Override
                    public void handleSuccess() {
                        IExpressionDMContext[] expressionDMCs = new IExpressionDMContext[localsDMData.length];
                        
                        int i = 0;
                        for (IVariableDMData localDMData : localsDMData) {
                        	expressionDMCs[i++] = createExpression(globDmc, localDMData.getName());
                        }
                        rm.done(expressionDMCs);
                    }
                };
                int countRM = 0;
                
				for (int index=0; index < numChildren; index++) {
					final int finalIndex = index;
                    stackService.getVariableData(localsDMCs[startIndex1 + finalIndex], new ImmediateDataRequestMonitor<IVariableDMData>(crm) {
                            @Override
                            public void handleSuccess() {
                                localsDMData[finalIndex] = getData();
                                crm.done();
                            }
                    });
                    
                    countRM++;
                }		
                crm.setDoneCount(countRM);
			}
		});
	}
	
	/** @since 4.1 */
	protected void matchGlobExpression(GlobExpressionDMC globDmc, final DataRequestMonitor<Integer> rm) {
		matchGlobExpression(globDmc, -1, -1, new ImmediateDataRequestMonitor<IExpressionDMContext[]>(rm) {
			@Override
			protected void handleSuccess() {
				rm.done(getData().length);
			}
		});
	}
}
