/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jul 19, 2004
 */
package org.eclipse.cdt.core.parser.tests.scanner2;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectMap;

/**
 * @author aniefer
 */
public class ObjectMapTest extends TestCase {

    static public class HashObject{
        HashObject( int h ){
            hash = h;
        }
        public int hashCode(){
            return hash;
        }
        final public int hash;
    }

    public void insertContents( ObjectMap map, Object[][] contents ) throws Exception {
        for( int i = 0; i < contents.length; i++ )
            map.put( contents[i][0], contents[i][1] );
    }
    
    public void assertContents( ObjectMap map, Object[][] contents ) throws Exception {
        for( int i = 0; i < contents.length; i++ ){
            assertEquals( map.keyAt(i), contents[i][0] );
            assertEquals( map.getAt(i), contents[i][1] );
            assertEquals( map.get(contents[i][0]), contents[i][1] );
        }
        assertEquals( map.size(), contents.length );
    }
    
    public void testSimpleAdd() throws Exception{
        ObjectMap map = new ObjectMap( 2 );
    
        Object [][] contents = new Object[][] { {"1", "ob" } };  //$NON-NLS-1$//$NON-NLS-2$

        insertContents( map, contents );
        assertContents( map, contents );
        
        assertEquals( map.size(), 1 );
        assertEquals( map.capacity(), 2 );
    }
    
    public void testSimpleCollision() throws Exception{
        ObjectMap map = new ObjectMap( 2 );
        
        HashObject key1 = new HashObject( 1 );
        HashObject key2 = new HashObject( 1 );
        
        Object [][] contents = new Object[][] { {key1, "1" }, //$NON-NLS-1$
                								{key2, "2" } };   //$NON-NLS-1$
        
        insertContents( map, contents );
        
        assertEquals( map.size(), 2 );
        assertEquals( map.capacity(), 2 );

        assertContents( map, contents );
    }
    
    public void testResize() throws Exception{
        ObjectMap map = new ObjectMap( 1 );
        
        assertEquals( map.size(), 0 );
        assertEquals( map.capacity(), 1 );
        
        Object [][] res = new Object [][] { { "0", "o0" },  //$NON-NLS-1$//$NON-NLS-2$
							                { "1", "o1" },  //$NON-NLS-1$//$NON-NLS-2$
							                { "2", "o2" },  //$NON-NLS-1$//$NON-NLS-2$
							                { "3", "o3" },  //$NON-NLS-1$//$NON-NLS-2$
							                { "4", "o4" } };  //$NON-NLS-1$//$NON-NLS-2$
        
        insertContents( map, res );
        assertEquals( map.capacity(), 8 );
        assertContents( map, res );
    }
    
    public void testCollisionResize() throws Exception{
        ObjectMap map = new ObjectMap( 1 );
        
        assertEquals( map.size(), 0 );
        assertEquals( map.capacity(), 1 );
        
        Object [][] res = new Object [][] { { new HashObject(0), "o0" },  //$NON-NLS-1$
							                { new HashObject(1), "o1" },  //$NON-NLS-1$
							                { new HashObject(0), "o2" },  //$NON-NLS-1$
							                { new HashObject(1), "o3" },  //$NON-NLS-1$
							                { new HashObject(0), "o4" } };  //$NON-NLS-1$
        
        insertContents( map, res );
        assertEquals( map.capacity(), 8 );
        assertContents( map, res );
    }
    
    public void testReAdd() throws Exception{
        ObjectMap map = new ObjectMap( 1 );
        
        assertEquals( map.size(), 0 );
        assertEquals( map.capacity(), 1 );
        
        Object [][] res = new Object [][] { { "0", "o0" },  //$NON-NLS-1$ //$NON-NLS-2$
							                { "1", "o1" } };  //$NON-NLS-1$ //$NON-NLS-2$
							                
        insertContents( map, res );
        assertEquals( map.capacity(), 2 );
        assertContents( map, res );
        
        res = new Object [][]{ { "0",  "o00" },  //$NON-NLS-1$ //$NON-NLS-2$
                			   { "1",  "o01" },  //$NON-NLS-1$ //$NON-NLS-2$
                			   { "10", "o10" },  //$NON-NLS-1$ //$NON-NLS-2$
        					   { "11", "o11" } };  //$NON-NLS-1$ //$NON-NLS-2$
        
        insertContents( map, res );
        assertContents( map, res );
    }
    
    public void testResizeResolvesCollision() throws Exception{
        ObjectMap map = new ObjectMap( 2 );
        
        Object k1 = new HashObject( 0 );
        Object k2 = new HashObject( 1 );
        Object k3 = new HashObject( 4 );	//collision with 0 in a table capacity 2, but ok in table capacity 4
        
        Object [][] con = new Object[][] { { k1, "1" },  //$NON-NLS-1$
                						   { k2, "2" }, //$NON-NLS-1$
                						   { k3, "3" } } ; //$NON-NLS-1$
        
        insertContents( map, con );
        assertContents( map, con );
    }
    
    public void testCharArrayUtils() throws Exception{
        char [] buffer = "A::B::C".toCharArray(); //$NON-NLS-1$
        
        assertEquals( CharArrayUtils.lastIndexOf( "::".toCharArray(), buffer ), 4 ); //$NON-NLS-1$
        assertTrue( CharArrayUtils.equals( CharArrayUtils.lastSegment( buffer, "::".toCharArray()), "C".toCharArray() ) ); //$NON-NLS-1$ //$NON-NLS-2$
        
        buffer = "A::B::C:foo".toCharArray(); //$NON-NLS-1$
        assertEquals( CharArrayUtils.lastIndexOf( "::".toCharArray(), buffer ), 4 ); //$NON-NLS-1$
        assertTrue( CharArrayUtils.equals( CharArrayUtils.lastSegment( buffer, "::".toCharArray()), "C:foo".toCharArray() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
