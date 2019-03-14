package com.digitalbot.jkl

/**
 * AttributeValue class.
 *
 * This data class has value and meta information.
 *
 * @constructor Primary
 * @param beanName bean name
 * @param attributeName attribute name
 * @param type attribute child. This is using on composite data.
 * @param value value
  */
data class AttributeValue(val beanName: String, val attributeName: String, val type: String?, val value: String) {
    /**
     * Secondary
     *
     * This constructor can initialize without `type`.
     *
     * @param beanName bean name
     * @param attributeName attribute name
     * @param value value
     */
    constructor(beanName: String, attributeName: String, value: String) : this(beanName, attributeName, null, value)
}
