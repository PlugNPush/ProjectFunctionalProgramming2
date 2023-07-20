import com.github.tototoshi.csv._
import java.io.File
import java.time.LocalDate
import zio.json._
import zio.jdbc._
import zio.stream.ZStream
import zio._


import java.sql.Date

object HomeTeams {

  opaque type HomeTeam = String

  object HomeTeam {

    def apply(value: String): HomeTeam = value

    def unapply(homeTeam: HomeTeam): String = homeTeam
  }

  given CanEqual[HomeTeam, HomeTeam] = CanEqual.derived
  implicit val homeTeamEncoder: JsonEncoder[HomeTeam] = JsonEncoder.string
  implicit val homeTeamDecoder: JsonDecoder[HomeTeam] = JsonDecoder.string
}

object AwayTeams {

  opaque type AwayTeam = String

  object AwayTeam {

    def apply(value: String): AwayTeam = value

    def unapply(awayTeam: AwayTeam): String = awayTeam
  }

  given CanEqual[AwayTeam, AwayTeam] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[AwayTeam] = JsonEncoder.string
  implicit val awayTeamDecoder: JsonDecoder[AwayTeam] = JsonDecoder.string
}

object GameDates {

  opaque type GameDate = LocalDate

  object GameDate {

    def apply(value: LocalDate): GameDate = value

    def unapply(gameDate: GameDate): LocalDate = gameDate
  }

  given CanEqual[GameDate, GameDate] = CanEqual.derived
  implicit val gameDateEncoder: JsonEncoder[GameDate] = JsonEncoder.localDate
  implicit val gameDateDecoder: JsonDecoder[GameDate] = JsonDecoder.localDate
}

object SeasonYears {

  opaque type SeasonYear <: Int = Int

  object SeasonYear {

    def apply(year: Int): SeasonYear = year

    def safe(value: Int): Option[SeasonYear] =
      Option.when(value >= 1876 && value <= LocalDate.now.getYear)(value)

    def unapply(seasonYear: SeasonYear): Int = seasonYear
  }

  given CanEqual[SeasonYear, SeasonYear] = CanEqual.derived
  implicit val seasonYearEncoder: JsonEncoder[SeasonYear] = JsonEncoder.int
  implicit val seasonYearDecoder: JsonDecoder[SeasonYear] = JsonDecoder.int
}

object PlayoffRounds {

  opaque type PlayoffRound = String

  object PlayoffRound {

    def apply(round: String): PlayoffRound = round

    def unapply(playoffRound: PlayoffRound): String = playoffRound
  }

  given CanEqual[PlayoffRound, PlayoffRound] = CanEqual.derived
  implicit val playoffRoundEncoder: JsonEncoder[PlayoffRound] = JsonEncoder.string
  implicit val playoffRoundDEncoder: JsonDecoder[PlayoffRound] = JsonDecoder.string
}

object HomeScores {

  opaque type HomeScore = Int

  object HomeScore {

    def apply(value: Int): HomeScore = value

    def unapply(homeScore: HomeScore): Int = homeScore
  }

  given CanEqual[HomeScore, HomeScore] = CanEqual.derived
  implicit val homeScoreEncoder: JsonEncoder[HomeScore] = JsonEncoder.int
  implicit val homeScoreDecoder: JsonDecoder[HomeScore] = JsonDecoder.int
}

object AwayScores {

  opaque type AwayScore = Int

  object AwayScore {

    def apply(value: Int): AwayScore = value

    def unapply(awayScore: AwayScore): Int = awayScore
  }

  given CanEqual[AwayScore, AwayScore] = CanEqual.derived
  implicit val awayScoreEncoder: JsonEncoder[AwayScore] = JsonEncoder.int
  implicit val awayScoreDecoder: JsonDecoder[AwayScore] = JsonDecoder.int
}

object HomeElos {

  opaque type HomeElo = Float

  object HomeElo {

    def apply(value: Float): HomeElo = value

    def unapply(awayElo: HomeElo): Float = awayElo
  }

  given CanEqual[HomeElo, HomeElo] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[HomeElo] = JsonEncoder.float
  implicit val awayTeamDecoder: JsonDecoder[HomeElo] = JsonDecoder.float
}

object AwayElos {

  opaque type AwayElo = Float

  object AwayElo {

    def apply(value: Float): AwayElo = value

    def unapply(awayElo: AwayElo): Float = awayElo
  }

  given CanEqual[AwayElo, AwayElo] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[AwayElo] = JsonEncoder.float
  implicit val awayTeamDecoder: JsonDecoder[AwayElo] = JsonDecoder.float
}

object HomeRartingProbs {

  opaque type HomeRatingProb = Float

  object HomeRatingProb {

    def apply(value: Float): HomeRatingProb = value

    def unapply(awayElo: HomeRatingProb): Float = awayElo
  }

  given CanEqual[HomeRatingProb, HomeRatingProb] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[HomeRatingProb] = JsonEncoder.float
  implicit val awayTeamDecoder: JsonDecoder[HomeRatingProb] = JsonDecoder.float
}

