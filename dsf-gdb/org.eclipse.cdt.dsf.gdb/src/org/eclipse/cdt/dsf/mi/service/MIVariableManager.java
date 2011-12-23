/*******************************************************************************
 * Copyright (c) 2008, 2011 Monta Vista and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Monta Vista - initial API and implementation
 *     Ericsson    - Modified for handling of multiple execution contexts
 *     Ericsson    - Major updates for GDB/MI implementation
 *     Ericsson    - Major re-factoring to deal with children
 *     Axel Mueller - Bug 306555 - Add support for cast to type / view as array (IExpressions2)
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Axel Mueller - Workaround for GDB bug where -var-info-path-expression gives invalid result (Bug 320277)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.ICastedExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.GDBTypeParser;
import org.eclipse.cdt.dsf.gdb.GDBTypeParser.GDBType;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.MIExpressions.ExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.MIExpressions.MIExpressionDMC;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetAttributes;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetChildCount;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetChildren;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetValue;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetVar;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataEvaluateExpression;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetAttributesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetChildCountInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetChildrenInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetValueInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetVarInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDisplayHint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDisplayHint.GdbDisplayHint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVar;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarAssignInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarChange;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarCreateInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarDeleteInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoPathExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarListChildrenInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarSetFormatInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarShowAttributesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarUpdateInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Manages a list of variable objects as created through GDB/MI commands.
 * 
 * This class is passed expression-meta-commands which have their own cache.
 * Therefore, we don't use the standard MICommandCache in this class.
 * In fact, we can't even use it, because many variableObject MI commands,
 * should not be cached as they alter the state of the back-end.
 * 
 * Design details:
 * ==============
 * 
 * GDB variable object information
 * -------------------------------
 * o Variable objects are recursively hierarchical, where children can be created through 
 *   the parent.
 * o A varObject created with -var-create is a ROOT
 * o A varObject created with -var-list-children, is not a root
 * o Only varObject with no children or varObjects that are pointers can change values 
 *   and therefore 
 *   those objects can be used with -var-assign
 * o After a program stops, a varObject must be 'updated' before used 
 * o Only root varObject can be updated with -var-update, which will trigger all
 *   of the root's descendants to be updated.
 * o Once updated, a varObject need not be updated until the program resumes again;
 *   this is true even after -var-assign is used (because it does an implicit -var-update)
 * o -var-update will return the list of all modifiable descendants of the udpated root which
 *   have changed
 * o -var-update will indicate if a root is out-of-scope (which implies that all
 *   its descendants are out-of-scope)
 * o if a varObject is out-of-scope, another varObject may be valid for the same
 *   expression as the out-of-scope varObject
 * o deleting a varObject will delete all its descendants, therefore, it is only
 *   necessary to delete roots
 * 
 * 
 * Class details
 * -------------
 * - We have an MIVariableObject class which represents a variable object in GDB
 * 
 * - MIVariableObject includes a buffered value for each allowed format.
 * 
 * - We have an MIRootVariableObject class inheriting from MIVariableObject to describe
 *   root varObjects created with -var-create.  Objects created with -var-list-children
 *   are MIVariableObjects only.  The root class will keep track of if the root object 
 *   needs to be updated, if the root object is out-of-scope, and of a list of all 
 *   modifiable descendants of this root.  The list of modifiable descendants is 
 *   accessed using the gdb-given name to allow quick updates from the -var-update 
 *   result (see below.)
 * 
 * - we do not use -var-list-children for arrays, but create them manually
 *  
 * - when the program stops, we should mark all roots as needing to be updated. 
 * To achieve this efficiently, we have a dedicated list of roots that are updated.
 * When the program stops, we go through this list, remove each element and mark it
 * as needing to be updated.
 * 
 * - when a varObject is accessed, if its root must be updated, the var-update
 * command shall be used.  The result of that command will indicate all
 * modifiable descendants that have changed.  We also use --all-values with -var-update
 * to get the new value (in the current format) for each modified descendant.  Using the list of modifiable
 * descendants of the root, we can quickly update the changed ones to invalidate their buffered
 * values and store the new current format value.
 * 
 * - all values of non-modifiable varObjects (except arrays) will be set to {...}
 * without going to the back-end
 * 
 * - requesting the value of an array varObject will trigger the creation of a new
 * varObject for the array's address.  Note that we must still use a variable
 * object and not the command -data-evaluate-expression, because we still need to get
 * the array address in multiple formats.
 * 
 * - we keep an LRU (Least Recently Used) structure of all variable objects.  This LRU
 * will be bounded to a maximum allowed number of variable objects.  Whenever we get an
 * object from the LRU cleanup will be done if the maximum size has been reached.
 * The LRU will not delete a parent varObject until all its children are deleted; this is
 * achieved by touching each of the parents of an object whenever that object is put or get
 *
 * - It may happen that when accessing a varObject we find its root to be
 * out-of-scope.  The expression for which we are trying to access a varObject
 * could still be valid, and therefore we should try to create a new varObject for
 * that expression.  This can happen for example if two methods use the same name
 * for a variable. In the case when we find that a varObject is out-of-scope (when
 * its root is out-of-scope) the following should be done:
 *  - replace the varObject in the LRU with a newly created one in GDB
 *  - if the old object was a root, delete it in GDB.
 *  
 * - In GDB, -var-update will only report a change if -var-evaluate-expression has
 *   changed -- in the current format--.  This means that situations like
 *    double z = 1.2;
 *    z = 1.4;
 *   Will not report a change if the format is anything else than natural.
 *   This is because 1.2 and 1.4 are both printed as 1, 0x1, etc
 *   Since we cache the values of every format, we must know if the value has
 *   change in -any- format, not just the current one.
 *   To solve this, we always keep the display format of variable objects (and their
 *   children) to the natural format; we believe that if the value changes in any
 *   format, it guarantees that it will change in the natural format.
 *   The simplest way to do this is that whenever we change the format
 *   of a variable object, we immediately set it back to natural with a second
 *   var-set-format command.
 *   Note that versions of GDB after 6.7 will allows to issue -var-evaluate-expression
 *   with a specified format, therefore allowing us to never use -var-set-format, and
 *   consequently, to easily keep the display format of all variable objects to natural.
 *   
 * Notes on Dynamic Variable Objects (varobj)
 * ------------------------------------------
 *   - with version 7.0, gdb support so-called pretty printers.
 *   
 *   - pretty printers are registered for certain types
 *   
 *   - if there is a pretty printer registered for the type of a variable,
 *     the pretty printer provides the value and the children of that variable
 *     
 *   - a varobj whose value and/or children are provided by a pretty printer,
 *     are referred to as dynamic variable objects
 *     
 *   - dynamic varobjs change the game: it's not wise to ask it about all its
 *     children, not even the number of children it has. The reason is that
 *     in order to find out about the number of children the pretty printer
 *     must fetch all the children from the inferiors memory. If the variable
 *     is not yet initialized, the set of children are random, and thus might
 *     be huge. Even worse, there are data structures where fetching all
 *     children may result in an endless loop. The Eclipse debugger then hangs.
 *     
 *   - In order to avoid this, we will always fetch up to a certain maximum
 *     number of children. Furthermore, it is possible to find out whether there
 *     are more children available. In the UI, all the currently fetched
 *     children are available. In addition, if there are more children
 *     available, a special node will be appended to indicate that there is
 *     more the user could fetch.
 *     
 *   - Dynamic varobjs can change their value, as leaf varobjs can do.
 *     Especially, children can be added or removed during an update.
 *     
 *   - There is no expression for children of dynamic varobjs (at least not
 *     yet, http://sourceware.org/bugzilla/show_bug.cgi?id=10252 would fix that).
 *     The reason -var-info-path-expression returns garbage for children of
 *     dynamic varobjs.
 *
 *   - Because of this, the variable of an expression that is a child of
 *     a dynamic varobj cannot be created again using -var-create, once
 *     the LRU cache has deleted it. Instead, we track the parent and index
 *     within this parent for each non-root variable, and later use
 *       -var-list-children parent indexInParent (indexInParent + 1)
 *     in order to create the MI variable anew.
 *     
 *   - The fetching of children for dynamic varobjs becomes a bit more complicated.
 *     For the traditional varobjs, once children where requested, all children
 *     were fetched. For dynamic varobjs, we can no longer fetch all children.
 *     Instead, the client will provide a maximum number of children that
 *     is to be fetched. Every time the child count or children are requested,
 *     we must check whether there are additional children to be fetched,
 *     because the limit might have extended.
 *     Fetching additional children can only be done by one request monitor at a time.
 *     The serialization of the request monitors is done in getChildren by
 *     ensuring that fetchChildren is called for one request monitor at a time.
 *     fetchChildren in turn checks whether enough children are fetched, and
 *     if not, fetches the additional children.
 */
public class MIVariableManager implements ICommandControl {

	/**
	 * Stores the information about children of a variable object.
	 * 
	 * @since 4.0
	 */
	protected static class ChildrenInfo {
		private final ExpressionInfo[] children;
		private final boolean hasMore;
		
		public ChildrenInfo(ExpressionInfo[] children, boolean hasMore) {
			this.children = children;
			this.hasMore = hasMore;
		}

		/**
		 * @return The currently fetched children. Ask {@link #hasMore()} in
		 *         order to find out whether there are more to fetch.
		 */
		public ExpressionInfo[] getChildren() {
			return children;
		}
		
		/**
		 * @return true, if there are more than just those returned by
		 *         {@link #getChildren()}.
		 */
		public boolean hasMore() {
			return hasMore;
		}
	}
	
	/**
	 * Stores the information about the children count of a variable object.
	 * 
	 * @since 4.0
	 */
	protected static class ChildrenCountInfo {
		private final int childrenCount;
		private final boolean hasMore;
		
		public ChildrenCountInfo(int childrenCount, boolean hasMore) {
			this.childrenCount = childrenCount;
			this.hasMore = hasMore;
		}

		/**
		 * The number of children that we currently know of. Ask
		 * {@link #hasMore()} in order to find out whether there is at least one
		 * more.
		 */
		public int getChildrenCount() {
			return childrenCount;
		}

		/**
		 * @return <code>true</code> if there are more children than actually
		 *         returned by {@link #getChildrenCount()}.
		 */
		public boolean hasMore() {
			return hasMore;
		}
	}
	
	/**
	 * Utility class to track the progress and information of MI variable objects
	 */
	public class MIVariableObject {

		// Don't use an enumeration to allow subclasses to extend this
		protected static final int STATE_READY = 0;
		protected static final int STATE_UPDATING = 1;
		/** @since 4.0 */
		protected static final int STATE_NOT_CREATED = 10;
		/** @since 4.0 */
		protected static final int STATE_CREATING = 11;
		/** @since 4.0 */
		protected static final int STATE_CREATION_FAILED = 12;
	    
		protected int currentState;	

		// This is the lock used when we must run multiple
		// operations at once.  This lock should be independent of the
		// UPDATING state, which is why we don't make it part of the state
		private boolean locked = false;
		
	    // This id is the one used to search for this object in our hash-map
	    private final VariableObjectId internalId;
	    // This is the name of the variable object, as given by GDB (e.g., var1 or var1.public.x)
		private String gdbName = null;
		// The current format of this variable object, within GDB
		private String format = IFormattedValues.NATURAL_FORMAT;

		// The full expression that can be used to characterize this object
		// plus some other information that shall live longer than the
		// MIVariableObject.
		private ExpressionInfo exprInfo;
		private String type = null;
		private GDBType gdbType;
		// A hint at the number of children.  This value is obtained
		// from -var-create or -var-list-children.  It may not be right in the case
		// of C++ structures, where GDB has a level of children for private/public/protected.
		private int numChildrenHint = 0;
		private Boolean editable = null;

		// The current values of the expression for each format. (null if not known yet)
		private Map<String, String> valueMap = null;
		
		// A queue of request monitors waiting for this object to be ready
		private LinkedList<RequestMonitor> operationsPending;
		
		// A queue of request monitors that requested an update
		protected LinkedList<DataRequestMonitor<Boolean>> updatesPending;
		
		/** @since 4.0 */
		protected LinkedList<DataRequestMonitor<ChildrenInfo>> fetchChildrenPending;

		// The children of this variable, if any.  
		// Null means we didn't fetch them yet, while an empty array means no children
        private ExpressionInfo[] children = null; 
		private boolean hasMore = false;
		private MIDisplayHint displayHint = MIDisplayHint.NONE;
		
        // The parent of this variable object within GDB.  Null if this object has no parent
        private MIVariableObject parent = null;
        
        // The root parent that must be used to issue -var-update.
        // If this object is a root, then the rootToUpdate is itself
        private MIRootVariableObject rootToUpdate = null;
                
		protected boolean outOfScope = false;
		
		private boolean fetchingChildren = false;
		
		
		/** 
		 * In case of base class variables that are accessed in a derived class 
		 * we cannot trust var-info-path-expression because of a bug in gdb.
		 * We have to use a workaround and apply it to the complete hierarchy of this varObject.
		 * Bug 320277
		 */
		private boolean hasCastToBaseClassWorkaround = false;

