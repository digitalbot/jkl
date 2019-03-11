package com.digitalbot.jkl

import java.io.IOException
import java.net.MalformedURLException
import javax.management.MBeanServerConnection
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

/**
 * JmxClient class.
 *
 * This class can connect to JMX Service.
 *
 * @constructor Primary.
 *
 * This Object requires `host` and `port`.
 * JMX connection is established at the same time as initialization.
 *
 * @param host hostname
 * @param port port number
 * @throws JmxClientException if invalid host or port specified or connection cannot be made.
 */
open class JmxClient(val host: String, val port: Int) {
    companion object {
        /** static logger */
        @JvmStatic val logger = org.slf4j.LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    private val con: MBeanServerConnection

    init {
        logger.debug("INITIALIZING: start \"{}:{}\".", host, port)
        val location = "$host:$port"
        con = try {
            val jmxServiceURL = JMXServiceURL("service:jmx:rmi:///jndi/rmi://$location/jmxrmi")
            JMXConnectorFactory.connect(jmxServiceURL).use { it.mBeanServerConnection }
        } catch (e: MalformedURLException) {
            throw JmxClientException("Invalid host or port specified ($location).", e)
        } catch (e: IOException) {
            throw JmxClientException("Cannot connect jmx server ($location).", e)
        }
        logger.debug("INITIALIZING: done.")
    }

    /**
     * Gets the names of MBeans controlled by the MBean server.
     *
     * @return bean names list. This list is sorted by alphabetically.
     * @throws JmxClientException if cannot get any beans.
     */
    fun getBeanNames(): List<String> {
        return try {
            logger.debug("GET BEANS: start.")
            val set = con.queryNames(null, null)
            logger.debug("GET BEANS: done.")
            val result = set?.map { it.toString() }?.sorted()?.toList()
            logger.debug("GET BEANS: {}", result?.toString())
            result ?: emptyList()
        } catch (e: IOException) {
            throw JmxClientException("Cannot retrieve mbean.", e)
        }
    }

    /**
     * @return "host:port"
     */
    override fun toString(): String {
        return "$host:$port"
    }
}

/**
 * JmxClientException
 *
 * This Exception is thrown by JmxClient.
 *
 * @constructor
 * @param message error message
 * @param cause error cause
 */
open class JmxClientException(message: String?, cause: Throwable?) : RuntimeException(message, cause)
