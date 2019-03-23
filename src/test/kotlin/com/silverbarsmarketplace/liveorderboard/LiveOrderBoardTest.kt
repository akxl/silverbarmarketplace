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

        val singleBuyOrder = Order(
            orderId = UUID.randomUUID().toString(),
            userId = "John Smith",
            orderQuantity = BigDecimal(101),
            pricePerKg = BigDecimal(501),
            orderType = OrderType.BUY
        )

        val isFirstOrderAdded = liveOrderBoard.submitOrder(singleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isFirstOrderAdded)

        val firstExpectedSummaryBoard = SummaryInformation(buy = sortedMapOf(singleBuyOrder.pricePerKg to singleBuyOrder.orderQuantity), sell = sortedMapOf())

        val firstSummaryInformation = liveOrderBoard.getSummaryInformation()
        assertEquals(firstExpectedSummaryBoard, firstSummaryInformation)

        val anotherSingleBuyOrder = Order(
            orderId = UUID.randomUUID().toString(),
            userId = "Jane Doe",
            orderQuantity = BigDecimal(101),
            pricePerKg = BigDecimal(501),
            orderType = OrderType.BUY
        )

        val isSecondOrderAdded = liveOrderBoard.submitOrder(anotherSingleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isSecondOrderAdded)

        val secondExpectedSummaryBoard = SummaryInformation(buy = sortedMapOf(singleBuyOrder.pricePerKg to singleBuyOrder.orderQuantity.add(anotherSingleBuyOrder.orderQuantity)), sell = sortedMapOf())
        val secondSummaryInformation = liveOrderBoard.getSummaryInformation()
        assertEquals(secondExpectedSummaryBoard, secondSummaryInformation)

        val yetAnotherSingleBuyOrder = Order(
            orderId = UUID.randomUUID().toString(),
            userId = "Muhammad Ali",
            orderQuantity = BigDecimal(101),
            pricePerKg = BigDecimal(601),
            orderType = OrderType.BUY
        )

        val isThirdOrderAdded = liveOrderBoard.submitOrder(yetAnotherSingleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_ACCEPTED, isThirdOrderAdded)

        val thirdExpectedSummaryBoard = SummaryInformation(buy = sortedMapOf(yetAnotherSingleBuyOrder.pricePerKg to yetAnotherSingleBuyOrder.orderQuantity, singleBuyOrder.pricePerKg to singleBuyOrder.orderQuantity.add(anotherSingleBuyOrder.orderQuantity)), sell = sortedMapOf())
        val thirdSummaryInformation = liveOrderBoard.getSummaryInformation()
        assertEquals(thirdExpectedSummaryBoard, thirdSummaryInformation)

    }

    @Test
    fun unableToAddOrderWithSameOrderNumber() {

        val singleBuyOrder = Order(
            orderId = UUID.randomUUID().toString(),
            userId = "John Smith",
            orderQuantity = BigDecimal(-101),
            pricePerKg = BigDecimal(-501),
            orderType = OrderType.SELL
        )

        val isFirstOrderAdded = liveOrderBoard.submitOrder(singleBuyOrder)
        assertEquals(OrderStatus.SUBMISSION_REJECTED_INVALID, isFirstOrderAdded)

    }

}