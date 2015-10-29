package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * (gdb) itset view ggroup1
 * group1 contains:
 *   inferiors: 1, 2
 *   threads: 2, 3, 4, 5, 6, 7
 *   cores: 3, 3, 0, 3, 3, 3
 *
 * @since 5.0
 */
public class MIITSetViewInfo extends MIInfo {
	private static final String patternGroupStr = "(\\S+)\\s+contains:"; //$NON-NLS-1$
	private static final String patternInferiorsStr = "inferiors: (.*)"; //$NON-NLS-1$
	private static final String patternThreadsStr = "threads: (.*)"; //$NON-NLS-1$
	private static final String patternCoresStr = "cores: (.*)"; //$NON-NLS-1$
	
	final Pattern patternGroup;
	final Pattern patternInferiors;
	final Pattern patternThreads;
	final Pattern patternCores;
	
	public class ITSetView {
		String name;
		String[] inferiorIds;
		String[] threadIds;
		String[] coreIds;
		
		public String getGroupName() {
			return name;
		}
		public String[] getInferiorIds() {
			return inferiorIds;
		}
		public String[] getThreadIds() {
			return threadIds;
		}
		public String[] getCoreIds() {
			return coreIds;
		}
	}
	
    private ITSetView fITSetView;

    public MIITSetViewInfo(MIOutput rr) {
        super(rr);
        patternGroup = Pattern.compile(patternGroupStr);
        patternInferiors = Pattern.compile(patternInferiorsStr);
        patternThreads = Pattern.compile(patternThreadsStr);
        patternCores = Pattern.compile(patternCoresStr);
        
        parse();
    }

    public ITSetView getITSetView() {
        return fITSetView;
    }

    void parse() {
    	fITSetView = new ITSetView();
    	
		if (isDone()) {
			// CLI parsing
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++ ) {
				MIOOBRecord oob = oobs[i];
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput)oob).getString().trim();
					Matcher matcherGroup = patternGroup.matcher(line);
					Matcher matcherInferiors = patternInferiors.matcher(line);
					Matcher matcherThreads = patternThreads.matcher(line);
					Matcher matcherCores = patternCores.matcher(line);
					
					if (matcherGroup.find()) {
						fITSetView.name = matcherGroup.group(1);
						matcherGroup = null;
						continue;
					}
					else if (matcherInferiors.find()) {
						String[] inferiors = matcherInferiors.group(1).split(", "); //$NON-NLS-1$
						fITSetView.inferiorIds = inferiors;
						matcherInferiors = null;
						continue;
					}
					else if (matcherThreads.find()) {
						String[] threads = matcherThreads.group(1).split(", "); //$NON-NLS-1$
						fITSetView.threadIds = threads;
						matcherThreads = null;
						continue;
					}
					else if (matcherCores.find()) {
						String[] cores = matcherCores.group(1).split(", "); //$NON-NLS-1$
						fITSetView.coreIds = cores;
						matcherCores = null;
						continue;
					}
				}
			}
		}
    }
}
