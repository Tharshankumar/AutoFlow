package com.autoflow.app.core

/**
 * Logical operators for combining conditions in automation rules.
 * Supports AND, OR, NOT operators.
 */
enum class LogicalOperator {
    AND,
    OR,
    NOT;

    companion object {
        fun fromString(value: String): LogicalOperator {
            return when (value.uppercase()) {
                "AND" -> AND
                "OR" -> OR
                "NOT" -> NOT
                else -> AND // Default to AND
            }
        }
    }
}

/**
 * Represents a condition group with a logical operator.
 */
data class ConditionGroup(
    val operator: LogicalOperator = LogicalOperator.AND,
    val conditions: List<ConditionExpression> = emptyList()
)

/**
 * Represents a single condition expression that can be evaluated.
 */
data class ConditionExpression(
    val type: String,
    val value: String,
    val negate: Boolean = false
)

/**
 * Evaluates condition groups with logical operators.
 */
object ConditionGroupEvaluator {

    /**
     * Evaluate a condition group using the specified logical operator.
     */
    fun evaluate(
        group: ConditionGroup,
        conditionEvaluator: (ConditionExpression) -> Boolean
    ): Boolean {
        if (group.conditions.isEmpty()) return true

        return when (group.operator) {
            LogicalOperator.AND -> {
                group.conditions.all { condition ->
                    val result = conditionEvaluator(condition)
                    if (condition.negate) !result else result
                }
            }
            LogicalOperator.OR -> {
                group.conditions.any { condition ->
                    val result = conditionEvaluator(condition)
                    if (condition.negate) !result else result
                }
            }
            LogicalOperator.NOT -> {
                // NOT applies to the first condition
                val firstCondition = group.conditions.first()
                !conditionEvaluator(firstCondition)
            }
        }
    }
}
