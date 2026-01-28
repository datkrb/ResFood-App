const PaymentService = require('../services/payment.service');

const create = async (req, res) => {
    try {
        const { orderId } = req.body;
        const payment = await PaymentService.createSepayPayment(orderId);
        res.status(201).json(payment);
    }
    catch (error) {
        res.status(500).json({ error: error.message });
    }
}

const webhook = async (req, res) => {
    try {
        const webhookData = req.body;
        const result = await PaymentService.handleSepayWebhook(webhookData);
        res.json({ success: true });
    } catch (error) {
        console.error("Lỗi Webhook:", error);
        res.json({ success: true });
    }
}

module.exports = {
    create,
    webhook
};
