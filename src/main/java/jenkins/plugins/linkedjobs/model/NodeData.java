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

import jenkins.model.Jenkins;
import hudson.model.Node;

public class NodeData extends AbstractJobsGroup implements Comparable<NodeData> {

    private Node node;

    public NodeData(Node n) {
        super();
        node = n;
    }
    
    //************************************************
    // functions used to render display in index.jelly
    //************************************************
    
    public String getName() {
        return node.getDisplayName();
    }
    
    public String getLabelURL() {
        return node.getSelfLabel().getUrl();
    }
    
    public String getNodeURL() {
        return Jenkins.getInstance().getComputer(node.getNodeName()).getUrl();
    }

    /************************************
     * Comparable interface implementation
     ************************************/
    @Override
    public int compareTo(NodeData o) {
        return this.getName().compareTo(o.getName());
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof NodeData) && this.compareTo((NodeData)o) == 0;
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
