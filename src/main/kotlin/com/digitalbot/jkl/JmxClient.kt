package com.digitalbot.jkl

import java.io.IOException
import java.net.MalformedURLException
import javax.management.AttributeNotFoundException
import javax.management.InstanceNotFoundException
import javax.management.MBeanInfo
import javax.management.MBeanServerConnection
import javax.management.MalformedObjectNameException
import javax.management.ObjectName
import javax.management.openmbean.CompositeDataSupport
import javax.management.remote.JMXConnector
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
open class JmxClient(val host: String, val port: Int) : AutoCloseable {
    companion object {
        /** static logger */
        @JvmStatic val logger = org.slf4j.LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    /** connection */
    private val jmxConnection: JMXConnector

    init {
        logger.debug("INITIALIZING: start \"{}:{}\".", host, port)
        val location = "$host:$port"
        jmxConnection = try {
            val jmxServiceURL = JMXServiceURL("service:jmx:rmi:///jndi/rmi://$location/jmxrmi")
            JMXConnectorFactory.connect(jmxServiceURL)
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
        val mbsc = mbsc()
        return try {
            logger.debug("GET BEANS: start.")
            val set = mbsc.queryNames(null, null)
            logger.debug("GET BEANS: done.")
            val result = set?.map { it.toString() }?.sorted()?.toList()
            logger.debug("GET BEANS: {}", result?.toString())
            result ?: emptyList()
        } catch (e: IOException) {
            throw JmxClientException("Cannot retrieve mbean.", e)
        }
    }

    /**
     * Gets attributes name list of MBean.
     *
     * @param name bean name.
     * @return MBeanInfo.attributes.name (a.k.a. command) list.
     * @throws JmxClientException if cannot get command list.
     */
    fun getAttributeNames(name: String): List<String> {
        val info = getMBeanInfo(name)
        logger.debug("GET BEANS ATTRIBUTES: start.")
        val result = info.attributes.map { it.name }.toList()
        logger.debug("GET BEANS ATTRIBUTES: {}.", result)
        logger.debug("GET BEANS ATTRIBUTES: end.")
        return result
    }

    /**
     * Gets attribute values.
     *
     * @param beanName bean name.
     * @param attributeName attribute name.
     * @return values includes { beanName, attributeName, type, value }
     * @throws JmxClientException if cannot get value.
     */
    fun getValues(beanName: String, attributeName: String): List<AttributeValue> {
        val mbsc = mbsc()
        val objectName = toObjectName(beanName)
        try {
            logger.debug("GET VALUE: start.")
            val value = mbsc.getAttribute(objectName, attributeName)
            logger.debug("GET VALUE: {}.", value)
            val result = when (value) {
                is CompositeDataSupport -> value.compositeType.keySet()
                        .sorted()
                        .map { AttributeValue(beanName, attributeName, it, "${value.get(it)}") }
                        .toList()
                is Array<*> -> value
                        .mapIndexed { index, any -> AttributeValue(beanName, attributeName, "$index", "$any") }
                        .toList()
                else -> listOf(AttributeValue(beanName, attributeName, "$value"))
            }
            logger.debug("GET VALUE: end.")
            return result
        } catch (e: InstanceNotFoundException) {
            throw JmxClientException("Invalid mbean name specified ($beanName).", e)
        } catch (e: AttributeNotFoundException) {
            throw JmxClientException("Invalid attribute name specified ($beanName::$attributeName).", e)
        } catch (e: Exception) {
            throw JmxClientException("Cannot retrieve value ($beanName::$attributeName).", e)
        }
    }


    private fun getMBeanInfo(name: String): MBeanInfo {
        val mbsc = mbsc()
        val objectName = toObjectName(name)
        try {
            logger.debug("GET BEAN INFO: start.")
            val result = mbsc.getMBeanInfo(objectName)
            logger.debug("GET BEAN INFO: {}.", result)
            logger.debug("GET BEAN INFO: end.")
            return result
        } catch (e: InstanceNotFoundException) {
            throw JmxClientException("Invalid mbean name specified ($name).", e)
        } catch (e: Exception) {
            throw JmxClientException("Cannot retrieve mbean ($name).", e)
        }
    }

    private fun toObjectName(name: String): ObjectName {
        try {
            return ObjectName(name)
        } catch (e: MalformedObjectNameException) {
            throw JmxClientException("Invalid mbean name specified ($name).", e)
        }
    }

    private fun mbsc() : MBeanServerConnection {
        try {
            return jmxConnection.mBeanServerConnection
        } catch (e: IOException) {
            throw JmxClientException("Cannot connect bean server.", e)
        }
    }

    /**
     * @return "host:port"
     */
    override fun toString(): String {
        return "$host:$port"
    }

    /**
     * This client must be closed.
     */
    override fun close() {
        jmxConnection.close()
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