		public MIVariableObject(VariableObjectId id, MIVariableObject parentObj) {
			this(id, parentObj, false);
		}
		
		/** @since 4.0 */
		public MIVariableObject(VariableObjectId id, MIVariableObject parentObj,
				boolean needsCreation) {
			currentState = needsCreation ? STATE_NOT_CREATED : STATE_READY; 
			
			operationsPending = new LinkedList<RequestMonitor>();
			updatesPending = new LinkedList<DataRequestMonitor<Boolean>>();
			fetchChildrenPending = new LinkedList<DataRequestMonitor<ChildrenInfo>>();
			
			internalId = id;
			setParent(parentObj);
			
			// No values are available yet
			valueMap = new HashMap<String, String>();
			resetValues();
		}
		
		public VariableObjectId getInternalId() { return internalId; }
		public String getGdbName() { return gdbName; }
		public String getCurrentFormat() {	return format; }
		public MIVariableObject getParent() { return parent; }
		public MIRootVariableObject getRootToUpdate() { return rootToUpdate; }
		
		public String getExpression() { return exprInfo.getFullExpr(); }
		public String getType() { return type; }
		
		/**
		 * @since 4.0
		 */
		public ExpressionInfo getExpressionInfo() { return exprInfo; }

		/**
		 * @return <code>true</code> if value and children of this varobj are
		 *         currently provided by a pretty printer.
		 *         
		 * @since 4.0
		 */
		public boolean isDynamic() { return exprInfo.isDynamic(); }
		
		/**
		 * @return For dynamic varobjs ({@link #isDynamic() returns true}) this
		 *         method returns whether there are children in addition to the
		 *         currently fetched, i.e. whether there are more children than
		 *         {@link #getNumChildrenHint()} returns.
		 *         
		 * @since 4.0
		 */
		public boolean hasMore() { return hasMore; }

		/**
		 * @since 4.0
		 */
		public MIDisplayHint getDisplayHint() { return displayHint; };
		
		/**
		 * @since 4.0
		 */
		protected void setDisplayHint(MIDisplayHint displayHint) {
			this.displayHint = displayHint;
		};
		
		/**
		 * @since 4.0
		 */
		public boolean hasChildren() { return (getNumChildrenHint() != 0 || hasMore()); }

		/** @since 3.0 */
		public GDBType getGDBType() { return gdbType; }
		/** 
		 * Returns a hint to the number of children.  This hint is often correct,
		 * except when we are dealing with C++ complex structures where
		 * GDB has 'private/public/protected' as children.
		 * 
		 * Use <code>isNumChildrenHintTrustworthy()</code> to know if the
		 * hint can be trusted.
		 * 
		 * Note that a hint of 0 children can always be trusted, except for
		 * <code>{@link #hasMore} == true</code>.
		 * 
		 * @since 3.0 */
		public int getNumChildrenHint() { return numChildrenHint; }
		/** 
		 * Returns whether the number of children hint can be 
		 * trusted for this variable object.
		 * 
		 * @since 3.0 
		 */
		public boolean isNumChildrenHintTrustworthy() {
			// We cannot trust the hint about the number of children when we are
			// dealing with a complex structure that could have the
			// 'protected/public/private' children types.
			// Note that a pointer could have such children, if it points to
			// a complex structure.
			//
			// This is only valid for C++, so we should even check for it using
			// -var-info-expression.  Do we have to use -var-info-expression for each
			// variable object, or can we do it one time only for the whole program?
			// Right now, we always assume we could be using C++
			return ((getNumChildrenHint() == 0 && ! hasMore()) || isArray());
		}
 
		public String getValue(String format) { return valueMap.get(format); }
		
        public ExpressionInfo[] getChildren() { return children; }

        
		public boolean isArray() { return (getGDBType() == null) ? false : getGDBType().getType() == GDBType.ARRAY; }
		public boolean isPointer() { return (getGDBType() == null) ? false : getGDBType().getType() == GDBType.POINTER; }
		public boolean isMethod() { return (getGDBType() == null) ? false : getGDBType().getType() == GDBType.FUNCTION; }
		// A complex variable is one with children.  However, it must not be a pointer since a pointer 
		// does have children, but is still a 'simple' variable, as it can be modifed.  
		// Note that the numChildrenHint can be trusted when asking if the number of children is 0 or not
		public boolean isComplex() {
			return (getGDBType() == null) ? false
					: getGDBType().getType() != GDBType.POINTER
							&& (getNumChildrenHint() > 0
									|| hasMore() || getDisplayHint().isCollectionHint());
		}
		
		/**
		 * @return Whether this varobj can safely be asked for all its children.
		 * 
		 * @since 4.0
		 */
		public boolean isSafeToAskForAllChildren() {
			GdbDisplayHint displayHint = getDisplayHint().getGdbDisplayHint();
			
			// Here we balance usability against a slight risk of instability:
			//
			// Usability: if you have a class/struct-like pretty printer
			// all children are fetched, regardless of any limit. This
			// should be safe from gdb side.
			//
			// Risk: If somebody provides a pretty printer for a collection,
			// but forgets to implement the display_hint method, viewing
			// the collection while it is uninitiliazed may cause gdb
			// to never return.
			//
			// => The risk seams reasonable, so we require a limit only
			//    for collections.
			boolean isDynamicButSafe = (displayHint == GdbDisplayHint.GDB_DISPLAY_HINT_STRING)
					|| (displayHint == GdbDisplayHint.GDB_DISPLAY_HINT_NONE);
			
			return !isDynamic() || isDynamicButSafe;
		}
		
		public void setGdbName(String n) { gdbName = n; }
		public void setCurrentFormat(String f) { format = f; }

		/**
		 * @param fullExpression
		 * @param t
		 * @param num
		 * 
		 * @deprecated Use
		 *             {@link #setExpressionData(ExpressionInfo, String, int, boolean)}
		 *             instead.
		 */
		@Deprecated
		public void setExpressionData(String fullExpression, String t, int num) {
			new ExpressionInfo(fullExpression, fullExpression);
		}

		/**
		 * @param info
		 * @param t
		 * @param num
		 *            If the correspinding MI variable is dynamic, the number of
		 *            children currently fetched by gdb.
		 * @param hasMore
		 *            Whether their are more children to fetch.
		 *            
		 * @since 4.0
		 */
		public void setExpressionData(ExpressionInfo info, String t, int num, boolean hasMore) {
			exprInfo = info;
			type = t;
			gdbType = fGDBTypeParser.parse(t);
			numChildrenHint = num;
			this.hasMore = hasMore;
		}

		public void setValue(String format, String val) { valueMap.put(format, val); }
		
		public void resetValues(String valueInCurrentFormat) {
			resetValues();
			setValue(getCurrentFormat(), valueInCurrentFormat); 
		}
		
		public void resetValues() {
			valueMap.put(IFormattedValues.NATURAL_FORMAT, null);
			valueMap.put(IFormattedValues.BINARY_FORMAT, null);
			valueMap.put(IFormattedValues.HEX_FORMAT, null);
			valueMap.put(IFormattedValues.OCTAL_FORMAT, null);
			valueMap.put(IFormattedValues.DECIMAL_FORMAT, null);
		}
		
		/**
		 * @param c
		 *            The new children, or null in order to force fetching
		 *            children anew.
		 */
        public void setChildren(ExpressionInfo[] c) { 
        	children = c;
        	if (children != null) {
        		numChildrenHint = children.length;
        	}

        	if (children != null) {
        		for (ExpressionInfo child : children) {
        			assert (child != null);
        		}
        	}
		}
        
        /**
         * @param newChildren
         * 
         * @since 4.0
         */
        public void addChildren(ExpressionInfo[] newChildren) {
        	if (children == null) {
        		children = new ExpressionInfo[newChildren.length]; 
        		System.arraycopy(newChildren, 0, children, 0, newChildren.length);
        	} else {
        		ExpressionInfo[] oldChildren = children;
        		
        		children = new ExpressionInfo[children.length + newChildren.length];

        		System.arraycopy(oldChildren, 0, children, 0, oldChildren.length);
        		System.arraycopy(newChildren, 0, children, oldChildren.length, newChildren.length);
        	}
        	
        	numChildrenHint = children.length;

			for (ExpressionInfo child : children) {
				assert (child != null);
			}
        }

        /**
         * @param newChildren
         * 
         * @since 4.0
         */
        protected void addChildren(ExpressionInfo[][] newChildren) {
        	int requiredSize = 0;
        	
        	for (ExpressionInfo[] subArray : newChildren) {
        		requiredSize += subArray.length;
			}

        	ExpressionInfo[] plainChildren = new ExpressionInfo[requiredSize];

        	int i = 0;
        	for (ExpressionInfo[] subArray : newChildren) {
        		System.arraycopy(subArray, 0, plainChildren, i, subArray.length);
        		i += subArray.length;
			}
        	
        	addChildren(plainChildren);
        }
        
        /**
         * @param newNumChildren
         * 
         * @since 4.0
         */
        public void shrinkChildrenTo(int newNumChildren) {
        	if (children != null) {
        		ExpressionInfo[] oldChildren = children;
        		for (int i = oldChildren.length - 1; i >= newNumChildren; --i) {
        			String childFullExpression = children[i].getFullExpr();

        			VariableObjectId childId = new VariableObjectId();
        			childId.generateId(childFullExpression, getInternalId());
        			lruVariableList.remove(childId);
        		}


        		children = new ExpressionInfo[newNumChildren];
        		System.arraycopy(oldChildren, 0, children, 0, newNumChildren);
        	}
        	
        	numChildrenHint = newNumChildren;
        }
        
        public void setParent(MIVariableObject p) { 
        	parent = p; 
        	if (p == null) {
				rootToUpdate = (this instanceof MIRootVariableObject) ? (MIRootVariableObject) this
						: null;
        	} else {
        		rootToUpdate = p.getRootToUpdate();
        	}
        }
        
        public void executeWhenNotUpdating(RequestMonitor rm) {
			getRootToUpdate().executeWhenNotUpdating(rm);
		}

		private void lock() {
			locked = true;
		}
		
		private void unlock() {
			locked = false;

			while (operationsPending.size() > 0) {
				operationsPending.poll().done();
			}
		}

		public boolean isOutOfScope() { return outOfScope; }

