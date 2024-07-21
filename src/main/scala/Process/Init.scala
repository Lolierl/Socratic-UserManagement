package Process

import Shared.PasswordHasher
import Common.API.{API, PlanContext, TraceID}
import Global.{ServerConfig, ServiceCenter}
import Common.DBAPI.{initSchema, readDBBoolean, writeDB}
import Common.ServiceUtils.schemaName
import cats.effect.IO
import io.circe.generic.auto.*
import org.http4s.client.Client
import Common.Object.{ParameterList, SqlParameter}

import java.util.UUID

object Init {
  def init(config:ServerConfig):IO[Unit]=
    given PlanContext=PlanContext(traceID = TraceID(UUID.randomUUID().toString),0)
    val (defaultPassword, salt) = PasswordHasher.hashPassword("yuanshen")
    for{
      _ <- API.init(config.maximumClientConnection)
      _ <- initSchema(schemaName)
      _ <- writeDB(s"CREATE TABLE IF NOT EXISTS ${schemaName}.users (user_name TEXT, password_hash TEXT, salt TEXT, usertype TEXT)", List())
      existingUsers <- readDBBoolean(
        s"SELECT EXISTS(SELECT 1 FROM ${schemaName}.users WHERE usertype = ?)",
        List(SqlParameter("String", "superuser"))
      )
      _ <- if (!existingUsers) {
        writeDB(
          s"INSERT INTO ${schemaName}.users (user_name, password, usertype) VALUES (?, ?, ?)",
          List(
            SqlParameter("String", "Dragon"),
            SqlParameter("String", defaultPassword),
            SqlParameter("String", salt),
            SqlParameter("String", "superuser")
          )
        )
      } else {
        IO.unit
      }
    } yield ()

}
