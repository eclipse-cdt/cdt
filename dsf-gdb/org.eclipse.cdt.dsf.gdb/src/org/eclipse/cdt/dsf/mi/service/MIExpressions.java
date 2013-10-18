/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for handling of multiple execution contexts	
 *     Axel Mueller       - Bug 306555 - Add support for cast to type / view as array (IExpressions2)	
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IExpressions3;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.GDBTypeParser.GDBType;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetAttributes;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetChildCount;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetChildren;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetValue;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetVar;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetAttributesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetChildCountInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetChildrenInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetValueInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetVarInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * This class implements a debugger expression evaluator as a DSF service. The
 * primary interface that clients of this class should use is IExpressions.
 * 
 * This class used to be name ExpressionService in the 1.1 release.
 * 
 * @since 2.0
 */
public class MIExpressions extends AbstractDsfService implements IMIExpressions, ICachingService {

	private static final int PARTITION_LENGTH = 100;

    /**
     * A format that gives more details about an expression and supports pretty-printing
     * provided by the backend.
     * 
     * @since 3.0
     */
   	public static final String DETAILS_FORMAT = "Details"; //$NON-NLS-1$
   	
   	/* The order given here is the order that will be used by DSF in the Details Pane */
   	private static final String[] FORMATS_SUPPORTED = new String[] { 
			DETAILS_FORMAT,
			IFormattedValues.NATURAL_FORMAT,
			IFormattedValues.DECIMAL_FORMAT,
			IFormattedValues.HEX_FORMAT,
			IFormattedValues.BINARY_FORMAT,
			IFormattedValues.OCTAL_FORMAT };
   	
	/**
	 * This class represents the two expressions that characterize an Expression Context.
	 */
	public static class ExpressionInfo {
        private final String fullExpression;
        private final String relativeExpression;
        private boolean isDynamic = false;
    	private ExpressionInfo parent;
    	private int indexInParent = -1;
		private int childCountLimit = IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED;

        public ExpressionInfo(String full, String relative) {
        	fullExpression = full;
        	relativeExpression = relative;
        }

        /**
         * @since 4.0
         */
		public ExpressionInfo(String full, String relative, boolean isDynamic,
				ExpressionInfo parent, int indexInParent) {
			fullExpression = full;
			relativeExpression = relative;
			this.isDynamic = isDynamic;
			this.parent = parent;
			this.indexInParent = indexInParent;
		}

        public String getFullExpr() { return fullExpression; }
        public String getRelExpr() { return relativeExpression; }
        
        @Override
        public boolean equals(Object other) {
        	if (other instanceof ExpressionInfo) {
                if (fullExpression == null ? ((ExpressionInfo) other).fullExpression == null : 
                	fullExpression.equals(((ExpressionInfo) other).fullExpression)) {
                	if (relativeExpression == null ? ((ExpressionInfo) other).relativeExpression == null : 
                		relativeExpression.equals(((ExpressionInfo) other).relativeExpression)) {
                		// The other members don't play any role for equality.
                		return true;
                	}
                }
        	}
        	return false;
        }

        @Override
        public int hashCode() {
        	return (fullExpression == null ? 0 : fullExpression.hashCode()) ^
        	       (relativeExpression == null ? 0 : relativeExpression.hashCode());
    		// The other members don't play any role for equality.
        }
        
        @Override
        public String toString() {
            return "[" + fullExpression +", " + relativeExpression + ", isDynamic=" + isDynamic + "]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
        }
        
		/**
		 * @return The parent expression info, if existing.
		 * @since 4.0
		 */
		public ExpressionInfo getParent() {
			return parent;
		}

		/**
		 * @return The index in the child array of the parent. Only valid if
		 *         {@link #getParent()} returns not null.
		 * @since 4.0
		 */
		public int getIndexInParentExpression() {
			return indexInParent;
		}
		
		/**
		 * @return Whether the corresponding variable object is dynamic,
		 *         i.e. it's value and children are provided by a pretty printer.
		 * @since 4.0
		 */
		public boolean isDynamic() {
			return isDynamic;
		}
		
		/**
		 * @return Whether the expression info has any ancestor that is dynamic.
		 * @since 4.0
		 */
		public boolean hasDynamicAncestor() {
			for (ExpressionInfo parent = getParent(); parent != null; parent = parent.getParent()) {
				if (parent.isDynamic()) {
					return true;
				}
			}
			
			return false;
		}

		/**
		 * @param isDynamic
		 *            Whether the value and children of this expression is
		 *            currently provided by a pretty printer or not.
		 * @since 4.0
		 */
		public void setDynamic(boolean isDynamic) {
			this.isDynamic = isDynamic;
		}
		
		/**
		 * @param parent The new parent expression info.
		 * @since 4.0
		 */
		public void setParent(ExpressionInfo parent) {
			this.parent = parent;
		}
		
		/**
		 * @param index The index in the children array of the parent.
		 * @since 4.0
		 */
		public void setIndexInParent(int index) {
			this.indexInParent = index;
		}

		/**
		 * @return The current limit on the number of children to be fetched.
		 * @since 4.0
		 */
		public int getChildCountLimit() {
			return childCountLimit;
		}

		/**
		 * @param newLimit
		 *            The new limit on the number of children to be fetched.
		 * @since 4.0
		 */
		public void setChildCountLimit(int newLimit) {
			this.childCountLimit = newLimit;
		}
		
		/**
		 * @return if this expression is part of the memory space or not.
		 *         If it not part of the memory space, it won't have an address.
		 * @since 4.3
		 */
		public boolean inMemory() {
			// Registers and convenience variables which both start with $
			// are not part of memory.  We care about the top-most parent
			// as it is the only one that can be a register or convenience var.
			if (getParent() == null) {
				if (getRelExpr().startsWith("$")) { //$NON-NLS-1$			
					return false;
				}
				return true;
			}
			
			return getParent().inMemory();
		}
	}
	
    /**
     * This class represents an expression.
     * @noextend This class is not intended to be subclassed by clients.
     * @since 4.3
     */
    public static class MIExpressionDMC extends AbstractDMContext implements IExpressionDMContext {
        /**
         * This field holds an expression to be evaluated.
         */
    	private ExpressionInfo exprInfo;

        /**
         * ExpressionDMC Constructor for expression to be evaluated in context of 
         * a stack frame.
         * 
         * @param sessionId
         *            The session ID in which this context is created.
         * @param expression
         *            The expression to be described by this ExpressionDMC
         * @param relExpr
         *            The relative expression if this expression was created as a child
         * @param frameCtx
         *            The parent stack frame context for this ExpressionDMC. 
         */
        public MIExpressionDMC(String sessionId, String expression, String relExpr, IFrameDMContext frameCtx) {
            this(sessionId, expression, relExpr, (IDMContext)frameCtx);
        }

