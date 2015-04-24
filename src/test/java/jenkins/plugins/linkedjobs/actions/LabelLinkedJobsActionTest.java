/*
 * The MIT License
 * 
 * Copyright (C) 2014 Dominique Brice
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

import hudson.model.Label;
import hudson.model.labels.LabelAtom;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class LabelLinkedJobsActionTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    @Test
    public void testIsAssignedLabelLinkedEdgeCases() throws Exception {
        LabelLinkedJobsAction action = new LabelLinkedJobsAction(new LabelAtom("jdk7"));
        Assert.assertFalse("isAssignedLabelLinked fails when called with null", action.isLabelRelevant(null));
        Assert.assertFalse("isAssignedLabelLinked is confused when name matches partially", action.isLabelRelevant(new LabelAtom("jdk71")));
        Assert.assertFalse("isAssignedLabelLinked is confused when name matches partially", action.isLabelRelevant(new LabelAtom("jdk")));
    }
    
    @Test
    public void testIsAssignedLabelLinked() throws Exception {
        LabelLinkedJobsAction action = new LabelLinkedJobsAction(new LabelAtom("jdk7"));
        Assert.assertTrue(action.isLabelRelevant(Label.parseExpression("jdk7")));
        Assert.assertTrue(action.isLabelRelevant(Label.parseExpression("jdk7&&macos")));
        Assert.assertTrue(action.isLabelRelevant(Label.parseExpression("macos&&jdk7")));
        Assert.assertTrue(action.isLabelRelevant(Label.parseExpression("macos&&!jdk7")));
    }
}
