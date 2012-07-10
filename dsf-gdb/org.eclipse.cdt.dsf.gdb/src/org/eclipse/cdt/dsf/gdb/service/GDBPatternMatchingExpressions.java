/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
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

import com.ibm.icu.text.MessageFormat;

/**
 * Expressions service added as a layer above the standard Expressions service.
 * This layer allows to support group-expressions and glob-pattern matching.
 * Group-expressions give the user the ability to create a comma-separated
 * list of expressions in a single entry.
 * Glob-patterns are a way to specify a set of expressions that match the
 * pattern.
 * @since 4.2
 */
public class GDBPatternMatchingExpressions extends AbstractDsfService implements IGDBPatternMatchingExpressions, ICachingService {
	/**
	 * A regex representing each character that can be used to separate
	 * the different expressions contained in a group-expression.
	 * The [] are not part the characters, but are used in the regex format.
	 * Note that we don't allow a space separator because spaces are valid within
	 * an expression (e.g., i + 1)
	 */
	private final static String GROUP_EXPRESSION_SEPARATORS_REGEXP = "[,;]"; //$NON-NLS-1$

	/**
	 * A group-expression is an expression that requires expansion into a (potentially empty)
	 * list of sub-expressions.  Using a group-expression allows the user to create groups
	 * of expressions very quickly.
	 * 
	 * We support two aspects for group-expressions:
	 * 1- The glob syntax (http://www.kernel.org/doc/man-pages/online/pages/man7/glob.7.html)
	 *    This allows to user to specify glob-patterns to match different expressions.
	 * 2- Comma-separated expressions, each potentially using the glob-syntax
	 */
	protected static class GroupExpressionDMC implements IExpressionDMContext {
		
		/** 
		 * The expression context, as created by the main Expression service.
		 * We delegate the handling of the expression to it.
		 */
		private IExpressionDMContext fExprDelegate;
		
		/**
		 * The set of expressions making up the group expression.
		 * This list is the result of splitting the original expression
		 * and then trimming each resulting expression.
		 */
		private List<String> fExpressionsInGroup = null;

		public GroupExpressionDMC(IExpressionDMContext exprDmc) {
			fExprDelegate = exprDmc;
		}

		@Override
		public String getExpression() {
			return fExprDelegate.getExpression();
		}

		/**
		 * Returns an array representing the different expressions
		 * that make up this group-expression.
		 */
		public List<String> getExpressionsInGroup() {
			if (fExpressionsInGroup == null) {
				// Split the list
				String[] splitExpressions = getExpression().split(GROUP_EXPRESSION_SEPARATORS_REGEXP);
				
				// Remove any extra whitespace from each resulting expression,
				// and ignore any empty expressions.
				fExpressionsInGroup = new ArrayList<String>(splitExpressions.length);
				for (String expr : splitExpressions) {
					expr = expr.trim();
					if (!expr.isEmpty()) {
						fExpressionsInGroup.add(expr);
					}
				}
			}
			return fExpressionsInGroup;
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
			if (!(obj instanceof GroupExpressionDMC)) return false;
			
			return ((GroupExpressionDMC)obj).fExprDelegate.equals(fExprDelegate);
		}

		@Override
		public int hashCode() {
			return fExprDelegate.hashCode();
		}
	}
	
	/**
	 * The model data interface for group-expressions
	 */
	protected static class GroupExpressionDMData implements IExpressionDMDataExtension {
		private final String fRelativeExpression;
		private final int fNumChildren;

		public GroupExpressionDMData(String expr, int numChildren) {
			assert expr != null;
			
            fRelativeExpression = expr;
            fNumChildren = numChildren;
		}
		
		@Override
		public String getName() {
			return fRelativeExpression;
		}

		@Override
		public BasicType getBasicType() {
			return IExpressionDMData.BasicType.array;
		}

		@Override
		public String getTypeName() {
			return Messages.GroupPattern;
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
			if (!(other instanceof GroupExpressionDMData)) return false;
			return fRelativeExpression.equals(((GroupExpressionDMData)other).fRelativeExpression);
		}
		
		@Override
		public int hashCode() {
			return fRelativeExpression.hashCode();
		}
		
		@Override
		public String toString() {
			return "GroupExpr: " + fRelativeExpression; //$NON-NLS-1$
		}
	}
	
