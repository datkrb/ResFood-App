const express = require('express');
const router = express.Router();
const PaymentController = require('../controllers/payment.controller');

router.post("/sepay/create", PaymentController.create);
router.post("/sepay/webhook", PaymentController.webhook);

module.exports = router;
