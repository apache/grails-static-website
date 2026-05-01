package demo

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.ReadOnly
import groovy.transform.CompileDynamic

@CompileDynamic
@ReadOnly
class QueryService {

    // tag::findByProp[]
    Game queryGame(String name) {
        Game.findByName(name)
    }
    // end::findByProp[]

    // tag::findAllByProp[]
    List<Game> queryGamesWithAverageDuration(Integer averageDuration) {
        Game.findAllByAverageDuration(averageDuration)
    }
    // end::findAllByProp[]

    // tag::findAllNotEqual[]
    List<Game> queryGamesNotConsideredStrategy() {
        // General case: using the NotEqual comparator.
        Game.findAllByStrategyNotEqual(true)    // <1>

        // Special case: using exceptional form for Boolean properties.
        Game.findAllNotStrategy()    // <2>
    }
    // end::findAllNotEqual[]

    // tag::findAllByLessThan[]
    List<Game> queryGamesExpectedShorterThan(Integer duration) {
        Game.findAllByAverageDurationLessThan(duration)
    }
    // end::findAllByLessThan[]

    // tag::findAllByGreaterThan[]
    List<Game> queryGamesRatedMoreThan(BigDecimal rating) {
        Game.findAllByRatingGreaterThan(rating)
    }
    // end::findAllByGreaterThan[]

    // tag::countByGreaterThan[]
    int queryHowManyGamesRatedAtLeast(BigDecimal rating) {
        Game.countByRatingGreaterThanEquals(rating)
    }
    // end::countByGreaterThan[]

	// tag::findByBetween[]
    List<Game> queryMatchesPlayedBetweenDates(Date startDate, Date finishDate) {
        Match.findAllByStartedBetween(startDate, finishDate)
    }
	// end::findByBetween[]

    // tag::findByRange[]
    int queryHowManyScoresWithinRange(Range range) {
        Score.countByScoreInRange(range)
    }
	// end::findByRange[]

    // tag::findByLike[]
    List<Player> queryPlayersWithLastName(String lastName) {
        Player.findAllByNameLike("% ${lastName}")
    }
	// end::findByLike[]

    // tag::findByIlike[]
    List<Mechanic> queryMechanicsContaining(String text) {
        Mechanic.findAllByNameIlike("%${text}%")
    }
	// end::findByIlike[]

    // tag::findByRlike[]
    List<Game> queryGamesMatching(String pattern) {
        Game.findAllByNameRlike(pattern)      // Rlike: not universally supported
    }
	// end::findByRlike[]

    // tag::findByNull[]
    int queryHowManyMatchesInProgress() {
        Match.countByFinishedIsNull()
    }
	// end::findByNull[]

    // tag::findByNotNull[]
    int queryHowManyMatchesCompleted() {
        Match.countByFinishedIsNotNull()
    }
	// end::findByNotNull[]

    // tag::findStringInList[]
    List<Game> queryGamesForNames(List<String> names) {
        Game.findAllByNameInList(names)
    }
    // end::findStringInList[]

    // tag::findDomainInList[]
    List<Match> queryMatchesForGames(List<Game> games) {
        Match.findAllByGameInList(games)
    }
    // end::findDomainInList[]

    // tag::findNotInList[]
    List<Game> queryGamesOtherThan(List<Game> games) {
        Game.findAllByNameNotInList(games*.name)
    }
    // end::findNotInList[]

    // tag::findCombinatorAnd[]
    int queryHowManyGamesSupportPlayerCount(Integer playerCount) {
        Game.countByMinPlayersLessThanEqualsAndMaxPlayersGreaterThanEquals(playerCount, playerCount)
    }
	// end::findCombinatorAnd[]

    // tag::findCombinatorAnd2[]
    List<Game> queryGamesSupportExactPlayerCount(Integer playerCount) {
        Game.findAllByMinPlayersAndMaxPlayers(playerCount, playerCount)
    }
	// end::findCombinatorAnd2[]

    // tag::findCombinatorOr[]
    List<Game> queryGamesConsideredFamilyOrParty() {
        Game.findAllByFamilyOrParty(true, true)
    }
	// end::findCombinatorOr[]

    // tag::namedQueryChaining[]
    List<Game> queryGamesWithMechanicNoLongerThanDuration(Mechanic mechanic, int duration) {
        // Games provides a named query, 'gamesWithMechanic', to find all games that employ the provided game mechanic.
        // Dynamic finders can be chained onto named queries to narrow the results.
        Game.gamesWithMechanic(mechanic).findAllByAverageDurationLessThan(duration)
    }
    // end::namedQueryChaining[]

    // tag::detachedCriteriaChaining[]
    List<Game> queryGamesInCategoryWithAverageDuration(Category category, int duration) {
        // Here is a detached criteria to find all games within the specific category.
        DetachedCriteria<Game> detachedCriteria = new DetachedCriteria(Game).build {
            categories {
                eq 'id', category.id
            }
        }

        // Dynamic finders can be chained onto detached criteria to narrow the results.
        detachedCriteria.findAllByAverageDuration(duration)
    }
    // end::detachedCriteriaChaining[]

}
