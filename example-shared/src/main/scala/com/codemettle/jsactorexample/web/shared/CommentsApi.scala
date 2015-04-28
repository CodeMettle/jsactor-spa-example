package com.codemettle.jsactorexample.web.shared

/**
 * @author steven
 *
 */
object CommentsApi {
  case class Comment(author: String, text: String)

  case object SubscribeToComments

  case class UpdatedComments(comments: Seq[Comment])

  case class AddComment(comment: Comment)
}