		/**
		 * @param success
		 * 
		 * @since 4.0
		 */
		protected void creationCompleted(boolean success) {
        	// A creation completed we must be up-to-date, so we
			// can tell any pending monitors that updates are done
			if (success) {
				currentState = STATE_READY;
				while (updatesPending.size() > 0) {
					DataRequestMonitor<Boolean> rm = updatesPending.poll();
					// Nothing to be re-created
					rm.setData(false);
					rm.done();
				}
			} else {
				currentState = STATE_CREATION_FAILED;

				// Creation failed, inform anyone waiting.
				while (updatesPending.size() > 0) {
					RequestMonitor rm = updatesPending.poll();
		            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, 
		            		"Unable to create variable object", null)); //$NON-NLS-1$
					rm.done();
				}
			}
		}

		/**
		 * This method updates the variable object.
		 * Updating a variable object is done by updating its root.
		 */
		public void update(final DataRequestMonitor<Boolean> rm) {
			
			// We check to see if we are already out-of-scope
			// We must do this to avoid the risk of re-creating this object
			// twice, due to race conditions
			if (isOutOfScope()) {
				rm.setData(false);
				rm.done();
			} else if (currentState != STATE_READY) {
				// If we were already updating, we just queue the request monitor
				// until the on-going update finishes.
				updatesPending.add(rm);
			} else {			
				currentState = STATE_UPDATING;
				getRootToUpdate().update(new DataRequestMonitor<Boolean>(fSession.getExecutor(), rm) {
					@Override
					protected void handleCompleted() {
						currentState = STATE_READY;
						
						if (isSuccess()) {
							outOfScope = getRootToUpdate().isOutOfScope();
							// This request monitor is the one that should re-create
							// the variable object if the old one was out-of-scope							
							rm.setData(outOfScope);
							rm.done();
							
							// All the other request monitors must be notified but must
							// not re-create the object, even if it is out-of-scope
							while (updatesPending.size() > 0) {
								DataRequestMonitor<Boolean> pendingRm = updatesPending.poll();
								pendingRm.setData(false);
								pendingRm.done();
							}
						} else {
							rm.setStatus(getStatus());
							rm.done();

							while (updatesPending.size() > 0) {
								DataRequestMonitor<Boolean> pendingRm = updatesPending.poll();
								pendingRm.setStatus(getStatus());
								pendingRm.done();
							}							
						}
					}
				});
			}
		}
		
		/**
         * Process an update on this variable object.
         *
         * @param update What has changed.
         * 
         * @since 4.0
         */
		protected void processChange(final MIVarChange update, final RequestMonitor rm) {

			MIVar[] newChildren = update.getNewChildren();

			// children == null means fetchChildren will happen later, so
			// don't try to create a sparsely filled children array here.
			final boolean addNewChildren = (children != null);

			final ExpressionInfo[] addedChildren = (addNewChildren && (newChildren != null)) ? new ExpressionInfo[newChildren.length]
					: null;

			final CountingRequestMonitor countingRm = new CountingRequestMonitor(fSession.getExecutor(), rm) {

				@Override
				protected void handleCompleted() {
					
					if (! isSuccess()) {
						rm.setStatus(getStatus());
					} else {
						if (update.numChildrenChanged()) {
							if (children != null) {
								// Remove those children that don't exist any longer.
								if (children.length > update.getNewNumChildren()) {
									shrinkChildrenTo(update.getNewNumChildren());
								}
							} else {
								// Just update the child count.
								numChildrenHint = update.getNewNumChildren();
							}

							// Add the new children.
							if ((addedChildren != null) && (addedChildren.length != 0)) {
								addChildren(addedChildren);
							}
						}
						
						assert((children == null) || (children.length == numChildrenHint));

						hasMore = update.hasMore();

						// If there was no -var-list-children yet for a varobj,
						// the new children will not be reported by -var-update.
						// Set the children to null such that the next time children
						// are requested, they will be fetched.
						if (hasMore() && (numChildrenHint == 0)) {
							setChildren(null);
						}	

						resetValues(update.getValue());
					}
					rm.done();
				}
			};
			
			// Process all the child MIVariableObjects.
			int pendingVariableCreationCount = 0;
			if (newChildren != null && newChildren.length != 0) {
				int i = update.getNewNumChildren() - newChildren.length;
				int arrayPosition = 0;				

				for (final MIVar newChild : newChildren) {

					// As long as http://sourceware.org/bugzilla/show_bug.cgi?id=10252
					// is not fixed, it doesn't make sense to use
					// -var-info-path-expression. New children can only
					// be added during the update, if we are a child of a
					// dynamic varobj, and in this case -var-info-path-expression
					// won't work.
					final String childFullExpression = buildChildExpression(
							getExpression(), newChild.getExp());
					
					// Now try to see if we already have this variable
					// object in our Map
					// Since our map names use the expression, and not the
					// GDB given
					// name, we must determine the correct map name from the
					// varName
					final VariableObjectId childId = new VariableObjectId();
					childId.generateId(childFullExpression, getInternalId());
					MIVariableObject childVar = lruVariableList.get(childId);

					if (childVar != null) {
						if ((childVar.currentState == STATE_CREATING)
								|| (childVar.currentState == STATE_NOT_CREATED)) {

							// We must wait until the child MIVariableObject is fully created.
							// This might succeed, or fail. If it succeeds, we can reuse it as
							// a child, otherwise we create a new MIVariableObject for the
							// varobj just provided by gdb.
							
							++pendingVariableCreationCount;
							
							final int insertPosition = arrayPosition;
							final MIVariableObject monitoredVar = childVar;
							final int indexInParent = i;
							
							// varobj is not fully created so add RequestMonitor to pending queue
							childVar.updatesPending.add(new DataRequestMonitor<Boolean>(fSession.getExecutor(), countingRm) {

								@Override
								protected void handleCompleted() {
									if (isSuccess()) {
										if (addNewChildren) {
											addedChildren[insertPosition] = monitoredVar.exprInfo;
										}
									} else {
										// Create a fresh MIVariableObject for this child, using
										// the new varobj provided by gdb.
										MIVariableObject newVar = createChild(childId, childFullExpression,
												indexInParent, newChild);
										if (addNewChildren) {
											addedChildren[insertPosition] = newVar.exprInfo;
										}
									}
									
									countingRm.done();
								}
								
							});

						} else if (childVar.currentState == STATE_CREATION_FAILED) {
							// There has been an attempt the create a MIRootVariableObject for a full
							// expression representing a child of a dynamic varobj. Such an attempt
							// always fails. But here we can now create it (see below).
							childVar = null;
						}
						// Note that we must check the root to know if it is out-of-scope.
						// We cannot check the child as it has not be updated and its
						// outOfScope variable is not updated either.
						else if (childVar.getRootToUpdate().isOutOfScope()) {
							childVar.deleteInGdb();
							childVar = null;
						}
					}

					// Note: we don't need to check for fake children (public, protected, private) here.
					// We enter this code only if the children are provided by a pretty printer and
					// they don't return such children.
					
					if (childVar == null) {
						// Create a fresh MIVariableObject for this child, using
						// the new varobj provided by -var-update.
						childVar = createChild(childId, childFullExpression, i, newChild);
						if (addNewChildren) {
							addedChildren[arrayPosition] = childVar.exprInfo;
						}
					}
					
					++i;
					++arrayPosition;
				}
			}

			countingRm.setDoneCount(pendingVariableCreationCount);
		}

		/**
		 * Variable objects need not be deleted unless they are root.
		 * This method is specialized in the MIRootVariableObject class.
		 */
		public void deleteInGdb() {}
		
		/** 
		 * This method returns the value of the variable object attributes by
		 * using -var-show-attributes.
		 * Currently, the only attribute available is 'editable'.
		 *  
		 * @param rm
		 *            The data request monitor that will hold the value returned
		 */
        private void getAttributes(final DataRequestMonitor<Boolean> rm) {
            if (editable != null) { 
                rm.setData(editable);
                rm.done();
            } else if (isComplex()) {
                editable = false;
                rm.setData(editable);
                rm.done();
            } else {
                fCommandControl.queueCommand(
                		fCommandFactory.createMIVarShowAttributes(getRootToUpdate().getControlDMContext(), getGdbName()), 
                        new DataRequestMonitor<MIVarShowAttributesInfo>(fSession.getExecutor(), rm) {
                            @Override
                            protected void handleSuccess() {
                            	editable = getData().isEditable();

                            	rm.setData(editable);
                            	rm.done();
                            }
                        });
            }
        }

		/** 
		 * This method returns the value of the variable object.
		 * This operation translates to multiple MI commands which affect the state of the
		 * variable object in the back-end; therefore, we must make sure the object is not
		 * locked doing another operation, and we must lock the object once it is our turn
		 * to use it.
		 * 
		 * @param dmc  
		 *            The context containing the format to be used for the evaluation
		 * @param rm
		 *            The data request monitor that will hold the value returned
		 */
		private void getValue(final FormattedValueDMContext dmc,
				              final DataRequestMonitor<FormattedValueDMData> rm) {

			// We might already know the value
			String value = getValue(dmc.getFormatID());
			if (value != null) {
				rm.setData(new FormattedValueDMData(value));
				rm.done();
				return;
			}

			// If the variable is a complex structure, there is no need to ask the back-end for a value,
			// we can give it the {...} ourselves
			// Unless we are dealing with an array, in which case, we want to get the address of it
			if (isComplex() && ! isDynamic()) {
				if (isArray()) {
					// Figure out the address
					IExpressionDMContext exprCxt = DMContexts.getAncestorOfType(dmc, IExpressionDMContext.class);
					IExpressionDMContext addrCxt = fExpressionService.createExpression(exprCxt, "&(" + exprCxt.getExpression() + ")");  //$NON-NLS-1$//$NON-NLS-2$

					final FormattedValueDMContext formatCxt = new FormattedValueDMContext(
							fSession.getId(),
							addrCxt,
							dmc.getFormatID()
					);

					getVariable(
							addrCxt,
							new DataRequestMonitor<MIVariableObject>(fSession.getExecutor(), rm) {
								@Override
								protected void handleSuccess() {
									getData().getValue(formatCxt, rm);

								}
							});
				} else {
					// Other complex structure
					String complexValue = "{...}";     //$NON-NLS-1$
					setValue(dmc.getFormatID(), complexValue);
					rm.setData(new FormattedValueDMData(complexValue));
					rm.done();
				}
				
				return;
			}

			if (locked) {
				operationsPending.add(new RequestMonitor(fSession.getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						getValue(dmc, rm);
					}
				});
			} else {
				lock();

				// If the format is already the one set for this variable object,
				// we don't need to set it again
				if (dmc.getFormatID().equals(getCurrentFormat())) {
					evaluate(rm);								
				} else {
					// We must first set the new format and then evaluate the variable
					fCommandControl.queueCommand(
							fCommandFactory.createMIVarSetFormat(getRootToUpdate().getControlDMContext(), getGdbName(), dmc.getFormatID()), 
							new DataRequestMonitor<MIVarSetFormatInfo>(fSession.getExecutor(), rm) {
								@Override
								protected void handleCompleted() {
									if (isSuccess()) {
										setCurrentFormat(dmc.getFormatID());
										
										// If set-format returned the value, no need to evaluate
										// This is only valid after GDB 6.7
										if (getData().getValue() != null) {
    										setValue(dmc.getFormatID(), getData().getValue());
											rm.setData(new FormattedValueDMData(getData().getValue()));
											rm.done();
											
											// Unlock is done within this method
											resetFormatToNatural();
										} else {
										    evaluate(rm);
										}
									} else {
										rm.setStatus(getStatus());
										rm.done();

										unlock();
									}
								}
							});
				}
			}
		}

		/** 
		 * This method evaluates a variable object 
		 */
		private void evaluate(final DataRequestMonitor<FormattedValueDMData> rm) {
			fCommandControl.queueCommand(
					fCommandFactory.createMIVarEvaluateExpression(getRootToUpdate().getControlDMContext(), getGdbName()), 
					new DataRequestMonitor<MIVarEvaluateExpressionInfo>(fSession.getExecutor(), rm) {
						@Override
						protected void handleCompleted() {
							if (isSuccess()) {
								setValue(getCurrentFormat(), getData().getValue());
								rm.setData(new FormattedValueDMData(getData().getValue()));
								rm.done();
							} else {
								rm.setStatus(getStatus());
								rm.done();
							}
							
							// Unlock is done within this method
							resetFormatToNatural();
						}
					});
		}

		// In GDB, var-update will only report a change if -var-evaluate-expression has
		// changed -- in the current format--.  This means that situations like
		// double z = 1.2;
		// z = 1.4;
		// Will not report a change if the format is anything else than natural.
		// This is because 1.2 and 1.4 are both printed as 1, 0x1, etc
		// Since we cache the values of every format, we must know if -any- format has
		// changed, not just the current one.
		// To solve this, we always do an update in the natural format; I am not aware
		// of any case where the natural format would stay the same, but another format
		// would change.  However, since a var-update update all children as well,
		// we must make sure these children are also in the natural format
		// The simplest way to do this is that whenever we change the format
		// of a variable object, we immediately set it back to natural with a second
		// var-set-format command.
		private void resetFormatToNatural() {
			if (!getCurrentFormat().equals(IFormattedValues.NATURAL_FORMAT)) {
				fCommandControl.queueCommand(
						fCommandFactory.createMIVarSetFormat(getRootToUpdate().getControlDMContext(), getGdbName(), IFormattedValues.NATURAL_FORMAT), 
						new DataRequestMonitor<MIVarSetFormatInfo>(fSession.getExecutor(), null) {
							@Override
							protected void handleCompleted() {
								if (isSuccess()) {
									setCurrentFormat(IFormattedValues.NATURAL_FORMAT);
								}
								unlock();
							}
						});
			} else {
				unlock();
			}			
		}

		/**
		 * This method returns the list of children of the variable object
		 * passed as a parameter.
		 * 
		 * @param exprDmc
		 * 
		 * @param clientNumChildrenLimit
		 *            If the current limit for the given expression is smaller,
		 *            this limit will be applied.
		 * @param rm
		 *            The data request monitor that will hold the children
		 *            returned
		 */
		private void getChildren(final MIExpressionDMC exprDmc,
				final int clientNumChildrenLimit, final DataRequestMonitor<ChildrenInfo> rm) {
			
			if (fetchingChildren) {
				// Only one request monitor can fetch children at a time.
				fetchChildrenPending.add(new DataRequestMonitor<ChildrenInfo>(fSession.getExecutor(), rm) {

					@Override
					protected void handleSuccess() {
						ChildrenInfo info = getData();
						int numChildren = info.getChildren().length;
						if (! info.hasMore() || numChildren >= clientNumChildrenLimit) {
							// No need to fetch further children.
							rm.setData(getData());
							rm.done();
						} else {
							// Need to retry.
							getChildren(exprDmc, clientNumChildrenLimit, rm);
						}
					}
					
				});
			} else {
			
				fetchingChildren = true;
				
				fetchChildren(exprDmc, clientNumChildrenLimit, new DataRequestMonitor<ChildrenInfo>(fSession.getExecutor(), rm) {

					@Override
					protected void handleCompleted() {
						fetchingChildren = false;

						if (isSuccess()) {
							rm.setData(getData());
							rm.done();
							
							while (fetchChildrenPending.size() > 0) {
								DataRequestMonitor<ChildrenInfo> pendingRm = fetchChildrenPending.poll();
								pendingRm.setData(getData());
								pendingRm.done();
							}
						} else {
							rm.setStatus(getStatus());
							rm.done();

							while (fetchChildrenPending.size() > 0) {
								DataRequestMonitor<ChildrenInfo> pendingRm = fetchChildrenPending.poll();
								pendingRm.setStatus(getStatus());
								pendingRm.done();
							}							
						}
					}
				});
			}
		}

		/**
		 * Fetch the out-standing children.
		 * 
		 * @param exprDmc
		 * 
		 * @param clientNumChildrenLimit
		 *            If the current limit for the given expression is smaller,
		 *            this limit will be applied.
		 * @param rm
		 *            The data request monitor that will hold the children
		 *            returned
		 */
		private void fetchChildren(final MIExpressionDMC exprDmc,
				int clientNumChildrenLimit, final DataRequestMonitor<ChildrenInfo> rm) {
			
			final int newNumChildrenLimit = clientNumChildrenLimit != IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED ?
					clientNumChildrenLimit : 1;

			boolean addChildren = requiresAdditionalChildren(newNumChildrenLimit);
			
	        // If we already know the children, no need to go to the back-end
			ExpressionInfo[] childrenArray = getChildren();
	        if (childrenArray != null && ! addChildren) {
	            rm.setData(new ChildrenInfo(childrenArray, hasMore));
	            rm.done();
	            return;
	        }
	        
			// If the variable does not have children, we can return an empty list right away
			if (! hasChildren()) {
	        	// First store the empty list, for the next time
				setChildren(new ExpressionInfo[0]);
				rm.setData(new ChildrenInfo(getChildren(), hasMore));
				rm.done();
				return;
			}
	        
	        // For arrays (which could be very large), we create the children ourselves.  This is
	        // to avoid creating an enormous amount of children variable objects that the view may
	        // never need.  Using -var-list-children will create a variable object for every child
	        // immediately, that is why we don't want to use it for arrays.
	        if (isArray()) {
	        	ExpressionInfo[] childrenOfArray = new ExpressionInfo[getNumChildrenHint()];
	        	String exprName = exprDmc.getExpression();
	        	
        		int castingIndex = 0;
	        	// in case of casts, need to resolve that before dereferencing, to be safe
	        	if (exprDmc instanceof ICastedExpressionDMContext) {
	        		// When casting, if we are dealing with a resulting array, we should surround
	        		// it with parenthesis before we subscript it.
	        		exprName = '(' + exprName + ')';
	        		castingIndex = ((ICastedExpressionDMContext)exprDmc).getCastInfo().getArrayStartIndex();
	        	}
	        	for (int i= 0; i < childrenOfArray.length; i++) {
	        		String fullExpr = exprName + "[" + i + "]";//$NON-NLS-1$//$NON-NLS-2$
	        		String relExpr = exprDmc.getRelativeExpression() + "[" + (castingIndex + i) + "]";//$NON-NLS-1$//$NON-NLS-2$

	        		childrenOfArray[i] = new ExpressionInfo(fullExpr, relExpr, false, exprInfo, i);
	        	}

	        	// First store these children, for the next time
				setChildren(childrenOfArray);
				hasMore = false;
				rm.setData(new ChildrenInfo(getChildren(), hasMore));
	        	rm.done();
	        	return;
	        }
	        
	        // No need to wait for the object to be ready since listing children can be performed
	        // at any time, as long as the object is created, which we know it is, since we can only
	        // be called here with a fully created object.
	        // Also no need to lock the object, since getting the children won't affect other operations

	        final int from = (addChildren && (children != null)) ? getNumChildrenHint() : 0;
	        final int to = Math.max(newNumChildrenLimit, exprInfo.getChildCountLimit());
	        
	        ICommand<MIVarListChildrenInfo> varListChildren = isSafeToAskForAllChildren() ?
	        		fCommandFactory.createMIVarListChildren(getRootToUpdate().getControlDMContext(), getGdbName())
	        		:fCommandFactory.createMIVarListChildren(getRootToUpdate().getControlDMContext(), getGdbName(), from, to);
					
	        fCommandControl.queueCommand(
	        		varListChildren,
	        		new DataRequestMonitor<MIVarListChildrenInfo>(fSession.getExecutor(), rm) {
	        			@Override
	        			protected void handleSuccess() {
	        				MIVar[] children = getData().getMIVars();
	        				final boolean localHasMore = getData().hasMore();
	        				
	        				// The elements of this array normally are an ExpressionInfo, unless it corresponds to
	        				// a fake child (public, protected, private). In this case it is an ExpressionInfo[]
	        				// representing the children of the fake child.
	        				// This in done in order to preserve the order (the index-in-parent information),
	        				// when replacing a fake child by its real children.
	        				final ExpressionInfo[][] realChildren = new ExpressionInfo[children.length][];

	        				final CountingRequestMonitor countingRm = new CountingRequestMonitor(fSession.getExecutor(), rm) {
	        					@Override
	        					protected void handleSuccess() {
	        						// Store the children in our variable object cache
	        						addChildren(realChildren);
	        						hasMore = localHasMore; 
	        						rm.setData(new ChildrenInfo(getChildren(), hasMore));
	        						
	        						int updateLimit = updateLimit(to);
	        						
	        						if (! isSafeToAskForAllChildren()) {
	        							// Make sure the gdb will not hang, if later
	        							// the varobj is updated, but the underlying
	        							// data is still uninitialized.
	        							fCommandControl.queueCommand(
	        									fCommandFactory.createMIVarSetUpdateRange(getRootToUpdate().getControlDMContext(),
	        									getGdbName(), 0, updateLimit),	new DataRequestMonitor<MIInfo>(fSession.getExecutor(), rm));
	        						} else {	        							
	        							rm.done();
	        						}
	        					}
	        				};

	        				int numSubRequests = 0;
	        				for (int i = 0; i < children.length; ++i) {
	        					final MIVar child = children[i];
	        					final int indexInParent = from + i;
	        					final int arrayPosition = i;
	        					
	        					// These children get created automatically as variable objects in GDB, so we should
	        					// add them to the LRU.
	        					// Note that if this variable object already exists, we can be in three scenarios:
	        					// 1- the existing object is the same variable object in GDB.  In this case,
	        					//    the existing and new one are identical so we can keep either one.
	        					// 2- the existing object is out-of-scope and should be replaced by the new one.
	        					//    This can happen if a root was found to be out-of-scope, but this child
	        					//    had not been accessed and therefore had not been removed.
	        					// 3- the existing object is an in-scope root object representing the same expression.
	        					//    In this case, either object can be kept and the other one can be deleted.
	        					//    The existing root could currently be in-use by another operation and may
	        					//    not be deleted; but since we can only have one entry in the LRU, we are
	        					//    forced to keep the existing root.  Note that we need not worry about
	        					//    the newly created child since it will automatically be deleted when
	        					//    its root is deleted.

	        					numSubRequests++;
	        					
	        					// Class to keep track of the child's full expression, but also
	        					// if that child had to use the CastToBaseClassWorkaround,
	        					// which needs to be propagated to its own children.
	        					class ChildFullExpressionInfo {
	        						private String childFullExpression;
	        						private boolean childHasCastToBaseClassWorkaround;
	        						
	        						public ChildFullExpressionInfo(String path) {
	        							this(path, false);
	        						}
	        						
	        						public ChildFullExpressionInfo(String path, boolean hasWorkaround) {
	        							childFullExpression = path == null ? "" : path; //$NON-NLS-1$
	        							childHasCastToBaseClassWorkaround = hasWorkaround;
	        						}
	        						
	        						public String getChildPath() { return childFullExpression; }
	        						public boolean getChildHasCastToBaseClassWorkaround() { return childHasCastToBaseClassWorkaround; }
	        					};
	        					
	        					final DataRequestMonitor<ChildFullExpressionInfo> childPathRm =
	        						new DataRequestMonitor<ChildFullExpressionInfo>(fSession.getExecutor(), countingRm) {
	        						@Override
	        						protected void handleSuccess() {
	        							final String childPath = getData().getChildPath();
	        							// The child varObj we are about to create should have hasCastToBaseClassWorkaround
	        							// set in two conditions:
	        							// 1- if its parent was set (which is the current varObj)
	        							// 2- if the workaround was used for the child itself, which is part of ChildFullExpressionInfo
	        							final boolean childHasCastToBaseClassWorkaround = 
	        									hasCastToBaseClassWorkaround || getData().getChildHasCastToBaseClassWorkaround();
	        							
	        							// For children that do not map to a real expression (such as f.public)
	        							// GDB returns an empty string.  In this case, we can use another unique
	        							// name, such as the variable name
	        							final boolean fakeChild = (childPath.length() == 0);
	        							final String childFullExpression = fakeChild ? child.getVarName() : childPath;
	        							
	        							// Now try to see if we already have this variable object in our Map
	        							// Since our map names use the expression, and not the GDB given
	        							// name, we must determine the correct map name from the varName
	        							final VariableObjectId childId = new VariableObjectId();
	        							childId.generateId(childFullExpression, getInternalId());
	        							MIVariableObject childVar = lruVariableList.get(childId);

										if (childVar != null) {
											
											if ((childVar.currentState == STATE_CREATING)
													|| (childVar.currentState == STATE_NOT_CREATED)) {

												// We must wait until the child MIVariableObject is fully created.
												// This might succeed, or fail. If it succeeds, we can reuse it as
												// a child, otherwise we create a new MIVariableObject for the
												// varobj just provided by gdb.
												
												final MIVariableObject monitoredVar = childVar;
												
												// childVar is not fully created so add a RequestMonitor to the queue
												childVar.updatesPending.add(new DataRequestMonitor<Boolean>(fSession.getExecutor(), countingRm) {

													@Override
													protected void handleCompleted() {
														MIVariableObject var = monitoredVar;
														
														if (! isSuccess()) {
															
															// Create a fresh MIVariableObject for this child, using
															// the new varobj provided by gdb.
															var = createChild(childId, childFullExpression, indexInParent, child);
														}
														
														var.hasCastToBaseClassWorkaround = childHasCastToBaseClassWorkaround;
														
														if (fakeChild) {

															addRealChildrenOfFake(var,	exprDmc, realChildren,
																	arrayPosition, countingRm);
														} else {
															// This is a real child
															realChildren[arrayPosition] = new ExpressionInfo[] { var.exprInfo };
															countingRm.done();
														}														
													}
												});
												
											} else if (childVar.currentState == STATE_CREATION_FAILED) {
												// There has been an attempt the create a MIRootVariableObject for a full
												// expression representing a child of a dynamic varobj. Such an attempt always
												// fails. But here we can now create it (see below).
												childVar = null;
											}
											// Note that we must check the root to know if it is out-of-scope.
											// We cannot check the child as it has not be updated and its
											// outOfScope variable is not updated either.
											else if (childVar.getRootToUpdate().isOutOfScope()) {
												childVar.deleteInGdb();
												childVar = null;
											}
										}

	        							if (childVar == null) {
	        								childVar = createChild(childId, childFullExpression, indexInParent, child);
											
											childVar.hasCastToBaseClassWorkaround = childHasCastToBaseClassWorkaround;

											if (fakeChild) {
												
												addRealChildrenOfFake(childVar,	exprDmc, realChildren,
														arrayPosition, countingRm);
											} else {
												// This is a real child
												realChildren[arrayPosition] = new ExpressionInfo[] { childVar.exprInfo };
												countingRm.done();
											}
	        							}
	        						}
	        					};


	        					if (isAccessQualifier(child.getExp())) {	        						
	        						// This is just a qualifier level of C++, so we don't need
	        						// to call -var-info-path-expression for real, but just pretend we did.
	        						childPathRm.setData(new ChildFullExpressionInfo(""));  //$NON-NLS-1$
	        						childPathRm.done();
	        					} else if (isDynamic() || exprInfo.hasDynamicAncestor()) {
	        						// Equivalent to (which can't be implemented): child.hasDynamicAncestor
	        						// The new child has a dynamic ancestor. Such children don't support
	        						// var-info-path-expression. Build the expression ourselves.
    	    						childPathRm.setData(new ChildFullExpressionInfo(buildChildExpression(exprDmc.getExpression(), child.getExp())));
	        						childPathRm.done();
	        					} else if (hasCastToBaseClassWorkaround) {
	        						// We had to use the "CastToBaseClass" workaround in the hierarchy, so we
	        						// know -var-info-path-expression won't work in this case.  We have to
	        						// build the expression ourselves again to keep the workaround as part 
	        						// of the child's expression.
	        						childPathRm.setData(new ChildFullExpressionInfo(buildChildExpression(exprDmc.getExpression(), child.getExp())));
	        						childPathRm.done();                                 
	        					} else {
	        						// To build the child id, we need the fully qualified expression which we
	        						// can get from -var-info-path-expression starting from GDB 6.7 
	        						fCommandControl.queueCommand(
	        								fCommandFactory.createMIVarInfoPathExpression(getRootToUpdate().getControlDMContext(), child.getVarName()),
	    	        						new DataRequestMonitor<MIVarInfoPathExpressionInfo>(fSession.getExecutor(), childPathRm) {
	        	        						@Override
	        	        						protected void handleCompleted() {
	        	        							if (isSuccess()) {
	        	        								final String expression = getData().getFullExpression();

	        	        								if (needFixForGDBBug320277() && child.getExp().equals(child.getType()) && !isAccessQualifier(getExpressionInfo().getRelExpr())) {
	        	        	        						// Special handling for a derived class that is cast to its base class (see bug 320277)
	        	        	        						//
	        	        	        						// If the name of a child equals its type then it could be a base class.
	        	        	        						// The exception is when the name of the actual variable is identical with the type name (bad coding style :-))
	        	        	        						// The only way to tell the difference is to check if the parent is a fake (public/private/protected).
	        	        	        						// 
	        	        	        						// What we could do instead, is make sure we are using C++ (using -var-info-expression).  That would
	        	        	        						// be safer.  However, at this time, there does not seem to be a way to create a variable with the same
	        	        	        						// name and type using plain C, so we are safe.
	        	        	        						//
	        	        	        						// When we know we are dealing with derived class that is cast to its base class
	        	        	        						// -var-info-path-expression returns (*(testbase*) this) and in some cases
	        	        	        						// this expression will fail when being evaluated in GDB because of a GDB bug.
	        	        	        						// Instead, we need (*(struct testbase*) this).
	        	        									//
	        	        									// To check if GDB actually has this bug we call -data-evaluate-expression with the return value
	        	        									// of -var-info-path-expression
	        	        									IExpressionDMContext exprDmcMIData = fExpressionService.createExpression(exprDmc, expression);
	        	        									fCommandControl.queueCommand(
	        	        											fCommandFactory.createMIDataEvaluateExpression(exprDmcMIData),
	        	        											new DataRequestMonitor<MIDataEvaluateExpressionInfo>(fSession.getExecutor(), childPathRm) {
	        	        												@Override
	        	        												protected void handleCompleted() {
	        	        													if (isSuccess()) {
	        	        														childPathRm.setData(new ChildFullExpressionInfo(expression));
	        	        														childPathRm.done();
	        	        													} else {
        	        															// We build the expression ourselves
        	        															// We must also indicate that this workaround has been used for this child
        	        															// so that we know to keep using it for further descendants.
	        	        														childPathRm.setData(new ChildFullExpressionInfo(buildDerivedChildExpression(exprDmc.getExpression(), child.getExp()), true));
	        	        														childPathRm.done();
	        	        													}
	        	        												}
	        	        											});
	        	        								} else {
	        	        									childPathRm.setData(new ChildFullExpressionInfo(expression));
	        	        									childPathRm.done();
	        	        								}
	        	        							} else {
	        	        								// If we don't have var-info-path-expression
	        	        								// build the expression ourselves
	        	        								// Note that this does not work well yet
	        	        								childPathRm.setData(new ChildFullExpressionInfo(buildChildExpression(exprDmc.getExpression(), child.getExp())));
	        	        								childPathRm.done();
	        	        							}
	        	        						}
	        								});
	        					}
	        				}

	        				countingRm.setDoneCount(numSubRequests);
	        			}
	        		});
		}
		
		/**
		 * Create a child variable of this MIVariableObject and initialize
		 * it from the given MIVar data.
		 * 
		 * @param childId
		 * @param childFullExpression
		 * @param indexInParent
		 * @param childData
		 * 
		 * @return The new child.
		 * 
		 * @since 4.0
		 */
		protected MIVariableObject createChild(final VariableObjectId childId,
				final String childFullExpression, final int indexInParent,
				final MIVar childData) {
			
			MIVariableObject var = createVariableObject(childId, this);
			ExpressionInfo childInfo = new ExpressionInfo(childFullExpression,
					childData.getExp(), childData.isDynamic(), exprInfo,
					indexInParent);

			var.initFrom(childData, childInfo);
			return var;
		}
		
		/**
		 * @param clientLimit
		 * @return True, if the client specified limit requires to check for
		 *         further children.
		 *         
		 * @since 4.0
		 */
		protected boolean requiresAdditionalChildren(int clientLimit) {
			return !isSafeToAskForAllChildren()
					&& (exprInfo.getChildCountLimit() < calculateNewLimit(clientLimit));
		}

		/**
		 * @param newLimit
		 *            The new limit on the number of children being asked for or
		 *            being updated.
		 * 
		 * @return The new limit.
		 * @since 4.0
		 */
		protected int updateLimit(int newLimit) {
			exprInfo.setChildCountLimit(calculateNewLimit(newLimit));
			return exprInfo.getChildCountLimit();
		}
		
		private int calculateNewLimit(int clientLimit) {
			int limit = exprInfo.getChildCountLimit();
			
			if (!isSafeToAskForAllChildren() && clientLimit != IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED) {
				if (limit == IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED) {
					return clientLimit;
				} else if (limit < clientLimit) {
					return clientLimit;
				}
			}
			
			return limit;
		}

		/**
		 * Obtain the children of the given fake child (public, protected, or
		 * private) and insert them into a list of children at a given position.
		 * 
		 * @param fakeChild
		 * @param frameCtxProvider
		 * @param realChildren
		 * @param position
		 * @param rm The request monitor on which to call done upon completion.
		 */
		private void addRealChildrenOfFake(MIVariableObject fakeChild,
				IExpressionDMContext frameCtxProvider,
				final ExpressionInfo[][] realChildren, final int position,
				final RequestMonitor rm) {

			MIExpressionDMC fakeExprCtx = createExpressionCtx(frameCtxProvider,
					fakeChild.exprInfo);

			// This is just a qualifier level of C++, and we must get the
			// children of this child to get the real children
			fakeChild.getChildren(fakeExprCtx,
					IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED,
					new DataRequestMonitor<ChildrenInfo>(
							fSession.getExecutor(), rm) {

						@Override
						protected void handleCompleted() {
							if (isSuccess()) {
								realChildren[position] = getData()
										.getChildren();

								// Fake nodes can never be dynamic varobjs, and because
								// of this, hasMore is always false.
								assert (!getData().hasMore());
							}
							rm.done();
						}
					});
		}

		/**
		 * Method performing special handling for a derived class that is cast to its base class (see bug 320277).
		 * The command '-var-info-path-expression' returns (*(testbase*) this) but we need (*(struct testbase*) this).
		 * Also, in case of a namespace this method adds additional backticks:
		 * (*(struct 'namespace::testbase'*) this)
		 */
		private String buildDerivedChildExpression(String parentExp, String childExpr) {
			
			final String CAST_PREFIX = "struct "; //$NON-NLS-1$
			
			// Before doing the cast, let's surround the child expression (base class name) with quotes 
			// if it contains a :: which indicates a namespace
			String childNameForCast = childExpr.contains("::") ? "'" + childExpr + "'" : childExpr; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String childFullExpression;
			if (isPointer()) {
				// casting to pointer base class requires a slightly different format
				childFullExpression = "*(" + CAST_PREFIX + childNameForCast + "*)(" + parentExp + ")";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				// casting to base class
				childFullExpression = "(" + CAST_PREFIX + childNameForCast + ")" + parentExp;//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			return childFullExpression;
		}
		
		/**
		 * This method builds a child expression based on its parent's expression.
		 * It is a fallback solution for when GDB doesn't support the var-info-path-expression.
		 * 
		 * This method does not take care of inherited classes such as
		 * class foo : bar {
		 * ...
		 * }
		 * that case is hanlded by buildDerivedChildExpression
		 */
		private String buildChildExpression(String parentExp, String childExp) {
			String childFullExpression;

			// If the current varObj is a fake object, we obtain the proper parent
			// expression from the parent of the varObj.
			if (isAccessQualifier(exprInfo.getRelExpr())) {
				parentExp = getParent().getExpression();
			}
			
			// For pointers, the child expression is already contained in the parent,
			// so we must simply prefix with *
		    //  See Bug219179 for more information.
			if (!isDynamic() && !exprInfo.hasDynamicAncestor() && isPointer()) {
				childFullExpression =  "*("+parentExp+")"; //$NON-NLS-1$//$NON-NLS-2$
			} else {
				// We must surround the parentExp with parentheses because it
				// may be a casted expression.
				childFullExpression = "("+parentExp+")." + childExp; //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		    // No need for a special case for arrays since we deal with arrays differently
		    // and don't call this method for them

			return childFullExpression;
		}

		/**
		 * This method returns the count of children of the variable object
		 * passed as a parameter.
		 * 
		 * @param exprDmc
		 * 
		 * @param numChildrenLimit
		 *            No need to check for more than this number of children.
		 *            However, it is legal to return a higher count if
		 *            we already new from earlier call that there are more
		 *            children.
		 *            
		 * @param rm
		 *            The data request monitor that will hold the count of
		 *            children returned
		 */
		private void getChildrenCount(MIExpressionDMC exprDmc, final int numChildrenLimit,
				final DataRequestMonitor<ChildrenCountInfo> rm) {
			if (isNumChildrenHintTrustworthy()){
				rm.setData(new ChildrenCountInfo(getNumChildrenHint(), hasMore()));
				rm.done();
				return;
			}
			
			getChildren(exprDmc, numChildrenLimit,
					new DataRequestMonitor<ChildrenInfo>(fSession.getExecutor(), rm) {

				@Override
				protected void handleSuccess() {
					rm.setData(new ChildrenCountInfo(getData()
							.getChildren().length, getData().hasMore()));
					rm.done();
				}
			});
		}
		

		
		/** 
		 * This method request the back-end to change the value of the variable object.
		 * 
		 * @param value
		 *            The new value.
		 *  @param formatId
		 *            The format the new value is specified in.
		 * @param rm
		 *            The request monitor to indicate the operation is finished
		 */
		private void writeValue(String value, String formatId, final RequestMonitor rm) {

			// If the variable is a complex structure (including an array), then we cannot write to it
			if (isComplex()) {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, 
						"Cannot change the value of a complex expression", null)); //$NON-NLS-1$
				rm.done();
				return;
	        }
			
			// First deal with the format.  For GDB, the way to specify a format is to prefix the value with
			// 0x for hex, 0 for octal etc  So we need to make sure that 'value' has this prefix.
			// Note that there is no way to specify a binary format for GDB up to and including
			// GDB 6.7.1, so we convert 'value' into a decimal format.  
			// If the formatId is NATURAL, we do nothing for now because it is more complicated.
			// For example for a bool, a value of "true" is correct and should be left as is,
			// but for a pointer a value of 16 should be sent to GDB as 0x16.  To figure this out,
			// we need to know the type of the variable, which we don't have yet.
			
			if (formatId.equals(IFormattedValues.HEX_FORMAT)) {
				if (!value.startsWith("0x")) value = "0x" + value;  //$NON-NLS-1$  //$NON-NLS-2$
			}
			else if (formatId.equals(IFormattedValues.OCTAL_FORMAT)) {
				if (!value.startsWith("0")) value = "0" + value;  //$NON-NLS-1$  //$NON-NLS-2$
			}
			else if (formatId.equals(IFormattedValues.BINARY_FORMAT)) {
				// convert from binary to decimal
				if (value.startsWith("0b")) value = value.substring(2, value.length());  //$NON-NLS-1$
				try {
	    			value = Integer.toString(Integer.parseInt(value, 2));
				} catch (NumberFormatException e) {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, 
							"Invalid binary number: " + value, e)); //$NON-NLS-1$
					rm.done();
					return;
				}
				
				formatId = IFormattedValues.DECIMAL_FORMAT;
			}
			else if (formatId.equals(IFormattedValues.DECIMAL_FORMAT)) {
				// nothing to do
			}
			else if (formatId.equals(IFormattedValues.NATURAL_FORMAT)) {
				// we do nothing for now and let the user have put in the proper value
			}
			else {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, 
						"Unknown format: " + formatId, null)); //$NON-NLS-1$
				rm.done();
				return;
			}
			
			// If the value has not changed, no need to set it.
			// Return a warning status so that handleSuccess is not called and we don't send
			// an ExpressionChanged event
			if (value.equals(getValue(formatId))) {
				rm.setStatus(new Status(IStatus.WARNING, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, 
						"Setting to the same value of: " + value, null)); //$NON-NLS-1$
				rm.done();
				return;				
			}

			// No need to be in ready state or to lock the object
			fCommandControl.queueCommand(
					fCommandFactory.createMIVarAssign(getRootToUpdate().getControlDMContext(), getGdbName(), value),
					new DataRequestMonitor<MIVarAssignInfo>(fSession.getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							// We must also mark all variable objects
							// as out-of-date. This is because some variable objects may be affected
							// by this one having changed.
							// e.g., 
							//    int i;  
							//    int* pi = &i;
							// Here, if 'i' is changed by the user, then 'pi' will also change
							// Since there is no way to know this unless we keep track of all addresses,
							// we must mark everything as out-of-date.  See bug 213061
							markAllOutOfDate();
							
							// Useless since we just marked everything as out-of-date
							// resetValues(getData().getValue());
							
							rm.done();
						}
					});
		}

		private boolean isAccessQualifier(String str) {
			return str.equals("private") || str.equals("public") || str.equals("protected");  //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$ 
	    }

		/**
		 * @return If true, this variable object can be reported as changed in
		 *         a -var-update MI command.
		 */
		public boolean isModifiable() {
			if (!isComplex() || isDynamic()) return true;
			return false;
		}

		/**
		 * @param exprCtx
		 * @param rm
		 * 
		 * @since 4.0
		 */
		public void create(final IExpressionDMContext exprCtx,
				final RequestMonitor rm) {

			if (currentState == STATE_NOT_CREATED) {

				currentState = STATE_CREATING;
				
				final MIExpressionDMC miExprCtx = (MIExpressionDMC) exprCtx;
				final int indexInParent = miExprCtx.getExpressionInfo().getIndexInParentExpression();

				fCommandControl.queueCommand(fCommandFactory.createMIVarListChildren(getParent().getRootToUpdate().getControlDMContext(),
								getParent().getGdbName(), indexInParent, indexInParent + 1),
						new DataRequestMonitor<MIVarListChildrenInfo>(fSession.getExecutor(), rm) {

							@Override
							protected void handleSuccess() {
								if (getData().getMIVars().length == 1) {
									MIVar miVar = getData().getMIVars()[0];
									
									ExpressionInfo localExprInfo = miExprCtx.getExpressionInfo();
									
									localExprInfo.setDynamic(miVar.isDynamic());
									
									initFrom(miVar, localExprInfo);

									if (exprInfo.isDynamic()
											&& (exprInfo.getChildCountLimit() != IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED)) {
										// Restore the original update range.
										fCommandControl.queueCommand(fCommandFactory.createMIVarSetUpdateRange(
												getRootToUpdate().getControlDMContext(),
												getGdbName(), 0, exprInfo.getChildCountLimit()),
												new DataRequestMonitor<MIInfo>(fSession.getExecutor(), rm));
									} else {
										rm.done();
									}
								} else {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, 
											"Unexpected return on -var-list-children", null)); //$NON-NLS-1$
									rm.done();
								}
							}
						});
			} else {
				assert false;
			}
		}

		private void initFrom(MIVar miVar, ExpressionInfo newExprInfo) {
			// Possible Optimization in GDB: In -var-list-children, the has_more
			// field is missing for the children. As a workaround, we assume that
			// if numChild is 0 and has_more is omitted, we have more children.
			boolean newHasMore = miVar.hasMore()
					|| (miVar.isDynamic() && (miVar.getNumChild() == 0));
			
			setGdbName(miVar.getVarName());
			setDisplayHint(miVar.getDisplayHint());
			setExpressionData(
					newExprInfo,
					miVar.getType(),
			        miVar.getNumChild(),
					newHasMore);

			// This will replace any existing entry
			lruVariableList.put(getInternalId(), this);

			// Is this new child a modifiable descendant of the root?
			if (isModifiable()) {
				getRootToUpdate().addModifiableDescendant(miVar.getVarName(), this);
			}
		}
	}
	
	/**
	 * Method to allow to override the MIVariableObject creation
	 * 
     * @since 3.0
     */
	protected MIVariableObject createVariableObject(VariableObjectId id, MIVariableObject parentObj) {
	    return new MIVariableObject(id, parentObj);
	}

	/**
	 * Method to allow to override the MIVariableObject creation
	 * 
     * @since 4.0
	 */
	protected MIVariableObject createVariableObject(VariableObjectId id,
			MIVariableObject parentObj, boolean needsCreation) {
		return new MIVariableObject(id, parentObj, needsCreation);
	}

	/**
     * @since 3.0
     */
	public class MIRootVariableObject extends MIVariableObject {

		// The control context within which this variable object was created
		// It only needs to be stored in the Root VarObj since any children
		// will have the same control context
	    private ICommandControlDMContext fControlContext = null;
	    
		private boolean fOutOfDate = false;
		
		/**
	     * A modifiable descendant is any variable object that is a descendant and
	     * for which the value (leaf variable objects and dynamic variable objects)
	     * or number of children (dynamic variable objects) can change.
		 */
		private Map<String, MIVariableObject> modifiableDescendants;

		public MIRootVariableObject(VariableObjectId id) {
			super(id, null);
			currentState = STATE_NOT_CREATED;
			modifiableDescendants = new HashMap<String, MIVariableObject>();
		}

		public ICommandControlDMContext getControlDMContext() { return fControlContext; }

		public boolean isUpdating() { return currentState == STATE_UPDATING; }
        
		public void setOutOfDate(boolean outOfDate) { fOutOfDate = outOfDate; }
		
		public boolean getOutOfDate() { return fOutOfDate; }
		
		// Remember that we must add ourself as a modifiable descendant if our value can change
		public void addModifiableDescendant(String gdbName, MIVariableObject descendant) {
			modifiableDescendants.put(gdbName, descendant);
		}
		
		/**
		 * @since 4.0
		 */
		public void processChanges(MIVarChange[] updates, RequestMonitor rm) {
			CountingRequestMonitor countingRm = new CountingRequestMonitor(fSession.getExecutor(), rm);
			countingRm.setDoneCount(updates.length);
			
			for (MIVarChange update : updates) {
				MIVariableObject descendant = modifiableDescendants.get(update.getVarName());
				
				// Descendant should never be null, but just to be safe
				if (descendant != null) {
					descendant.processChange(update, countingRm);
				} else {
					// This can for instance happen if a child MIVariableObject is deleted
					// from the cache. The corresponding varobj in gdb does still exist,
					// and if it is changed, gdb reports it as changed in -var-update.
					countingRm.done();
				}
			}
		}
		
		@Override
		public void create(final IExpressionDMContext exprCtx,
                           final RequestMonitor rm) {

			if (currentState == STATE_NOT_CREATED) {
				
				currentState = STATE_CREATING;
				fControlContext = DMContexts.getAncestorOfType(exprCtx, ICommandControlDMContext.class);

				fCommandControl.queueCommand(
						fCommandFactory.createMIVarCreate(exprCtx, exprCtx.getExpression()), 
						new DataRequestMonitor<MIVarCreateInfo>(fSession.getExecutor(), rm) {
							@Override
							protected void handleCompleted() {
								if (isSuccess()) {
									setGdbName(getData().getName());
									setDisplayHint(getData().getDisplayHint());
									
									MIExpressionDMC miExprCtx = (MIExpressionDMC) exprCtx;
									ExpressionInfo localExprInfo = miExprCtx
											.getExpressionInfo();
									
									localExprInfo.setDynamic(getData()
											.isDynamic());
									localExprInfo.setParent(null);
									localExprInfo.setIndexInParent(-1);
									
									setExpressionData(
											localExprInfo,
											getData().getType(),
											getData().getNumChildren(),
											getData().hasMore());
									
									// Store the value returned at create (available in GDB 6.7)
									// Don't store if it is an array, since we want to show
									// the address of an array as its value
									if (getData().getValue() != null && !isArray()) { 
										setValue(getCurrentFormat(), getData().getValue());
									}
									
									// If we are modifiable, we should be in our modifiable list
									if (isModifiable()) {
										addModifiableDescendant(getData().getName(), MIRootVariableObject.this);
									}

									if (localExprInfo.isDynamic()
											&& (localExprInfo.getChildCountLimit() != IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED)) {
										
										// Restore the original update range.
										fCommandControl.queueCommand(fCommandFactory.createMIVarSetUpdateRange(
												getRootToUpdate().getControlDMContext(),getGdbName(),
												0,localExprInfo.getChildCountLimit()),
												new DataRequestMonitor<MIInfo>(fSession.getExecutor(), rm));
									} else {
										rm.done();
									}											 	        						
								} else {
									rm.setStatus(getStatus());
									rm.done();
								}								
							}
						});
			} else {
				assert false;
			}
		}
		
		@Override
		public void update(final DataRequestMonitor<Boolean> rm) {

			if (isOutOfScope()) {
		    	rm.setData(false);
				rm.done();
			} else if (currentState != STATE_READY) {
				// Object is not fully created or is being updated
				// so add RequestMonitor to pending queue
				updatesPending.add(rm);
			} else if (getOutOfDate() == false) {
				rm.setData(false);
				rm.done();
			} else {
				// Object needs to be updated in the back-end
				currentState = STATE_UPDATING;

				// In GDB, var-update will only report a change if -var-evaluate-expression has
				// changed -- in the current format--.  This means that situations like
				// double z = 1.2;
				// z = 1.4;
				// Will not report a change if the format is anything else than natural.
				// This is because 1.2 and 1.4 are both printed as 1, 0x1, etc
				// Since we cache the values of every format, we must know if -any- format has
				// changed, not just the current one.
				// To solve this, we always do an update in the natural format; I am not aware
				// of any case where the natural format would stay the same, but another format
				// would change.  However, since a var-update update all children as well,
			    // we must make sure these children are also in the natural format
				// The simplest way to do this is that whenever we change the format
				// of a variable object, we immediately set it back to natural with a second
				// var-set-format command.  This is done in the getValue() method
				fCommandControl.queueCommand(
						fCommandFactory.createMIVarUpdate(getRootToUpdate().getControlDMContext(), getGdbName()),
						new DataRequestMonitor<MIVarUpdateInfo>(fSession.getExecutor(), rm) {
							@Override
							protected void handleCompleted() {
								if (isSuccess()) {
									setOutOfDate(false);
									
									MIVarChange[] changes = getData().getMIVarChanges();
									if (changes.length > 0 && changes[0].isInScope() == false) {
										// Object is out-of-scope
										currentState = STATE_READY;

										outOfScope = true;
										
										// We can delete this root in GDB right away.  This is safe, even
									 	// if the root has children, because they are also out-of-scope.
										// We -must- also remove this entry from our LRU.  If we don't
										// we can end-up with a race condition that create this object
										// twice, or have an infinite loop while never re-creating the object.
										// The can happen if we update a child first then we request 
										// the root later,
										lruVariableList.remove(getInternalId());

										rm.setData(true);
										rm.done();
										
										while (updatesPending.size() > 0) {
											DataRequestMonitor<Boolean> pendingRm = updatesPending.poll();
											pendingRm.setData(false);
											pendingRm.done();
										}
									} else {
										// The root object is now up-to-date, we must parse the changes, if any.
										processChanges(changes, new RequestMonitor(fSession.getExecutor(), rm) {
											@Override
											protected void handleCompleted() {
												currentState = STATE_READY;

												// We only mark this root as updated in our list if it is in-scope.
												// For out-of-scope object, we don't ever need to re-update them so
												// we don't need to add them to this list.
												rootVariableUpdated(MIRootVariableObject.this);

												if (isSuccess()) {
													rm.setData(false);
												} else {
													rm.setStatus(getStatus());
												}
												rm.done();

												while (updatesPending.size() > 0) {
													DataRequestMonitor<Boolean> pendingRm = updatesPending.poll();
													if (isSuccess()) {
														pendingRm.setData(false);
													} else {
														pendingRm.setStatus(getStatus());
													}
													pendingRm.done();
												}
											};
										});
									}									
								} else {
									// We were not able to update for some reason
									currentState = STATE_READY;

									rm.setData(false);
									rm.done();

									while (updatesPending.size() > 0) {
										DataRequestMonitor<Boolean> pendingRm = updatesPending.poll();
										pendingRm.setStatus(getStatus());
										pendingRm.done();
									}
								}
							}
						});
		    }
		}

		/**
		 * This method request the back-end to delete a variable object.
		 * We check if the GDB name has been filled to confirm that this object
		 * was actually successfully created on the back-end.
		 * Only root variable objects are deleted, while children are left in GDB 
		 * to be deleted automatically when their root is deleted.
		 */
		@Override
		public void deleteInGdb() {			
		    if (getGdbName() != null) {
	    		fCommandControl.queueCommand(
	    				fCommandFactory.createMIVarDelete(getRootToUpdate().getControlDMContext(), getGdbName()),
	    				new DataRequestMonitor<MIVarDeleteInfo>(fSession.getExecutor(), null));
		        // Nothing to do in the requestMonitor, since the object was already
		        // removed from our list before calling this method.

		    	// Set the GDB name to null to make sure we don't attempt to delete
		    	// this variable a second time.  This can happen if the LRU triggers
		    	// an automatic delete.
		    	setGdbName(null);
		    } else {
		        // Variable was never created or was already deleted, no need to do anything.
		    }
		    
			super.deleteInGdb();
		}
	}
	
    /**
 	 * Method to allow to override the MIRootVariableObject creation.
	 *
     * @since 3.0
     */
	protected MIRootVariableObject createRootVariableObject(VariableObjectId id) {
	    return new MIRootVariableObject(id);
	}
	
	/**
	 * This class represents an unique identifier for a variable object.
	 * 
	 * The following must be considered to obtain a unique name:
	 *     - the expression itself
	 *     - the execution context 
	 *     - relative depth of frame based on the frame context and the total depth of the stack
	 *     
	 * Note that if no frameContext is specified (only Execution, or even only Container), which can
	 * characterize a global variable for example, we will only use the available information.
	 * 
	 * @since 3.0
	 */
	public class VariableObjectId {
		// We don't use the expression context because it is not safe to compare them
		// See bug 187718.  So we store the expression itself, and it's parent execution context.
		String fExpression = null;
		IExecutionDMContext fExecContext = null;
		// We need the depth of the frame.  The frame level is not sufficient because 
        // the same frame will have a different level based on the current depth of the stack 
		Integer fFrameId = null;
		
		public VariableObjectId() {
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof VariableObjectId) {
				VariableObjectId otherId = (VariableObjectId) other;
				return (fExpression == null ? otherId.fExpression == null : fExpression.equals(otherId.fExpression)) &&
    				(fExecContext == null ? otherId.fExecContext == null : fExecContext.equals(otherId.fExecContext)) &&
                    (fFrameId == null ? otherId.fFrameId == null : fFrameId.equals(otherId.fFrameId));
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (fExpression == null ? 0 : fExpression.hashCode()) + 
		           (fExecContext == null ? 0 : fExecContext.hashCode()) +
			       (fFrameId  == null ? 0 : fFrameId.hashCode());
		}

		public void generateId(IExpressionDMContext exprCtx, final RequestMonitor rm) {
			fExpression = exprCtx.getExpression();

			fExecContext = DMContexts.getAncestorOfType(exprCtx, IExecutionDMContext.class);
			if (fExecContext == null) {
				rm.done();
				return;
			}
			
			final IFrameDMContext frameCtx = DMContexts.getAncestorOfType(exprCtx, IFrameDMContext.class);
			if (frameCtx == null) {
				rm.done();
				return;
			}

			// We need the current stack depth to be able to make a unique and reproducible name
			// for this expression.  This is pretty efficient since the stackDepth will be retrieved
			// from the StackService command cache after the first time.
			fStackService.getStackDepth(
					fExecContext, 0,
					new DataRequestMonitor<Integer>(fSession.getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							fFrameId = new Integer(getData() - frameCtx.getLevel());
							rm.done();
						}
					});
		}
		
		public void generateId(String childFullExp, VariableObjectId parentId) {
			// The execution context and the frame depth are the same as the parent
			fExecContext = parentId.fExecContext;
			fFrameId = parentId.fFrameId;
			// The expression here must be the one that is part of IExpressionContext for this child
			// This will allow us to find a variable object directly
			fExpression = childFullExp;
		}
	}
	
    /**
 	 * Method to allow to override the VariableObjectId creation.
	 *
     * @since 3.0
     */
	protected VariableObjectId createVariableObjectId() {
	    return new VariableObjectId();
	}

	
	/**
	 * This is the real work horse of managing our objects. Not only must every
	 * value be unique to get inserted, this also creates an LRU (least recently
	 * used). When we hit our size limitation, the LRUsed will be removed to
	 * make space. Removing means that a GDB request to delete the object is
	 * generated.  We must also take into consideration the fact that GDB will
	 * automatically delete children of a variable object, when deleting the parent
	 * variable object.  Our solution to that is to tweak the LRU to make sure that 
	 * children are always older than their parents, to guarantee the children will 
	 * always be delete before their parents.
	 * 
	 */
	private class LRUVariableCache extends LinkedHashMap<VariableObjectId, MIVariableObject> {
		public static final long serialVersionUID = 0;

		// Maximum allowed concurrent variables
		private static final int MAX_VARIABLE_LIST = 1000;
		
		public LRUVariableCache() {
			super(0,     // Initial load capacity
				  0.75f, // Load factor as defined in JAVA 1.5
				  true); // Order is dictated by access, not insertion
		}

		// We never remove doing put operations.  Instead, we rely on our get() operations
		// to trigger the remove.  See bug 200897
		@Override
		public boolean removeEldestEntry(Map.Entry<VariableObjectId, MIVariableObject> eldest) {
		    return false;
		}

		@Override
		public MIVariableObject get(Object key) {
			MIVariableObject varObj = super.get(key);
		    touchAncestors(varObj);
		    
		    // If we're over our max size, attempt to remove eldest entry.
		    if (size() > MAX_VARIABLE_LIST) {
		    	Map.Entry<VariableObjectId, MIVariableObject> eldest = entrySet().iterator().next();
		    	// First make sure we are not deleting ourselves!
		    	if (!eldest.getValue().equals(varObj)) {
		    		if (eldest.getValue().currentState == MIVariableObject.STATE_READY) {
		    			remove(eldest.getKey());
		    		}
		    	}
		    }
		    return varObj;
		}
		
		private void touchAncestors(MIVariableObject varObj) {
			while (varObj != null) {
				varObj = varObj.getParent();
				// If there is a parent, touch it
				if (varObj != null) super.get(varObj.getInternalId());
			}
		}

        @Override
		public MIVariableObject put(VariableObjectId key, MIVariableObject varObj) {
        	MIVariableObject retVal = super.put(key, varObj);

            // Touch all parents of this element so as
            // to guarantee they are not deleted before their children.
            touchAncestors(varObj);

            return retVal;
        }

		@Override
		public MIVariableObject remove(Object key) {
			MIVariableObject varObj = super.remove(key);
			if (varObj != null) {
				varObj.deleteInGdb();
			}
			return varObj; 
		}
	}

    /**
     * @since 3.0
     */
    private static final GDBTypeParser fGDBTypeParser = new GDBTypeParser();
    
	private final DsfSession fSession;
	
	/** Provides access to the GDB/MI back-end */
	private final ICommandControl fCommandControl;
	private CommandFactory fCommandFactory;

	// The stack service needs to be used to get information such
	// as the stack depth to differentiate between expressions that have the
	// same name but refer to a different context
	private final IStack fStackService;
	private IExpressions fExpressionService;

	// Typically, there will only be one listener, since only the ExpressionService will use this class
    private final List<ICommandListener> fCommandProcessors = new ArrayList<ICommandListener>();
    
	/** Our least recently used cache */
	private final LRUVariableCache lruVariableList;
	
	/** The list of root variable objects that have been updated */
	private final LinkedList<MIRootVariableObject> updatedRootList = new LinkedList<MIRootVariableObject>();

	/**
	 * MIVariableManager constructor
	 * 
	 * @param session
	 *            The session we are working with
	 * @param tracker
	 *            The service tracker that can be used to find other services
	 */
	public MIVariableManager(DsfSession session, DsfServicesTracker tracker) {
	    fSession = session;
		lruVariableList = new LRUVariableCache();
		fCommandControl = tracker.getService(ICommandControl.class);
		fStackService  = tracker.getService(IStack.class);
		fExpressionService = tracker.getService(IExpressions.class);
		fCommandFactory = tracker.getService(IMICommandControl.class).getCommandFactory();

		// Register to receive service events for this session.
        fSession.addServiceEventListener(this, null);
	}

	public void dispose() {
    	fSession.removeServiceEventListener(this);
	}

    /**
     * @since 3.0
     */
	protected DsfSession getSession() {
	    return fSession;
	}
	
	/**
     * @since 3.0
     */
	protected ICommandControl getCommandControl() {
	    return fCommandControl;
	}
	
    /**
     * @since 3.0
     */
	protected void rootVariableUpdated(MIRootVariableObject rootObj) {
	    updatedRootList.add(rootObj);
	}
	
    /**
     * @since 3.0
     */
	protected Map<VariableObjectId, MIVariableObject> getLRUCache() {
		return lruVariableList;
	}
	
	/** 
	 * This method returns a variable object based on the specified
	 * ExpressionDMC, creating it in GDB if it was not created already.
	 * The method guarantees that the variable is finished creating and that
	 * is it not out-of-scope.
	 * 
	 * @param exprCtx
	 *            The expression context to which the variable object is applied to.
	 * 
	 * @param rm
	 *            The data request monitor that will contain the requested variable object
	 */
	private void getVariable(final IExpressionDMContext exprCtx,
                             final DataRequestMonitor<MIVariableObject> rm) {
		// Generate an id for this expression so that we can determine if we already
		// have a variable object tracking it.  If we don't we'll need to create one.
		final VariableObjectId id = createVariableObjectId();
		id.generateId(
				exprCtx,
				new RequestMonitor(fSession.getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
					    getVariable(id, exprCtx, rm);
					}
				});
	}

	private void getVariable(final VariableObjectId id,
			                 final IExpressionDMContext exprCtx,
			                 final DataRequestMonitor<MIVariableObject> rm) {

		final MIVariableObject varObj = lruVariableList.get(id);

		if (varObj == null) {
			// We do not have this varObject, so we create it
			createVariable(id, exprCtx, rm);
		} else {
			// We have found a varObject, but it may not be updated yet.
			// Updating the object will also tell us if it is out-of-scope
			// and if we should re-create it.
			varObj.update(new DataRequestMonitor<Boolean>(fSession.getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					
					boolean shouldCreateNew = getData().booleanValue();
					
					if (varObj.isOutOfScope()) {
					    // The variable object is out-of-scope and we
						// should not use it.
						if (shouldCreateNew) {
							/*
							 * It may happen that when accessing a varObject we find it to be
							 * out-of-scope.  The expression for which we are trying to access a varObject
							 * could still be valid, and therefore we should try to create a new varObject for
							 * that expression.  This can happen for example if two methods use the same name
							 * for a variable. In the case when we find that a varObject is out-of-scope (when
							 * its root is out-of-scope) the following should be done:
						     *
						     * - create a new varObject for the expression (as a root varObject) and insert it
						     * in the LRU.  Make sure that when creating children of this new varObject, they 
						     * will replace any old children with the same name in the LRU (this is ok since the 
						     * children being replaced are also out-of-scope).
							 */
							
						    createVariable(id, exprCtx, rm);
						} else {
							// Just request the variable object again
							// We must use this call to handle the fact that
							// the new object might be in the middle of being
							// created.
							getVariable(id, exprCtx, rm);
						}
					} else {
						// The variable object is up-to-date and valid

						MIExpressionDMC miExprCtx = (MIExpressionDMC) exprCtx;
						ExpressionInfo ctxExprInfo = miExprCtx.getExpressionInfo();
						ExpressionInfo varExprInfo = varObj.getExpressionInfo();
						if (ctxExprInfo != varExprInfo) {
							// exprCtrx could just be created via IExpressions.createExpression,
							// and thus the parent-child relationship is not yet set.
							miExprCtx.setExpressionInfo(varExprInfo);
						}
						
						rm.setData(varObj);
						rm.done();
					}
				}
			});
		}
	}

	
	
	/**
	 * This method creates a variable object in GDB.
	 */
	private void createVariable(final VariableObjectId id, 
			                    final IExpressionDMContext exprCtx,
                                final DataRequestMonitor<MIVariableObject> rm) {

		// If we have a dynamic variable object as ancestor, we cannot use
		// -var-create, so we must create ourselves creating the root, and
		// then use -var-list-children for each further ancestor.
		final MIExpressionDMC miExprCtx =(MIExpressionDMC) exprCtx;
		final ExpressionInfo parentInfo = miExprCtx.getExpressionInfo().getParent();
				
		if ((parentInfo != null) && miExprCtx.getExpressionInfo().hasDynamicAncestor()) {
			
			// Need to set parent when it is known.
			final MIVariableObject newVarObj = createVariableObject(id, null, true);

			// We must put this object in our map right away, in case it is 
			// requested again, before it completes its creation.
			// Note that this will replace any old entry with the same id.
			lruVariableList.put(id, newVarObj);

			MIExpressionDMC parentExprCtx = createExpressionCtx(exprCtx,
					parentInfo);
			
			getVariable(parentExprCtx,
					new DataRequestMonitor<MIVariableObject>(
							fSession.getExecutor(), rm) {
				
				@Override
				protected void handleCompleted() {
					
					if (isSuccess()) {
						final MIVariableObject parentObj = getData();
						newVarObj.setParent(parentObj);
						
						newVarObj.create(miExprCtx,	new RequestMonitor(fSession.getExecutor(), rm) {

							@Override
							protected void handleCompleted() {
								if (isSuccess()) {									
									rm.setData(newVarObj);
									newVarObj.creationCompleted(true);
								} else {
									// Object was not created, remove it from our list
									lruVariableList.remove(id);
									// We avoid this race condition by sending the notifications _after_ removing 
									// the object from the LRU, to avoid any new requests being queue.
									// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=231655
									newVarObj.creationCompleted(false);
									rm.setStatus(getStatus());									
								}
								rm.done();
							}
						});
					} else {
						// Object was not created, remove it from our list
						lruVariableList.remove(id);
						// We avoid this race condition by sending the notifications _after_ removing 
						// the object from the LRU, to avoid any new requests being queue.
						// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=231655
						newVarObj.creationCompleted(false);
						
						rm.setStatus(getStatus());
						rm.done();
					}
				}
			});
			
			return;
		}
		
		// Variable objects that are created directly like this, are considered ROOT variable objects
		// in comparison to variable objects that are children of other variable objects.
		final MIRootVariableObject newVarObj = createRootVariableObject(id);
		
		// We must put this object in our map right away, in case it is 
		// requested again, before it completes its creation.
		// Note that this will replace any old entry with the same id.
		lruVariableList.put(id, newVarObj);
		
		newVarObj.create(exprCtx, new RequestMonitor(fSession.getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					// Also store the object as a varObj that is up-to-date
    				rootVariableUpdated(newVarObj);	
					// VarObj can now be used by others
					newVarObj.creationCompleted(true);

					rm.setData(newVarObj);
					rm.done();
				} else {
					// Object was not created, remove it from our list
					lruVariableList.remove(id);
					// Tell any waiting monitors that creation failed.
					// It is important to do this call after we have removed the id
					// from our LRU; this is to avoid the following:
					//   The same varObj is requested before it was removed from the LRU
					//   but after we called creationCompleted().  
					//   In this case, the request for this varObj would be queued, but  
					//   since creationCompleted() already sent the notifications
					//   the newly queue request will never get serviced.
					// We avoid this race condition by sending the notifications _after_ removing 
					// the object from the LRU, to avoid any new requests being queue.
					// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=231655
					newVarObj.creationCompleted(false);

					rm.setStatus(getStatus());
					rm.done();
				}
			}
		});
	}

	private MIExpressionDMC createExpressionCtx(
			final IExpressionDMContext frameCtxProvider,
			final ExpressionInfo exprInfo) {
		
		IFrameDMContext frameCtx = DMContexts.getAncestorOfType(frameCtxProvider, IFrameDMContext.class);

		MIExpressionDMC exprCtx = new MIExpressionDMC(
				frameCtxProvider.getSessionId(), exprInfo, frameCtx);

		return exprCtx;
	}

	/** 
	 * This method requests the back-end to change the value of an expression.
	 * 
	 * @param ctx
	 *            The context of the expression we want to change
	 * @param expressionValue
	 *            The new value of the expression
	 * @param formatId
	 *            The format in which the expressionValue is specified in
	 * @param rm
	 *            The request monitor to indicate the operation is finished
	 */
	// This method can be called directly from the ExpressionService, since it cannot be cached
	public void writeValue(final IExpressionDMContext ctx, final String expressionValue, 
			final String formatId, final RequestMonitor rm) {
		
		getVariable(
				ctx, 
				new DataRequestMonitor<MIVariableObject>(fSession.getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						getData().writeValue(expressionValue, formatId, rm);
					}
				});
	}

	@Override
    public <V extends ICommandResult> ICommandToken queueCommand(final ICommand<V> command, DataRequestMonitor<V> rm) {
    	
        final ICommandToken token = new ICommandToken() {
        	@Override
            public ICommand<? extends ICommandResult> getCommand() {
                return command;
            }
        };
        
    	// The MIVariableManager does not buffer commands itself, but sends them directly to the real
    	// MICommandControl service.  Therefore, we must immediately tell our calling cache that the command
    	// has been sent, since we can never cancel it.  Note that this removes any option of coalescing, 
    	// but coalescing was not applicable to variableObjects anyway.
    	processCommandSent(token);
    	
    	if (command instanceof ExprMetaGetVar) {
            @SuppressWarnings("unchecked")            
    		final DataRequestMonitor<ExprMetaGetVarInfo> drm = (DataRequestMonitor<ExprMetaGetVarInfo>)rm;
            final MIExpressionDMC exprCtx = (MIExpressionDMC)(command.getContext());
            
            getVariable(
            		exprCtx, 
            		new DataRequestMonitor<MIVariableObject>(fSession.getExecutor(), drm) {
            			@Override
            			protected void handleSuccess() {
            				final MIVariableObject varObj = getData();
            				
							if (varObj.isDynamic() && (varObj.getNumChildrenHint() == 0) && varObj.hasMore()) {
								// Bug/feature in gdb? MI sometimes reports 0 number of children, and hasMore=1.
								// This however, is not a safe indicator that there are children,
								// unless there has been a -var-list-children before.
								// The following call will
								// 1) try to fetch at least one child, because isNumChildrenHintTrustworthy()
								//    returns false for this combination
								// 2) result in the desired -var-list-children, unless it has happened already
								// such that the number of children can at least be used to reliably
								// tell whether there are children or not.
								varObj.getChildrenCount(
										exprCtx, 1,
										new DataRequestMonitor<ChildrenCountInfo>(fSession.getExecutor(), drm) {
											@Override
											protected void handleSuccess() {
												drm.setData(
														new ExprMetaGetVarInfo(
																exprCtx.getRelativeExpression(),
																varObj.isSafeToAskForAllChildren(),
																getData().getChildrenCount(),
																varObj.getType(),
																varObj.getGDBType(),
																!varObj.isComplex(),
																varObj.getDisplayHint().isCollectionHint()));
												drm.done();
												processCommandDone(token, drm.getData());
											}
										});
							} else {
								drm.setData(
										new ExprMetaGetVarInfo(
												exprCtx.getRelativeExpression(),
												varObj.isSafeToAskForAllChildren(),
												// We only provide the hint here.  It will be used for hasChildren()
												// To obtain the correct number of children, the user should use
												// IExpressions#getSubExpressionCount()
												varObj.getNumChildrenHint(),
												varObj.getType(),
												varObj.getGDBType(),
												!varObj.isComplex(),
												varObj.getDisplayHint().isCollectionHint()));
								drm.done();
								processCommandDone(token, drm.getData());
							}
            			}
            		});
        } else if (command instanceof ExprMetaGetAttributes) {
            @SuppressWarnings("unchecked")        
            final DataRequestMonitor<ExprMetaGetAttributesInfo> drm = (DataRequestMonitor<ExprMetaGetAttributesInfo>)rm;
            final IExpressionDMContext exprCtx = (IExpressionDMContext)(command.getContext());
 
            getVariable(
                    exprCtx, 
                    new DataRequestMonitor<MIVariableObject>(fSession.getExecutor(), drm) {
                        @Override
                        protected void handleSuccess() {
                            getData().getAttributes(
                                    new DataRequestMonitor<Boolean>(fSession.getExecutor(), drm) {
                                        @Override
                                        protected void handleSuccess() {
                                            drm.setData(new ExprMetaGetAttributesInfo(getData()));
                                            drm.done();
                                            processCommandDone(token, drm.getData());
                                        }
                                    }); 
                        }
                    });
            

    	} else if (command instanceof ExprMetaGetValue) {
            @SuppressWarnings("unchecked")            
    		final DataRequestMonitor<ExprMetaGetValueInfo> drm = (DataRequestMonitor<ExprMetaGetValueInfo>)rm;
            final FormattedValueDMContext valueCtx = (FormattedValueDMContext)(command.getContext());
            final IExpressionDMContext exprCtx = DMContexts.getAncestorOfType(valueCtx, IExpressionDMContext.class);
            
    		getVariable(
    				exprCtx, 
    			    new DataRequestMonitor<MIVariableObject>(fSession.getExecutor(), drm) {
    	                @Override
    	                protected void handleSuccess() {
    	                	getData().getValue(
    	                			valueCtx, 
    	                			new DataRequestMonitor<FormattedValueDMData>(fSession.getExecutor(), drm) {
    	                    			@Override
    	                    			protected void handleSuccess() {
    	                    				drm.setData(
    	                    						new ExprMetaGetValueInfo(getData().getFormattedValue()));
    	                    				drm.done();
    	                                    processCommandDone(token, drm.getData());
    	                    			}
    	                    		});
    	                }
    			    });
    	
    	} else if (command instanceof ExprMetaGetChildren) {
            @SuppressWarnings("unchecked")            
    		final DataRequestMonitor<ExprMetaGetChildrenInfo> drm = (DataRequestMonitor<ExprMetaGetChildrenInfo>)rm;
            final MIExpressionDMC exprCtx = (MIExpressionDMC)(command.getContext());
            
    		getVariable(
    				exprCtx, 
    				new DataRequestMonitor<MIVariableObject>(fSession.getExecutor(), drm) {
    					@Override
    					protected void handleSuccess() {
    						getData().getChildren(exprCtx, ((ExprMetaGetChildren)command).getNumChildLimit(),
    								new DataRequestMonitor<ChildrenInfo>(fSession.getExecutor(), drm) {
    									@Override
    									protected void handleSuccess() {
    										drm.setData(new ExprMetaGetChildrenInfo(
    												getData().getChildren()));
    										drm.done();
    										processCommandDone(token, drm.getData());
    									}
    								});
    					}
    				});
    	
    	} else if (command instanceof ExprMetaGetChildCount) {
            @SuppressWarnings("unchecked")            
    		final DataRequestMonitor<ExprMetaGetChildCountInfo> drm = (DataRequestMonitor<ExprMetaGetChildCountInfo>)rm;
            final MIExpressionDMC exprCtx = (MIExpressionDMC)(command.getContext());

    		getVariable(
    				exprCtx, 
    				new DataRequestMonitor<MIVariableObject>(fSession.getExecutor(), drm) {
    					@Override
    					protected void handleSuccess() {
    						
    						getData().getChildrenCount(
    								exprCtx, ((ExprMetaGetChildCount) command).getNumChildLimit(),
    								new DataRequestMonitor<ChildrenCountInfo>(fSession.getExecutor(), drm) {
    									@Override
    									protected void handleSuccess() {
									drm.setData(new ExprMetaGetChildCountInfo(
											getData().getChildrenCount()));
    										drm.done();
    			                            processCommandDone(token, drm.getData());
    									}
    								}); 
    					}
    				});
    	
    	} else if (command instanceof MIDataEvaluateExpression<?>) {
    		// This does not use the variable objects but sends the command directly to the back-end
			fCommandControl.queueCommand(command, rm);
    	} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, 
					"Unexpected Expression Meta command", null)); //$NON-NLS-1$
			rm.done();
    	}
    	return token;
    }
    
    /*
     *   This is the command which allows the user to retract a previously issued command. The
     *   state of the command  is that it is in the waiting queue  and has not yet been handed 
     *   to the back-end yet.
     *   
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl#removeCommand(org.eclipse.cdt.dsf.mi.service.command.commands.ICommand)
     */
	@Override
    public void removeCommand(ICommandToken token) {
    	// It is impossible to remove a command from the MIVariableManager.
    	// This should never be called, if we did things right.
    	assert false;
    }
   
    /*
     *  This command allows the user to try and cancel commands  which have been handed off to the 
     *  back-end. Some back-ends support this with extended GDB/MI commands. If the support is there
     *  then we will attempt it.  Because of the bidirectional nature of the GDB/MI command stream
     *  there is no guarantee that this will work. The response to the command could be on its way
     *  back when the cancel command is being issued.
     *  
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.command.IDebuggerControl#cancelCommand(org.eclipse.cdt.dsf.mi.service.command.commands.ICommand)
     */
	@Override
    public void addCommandListener(ICommandListener processor) { fCommandProcessors.add(processor); }
    @Override
    public void removeCommandListener(ICommandListener processor) { fCommandProcessors.remove(processor); }    
	@Override
    public void addEventListener(IEventListener processor) {}
	@Override
    public void removeEventListener(IEventListener processor) {}

    
    private void processCommandSent(ICommandToken token) {
        for (ICommandListener processor : fCommandProcessors) {
            processor.commandSent(token);
        }
    }

    private void processCommandDone(ICommandToken token, ICommandResult result) {
        for (ICommandListener processor : fCommandProcessors) {
            processor.commandDone(token, result);
        }
    }
    
    /**
     * @since 1.1
     */
    public void markAllOutOfDate() {
    	MIRootVariableObject root;
    	while ((root = updatedRootList.poll()) != null) {
    		root.setOutOfDate(true);
    	}       
    }

    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.IResumedDMEvent e) {
    	// Program has resumed, all variable objects need to be updated.
    	// Since only roots can actually be updated in GDB, we only need
    	// to deal with those.  Also, to optimize this operation, we have
    	// a list of all roots that have been updated, so we only have to
    	// set those to needing to be updated.
    	markAllOutOfDate();
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.ISuspendedDMEvent e) {
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(IMemoryChangedEvent e) {
    	// Some memory has changed.  We currently do not know the address
    	// of each of our variable objects, so there is no way to know
    	// which one is affected.  Mark them all as out of date.
    	// The views will fully refresh on a MemoryChangedEvent
    	markAllOutOfDate();
    }
    
    /**
	 * @since 3.0
	 */
    @DsfServiceEventHandler 
    public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
    	// We have a big limitation with tracepoints!
    	// GDB usually only reports a depth of 1, for every trace record, no
    	// matter where it occurred.  This means that our naming scheme for VariableObjectId 
    	// fails miserably because all objects will have the same depth and we will confuse
    	// them.  Until we find a good solution, we have to clear our entire list of
    	// of variable objects (and delete them in GDB to avoid having too many).
    	Iterator<Map.Entry<VariableObjectId, MIVariableObject>> iterator = lruVariableList.entrySet().iterator();
    	while (iterator.hasNext()){
    		iterator.next();
    		iterator.remove();
    	}
    }
    
    /**
     * GDB has a bug which makes -data-evaluate-expression fail when using
     * the return value of -var-info-path-expression in the case of derived classes.
     * To work around this bug, we don't use -var-info-path-expression for some derived
     * classes and all their descendants.
     *
     * This method can be overridden to easily disable the workaround, for versions
     * of GDB that no longer have the bug.
     * 
     * See http://sourceware.org/bugzilla/show_bug.cgi?id=11912
     * and Bug 320277.
     * 
     * The bug was fixed in GDB 7.3.1.
     * 
     * @since 4.1 
     */
    protected boolean needFixForGDBBug320277() {
    	return true;
    }
}
