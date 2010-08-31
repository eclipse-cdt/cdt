/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A type of {@link Sequence} which uses reflection and annotations to
 * declare its different {@link Sequence.Step}.  It can be used to make
 * larger DSF sequences more readable and easier to override.
 * 
 * The order of execution of the {@code @Execute} methods is determined by
 * the {@link #getExecutionOrder()} method. 
 * 
 * {@code @Execute} methods can be grouped in a hierarchical set of groups,
 * which should be included in the result of {@link #getExecutionOrder()}.
 * Using groups can make overriding slightly simpler.
 * 
 * A usage example follows: <code><pre>
 *    public class MyReflectionSequence extends ReflectionSequence {
 *
 *        public MyReflectionSequence(DsfExecutor executor) {
 *            super(executor);
 *        }
 *        
 *        protected static final String GROUP_INIT = "GROUP_INIT";
 *   
 *        {@code @Override}
 *        protected String[] getExecutionOrder(String group) {
 *           if (GROUP_TOP_LEVEL.equals(group)) {
 *               // This is the top level group which contains
 *               // all sub-groups, or steps that are not in
 *               // other groups.
 *               return new String[] { GROUP_INIT, "step3", "step4" };
 *           }
 *           
 *           // Now deal with the content of sub-groups
 *           if (GROUP_INIT.equals(group)) {
 *               return new String[] { "step1", "step2" };
 *           }
 *           
 *           // An invalid group was requested
 *           return null;
 *        }
 *
 *        {@code @Execute}
 *        public void step1(RequestMonitor rm) {
 *            // Do something
 *            rm.done(); 
 *        }
 *
 *        {@code @RollBack("step1")}
 *        public void rollBack1(RequestMonitor rm) {
 *        	// Rollback what was done in step1()
 *        	rm.done(); 
 *        }
 *
 *        {@code @Execute}
 *        public void step2(RequestMonitor rm) {
 *            // Do something else
 *            rm.done(); 
 *        }
 *        
 *        {@code @Execute}
 *        public void step3(RequestMonitor rm) {
 *            // Do something else
 *            rm.done(); 
 *        }
 *
 *        {@code @Execute}
 *        public void step4(RequestMonitor rm) {
 *            // Do something else
 *            rm.done(); 
 *        }
 *    }
 * </pre></code>
 * 
 * @since 2.2
 */
abstract public class ReflectionSequence extends Sequence {

	/**
	 * The top-level group in which all sub-groups or steps that
	 * are not part of any sub-groups are contained.  This group
	 * identifier is the one that will be used in the first call to
	 * {@link #getExecutionOrder()}.
	 */
    public static final String GROUP_TOP_LEVEL = "GROUP_TOP_LEVEL"; //$NON-NLS-1$
    
	private Step[] fReflectionSteps;
   
    /**
     * Annotation used to indicate that a method corresponds to an
     * {@link Sequence.Step#execute()} method of a {@link Sequence.Step}.
     * The annotated method must be declared public.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Execute {}
    
    /**
     * Annotation used to indicate that a method corresponds to a
     * {@link Sequence.Step#rollBack()} method of a {@link Sequence.Step}.
     * Declaring such a method is optional.  If declared, the annotated
     * method must be declared public.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface RollBack {
        /**
         * Name of the method tagged with the {@link Execute} annotation that this method rolls back.
         */
        String value();
    }

    private class ReflectionStep extends Step {
        final private Method fExecuteMethod;
        final private Method fRollbackMethod;
        
        private ReflectionStep(Method executeMethod, Method rollbackMethod) {
        	assert executeMethod != null;
        	
            fExecuteMethod = executeMethod;;
            fRollbackMethod = rollbackMethod;
        }
        
        @Override
        public void execute(RequestMonitor rm) {
            try {
                fExecuteMethod.invoke(ReflectionSequence.this, rm);
            } catch (Exception e) {
                rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Error executing step execute method: " + fExecuteMethod.getName(), e)); //$NON-NLS-1$
                rm.done();
            }
        }
        
        @Override
        public void rollBack(RequestMonitor rm) {
        	if (fRollbackMethod == null) {
        		super.rollBack(rm);
        	} else {
        		try {
        			fRollbackMethod.invoke(ReflectionSequence.this, rm);
        		} catch (Exception e) {
        			rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Error executing step rollback method: " + fRollbackMethod.getName(), e)); //$NON-NLS-1$
        			rm.done();
        		}
            }
        }
    }

    public ReflectionSequence(DsfExecutor executor) {
        super(executor);
    }
    
    public ReflectionSequence(DsfExecutor executor, RequestMonitor rm) {
        super(executor, rm); 
    }

    public ReflectionSequence(DsfExecutor executor, IProgressMonitor pm, String taskName, String rollbackTaskName) {
        super(executor, pm, taskName, rollbackTaskName);
    }
    
    public ReflectionSequence(DsfExecutor executor, RequestMonitorWithProgress rm, String taskName, String rollbackTaskName) {
        super(executor, rm, taskName, rollbackTaskName); 
    }    
    
    /**
     * This method must return the execution order of {@code @Execute} methods and/or groups.
     * 
     * @param groupName The name of a group for which the list of {@code @Execute} methods
     *                  or sub-groups should be returned in the order they should be executed.
     *                  If the concept of groups is not used, then this parameter can be ignored,
     *                  at the top-level ordering should be returned.
     *                  
     * @return An array containing the list of @Execute methods and groups in the order
     *         they should be executed, or null if the specified groupName is unknown.
     */
    abstract protected String[] getExecutionOrder(String groupName); 
    
    @Override
    public Step[] getSteps() {
        if (fReflectionSteps == null) {
        	Map<String, Method> executeMethods = getAnnotatedMethods(Execute.class);
        	Map<String, Method> rollBackMethods = getAnnotatedMethods(RollBack.class);
            List<Step> steps = getGroupSteps(GROUP_TOP_LEVEL, executeMethods, rollBackMethods);
            fReflectionSteps = steps.toArray(new ReflectionStep[steps.size()]);
        }
        return fReflectionSteps;
    }

    private List<Step> getGroupSteps(String groupId, Map<String, Method> executeMethods, Map<String, Method> rollBackMethods) {
        List<Step> steps = new ArrayList<Step>(executeMethods.size());
        
        String[] order = getExecutionOrder(groupId);
        if (order == null) {
            throw new RuntimeException("Unknown group in sequence: " + groupId); //$NON-NLS-1$
        }
        
        for (String name : order) {
            Method executeMethod = executeMethods.get(name);
            if (executeMethod == null) {
                // name is a group id
                steps.addAll(getGroupSteps(name, executeMethods, rollBackMethods));
            } else {
                steps.add(new ReflectionStep(executeMethod, rollBackMethods.get(executeMethod.getName())));
            }
        }
        return steps;
    }
        
    private Map<String, Method> getAnnotatedMethods(Class<? extends Annotation> annotationType) {
    	Map<String, Method> retVal = new HashMap<String, Method>();
        try {
            Method[] methods = getClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(annotationType)) {
                	Class<?>[] paramTypes = method.getParameterTypes();
                	if (paramTypes.length != 1) {    // must have one and only param, the RequestMonitor
                		throw new IllegalArgumentException("Method " + //$NON-NLS-1$
                				method.getDeclaringClass().getSimpleName() + "#" + method.getName() + //$NON-NLS-1$ 
                		" must have a single parameter"); //$NON-NLS-1$
                	} else {
                		if (annotationType.equals(Execute.class)) {
                			retVal.put(method.getName(), method);
                		} else {// @Rollback 
                			retVal.put(method.getAnnotation(RollBack.class).value(), method);
                		}
                	}
                }
            }
        } catch(SecurityException e) {
            throw new IllegalArgumentException("No permission to access ReflectionSequence method"); //$NON-NLS-1$
        }   
        return retVal;
    }
    

}
