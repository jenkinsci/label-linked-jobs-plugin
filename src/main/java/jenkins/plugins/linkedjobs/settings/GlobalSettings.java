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

import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;

/**
 * The role of this class is to act as a central point for all things related to the
 * global configuration of the plugin, i.e. mainly the section shown in the 
 * Manage Jenkins\Configure System page.<BR/>
 * @author dominiquebrice
 */
@Extension
public class GlobalSettings extends GlobalConfiguration {
    
    /**
     * toggle to determine whether the jobs should be listed in a condensed way or not on the
     * Linked Jobs page (in each label section)
     */
    private boolean detailedView = true;
    
    /**
     * toggle to determine whether the jobs should be listed in a condensed way or not on the
     * Labels Dashboard
     */
    private boolean dashboardOrphanedJobsDetailedView = true;
    
    /**
     * toggle to determine whether jobs that can run on only one node should be shown
     * in the dashboard
     */
    private boolean showSingleNodeJobs = true;
    
    /**
     * toggle to determine whether jobs with no labels should be shown
     * in the dashboard
     */
    private boolean showLabellessJobs = true;
    
    public GlobalSettings() {
        // this loads the settings from this plugin xml file
        // into this instance's private members
        load();
    }
    
    /**
     * Some plugin implementations make use of @Inject to make this available
     * in other plugin classes. Only problem is that it requires the target class
     * to extend Descriptor, so a variable still has to be carried from objects to objects.
     * This class is supposed to be a singleton anyway, let's make it accessible with a good
     * old static function
     * @return
     */
    public static GlobalSettings get() {
        return GlobalConfiguration.all().get(GlobalSettings.class);
    }
    
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        // To persist global configuration information,
        // set that to properties and call save().
        detailedView = formData.getBoolean("detailedView");
        dashboardOrphanedJobsDetailedView = formData.getBoolean("dashboardOrphanedJobsDetailedView");
        showSingleNodeJobs = formData.getBoolean("showSingleNodeJobs");
        showLabellessJobs = formData.getBoolean("showLabellessJobs");
        
        // save this instance members to the plugin configuration file
        save();
        return super.configure(req,formData);
    }
    
    public boolean getDetailedView() {
        return detailedView;   
    }
    
    public boolean getDashboardOrphanedJobsDetailedView() {
        return dashboardOrphanedJobsDetailedView;   
    }
    
    public boolean getShowSingleNodeJobs() {
        return showSingleNodeJobs;
    }
    
    public boolean getShowLabellessJobs() {
        return showLabellessJobs;
    }
}
