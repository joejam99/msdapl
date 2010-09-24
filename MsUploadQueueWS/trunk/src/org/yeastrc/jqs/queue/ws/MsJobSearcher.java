/**
 * MsJobSearcher.java
 * @author Vagisha Sharma
 * Sep 10, 2010
 */
package org.yeastrc.jqs.queue.ws;

import org.yeastrc.jobqueue.MSJob;
import org.yeastrc.jobqueue.MSJobFactory;
import org.yeastrc.www.user.User;

/**
 * 
 */
public class MsJobSearcher {

	private MsJobSearcher() {}
	
	private static MsJobSearcher instance = new MsJobSearcher();
	
	public static MsJobSearcher getInstance() {
		return instance;
	}
	
	public MsJob search(int jobId) {
		
		if (jobId == 0)
			return null;
		
		try {
			MSJob msJob = MSJobFactory.getInstance().getJob(jobId);
			
			MsJob myJob = new MsJob();
			myJob.setId(msJob.getId());
			myJob.setProjectId(msJob.getProjectID());
			myJob.setPipeline(msJob.getPipeline().name());
			myJob.setDataDirectory(msJob.getServerDirectory());
			
			User submitter = new User();
			submitter.load(msJob.getSubmitter());
			myJob.setUserName(submitter.getUsername());
			
			myJob.setDate(msJob.getRunDate());
			myJob.setTargetSpecies(msJob.getTargetSpecies());
			myJob.setComments(msJob.getComments());
			String instrument = InstrumentLookup.getInstance().nameForId(msJob.getInstrumentId());
			myJob.setInstrument(instrument);
			
			myJob.setStatus(msJob.getStatusDescription());
			return myJob;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}