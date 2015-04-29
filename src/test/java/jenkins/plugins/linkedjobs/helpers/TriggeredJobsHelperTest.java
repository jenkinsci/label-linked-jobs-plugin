package jenkins.plugins.linkedjobs.helpers;

import org.junit.Assert;
import org.junit.Test;

public class TriggeredJobsHelperTest {

    @Test
    public void testIsSupportedLabel() {
        Assert.assertTrue(TriggeredJobsHelper.isSupportedLabel("label_xyz"));
        Assert.assertTrue(TriggeredJobsHelper.isSupportedLabel("label_xyz $ bouh"));
        Assert.assertTrue(TriggeredJobsHelper.isSupportedLabel("label || label$"));
        Assert.assertFalse(TriggeredJobsHelper.isSupportedLabel(null));
        Assert.assertFalse(TriggeredJobsHelper.isSupportedLabel(" ${ MYMACRO } "));
        Assert.assertFalse(TriggeredJobsHelper.isSupportedLabel(" ${ MYMACRO, param=22 } "));
        Assert.assertFalse(TriggeredJobsHelper.isSupportedLabel(" $MYMACRO "));
        Assert.assertFalse(TriggeredJobsHelper.isSupportedLabel(" $_MYMACRO "));
        Assert.assertFalse(TriggeredJobsHelper.isSupportedLabel(" $myMACRO "));
        Assert.assertTrue(TriggeredJobsHelper.isSupportedLabel(" $ myMacro "));
        Assert.assertFalse(TriggeredJobsHelper.isSupportedLabel("$BUILDNUMBER"));
    }
}
