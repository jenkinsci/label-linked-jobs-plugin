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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
public class LabelLinkedJobsActionTest {

    @Test
    public void testIsAssignedLabelLinkedEdgeCases(JenkinsRule j) throws Exception {
        LabelLinkedJobsAction action = new LabelLinkedJobsAction(new LabelAtom("jdk7"));
        assertFalse(action.isLabelRelevant(null), "isAssignedLabelLinked fails when called with null");
        assertFalse(
                action.isLabelRelevant(new LabelAtom("jdk71")),
                "isAssignedLabelLinked is confused when name matches partially");
        assertFalse(
                action.isLabelRelevant(new LabelAtom("jdk")),
                "isAssignedLabelLinked is confused when name matches partially");
    }

    @Test
    public void testIsAssignedLabelLinked(JenkinsRule j) throws Exception {
        LabelLinkedJobsAction action = new LabelLinkedJobsAction(new LabelAtom("jdk7"));
        assertTrue(action.isLabelRelevant(Label.parseExpression("jdk7")));
        assertTrue(action.isLabelRelevant(Label.parseExpression("jdk7&&macos")));
        assertTrue(action.isLabelRelevant(Label.parseExpression("macos&&jdk7")));
        assertTrue(action.isLabelRelevant(Label.parseExpression("macos&&!jdk7")));
    }
}
