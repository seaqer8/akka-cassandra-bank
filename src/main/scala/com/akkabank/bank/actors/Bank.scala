package com.akkabank.bank.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

import java.util.UUID

object Bank {

  //commands = message
  import PersistentBankAccount.Command._
  import PersistentBankAccount.Command

  //events
  sealed trait Event
  case class BankAccountCreated(id: String) extends Event
  //state

  case class State(accounts: Map[String,ActorRef[Command]])

  //command handler
  val commandHandler: (State, Command) => Effect[Event,State] = (state, command) => // This is called a signature
    command match {

      case CreateBankAccount(user, currency, initialBalance, replyTo) =>
        val id = UUID.randomUUID().toString
        val newBankAccount =
    }

  //event handler
  val eventHandler: (State, Event) => State = ???//A function from state and event and it produces a new state

  ///behavior
  def apply(): Behavior[Command] =
    EventSourcedBehavior[Command,Event, State](
      persistenceId = PersistenceId.ofUniqueId("Bank"),
      emptyState = State(Map()),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )



}
