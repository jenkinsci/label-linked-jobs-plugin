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

import net.sf.json.JSONObject;

import jenkins.plugins.linkedjobs.actions.LabelAction;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.labels.LabelAtom;
import hudson.model.labels.LabelAtomProperty;
import hudson.model.labels.LabelAtomPropertyDescriptor;

/**
 * Extension point of this plugin
 * two main roles:
 *  - add an Action, which makes an additional link available for Labels in the left-hand side menuy
 *  - define the Descriptor class, which manages the plugin configuration
 * @author dominiquebrice
 */
public class LabelExtension extends LabelAtomProperty {

    @DataBoundConstructor
    public LabelExtension() {
    }
    
    @Override
    public Collection<? extends Action> getActions(LabelAtom labelAtom) {
        // extend the left-side menu for Label with our new action/page
        return Collections.singleton(new LabelAction(labelAtom, getPluginSettings()));
    }
    
    public PluginSettings getPluginSettings() {
        return (PluginSettings)super.getDescriptor();
    }
    
    @Extension
    public static final class LabelPluginDescriptor extends LabelAtomPropertyDescriptor implements PluginSettings {
        
        private boolean detailedView = true;
        
        /**
         * In order to load the persisted global configuration, we have to 
         * call load() in the constructor.
         */
        public LabelPluginDescriptor() {
            load();
        }
        
        @Override
        public String getDisplayName() {
            // this string is displayed on the "Configure" page of labels
            // next to a checkbox that activates the plugin for that particular label
            // when checked, this leads to a call to getActions with the label as parameter
            return "Activate plugin to list projects linked to this label";
        }
        
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            detailedView = formData.getBoolean("detailedView");
            save();
            return super.configure(req,formData);
        }
        
        public boolean getDetailedView() {
            return detailedView;   
        }
    }
}
