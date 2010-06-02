/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.dm;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



public class DMContextsTest {

    TestDsfExecutor fExecutor;
    DsfSession fSession;
    
    @Before public void startExecutor() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
        fExecutor.submit(new DsfRunnable() { public void run() {
            fSession = DsfSession.startSession(fExecutor, "DMContextsTest"); //$NON-NLS-1$
        }}).get();

        // Build a hierarchy of contexts to run the tests.  Note that this hierarchy
        // is not valid in the DSF model, but that is ok for these tests.
        // Let's build the following:
        //
        //     SecondType4
        //        |
        //     FirstType3  SecondType7
        //        |             |         
        //     ThirdType2   ThirdType6
        //        |       /     |
        //     FirstType1   ThirdType5
        //        |       /
        //     FirstType0 
         c[7] = new SecondType(new IDMContext[0], 7);
         c[6] = new ThirdType(new IDMContext[]{c[7]}, 6);
         c[5] = new ThirdType(new IDMContext[]{c[6]}, 5);
         c[4] = new SecondType(new IDMContext[0], 4);
         c[3] = new FirstType(new IDMContext[]{c[4]}, 3);
         c[2] = new ThirdType(new IDMContext[]{c[3]}, 2);
         c[1] = new FirstType(new IDMContext[]{c[2],c[6]}, 1);      
         c[0] = new FirstType(new IDMContext[]{c[1],c[5]}, 0);
    }   
    
    @After public void shutdownExecutor() throws ExecutionException, InterruptedException {
        DsfSession.endSession(fSession);
        fSession = null;
        
        fExecutor.submit(new DsfRunnable() { public void run() {
            fExecutor.shutdown();
        }}).get();
        if (fExecutor.exceptionsCaught()) {
            Throwable[] exceptions = fExecutor.getExceptions();
            throw new ExecutionException(exceptions[0]);
        }
        fExecutor = null;
    }
    
    
	BaseContextType c[] = new BaseContextType[8];


	private class BaseContextType extends AbstractDMContext {
		final int fId;

        public BaseContextType(IDMContext[] parents, int id) {
            super(fSession.getId(), parents);
            fId = id;
        }
        
        @Override
        public String toString() { return baseToString() + ".[" + fId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$
    
        @Override
        public boolean equals(Object obj) {
            return super.baseEquals(obj) && ((BaseContextType)obj).fId == fId;
        }
        
        @Override
        public int hashCode() { return super.baseHashCode() ^ fId; }
	}

	private class FirstType extends BaseContextType {
        public FirstType(IDMContext[] parents, int id) {
            super(parents, id);
        }
	}

	private class SecondType extends BaseContextType {
        public SecondType(IDMContext[] parents, int id) {
            super(parents, id);
        }
	}
	
	private class ThirdType extends BaseContextType {
        public ThirdType(IDMContext[] parents, int id) {
            super(parents, id);
        }
	}
	
	private interface UnknownType extends IDMContext {}
    	
	/**
	 * Test that we get the closest ancestor in terms of depth.
	 */
	@Test
	public void testClosestAncestor() throws Throwable {
		BaseContextType ancestor = DMContexts.getAncestorOfType(c[0], FirstType.class);
		assertTrue("Got unexpected null ancestor", ancestor != null);
		assertTrue("Got ancestor " + ancestor.fId + " intead of 1", ancestor.fId == 0);

		ancestor = DMContexts.getAncestorOfType(c[0], SecondType.class);
		assertTrue("Got unexpected null ancestor", ancestor != null);
		assertTrue("Got ancestor " + ancestor.fId + " intead of 8", ancestor.fId == 7);

		ancestor = DMContexts.getAncestorOfType(c[0], ThirdType.class);
		assertTrue("Got unexpected null ancestor", ancestor != null);
		assertTrue("Got ancestor " + ancestor.fId + " intead of 6", ancestor.fId == 5);

		ancestor = DMContexts.getAncestorOfType(c[1], SecondType.class);
		assertTrue("Got unexpected null ancestor", ancestor != null);
		assertTrue("Got ancestor " + ancestor.fId + " intead of 8", ancestor.fId == 7);

		ancestor = DMContexts.getAncestorOfType(c[1], ThirdType.class);
		assertTrue("Got unexpected null ancestor", ancestor != null);
		assertTrue("Got ancestor " + ancestor.fId + " intead of 3", ancestor.fId == 2);

		ancestor = DMContexts.getAncestorOfType(c[5], FirstType.class);
		assertTrue("Got unexpected non-null ancestor", ancestor == null);
	}

	/**
	 * Test that we get all the ancestors in order of closest in terms of depth.
	 */
	@Test
	public void testAllClosestAncestors() throws Throwable {

		checkAncestors(c[0], BaseContextType.class, new int[]{0,1,5,2,6,3,7,4});
		checkAncestors(c[0], FirstType.class, new int[]{0,1,3});
		checkAncestors(c[0], SecondType.class, new int[]{7,4});
		checkAncestors(c[0], ThirdType.class, new int[]{5,2,6});

		UnknownType[] exprAncestors = DMContexts.getAllAncestorsOfType(c[0], UnknownType.class);
		assertTrue("Got unexpected non-null ancestor list", exprAncestors == null);
	}
	
	private <V extends IDMContext> void checkAncestors(BaseContextType ctx, Class<V> type, int[] expected) {
		BaseContextType[] ancestors = (BaseContextType[])DMContexts.getAllAncestorsOfType(ctx, type);
		assertTrue("Got unexpected null ancestor", ancestors != null);

		String ancestorsStr = "", expectedStr = "";
		for (int k=0;k<ancestors.length;k++) {
			ancestorsStr += ancestors[k].fId + ",";
		}
		for (int j=0;j<expected.length;j++) {
			expectedStr += expected[j] + ",";
		}

		assertTrue("Got " + ancestorsStr + " instead of " + expectedStr, ancestors.length == expected.length);
		for (int i=0;i<expected.length;i++) {
			if (ancestors[i].fId != expected[i]) {
				assertTrue("Got " + ancestorsStr + " instead of " + expectedStr, false);
			}
		}
	}

}