	/**
	 * The base expression service to which we delegate all non-group-expression logic.
	 */
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
	public IExpressionDMContext createExpression(IDMContext ctx, String expression) {
		IExpressionDMContext expressionDmc = fDelegate.createExpression(ctx, expression);
		
		if (isGroupExpression(expression)) {
			return new GroupExpressionDMC(expressionDmc);
		} else {
			return expressionDmc;
		}
	}

	@Override
	public ICastedExpressionDMContext createCastedExpression(IExpressionDMContext context, CastInfo castInfo) {
		// Cannot cast a GroupExpression
		assert (!(context instanceof GroupExpressionDMC));
		
		return fDelegate.createCastedExpression(context, castInfo);
	}

	@Override
	public void getExpressionDataExtension(final IExpressionDMContext dmc, final DataRequestMonitor<IExpressionDMDataExtension> rm) {
		if (dmc instanceof GroupExpressionDMC) {
			getSubExpressionCount(dmc, new ImmediateDataRequestMonitor<Integer>(rm) {
				@Override
				protected void handleSuccess() {
					rm.done(new GroupExpressionDMData(((GroupExpressionDMC)dmc).getExpression(), getData()));
				}
			});
    		return;
    	}
		
		fDelegate.getExpressionDataExtension(dmc, rm);
	}


	@Override
	public void getExpressionData(final IExpressionDMContext dmc, final DataRequestMonitor<IExpressionDMData> rm) {
		if (dmc instanceof GroupExpressionDMC) {
			getSubExpressionCount(dmc, new ImmediateDataRequestMonitor<Integer>(rm) {
				@Override
				protected void handleSuccess() {
					rm.done(new GroupExpressionDMData(((GroupExpressionDMC)dmc).getExpression(), getData()));
				}
			});
    		return;
    	}
		
		fDelegate.getExpressionData(dmc, rm);
	}

	@Override
	public void getExpressionAddressData(IExpressionDMContext dmc, DataRequestMonitor<IExpressionDMAddress> rm) {
		// A GroupExpression does not have an address
		if (dmc instanceof GroupExpressionDMC) {
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
		if (exprCtx instanceof GroupExpressionDMC) {
			matchGroupExpression((GroupExpressionDMC)exprCtx, -1, -1, rm);
		} else {
			fDelegate.getSubExpressions(exprCtx, rm);
		}
	}

	@Override
	public void getSubExpressions(IExpressionDMContext exprCtx, int startIndex, int length, DataRequestMonitor<IExpressionDMContext[]> rm) {
		if (exprCtx instanceof GroupExpressionDMC) {
			matchGroupExpression((GroupExpressionDMC)exprCtx, startIndex, length, rm);
		} else {
			fDelegate.getSubExpressions(exprCtx, startIndex, length, rm);	
		}
	}

	@Override
	public void getSubExpressionCount(IExpressionDMContext dmc, final DataRequestMonitor<Integer> rm) {
		if (dmc instanceof GroupExpressionDMC) {
			matchGroupExpression((GroupExpressionDMC)dmc, -1, -1, new ImmediateDataRequestMonitor<IExpressionDMContext[]>(rm) {
				@Override
				protected void handleSuccess() {
					rm.done(getData().length);
				}
			});
		} else {
			fDelegate.getSubExpressionCount(dmc, rm);
		}
	}

	@Override
	public void getSubExpressionCount(IExpressionDMContext dmc, int maxNumberOfChildren, final DataRequestMonitor<Integer> rm) {
		if (dmc instanceof GroupExpressionDMC) {
			// No need to worry about maxNumberOfChildren for the case of a group-expression, since there won't be
			// a very large amount of them.
			matchGroupExpression((GroupExpressionDMC)dmc, -1, -1, new ImmediateDataRequestMonitor<IExpressionDMContext[]>(rm) {
				@Override
				protected void handleSuccess() {
					rm.done(getData().length);
				}
			});
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
		// A GroupExpression's value cannot be modified
		if (dmc instanceof GroupExpressionDMC) {
    		rm.done(false);
    		return;
    	}
		
		fDelegate.canWriteExpression(dmc, rm);	
	}

	@Override
	public void writeExpression(IExpressionDMContext dmc, String expressionValue, String formatId, RequestMonitor rm) {
		// A GroupExpression's value cannot be modified
		assert !(dmc instanceof GroupExpressionDMC);
		fDelegate.writeExpression(dmc, expressionValue, formatId, rm);	
	}

	@Override
	public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
		//For a group expression, we only show the NATURAL format
		if (dmc instanceof GroupExpressionDMC) {
    		rm.done(new String[] { IFormattedValues.NATURAL_FORMAT });
    		return;
    	}
		
		fDelegate.getAvailableFormats(dmc, rm);	
	}

	@Override
	public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
		// No special handling for GroupExpressions
		return fDelegate.getFormattedValueContext(dmc, formatId);
	}

