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

package jenkins.plugins.linkedjobs.extensions;

import java.util.Collection;
import java.util.Collections;

import jenkins.plugins.linkedjobs.actions.LabelAction;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.labels.LabelAtom;
import hudson.model.labels.LabelAtomProperty;
import hudson.model.labels.LabelAtomPropertyDescriptor;

/**
 * ExtensionPoint of this plugin<BR/>
 * Two main roles:
 * <ul><li>add an Action, which makes an additional link available for Labels in the left-hand side menu</li>
 * <li>define the Descriptor class, which manages the plugin configuration <i>per label</i> - that is,
 * returning the name of the option to activate the plugin for that label.
 * See <code>LabelConfigurationDescriptor.getDisplayName()</code></li>
 * </ul>
 * @author dominiquebrice
 */
public class LabelExtension extends LabelAtomProperty {
    
    @DataBoundConstructor
    public LabelExtension() {
    }
    
    @Override
    public Collection<? extends Action> getActions(LabelAtom labelAtom) {
        // extend the left-side menu for Label with our new action/page
        return Collections.singleton(new LabelAction(labelAtom));
    }
    
    /**
     * Since the plugin configuration is handled globally by LabelPluginSettings,
     * the sole role of this descriptor is to define getDisplayName()
     * @author domi
     *
     */
    @Extension
    public static final class LabelConfigurationDescriptor extends LabelAtomPropertyDescriptor {

        @Override
        public String getDisplayName() {
            // this string is displayed on the "Configure" page of labels
            // next to a checkbox that activates the plugin for that particular label
            // when checked, this leads to a call to getActions with the label as parameter
            return "Activate plugin to list projects linked to this label";
        }
    }
}