       /**
         * ExpressionDMC Constructor for expression to be evaluated in context of 
         * an thread.
         * 
         * @param sessionId
         *            The session ID in which this context is created.
         * @param expression
         *            The expression to be described by this ExpressionDMC
         * @param relExpr
         *            The relative expression if this expression was created as a child
         * @param execCtx
         *            The parent thread context for this ExpressionDMC. 
         */
        public MIExpressionDMC(String sessionId, String expression, String relExpr, IMIExecutionDMContext execCtx) {
            this(sessionId, expression, relExpr, (IDMContext)execCtx);
        }

        /**
         * ExpressionDMC Constructor for expression to be evaluated in context of 
         * a memory space.
         * 
         * @param sessionId
         *            The session ID in which this context is created.
         * @param expression
         *            The expression to be described by this ExpressionDMC
         * @param relExpr
         *            The relative expression if this expression was created as a child
         * @param memoryCtx
         *            The parent memory space context for this ExpressionDMC. 
         */
        public MIExpressionDMC(String sessionId, String expression, String relExpr, IMemoryDMContext memoryCtx) {
            this(sessionId, expression, relExpr, (IDMContext)memoryCtx);
        }

        private MIExpressionDMC(String sessionId, String expr, String relExpr, IDMContext parent) {
        	this(sessionId, new ExpressionInfo(expr, relExpr), parent);
        }

		/**
		 * ExpressionDMC Constructor for expression to be evaluated in context
		 * of a stack frame.
		 * 
		 * @param sessionId
		 *            The session ID in which this context is created.
		 * @param info
		 *            The expression info that this expression is to use.
		 * @param frameCtx
		 *            The parent stack frame context for this ExpressionDMC.
		 *            
		 * @since 4.0
		 */
        public MIExpressionDMC(String sessionId, ExpressionInfo info, IFrameDMContext frameCtx) {
            this(sessionId, info, (IDMContext)frameCtx);
        }

        /**
		 * @since 4.3
		 */
        public MIExpressionDMC(String sessionId, ExpressionInfo info, IDMContext parent) {
            super(sessionId, new IDMContext[] { parent });
            exprInfo = info;
        }

        /**
         * @return True if the two objects are equal, false otherwise.
         */
        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && exprInfo.equals(((MIExpressionDMC)other).exprInfo);
        }

        /**
         * 
         * @return The hash code of this ExpressionDMC object.
         */
        @Override
        public int hashCode() {
            return super.baseHashCode() + exprInfo.hashCode();
        }

        /**
         * 
         * @return A string representation of this ExpressionDMC (including the
         *         expression to which it is bound).
         */
        @Override
        public String toString() {
            return baseToString() + ".expr" + exprInfo.toString(); //$NON-NLS-1$ 
        }

        /**
         * @return The full expression string represented by this ExpressionDMC
         */
    	@Override
        public String getExpression() {
            return exprInfo.getFullExpr();
        }
        
        /**
         * @return The relative expression string represented by this ExpressionDMC
         */
        public String getRelativeExpression() {
            return exprInfo.getRelExpr();
        }

        /**
         * @return Get the expression info for this context.
         * @since 4.0
         */
        public ExpressionInfo getExpressionInfo() {
        	return exprInfo;
        }

