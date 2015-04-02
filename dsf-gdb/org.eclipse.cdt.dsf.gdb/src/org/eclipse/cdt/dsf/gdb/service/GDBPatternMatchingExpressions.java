/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Grzegorz Kuligowski - Cannot cast to type that contain commas (bug 393474)
 *     Marc Khouzam (Ericsson) - Support for glob-expressions for local variables (bug 394408)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Allow user to edit register groups (Bug 235747)
 *     Vladimir Prus (Mentor Graphics) - added setAutomaticUpdate method
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
import org.eclipse.cdt.dsf.debug.service.IExpressions4;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
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
 * This layer allows to support expression-groups and glob-pattern matching.
 * Expression-groups give the user the ability to create a separated list
 * of expressions in a single entry.
 * Glob-patterns are a way to specify a set of expressions that match the
 * pattern.
 * @since 4.2
 */
public class GDBPatternMatchingExpressions extends AbstractDsfService implements IMIExpressions, IExpressions4, ICachingService {
	/**
	 * A regex representing each character that can be used to separate
	 * n the different expressions contained in an expression-group.
	 * The [] are not part the characters, but are used in the regex format.
	 * Note that we don't allow a space separator because spaces are valid within
	 * an expression (e.g., i + 1).
	 * We also don't allow a comma because they are used in C/C++ templates (bug 393474)
	 * Furthermore, commas are used within array-index matches as well.
	 */
	private final static String EXPRESSION_GROUP_SEPARATORS_REGEXP = "[;]"; //$NON-NLS-1$

	private final static String REGISTER_PREFIX = "$"; //$NON-NLS-1$
	private final static String GLOB_EXPRESSION_PREFIX = "="; //$NON-NLS-1$
	
	/**
	 * This regular expression describes the supported content of an array index range.
	 * Valid range formats are are numbers, possibly separated by - and/or ,.  
	 * E.g, "23-56" or "32" or "23, 45-67, 12-15"
	 */
	private static final String ARRAY_INDEX_RANGE_REGEXP = "^*\\d+(\\s*-\\s*\\d+)?(\\s*,\\s*\\d+(\\s*-\\s*\\d+)?)*$";//$NON-NLS-1$

	/**
	 * An expression-group is an expression that requires expansion into a (potentially empty)
	 * list of sub-expressions.  Using an expression-group allows the user to create groups
	 * of expressions very quickly.
	 * 
	 * We support two aspects for expression-goups:
	 * 1- The glob syntax (http://www.kernel.org/doc/man-pages/online/pages/man7/glob.7.html)
	 *    This allows to user to specify glob-patterns to match different expressions.
	 * 2- Separated expressions, each potentially using the glob-syntax
	 */
	protected static class ExpressionGroupDMC implements IExpressionGroupDMContext {
		/** 
		 * The expression context, as created by the main Expression service.
		 * We delegate the handling of the expression to it.
		 */
		private IExpressionDMContext fExprDelegate;

		public ExpressionGroupDMC(IExpressionDMContext exprDmc) {
			fExprDelegate = exprDmc;
		}

		protected IExpressionDMContext getExprDelegate() {
			return fExprDelegate;
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
			if (!(obj instanceof ExpressionGroupDMC)) return false;
			
			return ((ExpressionGroupDMC)obj).fExprDelegate.equals(fExprDelegate);
		}

		@Override
		public int hashCode() {
			return fExprDelegate.hashCode();
		}

		@Override
		public String toString() {
			return "Group: " + getExprDelegate().toString(); //$NON-NLS-1$
		}
	}
	
	/**
	 * The model data interface for expression-groups
	 */
	protected static class ExpressionGroupDMData implements IExpressionDMDataExtension {
		private final String fRelativeExpression;
		private final int fNumChildren;

		public ExpressionGroupDMData(String expr, int numChildren) {
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
			if (!(other instanceof ExpressionGroupDMData)) return false;
			return fRelativeExpression.equals(((ExpressionGroupDMData)other).fRelativeExpression);
		}
		
		@Override
		public int hashCode() {
			return fRelativeExpression.hashCode();
		}
		
		@Override
		public String toString() {
			return "ExprGroup: " + fRelativeExpression; //$NON-NLS-1$
		}
	}
	
