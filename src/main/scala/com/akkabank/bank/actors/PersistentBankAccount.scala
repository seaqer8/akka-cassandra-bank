package com.akkabank.bank.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

//a single bank account
object PersistentBankAccount {


  /*
  event-sourcing
    -FAULT TOLERANCE
    -ITS A BANK, SO AUDITING IS IMP, TO REVERSE THE BAD ACTOR BAD ACT
 */

  sealed trait Command

  object Command {
    case class CreateBankAccount(user: String, currency: String, initialBalance: Double, replyTo: ActorRef[Response]) extends Command

    //don't use Double, coz they have some issue with multiplying with other doubles
    //https://stackoverflow.com/questions/3730019/why-not-use-double-or-float-to-represent-currency
    case class UpdateBalance(id: String, currencey: String, amount: Double /*can be positive or negative*/ , replyTo: ActorRef[Response]) extends Command

    case class GetBankAccount(id: String, replyTo: ActorRef[Response]) extends Command
  }


  //events = persist to cassandra
  sealed trait Event

  case class BankAccountCreated(bankAccount: BankAccount) extends Event

  case class BalanceUpdated(amount: Double) extends Event

  //states
  case class BankAccount(id: String, user: String, currency: String, balance: Double)

  //responses
  sealed trait Response

  case class BankAccountCreatedResponse(id: String) extends Response

  case class BankAccountBalanceUpdatedResponse(maybeBankAccount: Option[BankAccount]) extends Response

  case class GetBankAccountResponse(maybeBankAccount: Option[BankAccount]) extends Response

  import PersistentBankAccount._
  import PersistentBankAccount.Command._


  val commandHandler: (BankAccount, Command) => Effect[Event, BankAccount] = (state, command) =>
    command match {
      case CreateBankAccount(user, currency, initialBalance, bank) =>
        /*
          -BANK CREAES ME
          - Bank sends create bank account
          - I persist BankAccountCreated
          - I update my state
          - Reply back with createBankAccountResponse
          - (The bank surfaces the response back to HTTP server)
         */
        val id = state.id
        Effect
        .persist(BankAccountCreated(BankAccount(id, user,  currency, initialBalance)))
        .thenReply(bank)(_ => BankAccountCreatedResponse(id ))

      case UpdateBalance(_, _ ,amount, bank) =>
        val newBalance = state.balance + amount
        if(newBalance < 0)
          Effect.reply(bank)(BankAccountBalanceUpdatedResponse(None))
        else
          Effect
          .persist(BalanceUpdated(amount))
          .thenReply(bank)(newState => BankAccountBalanceUpdatedResponse(Some(newState)))

      case GetBankAccount(id, bank) =>
        Effect
        .reply(bank)(GetBankAccountResponse(Some(state)))
    }

  val eventHandler: (BankAccount, Event) => BankAccount = (state, event) =>
    event match {
      case BankAccountCreated(bankAccount) => bankAccount
      case BalanceUpdated(amount) => state.copy(balance = state.balance + amount)
    }

  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, BankAccount](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = BankAccount(id, "", "", 0.0),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )

}
