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

package jenkins.plugins.linkedjobs.settings;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * The role of this class is to act as a central point for all things related to the
 * global configuration of the plugin, i.e. mainly the section shown in the 
 * Manage Jenkins\Configure System page.
 * @author dominiquebrice
 */
public class LabelPluginSettings extends AbstractDescribableImpl<LabelPluginSettings> {
    
    public static boolean getDetailedView() {
        return getDescriptorImpl().getDetailedView();
    }
    
    public static boolean getDashboardOrphanedJobsDetailedView() {
        return getDescriptorImpl().getDashboardOrphanedJobsDetailedView();
    }
    
    private static LabelPluginDescriptorImpl getDescriptorImpl() {
        return (LabelPluginDescriptorImpl)(Jenkins.getInstance().getDescriptorByType(LabelPluginDescriptorImpl.class));
    }
    
    /**
     * this inner class holds and manages all global pugin settings, that are exposed
     * to the rest of the plugin via static method in the outer class
     * @author dominiquebrice
     */
    @Extension
    public static final class LabelPluginDescriptorImpl extends Descriptor<LabelPluginSettings> {     
        
        /**
         * toggle to determiner whether the jobs should be listed in a condensed way or not on the
         * Linked Jobs page (in each label section)
         */
        private boolean detailedView = true;
        
        /**
         * toggle to determiner whether the jobs should be listed in a condensed way or not on the
         * Labels Dashboard
         */
        private boolean dashboardOrphanedJobsDetailedView = true;
        
        /**
         * In order to load the persisted global configuration, we have to 
         * call load() in the constructor.
         */
        public LabelPluginDescriptorImpl() {
            load();
        }
        
        @Override
        public String getDisplayName() {
            return "Debug Domi";
        }
        
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            detailedView = formData.getBoolean("detailedView");
            dashboardOrphanedJobsDetailedView = formData.getBoolean("dashboardOrphanedJobsDetailedView");
            save();
            return super.configure(req,formData);
        }
        
        public boolean getDetailedView() {
            return detailedView;   
        }
        
        public boolean getDashboardOrphanedJobsDetailedView() {
            return dashboardOrphanedJobsDetailedView;   
        }
    }
}
