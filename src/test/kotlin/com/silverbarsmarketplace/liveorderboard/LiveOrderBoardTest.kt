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
    fun ableToAddBuyOrders() {

        val singleBuyOrder = createOrder(orderType = OrderType.BUY, pricePerKg = BigDecimal(501), orderQuantity = BigDecimal(101))

        val isFirstOrderAdded = liveOrderBoard.submitOrder(singleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isFirstOrderAdded)

        val firstExpectedSummaryBoard = SummaryInformation(buy = sortedMapOf(singleBuyOrder.pricePerKg to singleBuyOrder.orderQuantity), sell = sortedMapOf())

        val firstSummaryInformation = liveOrderBoard.getSummaryInformation()
        assertEquals(firstExpectedSummaryBoard, firstSummaryInformation)

        val anotherSingleBuyOrder = createOrder(orderType = OrderType.BUY, pricePerKg = BigDecimal(501), orderQuantity = BigDecimal(101))

        val isSecondOrderAdded = liveOrderBoard.submitOrder(anotherSingleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isSecondOrderAdded)

        val secondExpectedSummaryBoard = SummaryInformation(buy = sortedMapOf(singleBuyOrder.pricePerKg to singleBuyOrder.orderQuantity.add(anotherSingleBuyOrder.orderQuantity)), sell = sortedMapOf())
        val secondSummaryInformation = liveOrderBoard.getSummaryInformation()
        assertEquals(secondExpectedSummaryBoard, secondSummaryInformation)

        val yetAnotherSingleBuyOrder = createOrder(orderType = OrderType.BUY, pricePerKg = BigDecimal(601), orderQuantity = BigDecimal(101))

        val isThirdOrderAdded = liveOrderBoard.submitOrder(yetAnotherSingleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isThirdOrderAdded)

        val thirdExpectedSummaryBoard = SummaryInformation(buy = sortedMapOf(yetAnotherSingleBuyOrder.pricePerKg to yetAnotherSingleBuyOrder.orderQuantity, singleBuyOrder.pricePerKg to singleBuyOrder.orderQuantity.add(anotherSingleBuyOrder.orderQuantity)), sell = sortedMapOf())
        val thirdSummaryInformation = liveOrderBoard.getSummaryInformation()
        assertEquals(thirdExpectedSummaryBoard, thirdSummaryInformation)

    }

    @Test
    fun unableToAddOrderWithSameOrderNumber() {

        val singleBuyOrder = createOrder(orderType = OrderType.BUY, pricePerKg = BigDecimal(-501), orderQuantity = BigDecimal(-101))

        val isFirstOrderAdded = liveOrderBoard.submitOrder(singleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_REJECTED_INVALID, isFirstOrderAdded)

    }

    private fun createOrder(
        orderId: String = UUID.randomUUID().toString(),
        userId: String = UUID.randomUUID().toString(),
        orderQuantity: BigDecimal = BigDecimal(Math.abs(Math.random())),
        pricePerKg: BigDecimal = BigDecimal(Math.abs(Math.random())),
        orderType: OrderType = OrderType.values().toList().random()
    ): Order {
        return Order(orderId, userId, orderQuantity, pricePerKg, orderType)
    }

}