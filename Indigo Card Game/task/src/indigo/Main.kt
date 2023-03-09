package indigo


import java.util.Scanner
import kotlin.system.exitProcess

const val ranks = "A 2 3 4 5 6 7 8 9 10 J Q K" //sets of ranks and suits
const val suits = "♠ ♥ ♦ ♣"

class Card(val rank: String, val suit: String) {
    override fun toString(): String {
        return rank + suit
    }
}

open class Player {

    var cardsInHand = mutableListOf<Card>()//cards which could be played
    var wonCards = mutableListOf<Card>()//cards which already claimed by player
    var score: Int = 0 //score from won cards

    fun countWonCards(deckWithPlayedCards: MutableList<Card>) { // add cards from deck to cards that are won by player
        score += deckWithPlayedCards.count { card ->
            ranks.replaceAfter("A", ranks.substringAfter("9"))
                .contains(card.rank) // only "A 10 J K Q" each gives 1 score
        }
        wonCards += deckWithPlayedCards
        deckWithPlayedCards.clear()
    }

    fun getCardsFromDeck(deck: MutableList<Card>) {
        cardsInHand = get(deck, 6)
    }

    open fun getFullInfo() {//print out cards in player's hand
        print("Cards in hand: ")
        cardsInHand.forEach { print("${cardsInHand.indexOf(it) + 1}" + ")" + it + " ") }
        println()
    }

    open fun turn(deckWithPlayedCards: MutableList<Card>): Boolean { //return answer on question: does player win in this turn?
        while (true) { //infinite loop waiting for correct number
            println("Choose a card to play (1-${cardsInHand.size}):")

            val scanner = Scanner(System.`in`)
            if (scanner.hasNextInt()) {
                val number = scanner.nextInt()
                if (number in 1..cardsInHand.size) {
                    if (deckWithPlayedCards.isNotEmpty()) {//if there are some cards on table you could win them or just put a card on the table
                        if (cardsInHand[number - 1].rank == deckWithPlayedCards.last().rank
                            || cardsInHand[number - 1].suit == deckWithPlayedCards.last().suit
                        ) {//if suit or rank of last card on table correspond with properties of last played card
                            deckWithPlayedCards.add(cardsInHand.removeAt(number - 1))
                            countWonCards(deckWithPlayedCards)
                            println("Player wins cards")
                            return true
                        }
                    } //if there aren't any cards on table or if you could not win you could only put a card on the table
                    deckWithPlayedCards.add(cardsInHand.removeAt(number - 1))
                    println()
                    return false

                }
            }
            if (scanner.nextLine() == "exit") {
                exit()
            }
        }
    }
}

class PlayerAI : Player() {
    override fun getFullInfo() {
        println(cardsInHand.joinToString(" "))
    }

    private fun strategy(deckWithPlayedCards: MutableList<Card>): Int {
        val candidatesRank = mutableListOf<Card>() //special decks for cards which can win turn
        val candidatesSuit = mutableListOf<Card>()

        val cardsWithSameSuit = mutableListOf<Card>()
        val cardsWithSameRank = mutableListOf<Card>()

        if (cardsInHand.size == 1) {
            return 0
        }
        for (i in cardsInHand) { //finding candidate cards
            if (deckWithPlayedCards.size == 0) break
            if (i.rank == deckWithPlayedCards.last().rank) {
                candidatesRank.add(i)
            }
            if (i.suit == deckWithPlayedCards.last().suit) {
                candidatesSuit.add(i)
            }
        }

        if (candidatesRank.size + candidatesSuit.size >= 1) { // if there are some candidate cards return random index of them in cardsInHand

            return if (candidatesRank.size <= candidatesSuit.size) { //single suit candidate is prioritized if there are not paired rank candidates
                cardsInHand.indexOf(candidatesSuit.random())
            } else {//if it does not have multiple suit candidates it should play multiple rank candidates or single rank candidate if there are no suit candidate
                cardsInHand.indexOf(candidatesRank.random())
            }
        }

        if (deckWithPlayedCards.size == 0 || candidatesRank.size + candidatesSuit.size == 0) {//if there are no cards on the table and no candidate cards
//finding cards with same suits and ranks in hand and recording them in cardsWithSameSuit and ...
            var suit: String
            var rank: String

            for (i in cardsInHand.indices) {
                if (i == cardsInHand.lastIndex) {
                    break
                }
                suit = cardsInHand[i].suit
                rank = cardsInHand[i].rank
                for (j in i + 1..cardsInHand.lastIndex) {
                    if (cardsInHand[j].suit == suit) {
                        if (cardsWithSameSuit.contains(cardsInHand[i])) cardsWithSameSuit.add(cardsInHand[j]) else cardsWithSameSuit.addAll(
                            listOf(cardsInHand[i], cardsInHand[j])
                        )
                    }
                    if (cardsInHand[j].rank == rank) {
                        if (cardsWithSameRank.contains(cardsInHand[i])) cardsWithSameRank.add(cardsInHand[j]) else cardsWithSameRank.addAll(
                            listOf(cardsInHand[i], cardsInHand[j])
                        )

                    }
                }

            }
        }

        return if (cardsWithSameSuit.size != 0) cardsInHand.indexOf(cardsWithSameSuit.random())//if there are some cards with same suit and , it will throw one of them randomly
        else if (cardsWithSameRank.size != 0) cardsInHand.indexOf(cardsWithSameRank.random())//...
        else cardsInHand.indices.random()


    }

