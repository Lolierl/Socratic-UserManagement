package Impl

import Shared.PasswordHasher.hashPassword
import Common.API.{PlanContext, Planner}
import Common.DBAPI.{writeDB, *}
import Common.Object.{ParameterList, SqlParameter}
import Common.ServiceUtils.schemaName
import cats.effect.IO
import io.circe.generic.auto.*
import Shared.PasswordHasher.hashPassword
import Shared.PasswordHasher.verifyPassword


case class EditPasswordMessagePlanner(userName:String, oldPassword:String, newPassword:String, override val planContext: PlanContext) extends Planner[String] {
  override def plan(using planContext: PlanContext): IO[String] = {
    val (passwordHash, salt) = hashPassword(newPassword)
    for {
      storedHash <- readDBString(
        s"SELECT password_hash FROM ${schemaName}.users WHERE user_name = ?",
        List(SqlParameter("String", userName))
      )
      storedSalt <- readDBString(
        s"SELECT salt FROM ${schemaName}.users WHERE user_name = ?",
        List(SqlParameter("String", userName))
      )
      result <-
        if (verifyPassword(oldPassword, storedHash, storedSalt)) {
          writeDB(
            s"UPDATE ${schemaName}.users SET password_hash = ?, salt = ? WHERE user_name = ?",
            List(
              SqlParameter("String", passwordHash),
              SqlParameter("String", salt),
              SqlParameter("String", userName)
            )
          ).map(_ => "OK")
        } else {
          IO.pure("Original password input is incorrect")
        }
    } yield result
  }
}
