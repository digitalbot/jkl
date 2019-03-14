package com.digitalbot.jkl

import org.junit.AfterClass
import org.junit.BeforeClass
import java.lang.management.ManagementFactory
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.rmi.server.UnicastRemoteObject
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
        private var jmxServer: JMXConnectorServer? = null
        private var registry: Registry? = null

        /**
         * Set up JMX Server.
         */
        @BeforeClass
        @JvmStatic
        fun before() {
            val location = "service:jmx:rmi:///jndi/rmi://$HOST:$PORT/jmxrmi"
            println("start jmx server ($location).")

            registry = LocateRegistry.createRegistry(PORT)
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
            if (registry != null) {
                UnicastRemoteObject.unexportObject(registry, true)
            }
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

    @Test
    fun getAttributeNamesTest() {
        JmxClient(HOST, PORT).use { client ->
            // jvm11
            val memoryAttributes = setOf(
                    "Verbose",
                    "ObjectPendingFinalizationCount",
                    "HeapMemoryUsage",
                    "NonHeapMemoryUsage",
                    "ObjectName"
            )

            expect(memoryAttributes) {
                client.getAttributeNames("java.lang:type=Memory").toSet()
            }
        }
    }

    @Test
    fun getValuesTest() {
        JmxClient(HOST, PORT).use { client ->
            val memoryValues = client.getValues(
                    "java.lang:type=Memory",
                    "HeapMemoryUsage"
            )
            expect(4) { memoryValues.size }
            expect("java.lang:type=Memory") { memoryValues[0].beanName }
            expect("HeapMemoryUsage") { memoryValues[0].attributeName }
            expect("committed") { memoryValues[0].type }
            expect("java.lang:type=Memory") { memoryValues[1].beanName }
            expect("HeapMemoryUsage") { memoryValues[1].attributeName }
            expect("init") { memoryValues[1].type }
            expect("java.lang:type=Memory") { memoryValues[2].beanName }
            expect("HeapMemoryUsage") { memoryValues[2].attributeName }
            expect("max") { memoryValues[2].type }
            expect("java.lang:type=Memory") { memoryValues[3].beanName }
            expect("HeapMemoryUsage") { memoryValues[3].attributeName }
            expect("used") { memoryValues[3].type }
        }
    }

}
