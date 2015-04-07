package jenkins.plugins.linkedjobs.model;

import java.util.ArrayList;
import java.util.List;

import hudson.model.AbstractProject;

// POJO to represent a triggered job and the list
// of the job(s) triggering it
public class TriggeredJob {

    private AbstractProject<?, ?> triggeredJob;
    private ArrayList<AbstractProject<?, ?>> triggeringJobs;
    
    public TriggeredJob(AbstractProject<?, ?> triggeredJob, AbstractProject<?, ?> triggeringJob) {
        this.triggeredJob = triggeredJob;
        triggeringJobs = new ArrayList<AbstractProject<?,?>>();
        triggeringJobs.add(triggeringJob);
    }
    
    public void addTriggeringJob(AbstractProject<?, ?> triggeringJob) {
        triggeringJobs.add(triggeringJob);
    }
    
    public AbstractProject<?, ?> getTriggeredJob() {
        return triggeredJob;
    }
    
    public List<AbstractProject<?, ?>> getTriggeringJobs() {
        return triggeringJobs;
    }
}
