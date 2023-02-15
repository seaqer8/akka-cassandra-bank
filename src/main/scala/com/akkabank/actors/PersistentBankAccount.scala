package com.akkabank.actors

import akka.actor.typed.ActorRef

//a single bank account
class PersistentBankAccount {

}
/*
  event-sourcing
    -FAULT TAERANCE
    -ITS A BANK, SO AUDITING IS IMP, TO REVERSE THE BAD ACTOR BAD ACT
 */
// commands = messages
sealed trait Command

case class CreateBankAccount(user: String, currency: String, initialBalance: Double, replyTo: ActorRef[Response]) extends Command
//don't use Double, coz they have some issue with multiplying with other doubles
//https://stackoverflow.com/questions/3730019/why-not-use-double-or-float-to-represent-currency
case class updateBalance(id:String, currencey: String, amount: Double/*can be positive or negative*/, replyTo: ActorRef[Response]) extends Command
case class getBankAccount(id: String, replyTo: ActorRef[Response]) extends Command
//events = persist to cassandra

//states


//responses
sealed trait Response
