/*
 * The MIT License
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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import hudson.util.FormApply;
import jakarta.servlet.ServletException;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.actions.LabelDashboardAction;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.POST;

/**
 * The "Label Linked Jobs" page under Manage Jenkins: the plugin settings, which used to
 * be a section of Configure System, and a link to the Labels Dashboard.
 * Users with Overall/Manage can change the settings; users with Overall/SystemRead see
 * them read-only.
 */
@Extension
@Symbol("labelLinkedJobs")
@Restricted(NoExternalUse.class)
public class LabelLinkedJobsManagement extends ManagementLink {

    @Override
    public String getIconFileName() {
        return "symbol-label-linked-jobs plugin-label-linked-jobs";
    }

    @Override
    public String getDisplayName() {
        return "Label Linked Jobs";
    }

    @Override
    public String getDescription() {
        return "Choose how the Linked Jobs pages and the Labels Dashboard list jobs.";
    }

    @Override
    public String getUrlName() {
        return "label-linked-jobs";
    }

    @NonNull
    @Override
    public Category getCategory() {
        return Category.CONFIGURATION;
    }

    /**
     * Shown to everyone who can open Manage Jenkins (Overall/Manage or Overall/SystemRead),
     * as Configure System is; the page itself checks those permissions.
     */
    @NonNull
    @Override
    public Permission getRequiredPermission() {
        return Jenkins.READ;
    }

    /**
     * Used by {@code index.jelly}.
     * @return the settings shown on this page
     */
    public GlobalSettings getSettings() {
        return GlobalSettings.get();
    }

    /**
     * Used by {@code index.jelly}.
     * @return the URL of the Labels Dashboard, relative to the Jenkins root
     */
    public String getDashboardUrl() {
        return ExtensionList.lookupSingleton(LabelDashboardAction.class).getUrlName();
    }

    @POST
    public synchronized void doConfigure(StaplerRequest2 req, StaplerResponse2 rsp)
            throws IOException, ServletException, Descriptor.FormException {
        Jenkins.get().checkPermission(Jenkins.MANAGE);
        GlobalSettings settings = getSettings();
        JSONObject form = req.getSubmittedForm();
        String name = settings.getJsonSafeClassName();
        settings.configure(req, form.has(name) ? form.getJSONObject(name) : new JSONObject());
        FormApply.success(req.getContextPath() + "/manage").generateResponse(req, rsp, null);
    }
}
