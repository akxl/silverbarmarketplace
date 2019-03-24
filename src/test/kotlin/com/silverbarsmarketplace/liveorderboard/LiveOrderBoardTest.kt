package com.silverbarsmarketplace.liveorderboard


import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class LiveOrderBoardTest {

    private lateinit var liveOrderBoard: LiveOrderBoard

    @Before
    fun createFreshInstance() {
        liveOrderBoard = LiveOrderBoard()
    }

    @Test
    fun ableToAddSingleOrder() {

        val anOrder = createOrder()
        val isOrderAdded = liveOrderBoard.submitOrder(anOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isOrderAdded)

        val expectedSummaryInformation = createExpectedSummaryInformation(anOrder)
        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }

    @Test
    fun summaryBoardAccumulatesOrdersOfSimilarPrice() {

        val orderType = anyOrderType()
        val firstOrder = createOrder(orderType = orderType, pricePerKg = BigDecimal(100), orderQuantity = BigDecimal(200))
        val secondOrder = createOrder(orderType = orderType, pricePerKg = BigDecimal(100), orderQuantity = BigDecimal(300))
        val expectedSummaryInformation = createExpectedSummaryInformation(firstOrder, secondOrder)

        liveOrderBoard.submitOrder(firstOrder)
        liveOrderBoard.submitOrder(secondOrder)

        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }

    @Test
    fun eachPriceIsShownAsDifferentRows() {

        val orderType = anyOrderType()
        val firstOrder = createOrder(pricePerKg = BigDecimal(100), orderQuantity = BigDecimal(800), orderType = orderType)
        val secondOrder = createOrder(pricePerKg = BigDecimal(200), orderQuantity = BigDecimal(900), orderType = orderType)
        val expectedSummaryInformation = createExpectedSummaryInformation(firstOrder, secondOrder)

        liveOrderBoard.submitOrder(firstOrder)
        liveOrderBoard.submitOrder(secondOrder)

        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }

    @Test
    fun unableToAddInvalidOrder() {

        val singleBuyOrder = createOrder(pricePerKg = BigDecimal(-501), orderQuantity = BigDecimal(-101))

        val isFirstOrderAdded = liveOrderBoard.submitOrder(singleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_REJECTED_INVALID, isFirstOrderAdded)

        val expectedSummaryInformation = createExpectedSummaryInformation()
        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }

    @Test
    fun unableToAddDuplicateOrder() {

        val anOrder = createOrder()
        val expectedSummaryInformation = createExpectedSummaryInformation(anOrder)
        liveOrderBoard.submitOrder(anOrder)

        val hasResubmissionFailed = liveOrderBoard.submitOrder(anOrder)
        assertEquals(OrderStatus.SUBMISSION_REJECTED_DUPLICATE, hasResubmissionFailed)
        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }


    @Test
    fun ableToRunExampleGivenOnSpecSheet() {

        val orders = arrayOf(
            createOrder(orderType = OrderType.SELL, orderQuantity = BigDecimal(3.5), pricePerKg = BigDecimal(306), userId = "user1"),
            createOrder(orderType = OrderType.SELL, orderQuantity = BigDecimal(1.2), pricePerKg = BigDecimal(310), userId = "user2"),
            createOrder(orderType = OrderType.SELL, orderQuantity = BigDecimal(1.5), pricePerKg = BigDecimal(307), userId = "user3"),
            createOrder(orderType = OrderType.SELL, orderQuantity = BigDecimal(2), pricePerKg = BigDecimal(306), userId = "user4")
        )
        val expectedSummaryInformation = createExpectedSummaryInformation(*orders)

        val areAllOrdersSubmittedSuccessfully = orders.map { liveOrderBoard.submitOrder(it) }.all { it == OrderStatus.SUBMISSION_ACCEPTED }
        assertEquals(true, areAllOrdersSubmittedSuccessfully)
        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }


    @Test
    fun rejectsCancellationsOfOrdersThatDoNotExist_emptyLiveOrderBoard() {

        val anUnsubmittedOrder = createOrder()
        val expectedSummaryInformation = createExpectedSummaryInformation()

        val hasOrderCancellationBeenRejected = liveOrderBoard.cancelOrder(anUnsubmittedOrder)
        assertEquals(OrderStatus.CANCELLATION_REJECTED_NOT_FOUND, hasOrderCancellationBeenRejected)
        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }

    @Test
    fun rejectsCancellationsOfOrdersThatDoNotExist_prepopulatedLiveOrderBoard() {

        val anOrderType = anyOrderType()
        val prepopulatedOrder = createOrder(orderType = anOrderType, pricePerKg = BigDecimal(500), orderQuantity = BigDecimal(600))
        liveOrderBoard.submitOrder(prepopulatedOrder)
        val expectedSummaryInformation = createExpectedSummaryInformation(prepopulatedOrder)

        val anUnsubmittedOrder = createOrder(orderType = anOrderType, pricePerKg = BigDecimal(1), orderQuantity = BigDecimal(42))
        val hasOrderCancellationBeenRejected = liveOrderBoard.cancelOrder(anUnsubmittedOrder)
        assertEquals(OrderStatus.CANCELLATION_REJECTED_NOT_FOUND, hasOrderCancellationBeenRejected)
        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }

    @Test
    fun ableToCancelExistingOrder_removalOfEntireRow() {

        val prepolulatedOrder = createOrder()
        val expectedSummaryInformation = createExpectedSummaryInformation()
        liveOrderBoard.submitOrder(prepolulatedOrder)

        val hasOrderBeenCancelled = liveOrderBoard.cancelOrder(prepolulatedOrder)
        assertEquals(OrderStatus.CANCELLATION_ACCEPTED, hasOrderBeenCancelled)
        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }

    @Test
    fun ableToCancelExistingOrder_rowStillHasRemainingQuantity() {

        val anOrderType = anyOrderType()
        val prepopulatedOrder1 = createOrder(orderType = anOrderType, pricePerKg = BigDecimal(100), orderQuantity = BigDecimal(1000))
        val expectedSummaryInformation = createExpectedSummaryInformation(prepopulatedOrder1)

        val prepopulatedOrder2 = createOrder(orderType = anOrderType, pricePerKg = BigDecimal(100), orderQuantity = BigDecimal(100))
        liveOrderBoard.submitOrder(prepopulatedOrder2) // let order #2 go in first
        liveOrderBoard.submitOrder(prepopulatedOrder1)

        liveOrderBoard.cancelOrder(prepopulatedOrder2)
        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }

    @Test
    fun ableToCancelExistingOrder_withoutAffectingOtherRows() {

        val tenOrders = (0..9).map { createOrder() }
        val firstExpectedSummaryInformation = createExpectedSummaryInformation(*tenOrders.toTypedArray())
        tenOrders.forEach { liveOrderBoard.submitOrder(it) }
        assertEquals(firstExpectedSummaryInformation, liveOrderBoard.getSummaryInformation())

        val ordersToBeCancelled= tenOrders.filterIndexed { index, _ -> index % 2 == 0 }
        val remainingOrders = tenOrders.filter { !ordersToBeCancelled.contains(it) }
        val secondExpectedSummaryInformation = createExpectedSummaryInformation(*remainingOrders.toTypedArray())
        ordersToBeCancelled.forEach { liveOrderBoard.cancelOrder(it) }
        assertEquals(secondExpectedSummaryInformation, liveOrderBoard.getSummaryInformation())

    }

    @Test
    fun humbleAttemptAtTestingTheClassUnderConcurrentSituations() {

        // Note: I would expect that such a functionality would be used under high load in a production setting, hence there might be a need for it to be thread-safe? This is a primitive attempt at testing the class under such a situation.

        val maxNumberOfCoroutines = 10000

        val orders = runBlocking {
            (1..maxNumberOfCoroutines).map {
                async {
                    val randomLong = (1..50).toList().random().toLong()
                    val anOrder = createOrder(pricePerKg = somePossibleBigDecimal(), orderQuantity = somePossibleBigDecimal())
                    delay(randomLong)
                    liveOrderBoard.submitOrder(anOrder)
                    anOrder
                }
            }.awaitAll()
        }
        val firstExpectedInformationSummary = createExpectedSummaryInformation(*orders.toTypedArray())
        assertEquals(firstExpectedInformationSummary, liveOrderBoard.getSummaryInformation())

        // These single-threaded filtration are the bottlenecks
        val ordersToBeCancelled = orders.filterIndexed { index, _ -> index % 2 == 0 }
        val remainingOrders = orders.filter { !ordersToBeCancelled.contains(it) }
        val secondExpectedInformationSummary = createExpectedSummaryInformation(*remainingOrders.toTypedArray())

        runBlocking {
            ordersToBeCancelled.map {
                async {
                    val randomLong = (1..50).toList().random().toLong()
                    delay(randomLong)
                    liveOrderBoard.cancelOrder(it)
                }
            }
        }

        assertEquals(secondExpectedInformationSummary, liveOrderBoard.getSummaryInformation())



    }




    private fun createOrder(
        orderId: String = UUID.randomUUID().toString(),
        userId: String = UUID.randomUUID().toString(),
        orderQuantity: BigDecimal = BigDecimal(Math.abs(Math.random())),
        pricePerKg: BigDecimal = BigDecimal(Math.abs(Math.random())),
        orderType: OrderType = anyOrderType()
    ): Order {
        return Order(orderId, userId, orderQuantity, pricePerKg, orderType)
    }

    private fun anyOrderType(): OrderType {
        return OrderType.values().toList().random()
    }

    private fun createExpectedSummaryInformation(vararg orders: Order): SummaryInformation {

        val buyOrders = orders.filter { it.orderType == OrderType.BUY }
            .groupingBy { it.pricePerKg }
            .aggregate { _, accumulator: BigDecimal?, element, _ -> (accumulator ?: BigDecimal.ZERO).add(element.orderQuantity) }
            .toSortedMap(Collections.reverseOrder())

        val sellOrders = orders.filter { it.orderType == OrderType.SELL }
            .groupingBy { it.pricePerKg }
            .aggregate { _, accumulator: BigDecimal?, element, _ -> (accumulator ?: BigDecimal.ZERO).add(element.orderQuantity) }
            .toSortedMap()

        return createExpectedSummaryInformation(buyOrders, sellOrders)

    }

    private fun createExpectedSummaryInformation(
        buy: SortedMap<BigDecimal, BigDecimal> = sortedMapOf(),
        sell: SortedMap<BigDecimal, BigDecimal> = sortedMapOf()
    ): SummaryInformation {
        return SummaryInformation(buy, sell)
    }

    private fun somePossibleBigDecimal(): BigDecimal = listOf(
        BigDecimal(100),
        BigDecimal(300),
        BigDecimal(500)
    ).random()

}