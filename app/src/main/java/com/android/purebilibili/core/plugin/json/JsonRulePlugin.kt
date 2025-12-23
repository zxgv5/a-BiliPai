// 文件路径: core/plugin/json/JsonRulePlugin.kt
package com.android.purebilibili.core.plugin.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 🎯 JSON 规则插件数据模型
 */
@Serializable
data class JsonRulePlugin(
    val id: String,
    val name: String,
    val description: String = "",
    val version: String = "1.0.0",
    val author: String = "Unknown",
    val type: String,  // "feed" | "danmaku"
    val iconUrl: String? = null,  // 🆕 插件图标 URL
    val rules: List<Rule>
)

/**
 * 🆕 条件表达式（支持 AND/OR 嵌套）
 */
@Serializable
sealed class Condition {
    /**
     * 简单条件：单个字段比较
     */
    @Serializable
    @SerialName("simple")
    data class Simple(
        val field: String,
        val op: String,
        val value: JsonElement
    ) : Condition()
    
    /**
     * AND 条件：所有子条件都必须满足
     */
    @Serializable
    @SerialName("and")
    data class And(
        val and: List<Condition>
    ) : Condition()
    
    /**
     * OR 条件：任一子条件满足即可
     */
    @Serializable
    @SerialName("or")
    data class Or(
        val or: List<Condition>
    ) : Condition()
}

/**
 * 单条规则
 * 
 * 支持两种格式：
 * 1. 旧格式（向后兼容）：直接使用 field/op/value
 * 2. 新格式：使用 condition 复合条件
 */
@Serializable
data class Rule(
    // 旧格式字段（向后兼容）
    val field: String? = null,
    val op: String? = null,
    val value: JsonElement? = null,
    
    // 🆕 新格式：复合条件
    val condition: Condition? = null,
    
    val action: String,       // 动作: hide, highlight
    val style: HighlightStyle? = null  // 仅 highlight 时使用
) {
    /**
     * 获取统一的条件对象（兼容新旧格式）
     */
    fun toCondition(): Condition? {
        // 优先使用新格式
        if (condition != null) return condition
        
        // 回退到旧格式
        if (field != null && op != null && value != null) {
            return Condition.Simple(field, op, value)
        }
        
        return null
    }
}

/**
 * 高亮样式
 */
@Serializable
data class HighlightStyle(
    val color: String? = null,     // 十六进制颜色 "#FFD700"
    val bold: Boolean = false,
    val scale: Float = 1.0f
)

/**
 * 规则操作符
 */
object RuleOperator {
    const val EQ = "eq"              // 等于
    const val NE = "ne"              // 不等于
    const val LT = "lt"              // 小于
    const val LE = "le"              // 小于等于
    const val GT = "gt"              // 大于
    const val GE = "ge"              // 大于等于
    const val CONTAINS = "contains"  // 包含
    const val STARTS_WITH = "startsWith"
    const val ENDS_WITH = "endsWith"
    const val REGEX = "regex"        // 正则匹配
    const val IN = "in"              // 在列表中
}

/**
 * 规则动作
 */
object RuleAction {
    const val HIDE = "hide"
    const val HIGHLIGHT = "highlight"
}

