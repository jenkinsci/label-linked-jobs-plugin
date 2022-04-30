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

package jenkins.plugins.linkedjobs.model;

import java.util.ArrayList;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.actions.LabelLinkedJobsAction;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.slaves.Cloud;
import hudson.util.VersionNumber;

public class LabelAtomData extends AbstractJobsGroup implements Comparable<LabelAtomData> {

    private final LabelAtom labelAtom;
    // list all nodes defining this label
    private ArrayList<Node> nodes;

    public LabelAtomData(LabelAtom l) {
        super();
        labelAtom = l;
        nodes = new ArrayList<Node>();
    }

    public void add(Node n) {
        nodes.add(n);
    }
    
    //************************************************
    // functions used to render display in index.jelly
    //************************************************
    
    @Exported
    public String getDescription() {
        // configurable description for LabelAtom was implemented in Jenkins core v1.580
        if (Jenkins.getVersion() != null && !Jenkins.getVersion().isOlderThan(new VersionNumber("1.580"))) {
            return labelAtom.getDescription() != null && labelAtom.getDescription().trim().length() > 0 ? labelAtom.getDescription() : null;
        }
        else {
            return null;
        }
    }
    
    @Exported
    public String getLabel() {
        return labelAtom.getDisplayName();
    }
    
    @Exported
    public String getLabelURL() {
        return labelAtom.getUrl();
    }
    
    @Exported
    public int getNodesCount() {
        return nodes.size();
    }
    
    // JENKINS-32445
    // return the number of clouds that can provision this atomic label
    @Exported
    public int getCloudsCount() {
        int result = 0;
        for (Cloud c : Jenkins.getInstance().clouds) {
            if (c.canProvision(labelAtom)) {
                result++;
            }
        }
        return result;
    }
    
    @Exported
    public boolean getPluginActiveForLabel() {
        for (hudson.model.Action a : labelAtom.getActions()) {
            if (a instanceof LabelLinkedJobsAction) {
                return true;
            }
        }
        return false;
    }

    /************************************
     * Comparable interface implementation
     ************************************/
    @Override
    public int compareTo(LabelAtomData o) {
        return this.labelAtom.compareTo(o.labelAtom);
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof LabelAtomData) && this.compareTo((LabelAtomData)o) == 0;
    }
    
    @Override
    public int hashCode() {
        return labelAtom.hashCode();
    }
}
