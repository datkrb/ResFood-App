const express = require('express');
const router = express.Router();
const PaymentController = require('../controllers/payment.controller');

router.post('/zalopay', PaymentController.createPayment);
router.post('/zalopay/callback', PaymentController.callback);
router.post('/zalopay/check-status', PaymentController.checkStatus);

module.exports = router;
