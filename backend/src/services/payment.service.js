const CryptoJS = require('crypto-js');
const moment = require('moment');
const { db } = require('../config/firebase.config');
const zalopayConfig = require('../config/zalopay.config');

const generateMac = (data, key1) => {
    const dataString = data.app_id + '|' + data.app_trans_id + '|' + data.app_user + '|' + data.amount + '|' +
        data.app_time + '|' + data.embed_data + '|' + data.item;

    return CryptoJS.HmacSHA256(dataString, key1).toString();
}

const createZaloPayOrder = async (order_id) => {
    const orderDoc = await db.collection('orders').doc(order_id).get();
    if (!orderDoc.exists) {
        throw new Error("Order not found");
    }
    const orderData = orderDoc.data();

    // get data from order
    const userId = orderData.userId;
    const items = orderData.items;
    const amount = orderData.total;
    const embed_data = { redirecturl: "demozpdk://app" }; // khop voi scheme trong AndroidManifest
    const transID = Math.floor(Math.random() * 1000000);
    const description = `Thanh toán đơn hàng #${transID}`;

    const order = {
        app_id: parseInt(zalopayConfig.app_id),
        app_trans_id: `${moment().format('YYMMDD')}_${transID}`,
        app_user: `${userId}`,
        app_time: Date.now(), // milliseconds
        item: JSON.stringify(items),
        embed_data: JSON.stringify(embed_data),
        amount: amount,
        description: description,
        bank_code: '',
        callback_url: zalopayConfig.callback_url
    };

    // Calculate MAC
    const data = zalopayConfig.app_id + "|" + order.app_trans_id + "|" + order.app_user + "|" + order.amount + "|" + order.app_time + "|" + order.embed_data + "|" + order.item;
    order.mac = CryptoJS.HmacSHA256(data, zalopayConfig.key1).toString();

    // Save app_trans_id to order to query status later
    await db.collection('orders').doc(order_id).update({
        app_trans_id: order.app_trans_id
    });

    try {
        const axios = require('axios');
        const result = await axios.post(zalopayConfig.endpoint, null, { params: order });
        return result.data;
    } catch (err) {
        console.log(err);
        throw new Error("ZaloPay API Error");
    }
};

const handleCallback = async (dataStr, reqMac) => {
    const mac = CryptoJS.HmacSHA256(dataStr, zalopayConfig.key2).toString();

    // verify mac
    if (reqMac !== mac) {
        return { return_code: -1, return_message: "mac not equal" };
    }

    const dataJson = JSON.parse(dataStr);
    console.log("update order's status = success where app_trans_id =", dataJson['app_trans_id']);

    const ordersSnapshot = await db.collection('orders').where('app_trans_id', '==', dataJson['app_trans_id']).get();

    if (ordersSnapshot.empty) {
        return { return_code: 0, return_message: "order not found" };
    }

    const batch = db.batch();
    ordersSnapshot.forEach(doc => {
        batch.update(doc.ref, { status: 'PAID' });
    });
    await batch.commit();

    return { return_code: 1, return_message: "success" };
};

const queryOrder = async (app_trans_id) => {
    const postData = {
        app_id: zalopayConfig.app_id,
        app_trans_id: app_trans_id,
    };

    const data = postData.app_id + "|" + postData.app_trans_id + "|" + zalopayConfig.key1;
    postData.mac = CryptoJS.HmacSHA256(data, zalopayConfig.key1).toString();

    const postConfig = {
        method: 'post',
        url: 'https://sb-openapi.zalopay.vn/v2/query',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        data: new URLSearchParams(postData).toString() // qs.stringify(postData)
    };

    try {
        const axios = require('axios');
        const result = await axios(postConfig);

        // If ZaloPay says success, sync our DB
        if (result.data && result.data.return_code === 1) {
            const ordersSnapshot = await db.collection('orders').where('app_trans_id', '==', app_trans_id).get();
            if (!ordersSnapshot.empty) {
                const batch = db.batch();
                ordersSnapshot.forEach(doc => {
                    if (doc.data().status !== 'PAID') {
                        batch.update(doc.ref, { status: 'PAID' });
                    }
                });
                await batch.commit();
            }
        }

        return result.data;
    } catch (error) {
        console.log(error);
        throw new Error("ZaloPay Query Error");
    }
}

module.exports = {
    createZaloPayOrder,
    handleCallback,
    queryOrder
};