		/**
		 * @param info
		 * 
		 * @since 4.0
		 */
		public void setExpressionInfo(ExpressionInfo info) {
			assert (this.exprInfo.getFullExpr().equals(info.getFullExpr()));
			assert (this.exprInfo.getRelExpr().equals(info.getRelExpr()));

			this.exprInfo = info;
		}
    }

    protected static class InvalidContextExpressionDMC extends AbstractDMContext 
        implements IExpressionDMContext
    {
        private final String expression;

        public InvalidContextExpressionDMC(String sessionId, String expr, IDMContext parent) {
            super(sessionId, new IDMContext[] { parent });
            expression = expr;
        }

        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && 
            		(expression == null ? ((InvalidContextExpressionDMC) other).getExpression() == null : expression.equals(((InvalidContextExpressionDMC) other).getExpression()));
        }

        @Override
        public int hashCode() {
            return expression == null ? super.baseHashCode() : super.baseHashCode() ^ expression.hashCode();
        }

        @Override
        public String toString() {
            return baseToString() + ".invalid_expr[" + expression + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

    	@Override
        public String getExpression() {
            return expression;
        }
    }

    /**
	 * @since 4.1
	 */
    protected static class IndexedPartitionDMC extends MIExpressionDMC implements IIndexedPartitionDMContext {

    	final private MIExpressionDMC fParentExpression;
		private final int fIndex;
    	private final int fLength;
    	
    	/**
    	 * @deprecated This method does not keep track of casted expressions.
    	 * It has been replaced by the constructor that takes an MIExpressionDMC
    	 * as a parameter.
    	 */
    	@Deprecated
    	public IndexedPartitionDMC(
				String sessionId, 
				ExpressionInfo parentInfo, 
				IFrameDMContext frameCtx, 
				int index, 
				int length) {
    		this(new MIExpressionDMC(sessionId, parentInfo, frameCtx), frameCtx, index, length);
		}

    	/**
		 * @since 4.2
		 */
    	public IndexedPartitionDMC(
    			MIExpressionDMC parentExpr,
    			int index, 
    			int length) {
    		this(parentExpr, getParentDmc(parentExpr), index, length);
    	}
    	/**
    	 * @param parentExpr The expression of the array.  This can be a casted expression.
    	 *                   This is not the parent that will be used in the context hierarchy, as we chose
    	 *                   not to stack up partitions.
    	 * @param parentDmc The frame or thread context that will be used as a parent in the context hierarchy.
    	 */
    	private IndexedPartitionDMC(
    			MIExpressionDMC parentExpr,
    			IDMContext parentDmc,
    			int index, 
    			int length) {
			super(parentExpr.getSessionId(), createExpressionInfo(parentExpr.getExpressionInfo(), index, length), parentDmc);
			fIndex = index;
			fLength = length;
			fParentExpression = parentExpr;
		}

    	/**
    	 * Find the frame context that will be the parent of this partition in the context hierarchy.
    	 * Not to be confused with the original parent array that contains the partition.  That parent
    	 * can be obtained using getParentExpressionContext()
    	 */
    	private static IDMContext getParentDmc(MIExpressionDMC parentExpr) {
    		IFrameDMContext frameDmc = DMContexts.getAncestorOfType(parentExpr, IFrameDMContext.class);
    		if (frameDmc != null) {
    			return frameDmc;
    		}
    		
    		IMIExecutionDMContext execCtx = DMContexts.getAncestorOfType(parentExpr, IMIExecutionDMContext.class);
    		if (execCtx != null) {
    			// If we have a thread context but not a frame context, we give the user
    			// the expression as per the top-most frame of the specified thread.
    			// To do this, we create our own frame context.
    			DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), parentExpr.getSessionId());
    			MIStack stackService = tracker.getService(MIStack.class);
    			tracker.dispose();
    			
    			if (stackService != null) {
    				return stackService.createFrameDMContext(execCtx, 0);
    			}
    		}
    	
    		return parentExpr;
    	}
    	public ExpressionInfo getParentInfo() {
    		return fParentExpression.getExpressionInfo();
    	}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IExpressions4.IIndexedPartitionDMContext#getParentExpression()
		 */
		@Override
		public String getParentExpression() {
			return getParentExpressionContext().getExpression();
		}

		/**
		 * Get the context of the parent array.  This can be used to know if the
		 * parent array is a casted expression.
		 * @since 4.2
		 */
		public MIExpressionDMC getParentExpressionContext() {
			return fParentExpression;
		}
		
		@Override
		public int getIndex() {
			return fIndex;
		}

		@Override
		public int getLength() {
			return fLength;
		}

        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) &&
                    ((IndexedPartitionDMC) other).getParentExpressionContext().equals(getParentExpressionContext()) &&
                    ((IndexedPartitionDMC) other).getIndex() == getIndex() &&
                    ((IndexedPartitionDMC) other).getLength() == getLength();                
        }

        @Override
        public int hashCode() {
            return super.baseHashCode() + 17*getIndex() + 31*getLength();
        }

        @Override
        public String toString() {
            return String.format( "%s.expr[%s][%d-%d]", baseToString(), getParentExpression(), getIndex(), getIndex() + getLength() - 1); //$NON-NLS-1$
        }

        private static ExpressionInfo createExpressionInfo(ExpressionInfo parentInfo, int index, int length) {
        	String expression = String.format(
        			"*((%s)+%d)@%d",  //$NON-NLS-1$
        			parentInfo.getFullExpr(), 
        			Integer.valueOf(index), 
        			Integer.valueOf(length));
        	return new ExpressionInfo(expression, expression);
        }
    }
    
    
	/**
	 * Contains the address of an expression as well as the size of its type.
	 */
    protected static class ExpressionDMAddress implements IExpressionDMAddress {
    	IAddress fAddr;
    	int fSize;
    	
    	public ExpressionDMAddress(IAddress addr, int size) {
    		fAddr = addr;
    		fSize = size;
    	}

    	public ExpressionDMAddress(String addrStr, int size) {
    		fSize = size;
    		// We must count the "0x" and that
    		// is why we compare with 10 characters
    		// instead of 8
    		if (addrStr.length() <= 10) {
    			fAddr = new Addr32(addrStr);
    		} else {
    			fAddr = new Addr64(addrStr);
    		}
    	}

    	@Override
    	public IAddress getAddress() { return fAddr; }
    	@Override
    	public int getSize() { return fSize; }
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof ExpressionDMAddress) {
				ExpressionDMAddress otherAddr = (ExpressionDMAddress) other;
				return (fSize == otherAddr.getSize()) && 
    				(fAddr == null ? otherAddr.getAddress() == null : fAddr.equals(otherAddr.getAddress()));
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (fAddr == null ? 0 :fAddr.hashCode()) + fSize;
		}

		@Override
		public String toString() {
			return (fAddr == null ? "null" : "(" + fAddr.toHexAddressString()) + ", " + fSize + ")"; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
		}
    }
    
    /**
     * If an expressions doesn't have an address, or it cannot be determined,
     * use this class.
   	 * @since 4.0
     */
    protected class InvalidDMAddress implements IExpressionDMLocation {

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
    }
    
	/**
	 * This class represents the static data referenced by an instance of ExpressionDMC,
	 * such as its type and number of children; it does not contain the value or format
	 * of the expression.
	 */
	protected static class ExpressionDMData implements IExpressionDMDataExtension {
		// This is the relative expression, such as the name of a field within a structure,
		// in contrast to the fully-qualified expression contained in the ExpressionDMC,
		// which refers to the full name, including parent structure.
		private final String relativeExpression;
		private final String exprType;
		
		/**
		 * A hint at the number of children.
		 * In the case of C++ complex structures, this number will not be the
		 * actual number of children.  This is because GDB considers
		 * 'private/protected/public' as an actual level of children, but
		 * we do not.  This number is meant to be used to know if the expression
		 * has children at all.
		 */
		private final int numChildrenHint;
		
		private final boolean editable;
		private final BasicType fBasicType;

		/**
		 * ExpressionDMData constructor.
		 */
		public ExpressionDMData(String expr, String type, int num, boolean edit) {
		    this (expr, type, num, edit, null);
		}

		/**
         * ExpressionDMData constructor.
		 * @since 3.0
         */
        public ExpressionDMData(String expr, String type, int num, boolean edit, BasicType basicType) {
            relativeExpression = expr;
            exprType = type;
            numChildrenHint = num;
            editable = edit;
            fBasicType = basicType;
        }

    	@Override
		public BasicType getBasicType() {
		    return fBasicType;
		}
		
    	@Override
		public String getEncoding() {
			return null;
		}

    	@Override
		public Map<String, Integer> getEnumerations() {
			return new HashMap<String, Integer>();
		}

    	@Override
		public String getName() {
			return relativeExpression;
		}

    	@Override
		public IRegisterDMContext getRegister() {
			return null;
		}

		// See class VariableVMNode for an example of usage of this method
		public String getStringValue() {
			return null;
		}

		@Override
		public String getTypeId() {
			return null;
		}

		@Override
		public String getTypeName() {
			return exprType;
		}

		/**
		 * This method only returns a 'hint' to the number of children.
		 * In the case of C++ complex structures, this number will not be the
		 * actual number of children.  This is because GDB considers
		 * 'private/protected/public' as an actual level of children, but
		 * we do not.
		 * 
		 * This method can be used reliably to know if the expression
		 * does have children or not.  However, for this particular use,
		 * the new {@link IExpressionDMDataExtension#hasChildren()} method should be used instead.
		 * 
		 * To get the correct number of children of an expression, a call
		 * to {@link IExpressions#getSubExpressionCount} should be used.
		 * 
		 * @deprecated
		 */
		@Deprecated
		public int getNumChildren() {
			return numChildrenHint;	
		}
		
		public boolean isEditable() {
			return editable;
		}

		/**
         * @since 4.0
         */
		@Override
		public boolean hasChildren() {
		    return numChildrenHint > 0;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof ExpressionDMData) {
				ExpressionDMData otherData = (ExpressionDMData) other;
				return (numChildrenHint == otherData.numChildrenHint) && 
    				(getTypeName() == null ? otherData.getTypeName() == null : getTypeName().equals(otherData.getTypeName())) &&
    				(getName() == null ? otherData.getName() == null : getName().equals(otherData.getName()));
			}
			return false;
		}

		@Override
		public int hashCode() {
			return relativeExpression == null ? 0 : relativeExpression.hashCode() + 
					exprType == null ? 0 : exprType.hashCode() + numChildrenHint;
		}

		@Override
		public String toString() {
			return "relExpr=" + relativeExpression + ", type=" + exprType + ", numchildren=" + numChildrenHint; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		}
	}

	/**
	 * Event generated every time an expression is changed by the ExpressionService.
	 * 
	 * A client wishing to receive such events has to register as a service
	 * event listener and implement the corresponding eventDispatched method.
	 * 
	 * E.g.:
	 *
	 *    getSession().addServiceEventListener(listenerObject, null);
	 *     
	 *    @DsfServiceEventHandler
	 *    public void eventDispatched(ExpressionChangedEvent e) {
	 *       IExpressionDMContext context = e.getDMContext();
	 *       // do something...
	 *    }
	 */
    protected static class ExpressionChangedEvent extends AbstractDMEvent<IExpressionDMContext>
                        implements IExpressionChangedDMEvent {
    	
        public ExpressionChangedEvent(IExpressionDMContext context) {
        	super(context);
        }
    }

	/**
	 * @since 4.3
	 */
	protected CommandCache fExpressionCache;

	private CommandFactory fCommandFactory;
	private MIVariableManager varManager;

	/** 
	 * Indicates that we are currently visualizing trace data.
	 * In this case, some errors should not be reported.
	 */
	private boolean fTraceVisualization;
	
	public MIExpressions(DsfSession session) {
		super(session);
	}

	/**
	 * This method initializes this service.
	 * 
	 * @param requestMonitor
	 *            The request monitor indicating the operation is finished
	 */
	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}
	
	/**
	 * This method initializes this service after our superclass's initialize()
	 * method succeeds.
	 * 
	 * @param requestMonitor
	 *            The call-back object to notify when this service's
	 *            initialization is done.
	 */
	private void doInitialize(RequestMonitor requestMonitor) {

		// Register to receive service events for this session.
        getSession().addServiceEventListener(this, null);
        
		// Register this service, but only if we don't already have an
        // IExpression service present.  This allows another expression
        // service to be used, while delegating calls to this service.
        if (getServicesTracker().getService(IExpressions.class) == null) {
        	register(new String[] { IExpressions.class.getName(),
        			                IExpressions2.class.getName(), 
        			                IExpressions3.class.getName(),
        			                IMIExpressions.class.getName(),
        			                MIExpressions.class.getName() },
        			new Hashtable<String, String>());
        }
        
		// Create the expressionService-specific CommandControl which is our
        // variable object manager.
        // It will deal with the meta-commands, before sending real MI commands
        // to the back-end, through the MICommandControl service
		// It must be created after the ExpressionService is registered
		// since it will need to find it.
        varManager = createMIVariableManager();

        // Create the meta command cache which will use the variable manager
        // to actually send MI commands to the back-end
        fExpressionCache = new CommandCache(getSession(), varManager);
        ICommandControlService commandControl = getServicesTracker().getService(ICommandControlService.class);
        fExpressionCache.setContextAvailable(commandControl.getContext(), true);
        
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		requestMonitor.done();
	}

	/**
	 * Creates the MI variable manager to be used by this expression service.
	 * Overriding classes may override to provide a custom services tracker. 
	 * 
	 * @since 3.0
	 */
	protected MIVariableManager createMIVariableManager() {
	    return new MIVariableManager(getSession(), getServicesTracker());
	}
	
	/**
	 * This method shuts down this service. It unregisters the service, stops
	 * receiving service events, and calls the superclass shutdown() method to
	 * finish the shutdown process.
	 */
	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		varManager.dispose();
		getSession().removeServiceEventListener(this);
		super.shutdown(requestMonitor);
	}
	
	/**
	 * @return The bundle context of the plug-in to which this service belongs.
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}
	
	/**
	 * Create an expression context with the same full and relative expression
	 */
	@Override
	public IExpressionDMContext createExpression(IDMContext ctx, String expression) {
		return createExpression(ctx, expression, expression);
	}

	/**
	 * Create an expression context.
	 */
	public IExpressionDMContext createExpression(IDMContext ctx, String expression, String relExpr) {
		return createExpression(ctx, new ExpressionInfo(expression, relExpr));
	}
	
	/**
	 * Create an expression context from a given expression info.
	 * @since 4.0
	 */
	private IExpressionDMContext createExpression(IDMContext ctx, ExpressionInfo info) {
		String expression = info.getFullExpr();
	    IFrameDMContext frameDmc = DMContexts.getAncestorOfType(ctx, IFrameDMContext.class);
	    if (frameDmc != null) {
	        return new MIExpressionDMC(getSession().getId(), info, frameDmc);
	    } 
	    
	    IMIExecutionDMContext execCtx = DMContexts.getAncestorOfType(ctx, IMIExecutionDMContext.class);
	    if (execCtx != null) {
	    	// If we have a thread context but not a frame context, we give the user
	    	// the expression as per the top-most frame of the specified thread.
	    	// To do this, we create our own frame context.
	    	MIStack stackService = getServicesTracker().getService(MIStack.class);
	    	if (stackService != null) {
	    		frameDmc = stackService.createFrameDMContext(execCtx, 0);
	            return new MIExpressionDMC(getSession().getId(), info, frameDmc);
	    	}

            return new InvalidContextExpressionDMC(getSession().getId(), expression, execCtx);
        } 
	    
        IMemoryDMContext memoryCtx = DMContexts.getAncestorOfType(ctx, IMemoryDMContext.class);
        if (memoryCtx != null) {
            return new MIExpressionDMC(getSession().getId(), info, memoryCtx);
        } 
        
        // Don't care about the relative expression at this point
        return new InvalidContextExpressionDMC(getSession().getId(), expression, ctx);
	}

	/**
	 * @see IFormattedValues.getFormattedValueContext(IFormattedDataDMContext, String)
	 * 
	 * @param dmc
	 *            The context describing the data for which we want to create
	 *            a Formatted context.
	 * @param formatId
	 *            The format that will be used to create the Formatted context
	 *            
	 * @return A FormattedValueDMContext that can be used to obtain the value
	 *         of an expression in a specific format. 
	 */

	@Override
	public FormattedValueDMContext getFormattedValueContext(
			IFormattedDataDMContext dmc, String formatId) {
		return new FormattedValueDMContext(this, dmc, formatId);
	}

	/**
	 * @see IFormattedValues.getAvailableFormats(IFormattedDataDMContext, DataRequestMonitor)
	 * 
	 * @param dmc
	 *            The context describing the data for which we want to know
	 *            which formats are available.
	 * @param rm
	 *            The data request monitor for this asynchronous operation. 
	 *  
	 */

	@Override
	public void getAvailableFormats(IFormattedDataDMContext dmc,
			final DataRequestMonitor<String[]> rm) {
		rm.setData(FORMATS_SUPPORTED);
		rm.done();
	}

	/**
	 * Obtains the static data of an expression represented 
	 * by an ExpressionDMC object (<tt>dmc</tt>).
	 * 
	 * @param dmc
	 *            The ExpressionDMC for the expression to be evaluated.
	 * @param rm
	 *            The data request monitor that will contain the requested data
	 */
	@Override
	public void getExpressionData(
			final IExpressionDMContext dmc,
			final DataRequestMonitor<IExpressionDMData> rm) 
	{
	    if (dmc instanceof MIExpressionDMC) {
            fExpressionCache.execute(
                new ExprMetaGetVar(dmc), 
                new DataRequestMonitor<ExprMetaGetVarInfo>(getExecutor(), rm) {
                	
					@Override
                    protected void handleSuccess() {
                        IExpressionDMData.BasicType basicType = null;

                        GDBType gdbType = getData().getGDBType();
                        
                        if (gdbType != null) {
                            switch (gdbType.getType()) {
                            case GDBType.ARRAY:
                                basicType = IExpressionDMData.BasicType.array;
                                break;
                            case GDBType.FUNCTION:
                                basicType = IExpressionDMData.BasicType.function;
                                break;
                            case GDBType.POINTER:
                            case GDBType.REFERENCE:
                                basicType =  IExpressionDMData.BasicType.pointer;
                                break;
                            case GDBType.GENERIC:
                            default:
                            	// The interesting question is not hasChildren,
                            	// but canHaveChildren. E.g. an empty
                            	// collection still is a composite.
                            	if (getData().hasChildren() || getData().getCollectionHint()) {
                                    basicType =  IExpressionDMData.BasicType.composite;
                                } else {
                                    basicType =  IExpressionDMData.BasicType.basic;
                                }
                                break;
                            }
                        }
                        
                        rm.setData(new ExpressionDMData(
                            getData().getExpr(),getData().getType(), getData().getNumChildren(), 
                            getData().getEditable(), basicType));
                        rm.done();
                    }
                });	        
	    } else if (dmc instanceof InvalidContextExpressionDMC) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context for evaluating expressions.", null)); //$NON-NLS-1$
            rm.done();
	    } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid expression context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}
	
	/**
	 * Obtains the address of an expression and the size of its type. 
	 * 
	 * @param dmc
	 *            The ExpressionDMC for the expression.
	 * @param rm
	 *            The data request monitor that will contain the requested data
	 */
	@Override
    public void getExpressionAddressData(
    		IExpressionDMContext dmc, 
    		final DataRequestMonitor<IExpressionDMAddress> rm) {
        
    	if (dmc instanceof MIExpressionDMC) {
    		MIExpressionDMC miDMC = (MIExpressionDMC) dmc;
    		if (miDMC.getExpressionInfo().hasDynamicAncestor() ||
    			!miDMC.getExpressionInfo().inMemory()) {
    			// For children of dynamic varobjs, there is no full expression that gdb
    			// could evaluate in order to provide address and size.
    			// Also, if an expression is not in memory, such as a register
    			// or a GDB convenience variable, there is no address to return
				rm.setData(new InvalidDMAddress());
				rm.done();
				return;
    		}
    	}

    	// First create an address expression and a size expression
    	// to be used in back-end calls
    	final IExpressionDMContext addressDmc = 
    	    createExpression( dmc, "&(" + dmc.getExpression() + ")" );//$NON-NLS-1$//$NON-NLS-2$
    	final IExpressionDMContext sizeDmc = 
    	    createExpression( dmc, "sizeof(" + dmc.getExpression() + ")" ); //$NON-NLS-1$//$NON-NLS-2$

    	if (addressDmc instanceof InvalidContextExpressionDMC || sizeDmc instanceof InvalidContextExpressionDMC) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context for evaluating expressions.", null)); //$NON-NLS-1$
            rm.done();
    	} else {
        	fExpressionCache.execute(
        		fCommandFactory.createMIDataEvaluateExpression(addressDmc), 
    			new DataRequestMonitor<MIDataEvaluateExpressionInfo>(getExecutor(), rm) {
    				@Override
    				protected void handleSuccess() {
    					String tmpAddrStr = getData().getValue();
    					
    					// Deal with addresses of contents of a char* which is in
    					// the form of "0x12345678 \"This is a string\""
    					int split = tmpAddrStr.indexOf(' '); 
    			    	if (split != -1) tmpAddrStr = tmpAddrStr.substring(0, split);
    			    	final String addrStr = tmpAddrStr;
    			    	
    					fExpressionCache.execute(
    						fCommandFactory.createMIDataEvaluateExpression(sizeDmc), 
    						new DataRequestMonitor<MIDataEvaluateExpressionInfo>(getExecutor(), rm) {
    							@Override
    							protected void handleSuccess() {
    								try {
    									int size = Integer.parseInt(getData().getValue());
    									rm.setData(new ExpressionDMAddress(addrStr, size));
    								} catch (NumberFormatException e) {
    						            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
    						            		"Unexpected size format from backend: " + getData().getValue(), null)); //$NON-NLS-1$
    								}
    								rm.done();
    							}
    						});
    				}
    			});
    	}
	}

	/**
	 * Obtains the value of an expression in a specific format.
	 * 
	 * @param dmc
	 *            The context for the format of the value requested and 
	 *            for the expression to be evaluated.  The expression context
	 *            should be a parent of the FormattedValueDMContext.
	 * @param rm
	 *            The data request monitor that will contain the requested data
	 */
	@Override
	public void getFormattedExpressionValue(
			final FormattedValueDMContext dmc,
			final DataRequestMonitor<FormattedValueDMData> rm) 
	{
		// We need to make sure the FormattedValueDMContext also holds an ExpressionContext,
		// or else this method cannot do its work.
		// Note that we look for MIExpressionDMC and not IExpressionDMC, because
		// looking for IExpressionDMC could yield InvalidContextExpressionDMC which is still
		// not what we need.
		MIExpressionDMC exprDmc = DMContexts.getAncestorOfType(dmc, MIExpressionDMC.class);
        if (exprDmc == null ) {
        	rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context for evaluating expressions.", null)); //$NON-NLS-1$
        	rm.done();
        } else {
        	if (DETAILS_FORMAT.equals(dmc.getFormatID())) {
        		if (exprDmc.getExpressionInfo().hasDynamicAncestor()) {
        			// -data-evaluate-expression does not work for children of
        			// dynamic varobjs, since there is no full expression
        			// that gdb could evaluate.
					rm.setData(new FormattedValueDMData(Messages.MIExpressions_NotAvailableBecauseChildOfDynamicVarobj));
					rm.done();
        		} else {
        			// This format is obtained through a different GDB command.
        			// It yields more details than the variableObject output.
        			// Starting with GDB 7.0, this format automatically supports pretty-printing, as long as
        			// GDB has been configured to support it.
        			fExpressionCache.execute(
        					fCommandFactory.createMIDataEvaluateExpression(exprDmc), 
        					new DataRequestMonitor<MIDataEvaluateExpressionInfo>(getExecutor(), rm) {
        						@Override
        						protected void handleSuccess() {
        							rm.setData(new FormattedValueDMData(getData().getValue()));
        							rm.done();
        						}
        						@Override
        						protected void handleError() {
        							if (fTraceVisualization) {
        								rm.setData(new FormattedValueDMData("")); //$NON-NLS-1$
        								rm.done();
        							} else {
        								super.handleError();
        							}
        						}
        					});
        		}
        	} else {
        		fExpressionCache.execute(
        				new ExprMetaGetValue(dmc),
        				new DataRequestMonitor<ExprMetaGetValueInfo>(getExecutor(), rm) {
        					@Override
        					protected void handleSuccess() {
        						rm.setData(new FormattedValueDMData(getData().getValue()));
        						rm.done();
        					}
        				});
        	}
        }
	}

	/* Not implemented
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IExpressions#getBaseExpressions(org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	public void getBaseExpressions(IExpressionDMContext exprContext,
			DataRequestMonitor<IExpressionDMContext[]> rm) {
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	/**
	 * Retrieves the children expressions of the specified expression
	 * 
	 * @param dmc
	 *            The context for the expression for which the children
	 *            should be retrieved.
	 * @param rm
	 *            The data request monitor that will contain the requested data
	 */
	@Override
	public void getSubExpressions(IExpressionDMContext dmc, DataRequestMonitor<IExpressionDMContext[]> rm) {
		getSubExpressions(dmc, -1, -1, rm);
	}

	/**
	 * Retrieves a range of children expressions of the specified expression
	 * 
	 * @param exprCtx
	 *            The context for the expression for which the children
	 *            should be retrieved.
	 * @param startIndex
	 *            The starting index within the list of all children of the parent
	 *            expression.  Must be a positive integer.  
	 * @param length
	 *            The length or number of elements of the range requested.  
	 *            Must be a positive integer.
	 * @param rm
	 *            The data request monitor that will contain the requested data
	 */
	@Override
	public void getSubExpressions(final IExpressionDMContext exprCtx, final int startIndex,
			final int length, final DataRequestMonitor<IExpressionDMContext[]> rm) {

		if (exprCtx instanceof IndexedPartitionDMC) {
			getIndexedPartitionChildren((IndexedPartitionDMC)exprCtx, startIndex, length, rm);
		}
		else if (exprCtx instanceof MIExpressionDMC) {
			getRealSubExpressionCount(
				exprCtx, 
				IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED, 
				new DataRequestMonitor<Integer>(getExecutor(), rm) {
					/* (non-Javadoc)
					 * @see org.eclipse.cdt.dsf.concurrent.RequestMonitor#handleSuccess()
					 */
					@Override
					protected void handleSuccess() {
						final int realNumChildren = getData().intValue();
						if (realNumChildren == 0) {
							rm.setData(new IExpressionDMContext[0]);
							rm.done();
							return;
						}

						if (realNumChildren <= getArrayPartitionLength()) {
							getRealSubExpressions(exprCtx, startIndex, length, rm);
						}
						else {
							getExpressionData(
									exprCtx, 
									new DataRequestMonitor<IExpressionDMData>(ImmediateExecutor.getInstance(), rm) {

										@Override
										protected void handleSuccess() {
											if (IExpressionDMData.BasicType.array.equals(getData().getBasicType())) {
												rm.setData(getTopLevelIndexedPartitions((MIExpressionDMC)exprCtx, realNumChildren, startIndex, length ));
												rm.done();
											}
											else {
												getRealSubExpressions(exprCtx, startIndex, length, rm);
											}
										}
									});
						}
					}
				});
		} else if (exprCtx instanceof InvalidContextExpressionDMC) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context for evaluating expressions.", null)); //$NON-NLS-1$
			rm.done();
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid expression context.", null)); //$NON-NLS-1$
			rm.done();
		}		
	}
	
	/**
	 * @since 4.0
	 */
	@Override
	public void safeToAskForAllSubExpressions(IExpressionDMContext dmc,
			final DataRequestMonitor<Boolean> rm) {
	    if (dmc instanceof MIExpressionDMC) {
            fExpressionCache.execute(
                new ExprMetaGetVar(dmc), 
                new DataRequestMonitor<ExprMetaGetVarInfo>(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {							
							boolean safe = getData().isSafeToAskForAllChildren();
							
							rm.setData(safe);
							rm.done();
                    }
                });	        
	    } else if (dmc instanceof InvalidContextExpressionDMC) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context for evaluating expressions.", null)); //$NON-NLS-1$
            rm.done();
	    } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid expression context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}

	/**
	 * @since 4.0
	 */
	@Override
	public void getSubExpressionCount(final IExpressionDMContext dmc,
			final int numChildLimit, final DataRequestMonitor<Integer> rm) {

		if (dmc instanceof MIExpressionDMC) {
			if (dmc instanceof IndexedPartitionDMC) {
				int length = ((IndexedPartitionDMC)dmc).getLength();
				rm.setData(computeNumberOfChildren(length));
				rm.done();
			}
			else {
				getRealSubExpressionCount( 
					dmc, 
					numChildLimit, 
					new DataRequestMonitor<Integer>(getExecutor(), rm) {

						@Override
						protected void handleSuccess() {
							final int realNum = getData().intValue();
							if (realNum <= getArrayPartitionLength()) {
								rm.setData(Integer.valueOf(realNum));
								rm.done();
							}
							else {
								getExpressionData(
									dmc, 
									new DataRequestMonitor<IExpressionDMData>(ImmediateExecutor.getInstance(), rm) {

										@Override
										protected void handleSuccess() {
											if (IExpressionDMData.BasicType.array.equals(getData().getBasicType())) {
												rm.setData(computeNumberOfChildren(realNum));
											}
											else {
												rm.setData(Integer.valueOf(realNum));
											}
											rm.done();
										}
									});
							}
						}				
					});
			}
		} else if (dmc instanceof InvalidContextExpressionDMC) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context for evaluating expressions.", null)); //$NON-NLS-1$
			rm.done();
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid expression context.", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	/**
	 * Retrieves the count of children expressions of the specified expression
	 * 
	 * @param dmc
	 *            The context for the expression for which the children count
	 *            should be retrieved.
	 * @param rm
	 *            The data request monitor that will contain the requested data
	 */
	@Override
	public void getSubExpressionCount(IExpressionDMContext dmc,
			final DataRequestMonitor<Integer> rm) 
	{
		getSubExpressionCount(dmc, IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED, rm);
	}
	
    /**
     * This method indicates if an expression can be written to.
     * 
     * @param dmc The data model context representing an expression.
     *
     * @param rm Data Request monitor containing True if this expression's value can be edited.  False otherwise.
     */

	@Override
	public void canWriteExpression(IExpressionDMContext dmc, final DataRequestMonitor<Boolean> rm) {
        if (dmc instanceof MIExpressionDMC) {
            fExpressionCache.execute(
                    new ExprMetaGetAttributes(dmc),
                    new DataRequestMonitor<ExprMetaGetAttributesInfo>(getExecutor(), rm) {
                        @Override
                        protected void handleSuccess() {
                            rm.setData(getData().getEditable());
                            rm.done();
                        }
                    });
        } else if (dmc instanceof InvalidContextExpressionDMC) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context for evaluating expressions.", null)); //$NON-NLS-1$
            rm.done();
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid expression context.", null)); //$NON-NLS-1$
            rm.done();
        }
	}


	/**
	 * Changes the value of the specified expression based on the new value and format.
	 * 
	 * @param dmc
	 *            The context for the expression for which the value 
	 *            should be changed.
	 * @param expressionValue
	 *            The new value for the specified expression
	 * @param formatId
	 *            The format in which the value is specified
	 * @param rm
	 *            The request monitor that will indicate the completion of the operation
	 */
	@Override
	public void writeExpression(final IExpressionDMContext dmc, String expressionValue, 
			String formatId, final RequestMonitor rm) {

		if (dmc instanceof MIExpressionDMC) {
			// This command must not be cached, since it changes the state of the back-end.
			// We must send it directly to the variable manager
			varManager.writeValue(
			        dmc, 
					expressionValue, 
					formatId, 
					new RequestMonitor(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							// A value has changed, we should remove any references to that
							// value in our cache.  Since we don't have such granularity,
							// we must clear the entire cache.
							// We cannot use the context to do a more-specific reset, because
							// the same global variable can be set with different contexts
							fExpressionCache.reset();
							
							// Issue event that the expression has changed
							getSession().dispatchEvent(new ExpressionChangedEvent(dmc), getProperties());
							
							rm.done();
						}
					});
		} else if (dmc instanceof InvalidContextExpressionDMC) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context for evaluating expressions.", null)); //$NON-NLS-1$
			rm.done();
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid expression context.", null)); //$NON-NLS-1$
			rm.done();
		}
	}

    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.IResumedDMEvent e) {
        fExpressionCache.setContextAvailable(e.getDMContext(), false);
        if (e.getReason() != StateChangeReason.STEP) {
            fExpressionCache.reset();
        }
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.ISuspendedDMEvent e) {
        fExpressionCache.setContextAvailable(e.getDMContext(), true);
        fExpressionCache.reset();
    }

    @DsfServiceEventHandler 
    public void eventDispatched(IMemoryChangedEvent e) {
        fExpressionCache.reset();
        // MIVariableManager separately traps this event
    }

    /** @since 3.0 */
    @DsfServiceEventHandler
    public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
    	if (e.isVisualizationModeEnabled()) {
    		fTraceVisualization = true;
    	} else {
    		fTraceVisualization = false;
    	}
    }
    
    /**
     * {@inheritDoc}
     * @since 1.1
     */
	@Override
    public void flushCache(IDMContext context) {
        fExpressionCache.reset(context);
        // We must also mark all variable objects as out-of-date
        // to refresh them as well
        varManager.markAllOutOfDate();
    }
	
	/** 
	 * A casted or array-displayed expression. 
	 * @since 3.0 
	 */
	protected class CastedExpressionDMC extends MIExpressionDMC implements ICastedExpressionDMContext {

		private final CastInfo fCastInfo;

		public CastedExpressionDMC(MIExpressionDMC exprDMC, String castExpression, CastInfo castInfo) {
			super(getSession().getId(), castExpression, exprDMC.getRelativeExpression(), exprDMC);
			fCastInfo = castInfo;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IExpressions2.ICastedExpressionDMContext#getCastInfo()
		 */
		@Override
		public CastInfo getCastInfo() {
			return fCastInfo;
		}
		        
        /**
         * @return True if the two objects are equal, false otherwise.
         */
        @Override
		public boolean equals(Object other) {
			return super.equals(other)
					&& fCastInfo.equals(((CastedExpressionDMC) other).fCastInfo);
        }
	}
	
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IExpressions2#createCastedExpression(org.eclipse.cdt.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.cdt.dsf.debug.service.IExpressions2.ICastedExpressionDMContext)
	 */
	/** @since 3.0 */
	@Override
	public ICastedExpressionDMContext createCastedExpression(IExpressionDMContext exprDMC, CastInfo castInfo) {
		if (exprDMC instanceof MIExpressionDMC && castInfo != null) {
			String castType = castInfo.getTypeString();
			String castExpression = exprDMC.getExpression();
			int castingLength = castInfo.getArrayCount(); 
			int castingIndex = castInfo.getArrayStartIndex();
		 
			// cast to type 
			if (castType != null && castType.length() > 0) {
				StringBuffer buffer = new StringBuffer();
				buffer.append('(').append(castType).append(')');
				buffer.append('(').append(castExpression).append(')');
				castExpression = buffer.toString();
			}	
			
			// cast to array (can be in addition to cast to type) 
			if (castingLength > 0) {
				StringBuffer buffer = new StringBuffer();
				buffer.append("*("); //$NON-NLS-1$
				buffer.append('(').append(castExpression).append(')');
				buffer.append('+').append(castingIndex).append(')');
				buffer.append('@').append(castingLength);
				castExpression = buffer.toString();
			}
			
			// Surround the entire casted expression with parenthesis in case we are
			// dealing with an array.  Arrays must be parenthesized before they are
			// subscripted.  Note that we can be casting to an array or displaying
			// as an array, so we must do this all the time.
			castExpression = String.format("(%s)", castExpression); //$NON-NLS-1$
					
			return new CastedExpressionDMC((MIExpressionDMC) exprDMC, castExpression, castInfo);
		} else {
			assert false;
			return null;
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IExpressions3#getExpressionDataExtension(org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    /** @since 4.0 */
	@Override
    public void getExpressionDataExtension(IExpressionDMContext dmc, final DataRequestMonitor<IExpressionDMDataExtension> rm) {
        getExpressionData(dmc, new DataRequestMonitor<IExpressionDMData>(getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
                rm.setData((IExpressionDMDataExtension)getData());
                super.handleSuccess();
            }
        }); 
    }

	private IndexedPartitionDMC[] getTopLevelIndexedPartitions(
			MIExpressionDMC exprCtx, 
			int realNumChildren, 
			int startIndex, 
			int length) {

		int numChildren = computeNumberOfChildren(realNumChildren);
		if (startIndex >= numChildren)
			return new IndexedPartitionDMC[0];
		int startIndex1 = (startIndex < 0) ? 0 : startIndex;
		int length1 = (length < 0) ? numChildren - startIndex1 : Math.min(length, numChildren - startIndex1);

		IndexedPartitionDMC[] children = new IndexedPartitionDMC[numChildren];
		int index = 0;
		// If the parent array is a casted expression it could have a different
		// start index.  We want the partition to start at the right index, not always 0
//		if (exprCtx instanceof ICastedExpressionDMContext) {
//			index = ((ICastedExpressionDMContext)exprCtx).getCastInfo().getArrayStartIndex();
//		}
		for(int i = 0; i < children.length; ++i) {
			int partLength = computePartitionLength(realNumChildren, i);
			children[i] = createIndexedPartition(
				exprCtx, 
				index, 
				partLength);
			index += partLength;
		}
		return Arrays.copyOfRange(children, startIndex1, startIndex1 + length1 );
	}

	private void getIndexedPartitionChildren(
			final IndexedPartitionDMC partDmc, 
			final int startIndex,
			final int length, 
			final DataRequestMonitor<IExpressionDMContext[]> rm) {
		
		final int startIndex1 = (startIndex < 0) ? 0 : startIndex;
		final int length1 = (length < 0) ? Integer.MAX_VALUE : length;

		final int partStartIndex = partDmc.getIndex();
		final int partLength = partDmc.getLength();
		if (partLength > getArrayPartitionLength()) {
			// create subpartitions
			int numChildren = computeNumberOfChildren(partLength);
			
			if (startIndex1 >= numChildren) {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, "Invalid range for evaluating sub expressions.", null)); //$NON-NLS-1$
				rm.done();
				return;
			}

			int numPart = Math.min(numChildren, length1);
			IndexedPartitionDMC[] children = new IndexedPartitionDMC[numPart];
			int index = partStartIndex;
			for (int i = 0; i < startIndex1; ++i)
				index += computePartitionLength(partLength, i);
			for (int i = 0; i < children.length; ++i) {
				int childPartLength = computePartitionLength(partLength, i + startIndex1);
				children[i] = createIndexedPartition(
					partDmc.getParentExpressionContext(), 
					index, 
					childPartLength);
				index += childPartLength;
			}
			rm.setData(children);
			rm.done();
		}
		else {
			// this is the last partition level, create "real" children
			if (startIndex1 > partLength) {
				rm.setData(new IExpressionDMContext[0]);
				rm.done();
			}
			else {
				getRealSubExpressions(
						partDmc.getParentExpressionContext(), 
						partStartIndex + startIndex1, 
						Math.min(length1, partLength - startIndex1), 
						rm);
			}
		}
	}

	void getRealSubExpressions(
			final IExpressionDMContext exprCtx, 
			int startIndex,
			int length, 
			final DataRequestMonitor<IExpressionDMContext[]> rm) {
		
		ExprMetaGetChildren getChildren = (startIndex < 0 || length < 0) ? 
				new ExprMetaGetChildren(exprCtx) : new ExprMetaGetChildren(exprCtx, startIndex + length);
		final int startIndex1 = (startIndex < 0) ? 0 : startIndex;
		final int length1 = (length < 0) ? Integer.MAX_VALUE : length;
		fExpressionCache.execute(
				getChildren,				
				new DataRequestMonitor<ExprMetaGetChildrenInfo>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						ExpressionInfo[] childrenExpr = getData().getChildrenExpressions();

						if (startIndex1 >= childrenExpr.length) {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, "Invalid range for evaluating sub expressions.", null)); //$NON-NLS-1$
							rm.done();
							return;
						}

						int numChildren = childrenExpr.length - startIndex1;
						numChildren = Math.min(length1, numChildren);
						IExpressionDMContext[] childrenArray = new IExpressionDMContext[numChildren];
						for (int i=0; i < numChildren; i++) {
							childrenArray[i] = createExpression(exprCtx.getParents()[0], childrenExpr[startIndex1 + i]);
						}
						rm.setData(childrenArray);
						rm.done();
					}
				});
	}

	/**
	 * Returns the number of "real" children if it is less or equal to the partition size, 
	 * otherwise returns the number of partitions. 
	 */
	private int computeNumberOfChildren(int realNumberOfChildren) {
		int childNum = realNumberOfChildren;
		int partLength = getArrayPartitionLength();
		while (childNum > partLength) {
			childNum /= partLength;
		}
		if (childNum*partLength < realNumberOfChildren)
			++childNum;
		return childNum;
	}

	private int computePartitionLength(int realNumberOfChildren, int index) {
		int childNum = realNumberOfChildren;
		int depth = 0;
		int partLength = getArrayPartitionLength();
		int length = partLength;
		while (childNum > partLength) {
			childNum /= partLength;
			if (depth > 0)
				length *= partLength;
			++depth;
		}
		int diff = realNumberOfChildren - length*index;
		return ( diff > length ) ? length : diff ;
	}

	private IndexedPartitionDMC createIndexedPartition(MIExpressionDMC parentExpr, int index, int length) {
		return new IndexedPartitionDMC(parentExpr, index, length);
	}

	private void getRealSubExpressionCount(IExpressionDMContext dmc, int numChildLimit, final DataRequestMonitor<Integer> rm) {
		if (dmc instanceof MIExpressionDMC) {
			fExpressionCache.execute(
					new ExprMetaGetChildCount(dmc, numChildLimit),				
					new DataRequestMonitor<ExprMetaGetChildCountInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(getData().getChildNum());
							rm.done();
						}
					});
		} else if (dmc instanceof InvalidContextExpressionDMC) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context for evaluating expressions.", null)); //$NON-NLS-1$
			rm.done();
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid expression context.", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	private int getArrayPartitionLength() {
		// Replace this in case we or the platform decide to add a user preference.
		// See org.eclipse.debug.internal.ui.model.elements.VariableContentProvider.
		return PARTITION_LENGTH;
	}
}
