/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.pdom.tests;

import java.util.Arrays;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.dom.ast.tag.Tag;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.tag.PDOMTag;
import org.eclipse.cdt.internal.core.pdom.tag.PDOMTagIndex;

// copy/pasted from BTreeTests
public class PDOMTagIndexTests extends BaseTestCase
{
    private ICProject cProject;
	private PDOM pdom;

	public static Test suite()
	{
		return suite( PDOMTagIndexTests.class );
	}

	@Override
	protected void setUp() throws Exception
	{
	    super.setUp();

	    int attempts = 10;
	    IIndexFragment[] fragments = null;
	    do
	    {
            // If this test is run too quickly after the previous, then the old project is still being
            // deleted and this one gets a PDOMProxy.  So, if we don't get what we want, just wait and
            // try again.
            if( fragments != null )
            {
                //CProjectHelper.delete( cProject );
                Thread.sleep( 1000 );
            }

	        cProject = CProjectHelper.createCCProject( "PDOMTagIndexTests." + Double.toString( Math.random() ).substring( 2 ), "bin", IPDOMManager.ID_FAST_INDEXER );
    		IIndex index = CCorePlugin.getIndexManager().getIndex(  cProject );
    		assertTrue( index instanceof CIndex );

    		fragments = ( (CIndex)index ).getFragments();
    		assertTrue( fragments.length > 0 );
	    }
    	while( ! ( fragments[0] instanceof PDOM ) && --attempts >= 0 );

	    // Create a dummy instance of the PDOM for the various tests to operate upon.  Also acquire the PDOM's
	    // write lock to simulate the condition under which the tag index is normally accessed.
		assertTrue( fragments[0] instanceof PDOM );
		pdom = (PDOM)fragments[0];
		pdom.acquireWriteLock();
	}

	@Override
	protected void tearDown() throws Exception
	{
	    // Don't delete the project because there are timing issues with recreating the project on the
	    // next test case.  A new handle is created for every test case, and they are all cleaned up
	    // when the suite is done.

        super.tearDown();
	}

	// return the nearest valid record that is less than the specified base
	private static long lastRecordBase = 1000;
	private static long computeValidRecord()
	{
	    lastRecordBase += 1000;
	    return ( lastRecordBase & ~7L ) | 2;
	}

    // A quick sanity test to confirm basic functionality.
    public void testSimple() throws Exception
    {
        String tagger = "tagger_a";
        long rec = computeValidRecord();

        assertNotNull( PDOMTagIndex.createTag( pdom, rec, tagger, 1 ) );
        assertNotNull( PDOMTagIndex.getTag( pdom, rec, tagger ) );
    }

    public void testMultipleTaggers() throws Exception
    {
        String tagger_a = "tagger_a";
        String tagger_b = "tagger_b";
        long rec1 = computeValidRecord();
        long rec2 = computeValidRecord();

        assertNotNull( PDOMTagIndex.createTag( pdom, rec1, tagger_a, 1 ) );
        assertNotNull( PDOMTagIndex.createTag( pdom, rec1, tagger_b, 1 ) );
        assertNotNull( PDOMTagIndex.createTag( pdom, rec2, tagger_a, 1 ) );

        assertNotNull( PDOMTagIndex.getTag( pdom, rec2, tagger_a ) );
        assertNull(    PDOMTagIndex.getTag( pdom, rec2, tagger_b ) );

        Iterable<ITag> tags1 = PDOMTagIndex.getTags( pdom, rec1 );
        int tag_count = 0;
        for( ITag tag : tags1 )
        {
            ++tag_count;
            assertTrue( tag.getTaggerId().equals( tagger_a ) || tag.getTaggerId().equals( tagger_b ) );
            assertEquals( 1, tag.getDataLen() );
        }
        assertEquals( 2, tag_count );
    }

    public void testReplaceTags() throws Exception
    {
        String tagger_a = "tagger_a";
        String tagger_b = "tagger_b";
        long rec = computeValidRecord();

        ITag taga = PDOMTagIndex.createTag( pdom, rec, tagger_a, 2 );
        assertNotNull( taga );
        assertTrue( taga instanceof PDOMTag );
        PDOMTag taga_pdom = (PDOMTag)taga;
        ITag tagb = PDOMTagIndex.createTag( pdom, rec, tagger_a, 2 );
        assertNotNull( tagb );

        // replacement should delete tags for taggers that are no longer present and shorter tags
        // should be modified in place
        PDOMTagIndex.setTags( pdom, rec, Arrays.<ITag>asList( new Tag( tagger_a, 1 ) ) );
        assertNull( PDOMTagIndex.getTag( pdom, rec, tagger_b ) );
        ITag shorter_ = PDOMTagIndex.getTag( pdom, rec, tagger_a );
        assertNotNull( shorter_ );
        assertTrue( shorter_ instanceof PDOMTag );
        PDOMTag shorter_pdom = (PDOMTag)shorter_;
        assertEquals( taga_pdom.getRecord(), shorter_pdom.getRecord() );

        // longer tags should create a new record
        PDOMTagIndex.setTags( pdom, rec, Arrays.<ITag>asList( new Tag( tagger_a, 4 ) ) );
        ITag longer_ = PDOMTagIndex.getTag( pdom, rec, tagger_a );
        assertNotNull( longer_ );
        assertTrue( longer_ instanceof PDOMTag );
        PDOMTag longer_pdom = (PDOMTag)longer_;
        assertTrue( taga_pdom.getRecord() != longer_pdom.getRecord() );

        // TODO figure out how to tell if the original tag was free'd
    }
}
