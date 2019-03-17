package com.digitalbot.jkl

import org.junit.AfterClass
import org.junit.BeforeClass
import java.lang.management.ManagementFactory
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.rmi.server.UnicastRemoteObject
import java.security.Permission
import javax.management.remote.JMXConnectorServer
import javax.management.remote.JMXConnectorServerFactory
import javax.management.remote.JMXServiceURL
import kotlin.test.Test
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
        private lateinit var jmxServer: JMXConnectorServer
        private lateinit var registry: Registry

        /**
         * Set up trapping System.exit(?) and JMX Server.
         */
        @BeforeClass
        @JvmStatic
        fun before() {
            System.setSecurityManager(NoExitManager())

            val location = "service:jmx:rmi:///jndi/rmi://localhost:$PORT/jmxrmi"
            println("start jmx server ($location).")

            registry = LocateRegistry.createRegistry(PORT)
            jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(
                    JMXServiceURL(location),
                    null,
                    ManagementFactory.getPlatformMBeanServer())
            jmxServer.start()

            println("jmx server is started.")
        }

        /**
         * Shutdown JMX Server.
         */
        @AfterClass
        @JvmStatic
        fun after() {
            println("stop jmx server.")
            jmxServer.stop()
            UnicastRemoteObject.unexportObject(registry, true)
            println("jmx server is stopped.")
        }
    }

    // === TESTS ===

    @Test
    fun noArgumentTest() {
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
    fun helpTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf("-h"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun invalidHostportTest() {
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
    fun invalidPortTest() {
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
    fun pingTest() {
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

    @Test
    fun validHostportTest() {
        expect(0) {
            try {
                // use ping option
                Jkl().main(arrayOf("localhost:$PORT"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun beanArgumentTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--", "java.lang:type=Memory"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun attributeArgumentTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--", "java.lang:type=Memory", "HeapMemoryUsage"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun fullArgumentsTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--", "java.lang:type=Memory", "HeapMemoryUsage", "max"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun attributeArgumentsWithKeysTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--show-keys", "--", "java.lang:type=Memory", "HeapMemoryUsage"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun fullArgumentsWithKeysTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--show-keys", "--", "java.lang:type=Memory", "HeapMemoryUsage", "init"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun simpleTargetTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "-t=java.lang:type=Memory\tHeapMemoryUsage"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun multiTargetsWithKeysTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf(
                        "localhost:$PORT",
                        "--show-keys",
                        "-t=java.lang:type=Memory\tHeapMemoryUsage",
                        "-t=java.lang:type=GarbageCollector,name=G1 Young Generation\tCollectionCount"
                ))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun invalidArgumentAndTargetTest() {
        expect(1) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--show-keys", "-t=java.lang:type=Memory\tHeapMemoryUsage", "--", "java.lang:type=Memory"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun invalidTargetTest() {
        expect(1) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--show-keys", "-t=java.lang:type=Memory"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun invalidMultiTargetsTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--show-keys", "-t=foo\tbar", "-t=bar\tbaz"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun aliasedTargetsTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf(
                        "localhost:$PORT",
                        "--show-keys",
                        "-t=foo\tbar\tbaz\talias",
                        "-t=java.lang:type=Memory\tHeapMemoryUsage\tmax\tHeapMemoryUsageMax"
                ))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun aliasedTargetsListTest() {
        expect(0) {
            try {
                Jkl().main(arrayOf(
                        "localhost:$PORT",
                        "--output=list",
                        "--show-keys",
                        "-t=foo\tbar\tbaz\talias",
                        "-t=java.lang:type=Memory\tHeapMemoryUsage\tmax\tHeapMemoryUsageMax"
                ))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun invalidShowKeysTest() {
        expect(1) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--show-keys", "--", "java.lang:type=Memory"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }

    @Test
    fun fileTargetTest() {
        val tmpTargetFile = createTempFile("target")
        if (tmpTargetFile.exists()) {
             tmpTargetFile.delete()
        }
        tmpTargetFile.createNewFile()
        tmpTargetFile.appendText("java.lang:type=Memory\tHeapMemoryUsage\tmax\tHeapMemoryUsageMax\n")
        tmpTargetFile.appendText("java.lang:type=Memory\tNonHeapMemoryUsage\tinit\tNonHeapMemoryUsageInit\n")
        tmpTargetFile.appendText("java.lang:name=G1 Old Generation,type=GarbageCollector\tCollectionCount\t\tG1OldGCCount")
        expect(0) {
            try {
                Jkl().main(arrayOf("localhost:$PORT", "--show-keys", "-f=${tmpTargetFile.path}"))
                0
            } catch (e: ExitException) {
                e.state
            }
        }
    }
}
