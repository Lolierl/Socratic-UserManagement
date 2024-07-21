package Impl

import Shared.PasswordHasher.hashPassword
import Common.API.{PlanContext, Planner}
import Common.DBAPI.{writeDB, *}
import Common.Object.{ParameterList, SqlParameter}
import Common.ServiceUtils.schemaName
import cats.effect.IO
import io.circe.generic.auto.*


case class RegisterMessagePlanner(userName: String, password: String, usertype: String, override val planContext: PlanContext) extends Planner[String] {
  override def plan(using planContext: PlanContext): IO[String] = {
    val (hashedPassword, salt) = hashPassword(password)
    writeDB(
      s"INSERT INTO ${schemaName}.users (user_name, password_hash, salt, usertype) VALUES (?, ?, ?, ?)",
      List(
        SqlParameter("String", userName),
        SqlParameter("String", hashedPassword),
        SqlParameter("String", salt),
        SqlParameter("String", usertype)
      ))
  }}



