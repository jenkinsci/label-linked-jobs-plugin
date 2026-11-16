package jenkins.plugins.linkedjobs.helpers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TriggeredJobsHelperTest {

    @Test
    public void testIsSupportedLabel() {
        assertTrue(TriggeredJobsHelper.isSupportedLabel("label_xyz"));
        assertTrue(TriggeredJobsHelper.isSupportedLabel("label_xyz $ bouh"));
        assertTrue(TriggeredJobsHelper.isSupportedLabel("label || label$"));
        assertFalse(TriggeredJobsHelper.isSupportedLabel(null));
        assertFalse(TriggeredJobsHelper.isSupportedLabel(" ${ MYMACRO } "));
        assertFalse(TriggeredJobsHelper.isSupportedLabel(" ${ MYMACRO, param=22 } "));
        assertFalse(TriggeredJobsHelper.isSupportedLabel(" $MYMACRO "));
        assertFalse(TriggeredJobsHelper.isSupportedLabel(" $_MYMACRO "));
        assertFalse(TriggeredJobsHelper.isSupportedLabel(" $myMACRO "));
        assertTrue(TriggeredJobsHelper.isSupportedLabel(" $ myMacro "));
        assertFalse(TriggeredJobsHelper.isSupportedLabel("$BUILDNUMBER"));
    }
}
