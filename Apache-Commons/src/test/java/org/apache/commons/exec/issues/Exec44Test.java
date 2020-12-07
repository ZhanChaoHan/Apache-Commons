package org.apache.commons.exec.issues;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.TestUtil;
import org.junit.Test;

/**
 * Test EXEC-44 (https://issues.apache.org/jira/browse/EXEC-44).
 *
 * @version $Id: Exec44Test.java 1557130 2014-01-10 14:23:40Z britter $
 */
public class Exec44Test {

    private final Executor exec = new DefaultExecutor();
    private final File testDir = new File("src/test/scripts");
    private final File foreverTestScript = TestUtil.resolveScriptForOS(testDir + "/forever");

    /**
     *
     * Because the ExecuteWatchdog is the only way to destroy asynchronous
     * processes, it should be possible to set it to an infinite timeout,
     * for processes which should not timeout, but manually destroyed
     * under some circumstances.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExec44() throws Exception {

        final CommandLine cl = new CommandLine(foreverTestScript);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);

        exec.setWatchdog(watchdog);
        exec.execute(cl, resultHandler);

        // wait for script to run
        Thread.sleep(5000);
        assertTrue("The watchdog is watching the process", watchdog.isWatching());

        // terminate it
        watchdog.destroyProcess();
        assertTrue("The watchdog has killed the process", watchdog.killedProcess());
        assertFalse("The watchdog is no longer watching any process", watchdog.isWatching());
    }
}
