package mlb

import zio.json._
import zio.jdbc._

import java.time.LocalDate

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

  opaque type PlayoffRound <: Int = Int

  object PlayoffRound {

    def apply(round: Int): PlayoffRound = round

    def safe(value: Int): Option[PlayoffRound] =
      Option.when(value >= 1 && value <= 4)(value)

    def unapply(playoffRound: PlayoffRound): Int = playoffRound
  }

  given CanEqual[PlayoffRound, PlayoffRound] = CanEqual.derived
  implicit val playoffRoundEncoder: JsonEncoder[PlayoffRound] = JsonEncoder.int
  implicit val playoffRoundDEncoder: JsonDecoder[PlayoffRound] = JsonDecoder.int
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

  opaque type HomeElo = Double

  object HomeElo {

    def apply(value: Double): HomeElo = value

    def unapply(awayElo: HomeElo): Double = awayElo
  }

  given CanEqual[HomeElo, HomeElo] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[HomeElo] = JsonEncoder.double
  implicit val awayTeamDecoder: JsonDecoder[HomeElo] = JsonDecoder.double
}

object AwayElos {

  opaque type AwayElo = Double

  object AwayElo {

    def apply(value: Double): AwayElo = value

    def unapply(awayElo: AwayElo): Double = awayElo
  }

  given CanEqual[AwayElo, AwayElo] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[AwayElo] = JsonEncoder.double
  implicit val awayTeamDecoder: JsonDecoder[AwayElo] = JsonDecoder.double
}

object HomeRartingProbs {

  opaque type HomeRatingProb = Double

  object HomeRatingProb {

    def apply(value: Double): HomeRatingProb = value

    def unapply(awayElo: HomeRatingProb): Double = awayElo
  }

  given CanEqual[HomeRatingProb, HomeRatingProb] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[HomeRatingProb] = JsonEncoder.double
  implicit val awayTeamDecoder: JsonDecoder[HomeRatingProb] = JsonDecoder.double
}

object AwayRartingProbs {

  opaque type AwayRatingProb = Double

  object AwayRatingProb {

    def apply(value: Double): AwayRatingProb = value

    def unapply(awayElo: AwayRatingProb): Double = awayElo
  }

  given CanEqual[AwayRatingProb, AwayRatingProb] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[AwayRatingProb] = JsonEncoder.double
  implicit val awayTeamDecoder: JsonDecoder[AwayRatingProb] = JsonDecoder.double
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
  type Row = (String, Int, Int, String, String, Int, Int, Double, Double, Double, Double)

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

val games: List[Game] = List(
  Game(GameDate(LocalDate.parse("2021-10-03")), SeasonYear(2023), PlayoffRound(-1), HomeTeam("ATL"), AwayTeam("NYM"), HomeScore(7), AwayScore(0), HomeElo(1529.0), AwayElo(1520.0), HomeRatingProb(0.631), AwayRatingProb(0.369)),
  Game(GameDate(LocalDate.parse("2021-10-03")), SeasonYear(2023), PlayoffRound(-1), HomeTeam("STL"), AwayTeam("CHC"), HomeScore(4), AwayScore(2), HomeElo(1519.0), AwayElo(1470.0), HomeRatingProb(0.631), AwayRatingProb(0.369))
)