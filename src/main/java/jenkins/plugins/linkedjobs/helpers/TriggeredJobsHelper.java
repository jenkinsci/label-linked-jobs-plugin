package jenkins.plugins.linkedjobs.helpers;

import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import hudson.model.Label;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;

import java.util.HashMap;
import java.util.List;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.model.TriggeredJob;

import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger.AllNodesForLabelBuildParameterFactory;
import org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger.NodeLabelBuildParameter;

public class TriggeredJobsHelper {

    // list all jobs that are triggered by other jobs, for which configuration makes use of
    // NodeLabel Parameter Plugin and Parameterized Trigger Plugin to override 'on the fly'
    // the label parameter of the triggered job
    public static void populateTriggeredJobs(
            HashMap<Label, HashMap<AbstractProject<?,?>, TriggeredJob>> triggeredJobsByLabel) {
        
        if (Jenkins.getInstance().getPlugin("parameterized-trigger") == null
         || Jenkins.getInstance().getPlugin("nodelabelparameter") == null) {
            return; // plugins not active, nothing to do
        }
        
        for (AbstractProject<?, ?> triggeringJob : Jenkins.getInstance().getAllItems(
                AbstractProject.class)) {
            
            for (JobProperty property : triggeringJob.getProperties().values()) {
                if (property instanceof ParametersDefinitionProperty) {
                    for (ParameterDefinition pdef : ((ParametersDefinitionProperty)property).getParameterDefinitions()) {
                        if (pdef instanceof LabelParameterDefinition) {
                            String defaultLabel = ((LabelParameterDefinition)pdef).defaultValue;
                            // TODO: do something with these default labels?
                        }
                    }
                }
            }
            
            if (!(triggeringJob instanceof Project)) {
                continue;
            }
            for (Builder builder : (List<Builder>)((Project)triggeringJob).getBuilders()) {
                if (!(builder instanceof TriggerBuilder)) {
                    continue;
                }
                // this job is triggering other jobs...
                List<BlockableBuildTriggerConfig> configs = ((TriggerBuilder) builder).getConfigs();
                for (BlockableBuildTriggerConfig config : configs) {
                    
                    // use case one: a Parameters section
                    for (AbstractBuildParameters parameter : config.getConfigs()) {
                        if (parameter instanceof NodeLabelBuildParameter) {
                            // job is triggering other jobs based on specific label
                            // using the nodelabelparameter plugin
                            addTriggeredJob(triggeredJobsByLabel, ((NodeLabelBuildParameter)parameter).nodeLabel,
                                    config, triggeringJob);
                        }
                    }
                    
                    // use case two: a ParameterFactories section
                    List<AbstractBuildParameterFactory> factories = config.getConfigFactories();
                    if (factories == null) {
                        continue;
                    }
                    
                    for (AbstractBuildParameterFactory factory : factories) {
                        if (!(factory instanceof AllNodesForLabelBuildParameterFactory)) {
                            continue;
                        }

                        // job is triggering other jobs based on specific label
                        // using the nodelabelparameter plugin
                        addTriggeredJob(triggeredJobsByLabel, ((AllNodesForLabelBuildParameterFactory) factory).nodeLabel,
                                config, triggeringJob);
                    }
                }
            }
        }
    }
    
    private static void addTriggeredJob(HashMap<Label, HashMap<AbstractProject<?,?>, TriggeredJob>> triggeredJobsByLabel,
            String strLabel, BlockableBuildTriggerConfig config, AbstractProject<?, ?> triggeringJob) {
        
        // TODO: expand nodeLabel?
        Label label = Jenkins.getInstance().getLabel(strLabel);
        
        HashMap<AbstractProject<?, ?>, TriggeredJob> jobsTriggeredByCurrentLabel = triggeredJobsByLabel.get(label);
        if (jobsTriggeredByCurrentLabel == null) {
            jobsTriggeredByCurrentLabel = new HashMap<AbstractProject<?,?>, TriggeredJob>();
            triggeredJobsByLabel.put(label, jobsTriggeredByCurrentLabel);
        }

        // get the list of all triggered jobs
        List<AbstractProject> triggeredJobs = config.getProjectList(null);
        if (triggeredJobs != null) {
            for (AbstractProject triggeredJob : triggeredJobs) {
                // and store them, associated to the triggering job and currentLabel
                TriggeredJob triggeredJobData = jobsTriggeredByCurrentLabel.get(triggeredJob);
                if (triggeredJobData == null) {
                    triggeredJobData = new TriggeredJob(triggeredJob, triggeringJob);
                    jobsTriggeredByCurrentLabel.put(triggeredJob, triggeredJobData);
                }
                else {
                    triggeredJobData.addTriggeringJob(triggeringJob);
                }
            }
        }
    }
}
