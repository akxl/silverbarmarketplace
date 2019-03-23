package com.silverbarsmarketplace.liveorderboard


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
            createOrder(orderType = OrderType.SELL, orderQuantity = BigDecimal(1.5), pricePerKg = BigDecimal(307)),
            createOrder(orderType = OrderType.SELL, orderQuantity = BigDecimal(2), pricePerKg = BigDecimal(306))
        )
        val expectedSummaryInformation = createExpectedSummaryInformation(*orders)

        val areAllOrdersSubmittedSuccessfully = orders.map { liveOrderBoard.submitOrder(it) }.all { it == OrderStatus.SUBMISSION_ACCEPTED }
        assertEquals(true, areAllOrdersSubmittedSuccessfully)
        assertEquals(expectedSummaryInformation, liveOrderBoard.getSummaryInformation())


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

}