    override fun turn(deckWithPlayedCards: MutableList<Card>): Boolean {//same with player's method but you don't need to write number: there is a strategy for picking it

        val number = strategy(deckWithPlayedCards)

        if (deckWithPlayedCards.isNotEmpty()) {
            if (cardsInHand[number].rank == deckWithPlayedCards.last().rank
                || cardsInHand[number].suit == deckWithPlayedCards.last().suit
            ) {
                deckWithPlayedCards.add(cardsInHand.removeAt(number))
                println("Computer plays ${deckWithPlayedCards.last()}")
                countWonCards(deckWithPlayedCards)
                println("Computer wins cards")
                return true
            }

        }
        deckWithPlayedCards.add(cardsInHand.removeAt(number))
        println("Computer plays ${deckWithPlayedCards.last()}\n")
        return false

    }
}

fun reset(): MutableList<Card> {   //for resetting pack of cards
    return ranks.split(" ").flatMap { rank -> suits.split(" ").map { suit -> Card(rank, suit) } }.toMutableList()
}

fun get(
    cards: MutableList<Card>,
    number: Int
): MutableList<Card> { //for getting inputted number of card from cards pack
    val pack = mutableListOf<Card>()
    if (number <= cards.size) {
        repeat(number) {
            pack.add(cards.removeLast())
        }
    }
    return pack
}

fun exit() {
    println("Game over")
    exitProcess(0)
}

fun result(playerAI: PlayerAI, player: Player) {//outputting result
    println("Score: Player ${player.score} - Computer ${playerAI.score}")
    println("Cards: Player ${player.wonCards.size} - Computer ${playerAI.wonCards.size}")
}

fun main() {

    println("Indigo Card Game")
    val player = Player()
    val playerAI = PlayerAI()
    var input: String //variable for answer
    var nextTurn: String //variable which show who`s turn is next
    var lastWinner = "Computer"
    while (true) { //infinite loop which waiting for correct answer
        println("Play first?")
        input = readln()
        if (input.lowercase() == "yes") {//by lowercase() input becomes no case-sensitive
            nextTurn = "Computer"
            break
        }
        if (input.lowercase() == "no") {
            nextTurn = "Player"
            break
        }
    }
    val deck = reset() //creating deck
    deck.shuffle() //shuffling cards

    val deckWithPlayedCards = get(deck, 4) //creating new MutableList for cards on the table representation
    println("Initial cards on the table: ${deckWithPlayedCards.joinToString(" ")}")
    println()

    while (true) { //loop for game
        println(
            if (deckWithPlayedCards.size != 0) "${deckWithPlayedCards.size} cards on the table, and the top card is ${deckWithPlayedCards.last()}"
            else "No cards on the table"
        )

        if (playerAI.wonCards.size + player.wonCards.size + deckWithPlayedCards.size == 52) {//when all cards are played

            if (lastWinner == "Computer") { //if there is some played cards on the table last winner gets them
                playerAI.countWonCards(deckWithPlayedCards)
            } else player.countWonCards(deckWithPlayedCards)
            if (playerAI.wonCards.size > player.wonCards.size) { //who has more cards gets additional 3 score
                playerAI.score += 3
            } else if (playerAI.wonCards.size == player.wonCards.size) {
                if (nextTurn == "Player") {
                    playerAI.score += 3
                } else {
                    player.score += 3
                }
            } else player.score += 3

            result(playerAI, player)
            exit()

        } else {//when there are some cards in hands or in deck
            if (nextTurn == "Player" && playerAI.cardsInHand.size == 0) {//when computer got turn, and it does not have cards - it takes it
                playerAI.getCardsFromDeck(deck)
            } else if (nextTurn == "Computer" && player.cardsInHand.size == 0) {//...
                player.getCardsFromDeck(deck)
            }

            nextTurn = if (nextTurn == "Player") {
                playerAI.getFullInfo() // prints cards in computer's hand
                if (playerAI.turn(deckWithPlayedCards)) {//if player win turn outputs resulting score and cards
                    lastWinner = "Computer"
                    result(playerAI, player)
                    println()
                }
                "Computer"//changing player
            } else {
                player.getFullInfo()
                if (player.turn(deckWithPlayedCards)) {
                    lastWinner = "Player"
                    result(playerAI, player)
                    println()
                }
                "Player"
            }
        }
    }
}
