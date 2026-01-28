const PaymentService = require('../services/payment.service');

const createPayment = async (req, res) => {
    try {
        const { order_id } = req.body;
        const order = await PaymentService.createZaloPayOrder(order_id);
        res.json(order);
    } catch (error) {
        console.error("Error processing payment:", error);
        if (error.message === "Order not found") {
            return res.status(404).json({ error: "Order not found" });
        }
        res.status(500).json({ error: "Internal Server Error" });
    }
};

const callback = async (req, res) => {
    let result = {};
    try {
        const { data: dataStr, mac: reqMac } = req.body;
        result = await PaymentService.handleCallback(dataStr, reqMac);
    } catch (ex) {
        result.return_code = 0; // ZaloPay server sends callback, we must return valid JSON
        result.return_message = ex.message;
    }
    // ZaloPay expects valid JSON response
    res.json(result);
};

const checkStatus = async (req, res) => {
    try {
        const { app_trans_id } = req.body;
        const result = await PaymentService.queryOrder(app_trans_id);
        res.json(result);
    } catch (error) {
        console.error("Error checking status:", error);
        res.status(500).json({ error: "Internal Server Error" });
    }
}

module.exports = {
    createPayment,
    callback,
    checkStatus
};