	/**
	 * The base expression service to which we delegate all non-expression-group logic.
	 */
	private IMIExpressions fDelegate;
	
	public GDBPatternMatchingExpressions(DsfSession session, IMIExpressions delegate) {
		super(session);
		fDelegate = delegate;
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
		
		if (isExpressionGroup(expression)) {
			return new ExpressionGroupDMC(expressionDmc);
		} else {
			return expressionDmc;
		}
	}

	@Override
	public ICastedExpressionDMContext createCastedExpression(IExpressionDMContext context, CastInfo castInfo) {
		// Cannot cast an expression-group
		assert (!(context instanceof IExpressionGroupDMContext));
		
		return fDelegate.createCastedExpression(context, castInfo);
	}

	@Override
	public void getExpressionDataExtension(final IExpressionDMContext dmc, final DataRequestMonitor<IExpressionDMDataExtension> rm) {
		if (dmc instanceof IExpressionGroupDMContext) {
			getSubExpressionCount(dmc, new ImmediateDataRequestMonitor<Integer>(rm) {
				@Override
				protected void handleSuccess() {
					rm.done(new ExpressionGroupDMData(((IExpressionGroupDMContext)dmc).getExpression(), getData()));
				}
			});
    		return;
    	}
		
		fDelegate.getExpressionDataExtension(dmc, rm);
	}


	@Override
	public void getExpressionData(final IExpressionDMContext dmc, final DataRequestMonitor<IExpressionDMData> rm) {
		if (dmc instanceof IExpressionGroupDMContext) {
			getSubExpressionCount(dmc, new ImmediateDataRequestMonitor<Integer>(rm) {
				@Override
				protected void handleSuccess() {
					rm.done(new ExpressionGroupDMData(((IExpressionGroupDMContext)dmc).getExpression(), getData()));
				}
			});
    		return;
    	}
		
		fDelegate.getExpressionData(dmc, rm);
	}

