require('dotenv').config();

const zalopayConfig = {
    app_id: "2553",
    key1: "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL",
    key2: "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz",
    endpoint: "https://sb-openapi.zalopay.vn/v2/create",
    callback_url: `${process.env.ZALOPAY_CALLBACK_URL}/api/v1/payments/zalopay/callback`
};

module.exports = zalopayConfig;
