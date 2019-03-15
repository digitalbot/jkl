/*
 * The MIT License (MIT)
 *
 * Copyright 2019 Kosuke Nakamura a.k.a. digitalbot
 */
package com.digitalbot.jkl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import kotlin.system.exitProcess

/**
 * Jkl - commandline jmx client tool entity.
 *
 * @author digitalbot
 */
class Jkl : CliktCommand() {
    /**
     * requires valid jmx rmi "host:port"
     */
    private val hostport by argument("HOST:PORT")
            .convert { it.split(":").let { t -> t[0] to t[1] } }
            .validate { require(it.second.toIntOrNull() != null) { "Host and port must be joined by ':'." } }

    /**
     * bean name
     */
    private val bean by argument().optional()

    /**
     * attribute name
     */
    private val attribute by argument().optional()

    /**
     * type name (for composite date supported attribute.)
     */
    private val type by argument().optional()

    /**
     * requires "BEAN\tATTRIBUTE[\tTYPE][\tALIAS]"
     */
    private val targets by option("-t", "--target", help = """
        Usage: \"BEAN\\tATTRIBUTE[\\tTYPE][\\tALIAS]\".
        This option suppress specifying error's message.
        An alias cannot contains double quote and comma.
        """)
            .convert { it.replace("\\t", "\t").split("\t") }
            .multiple()
            .validate { target ->
                require(target.find { it.size == 1 } == null) { "Target must be splittable by tab character." }
            }

    private val file by option("-f", "--file").file(exists = true, folderOkay = false, readable = true)

    /**
     * Only check client can connect to JMX Server if specified.
     */
    private val ping by option("-p", "--ping").flag()

    /**
     * Show header. Only can specify with returning value calling.
     */
    private val showKeys by option("--show-keys").flag()

    /**
     * Output format option for showing values.
     */
    private val output by option("-o", "--output")
            .choice("csv", "list")
            .default("csv")

    /**
     * Use tab character for csv output format.
     */
    private val useTab by option("--use-tab").flag()

    private fun escapeIfNeeded(string: String?): String {
        return if (string == null) {
            ""
        } else if (useTab) {
            string
        } else if (string.contains(",") || string.contains("\"") || string.contains(" ")) {
            "\"${string.replace("\"", "\\\"")}\""
        } else {
            string
        }
    }

    /**
     * Implementation of CLI application.
     */
    override fun run() {
        // validate. correlation check.
        if (targets.isNotEmpty() && file != null) {
            echo(message = "Cannot specify '--targets' option and '--file' option together.", err = true)
            exitProcess(1)
        }
        val outputTargets = file?.useLines { seq ->
            seq.map { it.replace("\\t", "\t").split("\t") }.toList()
        } ?: targets

        if ((attribute ?: bean) != null && outputTargets.isNotEmpty()) {
            echo(message = "Cannot specify BEAN or ATTRIBUTE argument with '--targets' or '--file' option.", err = true)
            exitProcess(1)
        }
        if (showKeys && (attribute == null && outputTargets.isEmpty())) {
            echo(message = "Cannot specify '--show-keys' without attribute or '--targets' or '--file' option.", err = true)
            exitProcess(1)
        }

        val csvDelimiter = if (useTab) "\t" else ","

        try {
            JmxClient(hostport.first, hostport.second.toInt()).use { client ->
                when {
                    // do nothing
                    ping -> true

                    // bean and attribute arguments
                    bean != null -> {
                        if (attribute != null) {
                            // show values
                            if (type != null) {
                                val result = client.getValue(bean!!, attribute!!, type!!)
                                when (output) {
                                    "csv" -> {
                                        if (showKeys) {
                                            echo(escapeIfNeeded(result.getHeader()))
                                        }
                                        echo(escapeIfNeeded(result.value))
                                    }
                                    "list" -> {
                                        if (showKeys) {
                                            echo("${result.getHeader()}\t${result.value}")
                                        } else {
                                            echo(result.value)
                                        }
                                    }
                                    else -> true
                                }
                            } else {
                                val values = client.getValues(bean!!, attribute!!)
                                when (output) {
                                    "csv" -> {
                                        if (showKeys) {
                                            val headers = values.map { escapeIfNeeded(it.getHeader()) }
                                            echo(headers.joinToString(csvDelimiter))
                                        }
                                        val result = values.map { escapeIfNeeded(it.value) }
                                        echo(result.joinToString(csvDelimiter))
                                    }
                                    "list" -> {
                                        if (showKeys) {
                                            values.forEach {
                                                echo("${it.getHeader()}\t${it.value}")
                                            }
                                        } else {
                                            values.forEach { echo(it.value) }
                                        }
                                    }
                                    else -> true
                                }
                            }
                        } else {
                            // show attribute list
                            val attributeNames = client.getAttributeNames(bean!!)
                            when (output) {
                                "csv" -> {
                                    echo(attributeNames.joinToString(csvDelimiter))
                                }
                                "list" -> {
                                    attributeNames.forEach { echo(it) }
                                }
                                else -> true
                            }
                        }
                    }

                    // show values
                    outputTargets.isNotEmpty() -> {
                        val values = outputTargets
                                .map {
                                    if (it.size >= 3 && it[2].isNotBlank()) {
                                        listOf(client.getValueOrNull(it[0], it[1], it[2]))
                                    } else {
                                        client.getValuesOrNull(it[0], it[1])
                                    }
                                }
                                .flatten()
                        when (output) {
                            "csv" -> {
                                if (showKeys) {
                                    val headers = outputTargets
                                            .mapIndexed { index, list ->
                                                if (list.size >= 4) {
                                                    list[3]
                                                } else {
                                                    escapeIfNeeded(values[index]?.getHeader())
                                                }
                                            }
                                    echo(headers.joinToString(csvDelimiter))
                                }
                                val result = values.map { escapeIfNeeded(it?.value) }
                                echo(result.joinToString(csvDelimiter))
                            }
                            "list" -> {
                                if (showKeys) {
                                    values.forEachIndexed { index, value ->
                                        val header = if (outputTargets[index].size >= 4) {
                                            outputTargets[index][3]
                                        } else {
                                            value?.getHeader() ?: ""
                                        }
                                        val v = value?.value ?: ""
                                        echo("$header\t$v")
                                    }
                                } else {
                                    values.forEach { echo(it?.value ?: "") }
                                }
                            }
                            else -> true
                        }
                    }

                    // show all beans
                    else -> {
                        when (output) {
                            "csv" -> {
                                val beans = client.getBeanNames().map(this::escapeIfNeeded)
                                echo(beans.joinToString(csvDelimiter))
                            }
                            "list" -> {
                                val beans = client.getBeanNames()
                                beans.forEach { echo(it) }
                            }
                            else -> true
                        }
                    }
                }
            }
        } catch (e: JmxClientException) {
            echo(message = e.message, err = true)
            exitProcess(1)
        }
    }
}

/**
 * Main
 *
 * This is a command line jmx client tool's endpoint.
 * This tool is written by kotlin. So, this is called Jmx KLient JKL (Jekyll).
 *
 * @author digitalbot
 */
fun main(argv: Array<String>) = Jkl().versionOption("1.0.0").main(argv)
