package org.eclipse.cdt.internal.core.win32;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

/*
 * Currently this will only work for Windows XP since tasklist
 * is only shipped on XP. This could change to some JNI
 * call out to get the list since the source to 'tlist' is
 * on the msdn web site but that can be done later.
 */
 
public class ProcessList implements IProcessList {

	private final int NAME = 1;
	private final int PID = 2;
	private final int OTHER = 3;
		
	public IProcessInfo[] getProcessList() {
		String OS = System.getProperty("os.name").toLowerCase();
		Process p = null;
		String command = null;
		InputStream	in = null;
		if ((OS.indexOf("windows xp") > -1)) {
			command = "tasklist /fo csv /nh  /svc";
		} else {
			return new IProcessInfo[0];
		}
		try {
			p = ProcessFactory.getFactory().exec(command);
		}
		catch (IOException e) {
			return null;
		}
		in = p .getInputStream();
		InputStreamReader reader = new InputStreamReader(in);
		StreamTokenizer tokenizer = new StreamTokenizer(reader);
		tokenizer.eolIsSignificant(true);
		tokenizer.parseNumbers();
		boolean done = false;
		ArrayList processList = new ArrayList();
		String name = null;
		int pid = 0, token_state = NAME;
		while( !done ) {
			try {
				switch ( tokenizer.nextToken() ) {
					case StreamTokenizer.TT_EOL:
						if ( name != null ) {
							processList.add(new ProcessInfo(pid, name));
							name = null;
						}
						break;
					case StreamTokenizer.TT_EOF:
						done = true;
						break;
					case '"':
						switch ( token_state ) {
							case NAME:
								name = tokenizer.sval;
								token_state = PID;
								break;
							case PID:
								try {
									pid = Integer.parseInt(tokenizer.sval);
								} catch (NumberFormatException e ) {
									name = null;
								}
								token_state = OTHER;
								break;
							case OTHER:
								token_state = NAME;
								break;
						}
						break;
				}
			}
			catch (IOException e) {
			}
		}						
		return (IProcessInfo[]) processList.toArray(new IProcessInfo[processList.size()]);
	}
}
