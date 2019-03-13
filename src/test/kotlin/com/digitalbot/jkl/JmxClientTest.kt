package com.digitalbot.jkl

import org.junit.AfterClass
import org.junit.BeforeClass
import java.lang.management.ManagementFactory
import java.rmi.registry.LocateRegistry
import javax.management.remote.JMXConnectorServer
import javax.management.remote.JMXConnectorServerFactory
import javax.management.remote.JMXServiceURL
import kotlin.test.Test
import kotlin.test.expect

/**
 * Client test.
 */
class JmxClientTest {
    companion object {
        /** jmx host */
        private const val HOST = "localhost"
        /** jmx PORT */
        private const val PORT = 10001

        /** test jmx server */
        private var jmxServer : JMXConnectorServer? = null

        /**
         * Set up JMX Server.
         */
        @BeforeClass
        @JvmStatic
        fun before() {
            val location = "service:jmx:rmi:///jndi/rmi://$HOST:$PORT/jmxrmi"
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
         * Shutdown JMX Server.
         */
        @AfterClass
        @JvmStatic
        fun after() {
            println("stop jmx server.")
            jmxServer?.stop()
            println("jmx server is stopped.")
        }
    }


    // === TEST ===

    @Test
    fun initTest() {
        JmxClient(HOST, PORT).use {
            expect(HOST) { it.host }
            expect(PORT) { it.port }
        }
    }

    @Test
    fun getBeanNamesTest() {
        JmxClient(HOST, PORT).use {
            // TODO: needs more detailed test...
            assert(it.getBeanNames().isNotEmpty())
        }
    }
}
