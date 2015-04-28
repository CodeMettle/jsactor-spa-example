package com.codemettle.jsactorexample.web.services

import com.codemettle.jsactorexample.web.shared.CommentsApi

import akka.actor.{Terminated, ActorRef, Props, Actor}

/**
 * @author steven
 *
 */
object CommentService {
  val props = Props(new CommentService)
}

class CommentService extends Actor {
  private var comments = Seq(
    CommentsApi.Comment("Steven", """ReActiveMQ
                                    |==========
                                    |
                                    |An [ActiveMQ](http://activemq.apache.org) client built on [Akka](http://akka.io)
                                    |[![Build Status](https://travis-ci.org/CodeMettle/reactivemq.svg?branch=master)](https://travis-ci.org/CodeMettle/reactivemq)
                                    |
                                    |The goal of ReActiveMQ is to provide an interface to ActiveMQ messaging that will feel familiar to Akka developers. At this point it is a fairly low-level connection-oriented interface that can consume AMQ queues and topics, send messages to endpoints, and do request-reply messaging, Camel-style, using temporary queues. More features will be added as necessary / requested.
                                    |
                                    |#### What about [akka-camel](http://doc.akka.io/docs/akka/snapshot/scala/camel.html)?
                                    |
                                    |Camel provides a plethora of components, and can be used with JMS/ActiveMQ, and if you need it then by all means use it. If, however, you're bringing in akka-camel, camel-core, and activemq-camel simply to do messaging, dealing with Camel's configuration (string arguments on endpoints, consumers, producers, etc), and adding another even-more blocking layer on top of ActiveMQ, then ReActiveMQ may be for you.
                                    |
                                    |#### What about (insert-jms-provider-here)?
                                    |
                                    |ReActiveMQ only uses JMS code, except to create the `ActiveMQConnectionFactory` and `ActiveMQMessage`s. If desired, ActiveMQ support can easily be moved into an optional library and other providers could be added. Make a feature request!
                                    |
                                    |
                                    |Import
                                    |------
                                    |
                                    |ReActiveMQ depends on akka-actor and activemq-client, but the dependencies are marked as `Provided`, so the end user must specify them as dependencies.
                                    |
                                    |#### Add Dependencies:
                                    |
                                    |sbt:
                                    |
                                    |```scala
                                    |libraryDependencies ++= Seq(
                                    |    "com.codemettle.reactivemq" %% "reactivemq" % "0.5.0",
                                    |    "org.apache.activemq" % "activemq-client" % "version",
                                    |    "com.typesafe.akka" %% "akka-actor" % "version"
                                    |)
                                    |```""".stripMargin)
  )

  private var subscribers = Set.empty[ActorRef]

  def receive = {
    case CommentsApi.SubscribeToComments ⇒
      context watch sender()
      subscribers += sender()
      sender() ! CommentsApi.UpdatedComments(comments)

    case Terminated(act) ⇒ subscribers -= act

    case CommentsApi.AddComment(comment) ⇒
      comments :+= comment
      val msg = CommentsApi.UpdatedComments(comments)
      subscribers foreach (_ ! msg)
  }
}
