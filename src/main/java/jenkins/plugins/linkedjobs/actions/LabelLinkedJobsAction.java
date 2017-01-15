/*
 * The MIT License
 * 
 * Copyright (C) 2014, 2015 Dominique Brice
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugins.linkedjobs.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.model.JobsGroup;
import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.slaves.Cloud;

/**
 * For each Label, Jenkins associates an object of this class to a Linked Jobs page.
 * <p>
 * This class is responsible for building and collecting the data necessary to build
 * this additional page. Besides boiler-plate code for pure UI purpose, the interesting
 * function is getJobsGroups, which scans all Jobs/Projects of this Jenkins instance
 * @author dominiquebrice
 *
 */
public class LabelLinkedJobsAction extends AbstractLinkedJobsAction {
    
    /**
     * The label associated to this action
     */
    private final LabelAtom label;

    public LabelLinkedJobsAction(LabelAtom labelAtom) {
        // for now only store the label
        // calculation is done when requested by display
        this.label = labelAtom;
    }

    public String getTitle() {
        return label.getDisplayName() + " " + getDisplayName();
    }

    // will be called by my friend jelly via ${it.label}. Of course!
    public LabelAtom getLabel() {
        return this.label;
    }
    
    // clouds that can provision this atomic label
    public List<Cloud> getProvisioningClouds() {
        List<Cloud> provisioningClouds = new ArrayList<Cloud>();
        for (Cloud c : Jenkins.getInstance().clouds) {
            if (c.canProvision(label)) {
                provisioningClouds.add(c);
            }
        }
        return provisioningClouds;
    }
    
    // util function, because List doesn't have a function
    // to get its size starting with 'get', so no way to call it
    // directly in index.jelly
    public int getSize(List<Cloud> l) {
        return l == null ? 0 : l.size();
    }

    /**
     * This function retrieves all jobs existing on this jenkins instance and
     * group them by "assigned label", keeping only such labels that contains
     * the particular label of this LabelLinkedJobsAction instance
     * 
     * @return a list of jobs grouped by assigned label
     */
    public List<JobsGroup> getJobsGroups() {
        return buildJobsGroups();
    }
    
    protected List<JobsGroup> buildResult(HashMap<Label, JobsGroup> tmpResult) {
        ArrayList<JobsGroup> result = new ArrayList<JobsGroup>(tmpResult.size());
        // keep track from where we must finally sort the result
        int fromIndex = 0;
        // if the label itself is present, put it first in the list
        if (tmpResult.containsKey(label)) {
            result.add(tmpResult.remove(label));
            fromIndex = 1;
        }
        // add all remaining groups
        result.addAll(tmpResult.values());
        // and order them
        Collections.sort(result.subList(fromIndex, result.size()));
        return result;
    }
    
    protected boolean isLabelRelevant(Label jobLabel) {
        // condition for a job to be listed is simply to use the label,
        // regardless of the actual meaning of the expression
        // In other words, jobs with assigned label "windows&&!jdk7"
        // will show up on the jdk7 "linked jobs" page
        return jobLabel != null && jobLabel.listAtoms().contains(this.label);
    }
}