package org.eclipse.dd.mi.service.command;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


class LargePipedInputStream extends PipedInputStream {
	
	private final int LARGE_BUF_SIZE = 1024 * 1024; // 1 megs
	
	public LargePipedInputStream(PipedOutputStream pipedoutputstream)
        throws IOException
    {
		super(pipedoutputstream);
		buffer = new byte[LARGE_BUF_SIZE];
    }

}
