package Impl

import APIs.PatientAPI.PatientQueryMessage
import Common.API.{PlanContext, Planner}
import Common.DBAPI.{writeDB, *}
import Common.Object.{ParameterList, SqlParameter}
import Common.ServiceUtils.schemaName
import cats.effect.IO
import io.circe.generic.auto.*
import Shared.PasswordHasher.verifyPassword


case class LoginMessagePlanner(userName: String, password:String, override val planContext: PlanContext) extends Planner[String]:
  override def plan(using planContext: PlanContext): IO[String] = {
    val checkUserExists = readDBBoolean(
      s"SELECT EXISTS(SELECT 1 FROM ${schemaName}.users WHERE user_name = ?)",
      List(SqlParameter("String", userName))
    )

    checkUserExists.flatMap { userExists =>
      if (!userExists) {
        IO.pure("Invalid user")
      } else {
        for {
          storedHash <- readDBString(
          s"SELECT password_hash FROM ${schemaName}.users WHERE user_name = ?",
          List(SqlParameter("String", userName))
          )
          storedSalt <- readDBString(
            s"SELECT salt FROM ${schemaName}.users WHERE user_name = ?",
            List(SqlParameter("String", userName))
          )
          result = verifyPassword(password, storedHash, storedSalt)
          userType <- readDBString(
            s"SELECT usertype FROM ${schemaName}.users WHERE user_name = ?",
            List(SqlParameter("String", userName))
          )
          output = if result then userType else "Wrong password"
        } yield output
        }
      }
    }


