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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.ExtensionList;
import hudson.model.ManagementLink;
import java.net.HttpURLConnection;
import java.net.URL;
import jenkins.model.Jenkins;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.html.HtmlCheckBoxInput;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
public class LabelLinkedJobsManagementTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
        Jenkins.MANAGE.setEnabled(true);
        Jenkins.SYSTEM_READ.setEnabled(true);
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.ADMINISTER)
                .everywhere()
                .to("admin")
                .grant(Jenkins.READ, Jenkins.MANAGE)
                .everywhere()
                .to("manager")
                .grant(Jenkins.READ, Jenkins.SYSTEM_READ)
                .everywhere()
                .to("reader")
                .grant(Jenkins.READ)
                .everywhere()
                .to("user"));
    }

    @AfterEach
    void tearDown() {
        Jenkins.MANAGE.setEnabled(false);
        Jenkins.SYSTEM_READ.setEnabled(false);
    }

    @Test
    void theLinkIsUnderManageJenkinsConfiguration() throws Exception {
        LabelLinkedJobsManagement link = ExtensionList.lookupSingleton(LabelLinkedJobsManagement.class);
        assertTrue(ManagementLink.all().contains(link));
        assertEquals(ManagementLink.Category.CONFIGURATION, link.getCategory());
        assertEquals("label-linked-jobs", link.getUrlName());
        assertNotNull(j.jenkins.getPlugin("label-linked-jobs"));

        HtmlPage manage = j.createWebClient().login("manager").goTo("manage");
        assertNotNull(manage.getAnchorByHref("label-linked-jobs"), "no link on Manage Jenkins");
    }

    @Test
    void theSettingsAreNoLongerOnConfigureSystem() throws Exception {
        assertTrue(GlobalSettings.get().getCategory() instanceof LabelLinkedJobsCategory);
        HtmlPage configure = j.createWebClient().login("admin").goTo("configure");
        assertTrue(
                configure.getElementsByName("_.detailedView").isEmpty(), "the settings are still on Configure System");
    }

    @Test
    void thePageShowsTheSettingsAndALinkToTheDashboard() throws Exception {
        HtmlPage page = j.createWebClient().login("admin").goTo("label-linked-jobs");
        HtmlForm form = page.getFormByName("config");
        for (String field : new String[] {
            "detailedView", "dashboardOrphanedJobsDetailedView", "showLabellessJobs", "showSingleNodeJobs"
        }) {
            HtmlCheckBoxInput box = form.getInputByName("_." + field);
            assertTrue(box.isChecked(), field + " should be checked by default");
        }
        assertNotNull(page.getAnchorByHref(j.contextPath + "/labelsdashboard/"));
    }

    @Test
    void savingThePageStoresTheSettings() throws Exception {
        HtmlPage page = j.createWebClient().login("manager").goTo("label-linked-jobs");
        HtmlForm form = page.getFormByName("config");
        form.<HtmlCheckBoxInput>getInputByName("_.detailedView").setChecked(false);
        form.<HtmlCheckBoxInput>getInputByName("_.showLabellessJobs").setChecked(false);
        j.submit(form);

        GlobalSettings settings = GlobalSettings.get();
        assertFalse(settings.getDetailedView());
        assertFalse(settings.getShowLabellessJobs());
        assertTrue(settings.getDashboardOrphanedJobsDetailedView());
        assertTrue(settings.getShowSingleNodeJobs());

        // written to the same file as before, so existing settings carry over
        GlobalSettings reloaded = new GlobalSettings();
        assertFalse(reloaded.getDetailedView());
        assertFalse(reloaded.getShowLabellessJobs());
    }

    @Test
    void systemReadSeesTheSettingsReadOnly() throws Exception {
        HtmlPage page = j.createWebClient().login("reader").goTo("label-linked-jobs");
        assertNotNull(page.getFormByName("config"));
        assertTrue(page.querySelectorAll("button[name=Submit]").isEmpty(), "a read-only user has a Save button");
    }

    @Test
    void systemReadCannotSave() throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient().login("reader");
        wc.setThrowExceptionOnFailingStatusCode(false);
        assertEquals(
                HttpURLConnection.HTTP_FORBIDDEN,
                wc.getPage(wc.addCrumb(
                                new WebRequest(new URL(j.getURL(), "label-linked-jobs/configure"), HttpMethod.POST)))
                        .getWebResponse()
                        .getStatusCode());
        assertTrue(GlobalSettings.get().getDetailedView());
    }

    @Test
    void readAloneCannotOpenThePage() throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient().login("user");
        FailingHttpStatusCodeException e =
                assertThrows(FailingHttpStatusCodeException.class, () -> wc.goTo("label-linked-jobs"));
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.getStatusCode());
    }
}
