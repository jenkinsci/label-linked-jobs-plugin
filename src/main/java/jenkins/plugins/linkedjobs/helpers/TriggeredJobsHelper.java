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
import hudson.plugins.parameterizedtrigger.PredefinedBuildParameters;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.model.TriggeredJob;

import org.apache.tools.ant.filters.StringInputStream;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger.AllNodesForLabelBuildParameterFactory;
import org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger.NodeLabelBuildParameter;

public class TriggeredJobsHelper {
    
    // list all jobs that are "parameterized", and for which at least one of the
    // parameter is a Label parameter from the NodeLabelParameter plugin.
    // group them by label, using the default parameter of their Label parameter
    public static void populateJobsWithLabelDefaultValue(
            HashMap<Label, List<AbstractProject<?,?>>> jobsByDefaultLabel) {

        if (Jenkins.getInstance().getPlugin("parameterized-trigger") == null
         || Jenkins.getInstance().getPlugin("nodelabelparameter") == null) {
            return; // plugins not active, nothing to do
        }

        // scan through all jobs defined on this jenkins instance
        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            // scan through the properties of each job
            for (JobProperty<?> property : job.getProperties().values()) {
                if (!(property instanceof ParametersDefinitionProperty)) {
                    continue;
                }

                // this job is 'parameterized', loop through its parameters list
                for (ParameterDefinition pdef : ((ParametersDefinitionProperty) property).getParameterDefinitions()) {
                    if (!(pdef instanceof LabelParameterDefinition)) {
                        continue;
                    }
                    String defaultLabel = ((LabelParameterDefinition) pdef).defaultValue;
                    if (isSupportedLabel(defaultLabel)) {
                        Label label = Jenkins.getInstance().getLabel(defaultLabel);
                        List<AbstractProject<?, ?>> jobsForThisLabel = jobsByDefaultLabel.get(label);
                        if (jobsForThisLabel == null) {
                            jobsForThisLabel = new ArrayList<AbstractProject<?,?>>();
                            jobsByDefaultLabel.put(label, jobsForThisLabel);
                        }
                        jobsForThisLabel.add(job);
                    }
                }
            }
        }
    }

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
            
            if (!(triggeringJob instanceof Project)) {
                continue;
            }
            for (Builder builder : (List<Builder>)((Project<?, ?>)triggeringJob).getBuilders()) {
                if (!(builder instanceof TriggerBuilder)) {
                    continue;
                }
                // this job is triggering other jobs...
                List<BlockableBuildTriggerConfig> configs = ((TriggerBuilder) builder).getConfigs();
                for (BlockableBuildTriggerConfig config : configs) {
                    
                    List<AbstractProject> triggeredJobs = config.getProjectList(null);
                    
                    // use case one: a Parameters section
                    for (AbstractBuildParameters parameter : config.getConfigs()) {
                        if (parameter instanceof NodeLabelBuildParameter) {
                            // job is triggering other jobs based on specific label
                            // using the nodelabelparameter plugin
                            addTriggeredJobsByLabel(triggeredJobsByLabel, ((NodeLabelBuildParameter)parameter).nodeLabel,
                                    triggeredJobs, triggeringJob);
                        }
                        else if (parameter instanceof PredefinedBuildParameters) {
                            // do something with predefined parameters, that could be labels in triggered jobs configuration
                            try {
                                Properties p = new Properties();
                                p.load(new StringInputStream(((PredefinedBuildParameters)parameter).getProperties()));
                                
                                // remove all potentially unacceptable label (because of macro/token usage)
                                Set<Object> keysSet = p.keySet();
                                for (Iterator<Object> ite = keysSet.iterator(); ite.hasNext() ;) {
                                    String key = (String)ite.next();
                                    if (!isSupportedLabel(p.getProperty(key))) {
                                        ite.remove();
                                    }
                                }
                                addTriggeredJobsByPredefinedParameters(triggeredJobsByLabel, p, triggeredJobs, triggeringJob);
                            }
                            catch (IOException ioe) {
                                // TODO: log exception?
                            }
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
                        addTriggeredJobsByLabel(triggeredJobsByLabel, ((AllNodesForLabelBuildParameterFactory) factory).nodeLabel,
                                triggeredJobs, triggeringJob);
                    }
                }
            }
        }
    }
    
    private static void addTriggeredJobsByPredefinedParameters(HashMap<Label, HashMap<AbstractProject<?,?>, TriggeredJob>> triggeredJobsByLabel,
            Properties p, List<AbstractProject> triggeredJobs, AbstractProject<?, ?> triggeringJob) {
        
        if (triggeredJobs == null) {
            return;
        }

        // loop through the list of jobs triggered by the triggeringJob
        for (AbstractProject<?, ?> triggeredJob : triggeredJobs) {

            // find the parameterized settings of the triggered job
            for (JobProperty<?> property : triggeredJob.getProperties().values()) {
                if (!(property instanceof ParametersDefinitionProperty)) {
                    continue;
                }
                ParametersDefinitionProperty pdproperties = (ParametersDefinitionProperty)property;

                // loop through the list of predefined properties of the triggeringJob
                for (Map.Entry<Object, Object> entry : p.entrySet()) {
                    String strParameterName = entry.getKey().toString();
                    String strParameterValue = entry.getValue().toString();
                    // is there a matching parameter in the triggered job?
                    ParameterDefinition pdef = pdproperties.getParameterDefinition(strParameterName);
                    if (pdef != null && (pdef instanceof LabelParameterDefinition)) {
                        // yes, and it's a Label parameter!
                        Label label = Jenkins.getInstance().getLabel(strParameterValue);
                        // let's store it in our result
                        HashMap<AbstractProject<?, ?>, TriggeredJob> jobsTriggeredByCurrentLabel = triggeredJobsByLabel.get(label);
                        if (jobsTriggeredByCurrentLabel == null) {
                            // create data structure for this label in the result if it doesn't exist yet
                            jobsTriggeredByCurrentLabel = new HashMap<AbstractProject<?,?>, TriggeredJob>();
                            triggeredJobsByLabel.put(label, jobsTriggeredByCurrentLabel);
                        }
                        // associate triggered job to triggering job
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
    }
    
    static boolean isSupportedLabel(String strLabel) {
        if (strLabel == null) { 
            return false;
        }
        // if label contains a macro, ignore it altogether. It could be complicated, or even impossible
        // to expand the macro without being in the context of a build
        // see JENKINS-27588
        
        // first look for the ${...} syntax
        int start = strLabel.indexOf("${");
        int end = strLabel.indexOf("}");
        if (0 <= start && start <= end) {
            return false;
        }
        
        // then test also the $TOKEN syntax
        int index = strLabel.indexOf('$');
        if (index >= 0) {
            if (index + 1 < strLabel.length()) {
                Character nextChar = strLabel.charAt(index + 1);
                if (('a' <= nextChar && nextChar <= 'z')
                 || ('A' <= nextChar && nextChar <= 'Z')
                 || nextChar == '_') {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static void addTriggeredJobsByLabel(HashMap<Label, HashMap<AbstractProject<?,?>, TriggeredJob>> triggeredJobsByLabel,
            String strLabel, List<AbstractProject> triggeredJobs, AbstractProject<?, ?> triggeringJob) {

        if (!isSupportedLabel(strLabel)) {
            return;
        }

        Label label = Jenkins.getInstance().getLabel(strLabel);
        
        HashMap<AbstractProject<?, ?>, TriggeredJob> jobsTriggeredByCurrentLabel = triggeredJobsByLabel.get(label);
        if (jobsTriggeredByCurrentLabel == null) {
            // create data structure for this label in the result if it doesn't exist yet
            jobsTriggeredByCurrentLabel = new HashMap<AbstractProject<?,?>, TriggeredJob>();
            triggeredJobsByLabel.put(label, jobsTriggeredByCurrentLabel);
        }

        // scan the list of all triggered jobs
        if (triggeredJobs != null) {
            for (AbstractProject<?, ?> triggeredJob : triggeredJobs) {
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