	@Override
	public void getFormattedExpressionValue(FormattedValueDMContext dmc, final DataRequestMonitor<FormattedValueDMData> rm) {
		GroupExpressionDMC groupExpr = DMContexts.getAncestorOfType(dmc, GroupExpressionDMC.class);
		if (groupExpr != null) {
			getSubExpressionCount(groupExpr, new ImmediateDataRequestMonitor<Integer>(rm) {
				@Override
				protected void handleSuccess() {
					int numChildren = getData();
					String value;
					if (numChildren == 0) {
						value = Messages.NoMatches;	
					} else if (numChildren == 1) {
						value = MessageFormat.format(Messages.UniqueMatch, numChildren);
					} else {
						value = MessageFormat.format(Messages.UniqueMatches, numChildren);
					}
					rm.done(new FormattedValueDMData(value));
				}
			});
    		return;
    	}
		
		fDelegate.getFormattedExpressionValue(dmc, rm);
	}

	@Override
	public void safeToAskForAllSubExpressions(IExpressionDMContext dmc,	DataRequestMonitor<Boolean> rm) {
		// Always safe to ask for all sub-expression of a group expression, since we don't expect large
		// amounts of children
		if (dmc instanceof GroupExpressionDMC) {
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
	
	/** 
	 * Verify if we are dealing with a group expression.
	 * @param expr The expression to verify
	 * @return True if expr is a group expression.  A group
	 *         expression is either a comma-separated list of
	 *         expressions, or an expression using a glob-pattern
	 */
	protected boolean isGroupExpression(String expr) {
		// First check for a comma separated list of expression
		// We want to re-use the regex that defines our separators, and we need to check
		// if the expression contains that regex.  I didn't find a method that
		// checks if a string contains a regex, so instead we all any character before
		// and after the regex, which achieves what we want.
		// Note that checking if expr.split(regex) is bigger than 1, will not notice
		// an expression that has a separator only at the end.
		if (expr.matches(".*" + GROUP_EXPRESSION_SEPARATORS_REGEXP +".*")) { //$NON-NLS-1$ //$NON-NLS-2$
			// We are dealing with a group expression.
			// It may not be a valid one, but it is one nonetheless.
			return true;
		}
		
		// Not a comma-separated list.  Check if we are dealing with a glob-pattern.
		return isGlobPattern(expr);
	}

	/**
	 * Verify if we are dealing with a glob-pattern.
	 * We support the expression * which will match all local variables.
	 * We support glob-patterns for registers (must start with $)
	 * @param expr The expression to verify
	 * @return True if expr is a glob-pattern we support.
	 */
	protected boolean isGlobPattern(String expr) {
		// Get rid of useless whitespace
		expr = expr.trim();

		// We support the glob-pattern '*' to indicate all local variables
		if (expr.equals("*")) { //$NON-NLS-1$
			return true;
		}

		// We only support glob-expressions for registers at this time
		if (expr.startsWith("$")) { //$NON-NLS-1$
			// see: 'man glob'
			if (expr.indexOf('*') != -1 || expr.indexOf('?') != -1 || expr.indexOf('[') != -1) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Find all expressions that match the specified group-expression.
	 * This method retains the order of the expressions in the group-expression, to show them
	 * in the same order as the one specified by the user.  The match of each expression in the group
	 * is sorted alphabetically however.
	 * 
	 * @param groupExprDmc The group-expression context for which we want the matches (sub-expressions)
	 * @param startIndex The beginning of the range of matches (-1 means all matches)
	 * @param length The length of the range of matches (-1 means all matches)
	 * @param rm RequestMonitor that will contain the range of found matches.
	 */
	protected void matchGroupExpression(final GroupExpressionDMC groupExprDmc, int startIndex, int length, 
			                            final DataRequestMonitor<IExpressionDMContext[]> rm) {
		// First separate the group into different expressions.
		// We need to create a new list, as we will modify it during our processing.
		final List<String> exprList = new ArrayList<String>(groupExprDmc.getExpressionsInGroup());
		
		// List to store the final result, which is all the sub-expressions of this group
      	final ArrayList<IExpressionDMContext> subExprList = new ArrayList<IExpressionDMContext>();
      	
      	final int startIndex1 = (startIndex < 0) ? 0 : startIndex;
      	final int length1 = (length < 0) ? Integer.MAX_VALUE : length;

      	matchExpressionList(exprList, subExprList, groupExprDmc, new ImmediateRequestMonitor(rm) {
      		@Override
      		protected void handleSuccess() {
        		// It would be nice to allow identical elements, so that the user
        		// can control their positioning.  For example, the pattern $eax, $*, would show
        		// the $eax first, followed by all other registers sorted alphabetically.  In that case
        		// $eax will be shown again within $*, but that would be ok.
        		// However, the platform does not handle the same element being there twice.
        		// Not only does selecting the element jump back and forth between the duplicates,
        		// but children of duplicated elements are not always right.  Because of this, we
        		// remove all duplicates here.
        		LinkedHashSet<IExpressionDMContext> uniqueSubExprSet = new LinkedHashSet<IExpressionDMContext>(subExprList);
        		subExprList.clear();
        		subExprList.addAll(uniqueSubExprSet);

      			// Extract the range of interest from the final list
				int endIndex = Math.min(startIndex1 + length1, subExprList.size());
				List<IExpressionDMContext> subExprRangeList = subExprList.subList(startIndex1, endIndex);
				IExpressionDMContext[] subExprRange = subExprRangeList.toArray(new IExpressionDMContext[subExprRangeList.size()]);
      			rm.done(subExprRange);
      		}
      	});      	
	}
	
	/** 
	 * We use this recursive method to serialize the request for matches.  Once one request is done,
	 * we create a new one.  This allows us to guarantee that the resulting matches will
	 * be ordered in the same way every time.
	 */
	private void matchExpressionList(final List<String> exprList, final List<IExpressionDMContext> subExprList, final IDMContext parentDmc,
			                         final RequestMonitor rm) {
    	// We've finished parsing the list
    	if (exprList.isEmpty()) {
    		rm.done();
    		return;
    	}
      			
    	// Remove the next element from the list and process it.  We handle glob-pattern matching if needed
    	// and sort the result alphabetically in that case.
    	String expr = exprList.remove(0);

    	if (isGlobPattern(expr)) {
    		IExpressionDMContext exprDmc = createExpression(parentDmc, expr);
     		matchGlobExpression(exprDmc, new ImmediateDataRequestMonitor<List<IExpressionDMContext>>(rm) {
    			@Override
    			protected void handleSuccess() {
    				List<IExpressionDMContext> matches = getData();
    				// Sort the matches to be more user-friendly
    				Collections.sort(matches, new Comparator<IExpressionDMContext>() {
    					@Override
    					public int compare(IExpressionDMContext o1, IExpressionDMContext o2) {
    						return o1.getExpression().compareTo(o2.getExpression());
    					}
    				});

    				subExprList.addAll(matches);
    	    		// Match the next expression from the list
    	    		matchExpressionList(exprList, subExprList, parentDmc, rm);
    			}
    		});
    	} else {
    		// Just a normal expression
    		subExprList.add(createExpression(parentDmc, expr));
    		// Match the next expression from the list
    		matchExpressionList(exprList, subExprList, parentDmc, rm);
    	}
    }
	
	/**
	 * Find all expressions that match the specified glob-pattern.
	 * 
	 * @param exprDmc The expression context for which we want the matches (sub-expressions)
	 * @param rm RequestMonitor that will contain the matches.
	 */
	protected void matchGlobExpression(final IExpressionDMContext exprDmc, final DataRequestMonitor<List<IExpressionDMContext>> rm) {
		final String fullExpr = exprDmc.getExpression().trim();

		if (fullExpr.equals("*")) { //$NON-NLS-1$
			matchLocals(exprDmc, rm);
			return;
		}
		
		// Currently, we only support glob-expressions for registers, so
		// we only need to match the glob-expression with register names.
		// We should not arrive here if we are not handling a register
		assert fullExpr.startsWith("$"); //$NON-NLS-1$
		
		final IRegisters registerService = getServicesTracker().getService(IRegisters.class);
		if (registerService == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Register service unavailable", null)); //$NON-NLS-1$
			return;
		}
		
		registerService.getRegisterGroups(exprDmc, new ImmediateDataRequestMonitor<IRegisterGroupDMContext[]>(rm) {
			@Override
			protected void handleSuccess() {
				registerService.getRegisters(
   	   				    new CompositeDMContext(new IDMContext[] { getData()[0], exprDmc } ), 
   	   		            new ImmediateDataRequestMonitor<IRegisterDMContext[]>(rm) {
   	   		            	@Override
   	   		            	protected void handleSuccess() {
   	   		            		assert getData() instanceof MIRegisterDMC[];
   	   		            		ArrayList<IExpressionDMContext> matches = new ArrayList<IExpressionDMContext>();
   	   		            		for (MIRegisterDMC register : (MIRegisterDMC[])getData()) {
   	   		            			String potentialMatch = "$"+register.getName();  //$NON-NLS-1$
   	   		            			if (globMatches(fullExpr, potentialMatch)) {
   	   		            				matches.add(createExpression(exprDmc, potentialMatch)); 
   	   		            			}
   	   		            		}

   	   		            		rm.done(matches);
   	   		            	}
   	   		            });
			}
		});
	}
	

	/**
	 * Find all local variables that match the specified glob-pattern.
	 * We currently only support matching all local variables using the '*' pattern.
	 * 
	 * @param globDmc The glob-expression context for which we want the matches (sub-expressions)
	 * @param rm RequestMonitor that will contain the matches.
	 */

	protected void matchLocals(final IExpressionDMContext globDmc, final DataRequestMonitor<List<IExpressionDMContext>> rm) {
		// We only support '*' for local variables at this time
		assert globDmc.getExpression().equals("*"); //$NON-NLS-1$
		
		final IStack stackService = getServicesTracker().getService(IStack.class);
		if (stackService == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Stack service unavailable", null)); //$NON-NLS-1$
			return;
		}
		
		IFrameDMContext frameCtx = DMContexts.getAncestorOfType(globDmc, IFrameDMContext.class);
		if (frameCtx == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Stack frame unavailable", null)); //$NON-NLS-1$
			return;
		}

		stackService.getLocals(frameCtx, new ImmediateDataRequestMonitor<IVariableDMContext[]>(rm) {
			@Override
			protected void handleSuccess() {
                IVariableDMContext[] localsDMCs = getData();               
                final IVariableDMData[] localsDMData = new IVariableDMData[localsDMCs.length];

                final CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm) {
                    @Override
                    public void handleSuccess() {
                        ArrayList<IExpressionDMContext> expressionDMCs = new ArrayList<IExpressionDMContext>(localsDMData.length);
                        
                        for (IVariableDMData localDMData : localsDMData) {
                        	expressionDMCs.add(createExpression(globDmc, localDMData.getName()));
                        }
                        rm.done(expressionDMCs);
                    }
                };
                int countRM = 0;
                
				for (int index=0; index < localsDMCs.length; index++) {
					final int finalIndex = index;
                    stackService.getVariableData(localsDMCs[finalIndex], new ImmediateDataRequestMonitor<IVariableDMData>(crm) {
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
	
	/**
	 * Verify if the potentialMatch variable matches the glob-pattern.
	 * 
	 * @param globPattern The glob-pattern to match
	 * @param potentialMatch The string that must match globPattern.
	 * @return True of potentialMatch does match globPattern.
	 */
	protected boolean globMatches(String globPattern, String potentialMatch) {
		 // Convert the glob-pattern into java regex to do the matching

		boolean inBrackets = false;
	    char[] patternArray = globPattern.toCharArray();
	    char[] resultArray = new char[patternArray.length * 2 + 2];
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

	    try {
	    	Pattern pattern = Pattern.compile(new String(resultArray, 0, pos), Pattern.CASE_INSENSITIVE);
	    	Matcher matcher = pattern.matcher(potentialMatch);
	    	return matcher.find();
	    } catch(Exception e) {
	    	// If the user put an invalid pattern, we just ignore it
	    	return false;
	    }
	}
}
