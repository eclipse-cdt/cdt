/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.Flags;
import org.eclipse.cdt.internal.core.model.IConstants;

/**
 * @author Peter Graves
 *
 * This is a very simple set of sanity tests for the flags class to make sure
 * there are no very silly problems in the class. It also verifies that there 
 * is no overlap in the IConstants.
 */
public class FlagTests extends TestCase {

    int flags[];
    /**
     * Constructor for FlagTests.
     * @param name
     */
    public FlagTests(String name) {
        super(name);
    }
    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     * 
     * Example code test the packages in the project 
     *  "com.qnx.tools.ide.cdt.core"
     */
    protected void setUp() {
        flags=new int[15];
        flags[0]=IConstants.AccPublic;
        flags[1]=IConstants.AccPrivate;
        flags[2]=IConstants.AccProtected;
        flags[3]=IConstants.AccStatic;
        flags[4]=IConstants.AccExtern;
        flags[5]=IConstants.AccInline;
        flags[6]=IConstants.AccVolatile;
        flags[7]=IConstants.AccRegister;
        flags[8]=IConstants.AccExplicit;
        flags[9]=IConstants.AccExport;
        flags[10]=IConstants.AccAbstract;
        flags[11]=IConstants.AccMutable;
        flags[12]=IConstants.AccAuto;
        flags[13]=IConstants.AccVirtual;
        flags[14]=IConstants.AccTypename;

    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() {
       // release resources here and clean-up
    }
    
    public static TestSuite suite() {
        return new TestSuite(FlagTests.class);
    }
    
    public static void main (String[] args){
        junit.textui.TestRunner.run(suite());
    }

    public void testIsStatic()
    {
        int x;
        assertTrue("isStatic with a static", Flags.isStatic(IConstants.AccStatic));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccStatic) 
                assertTrue("isStatic with a non-static", !Flags.isStatic(flags[x]));
        }        
    }

    public void testIsAbstract()
    {
        int x;
        assertTrue("isAbstract with a abstract", Flags.isAbstract(IConstants.AccAbstract));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccAbstract) 
                assertTrue("isAbstract with a non-abstract", !Flags.isAbstract(flags[x]));
        }        
    }

    public void testIsExplicit()
    {
        int x;
        assertTrue("isExplicit with a explicit", Flags.isExplicit(IConstants.AccExplicit));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccExplicit) 
                assertTrue("isExplicit with a non-explicit", !Flags.isExplicit(flags[x]));
        }        
    }

    public void testIsExport()
    {
        int x;
        assertTrue("isExport with a Export", Flags.isExport(IConstants.AccExport));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccExport) 
                assertTrue("isExport with a non-Export", !Flags.isExport(flags[x]));
        }        
    }
    public void testIsExtern()
    {
        int x;
        assertTrue("isExtern with a Extern", Flags.isExtern(IConstants.AccExtern));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccExtern) 
                assertTrue("isExtern with a non-Extern", !Flags.isExtern(flags[x]));
        }        
    }

    public void testIsInline()
    {
        int x;
        assertTrue("isInline with a Inline", Flags.isInline(IConstants.AccInline));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccInline) 
                assertTrue("isInline with a non-Inline", !Flags.isInline(flags[x]));
        }        
    }

    public void testIsMutable()
    {
        int x;
        assertTrue("isMutable with a Mutable", Flags.isMutable(IConstants.AccMutable));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccMutable) 
                assertTrue("isMutable with a non-Mutable", !Flags.isMutable(flags[x]));
        }        
    }

    public void testIsPrivate()
    {
        int x;
        assertTrue("isPrivate with a Private", Flags.isPrivate(IConstants.AccPrivate));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccPrivate) 
                assertTrue("isPrivate with a non-Private", !Flags.isPrivate(flags[x]));
        }        
    }

    public void testIsPublic()
    {
        int x;
        assertTrue("isPublic with a Public", Flags.isPublic(IConstants.AccPublic));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccPublic) 
                assertTrue("isPublic with a non-Public", !Flags.isPublic(flags[x]));
        }        
    }
    
    public void testIsProtected()
    {
        int x;
        assertTrue("isProtected with a Protected", Flags.isProtected(IConstants.AccProtected));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccProtected) 
                assertTrue("isProtected with a non-Protected", !Flags.isProtected(flags[x]));
        }        
    }

    public void testIsRegister()
    {
        int x;
        assertTrue("isRegister with a Register", Flags.isRegister(IConstants.AccRegister));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccRegister) 
                assertTrue("isRegister with a non-Register", !Flags.isRegister(flags[x]));
        }        
    }
    
    public void testIsVirtual()
    {
        int x;
        assertTrue("isVirtual with a Virtual", Flags.isVirtual(IConstants.AccVirtual));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccVirtual) 
                assertTrue("isVirtual with a non-Virtual", !Flags.isVirtual(flags[x]));
        }        
    }

    public void testIsVolatile()
    {
        int x;
        assertTrue("isVolatile with a Volatile", Flags.isVolatile(IConstants.AccVolatile));
        for (x=0;x<flags.length;x++) {
            if (flags[x]!=IConstants.AccVolatile) 
                assertTrue("isVolatile with a non-Volatile", !Flags.isVolatile(flags[x]));
        }        
    }






}
