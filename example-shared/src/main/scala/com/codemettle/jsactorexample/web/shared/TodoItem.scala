package com.codemettle.jsactorexample.web.shared

/**
 * @author steven
 *
 */
sealed trait TodoPriority
case object TodoLow extends TodoPriority
case object TodoNormal extends TodoPriority
case object TodoHigh extends TodoPriority

case class TodoItem(id: Long, content: String, priority: TodoPriority, completed: Boolean)