	@Override
	public void getExpressionAddressData(IExpressionDMContext dmc, DataRequestMonitor<IExpressionDMAddress> rm) {
		// An expression-group does not have an address
		if (dmc instanceof IExpressionGroupDMContext) {
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
		if (exprCtx instanceof IExpressionGroupDMContext) {
			matchExpressionGroup((IExpressionGroupDMContext)exprCtx, -1, -1, rm);
		} else {
			fDelegate.getSubExpressions(exprCtx, rm);
		}
	}

	@Override
	public void getSubExpressions(IExpressionDMContext exprCtx, int startIndex, int length, DataRequestMonitor<IExpressionDMContext[]> rm) {
		if (exprCtx instanceof IExpressionGroupDMContext) {
			matchExpressionGroup((IExpressionGroupDMContext)exprCtx, startIndex, length, rm);
		} else {
			fDelegate.getSubExpressions(exprCtx, startIndex, length, rm);	
		}
	}

	@Override
	public void getSubExpressionCount(IExpressionDMContext dmc, final DataRequestMonitor<Integer> rm) {
		if (dmc instanceof IExpressionGroupDMContext) {
			matchExpressionGroup((IExpressionGroupDMContext)dmc, -1, -1, new ImmediateDataRequestMonitor<IExpressionDMContext[]>(rm) {
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
		if (dmc instanceof IExpressionGroupDMContext) {
			// No need to worry about maxNumberOfChildren for the case of an expression-group, since there won't be
			// a very large amount of them.
			matchExpressionGroup((IExpressionGroupDMContext)dmc, -1, -1, new ImmediateDataRequestMonitor<IExpressionDMContext[]>(rm) {
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
		// An expression-group's value cannot be modified
		if (dmc instanceof IExpressionGroupDMContext) {
    		rm.done(false);
    		return;
    	}
		
		fDelegate.canWriteExpression(dmc, rm);	
	}

	@Override
	public void writeExpression(IExpressionDMContext dmc, String expressionValue, String formatId, RequestMonitor rm) {
		// An expression-group's value cannot be modified
		assert !(dmc instanceof IExpressionGroupDMContext);
		fDelegate.writeExpression(dmc, expressionValue, formatId, rm);	
	}

	@Override
	public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
		// For an expression-group, we only show the NATURAL format
		if (dmc instanceof IExpressionGroupDMContext) {
    		rm.done(new String[] { IFormattedValues.NATURAL_FORMAT });
    		return;
    	}
		
		fDelegate.getAvailableFormats(dmc, rm);	
	}

	@Override
	public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
		// No special handling for expression-groups
		return fDelegate.getFormattedValueContext(dmc, formatId);
	}

	@Override
	public void getFormattedExpressionValue(FormattedValueDMContext dmc, final DataRequestMonitor<FormattedValueDMData> rm) {
		IExpressionGroupDMContext exprGroup = DMContexts.getAncestorOfType(dmc, IExpressionGroupDMContext.class);
		if (exprGroup != null) {
			getSubExpressionCount(exprGroup, new ImmediateDataRequestMonitor<Integer>(rm) {
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
		// Always safe to ask for all sub-expression of an expression-group, 
		// since we don't expect large amounts of children
		if (dmc instanceof IExpressionGroupDMContext) {
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
	 * Verify if we are dealing with an expression-group.
	 * @param expr The expression to verify
	 * @return True if expr is an expression-group.  An
	 *         expression-group is either a separated list of
	 *         expressions, or an expression using a glob-pattern
	 */
	protected boolean isExpressionGroup(String expr) {
		// First check for a separated list of expression
		// We want to re-use the regex that defines our separators, and we need to check
		// if the expression contains that regex.  I didn't find a method that
		// checks if a string contains a regex, so instead we add any character before
		// and after the regex, which achieves what we want.
		// Note that checking if (expr.split(regex) > 1), will not notice
		// an expression that has a separator only at the end.
		if (expr.matches(".*" + EXPRESSION_GROUP_SEPARATORS_REGEXP +".*")) { //$NON-NLS-1$ //$NON-NLS-2$
			// We are dealing with a group of expressions.
			// It may not be a valid one, but it is one nonetheless.
			return true;
		}
		
		// Not a list.  Check if we are dealing with a glob-pattern.
		return isGlobExpression(expr);
	}
	
	/**
	 * Verify if we are dealing with a glob-pattern.
	 * We support the expression '*' which will match all local variables
	 * as well as the expression '$*' which will match all registers.
	 * We support glob-patterns for any expression starting with '='
	 * 
	 * @param expr The expression to verify
	 * @return True if expr is a glob-pattern we support.
	 */
	protected boolean isGlobExpression(String expr) {
		// Get rid of useless whitespace
		expr = expr.trim();

		// We support the glob-pattern '*' to indicate all local variables
		// and $* for all registers
		if (expr.equals("*") || expr.equals("$*")) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}

		// Glob-expressions must start with '='
		if (expr.startsWith(GLOB_EXPRESSION_PREFIX)) {
			return true;
		}

		return false;
	}
	
	/**
	 * Verify if the glob-pattern represents a register.
	 * 
	 * @param expr The glob-pattern that may be a register-pattern
	 * @return True if expr follows the rules of an register-pattern
	 */
	protected boolean isRegisterPattern(String expr) {
		// Get rid of useless whitespace
		expr = expr.trim();

		if (expr.startsWith(REGISTER_PREFIX)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Verify if the glob-pattern should be handled as an array index range.
	 * When dealing with variables (on contrast to registers), the [] will
	 * map to array indices instead of ranges within the array name.
	 * For example =array[1-2] will map to array[1] and array[2] instead of
	 * array1 and array2.
	 * 
	 * If the range contains non-digits, the matching will not be handled
	 * as array indices.
	 * 
	 * @param expr The glob-pattern that may be an array-pattern
	 * @return True if expr follows the rules of an array-pattern
	 */
	protected boolean isArrayPattern(String expr) {
		// Get rid of useless whitespace
		expr = expr.trim();

		int openBracketIndex = expr.indexOf('[');
		// There must be an open bracket and it cannot be in the first position
		// (as we need some indication of the array name before the brackets)
		if (openBracketIndex < 1) {
			return false;
		}
		
		// We don't support any characters after the closing bracket
		// since we don't support any operations on an expression-group.
		if (!expr.endsWith("]")) { //$NON-NLS-1$
			return false;
		}

		// We have a match for a variable which uses brackets.
		// Check if the indices are integer ranges (using - or commas).
	    try {
	    	Pattern pattern = Pattern.compile(ARRAY_INDEX_RANGE_REGEXP, Pattern.CASE_INSENSITIVE);
	    	Matcher matcher = pattern.matcher(expr.substring(openBracketIndex+1, expr.length()-1));
	    	if (!matcher.find()) {
	    		return false;
	    	}
	    } catch(Exception e) {
	    	// If the user put an invalid pattern, we just ignore it
	    	return false;
	    }
	    
		return true;
	}
	
	/**
	 * Split the expression-group into a list of individual expression strings. 
	 */
	protected List<String> splitExpressionsInGroup(IExpressionGroupDMContext groupDmc) {
		// Split the list of expressions
		String[] splitExpressions = groupDmc.getExpression().split(EXPRESSION_GROUP_SEPARATORS_REGEXP);

		// Remove any extra whitespace from each resulting expression,
		// and ignore any empty expressions.
		List<String> expressions = new ArrayList<String>(splitExpressions.length);
		for (String expr : splitExpressions) {
			expr = expr.trim();
			if (!expr.isEmpty()) {
				expressions.add(expr);
			}
		}
		return expressions;
	}
	
	/**
	 * Find all expressions that match the specified expression-group.
	 * This method retains the order of the expressions in the expression-group, to show them
	 * in the same order as the one specified by the user.  The matches of each expression within the group
	 * are sorted alphabetically however.
	 * 
	 * @param exprGroupDmc The expression-group context for which we want the matches (sub-expressions)
	 * @param startIndex The beginning of the range of matches (-1 means all matches)
	 * @param length The length of the range of matches (-1 means all matches)
	 * @param rm RequestMonitor that will contain the range of found matches.
	 */
	protected void matchExpressionGroup(final IExpressionGroupDMContext exprGroupDmc, int startIndex, int length, 
			                            final DataRequestMonitor<IExpressionDMContext[]> rm) {
		// First separate the group into different expressions.
		// We need to create a new list, as we will modify it during our processing.
		final List<String> exprList = new ArrayList<String>(splitExpressionsInGroup(exprGroupDmc));

		// List to store the final result, which is all the sub-expressions of this group
      	final ArrayList<IExpressionDMContext> subExprList = new ArrayList<IExpressionDMContext>();
      	
      	final int startIndex1 = (startIndex < 0) ? 0 : startIndex;
      	final int length1 = (length < 0) ? Integer.MAX_VALUE : length;

      	matchExpressionList(exprList, subExprList, exprGroupDmc, new ImmediateRequestMonitor(rm) {
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

    	IExpressionDMContext exprDmc = createExpression(parentDmc, expr);
    	if (exprDmc instanceof IExpressionGroupDMContext) {
     		matchGlobExpression((IExpressionGroupDMContext)exprDmc, new ImmediateDataRequestMonitor<List<IExpressionDMContext>>(rm) {
    			@Override
    			protected void handleSuccess() {
    				List<IExpressionDMContext> matches = getData();
    				// Sort the matches to be more user-friendly
    				Collections.sort(matches, new Comparator<IExpressionDMContext>() {
    					@Override
    					public int compare(IExpressionDMContext o1, IExpressionDMContext o2) {
    						// For elements of the same array, we need to sort by index
    						if (isArrayPattern(o1.getExpression()) && isArrayPattern(o2.getExpression())) {
    							// Extract the array names and the array indices specification.
    							// The regex used will remove both [ and ]
    							String[] arrayExprParts1 = o1.getExpression().split("[\\[\\]]"); //$NON-NLS-1$
    							assert arrayExprParts1 != null && arrayExprParts1.length == 2;

    							String[] arrayExprParts2 = o2.getExpression().split("[\\[\\]]"); //$NON-NLS-1$
    							assert arrayExprParts2 != null && arrayExprParts2.length == 2;

    							// Compare array names
    							if (arrayExprParts1[0].compareTo(arrayExprParts2[0]) == 0) {
    								// We are dealing with the same array
    								try {
    									int arrayIndex1 = Integer.parseInt(arrayExprParts1[1]);
    									int arrayIndex2 = Integer.parseInt(arrayExprParts2[1]);

    									if (arrayIndex1 == arrayIndex2) return 0;
    									if (arrayIndex1 > arrayIndex2) return 1;
    									return -1;
    								} catch (NumberFormatException e) {
    									// Invalid array index.  Fall-back to sorting lexically.
    								}
    							}
    						}
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
    		subExprList.add(exprDmc);
    		// Match the next expression from the list
    		matchExpressionList(exprList, subExprList, parentDmc, rm);
    	}
    }
	
	/**
	 * Find all expressions that match the specified glob-pattern.
	 * 
	 * @param exprDmc The expression context for which we want the matches (sub-expressions)
	 * @param rm RequestMonitor that will contain the unsorted matches.
	 */
	protected void matchGlobExpression(final IExpressionGroupDMContext exprDmc, final DataRequestMonitor<List<IExpressionDMContext>> rm) {
		String fullExpr = exprDmc.getExpression().trim();

		if (fullExpr.startsWith(GLOB_EXPRESSION_PREFIX)) {
			// Strip the leading '=' and any extra spaces
			fullExpr = fullExpr.substring(1).trim();
		}
		
		if (isRegisterPattern(fullExpr)) {
			matchRegisters(exprDmc, rm);
		} else {
			if (!isArrayPattern(fullExpr)) {
				matchLocals(exprDmc, rm);
			} else {
				// If we are dealing with an expression that could represent an array, we must
				// try to match arrays and non-arrays. The reason is that a pattern such as
				// =a[1-2] can be a valid match for both a[1], a[2] and a1, a2.
				matchArrays(exprDmc, new ImmediateDataRequestMonitor<List<IExpressionDMContext>>(rm) {
					@Override
					protected void handleSuccess() {
						final List<IExpressionDMContext> exprList = 
								getData() != null ? getData() : new ArrayList<IExpressions.IExpressionDMContext>();
						matchLocals(exprDmc, new ImmediateDataRequestMonitor<List<IExpressionDMContext>>(rm) {
							@Override
							protected void handleSuccess() {
								if (getData() != null) {
									exprList.addAll(getData());
								}
								rm.done(exprList);
							}
						});
					}
				});
			}
		}
	}

	/**
	 * Find all registers that match the specified glob-pattern.
	 * 
	 * @param globDmc The glob-expression context for which we want the matches (sub-expressions)
	 * @param rm RequestMonitor that will contain the unsorted matches.
	 */
	protected void matchRegisters(final IExpressionGroupDMContext globDmc, final DataRequestMonitor<List<IExpressionDMContext>> rm) {
		final IRegisters2 registerService = getServicesTracker().getService(IRegisters2.class);
		if (registerService == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Register service unavailable", null)); //$NON-NLS-1$
			return;
		}

		final IContainerDMContext contDmc = DMContexts.getAncestorOfType(globDmc, IContainerDMContext.class);
		
		registerService.getRegisters(
				new CompositeDMContext(new IDMContext[] { contDmc, globDmc } ), 
				new ImmediateDataRequestMonitor<IRegisterDMContext[]>(rm) {
					@Override
					protected void handleSuccess() {
						assert getData() instanceof MIRegisterDMC[];
						ArrayList<IExpressionDMContext> matches = new ArrayList<IExpressionDMContext>();
						
						String fullExpr = globDmc.getExpression().trim();
                		if (fullExpr.startsWith(GLOB_EXPRESSION_PREFIX)) {
                			// Strip the leading '=' and any extra spaces
                			fullExpr = fullExpr.substring(1).trim();
                		}
                		
						for (MIRegisterDMC register : (MIRegisterDMC[])getData()) {
							String potentialMatch = REGISTER_PREFIX + register.getName();
							if (globMatches(fullExpr, potentialMatch)) {
								matches.add(createExpression(globDmc, potentialMatch)); 
							}
						}

						rm.done(matches);
					}
				});
	}
	
	/**
	 * Find all local variables that match the specified glob-pattern.
	 * 
	 * @param globDmc The glob-expression context for which we want the matches (sub-expressions)
	 * @param rm RequestMonitor that will contain the unsorted matches.
	 */
	protected void matchLocals(final IExpressionGroupDMContext globDmc, final DataRequestMonitor<List<IExpressionDMContext>> rm) {
		
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

                final CountingRequestMonitor varNameCRM = new CountingRequestMonitor(getExecutor(), rm) {
                    @Override
                    public void handleSuccess() {
                        ArrayList<IExpressionDMContext> matches = new ArrayList<IExpressionDMContext>(localsDMData.length);
                        
                		String fullExpr = globDmc.getExpression().trim();
                		if (fullExpr.startsWith(GLOB_EXPRESSION_PREFIX)) {
                			// Strip the leading '=' and any extra spaces
                			fullExpr = fullExpr.substring(1).trim();
                		}
                		
                        for (IVariableDMData localDMData : localsDMData) {
							String potentialMatch = localDMData.getName();
							if (globMatches(fullExpr, potentialMatch)) {
								matches.add(createExpression(globDmc, potentialMatch));
							}
                        }
                        
                        rm.done(matches);
                    }
                };
                
                // Get all the names of the variables
                int count = 0;
				for (int index=0; index < localsDMCs.length; index++) {
					final int finalIndex = index;
                    stackService.getVariableData(localsDMCs[finalIndex], new ImmediateDataRequestMonitor<IVariableDMData>(varNameCRM) {
                            @Override
                            public void handleSuccess() {
                                localsDMData[finalIndex] = getData();
                                varNameCRM.done();
                            }
                    });
                    
                    count++;
                }		
                varNameCRM.setDoneCount(count);
			}
		});
	}
	/**
	 * Find all arrays elements that match the specified glob-pattern.
	 * 
	 * @param globDmc The glob-expression context for which we want the matches (sub-expressions)
	 * @param rm RequestMonitor that will contain the unsorted matches.
	 */
	protected void matchArrays(final IExpressionGroupDMContext globDmc, final DataRequestMonitor<List<IExpressionDMContext>> rm) {
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
		
		String fullExpr = globDmc.getExpression().trim();
		if (fullExpr.startsWith(GLOB_EXPRESSION_PREFIX)) {
			// Strip the leading '=' and any extra spaces
			fullExpr = fullExpr.substring(1).trim();
		}

		// Extract the array name and the array index specification.
		// The regex used will remove both [ and ]
		String[] arrayExprParts = fullExpr.split("[\\[\\]]"); //$NON-NLS-1$
		assert arrayExprParts != null && arrayExprParts.length == 2;

		if (arrayExprParts == null || arrayExprParts.length < 2) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Error parsing array expression", null)); //$NON-NLS-1$
			return;
		}
		
		final String arrayName = arrayExprParts[0].trim();
		final String arrayIndexSpec = arrayExprParts[1].trim();

		stackService.getLocals(frameCtx, new ImmediateDataRequestMonitor<IVariableDMContext[]>(rm) {
			@Override
			protected void handleSuccess() {
                IVariableDMContext[] localsDMCs = getData();               
                final IVariableDMData[] localsDMData = new IVariableDMData[localsDMCs.length];

                final CountingRequestMonitor varNameCRM = new CountingRequestMonitor(getExecutor(), rm) {
                    @Override
                    public void handleSuccess() {
                    	final ArrayList<IExpressionDMContext> matches = new ArrayList<IExpressionDMContext>();
                    	final CountingRequestMonitor elementMatchesCRM = new CountingRequestMonitor(getExecutor(), rm) {
                    		@Override
                    		public void handleSuccess() {
                    			rm.done(matches);
                    		}
                    	};

                    	int count = 0;
                    	for (IVariableDMData localDMData : localsDMData) {
                    		final String potentialMatch = localDMData.getName();

                    		if (globMatches(arrayName, potentialMatch)) {
                    			// We have a variable that matches the name part of the array.
                    			// Let's create the matching elements if that variable is an array.
                    			createPotentialArrayMatches(createExpression(globDmc, potentialMatch), arrayIndexSpec,
                    					                    new ImmediateDataRequestMonitor<List<IExpressionDMContext>>(elementMatchesCRM){
                    				@Override
                    				protected void handleSuccess() {
                    					if (getData() != null) {
                    						matches.addAll(getData());
                    					}
                    					elementMatchesCRM.done();
                    				}
                    			});

                    			count++;
                    		}
                    	}
                    	elementMatchesCRM.setDoneCount(count);
                    }
                };
                
                // Get all the names of the variables
                int count = 0;
				for (int index=0; index < localsDMCs.length; index++) {
					final int finalIndex = index;
                    stackService.getVariableData(localsDMCs[finalIndex], new ImmediateDataRequestMonitor<IVariableDMData>(varNameCRM) {
                            @Override
                            public void handleSuccess() {
                                localsDMData[finalIndex] = getData();
                                varNameCRM.done();
                            }
                    });
                    
                    count++;
                }		
                varNameCRM.setDoneCount(count);
			}
		});
	}
	
	/**
	 * Creates requested array elements if exprDmc is indeed an array.
	 * 
	 * @param exprDmc The potential array expression to be used
	 * @param indexSpec The specification of the element indices
	 * @param rm The list of created element expressions.  
     *           If exprDmc is not an array, the list will be empty but not null.
	 */
	protected void createPotentialArrayMatches(final IExpressionDMContext exprDmc, final String indexSpec, 
			                                   final DataRequestMonitor<List<IExpressionDMContext>> rm) {
		// We check if the variable is an array or not.  If it is an array,
		// we create the elements based on the specified indices.
		// If it is not an array, we don't need to handle it in this method
		getExpressionData(exprDmc, new ImmediateDataRequestMonitor<IExpressionDMData>(rm) {
			@Override
			protected void handleCompleted() {
				boolean isArray = 
						isSuccess() && 
						getData().getBasicType().equals(IExpressionDMData.BasicType.array);

            	final ArrayList<IExpressionDMContext> elements = new ArrayList<IExpressionDMContext>();

				if (isArray) {
					// we must now create the elements based on the indices
					List<IExpressionDMContext> indicesDmcs = 
							createArrayIndicesExpression(exprDmc, indexSpec);
					if (indicesDmcs != null) {
						elements.addAll(indicesDmcs);
					}
				}
				rm.done(elements);
			}
		});
	}

	/**
	 * Create all the expressions characterizing the specified arrayDmc and
	 * indexSpec pattern.
	 * 
	 * @param arrayDmc The expression context that represents the array itself
	 * @param indexSpec A string describing the range of indexes to be used.
	 *                  Valid range formats are described by {@code ARRAY_INDEX_RANGE_REGEXP}
	 *                  The string should not contain the index [] characters.
	 * @return A list of expression contexts representing all the different
	 *         array elements possible using the name of the array and indexSpec.
	 *         If the indexSpec is invalid (e.g, 3-2) it will be used as-is which
	 *         could be a valid expression (i.e., the index 3-2=1 in this case)
	 */
	protected List<IExpressionDMContext> createArrayIndicesExpression(IExpressionDMContext arrayDmc, String indexSpec) {
        ArrayList<IExpressionDMContext> expressionDMCs = new ArrayList<IExpressionDMContext>();
        String arrayName = arrayDmc.getExpression();
        IDMContext parentDmc = arrayDmc.getParents()[0];
        
		// First split the indexRange by comma
		String[] ranges = indexSpec.split(","); //$NON-NLS-1$
		
		for (String range : ranges) {
			// Get rid of any useless spaces
			range = range.trim();
					
			// Try to split the range with the - separator
			String[] rangeNumbers = range.split("-");//$NON-NLS-1$
			if (rangeNumbers.length == 2) {
				try {
					int lowIndex = Integer.parseInt(rangeNumbers[0]);
					int highIndex = Integer.parseInt(rangeNumbers[1]);
					
					if (lowIndex <= highIndex) {
						for (int i = lowIndex; i <= highIndex; i++) {
							expressionDMCs.add(createExpression(parentDmc, arrayName + "[" + i + "]")); //$NON-NLS-1$ //$NON-NLS-2$						
						}

						continue;
					}
				} catch (NumberFormatException e) {
					// Ignore and fall back on using range as-is below
				}
			}
			
			// Leave range as-is, which could be a single digit, or some non-expected expression
			expressionDMCs.add(createExpression(parentDmc, arrayName + "[" + range + "]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return expressionDMCs;
	}
	
	/**
	 * Verify if the potentialMatch variable matches the glob-pattern.
	 * 
	 * @param globPattern The glob-pattern to match
	 * @param potentialMatch The string that must match globPattern.
	 * @return True if potentialMatch does match globPattern.
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

	/**
	 * @since 4.7
	 */
	@Override
	public void setAutomaticUpdate(IExpressionDMContext context, boolean update) {
		if (fDelegate instanceof IExpressions4) {
			((IExpressions4)fDelegate).setAutomaticUpdate(context, update);
		}
	}
}