object AwayRartingProbs {

  opaque type AwayRatingProb = Float

  object AwayRatingProb {

    def apply(value: Float): AwayRatingProb = value

    def unapply(awayElo: AwayRatingProb): Float = awayElo
  }

  given CanEqual[AwayRatingProb, AwayRatingProb] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[AwayRatingProb] = JsonEncoder.float
  implicit val awayTeamDecoder: JsonDecoder[AwayRatingProb] = JsonDecoder.float
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

final case class Game(
    date: GameDate, // date
    season: SeasonYear, // season
    playoffRound: PlayoffRound, // playoff
    homeTeam: HomeTeam, // team1
    awayTeam: AwayTeam, // team2
    homeScore: HomeScore, // score1
    awayScore: AwayScore, // score2
    homeElo: HomeElo, // elo1_post
    awayElo: AwayElo, // elo2_post
    homeRatingProb: HomeRatingProb, // rating_prob1
    awayRatingProb: AwayRatingProb // rating_prob2
)

object Game {

  given CanEqual[Game, Game] = CanEqual.derived
  implicit val gameEncoder: JsonEncoder[Game] = DeriveJsonEncoder.gen[Game]
  implicit val gameDecoder: JsonDecoder[Game] = DeriveJsonDecoder.gen[Game]

  def unapply(game: Game): (GameDate, SeasonYear, PlayoffRound, HomeTeam, AwayTeam, HomeScore, AwayScore, HomeElo, AwayElo, HomeRatingProb, AwayRatingProb) =
    (game.date, game.season, game.playoffRound, game.homeTeam, game.awayTeam, game.homeScore, game.awayScore, game.homeElo, game.awayElo, game.homeRatingProb, game.awayRatingProb)

  // a custom decoder from a tuple
  type Row = (String, Int, String, String, String, Int, Int, Float, Float, Float, Float)

  extension (g:Game)
    def toRow: Row =
      val (d, y, p, h, a, hs, as, he, ae, hrp, arp) = Game.unapply(g)
      (
        GameDate.unapply(d).toString,
        SeasonYear.unapply(y),
        PlayoffRound.unapply(p),
        HomeTeam.unapply(h),
        AwayTeam.unapply(a),
        HomeScore.unapply(hs),
        AwayScore.unapply(as),
        HomeElo.unapply(he),
        AwayElo.unapply(ae),
        HomeRatingProb.unapply(hrp),
        AwayRatingProb.unapply(arp)
      )

  implicit val jdbcDecoder: JdbcDecoder[Game] = JdbcDecoder[Row]().map[Game] { t =>
      val (date, season, playoff, home, away, homeScore, awayScore, homeElo, awayElo, homeRatingProb, awayRatingProb) = t
      Game(
        GameDate(LocalDate.parse(date)),
        SeasonYear(season),
        PlayoffRound(playoff),
        HomeTeam(home),
        AwayTeam(away),
        HomeScore(homeScore),
        AwayScore(awayScore),
        HomeElo(homeElo),
        AwayElo(awayElo),
        HomeRatingProb(homeRatingProb),
        AwayRatingProb(awayRatingProb)
      )
    }
}


// Create a CSVReader
println("Importing CSV to Database")
println("Initializing Reader CSV")
var reader = CSVReader.open("mlb_elo.csv")
println("Initialized Reader CSV")

var games = List[Game]()

reader.iteratorWithHeaders.map[Game](row => 
        Game(
          if (row("date").isEmpty) GameDate(LocalDate.parse("1900-01-01")) else GameDate(LocalDate.parse(row("date"))),
          if (row("season").isEmpty) SeasonYear(-1) else SeasonYear(row("season").toInt),
          if (row("playoff").isEmpty) PlayoffRound("none") else PlayoffRound(row("playoff")),
          if (row("team1").isEmpty) HomeTeam("XXX") else HomeTeam(row("team1")),
          if (row("team2").isEmpty) AwayTeam("XXX") else AwayTeam(row("team2")),
          if (row("score1").isEmpty) HomeScore(-1) else HomeScore(row("score1").toInt),
          if (row("score2").isEmpty) AwayScore(-1) else AwayScore(row("score2").toInt),
          if (row("elo1_post").isEmpty) HomeElo(-1) else HomeElo(row("elo1_post").toFloat),
          if (row("elo2_post").isEmpty) AwayElo(-1) else AwayElo(row("elo2_post").toFloat),
          if (row("rating_prob1").isEmpty) HomeRatingProb(-1) else HomeRatingProb(row("rating_prob1").toFloat),
          if (row("rating_prob2").isEmpty) AwayRatingProb(-1) else AwayRatingProb(row("rating_prob2").toFloat)
          )
        ).grouped(1000).foreach(chunk =>{ 
            println("Should insert " + chunk + " rows")
            games = games ++ chunk.toList
        })

println("Initialized Games")
games

println(games.last.toRow)
