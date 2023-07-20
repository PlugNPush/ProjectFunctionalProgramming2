package mlb

import zio._
import zio.jdbc._
import zio.http._
import com.github.tototoshi.csv._
import java.io.File
import java.time.LocalDate
import zio.stream.ZStream
import zio.Console
import scala.util.*

import HomeTeams._
import AwayTeams._
import GameDates._
import SeasonYears._
import PlayoffRounds._
import HomeScores._
import AwayScores._
import HomeElos._
import AwayElos._
import HomeRartingProbs._
import AwayRartingProbs._

import java.sql.Date

object MlbApi extends ZIOAppDefault {

  import DataService._
  import ApiService._

  val static: App[Any] = Http.collect[Request] {
    case Method.GET -> Root / "text" => Response.text("Hello MLB Fans! How are you?\n")
    case Method.GET -> Root / "json" => Response.json("""{"greetings": "Hello MLB Fans!"}""")
  }.withDefaultErrorResponse

  val endpoints: App[ZConnectionPool] = Http.collectZIO[Request] {
    case Method.GET -> Root / "init" =>
      (for {
        // Create a CSVReader
        res: Response <- importGames()
      } yield res
      )
    case Method.GET -> Root / "game" / "latest" / homeTeam / awayTeam =>
      for {
        game: Option[Game] <- latest(HomeTeam(homeTeam), AwayTeam(awayTeam))
        res: Response = latestGameResponse(game)
      } yield res
    case Method.GET -> Root / "game" / "predict" / homeTeam / awayTeam =>
      // FIXME : implement correct logic and response
      ZIO.succeed(Response.text(s"$homeTeam vs $awayTeam win probability: 0.0"))
    case Method.GET -> Root / "games" / "count" =>
      for {
        count: Option[Int] <- count
        res: Response = countResponse(count)
      } yield res
    case Method.GET -> Root / "games" / "history" / homeTeam =>
      import zio.json.EncoderOps
      import Game._
      // FIXME: implement correct database request
      ZIO.succeed(Response.json(games.toJson).withStatus(Status.Ok))
    case _ =>
      ZIO.succeed(Response.text("Not Found").withStatus(Status.NotFound))
  }.withDefaultErrorResponse

  val appLogic: ZIO[ZConnectionPool & Server, Throwable, Unit] = for {
    _ <- create
    _ <- Server.serve[ZConnectionPool](static ++ endpoints)
  } yield ()

  override def run: ZIO[Any, Throwable, Unit] =
    appLogic.provide(createZIOPoolConfig >>> connectionPool, Server.default)
}

object ApiService {

  import zio.json.EncoderOps
  import Game._

  def countResponse(count: Option[Int]): Response = {
    count match
      case Some(c) => Response.text(s"$c game(s) in historical data").withStatus(Status.Ok)
      case None => Response.text("No game in historical data").withStatus(Status.NotFound)
  }

  def latestGameResponse(game: Option[Game]): Response = {
    println(game)
    game match
      case Some(g) => Response.json(g.toJson).withStatus(Status.Ok)
      case None => Response.text("No game found in historical data").withStatus(Status.NotFound)
  }

  def importGames(): ZIO[ZConnectionPool, Throwable, Response] = {
    for {
      _ <- Console.printLine("Importing CSV to Database")
      reader <- ZIO.succeed(CSVReader.open("../mlb_elo_latest.csv"))
      s <- ZStream.fromIterator(reader.iteratorWithHeaders).map[Game](row => 
        Game(
          GameDate(LocalDate.parse(row("date"))),
          SeasonYear(row("season").toInt),
          if (row("playoff").isEmpty) PlayoffRound(-1) else PlayoffRound(row("playoff").toInt),
          HomeTeam(row("team1")),
          AwayTeam(row("team2")),
          if (row("score1").isEmpty) HomeScore(-1) else HomeScore(row("score1").toInt),
          if (row("score2").isEmpty) AwayScore(-1) else AwayScore(row("score2").toInt),
          if (row("elo1_post").isEmpty) HomeElo(-1.0) else HomeElo(row("elo1_post").toDouble),
          if (row("elo2_post").isEmpty) AwayElo(-1) else AwayElo(row("elo2_post").toDouble),
          HomeRatingProb(row("rating_prob1").toDouble),
          AwayRatingProb(row("rating_prob2").toDouble)
          )
        ).grouped(1000).foreach(chunk =>{ 
          for {
            res <- DataService.insertRows(chunk.toList)
            _ <- Console.printLine("Should insert " + res.rowsUpdated + " rows")
          } yield ()
        })
        
      _ <- ZIO.succeed(reader.close())
      _ <- Console.printLine("CSV imported to Database")

    } yield (Response.text("CSV imported to Database").withStatus(Status.Ok))
  }
}

object DataService {

  val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
    ZLayer.succeed(ZConnectionPoolConfig.default)

  val properties: Map[String, String] = Map(
    "user" -> "postgres",
    "password" -> "postgres"
  )

  val connectionPool: ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool] =
    ZConnectionPool.h2mem(
      database = "mlb",
      props = properties
    )

  val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"DROP TABLE IF EXISTS games; CREATE TABLE IF NOT EXISTS games(date DATE NOT NULL, season_year INT NOT NULL, playoff_round INT, home_team VARCHAR(3), away_team VARCHAR(3), score1 INT, score2 INT, elo1_post DOUBLE, elo2_post DOUBLE, rating_prob1 DOUBLE NOT NULL, rating_prob2 DOUBLE NOT NULL)"
    )
  }

  import GameDates.*
  import PlayoffRounds.*
  import SeasonYears.*
  import HomeTeams.*
  import AwayTeams.*
  import HomeScores.*
  import AwayScores.*
  import HomeElos.*
  import AwayElos.*
  import HomeRartingProbs.*
  import AwayRartingProbs.*

  // Should be implemented to replace the `val insertRows` example above. Replace `Any` by the proper case class.
  def insertRows(games: List[Game]): ZIO[ZConnectionPool, Throwable, UpdateResult] = {
    Console.printLine("...")
    val rows: List[Game.Row] = games.map(_.toRow)
    Console.printLine("row: " + rows)
    transaction {
      insert(
        sql"INSERT INTO games(date, season_year, playoff_round, home_team, away_team, score1, score2, elo1_post, elo2_post, rating_prob1, rating_prob2)".values[Game.Row](rows)
      )
    }
  }

  val count: ZIO[ZConnectionPool, Throwable, Option[Int]] = transaction {
    selectOne(
      sql"SELECT COUNT(*) FROM games".as[Int]
    )
  }

  def latest(homeTeam: HomeTeam, awayTeam: AwayTeam): ZIO[ZConnectionPool, Throwable, Option[Game]] = {
    transaction {
      selectOne(
        sql"SELECT date, season_year, playoff_round, home_team, away_team, score1, score2, elo1_post, elo2_post, rating_prob1, rating_prob2 FROM games WHERE home_team = ${HomeTeam.unapply(homeTeam)} AND away_team = ${AwayTeam.unapply(awayTeam)} ORDER BY date DESC LIMIT 1".as[Game]
      )
    }
  }
}
