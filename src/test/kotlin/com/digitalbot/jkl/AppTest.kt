package com.digitalbot.jkl

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.lang.management.ManagementFactory
import java.rmi.registry.LocateRegistry
import java.security.Permission
import javax.management.remote.JMXConnectorServer
import javax.management.remote.JMXConnectorServerFactory
import javax.management.remote.JMXServiceURL
import kotlin.test.expect

/**
 * CLI application test.
 */
class AppTest {
    /** for trapping System exit status. */
    class ExitException(val state: Int = 1) : SecurityException()

    /** for trapping System exit status. */
    class NoExitManager : SecurityManager() {
        override fun checkPermission(perm: Permission?) {
            // NOP
        }
        override fun checkExit(status: Int) {
            throw ExitException(status)
        }
    }

    companion object {
        /** jmx PORT */
        private const val PORT = 10001

        /** test jmx server */
        private var jmxServer : JMXConnectorServer? = null

        /**
         * Set up trapping System.exit(?) and JMX Server.
         */
        @BeforeClass
        @JvmStatic
        fun before() {
            System.setSecurityManager(NoExitManager())

            val location = "service:jmx:rmi:///jndi/rmi://localhost:$PORT/jmxrmi"
            println("start jmx server ($location).")
            LocateRegistry.createRegistry(PORT)
            jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(
                    JMXServiceURL(location),
                    null,
                    ManagementFactory.getPlatformMBeanServer())
            jmxServer?.start()
            println("jmx server is started.")
        }

        /**
         * shutdown JMX Server.
         */
        @AfterClass
        @JvmStatic
        fun after() {
            println("stop jmx server.")
            jmxServer?.stop()
            println("jmx server is stopped.")
        }
    }


    // === TESTS ===

    @Test
    fun testNoArgument() {
        expect(1) {
            try {
                Jkl().main(emptyArray())
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun testInvalidHostport() {
        expect(1) {
            try {
                Jkl().main(arrayOf("hostport"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun testInvalidPort() {
        expect(1) {
            try {
                Jkl().main(arrayOf("host:foo"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun testValidHostport() {
        expect(0) {
            try {
                // use ping option
                Jkl().main(arrayOf("localhost:$PORT", "-p"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }
}
